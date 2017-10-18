package cf.terminator.bindle.inventory;

import cf.terminator.bindle.Main;
import cf.terminator.bindle.inventory.items.Items;
import cf.terminator.bindle.nbt.Loader;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

class SQLInventory extends BasicInventory{

    static final Map<Player, SQLInventory> OPEN_INVENTORIES = new HashMap<>();

    Integer page;
    private boolean isDirty = false;
    private List<Integer> dirtyStacks = new ArrayList<>();

    SQLInventory(Player player_, int page_) throws SQLException{
        super(player_, page_);
        this.page = page_;

        Map<Integer, ItemStack> data = Main.PLUGIN.SQLManager.getData(player.getUniqueId(), page);

        int i=0;
        for(Inventory grid : getInventory()){
            for(Inventory row : grid) {
                for(Inventory slot : row) {
                    if(i == 45){
                        if(Session.isSessionActive(player, page - 1)){
                            slot.set(Items.BUTTON_SAVING);
                        }else {
                            slot.set(Items.getPreviousButtonFor(page));
                        }
                    }else if (i == 53){
                        if(Session.isSessionActive(player, page + 1)){
                            slot.set(Items.BUTTON_SAVING);
                        }else {
                            slot.set(Items.getNextButtonFor(page));
                        }
                    }else if (data.containsKey(i)) {
                        slot.set(data.get(i));
                    }
                    i++;
                }
            }
        }
    }

    @Override
    void onOpen(InteractInventoryEvent.Open e){
        OPEN_INVENTORIES.put(player, this);
    }

    @Override
    void onClose(InteractInventoryEvent.Close ignored) {
        OPEN_INVENTORIES.remove(player);
        saveAll();
    }

    public void saveAll(){
        if (isDirty == false) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection conn = Main.PLUGIN.SQLManager.getConnection();
                    int slotNumber = 0;
                    for (Inventory slot : getInventory().slots()) {
                        if(slotNumber == 45 || slotNumber == 53){
                            slotNumber++;
                            continue;
                        }else if(dirtyStacks.contains(slotNumber)) {
                            Optional<ItemStack> stack = slot.peek();
                            if (stack.isPresent()) {
                                String encoded = Loader.encode(stack.get());
                                if (Main.PLUGIN.SQLManager.storeData(conn, player.getUniqueId(), page, slotNumber, encoded) == false) {
                                    player.getInventory().offer(stack.get());
                                    player.sendMessage(Text.of(stack.get().getItem().getName() + " can't be uploaded. Returned item to your inventory."));
                                }
                            } else {
                                Main.PLUGIN.SQLManager.removeData(conn, player.getUniqueId(), page, slotNumber);
                            }
                        }
                        slotNumber++;
                    }
                }catch (SQLException | IOException e){
                    e.printStackTrace();
                }
                Session.endSession(player, page);
                Handler.setPageDone(player);
                player.sendMessage(Text.of("Saved page " + page));
            }
        }).start();
    }

    @Override
    void onClick(ClickInventoryEvent e) {
        for(SlotTransaction t : e.getTransactions()){
            int slotNumber = getSlotNumber(t.getSlot());
            if(slotNumber == 53){  /* NEXT PAGE BUTTON */
                e.setCancelled(true);
                if(page + 1 <= 9) {
                    saveAll();
                    Handler handler = new Handler(player);
                    handler.execute(page + 1);
                }
                return;
            }else if(slotNumber == 45){ /*PREVIOUS PAGE BUTTON */
                e.setCancelled(true);
                if(page - 1 >= 1) {
                    saveAll();
                    Handler handler = new Handler(player);
                    handler.execute(page - 1);
                }
                return;
            }else{
                dirtyStacks.add(slotNumber);
                if(isDirty == false) {
                    Session.startSession(player, page);
                }
                isDirty = true;
            }
        }

    }
}
