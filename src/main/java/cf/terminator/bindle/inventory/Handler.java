package cf.terminator.bindle.inventory;

import cf.terminator.bindle.Main;
import cf.terminator.bindle.inventory.items.Items;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.text.Text;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.UUID;

public class Handler {

    private final Player player;

    public Handler(UUID playerUUID){
        player = Sponge.getServer().getPlayer(playerUUID).get();
    }

    public Handler(Player player_){
        player = player_;
    }

    public void execute(final Integer page){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (player.isOnline() == false) {
                    return;
                }
                if(Session.isSessionActive(player, page)){
                    player.sendMessage(Text.of("Sorry, this page is still processing!"));
                    return;
                }
                try {
                    SQLInventory sqlInventory = new SQLInventory(player, page);
                    Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
                        @Override
                        public void run() {
                            player.openInventory(sqlInventory.getInventory(), Cause.of(NamedCause.owner(Main.getInstance())));
                        }
                    }).submit(Main.PLUGIN);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void setPageDone(Player player){
        if(SQLInventory.OPEN_INVENTORIES.containsKey(player)){
            SQLInventory sqlInventory = SQLInventory.OPEN_INVENTORIES.get(player);

            int i=0;
            for(Inventory grid : sqlInventory.getInventory()){
                for(Inventory row : grid) {
                    for (Inventory slot : row) {
                        try {
                            if (i == 45 && slot.peek().get().equalTo(Items.BUTTON_SAVING)) {
                                slot.set(Items.getPreviousButtonFor(sqlInventory.page));
                            } else if (i == 53 && slot.peek().get().equalTo(Items.BUTTON_SAVING)) {
                                slot.set(Items.getNextButtonFor(sqlInventory.page));
                            }
                        }catch (NoSuchElementException e){
                            // Should never happen. But since it's a runtime exception we catch it just in case.
                            e.printStackTrace();
                        }
                        i++;
                    }
                }
            }
        }
    }
}


class Session{

    private static ArrayList<PlayerSession> SESSIONS = new ArrayList<>();

    static boolean isSessionActive(Player player, int page){
        return SESSIONS.contains(new PlayerSession(player.getUniqueId(), page));
    }

    static void startSession(Player player, int page){
        SESSIONS.add(new PlayerSession(player.getUniqueId(), page));
    }

    static void endSession(Player player, int page){
        SESSIONS.remove(new PlayerSession(player.getUniqueId(), page));
    }

    private static class PlayerSession{

        final UUID uuid;
        final int page;


        PlayerSession(UUID ui, int p){
            uuid = ui;
            page = p;
        }

        @Override
        public boolean equals(Object other){
            if (other instanceof PlayerSession == false){
                return false;
            }
            PlayerSession o = (PlayerSession) other;
            if(this.uuid.equals(o.uuid)){
                if(this.page == o.page){
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString(){
            return uuid.toString() + " > " + page;
        }
    }
}

