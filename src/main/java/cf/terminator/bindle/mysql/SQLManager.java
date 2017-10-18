package cf.terminator.bindle.mysql;

import cf.terminator.bindle.Main;
import cf.terminator.bindle.nbt.Loader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.sql.SqlService;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
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
        String sql = "CREATE TABLE IF NOT EXISTS `Data` " +
                "(`Player` TINYTEXT NOT NULL," +
                "`Page` TINYINT NOT NULL," +
                "`Slot` TINYINT NOT NULL," +
                "`Item` LONGTEXT NOT NULL," +
                "PRIMARY KEY (Player(36), Page, Slot));";
        try{
            conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeQuery();
        }catch (SQLException e){
            e.printStackTrace();
        }
        Main.LOGGER.info("SQL database: ready");
    }

    public boolean storeData(Connection conn, UUID uuid, int page, int slot, String data){
        StringBuilder build = new StringBuilder();
        build.append("INSERT INTO Data (Player, Page, Slot, Item)");
        build.append(" VALUES('");
        build.append(uuid.toString());
        build.append("',");
        build.append(page);
        build.append(",");
        build.append(slot);
        build.append(",");
        build.append("'");
        build.append(data);
        build.append("') ");
        build.append("ON DUPLICATE KEY UPDATE `Player`=VALUES(`Player`), `Page`=VALUES(`Page`), `Slot`=VALUES(`Slot`), `Item`=VALUES(`Item`);");
        String sql = build.toString();
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeQuery();
            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeData(Connection conn, UUID uuid, int page, int slot){
        String sql = "DELETE FROM `Data` WHERE `Player` = '" + uuid.toString() + "' AND `Page` = " + page+ " AND `Slot` = " + slot + ";";
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeQuery();
            return true;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }


    public Map<Integer, ItemStack> getData(UUID uuid, int page) throws SQLException{
        Map<Integer, ItemStack> map = new HashMap<>();
        String sql = "SELECT * FROM Data WHERE (Player='" + uuid.toString() + "') AND (Page=" + page + ");";
        try{
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet result = stmt.executeQuery();
            while(result.next()){
                map.put(result.getInt("Slot"), ItemStack.builder().fromContainer(Loader.decode(result.getString("Item"))).build());
            }
        }catch (SQLException | IOException e){
            throw new SQLException(e);
        }
        return map;
    }
}
