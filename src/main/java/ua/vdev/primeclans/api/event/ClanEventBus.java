package ua.vdev.primeclans.api.event;

import ua.vdev.primeclans.model.Clan;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ClanEventBus {

    private static final List<Consumer<ClanCreateEvent>> onCreate = new CopyOnWriteArrayList<>();
    private static final List<Consumer<ClanDeleteEvent>> onDelete = new CopyOnWriteArrayList<>();
    private static final List<Consumer<MemberJoinEvent>> onJoin = new CopyOnWriteArrayList<>();
    private static final List<Consumer<MemberLeaveEvent>> onLeave = new CopyOnWriteArrayList<>();
    private static final List<Consumer<ClanLevelUpEvent>> onLevelUp = new CopyOnWriteArrayList<>();
    private static final List<Consumer<ExpGainEvent>> onExpGain = new CopyOnWriteArrayList<>();
    private static final List<Consumer<PvpToggleEvent>> onPvp = new CopyOnWriteArrayList<>();
    private static final List<Consumer<BalanceChangeEvent>> onBalance = new CopyOnWriteArrayList<>();

    public static void onClanCreate(Consumer<ClanCreateEvent> h) { onCreate.add(h); }
    public static void onClanDelete(Consumer<ClanDeleteEvent> h) { onDelete.add(h); }
    public static void onMemberJoin(Consumer<MemberJoinEvent> h) { onJoin.add(h); }
    public static void onMemberLeave(Consumer<MemberLeaveEvent> h) { onLeave.add(h); }
    public static void onLevelUp(Consumer<ClanLevelUpEvent> h) { onLevelUp.add(h); }
    public static void onExpGain(Consumer<ExpGainEvent> h) { onExpGain.add(h); }
    public static void onPvpToggle(Consumer<PvpToggleEvent> h) { onPvp.add(h); }
    public static void onBalanceChange(Consumer<BalanceChangeEvent> h){ onBalance.add(h); }

    public static void fireCreate(Clan clan) {
        ClanCreateEvent e = new ClanCreateEvent(clan);
        onCreate.forEach(h -> safe(h, e));
    }

    public static void fireDelete(Clan clan) {
        ClanDeleteEvent e = new ClanDeleteEvent(clan);
        onDelete.forEach(h -> safe(h, e));
    }

    public static void fireJoin(Clan clan, UUID memberUuid) {
        MemberJoinEvent e = new MemberJoinEvent(clan, memberUuid);
        onJoin.forEach(h -> safe(h, e));
    }

    public static void fireLeave(Clan clan, UUID memberUuid) {
        MemberLeaveEvent e = new MemberLeaveEvent(clan, memberUuid);
        onLeave.forEach(h -> safe(h, e));
    }

    public static void fireLevelUp(Clan clan, int oldLevel, int newLevel) {
        ClanLevelUpEvent e = new ClanLevelUpEvent(clan, oldLevel, newLevel);
        onLevelUp.forEach(h -> safe(h, e));
    }

    public static void fireExpGain(Clan clan, long amount) {
        ExpGainEvent e = new ExpGainEvent(clan, amount);
        onExpGain.forEach(h -> safe(h, e));
    }

    public static void firePvpToggle(Clan clan, boolean enabled) {
        PvpToggleEvent e = new PvpToggleEvent(clan, enabled);
        onPvp.forEach(h -> safe(h, e));
    }

    public static void fireBalanceChange(Clan clan, double oldBalance, double newBalance) {
        BalanceChangeEvent e = new BalanceChangeEvent(clan, oldBalance, newBalance);
        onBalance.forEach(h -> safe(h, e));
    }

    private static <T> void safe(Consumer<T> handler, T event) {
        try { handler.accept(event); }
        catch (Exception ex) { ex.printStackTrace(); }
    }

    public record ClanCreateEvent(Clan clan) {}
    public record ClanDeleteEvent(Clan clan) {}
    public record MemberJoinEvent(Clan clan, UUID memberUuid) {}
    public record MemberLeaveEvent(Clan clan, UUID memberUuid) {}
    public record ClanLevelUpEvent(Clan clan, int oldLevel, int newLevel) {}
    public record ExpGainEvent(Clan clan, long amount) {}
    public record PvpToggleEvent(Clan clan, boolean enabled) {}
    public record BalanceChangeEvent(Clan clan, double oldBalance, double newBalance) {}
}