package top.blug.towerdefense;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import top.blug.towerdefense.command.TDCommand;
import top.blug.towerdefense.command.TDTabCompleter;
import top.blug.towerdefense.arena.Arena;
import top.blug.towerdefense.arena.MapManager;
import top.blug.towerdefense.arena.SelectMapListener;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class Main extends JavaPlugin {

    private String pluginPrefix;
    private MonstersConfig monstersConfig;
    private TDCommand tdCommand;
    private File arenasFile;
    private FileConfiguration arenasConfig;
    private List<Arena> arenaList; // 存储所有地图的列表
    private HashMap<UUID, Arena> playerArenaMap; // 存储玩家与地图的对应关系
    private MapManager mapManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // 先读取 pluginPrefix
        pluginPrefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("plugin-prefix", "&2TowerDefense"));
        printLoadMessage(); // 在获取了插件前缀后再打印加载消息

        monstersConfig = new MonstersConfig(this);
        monstersConfig.loadMonstersConfig();

        createArenasConfig(); // 初始化和加载 arenas.yml 配置文件
        playerArenaMap = new HashMap<>(); // 初始化玩家与地图关系的 HashMap
        mapManager = new MapManager(this, arenasFile, arenasConfig, pluginPrefix); // 初始化 MapManager

        // 注册命令
        tdCommand = new TDCommand(this);
        getCommand("towerdefense").setExecutor(tdCommand); // 注册命令执行器
        getCommand("towerdefense").setTabCompleter(new TDTabCompleter()); // 注册命令补全器
        getServer().getPluginManager().registerEvents(new SelectMapListener(this, mapManager.getArenaList(), pluginPrefix), this);

    }

    @Override
    public void onDisable() {
        printUnloadMessage();
        // 插件关闭时的逻辑
    }

    private void printLoadMessage() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "==============================");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + pluginPrefix + ChatColor.GREEN + " 插件已加载.");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Powered by Ampura16 " + ChatColor.GOLD + " @ BlockLand Studio");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "==============================");
    }

    private void printUnloadMessage() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "==============================");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + pluginPrefix + ChatColor.GREEN + " 插件已卸载.");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Powered by Ampura16 " + ChatColor.GOLD + " @ BlockLand Studio");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "==============================");
    }

    // 创建 arenas.yml 配置文件
    private void createArenasConfig() {
        arenasFile = new File(getDataFolder(), "arenas.yml");
        if (!arenasFile.exists()) {
            try {
                arenasFile.getParentFile().mkdirs();
                arenasFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("无法创建 arenas.yml 文件!");
            }
        }
        arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);

        // 使用 Arena 类的方法加载地图信息
        arenaList = Arena.loadArenasFromConfig(arenasConfig); // 调用静态方法加载 Arena 列表
    }

    // 获取 MapManager 实例
    public MapManager getMapManager() {
        return mapManager;
    }

    public File getArenasFile() {
        return arenasFile;
    }

    public FileConfiguration getArenasConfig() {
        return arenasConfig;
    }

    public String getPluginPrefix() {
        return pluginPrefix;
    }
}
