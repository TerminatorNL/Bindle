package cf.terminator.bindle.nbt;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Loader {

    public static NBTTagCompound decodeFromSQL(String s) {
        if (s == null || s.equals("")) {
            return new NBTTagCompound();
        } else {
            return decodeFromSQL(new ByteArrayInputStream(DatatypeConverter.parseHexBinary(s)));
        }
    }


    public static NBTTagCompound decodeFromSQL(InputStream s) {
        if (s == null) {
            return new NBTTagCompound();
        }
        try {
            return CompressedStreamTools.readCompressed(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeTag(NBTTagCompound tag) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(tag, b);
            return DatatypeConverter.printHexBinary(b.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}