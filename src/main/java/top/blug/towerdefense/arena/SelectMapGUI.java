package top.blug.towerdefense.arena;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SelectMapGUI {

    private final List<Arena> arenaList; // 存储地图列表
    private String guiTitle; // 存储 GUI 标题

    public SelectMapGUI(List<Arena> arenaList, FileConfiguration config) {
        this.arenaList = arenaList; // 初始化地图列表
        this.guiTitle = ChatColor.translateAlternateColorCodes('&', config.getString("select-map-gui-title", "&2选择地图")); // 从配置中读取标题
    }

    // 更新 GUI 标题
    public void updateTitle(FileConfiguration config) {
        this.guiTitle = ChatColor.translateAlternateColorCodes('&', config.getString("select-map-gui-title", "&b选择地图"));
    }

    // 打开选择地图的 GUI
    public void openSelectMapGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 18, guiTitle); // 创建GUI

        // 填充 GUI
        for (Arena arena : arenaList) {
            ItemStack mapItem = createMapItem(arena); // 创建显示地图的物品
            inventory.addItem(mapItem); // 添加到 GUI
        }

        player.openInventory(inventory); // 打开 GUI
    }

    // 创建地图项
    private ItemStack createMapItem(Arena arena) {
        Material material = Material.getMaterial(arena.getMaterial()); // 根据材质名称获取 Material
        if (material == null) {
            material = Material.BRICKS; // 如果材质无效，使用默认材质
        }

        ItemStack mapIcon = new ItemStack(material); // 使用指定材质
        ItemMeta meta = mapIcon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', arena.getName())); // 设置地图名称
            List<String> lore = List.of(
                    ChatColor.GRAY + "最小玩家数: " + arena.getMinPlayer(),
                    ChatColor.GRAY + "最大玩家数: " + arena.getMaxPlayer(),
                    ChatColor.GREEN + "点击加入该地图队列"
            );
            meta.setLore(lore); // 设置物品描述
            mapIcon.setItemMeta(meta); // 将 meta 设回物品
        }
        return mapIcon;
    }

    // 更新地图列表
    public void updateMapList() {
        Inventory inventory = Bukkit.createInventory(null, 18, guiTitle); // 创建新的 GUI

        // 填充 GUI
        for (Arena arena : arenaList) {
            ItemStack mapItem = createMapItem(arena); // 创建显示地图的物品
            inventory.addItem(mapItem); // 添加到 GUI
        }

        // 返回给玩家打开更新后的 GUI
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTitle().equals(guiTitle)) {
                player.closeInventory(); // 关闭之前的 GUI
                player.openInventory(inventory); // 打开新的 GUI
            }
        }
    }
}
