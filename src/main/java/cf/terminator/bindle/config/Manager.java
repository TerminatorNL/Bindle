package cf.terminator.bindle.config;

import cf.terminator.bindle.Main;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;

import java.io.IOException;

public class Manager {

    public Credentials setupConfig(Object key) throws IOException {
        if(Main.DEFAULT_CONFIG.toFile().exists() == false){
            Sponge.getAssetManager().getAsset(Main.PLUGIN, "config.conf").get().copyToFile(Main.DEFAULT_CONFIG);
        }
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(Main.DEFAULT_CONFIG).build();
        CommentedConfigurationNode config = loader.load();

        return new Credentials(key, config.getNode("url").getString());
    }

    public class Credentials{
        private Object key;
        private String url;

        Credentials(Object key, String url){
            this.key = key;
            this.url = url;
        }

        public String getUrl(Object key){
            if(this.key == key){
                return this.url;
            }else{
                RuntimeException e = new RuntimeException("WOOPS! SOMETHING TRIED TO GET YOUR PASSWORD!");
                e.printStackTrace();
                throw e;
            }
        }
    }
}
