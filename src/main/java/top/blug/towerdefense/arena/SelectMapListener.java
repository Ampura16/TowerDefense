package top.blug.towerdefense.arena;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import top.blug.towerdefense.Main;

import java.util.List;

public class SelectMapListener implements Listener {

    private final Main plugin; // 插件实例
    private final List<Arena> arenaList; // 存储地图列表
    private final String pluginPrefix; // 获取插件前缀
    private final String guiTitle; // 存储从配置文件中读取的 GUI 标题

    public SelectMapListener(Main plugin, List<Arena> arenaList, String pluginPrefix) {
        this.plugin = plugin;
        this.arenaList = arenaList;
        this.pluginPrefix = pluginPrefix;
        this.guiTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("select-map-gui-title", "&2选择地图")); // 从配置文件中读取标题
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // 确保只处理选择地图的 GUI
        if (event.getView().getTitle().equals(guiTitle)) {
            event.setCancelled(true); // 取消默认行为

            ItemStack clickedItem = event.getCurrentItem(); // 获取被点击的物品
            if (clickedItem == null || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
                return; // 如果没有物品或没有显示名称，则返回
            }

            String arenaName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()); // 获取地图名称
            Player player = (Player) event.getWhoClicked(); // 获取点击的玩家

            // System.out.println("点击的地图名称: " + arenaName); // 调试信息

            Arena selectedArena = getArenaByName(arenaName); // 根据名称获取对应的地图

            // 打印当前 arenaList 中的所有地图名称
            for (Arena arena : arenaList) {
                // System.out.println("已加载地图名称: " + arena.getName());
            }

            if (selectedArena != null) {
                // 将玩家添加到地图队列
                plugin.getMapManager().addPlayerToQueue(player, selectedArena);

                player.closeInventory(); // 关闭选择地图的界面
                player.sendMessage(pluginPrefix + ChatColor.GOLD + " 你可以输入 /td leave 离开当前队列.");
            } else {
                player.sendMessage(ChatColor.RED + "未找到该地图.");
            }
        }
    }

    private Arena getArenaByName(String name) {
        for (Arena arena : arenaList) {
            if (arena.getName().equalsIgnoreCase(name)) {
                return arena; // 找到对应的 Arena 并返回
            }
        }
        System.out.println("未找到地图: " + name); // 添加调试信息
        return null; // 如果没有找到，返回 null
    }
}
