package be.maximvdw.playermovebug2;

import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class PlayerMoveBug extends JavaPlugin implements Listener {
    private TinyProtocol tinyProtocol = null;
    private Reflection.FieldAccessor<Float> yawField = Reflection.getField("{nms}.PacketPlayInFlying", float.class, 0);
    private Reflection.FieldAccessor<Float> pitchField = Reflection.getField("{nms}.PacketPlayInFlying", float.class, 1);

    public void onEnable() {
        tinyProtocol = new TinyProtocol(this) {
            public Object onPacketInAsync(Player sender, Channel channel, Object packet) {
                if (yawField.hasField(packet)) {
                    float yaw = yawField.get(packet);
                    float pitch = pitchField.get(packet);
                    if (sender.getLocation().getYaw() != yaw || sender.getLocation().getPitch() != pitch) {
                        getLogger().info("[PACKET BEHAVIOR] " + sender.getName() + " is moving! " + yaw + " , " + pitch);
                    }
                }
                return super.onPacketInAsync(sender, channel, packet);
            }
        };
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Location location = event.getPlayer().getLocation();
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.addPassenger(event.getPlayer());
        tinyProtocol.injectPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        getLogger().info("[SPIGOT BEHAVIOR] " + event.getPlayer().getName() + " is moving!");
    }
}
