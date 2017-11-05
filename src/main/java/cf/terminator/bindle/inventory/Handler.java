package cf.terminator.bindle.inventory;

import cf.terminator.bindle.Main;
import cf.terminator.bindle.forgeinventory.ForgeSQLInventory;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.sql.SQLException;
import java.util.UUID;

public class Handler {

    private final Player player;

    public Handler(UUID playerUUID){
        player = Sponge.getServer().getPlayer(playerUUID).get();
    }

    public void execute(final int page) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (player.isOnline() == false) {
                    return;
                }
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                if (Session.isSessionActive(playerMP, page)) {
                    player.sendMessage(Text.of(TextColors.RED, "Sorry, this page is still processing!"));
                    return;
                }
                try {
                    ForgeSQLInventory sqlInventory = new ForgeSQLInventory(playerMP, page);
                    Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
                        @Override
                        public void run() {
                            playerMP.displayGUIChest(sqlInventory);
                        }
                    }).submit(Main.PLUGIN);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}