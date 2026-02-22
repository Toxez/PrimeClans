package ua.vdev.primeclans.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.glow.GlowColor;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.primeclans.perm.ClanPerm;
import ua.vdev.vlibapi.util.scheduler.Task;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class SqliteDb extends SqlDb {

    public SqliteDb() {
        File file = new File(PrimeClans.getInstance().getDataFolder(), "data.db");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setPoolName("PrimeClans-Pool");
        config.setMaximumPoolSize(1);
        this.ds = new HikariDataSource(config);

        createTables(
                "FOREIGN KEY(clan_name) REFERENCES clans(name) ON DELETE CASCADE",
                "COLLATE NOCASE"
        );
    }

    @Override
    public void saveClan(Clan clan) {
        Task.async(() -> {
            try (Connection c = ds.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT OR REPLACE INTO clans (name, owner, level, exp, pvp_enabled, balance, glow_color) " + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
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
                 PreparedStatement ps = c.prepareStatement("INSERT OR REPLACE INTO members (uuid, clan_name) VALUES (?, ?)")) {
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
                 PreparedStatement ps = c.prepareStatement("INSERT OR REPLACE INTO member_glow_colors (uuid, color) VALUES (?, ?)")) {
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
                 PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO member_perms (uuid, perm) VALUES (?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, perm.name());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}