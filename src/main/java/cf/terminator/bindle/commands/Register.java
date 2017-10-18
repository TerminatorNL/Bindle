package cf.terminator.bindle.commands;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class Register {
    public static void registerAll(Object plugin){

        CommandSpec bindleMe = CommandSpec.builder()
                .description(Text.of("Stores your player data"))
                .permission("bindle.user.putself")
                .executor(new PutSelf())
                .build();

        CommandSpec bindleGet = CommandSpec.builder()
                .description(Text.of("Retrieves your player data"))
                .permission("bindle.user.getself")
                .executor(new GetSelf())
                .build();

        CommandSpec bindle = CommandSpec.builder()
                .description(Text.of("Moves items between servers"))
                .permission("bindle.user.use")
                .executor(new Bindle())
                .child(bindleMe, "put-self")
                .child(bindleGet, "get-self")
                .arguments(GenericArguments.optional(GenericArguments.integer(Text.of("page"))))
                .build();

        Sponge.getCommandManager().register(plugin, bindle, "bindle");










    }
}
