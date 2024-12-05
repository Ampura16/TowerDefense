package top.blug.towerdefense.arena;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Arena {
    private String mapName;
    private int minPlayer;
    private int maxPlayer;
    private final String material;

    public Arena(String mapName, String material, int minPlayer, int maxPlayer) {
        this.mapName = mapName;
        this.minPlayer = minPlayer;
        this.maxPlayer = maxPlayer;
        this.material = material;
    }

    // 静态方法加载所有 Arena 实例
    public static List<Arena> loadArenasFromConfig(FileConfiguration config) {
        List<Arena> arenaList = new ArrayList<>(); // 初始化地图列表
        for (String mapName : config.getKeys(false)) {
            int minPlayer = config.getInt(mapName + ".min-player");
            int maxPlayer = config.getInt(mapName + ".max-player");
            String material = config.getString(mapName + ".material", "BRICKS");
            Arena arena = new Arena(mapName, material, minPlayer, maxPlayer); // 创建 Arena 对象
            arenaList.add(arena); // 加入到列表中
        }
        return arenaList; // 返回加载的 Arena 列表
    }

    public String getName() {
        return mapName;
    }

    public String getMaterial() {
        return material;
    }

    public int getMinPlayer() {
        return minPlayer;
    }

    public int getMaxPlayer() {
        return maxPlayer;
    }

}