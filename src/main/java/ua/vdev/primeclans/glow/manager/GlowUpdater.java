package ua.vdev.primeclans.glow.manager;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import ua.vdev.primeclans.glow.GlowColor;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.vlibapi.player.PlayerFind;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class GlowUpdater {

    private GlowUpdater() {}

    public static void forceUpdatePlayer(Player subject, Clan clan) {
        GlowColor color = clan.effectiveColorFor(subject.getUniqueId());
        boolean glows = GlowManager.isEnabled(subject);

        List<Equipment> equipment = (glows && color != null)
                ? buildGlowEquipment(subject, color)
                : buildRealEquipment(subject);

        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(subject.getEntityId(), equipment);

        clan.members().stream()
                .map(PlayerFind::uuid)
                .flatMap(Optional::stream)
                .filter(p -> !p.equals(subject) && p.isOnline())
                .forEach(viewer ->
                        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet)
                );
    }

    public static void forceUpdateAll(Clan clan) {
        List<Player> online = clan.members().stream()
                .map(PlayerFind::uuid)
                .flatMap(Optional::stream)
                .filter(Player::isOnline)
                .toList();

        if (online.size() < 2) return;

        record SubjectData(int entityId, boolean glows, List<Equipment> glowEquip, List<Equipment> realEquip) {}

        List<SubjectData> subjects = online.stream()
                .map(p -> {
                    GlowColor effectiveColor = clan.effectiveColorFor(p.getUniqueId());
                    boolean glows = GlowManager.isEnabled(p);
                    List<Equipment> glowEquip = (effectiveColor != null)
                            ? buildGlowEquipment(p, effectiveColor)
                            : buildRealEquipment(p);
                    return new SubjectData(p.getEntityId(), glows, glowEquip, buildRealEquipment(p));
                })
                .toList();

        for (int i = 0; i < online.size(); i++) {
            Player viewer = online.get(i);
            for (int j = 0; j < subjects.size(); j++) {
                if (i == j) continue;
                SubjectData subject = subjects.get(j);
                List<Equipment> toSend = subject.glows() ? subject.glowEquip() : subject.realEquip();

                PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, new WrapperPlayServerEntityEquipment(subject.entityId(), toSend)
                );
            }
        }
    }

    public static List<Equipment> buildGlowEquipment(Player player, GlowColor color) {
        PlayerInventory inv = player.getInventory();
        return List.of(
                new Equipment(EquipmentSlot.HELMET,toPacket(coloredLeather(Material.LEATHER_HELMET, color))),
                new Equipment(EquipmentSlot.CHEST_PLATE,toPacket(coloredLeather(Material.LEATHER_CHESTPLATE, color))),
                new Equipment(EquipmentSlot.LEGGINGS,toPacket(coloredLeather(Material.LEATHER_LEGGINGS, color))),
                new Equipment(EquipmentSlot.BOOTS,toPacket(coloredLeather(Material.LEATHER_BOOTS, color))),
                new Equipment(EquipmentSlot.MAIN_HAND,toPacket(inv.getItemInMainHand())),
                new Equipment(EquipmentSlot.OFF_HAND,toPacket(inv.getItemInOffHand()))
        );
    }

    public static List<Equipment> buildRealEquipment(Player player) {
        PlayerInventory inv = player.getInventory();
        return List.of(
                new Equipment(EquipmentSlot.HELMET,toPacket(inv.getHelmet())),
                new Equipment(EquipmentSlot.CHEST_PLATE,toPacket(inv.getChestplate())),
                new Equipment(EquipmentSlot.LEGGINGS,toPacket(inv.getLeggings())),
                new Equipment(EquipmentSlot.BOOTS,toPacket(inv.getBoots())),
                new Equipment(EquipmentSlot.MAIN_HAND,toPacket(inv.getItemInMainHand())),
                new Equipment(EquipmentSlot.OFF_HAND,toPacket(inv.getItemInOffHand()))
        );
    }

    private static com.github.retrooper.packetevents.protocol.item.ItemStack toPacket(ItemStack item) {
        return SpigotConversionUtil.fromBukkitItemStack(item);
    }

    private static ItemStack coloredLeather(Material leatherType, GlowColor color) {
        ItemStack item = new ItemStack(leatherType);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color.asBukkit());
        item.setItemMeta(meta);
        return item;
    }

    public static void sendRealEquipment(Player subject, Collection<Player> viewers) {
        if (subject == null || !subject.isOnline()) return;
        List<Equipment> equipment = buildRealEquipment(subject);
        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(subject.getEntityId(), equipment);
        viewers.stream()
                .filter(v -> v != null && !v.equals(subject) && v.isOnline())
                .forEach(viewer ->
                        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet)
                );
    }
}