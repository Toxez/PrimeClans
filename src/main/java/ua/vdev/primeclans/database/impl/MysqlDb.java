package ua.vdev.primeclans.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ua.vdev.primeclans.database.DbCreds;
import ua.vdev.primeclans.glow.GlowColor;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.primeclans.perm.ClanPerm;
import ua.vdev.vlibapi.util.scheduler.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class MysqlDb extends SqlDb {

    public MysqlDb(DbCreds creds) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + creds.host() + ":" + creds.port() + "/" + creds.database());
        config.setUsername(creds.user());
        config.setPassword(creds.pass());
        config.setPoolName("PrimeClans-Pool");
        config.setMaximumPoolSize(10);
        config.addDataSourceProperty("cachePrepStmts", "true");
        this.ds = new HikariDataSource(config);

        createTables(
                "CONSTRAINT fk_clan FOREIGN KEY (clan_name) REFERENCES clans(name) ON DELETE CASCADE",
                "COLLATE utf8mb4_unicode_ci"
        );
    }

    @Override
    public void saveClan(Clan clan) {
        Task.async(() -> {
            try (Connection c = ds.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO clans (name, owner, level, exp, pvp_enabled, balance, glow_color) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE owner=VALUES(owner), level=VALUES(level), " +
                                 "exp=VALUES(exp), pvp_enabled=VALUES(pvp_enabled), balance=VALUES(balance), " +
                                 "glow_color=VALUES(glow_color)")) {
                ps.setString(1, clan.name());
                ps.setString(2, clan.owner().toString());
                ps.setInt(3, clan.level());
                ps.setLong(4, clan.exp());
                ps.setInt(5, clan.pvpEnabled() ? 1 : 0);
                ps.setDouble(6, clan.balance());
                ps.setString(7, Optional.ofNullable(clan.glowColor()).map(GlowColor::toHex).orElse(null));
                ps.executeUpdate();
                addMember(clan.name(), clan.owner());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void addMember(String clanName, UUID uuid) {
        Task.async(() -> {
            try (Connection c = ds.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO members (uuid, clan_name) VALUES (?, ?) " +
                                 "ON DUPLICATE KEY UPDATE clan_name = VALUES(clan_name)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, clanName);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void setMemberGlowColor(UUID uuid, GlowColor color) {
        Task.async(() -> {
            try (Connection c = ds.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO member_glow_colors (uuid, color) VALUES (?, ?) " +
                                 "ON DUPLICATE KEY UPDATE color = VALUES(color)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, color.toHex());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void addMemberPerm(UUID uuid, ClanPerm perm) {
        Task.async(() -> {
            try (Connection c = ds.getConnection();
                 PreparedStatement ps = c.prepareStatement("INSERT IGNORE INTO member_perms (uuid, perm) VALUES (?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, perm.name());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}