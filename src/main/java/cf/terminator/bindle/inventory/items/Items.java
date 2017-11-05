package cf.terminator.bindle.inventory.items;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextFormatting;

public class Items {

    public static final ItemStack BUTTON_SAVING = generateButton(TextFormatting.RED + "Working...", -2, 7);
    private static final ItemStack BUTTON_DISABLED = generateButton(TextFormatting.DARK_GRAY + "Unavailable", -1, 14);

    public static ItemStack getNextButtonFor(int page){
        if(page == 9){
            return BUTTON_DISABLED;
        }
        return generateButton(TextFormatting.GREEN + "Next", page + 1, 5);
    }

    public static ItemStack getPreviousButtonFor(int page){
        if(page == 1){
            return BUTTON_DISABLED;
        }
        return generateButton(TextFormatting.GREEN + "Previous", page - 1, 5);
    }

    private static ItemStack generateButton(String name, int page, int meta) {
        Item type = Item.getItemFromBlock(Blocks.STAINED_GLASS_PANE);
        NBTTagCompound root = new ItemStack(type, 1, meta).serializeNBT();
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound display = new NBTTagCompound();
        NBTTagList loreList = new NBTTagList();
        if(page > 0) { /* NORMAL BUTTONS */
            loreList.appendTag(new NBTTagString(TextFormatting.GRAY + "Go to page: " + page));
            loreList.appendTag(new NBTTagString(TextFormatting.DARK_GRAY + "Please note: After editing a"));
            loreList.appendTag(new NBTTagString(TextFormatting.DARK_GRAY + "page, it needs time to save."));
            loreList.appendTag(new NBTTagString(TextFormatting.DARK_GRAY + ""));
            loreList.appendTag(new NBTTagString(TextFormatting.DARK_GRAY + "/bindle " + page));
        } else if (page == -2) { /* BUTTONS_SAVING */
            loreList.appendTag(new NBTTagString(TextFormatting.DARK_GRAY + "Processing your request."));
            loreList.appendTag(new NBTTagString(TextFormatting.DARK_GRAY + "This may take a while."));
        }
        display.setTag("Lore", loreList);
        display.setTag("Name", new NBTTagString(name));
        tag.setTag("display", display);
        root.setTag("tag", tag);
        return ItemStack.loadItemStackFromNBT(root);
    }
}
