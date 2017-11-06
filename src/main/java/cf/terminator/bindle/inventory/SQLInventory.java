package cf.terminator.bindle.inventory;

import cf.terminator.bindle.Main;
import cf.terminator.bindle.mysql.SQLManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

public class SQLInventory implements IInventory {

    public static List<UUID> SESSIONS = new ArrayList<>();

    TreeMap<Integer, ItemStack> stored = new TreeMap<>();
    SQLManager.SQLResult start;
    EntityPlayer player;


    public SQLInventory(EntityPlayer player) {
        this.player = player;
        try {
            start = Main.PLUGIN.SQLManager.getData(player.getUniqueID());
            int slot = 0;
            for (NBTTagCompound tag : start.stored) {
                ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
                if (stack != null && stack.stackSize > 0) {
                    stored.put(slot, stack);
                    slot++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.addChatMessage(new TextComponentString(TextFormatting.RED + "A database error occurred! We cant open your bindle right now."));
        }
    }


    @Override
    public void closeInventory(EntityPlayer ignored) {
        List<NBTTagCompound> currentItems = new ArrayList<>();
        for (int slot = 0; slot < getSizeInventory(); slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (stack != null && stack.stackSize == 1) {
                currentItems.add(stack.serializeNBT());
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Main.PLUGIN.SQLManager.storeData(player.getUniqueID(), currentItems.toArray(new NBTTagCompound[0]), start.playerData);
                SESSIONS.remove(SQLInventory.this.player.getUniqueID());
            }
        }).start();
    }


    @Override
    public int getSizeInventory() {
        return 54;
    }

    @Nullable
    @Override
    public ItemStack getStackInSlot(int slot) {
        return stored.get(slot);
    }

    @Nullable
    @Override
    public ItemStack decrStackSize(int slot, int count) {
        return stored.remove(slot);
    }

    @Nullable
    @Override
    public ItemStack removeStackFromSlot(int slot) {
        return stored.remove(slot);
    }

    @Override
    public void setInventorySlotContents(int slot, @Nullable ItemStack stack) {
        /* Cannot insert, only take items out. */
        player.inventory.addItemStackToInventory(stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {
        if (SESSIONS.contains(this.player.getUniqueID()) == false) {
            SESSIONS.add(this.player.getUniqueID());
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }
}
