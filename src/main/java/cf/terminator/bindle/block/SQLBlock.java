package cf.terminator.bindle.block;

import cf.terminator.bindle.Main;
import cf.terminator.bindle.mysql.SQLManager;
import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.Player;

import java.sql.SQLException;
import java.util.ArrayList;

public class SQLBlock {

    EntityPlayer player;
    BlockPos pos;
    World world;

    public SQLBlock(EntityPlayer player, BlockPos pos) {
        this.player = player;
        this.world = player.getEntityWorld();
        this.pos = pos;

        TileEntity entity = world.getTileEntity(pos);
        if (entity instanceof TileEntityChest) {
            TileEntityChest chest = (TileEntityChest) entity;
            ArrayList<String> list = new ArrayList<>();
            for (int slot = 0; slot < chest.getSizeInventory(); slot++) {
                ItemStack stack = chest.getStackInSlot(slot);
                if (stack != null) {
                    list.add(TextFormatting.GOLD + stack.getDisplayName() + TextFormatting.DARK_GRAY + " x" + stack.stackSize);
                }
            }
            IBlockState oldState = world.getBlockState(pos);

            NBTTagCompound tag = CustomItems.CHEST(list, chest.serializeNBT()).serializeNBT();
            world.removeTileEntity(pos);
            world.destroyBlock(pos, false);

            ParticleEffect effect = ParticleEffect.builder().type(ParticleTypes.FERTILIZER).quantity(100).build();
            ParticleEffect effect2 = ParticleEffect.builder().type(ParticleTypes.HEART).quantity(100).build();
            ((Player) player).getLocation().getExtent().spawnParticles(effect, new Vector3d(pos.getX(), pos.getY(), pos.getZ()));
            ((Player) player).getLocation().getExtent().spawnParticles(effect2, new Vector3d(pos.getX(), pos.getY(), pos.getZ()));

            player.addChatMessage(new TextComponentString(TextFormatting.GREEN + "Poof! " + TextFormatting.GRAY + "If you want to retrieve it, use " + TextFormatting.GOLD + "/bindle open"));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        SQLManager.SQLResult result = Main.PLUGIN.SQLManager.getData(player.getUniqueID());
                        NBTTagCompound[] newInventory = new NBTTagCompound[result.stored.length + 1];
                        for (int i = 0; i < result.stored.length; i++) {
                            newInventory[i] = result.stored[i];
                        }
                        newInventory[result.stored.length] = tag;
                        Main.PLUGIN.SQLManager.storeData(player.getUniqueID(), newInventory, result.playerData);
                    } catch (SQLException e) {
                        Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
                            @Override
                            public void run() {
                                player.addChatMessage(new TextComponentString(TextFormatting.RED + "Upload failed! Block was replaced."));
                                world.setBlockState(pos, oldState);
                                world.setTileEntity(pos, chest);
                            }
                        }).submit(Main.PLUGIN);
                    }
                }
            }).start();
        } else {
            player.addChatMessage(new TextComponentString(TextFormatting.RED + "That is not a valid inventory!"));
        }
    }

    public void eatBlock() {
        world.getTileEntity(pos);

    }
}
