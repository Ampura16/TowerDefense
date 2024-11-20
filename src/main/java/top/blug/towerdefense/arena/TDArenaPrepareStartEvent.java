package top.blug.towerdefense.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class TDArenaPrepareStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Arena arena;
    private final List<Player> players; // 队列中的玩家
    private BukkitTask arenaPreStartTimer; // 倒计时任务

    public TDArenaPrepareStartEvent(Arena arena, List<Player> players) {
        this.arena = arena;
        this.players = players;
    }

    public Arena getArena() {
        return arena;
    }

    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public void startCountdown(int countdownTime) {
        arenaPreStartTimer = new BukkitRunnable() {
            int timeLeft = countdownTime;

            @Override
            public void run() {
                // 检查当前队列玩家数是否满足 min-player
                if (players.size() < arena.getMinPlayer()) {
                    cancel(); // 中止任务
                    players.forEach(player -> player.sendMessage(ChatColor.RED + "游戏因人数不足而取消."));
                    return;
                }

                if (timeLeft > 0) {
                    // 向所有玩家发送倒计时消息
                    for (Player player : players) {
                        player.sendMessage(ChatColor.YELLOW + "游戏将在 " + timeLeft + " 秒后开始...");
                    }
                    timeLeft--;
                } else {
                    // 倒计时完成，开始比赛的逻辑可以在这里添加
                    cancel(); // 取消任务
                    players.forEach(player -> player.sendMessage(ChatColor.GREEN + "游戏开始!"));
                    // 此处可调用开始比赛的相关逻辑
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("TowerDefense"), 0, 20); // 每秒更新
    }

    public void cancelCountdown() {
        if (arenaPreStartTimer != null) {
            arenaPreStartTimer.cancel();
            arenaPreStartTimer = null; // 清理引用
        }
    }
}
