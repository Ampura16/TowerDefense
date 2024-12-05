package top.blug.towerdefense.arena;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import top.blug.towerdefense.Main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapManager {
    private final Main plugin;
    private final File arenasFile;
    private FileConfiguration arenasConfig;
    private final List<Arena> arenaList;
    private final String pluginPrefix;
    private final Map<String, List<Player>> mapQueues = new HashMap<>(); // 使用 Map 存储每个地图对应的队列
    private final SelectMapGUI selectMapGUI; // 新增 SelectMapGUI 实例
    private final Map<String, TDArenaPrepareStartEvent> activeCountdowns = new HashMap<>();

    public MapManager(Main plugin, File arenasFile, FileConfiguration arenasConfig, String pluginPrefix) {
        this.plugin = plugin;
        this.arenasFile = arenasFile;
        this.arenasConfig = arenasConfig; // 保留对配置的引用
        this.pluginPrefix = pluginPrefix;
        this.arenaList = new ArrayList<>(); // 初始化 arenaList
        this.selectMapGUI = new SelectMapGUI(arenaList, plugin.getConfig()); // 初始化 SelectMapGUI
        loadArenasConfig(); // 在构造时加载地图配置
    }

    // 创建地图配置
    public void createMap(Player player, String mapName, String materialName, int minPlayer, int maxPlayer) {
        if (arenasConfig.contains(mapName)) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 地图名称已存在,请使用其他名称.");
            return;
        }

        if (minPlayer >= maxPlayer) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 最小玩家数不得大于或等于最大玩家数.");
            return;
        }

        // 正确设置地图配置
        setArenaCfgToFile(mapName, materialName, minPlayer, maxPlayer);

        try {
            arenasConfig.save(arenasFile);
        } catch (IOException e) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 创建地图时出错,请重试.");
            e.printStackTrace();
            return;
        }

        // 将新地图添加到 arenaList
        arenaList.add(new Arena(mapName, materialName, minPlayer, maxPlayer));
        player.sendMessage(pluginPrefix + ChatColor.GREEN + " 地图 " + mapName + " 创建成功.");
        player.sendMessage(pluginPrefix + ChatColor.GREEN + " 最小玩家数: " + minPlayer + ChatColor.GREEN + " ; " + ChatColor.GREEN + "最大玩家数: " + maxPlayer);
    }

    // 私有方法写入地图配置
    private void setArenaCfgToFile(String mapName, String materialName, int minPlayer, int maxPlayer) {
        arenasConfig.set(mapName + ".material", materialName);
        arenasConfig.set(mapName + ".min-player", minPlayer);
        arenasConfig.set(mapName + ".max-player", maxPlayer);
        System.out.println(pluginPrefix + ChatColor.DARK_GREEN + " [DEBUG] 配置保存: " + mapName + ".material=" + materialName);
    }

    // 列出地图
    public void listMaps(Player player) {
        if (arenasConfig.getKeys(false).isEmpty()) {
            player.sendMessage(pluginPrefix + ChatColor.RED + "当前没有可用的地图.");
            return;
        }

        player.sendMessage(ChatColor.AQUA + "可用地图列表:");
        for (String mapName : arenasConfig.getKeys(false)) {
            int minPlayer = arenasConfig.getInt(mapName + ".min-player");
            int maxPlayer = arenasConfig.getInt(mapName + ".max-player");
            player.sendMessage(ChatColor.GOLD + mapName + ChatColor.WHITE + " - 最小玩家数: " + minPlayer + ", 最大玩家数: " + maxPlayer);
        }
    }

    // 删除地图
    public void removeMap(Player player, String mapName) {
        if (!arenasConfig.contains(mapName)) {
            player.sendMessage(ChatColor.RED + "地图名称不存在.");
            return;
        }

        arenasConfig.set(mapName, null);
        System.out.println("删除地图配置: " + mapName);

        try {
            arenasConfig.save(arenasFile);
            player.sendMessage(pluginPrefix + ChatColor.GREEN + " 地图 " + mapName + " 已成功删除.");
        } catch (IOException e) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 删除地图时出错,请重试.");
            e.printStackTrace();
            return;
        }

        arenaList.removeIf(arena -> arena.getName().equals(mapName));
    }

    // 加载 arenas.yml 配置
    private void loadArenasConfig() {
        // 清空当前的 arenaList
        arenaList.clear();

        // 重新加载 arenas.yml 配置
        arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);

        // 加载地图信息
        for (String mapName : arenasConfig.getKeys(false)) {
            int minPlayer = arenasConfig.getInt(mapName + ".min-player");
            int maxPlayer = arenasConfig.getInt(mapName + ".max-player");
            String materialName = arenasConfig.getString(mapName + ".material", "BRICKS");
            Arena arena = new Arena(mapName, materialName, minPlayer, maxPlayer);
            arenaList.add(arena);
            // System.out.println("加载地图: " + mapName); // 添加调试信息
        }
    }

    // 重载配置
    public void reloadConfigs(Player player) {
        // 重新加载主配置文件
        plugin.reloadConfig(); // 已经在 MapManager 中保存了对 plugin 的引用
        loadArenasConfig(); // 重新加载 arenas.yml 配置文件
        selectMapGUI.updateTitle(plugin.getConfig()); // 更新 SelectMapGUI 的标题
        player.sendMessage(pluginPrefix + ChatColor.GREEN + " 配置文件已重载.");
        // player.sendMessage(pluginPrefix + ChatColor.GREEN + " 地图配置已成功重载.");
    }

    // 将玩家添加到队列
    public void addPlayerToQueue(Player player, Arena selectedArena) {
        String mapName = selectedArena.getName();
        // System.out.println(player.getName() + " 尝试加入地图 " + mapName);

        for (Map.Entry<String, List<Player>> entry : mapQueues.entrySet()) {
            List<Player> queue = entry.getValue();
            if (queue.contains(player)) {
                player.sendMessage(pluginPrefix + ChatColor.RED + " 你已在 " + entry.getKey() + " 地图的队列中,不能加入其他地图.");
                return;
            }
        }

        mapQueues.putIfAbsent(mapName, new ArrayList<>());
        mapQueues.get(mapName).add(player);
        player.sendMessage(pluginPrefix + ChatColor.GREEN + " 你已加入 " + mapName + " 地图队列.");

        if (mapQueues.get(mapName).size() >= selectedArena.getMinPlayer()) {
            TDArenaPrepareStartEvent event = new TDArenaPrepareStartEvent(selectedArena, mapQueues.get(mapName));
            Bukkit.getPluginManager().callEvent(event);
            event.startCountdown(10);

            // 存储倒计时事件
            activeCountdowns.put(mapName, event);
        }
    }

    // 移除玩家的逻辑
    public void removePlayerFromQueue(Player player) {
        boolean removed = false;

        for (Map.Entry<String, List<Player>> entry : mapQueues.entrySet()) {
            String mapName = entry.getKey();
            List<Player> queue = entry.getValue();
            if (queue.contains(player)) {
                queue.remove(player);
                player.sendMessage(pluginPrefix + ChatColor.GREEN + " 你已成功离开 " + mapName + " 地图的队列.");
                removed = true;

                if (queue.size() < getArenaByName(mapName).getMinPlayer()) {
                    // 检查是否有活动的倒计时
                    TDArenaPrepareStartEvent event = activeCountdowns.get(mapName);
                    if (event != null) {
                        event.cancelCountdown(); // 取消倒计时
                        activeCountdowns.remove(mapName); // 移除该事件
                    }
                }
                break;
            }
        }

        if (!removed) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 你不在任何地图的队列中.");
            player.sendMessage(pluginPrefix + ChatColor.GREEN + " 使用 /td gui 选择并加入一个游戏.");
        }
    }

    // 辅助方法获取 Arena
    private Arena getArenaByName(String name) {
        for (Arena arena : arenaList) {
            if (arena.getName().equalsIgnoreCase(name)) {
                return arena; // 找到对应的 Arena 并返回
            }
        }
        return null; // 如果没有找到，返回 null
    }

    public List<Arena> getArenaList() {
        return arenaList; // 返回地图列表
    }

}
