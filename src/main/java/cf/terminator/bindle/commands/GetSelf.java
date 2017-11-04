package cf.terminator.bindle.commands;

import cf.terminator.bindle.Main;
import cf.terminator.bindle.nbt.Loader;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class GetSelf implements CommandExecutor{

    private static int waitForUnlock(File file) {
        int count = 0;
        while (true) {
            try {
                FileInputStream i = new FileInputStream(file);
                i.close();
                break;
            } catch (FileNotFoundException ignored) {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                if (count > 20000) {
                    throw new RuntimeException("Minecraft has been writing to the playerfile " + file + " for 20 seconds now... I am going on a limb here, and assume that the server is broken by now.");
                }
                Thread.sleep(10);
                count = count + 10;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext commandContext) throws CommandException {
        UUID uuidR;
        try {
            uuidR = UUID.fromString(src.getIdentifier());
        } catch (IllegalArgumentException e) {
            throw new CommandException(Text.of("You can\'t run this command, because you aren\'t a player!"));
        }
        final UUID uuid = uuidR;
        final Player player = Sponge.getServer().getPlayer(uuid).get();
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
                            player.sendMessage(Text.of(TextColors.RED, "Something went horribly wrong with your player.dat! Contact the server staff for help."));
                            throw new RuntimeException("There are two entries for this player in the SQL database, this was thought to be impossible!");
                        }
                        dataTMP = DatatypeConverter.parseHexBinary(result.getString("Item"));
                    }
                    if(dataTMP.length == 0){
                        player.sendMessage(Text.of(TextColors.RED, "You have to use /bindle put-self first!"));
                        return;
                    }
                    final byte[] data = dataTMP;
                    /* Everything works! The next tick, the player will have it's stuff! Whooho */
                    MinecraftForge.EVENT_BUS.register(new PlayerInjection(
                            (EntityPlayerMP) player,
                            Loader.decodeFile(new ByteArrayInputStream(data))
                    ));
                    if (Main.PLUGIN.SQLManager.removeData(Main.PLUGIN.SQLManager.getConnection(), uuid, Bindle.PLAYER_NBT_DATA, 0) == false) {
                        throw new RuntimeException("Failed to remove data for player: " + player.getName() + " !!!! THINGS MAY HAVE DUPED!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return CommandResult.success();
    }

    private class PlayerInjection {

        NBTTagCompound tag;
        EntityPlayerMP player;

        PlayerInjection(EntityPlayerMP player, NBTTagCompound tag) {
            this.player = player;
            this.tag = tag;

        }

        @SubscribeEvent
        public void TickEvent(TickEvent.ServerTickEvent e) {
            MinecraftForge.EVENT_BUS.unregister(this);

            for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
                ItemStack itemstack = player.inventory.getStackInSlot(slot);
                if (itemstack != null) {
                    EntityItem item = new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, itemstack);
                    player.worldObj.spawnEntityInWorld(item);
                    player.inventory.setInventorySlotContents(slot, null);
                }
            }


            System.out.println(player.getName() + "'s player data has been updated!");
            player.readEntityFromNBT(tag);
        }
    }
}
