package cf.terminator.bindle.commands;

import cf.terminator.bindle.Main;
import cf.terminator.bindle.inventory.SQLInventory;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.util.UUID;

import static cf.terminator.bindle.inventory.SQLInventory.SESSIONS;

public class Open implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        UUID uuid;
        try {
            uuid = UUID.fromString(src.getIdentifier());
        } catch (IllegalArgumentException e) {
            throw new CommandException(Text.of("You can\'t run this command, because you aren\'t a player!"));
        }
        EntityPlayer player = Main.MINECRAFT_SERVER.getPlayerList().getPlayerByUUID(uuid);

        if (SESSIONS.contains(uuid)) {
            throw new CommandException(Text.of("Sorry! We're still processing, try again later. :)"));
        }
        SQLInventory inventory = new SQLInventory(player);
        player.displayGUIChest(inventory);


        return CommandResult.success();
    }
}
