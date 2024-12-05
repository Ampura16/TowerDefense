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
        if (event.getView().getTitle().equals(guiTitle)) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
                return;
            }

            String arenaName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            Player player = (Player) event.getWhoClicked();

            Arena selectedArena = getArenaByName(arenaName);
            if (selectedArena != null) {
                try {
                    plugin.getMapManager().addPlayerToQueue(player, selectedArena);
                    player.closeInventory();
                    player.sendMessage(pluginPrefix + ChatColor.GOLD + " 你可以输入 /td leave 离开当前队列.");
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "加入队列时出现问题，请重试.");
                    plugin.getLogger().severe("玩家尝试加入游戏时发生错误: " + e.getMessage());
                }
            } else {
                player.sendMessage(ChatColor.RED + "未找到该地图.");
                player.sendMessage(ChatColor.YELLOW + "请检查地图名称是否正确或确保地图已加载.");
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
