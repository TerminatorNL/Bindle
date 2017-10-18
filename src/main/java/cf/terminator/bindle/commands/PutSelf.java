package cf.terminator.bindle.commands;

import cf.terminator.bindle.Main;
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
import java.io.File;
import java.nio.file.*;
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
        Player player = Sponge.getServer().getPlayer(uuid).get();
        File playerFile = new File("./world/playerdata/" + uuid.toString() + ".dat");
        if (player.isOnline() == false) {
            throw new CommandException(Text.of("This should never happen."));
        }
        if (playerFile.exists() == false) {
            throw new CommandException(Text.of("Unable to find your nbt data!"));
        }



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
                            if(changed.endsWith(playerFile.getName() + ".tmp")){
                                Main.LOGGER.info(player.getName() + "\'s files are updated. Starting upload!");
                                isDone=true;
                                break;
                            }
                        }
                        service.close();
                    }
                    if (Main.PLUGIN.SQLManager.storeData(Main.PLUGIN.SQLManager.getConnection(), uuid, Bindle.PLAYER_NBT_DATA, 0, DatatypeConverter.printHexBinary(Files.readAllBytes(playerFile.toPath()))) == false) {
                        throw new SQLException("Failed to upload!");
                    }else{
                        if(playerFile.delete() == false){
                            throw new RuntimeException("UNABLE TO REMOVE PLAYER.DAT!!! THINGS MAY HAVE DUPED!");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"Bindle: saving player.dat: " + player.getName()).start();

        player.kick(Text.of(TextColors.GREEN, "Bindle saved your player data! Please log into the other server where you want to move your data to and type ", TextColors.GOLD, "/bindle get-self"));
        return CommandResult.success();
    }
}
