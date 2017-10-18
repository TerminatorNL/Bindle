package cf.terminator.bindle.commands;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class Register {
    public static void registerAll(Object plugin){
        CommandSpec bindle = CommandSpec.builder()
                .description(Text.of("Opens your bindle."))
                .permission("bindle.user.use")
                .executor(new Bindle())
                .arguments(GenericArguments.optionalWeak(GenericArguments.integer(Text.of("page"))))
                .build();
        Sponge.getCommandManager().register(plugin, bindle, "Bindle");
    }
}
