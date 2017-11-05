package cf.terminator.bindle.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Loader {

    public static ItemStack decodeString(String s) throws IOException {
        return decodeStream(new ByteArrayInputStream(DatatypeConverter.parseHexBinary(s)));
    }

    private static ItemStack decodeStream(InputStream stream) throws IOException {
        NBTTagCompound tag = CompressedStreamTools.readCompressed(stream);
        return ItemStack.loadItemStackFromNBT(tag);
    }

    public static NBTTagCompound decodePlayer(InputStream stream) throws IOException {
        return CompressedStreamTools.readCompressed(stream);
    }

    public static String encode(ItemStack data) throws IOException{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(data.serializeNBT(), outputStream);
        return DatatypeConverter.printHexBinary(outputStream.toByteArray());
    }
}