package cf.terminator.bindle;

import cf.terminator.bindle.commands.Register;
import cf.terminator.bindle.commands.Reload;
import cf.terminator.bindle.config.Manager;
import cf.terminator.bindle.mysql.SQLManager;
import com.google.inject.Inject;
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
    static final String VERSION = "1.0";

    public static Main PLUGIN;
    public static Logger LOGGER;
    public static Path DEFAULT_CONFIG;
    public static Manager CONFIG_MANAGER;
    private final Object key = new Object();
    public Manager.Credentials Credentials;
    public SQLManager SQLManager;
    @Inject
    private Logger LOGGER_;
    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path DEFAULT_CONFIG_;

    public static Main getInstance() {
        return PLUGIN;
    }

    @Listener
    public void onStart(org.spongepowered.api.event.game.state.GamePreInitializationEvent ignored){
        LOGGER = LOGGER_;
        PLUGIN = this;
        DEFAULT_CONFIG = DEFAULT_CONFIG_;
        CommandSpec reload = CommandSpec.builder()
                .description(Text.of("Reloads Bindle, used for MySQL testing."))
                .permission("bindle.admin.reload")
                .executor(new Reload())
                .build();
        Sponge.getCommandManager().register(this, reload, "bindlereload");
        load();
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
}
