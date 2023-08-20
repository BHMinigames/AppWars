package me.bjedev.appwars.mirror;

import me.bjedev.appwars.game.GameManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHuman;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class Mirror {
    // Mirror Coordinates
    private final Location MIRROR_TOP_RIGHT = new Location(Bukkit.getWorld("world"), 3843, 159, 3979);
    private final Location MIRROR_BOTTOM_LEFT = new Location(Bukkit.getWorld("world"), 3864, 143, 3979);
    // Mirror box coordinates
    private static final Location MIRROR_BOX_TOP_RIGHT = new Location(Bukkit.getWorld("world"), 3866, 160, 3993);
    private static final Location MIRROR_BOX_BOTTOM_LEFT = new Location(Bukkit.getWorld("world"), 3841, 141, 3965);
    private final Player player;
    public NPC npc;

    public Mirror(Player player) {
        this.player = player;
    }

    public boolean isInMirrorBox() {
        Location loc = player.getLocation();

        return loc.getX() <= MIRROR_BOX_TOP_RIGHT.getX() && loc.getX() >= MIRROR_BOX_BOTTOM_LEFT.getX() &&
                loc.getY() >= MIRROR_BOX_BOTTOM_LEFT.getY() && loc.getY() <= MIRROR_BOX_TOP_RIGHT.getY() &&
                loc.getZ() <= MIRROR_BOX_TOP_RIGHT.getZ() && loc.getZ() >= MIRROR_BOX_BOTTOM_LEFT.getZ();
    }

    public static boolean isInMirrorBox(Player player) {
        Location loc = player.getLocation();

        return loc.getX() <= MIRROR_BOX_TOP_RIGHT.getX() && loc.getX() >= MIRROR_BOX_BOTTOM_LEFT.getX() &&
                loc.getY() >= MIRROR_BOX_BOTTOM_LEFT.getY() && loc.getY() <= MIRROR_BOX_TOP_RIGHT.getY() &&
                loc.getZ() <= MIRROR_BOX_TOP_RIGHT.getZ() && loc.getZ() >= MIRROR_BOX_BOTTOM_LEFT.getZ();
    }


    public void handleReflect() {
        if (!isInMirrorBox()) {
            return; // Don't do anything if the player is not in the mirror box.
        }

        // Check if NPC exists for this player, if not spawn it.
        if (npc == null)
            npc = spawnNPC(player.getLocation());

        // Get the mirrored location and teleport NPC to it.
        Location mirroredLocation = getMirroredLocation(player.getLocation());

        // Get the player's direction (yaw and pitch).
        float playerYaw = player.getLocation().getYaw() + 180;
        float playerPitch = player.getLocation().getPitch();

        // Calculate the mirrored yaw.
        float centerYaw = getMirrorCenter().getYaw();
        float mirroredYaw = 2 * centerYaw - playerYaw;

        // Set the NPC's yaw and pitch.
        mirroredLocation.setYaw(mirroredYaw);
        mirroredLocation.setPitch(playerPitch);

        npc.teleport(mirroredLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);

        try {
            final Trait trait = npc.getTrait(CitizensAPI.getTraitFactory().getTraitClass("gravity"));
            trait.getClass().getDeclaredMethod("gravitate", boolean.class).invoke(trait, player.isFlying());
        } catch (final Exception ignored) {}

        this.copyItemToNPC(new ItemStack(Material.AIR));
        this.copyItemToNPC(player.getItemInHand());
    }

    public void setNpcSneaking(boolean isSneaking) {
        if (npc == null) return;

        Entity nmsEntity = ((CraftEntity) npc.getEntity()).getHandle();
        nmsEntity.setSneaking(isSneaking);
    }

    public void makeNpcSwingArm() {
        if (npc == null) return;

        EntityHuman nmsHuman = ((CraftHumanEntity) npc.getEntity()).getHandle();
        nmsHuman.bw(); // NMS Swing method
    }

    public void copyItemToNPC(final ItemStack item) {
        // Check if the NPC is spawnable as a LivingEntity (e.g., a human NPC)
        if (npc.isSpawned() && npc.getEntity() instanceof Player) {
            final Player npcEntity = (Player) npc.getEntity();

            npcEntity.setItemInHand(item);
            npcEntity.updateInventory();
        }
    }

    public NPC spawnNPC(Location location) {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();

        NPC npc = registry.createNPC(EntityType.PLAYER, player.getDisplayName());

        npc.spawn(location);

//        EntityPlayer npcEntity = ((CraftPlayer) npc.getEntity()).getHandle();
//        EntityPlayer playerEntity = ((CraftPlayer) player).getHandle();
//
//        GameProfile gameProfile = new GameProfile(player.getUniqueId(), reverseString(player.getName()));
//
//        // Set the GameProfile
//        try {
//            Field profileField = playerEntity.getClass().getSuperclass().getDeclaredField("bH");
//            profileField.setAccessible(true);
//            profileField.set(npcEntity, gameProfile);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            e.printStackTrace();
//        }

        return npc;
    }

    public Location getMirroredLocation(Location original) {
        Location center = getMirrorCenter();
        double mirroredZ = center.getZ() - (original.getZ() - center.getZ());
        return new Location(original.getWorld(), original.getX(), original.getY(), mirroredZ + 1);
    }

    private String reverseString(String input) {
        return new StringBuilder(input).reverse().toString();
    }

    private Location getMirrorCenter() {
        return new Location(
                MIRROR_TOP_RIGHT.getWorld(),
                (MIRROR_TOP_RIGHT.getX() + MIRROR_BOTTOM_LEFT.getX()) / 2,
                (MIRROR_TOP_RIGHT.getY() + MIRROR_BOTTOM_LEFT.getY()) / 2,
                (MIRROR_TOP_RIGHT.getZ() + MIRROR_BOTTOM_LEFT.getZ()) / 2
        );
    }

    public void deleteNPC() {
        if (npc == null) return;
        npc.destroy();
        GameManager.playerMirrors.remove(player);
    }
}
