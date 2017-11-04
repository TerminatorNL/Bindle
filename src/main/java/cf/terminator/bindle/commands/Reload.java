package cf.terminator.bindle.commands;

import cf.terminator.bindle.Main;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class Reload implements CommandExecutor{
    @Override
    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        Main.PLUGIN.load();
        commandSource.sendMessage(Text.of(TextColors.GREEN, "Reloading Bindle asynchronously, check the console for errors!"));
        return CommandResult.success();
    }
}
