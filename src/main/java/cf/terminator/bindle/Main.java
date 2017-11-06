package cf.terminator.bindle;

import cf.terminator.bindle.commands.Register;
import cf.terminator.bindle.commands.Reload;
import cf.terminator.bindle.config.Manager;
import cf.terminator.bindle.mysql.SQLManager;
import com.google.inject.Inject;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockSkull;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Path;

import static cf.terminator.bindle.Main.*;

@Plugin(name = MOD_NAME, id = MOD_ID, version = VERSION)
public class Main {

    static final String MOD_ID = "bindle";
    static final String MOD_NAME = "Bindle";
    static final String VERSION = "4.0";

    public static Main PLUGIN;
    public static Logger LOGGER;
    public static Path DEFAULT_CONFIG;
    public static Manager CONFIG_MANAGER;
    public static MinecraftServer MINECRAFT_SERVER;
    private final Object key = new Object();
    public Manager.Credentials Credentials;
    public SQLManager SQLManager;
    @Inject
    private Logger LOGGER_;
    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path DEFAULT_CONFIG_;

    public static boolean isValidLocation(World world, BlockPos pos) {
        return !(isChest(world, pos.north()) || isChest(world, pos.east()) || isChest(world, pos.south()) || isChest(world, pos.west()));
    }

    public static boolean isChest(World w, BlockPos pos) {
        return w.getBlockState(pos).getBlock() instanceof BlockChest;
    }

    @Listener
    public void onStart(org.spongepowered.api.event.game.state.GamePreInitializationEvent ignored){
        LOGGER = LOGGER_;
        PLUGIN = this;
        MINECRAFT_SERVER = FMLCommonHandler.instance().getMinecraftServerInstance();
        DEFAULT_CONFIG = DEFAULT_CONFIG_;
        CommandSpec reload = CommandSpec.builder()
                .description(Text.of("Reloads Bindle, used for MySQL testing."))
                .permission("bindle.admin.reload")
                .executor(new Reload())
                .build();
        Sponge.getCommandManager().register(this, reload, "bindlereload");
        load();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void load(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CONFIG_MANAGER = new Manager();
                    SQLManager = new SQLManager();
                    Credentials = CONFIG_MANAGER.setupConfig(key);
                    SQLManager.prepareDatabase(key);
                    Register.registerAll(Main.this);
                } catch (IOException e) {
                    LOGGER.info("ERROR while setting up configuration!");

                    e.printStackTrace();
                }
            }
        }).start();
    }

    @SubscribeEvent
    public void onPlace(BlockEvent.PlaceEvent e) {
        if (e.getPlacedBlock().getBlock() instanceof BlockSkull) {
            if ("148ce164-81e8-43d3-b057-4b21cf96d9d3".equals(e.getBlockSnapshot().getNbt().getCompoundTag("Owner").getString("Id"))) {
                e.setCanceled(true);
                MinecraftForge.EVENT_BUS.register(new PlayerSkullChecker(e));
            }
        }
    }

    class PlayerSkullChecker {

        BlockEvent.PlaceEvent event;

        PlayerSkullChecker(BlockEvent.PlaceEvent e) {
            event = e;
        }

        @SubscribeEvent
        public void tick(TickEvent.ServerTickEvent e) {
            MinecraftForge.EVENT_BUS.unregister(this);
            if (isValidLocation(event.getWorld(), event.getPos()) == false) {
                event.getPlayer().addChatMessage(new TextComponentString(TextFormatting.RED + "Woops! That doesn't go there!"));
                return;
            }
            ItemStack stack = event.getPlayer().getHeldItem(event.getHand());
            NBTTagCompound TILE_ENTITY_TAG = stack.getTagCompound().getCompoundTag("Bindle");
            TileEntity tileEntity = TileEntity.create(event.getWorld(), TILE_ENTITY_TAG);

            event.getWorld().setBlockState(event.getPos(), Blocks.CHEST.getDefaultState());
            event.getWorld().setTileEntity(event.getPos(), tileEntity);

            /* Consume item */
            if (event.getHand() == EnumHand.MAIN_HAND) {
                event.getPlayer().inventory.removeStackFromSlot(event.getPlayer().inventory.currentItem);
            } else {
                event.getPlayer().inventory.offHandInventory[0] = null; //Offhand slot
            }
        }
    }
}
