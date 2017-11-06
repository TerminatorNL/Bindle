package cf.terminator.bindle.commands;

import cf.terminator.bindle.Main;
import cf.terminator.bindle.mysql.SQLManager;
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

import java.util.UUID;

public class GetSelf implements CommandExecutor{

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
                    SQLManager.SQLResult result = Main.PLUGIN.SQLManager.getData(uuid);
                    if (result.playerData.equals(new NBTTagCompound())) {
                        player.sendMessage(Text.of(TextColors.RED, "You have to use /bindle put-self first!"));
                        return;
                    }
                    /* Everything works! The next tick, the player will have it's stuff! Whooho */
                    MinecraftForge.EVENT_BUS.register(new PlayerInjection((EntityPlayerMP) player, result.playerData));

                    if (Main.PLUGIN.SQLManager.storeData(uuid, result.stored, new NBTTagCompound()) == false) {
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
            Main.LOGGER.info(player.getName() + "'s player data has been retrieved!");
            player.readEntityFromNBT(tag);
        }
    }
}
