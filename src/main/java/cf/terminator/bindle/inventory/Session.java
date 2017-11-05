package cf.terminator.bindle.inventory;

import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.UUID;

public class Session {

    private static ArrayList<PlayerSession> SESSIONS = new ArrayList<>();

    public static boolean isSessionActive(EntityPlayer player, int page) {
        return SESSIONS.contains(new PlayerSession(player.getUniqueID(), page));
    }

    public static void startSession(EntityPlayer player, int page) {
        if (SESSIONS.contains(new PlayerSession(player.getUniqueID(), page)) == false) {
            SESSIONS.add(new PlayerSession(player.getUniqueID(), page));
        }
    }

    public static void endSession(EntityPlayer player, int page) {
        SESSIONS.remove(new PlayerSession(player.getUniqueID(), page));
    }

    private static class PlayerSession {

        final UUID uuid;
        final int page;


        PlayerSession(UUID ui, int p) {
            uuid = ui;
            page = p;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof PlayerSession == false) {
                return false;
            }
            PlayerSession o = (PlayerSession) other;
            if (this.uuid.equals(o.uuid)) {
                if (this.page == o.page) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return uuid.toString() + " > " + page;
        }
    }
}