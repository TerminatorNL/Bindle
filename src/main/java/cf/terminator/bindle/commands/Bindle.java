package cf.terminator.bindle.commands;


import cf.terminator.bindle.Main;
import cf.terminator.bindle.block.SQLBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.util.UUID;

public class Bindle implements CommandExecutor{
    
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        UUID uuid;
        try {
            uuid = UUID.fromString(src.getIdentifier());
        } catch (IllegalArgumentException e) {
            throw new CommandException(Text.of("You can\'t run this command, because you aren\'t a player!"));
        }
        EntityPlayer player = Main.MINECRAFT_SERVER.getPlayerList().getPlayerByUUID(uuid);
        new BlockSelector(player);

        return CommandResult.success();
    }
}

class BlockSelector {

    private final UUID uuid;
    private final EntityPlayer player;
    private boolean isActive = true;
    private int ticksLeft = 20 * 10;

    public BlockSelector(EntityPlayer p) {
        uuid = p.getUniqueID();
        player = p;
        p.addChatMessage(new TextComponentString(TextFormatting.GREEN + "Please click the chest you intend to save! Sneak to cancel."));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClick(net.minecraftforge.event.entity.player.PlayerInteractEvent e) {
        if (uuid.equals(e.getEntityPlayer().getUniqueID())) {
            e.setCanceled(true);
            new SQLBlock(player, e.getPos());
            unRegister();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent e) {
        if (player.isSneaking()) {
            unRegister();
        }
        if (e.phase == TickEvent.ServerTickEvent.Phase.START) {
            ticksLeft--;
            if (ticksLeft <= 0) {
                unRegister();
            }
        }
    }

    private void unRegister() {
        if (isActive == false) {
            return;
        }
        if (ticksLeft <= 0) {
            player.addChatMessage(new TextComponentString(TextFormatting.RED + "Bindle timed out after 10 seconds."));
        } else if (player.isSneaking()) {
            player.addChatMessage(new TextComponentString(TextFormatting.GOLD + "Cancelled."));
        }
        MinecraftForge.EVENT_BUS.unregister(this);
        isActive = false;
    }





}
