package top.blug.towerdefense.command;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import top.blug.towerdefense.Main;
import top.blug.towerdefense.arena.MapManager;
import top.blug.towerdefense.arena.SelectMapGUI;

public class TDCommand implements CommandExecutor, Listener {

    private final Main plugin;
    private final MapManager mapManager;

    public TDCommand(Main plugin) {
        this.plugin = plugin;
        this.mapManager = plugin.getMapManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            printConsoleHelpMessages(sender);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            printPlayerHelpMessages(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                printPlayerHelpMessages(player);
                break;

            case "create":
                handleCreateCommand(player, args);
                break;

            case "remove":
                handleRemoveCommand(player, args);
                break;

            case "gui":
                new SelectMapGUI(mapManager.getArenaList(), plugin.getConfig()).openSelectMapGUI(player);
                break;

            case "leave":
                mapManager.removePlayerFromQueue(player);
                break;

            case "maplist":
                mapManager.listMaps(player);
                break;

            case "reload":
                if (player.hasPermission("towerdefense.reload")) {
                    mapManager.reloadConfigs(player);
                    player.sendMessage(ChatColor.GREEN + "插件配置已重载.");
                } else {
                    player.sendMessage(ChatColor.RED + "你没有权限执行此命令.");
                }
                break;

            default:
                player.sendMessage(ChatColor.GOLD + "未知的命令,请查看指令帮助.");
                printPlayerHelpMessages(player);
        }
        return true;
    }

    // create子命令
    private void handleCreateCommand(Player player, String[] args) {
        if (args.length < 4 || args.length > 5) {
            player.sendMessage(ChatColor.RED + "用法: /td create <地图名称> <最小玩家数> <最大玩家数> [材质（可选）]");
            return;
        }
        String mapName = args[1];
        try {
            int minPlayer = Integer.parseInt(args[2]);
            int maxPlayer = Integer.parseInt(args[3]);
            String materialName = (args.length == 5) ? args[4].toUpperCase() : "BRICKS";
            mapManager.createMap(player, mapName, materialName, minPlayer, maxPlayer);
            player.sendMessage(ChatColor.GREEN + "地图创建成功!");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "最小玩家数和最大玩家数必须是整数.");
        }
    }

    // remove子命令
    private void handleRemoveCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "用法: /td remove <地图名称>");
            return;
        }
        String mapToRemove = args[1];
        mapManager.removeMap(player, mapToRemove);
        player.sendMessage(ChatColor.GREEN + "地图 " + mapToRemove + " 已被移除.");
    }

    private void printPlayerHelpMessages(Player player) {
        player.sendMessage(ChatColor.AQUA + "==========[ TowerDefense - 指令帮助 ]==========");
        player.sendMessage(ChatColor.GOLD + "/td help - 查看指令帮助");
        player.sendMessage(ChatColor.GOLD + "/td gui - 打开选择地图GUI");
        player.sendMessage(ChatColor.GOLD + "/td create <地图名称> <最小玩家数> <最大玩家数> - 创建地图");
        player.sendMessage(ChatColor.GOLD + "/td remove <地图名称> - 删除一个现有地图");
        player.sendMessage(ChatColor.GOLD + "/td maplist - 展示可用地图列表");
        player.sendMessage(ChatColor.GOLD + "/td reload - 重载插件配置");
        player.sendMessage(ChatColor.AQUA + "===============================================");
    }

    private void printConsoleHelpMessages(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "==============================");
        sender.sendMessage(ChatColor.AQUA + "TowerDefense - 指令帮助");
        sender.sendMessage(ChatColor.AQUA + "==============================");
    }

}
