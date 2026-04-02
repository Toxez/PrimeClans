package ua.vdev.primeclans.level.model;
public record ClanLevel(
        int level,
        int maxMembers,
        int storageSlots,
        long requiredExp,
        LevelUp levelUp
) {}