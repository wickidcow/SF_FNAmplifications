package ne.fnfal113.fnamplifications;

import javax.annotation.Nonnull;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;

import ne.fnfal113.fnamplifications.config.ConfigManager;
import ne.fnfal113.fnamplifications.gears.commands.GearCommands;
import ne.fnfal113.fnamplifications.gears.runnables.ArmorEquipRunnable;
import ne.fnfal113.fnamplifications.integrations.VaultIntegration;
import ne.fnfal113.fnamplifications.test.ShockwaveTest;
import ne.fnfal113.fnamplifications.items.FNAmpItemSetup;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public final class FNAmplifications extends JavaPlugin implements SlimefunAddon {

    private static FNAmplifications instance;
    private static VaultIntegration vaultIntegration;

    private final ConfigManager configManager = new ConfigManager();

    @Override
    public void onEnable() {
        setInstance(this);

        new Metrics(this, 13219);

        getLogger().info("************************************************************");
        getLogger().info("*        FN Amplifications - Slimefun Legacy Edition       *");
        getLogger().info("*             Original project by FN_FAL113                *");
        getLogger().info("*          Minecraft 1.21.11+ / Paper 26.2 target          *");
        getLogger().info("************************************************************");

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        setVaultIntegration(this);
        FNAmpItemSetup.INSTANCE.init();
        registerCommands();

        // ArmorEquipRunnable reads and updates Bukkit player state, so keep it on
        // the server thread for Paper and Paper-derived servers.
        long period = Math.max(1L, getConfig().getLong("armor-update-period", 10L)) * 20L;
        getServer().getScheduler().runTaskTimer(this, new ArmorEquipRunnable(), 0L, period);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(FNAmplifications.getInstance());
        getLogger().log(Level.INFO, "Cancelled FN Amplifications tasks");
    }

    public void registerCommands() {
        Objects.requireNonNull(getCommand("fngear"), "fngear command is missing from plugin.yml")
            .setExecutor(new GearCommands());
        Objects.requireNonNull(getCommand("fnshockwavetest"), "fnshockwavetest command is missing from plugin.yml")
            .setExecutor(new ShockwaveTest());
    }

    @Nonnull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/wickidcow/SF_FNAmplifications/issues";
    }

    public ConfigManager getConfigManager() {
        return instance.configManager;
    }

    private static void setInstance(FNAmplifications ins) {
        instance = ins;
    }

    public static FNAmplifications getInstance() {
        return instance;
    }

    public static void setVaultIntegration(FNAmplifications ins) {
        vaultIntegration = new VaultIntegration(ins);
    }

    public static VaultIntegration getVaultIntegration() {
        return vaultIntegration;
    }
}
