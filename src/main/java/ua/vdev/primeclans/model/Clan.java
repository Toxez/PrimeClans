package ua.vdev.primeclans.model;

import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.glow.GlowColor;
import ua.vdev.primeclans.perm.ClanPerm;
import java.util.*;

public record Clan(
        String name,
        UUID owner,
        Set<UUID> members,
        int level,
        long exp,
        boolean pvpEnabled,
        double balance,
        GlowColor glowColor,
        Map<UUID, GlowColor> memberColors,
        Map<UUID, Set<String>> memberPerms
) {

    public Clan {
        memberColors = memberColors != null
                ? Collections.unmodifiableMap(new HashMap<>(memberColors))
                : Collections.emptyMap();
        memberPerms = memberPerms != null
                ? Collections.unmodifiableMap(deepCopyPerms(memberPerms))
                : Collections.emptyMap();
    }

    public Clan(String name, UUID owner, Set<UUID> members, int level, long exp,
                boolean pvpEnabled, double balance, GlowColor glowColor) {
        this(name, owner, members, level, exp, pvpEnabled, balance,
                glowColor, Collections.emptyMap(), Collections.emptyMap());
    }

    public boolean hasPerm(UUID uuid, String permKey) {
        if (isOwner(uuid)) return true;
        return Optional.ofNullable(memberPerms.get(uuid))
                .map(perms -> perms.contains(permKey.toUpperCase()))
                .orElse(false);
    }

    public boolean hasPerm(UUID uuid, ClanPerm perm) {
        return hasPerm(uuid, perm.name());
    }

    public Clan withMemberPerm(UUID uuid, String permKey) {
        Map<UUID, Set<String>> updated = deepCopyPerms(memberPerms);
        updated.computeIfAbsent(uuid, k -> new HashSet<>()).add(permKey.toUpperCase());
        return new Clan(name, owner, members, level, exp, pvpEnabled,
                balance, glowColor, memberColors, updated);
    }

    public Clan withMemberPerm(UUID uuid, ClanPerm perm) {
        return withMemberPerm(uuid, perm.name());
    }

    public Clan withoutMemberPerm(UUID uuid, String permKey) {
        Map<UUID, Set<String>> updated = deepCopyPerms(memberPerms);
        Optional.ofNullable(updated.get(uuid)).ifPresent(perms -> {
            perms.remove(permKey.toUpperCase());
            if (perms.isEmpty()) updated.remove(uuid);
        });
        return new Clan(name, owner, members, level, exp, pvpEnabled,
                balance, glowColor, memberColors, updated);
    }

    public Clan withoutMemberPerm(UUID uuid, ClanPerm perm) {
        return withoutMemberPerm(uuid, perm.name());
    }

    public Clan withoutAllMemberPerms(UUID uuid) {
        Map<UUID, Set<String>> updated = deepCopyPerms(memberPerms);
        updated.remove(uuid);
        return new Clan(name, owner, members, level, exp, pvpEnabled,
                balance, glowColor, memberColors, updated);
    }

    public GlowColor effectiveColorFor(UUID uuid) {
        return Optional.ofNullable(memberColors.get(uuid)).orElse(glowColor);
    }

    public Clan withMemberColor(UUID uuid, GlowColor color) {
        Map<UUID, GlowColor> updated = new HashMap<>(memberColors);
        if (color == null) updated.remove(uuid);
        else updated.put(uuid, color);
        return new Clan(name, owner, members, level, exp, pvpEnabled,
                balance, glowColor, updated, memberPerms);
    }

    public Clan withoutMemberColor(UUID uuid) {
        return withMemberColor(uuid, null);
    }

    public Clan withBalance(double newBalance) {
        return new Clan(name, owner, members, level, exp, pvpEnabled,
                Math.max(0, newBalance), glowColor, memberColors, memberPerms);
    }

    public Clan withExp(long newExp, int newLevel) {
        return new Clan(name, owner, members, newLevel, newExp, pvpEnabled,
                balance, glowColor, memberColors, memberPerms);
    }

    public Clan withPvp(boolean pvp) {
        return new Clan(name, owner, members, level, exp, pvp,
                balance, glowColor, memberColors, memberPerms);
    }

    public Clan withOwner(UUID newOwner) {
        return new Clan(name, newOwner, members, level, exp, pvpEnabled,
                balance, glowColor, memberColors, memberPerms);
    }

    public Clan withMembers(Set<UUID> newMembers) {
        return new Clan(name, owner, newMembers, level, exp, pvpEnabled,
                balance, glowColor, memberColors, memberPerms);
    }

    public Clan withGlowColor(GlowColor newGlowColor) {
        return new Clan(name, owner, members, level, exp, pvpEnabled,
                balance, newGlowColor, memberColors, memberPerms);
    }

    public boolean isOwner(UUID uuid) {
        return owner.equals(uuid);
    }

    public int getMaxMembers() {
        return PrimeClans.getInstance().getLevelService().maxMembersForLevel(level);
    }

    private static Map<UUID, Set<String>> deepCopyPerms(Map<UUID, Set<String>> source) {
        Map<UUID, Set<String>> copy = new HashMap<>();
        source.forEach((uuid, perms) -> copy.put(uuid, new HashSet<>(perms)));
        return copy;
    }
}