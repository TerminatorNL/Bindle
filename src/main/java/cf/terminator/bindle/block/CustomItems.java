package cf.terminator.bindle.block;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class CustomItems {

    /*
     *  I used textures from http://heads.freshcoal.com
     */

    public static ItemStack CHEST(List<String> lore, NBTTagCompound data) {
        StringBuilder builder = new StringBuilder(200);
        for (String line : lore) {
            builder.append('\"' + line + '\"');
            builder.append(",");
        }
        ItemStack stack = getCustom("{display:{Name:\"" + TextFormatting.AQUA + TextFormatting.BOLD + "Bindle\",Lore:[" + builder.substring(0, Math.max(builder.length() - 1, 0)) + "]},SkullOwner:{Id:\"148ce164-81e8-43d3-b057-4b21cf96d9d3\",Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmY2OGQ1MDliNWQxNjY5Yjk3MWRkMWQ0ZGYyZTQ3ZTE5YmNiMWIzM2JmMWE3ZmYxZGRhMjliZmM2ZjllYmYifX19\"}]}}}");
        stack.setTagInfo("Bindle", data);
        return stack;
    }


    public static ItemStack getCustom(String JSON) {
        try {
            NBTTagCompound tag = JsonToNBT.getTagFromJson(JSON);
            ItemStack skull = new ItemStack(Items.SKULL, 1, 3);
            skull.setTagCompound(tag);
            return skull;
        } catch (NBTException e) {
            throw new RuntimeException(e);
        }
    }
}
