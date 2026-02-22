package ua.vdev.primeclans.database;

import ua.vdev.primeclans.glow.GlowColor;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.primeclans.perm.ClanPerm;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Db {
    CompletableFuture<Map<String, Clan>> loadAll();
    void saveClan(Clan clan);
    void deleteClan(String name);
    void addMember(String clanName, UUID uuid);
    void removeMember(UUID uuid);
    void setMemberGlowColor(UUID uuid, GlowColor color);
    void removeMemberGlowColor(UUID uuid);
    void addMemberPerm(UUID uuid, ClanPerm perm);
    void removeMemberPerm(UUID uuid, ClanPerm perm);
    void removeMemberPerms(UUID uuid);
    void close();
}