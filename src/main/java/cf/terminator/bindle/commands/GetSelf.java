package cf.terminator.bindle.commands;

import cf.terminator.bindle.Main;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;

public class GetSelf implements CommandExecutor{
    private static void attemptDrop(Player player, Inventory i, Location<World> OLD_PLAYER_LOCATION) {
        if (i instanceof SlotAdapter) {
            Optional<ItemStack> stack = i.peek();
            if (stack.isPresent()) {
                i.clear();
                Item item = (Item) player.getWorld().createEntity(EntityTypes.ITEM, OLD_PLAYER_LOCATION.getPosition());
                item.offer(Keys.REPRESENTED_ITEM, stack.get().createSnapshot());
                Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
                    @Override
                    public void run() {
                        player.getWorld().spawnEntity(item, Cause.of(NamedCause.owner(Main.PLUGIN)));
                    }
                }).submit(Main.PLUGIN);
            }
        } else {
            for (Inventory sub : i) {
                attemptDrop(player, sub, OLD_PLAYER_LOCATION);
            }
        }
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext commandContext) throws CommandException {
        UUID uuid;
        try {
            uuid = UUID.fromString(src.getIdentifier());
        } catch (IllegalArgumentException e) {
            throw new CommandException(Text.of("You can\'t run this command, because you aren\'t a player!"));
        }
        final Player player = Sponge.getServer().getPlayer(uuid).get();
        final File playerFile = new File("./world/playerdata/" + uuid.toString() + ".dat");
        if (player.isOnline() == false) {
            throw new CommandException(Text.of("This should never happen."));
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String sql = "SELECT * FROM Data WHERE (Player='" + uuid.toString() + "') AND (Page=" + Bindle.PLAYER_NBT_DATA + ");";

                    Connection conn = Main.PLUGIN.SQLManager.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet result = stmt.executeQuery();
                    int count = 0;
                    byte[] dataTMP = new byte[0];
                    while (result.next()) {
                        count++;
                        if(count > 1){
                            player.sendMessage(Text.of("Something went horribly wrong with your player.dat! Contact the server staff for help."));
                            throw new RuntimeException("There are two entries for this player in the SQL database, this was thought to be impossible!");
                        }
                        dataTMP = DatatypeConverter.parseHexBinary(result.getString("Item"));
                    }
                    if(dataTMP.length == 0){
                        player.sendMessage(Text.of("You have to use /bindle put-self first!"));
                        return;
                    }
                    final byte[] data = dataTMP;
                    /* Everything works! Preparing the player.... */

                    Inventory playerInventory = player.getInventory();
                    final Location<World> OLD_PLAYER_LOCATION = player.getLocation();

                    attemptDrop(player, playerInventory, OLD_PLAYER_LOCATION);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                boolean isDone = false;
                                while (isDone == false) {
                                    WatchService service = FileSystems.getDefault().newWatchService();
                                    playerFile.getParentFile().toPath().register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW);
                                    for (WatchEvent<?> l : service.take().pollEvents()) {
                                        final Path changed = (Path) l.context();
                                        if (changed.endsWith(playerFile.getName() + ".tmp")) {
                                            Main.LOGGER.info(player.getName() + "\'s files are updated. Starting file replacement!");
                                            isDone = true;
                                            break;
                                        }
                                    }
                                    service.close();
                                }
                                Thread.sleep(200); //Wait for minecraft to finish updating the NBT data
                                if (playerFile.delete() == false) {
                                    throw new RuntimeException("Failed to delete data for: " + player.getName());
                                }
                                if(playerFile.createNewFile() == false){
                                    throw new RuntimeException("Failed to create new data file for: " + player.getName());
                                }
                                FileOutputStream fo = new FileOutputStream(playerFile);
                                fo.write(data);
                                fo.close();
                                if (Main.PLUGIN.SQLManager.removeData(Main.PLUGIN.SQLManager.getConnection(), uuid, Bindle.PLAYER_NBT_DATA, 0) == false) {
                                    throw new RuntimeException("Failed to remove data for player: " + player.getName() + " !!!! THINGS MAY HAVE DUPED!");
                                }

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    player.kick(Text.of(TextColors.GREEN, "Hurray! Your player data should be transferred now!", TextColors.GOLD, " Log back in to find out!"));
                    Sponge.getEventManager().registerListener(Main.PLUGIN, ClientConnectionEvent.Join.class, new EventListener<ClientConnectionEvent.Join>() {
                        @Override
                        public void handle(ClientConnectionEvent.Join join) throws Exception {
                            if(join.getTargetEntity().getUniqueId().equals(uuid)) {
                                join.getTargetEntity().setLocation(OLD_PLAYER_LOCATION);
                                Sponge.getEventManager().unregisterListeners(this);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


        return CommandResult.success();
    }
}
