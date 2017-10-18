package cf.terminator.bindle.inventory.items;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;

public class Items {

    public static ItemStack getNextButtonFor(int page){
        if(page == 9){
            return BUTTON_DISABLED;
        }
        return generateButton(Text.of(TextColors.GREEN, "Next"), page + 1, 5);
    }

    public static ItemStack getPreviousButtonFor(int page){
        if(page == 1){
            return BUTTON_DISABLED;
        }
        return generateButton(Text.of(TextColors.GREEN, "Previous"), page - 1, 5);
    }

    private static final ItemStack BUTTON_DISABLED = generateButton(Text.of(TextColors.DARK_GRAY,"Unavailable"), -1, 14);
    public static final ItemStack BUTTON_SAVING = generateButton(Text.of(TextColors.RED, "Working..."), -2, 7);

    private static DataContainer getBasicButton(int meta){
        DataContainer container = ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .quantity(1)
                .build().toContainer();
        return container.set(DataQuery.of("UnsafeDamage"), meta);
    }

    private static ItemStack generateButton(Text text, int page, int meta){
        ItemStack stack = ItemStack.builder().fromContainer(getBasicButton(meta)).build();
        stack.offer(Keys.DISPLAY_NAME, text);
        if(page > 0) { /* NORMAL BUTTONS */
            List<Text> lore = new ArrayList<>();
            lore.add(Text.of(TextColors.GRAY, "Go to page: " + page));
            lore.add(Text.of(TextColors.DARK_GRAY, "Please note: After editing a"));
            lore.add(Text.of(TextColors.DARK_GRAY, "page, it needs time to save."));
            lore.add(Text.of(TextColors.DARK_GRAY, ""));
            lore.add(Text.of(TextColors.DARK_GRAY, "/bindle " + page));
            stack.offer(Keys.ITEM_LORE, lore);
        }else if (page == -2){ /* BUTTONS_SAVING */
            List<Text> lore = new ArrayList<>();
            lore.add(Text.of(TextColors.GRAY, "Processing your request."));
            lore.add(Text.of(TextColors.GRAY, "This may take a while."));
            stack.offer(Keys.ITEM_LORE, lore);
        }
        return stack;
    }
}
