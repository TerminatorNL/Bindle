package cf.terminator.bindle.inventory;

import cf.terminator.bindle.Main;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.AffectItemStackEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.text.Text;

import java.util.Collection;

public abstract class BasicInventory {

    protected final Player player;
    private Inventory inventory;
    private int page;

    public BasicInventory(Player player, int page){
        this.player = player;
        this.page = page;
    }

    public void setInventory(Inventory i){
        inventory = i;
    }

    public Inventory getInventory(){
        if(inventory == null){
            inventory = setupInventory();
        }
        return inventory;
    }

    private Inventory setupInventory() {

        return Inventory.builder()
                .of(InventoryArchetypes.DOUBLE_CHEST)
                .withCarrier(player)
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9, 6))
                .property(InventoryTitle.PROPERTY_NAME, new InventoryTitle(Text.of("Bindle (" + page + "/9)")))
                .listener(InteractInventoryEvent.Open.class, this::onOpen)
                .listener(InteractInventoryEvent.Close.class, this::onClose)
                .listener(ClickInventoryEvent.class, this::onClick)
                .build(Main.getInstance());

    }

    public static int getSlotNumber(Slot slot){
        Collection<SlotIndex> properties = slot.parent().getProperties(slot, SlotIndex.class);
        return properties.iterator().next().getValue();
    }

    abstract void onOpen(InteractInventoryEvent.Open e);
    abstract void onClose(InteractInventoryEvent.Close e);
    abstract void onClick(ClickInventoryEvent e);

}
