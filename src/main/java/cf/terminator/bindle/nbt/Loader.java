package cf.terminator.bindle.nbt;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.ItemStack;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Loader {

    public static DataContainer decode(String s) throws IOException{
        return decode(new ByteArrayInputStream(DatatypeConverter.parseHexBinary(s)));
    }

    public static DataContainer decode(InputStream stream) throws IOException{
        return DataFormats.NBT.readFrom(stream);
    }

    public static NBTTagCompound decodeFile(InputStream stream) throws IOException {
        return CompressedStreamTools.readCompressed(stream);
    }

    public static String encode(ItemStack data) throws IOException{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataFormats.NBT.writeTo(outputStream, data.createSnapshot().toContainer());
        return DatatypeConverter.printHexBinary(outputStream.toByteArray());
    }
}
