package cf.terminator.bindle.mysql;

import cf.terminator.bindle.Main;
import cf.terminator.bindle.nbt.Loader;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class SQLManager {

    private SqlService sql;
    private String url;

    private Connection conn;

    public Connection getConnection() throws SQLException {
        if (sql == null) {
            sql = Sponge.getServiceManager().provide(SqlService.class).get();
        }
        if(conn == null){
            conn = sql.getDataSource(url).getConnection();
        }else if(conn.isValid(2000) == false){
            conn.close();
            conn = sql.getDataSource(url).getConnection();
        }
        return conn;
    }


    public void prepareDatabase(Object key){
        url = Main.PLUGIN.Credentials.getUrl(key);
        String sql = "CREATE TABLE IF NOT EXISTS `Data` (" +
                "`Player` TINYTEXT NOT NULL," +
                "`Stored` LONGTEXT NOT NULL," +
                "`Self` LONGTEXT NOT NULL," +
                "PRIMARY KEY (Player(36)));";
        try{
            conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeQuery();
        }catch (SQLException e){
            e.printStackTrace();
        }
        Main.LOGGER.info("SQL database: ready");
    }

    public boolean storeData(UUID uuid, NBTTagCompound[] stored, NBTTagCompound self) {
        try{
            if (isEmptyNBT(stored) && isEmptyNBT(self)) {
                removeData(uuid);
                return true;
            }
            ArrayList<String> storedList = new ArrayList<>();
            for (NBTTagCompound tag : stored) {
                storedList.add(Loader.encodeTag(tag));
            }
            StringBuilder build = new StringBuilder();
            build.append("INSERT INTO Data (`Player`, `Stored`, `Self`)");
            build.append(" VALUES('");
            build.append(uuid.toString());
            build.append("','");
            build.append(String.join(",", storedList));
            build.append("','");
            build.append(Loader.encodeTag(self));
            build.append("') ");
            build.append("ON DUPLICATE KEY UPDATE `Player`=VALUES(`Player`), `Stored`=VALUES(`Stored`), `Self`=VALUES(`Self`);");
            String sql = build.toString();
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            stmt.executeQuery();
            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean isEmptyNBT(NBTTagCompound tag) {
        return tag.equals(new NBTTagCompound());
    }

    private boolean isEmptyNBT(NBTTagCompound[] tagList) {
        for (NBTTagCompound tag : tagList) {
            if (isEmptyNBT(tag) == false) {
                return false;
            }
        }
        return true;
    }

    private void removeData(UUID uuid) throws SQLException {
        try{
            String sql = "DELETE FROM `Data` WHERE `Player` = '" + uuid.toString() + "';";
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            stmt.executeQuery();
        }catch (SQLException e){
            throw new SQLException(e);
        }
    }


    public SQLResult getData(UUID uuid) throws SQLException {
        String sql = "SELECT * FROM Data WHERE (Player='" + uuid.toString() + "');";
        try{
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return new SQLResult(result.getString("Stored"), result.getString("Self"));
            } else {
                return new SQLResult(null, null);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public class SQLResult {
        public final NBTTagCompound[] stored;
        public final NBTTagCompound playerData;

        SQLResult(String stored, String playerData) {
            if (stored == null) {
                this.stored = new NBTTagCompound[0];
            } else {
                String[] tmp;
                if (stored.contains(",")) {
                    tmp = stored.split(",");
                } else {
                    tmp = new String[]{stored};
                }
                this.stored = new NBTTagCompound[tmp.length];
                for (int i = 0; i < tmp.length; i++) {
                    this.stored[i] = Loader.decodeFromSQL(tmp[i]);
                }
            }
            if (playerData == null) {
                this.playerData = new NBTTagCompound();
            } else {
                this.playerData = Loader.decodeFromSQL(playerData);
            }
        }
    }
}
