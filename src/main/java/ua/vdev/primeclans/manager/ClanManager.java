package ua.vdev.primeclans.manager;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.api.ClanProvider;
import ua.vdev.primeclans.database.Db;
import ua.vdev.primeclans.glow.GlowColor;
import ua.vdev.primeclans.glow.manager.GlowManager;
import ua.vdev.primeclans.glow.manager.GlowUpdater;
import ua.vdev.primeclans.level.ClanLevelService;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.primeclans.perm.ClanPerm;
import ua.vdev.vlibapi.player.PlayerFind;
import ua.vdev.vlibapi.util.scheduler.Task;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class ClanManager implements ClanProvider {

    private final Db db;
    private final Map<String, Clan> clans = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerToClan = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>> invites = new ConcurrentHashMap<>();
    private final Map<String, Long> dirtyClanSchedule = new ConcurrentHashMap<>();
    private static final long SAVE_DELAY_MS = 5000L;

    public void load() {
        db.loadAll().thenAccept(loaded -> {
            clans.clear();
            playerToClan.clear();
            GlowManager.clear();
            ClanLevelService levelService = PrimeClans.getInstance().getLevelService();
            loaded.forEach((lowerName, clan) -> {
                int calculated = levelService.calculateLevel(clan.exp());
                Clan fixed = calculated != clan.level()
                        ? clan.withExp(clan.exp(), calculated)
                        : clan;

                if (!fixed.equals(clan)) db.saveClan(fixed);
                clans.put(lowerName, fixed);
                fixed.members().forEach(m -> {
                    playerToClan.put(m, fixed.name());
                    PlayerFind.uuid(m).ifPresent(GlowManager::enable);
                });
            });
        });
    }

    public void startSaveTask() {
        Task.timer(20L, 20L, () -> {
            long now = System.currentTimeMillis();
            dirtyClanSchedule.entrySet().removeIf(entry -> {
                if (now - entry.getValue() >= SAVE_DELAY_MS) {
                    Optional.ofNullable(clans.get(entry.getKey()))
                            .ifPresent(db::saveClan);
                    return true;
                }
                return false;
            });
        });
    }

    public void startInviteCleanupTask() {
        Task.timer(20L * 60L, 20L * 60L, () -> {
            int timeout = PrimeClans.getInstance().getConfig()
                    .getInt("settings.invite-timeout", 60);
            long now = System.currentTimeMillis();
            long expireMs = timeout * 1000L;
            invites.forEach((uuid, clanMap) ->
                    clanMap.entrySet().removeIf(e -> now - e.getValue() > expireMs)
            );

            invites.entrySet().removeIf(e -> e.getValue().isEmpty());
        });
    }

    private void scheduleSave(Clan clan) {
        dirtyClanSchedule.put(clan.name().toLowerCase(), System.currentTimeMillis());
    }

    @Override
    public Optional<Clan> getClan(String name) {
        return Optional.ofNullable(clans.get(name.toLowerCase()));
    }

    @Override
    public Optional<Clan> getPlayerClan(UUID uuid) {
        return Optional.ofNullable(playerToClan.get(uuid))
                .flatMap(this::getClan);
    }

    @Override
    public Collection<String> getClanNames() {
        return Collections.unmodifiableCollection(clans.keySet());
    }

    @Override
    public boolean isSameClan(UUID p1, UUID p2) {
        return Objects.equals(playerToClan.get(p1), playerToClan.get(p2));
    }

    public boolean exists(String name) {
        return clans.containsKey(name.toLowerCase());
    }

    public boolean hasPerm(UUID uuid, ClanPerm perm) {
        return getPlayerClan(uuid)
                .map(clan -> clan.hasPerm(uuid, perm))
                .orElse(false);
    }

    public Optional<Boolean> toggleMemberPerm(String clanName, UUID targetUuid, ClanPerm perm) {
        return getClan(clanName).map(clan -> {
            if (!clan.members().contains(targetUuid)) return null;
            if (clan.isOwner(targetUuid)) return null;
            boolean hadPerm = Optional.ofNullable(clan.memberPerms().get(targetUuid))
                    .map(perms -> perms.contains(perm))
                    .orElse(false);

            Clan updated;
            if (hadPerm) {
                updated = clan.withoutMemberPerm(targetUuid, perm);
                db.removeMemberPerm(targetUuid, perm);
            } else {
                updated = clan.withMemberPerm(targetUuid, perm);
                db.addMemberPerm(targetUuid, perm);
            }

            clans.put(clanName.toLowerCase(), updated);
            return !hadPerm;
        });
    }

    public void createClan(String name, UUID owner) {
        Clan clan = new Clan(name, owner, new HashSet<>(Set.of(owner)),
                1, 0L, false, 0.0, GlowColor.of(255, 100, 0));
        String lower = name.toLowerCase();
        clans.put(lower, clan);
        playerToClan.put(owner, name);
        db.saveClan(clan);
        PlayerFind.uuid(owner).ifPresent(GlowManager::enable);
    }

    public void deleteClan(String name) {
        String lower = name.toLowerCase();
        Optional.ofNullable(clans.remove(lower)).ifPresent(clan -> {
            dirtyClanSchedule.remove(lower);
            List<Player> onlineMembers = clan.members().stream()
                    .map(PlayerFind::uuid)
                    .flatMap(Optional::stream)
                    .filter(Player::isOnline)
                    .toList();
            onlineMembers.forEach(member ->
                    GlowUpdater.sendRealEquipment(member, onlineMembers)
            );
            clan.members().forEach(uuid -> {
                playerToClan.remove(uuid);
                PlayerFind.uuid(uuid).ifPresent(GlowManager::disable);
            });
            db.deleteClan(name);
        });
    }

    public void addMember(String clanName, UUID player) {
        getClan(clanName).ifPresent(clan -> {
            Set<UUID> newMembers = new HashSet<>(clan.members());
            newMembers.add(player);
            Clan updated = clan.withMembers(newMembers);
            clans.put(clanName.toLowerCase(), updated);
            playerToClan.put(player, clan.name());
            db.addMember(clan.name(), player);
            PlayerFind.uuid(player).ifPresent(GlowManager::enable);
        });
        invites.remove(player);
    }

    public void removeMember(String clanName, UUID player) {
        getClan(clanName).ifPresent(clan -> {
            List<Player> viewers = clan.members().stream()
                    .filter(m -> !m.equals(player))
                    .map(PlayerFind::uuid)
                    .flatMap(Optional::stream)
                    .filter(Player::isOnline)
                    .toList();
            PlayerFind.uuid(player).ifPresent(removed ->
                    GlowUpdater.sendRealEquipment(removed, viewers)
            );

            Set<UUID> newMembers = new HashSet<>(clan.members());
            newMembers.remove(player);
            Clan updated = clan.withMembers(newMembers)
                    .withMemberColor(player, null)
                    .withoutAllMemberPerms(player);
            clans.put(clanName.toLowerCase(), updated);
            playerToClan.remove(player);
            db.removeMember(player);
            PlayerFind.uuid(player).ifPresent(GlowManager::disable);
        });
    }

    public void setOwner(String clanName, UUID newOwner) {
        getClan(clanName).ifPresent(clan -> {
            Clan updated = clan.withOwner(newOwner);
            clans.put(clanName.toLowerCase(), updated);
            db.saveClan(updated);
        });
    }

    public void setGlowColor(String clanName, GlowColor color) {
        getClan(clanName).ifPresent(clan -> {
            Clan updated = clan.withGlowColor(color);
            clans.put(clanName.toLowerCase(), updated);
            db.saveClan(updated);
            GlowUpdater.forceUpdateAll(updated);
        });
    }

    public void setMemberGlowColor(String clanName, UUID memberUuid, GlowColor color) {
        getClan(clanName).ifPresent(clan -> {
            if (!clan.members().contains(memberUuid)) return;
            Clan updated = clan.withMemberColor(memberUuid, color);
            clans.put(clanName.toLowerCase(), updated);
            db.setMemberGlowColor(memberUuid, color);
            GlowUpdater.forceUpdateAll(updated);
        });
    }

    public void resetMemberGlowColor(String clanName, UUID memberUuid) {
        getClan(clanName).ifPresent(clan -> {
            if (!clan.members().contains(memberUuid)) return;
            Clan updated = clan.withoutMemberColor(memberUuid);
            clans.put(clanName.toLowerCase(), updated);
            db.removeMemberGlowColor(memberUuid);
            GlowUpdater.forceUpdateAll(updated);
        });
    }

    public void addExp(UUID playerUuid, long amount) {
        if (amount <= 0) return;
        getPlayerClan(playerUuid).ifPresent(clan -> {
            long newExp = clan.exp() + amount;
            int oldLevel = clan.level();
            int newLevel = PrimeClans.getInstance().getLevelService().calculateLevel(newExp);
            Clan updated = clan.withExp(newExp, newLevel);
            clans.put(clan.name().toLowerCase(), updated);
            scheduleSave(updated);
            if (newLevel > oldLevel) {
                PrimeClans.getInstance().getLevelService().handleLevelUp(updated, oldLevel);
            }
        });
    }

    public void setPvpEnabled(String clanName, boolean enabled) {
        getClan(clanName).ifPresent(clan -> {
            Clan updated = clan.withPvp(enabled);
            clans.put(clanName.toLowerCase(), updated);
            db.saveClan(updated);
        });
    }

    public Optional<Double> getBalance(String clanName) {
        return getClan(clanName).map(Clan::balance);
    }

    public void deposit(String clanName, double amount) {
        if (amount <= 0) return;
        getClan(clanName).ifPresent(clan -> {
            Clan updated = clan.withBalance(clan.balance() + amount);
            clans.put(clanName.toLowerCase(), updated);
            db.saveClan(updated);
        });
    }

    public Optional<Boolean> withdraw(String clanName, double amount) {
        if (amount <= 0) return Optional.of(false);
        return getClan(clanName).map(clan -> {
            if (clan.balance() < amount) return false;
            Clan updated = clan.withBalance(clan.balance() - amount);
            clans.put(clanName.toLowerCase(), updated);
            db.saveClan(updated);
            return true;
        });
    }

    public boolean hasActiveInvite(UUID target, String clanName) {
        int timeout = PrimeClans.getInstance().getConfig()
                .getInt("settings.invite-timeout", 60);
        long now = System.currentTimeMillis();
        return Optional.ofNullable(invites.get(target))
                .map(m -> m.get(clanName.toLowerCase()))
                .filter(time -> now - time <= timeout * 1000L)
                .isPresent();
    }

    public void createInvite(UUID target, String clanName) {
        invites.computeIfAbsent(target, k -> new ConcurrentHashMap<>())
                .put(clanName.toLowerCase(), System.currentTimeMillis());
    }
}