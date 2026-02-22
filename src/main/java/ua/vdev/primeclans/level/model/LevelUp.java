package ua.vdev.primeclans.level.model;

import java.util.List;
import java.util.Optional;

public record LevelUp(
        Optional<PlaySoundData> sound,
        List<String> messages
) {}