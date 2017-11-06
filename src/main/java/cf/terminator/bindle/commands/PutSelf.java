package cf.terminator.bindle.commands;

import cf.terminator.bindle.Main;
import cf.terminator.bindle.mysql.SQLManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.sql.SQLException;
import java.util.UUID;

public class PutSelf implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        UUID uuid;
        try {
            uuid = UUID.fromString(src.getIdentifier());
        } catch (IllegalArgumentException e) {
            throw new CommandException(Text.of("You can\'t run this command, because you aren\'t a player!"));
        }
        EntityPlayerMP player = Main.MINECRAFT_SERVER.getPlayerList().getPlayerByUUID(uuid);

        final NBTTagCompound playerNBT = new NBTTagCompound();
        player.writeEntityToNBT(playerNBT);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SQLManager.SQLResult current = Main.PLUGIN.SQLManager.getData(uuid);
                    if (Main.PLUGIN.SQLManager.storeData(uuid, current.stored, playerNBT) == false) {
                        player.addChatMessage(new TextComponentString(TextFormatting.RED + "Unable to store your data! Aborted."));
                        throw new SQLException("Failed to upload!");
                    }else{
                        Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
                            @Override
                            public void run() {
                                player.readEntityFromNBT(new NBTTagCompound());
                                player.addChatMessage(new TextComponentString(TextFormatting.GREEN + "Bindle saved your player data! Please log into the other server where you want to move your data to and type " + TextFormatting.GOLD + "/bindle get-self"));
                            }
                        }).submit(Main.PLUGIN);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"Bindle: saving player.dat: " + player.getName()).start();
        return CommandResult.success();
    }
}
