package cf.terminator.bindle.commands;


import cf.terminator.bindle.inventory.Handler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.util.UUID;

public class Bindle implements CommandExecutor{
    public static final int PLAYER_NBT_DATA = -128;
    
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        UUID uuid;
        try {
            uuid = UUID.fromString(src.getIdentifier());
        } catch (IllegalArgumentException e) {
            throw new CommandException(Text.of("You can\'t run this command, because you aren\'t a player!"));
        }
        int page = (int) args.getOne("page").orElse(1);
        page = Math.max(page, 1);
        page = Math.min(page, 9);
        Handler handler = new Handler(uuid);
        handler.execute(page);

        return CommandResult.success();
    }
}
