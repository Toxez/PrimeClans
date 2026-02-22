package ua.vdev.primeclans.level.model;

public record ClanLevel(
        int level,
        int maxMembers,
        long requiredExp,
        LevelUp levelUp
) {}