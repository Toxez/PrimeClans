package ua.vdev.primeclans.perm;

import java.util.Optional;

public enum ClanPerm {
    KICK_MEMBERS,
    INVITE_MEMBERS,
    MANAGE_GLOW,
    MANAGE_MEMBER_GLOW,
    INVEST_BALANCE,
    WITHDRAW_BALANCE,
    CLAN_CHAT,
    TOGGLE_PVP;

    public static Optional<ClanPerm> of(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        try {
            return Optional.of(valueOf(name.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}