package ua.vdev.primeclans.database.impl;

import com.zaxxer.hikari.HikariDataSource;
import ua.vdev.primeclans.database.Db;
import ua.vdev.primeclans.glow.GlowColor;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.primeclans.perm.ClanPerm;
import ua.vdev.vlibapi.util.scheduler.Task;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SqlDb implements Db {

    protected HikariDataSource ds;

    protected void createTables(String foreignKeySql, String ignoreCaseSql) {
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {

            s.execute("CREATE TABLE IF NOT EXISTS clans (" +
                    "name VARCHAR(16) PRIMARY KEY " + ignoreCaseSql + ", " +
                    "owner VARCHAR(36) NOT NULL, " +
                    "level INT DEFAULT 1, " +
                    "exp BIGINT DEFAULT 0, " +
                    "pvp_enabled INTEGER DEFAULT 0, " +
                    "balance DOUBLE DEFAULT 0, " +
                    "glow_color VARCHAR(7) DEFAULT NULL)");

            s.execute("CREATE TABLE IF NOT EXISTS members (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "clan_name VARCHAR(16) " + ignoreCaseSql + ", " +
                    foreignKeySql + ")");

            s.execute("CREATE INDEX IF NOT EXISTS idx_members_clan ON members(clan_name)");

            s.execute("CREATE TABLE IF NOT EXISTS member_glow_colors (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "color VARCHAR(7) NOT NULL)");

            s.execute("CREATE TABLE IF NOT EXISTS member_perms (" +
                    "uuid VARCHAR(36) NOT NULL, " +
                    "perm VARCHAR(50) NOT NULL, " +
                    "PRIMARY KEY (uuid, perm))");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<Map<String, Clan>> loadAll() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Clan> map = new ConcurrentHashMap<>();

            try (Connection c = ds.getConnection()) {

                try (PreparedStatement ps = c.prepareStatement("SELECT * FROM clans")) {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String name = rs.getString("name");
                        UUID owner = UUID.fromString(rs.getString("owner"));
                        int level = rs.getInt("level");
                        long exp = rs.getLong("exp");
                        boolean pvp = rs.getBoolean("pvp_enabled");
                        double bal = rs.getDouble("balance");
                        GlowColor color = GlowColor.fromHex(rs.getString("glow_color")).orElse(null);

                        map.put(name.toLowerCase(), new Clan(name, owner, new HashSet<>(Set.of(owner)), level, exp, pvp, bal, color));
                    }
                }

                try (PreparedStatement ps = c.prepareStatement("SELECT * FROM members")) {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String clanName = rs.getString("clan_name");
                        if (clanName == null) continue;
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        Optional.ofNullable(map.get(clanName.toLowerCase()))
                                .ifPresent(clan -> clan.members().add(uuid));
                    }
                }

                Map<UUID, GlowColor> memberColors = new HashMap<>();
                try (PreparedStatement ps = c.prepareStatement("SELECT * FROM member_glow_colors")) {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        GlowColor.fromHex(rs.getString("color"))
                                .ifPresent(col -> memberColors.put(uuid, col));
                    }
                }

                Map<UUID, Set<ClanPerm>> memberPerms = new HashMap<>();
                try (PreparedStatement ps = c.prepareStatement("SELECT * FROM member_perms")) {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        ClanPerm.of(rs.getString("perm"))
                                .ifPresent(perm -> memberPerms
                                        .computeIfAbsent(uuid, k -> new HashSet<>())
                                        .add(perm));
                    }
                }

                if (!memberColors.isEmpty() || !memberPerms.isEmpty()) {
                    Map<UUID, String> uuidToClan = new HashMap<>();
                    map.forEach((lower, clan) ->
                            clan.members().forEach(m -> uuidToClan.put(m, lower)));

                    memberColors.forEach((uuid, color) ->
                            Optional.ofNullable(uuidToClan.get(uuid))
                                    .map(map::get)
                                    .ifPresent(clan -> map.put(
                                            clan.name().toLowerCase(),
                                            clan.withMemberColor(uuid, color))));

                    memberPerms.forEach((uuid, perms) -> {
                        String lower = uuidToClan.get(uuid);
                        if (lower == null) return;
                        Clan clan = map.get(lower);
                        if (clan == null) return;
                        Clan updated = clan;
                        for (ClanPerm perm : perms) {
                            updated = updated.withMemberPerm(uuid, perm);
                        }
                        map.put(lower, updated);
                    });
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return map;
        });
    }

    @Override
    public void deleteClan(String name) {
        Task.async(() -> {
            try (Connection c = ds.getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM clans WHERE name = ?")) {
                ps.setString(1, name);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void removeMember(UUID uuid) {
        Task.async(() -> {
            try (Connection c = ds.getConnection()) {
                for (String table : List.of("members", "member_glow_colors", "member_perms")) {
                    try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + table + " WHERE uuid = ?")) {
                        ps.setString(1, uuid.toString());
                        ps.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void removeMemberGlowColor(UUID uuid) {
        Task.async(() -> {
            try (Connection c = ds.getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM member_glow_colors WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void removeMemberPerm(UUID uuid, ClanPerm perm) {
        Task.async(() -> {
            try (Connection c = ds.getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM member_perms WHERE uuid = ? AND perm = ?")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, perm.name());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void removeMemberPerms(UUID uuid) {
        Task.async(() -> {
            try (Connection c = ds.getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM member_perms WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void close() {
        if (ds != null && !ds.isClosed()) ds.close();
    }
}