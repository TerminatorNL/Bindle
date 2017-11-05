package cf.terminator.bindle.forgeinventory;

import cf.terminator.bindle.Main;
import cf.terminator.bindle.inventory.Session;
import cf.terminator.bindle.inventory.items.Items;
import cf.terminator.bindle.nbt.Loader;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.Sponge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ForgeSQLInventory extends InventoryBasic implements IInventoryChangedListener {

    public static final HashMap<EntityPlayer, ForgeSQLInventory> OPEN_INVENTORY = new HashMap<>();

    private final int page;
    private final Map<Integer, ItemStack> data;
    private final Map<Integer, ItemStack> oldData = new HashMap<>();
    private final EntityPlayerMP player;

    private final int BINDLE_PREV_BUTTON = 45;
    private final int BINDLE_NEXT_BUTTON = 53;

    private ItemStack BUTTON_NEXT;
    private ItemStack BUTTON_PREV;

    private boolean hasChangedPage = false;
    private boolean hasClosedPage = false;


    public ForgeSQLInventory(EntityPlayerMP player, int page) throws SQLException {
        super("Bindle (" + page + "/9)", true, 54);
        this.player = player;
        this.page = page;
        data = Main.PLUGIN.SQLManager.getData(player.getUniqueID(), page);
        oldData.putAll(data);
        data.put(BINDLE_PREV_BUTTON, BUTTON_PREV);
        data.put(BINDLE_NEXT_BUTTON, BUTTON_NEXT);
        addInventoryChangeListener(this);
    }

    @Nullable
    @Override
    public ItemStack getStackInSlot(int slot) {
        return data.get(slot);
    }

    @Nullable
    @Override
    public ItemStack decrStackSize(int slot, int count) {
        if (slot == BINDLE_PREV_BUTTON || slot == BINDLE_NEXT_BUTTON) {
            checkPlayer(true);
            switchPages(slot == BINDLE_PREV_BUTTON ? BUTTON.PREVIOUS : BUTTON.NEXT);
            return null;
        }
        ItemStack stored = data.get(slot);
        if (count >= stored.stackSize) {
            return data.remove(slot);
        }
        stored.stackSize = stored.stackSize - count;
        ItemStack result = stored.copy();
        result.stackSize = count;
        data.put(slot, stored);
        return result;
    }

    @Nullable
    @Override
    public ItemStack addItem(ItemStack stack) {
        return super.addItem(stack);
    }


    @Nullable
    @Override
    public ItemStack removeStackFromSlot(int slot) {
        if (slot == BINDLE_PREV_BUTTON || slot == BINDLE_NEXT_BUTTON) {
            checkPlayer(true);
            switchPages(slot == BINDLE_PREV_BUTTON ? BUTTON.PREVIOUS : BUTTON.NEXT);
            return null;
        }
        return data.remove(slot);
    }


    @Override
    public void setInventorySlotContents(int slot, @Nullable ItemStack stack) {
        if (slot == BINDLE_PREV_BUTTON || slot == BINDLE_NEXT_BUTTON) {
            checkPlayer(true);
            switchPages(slot == BINDLE_PREV_BUTTON ? BUTTON.PREVIOUS : BUTTON.NEXT);
            return;
        }
        if (stack == null) {
            data.remove(slot);
        } else {
            data.put(slot, stack);
        }
    }

    @Override
    public void openInventory(EntityPlayer player) {
        OPEN_INVENTORY.put(player, this);
        generateButtons();
        updateButtons();
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (hasClosedPage == true) {
            return;
        }
        hasClosedPage = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean changed = false;
                    Session.startSession(player, page);
                    Connection conn = Main.PLUGIN.SQLManager.getConnection();
                    for (int slot = 0; slot < getSizeInventory(); slot++) {
                        if (slot == BINDLE_NEXT_BUTTON || slot == BINDLE_PREV_BUTTON) {
                            continue;
                        }
                        ItemStack oldStack = oldData.get(slot);
                        ItemStack newStack = data.get(slot);
                        if (oldStack == null && newStack == null) {
                            continue;
                        }
                        if (oldStack == null) {
                            /* An item was added */
                            changed = true;
                            Main.PLUGIN.SQLManager.storeData(conn, player.getUniqueID(), page, slot, Loader.encode(data.remove(slot)));
                        } else if (newStack == null) {
                            /* Item was removed */
                            changed = true;
                            Main.PLUGIN.SQLManager.removeData(conn, player.getUniqueID(), page, slot);
                        } else {
                            /* There's still an item. If it updated, we upload it. */
                            NBTTagCompound oldTag = oldStack.serializeNBT();
                            NBTTagCompound newTag = newStack.serializeNBT();
                            if (oldTag.equals(newTag) == false) {
                                /* Slot was updated, uploading new data. */
                                changed = true;
                                Main.PLUGIN.SQLManager.storeData(conn, player.getUniqueID(), page, slot, Loader.encode(data.remove(slot)));
                            }
                        }
                    }
                    if (changed) {
                        player.addChatMessage(new TextComponentString(TextFormatting.GREEN + "Saved page " + page));
                    }
                } catch (SQLException | IOException e) {
                    /* Give all remaining items back to the player, we messed up. */
                    Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
                        @Override
                        public void run() {
                            for (Map.Entry<Integer, ItemStack> failed : data.entrySet()) {
                                player.addChatMessage(new TextComponentString(TextFormatting.RED + "Item failed: " + TextFormatting.WHITE + failed.getValue().getDisplayName()));
                                if (player.inventory.addItemStackToInventory(failed.getValue()) == false) {
                                    player.addChatMessage(new TextComponentString(TextFormatting.RED + "No space in inventory, dropped item on floor"));
                                    EntityItem item = new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, failed.getValue());
                                    player.worldObj.spawnEntityInWorld(item);
                                }
                            }
                        }
                    }).submit(Main.PLUGIN);
                    e.printStackTrace();
                }
                Session.endSession(player, page);
                if (OPEN_INVENTORY.get(player) != null && OPEN_INVENTORY.get(player) != ForgeSQLInventory.this) {
                    OPEN_INVENTORY.get(player).generateButtons();
                    OPEN_INVENTORY.get(player).updateButtons();
                }
                OPEN_INVENTORY.remove(player, ForgeSQLInventory.this);
            }
        }, "Bindle: uploading " + player.getName() + "...").start();
    }

    private void checkPlayer(boolean mustRefresh) {
        boolean switchedPages = false;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null) {
                if (isButton(stack)) {
                    player.inventory.setInventorySlotContents(i, null);
                    BUTTON_PREV = Items.getPreviousButtonFor(page).copy();
                    BUTTON_NEXT = Items.getNextButtonFor(page).copy();
                    switchedPages = switchPages(stack.getDisplayName().equals(BUTTON_PREV.getDisplayName()) ? BUTTON.PREVIOUS : BUTTON.NEXT);
                }
            }
        }
        if (switchedPages == false && mustRefresh) {
            player.displayGUIChest(this);
        }
    }

    private boolean isButton(@Nonnull ItemStack stack) {
        return stack.getDisplayName().equals(BUTTON_PREV.getDisplayName()) || stack.getDisplayName().equals(BUTTON_NEXT.getDisplayName());
    }

    @Override
    public void onInventoryChanged(InventoryBasic invBasic) {
        checkPlayer(false);
    }

    private boolean switchPages(BUTTON mode) {
        int newPage;
        if (mode == BUTTON.NEXT) {
            newPage = Math.min(page + 1, 9);
        } else {
            newPage = Math.max(page - 1, 1);
        }
        if (page == newPage || Session.isSessionActive(player, newPage)) {
            return false;
        }
        try {
            if (hasChangedPage == false) {
                hasChangedPage = true;
                ForgeSQLInventory newGUI = new ForgeSQLInventory(player, newPage);
                player.displayGUIChest(newGUI);
            }
        } catch (SQLException e) {
            player.addChatMessage(new TextComponentString(TextFormatting.RED + "Unable to open bindle! There was an error communicating with the database."));
            e.printStackTrace();
        }
        return true;
    }

    public void generateButtons() {
        if (Session.isSessionActive(player, page + 1)) {
            BUTTON_NEXT = Items.BUTTON_SAVING.copy();
        } else {
            BUTTON_NEXT = Items.getNextButtonFor(page);
        }
        if (Session.isSessionActive(player, page - 1)) {
            BUTTON_PREV = Items.BUTTON_SAVING.copy();
        } else {
            BUTTON_PREV = Items.getPreviousButtonFor(page);
        }
    }

    public void updateButtons() {
        data.put(BINDLE_PREV_BUTTON, BUTTON_PREV);
        data.put(BINDLE_NEXT_BUTTON, BUTTON_NEXT);
        super.setInventorySlotContents(BINDLE_NEXT_BUTTON, BUTTON_NEXT);
        super.setInventorySlotContents(BINDLE_PREV_BUTTON, BUTTON_PREV);
    }

    private enum BUTTON {
        NEXT, PREVIOUS
    }
}
