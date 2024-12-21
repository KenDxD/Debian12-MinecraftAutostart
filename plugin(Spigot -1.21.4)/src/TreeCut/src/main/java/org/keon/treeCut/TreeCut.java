package org.keon.treeCut;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class TreeCut extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Check if the block being broken is part of a tree (log or leaf)
        Block brokenBlock = event.getBlock();
        if (isWood(brokenBlock) || isLeaf(brokenBlock)) {
            // Get all blocks connected to the broken one and cut them down
            Set<Block> toBreak = new HashSet<>();
            findTree(brokenBlock, toBreak);

            // Remove all blocks in the tree from the world
            for (Block block : toBreak) {
                block.breakNaturally();
            }
        }
    }

    private boolean isWood(Block block) {
        Material type = block.getType();
        return type == Material.OAK_LOG || type == Material.SPRUCE_LOG || type == Material.BIRCH_LOG ||
                type == Material.JUNGLE_LOG || type == Material.ACACIA_LOG || type == Material.DARK_OAK_LOG ||
                type == Material.MANGROVE_LOG || type == Material.CHERRY_LOG || type == Material.WARPED_STEM || type == Material.PALE_OAK_LOG;
    }

    private boolean isLeaf(Block block) {
        Material type = block.getType();
        return type == Material.OAK_LEAVES || type == Material.SPRUCE_LEAVES || type == Material.BIRCH_LEAVES ||
                type == Material.JUNGLE_LEAVES || type == Material.ACACIA_LEAVES || type == Material.DARK_OAK_LEAVES ||
                type == Material.MANGROVE_LEAVES || type == Material.CHERRY_LEAVES || type == Material.WARPED_FUNGUS || type == Material.PALE_OAK_LEAVES;
    }

    private void findTree(Block startBlock, Set<Block> toBreak) {
        // If the block is already in the set, stop to avoid infinite loops
        if (toBreak.contains(startBlock)) return;

        // Add the block to the set to break
        toBreak.add(startBlock);

        // Iterate over adjacent blocks (up, down, north, south, east, west)
        for (Block adjacentBlock : getAdjacentBlocks(startBlock)) {
            // If the adjacent block is wood or leaf, continue searching recursively
            if (isWood(adjacentBlock) || isLeaf(adjacentBlock)) {
                findTree(adjacentBlock, toBreak);
            }
        }
    }

    private Set<Block> getAdjacentBlocks(Block block) {
        Set<Block> adjacentBlocks = new HashSet<>();
        adjacentBlocks.add(block.getRelative(1, 0, 0));  // East
        adjacentBlocks.add(block.getRelative(-1, 0, 0)); // West
        adjacentBlocks.add(block.getRelative(0, 0, 1));  // South
        adjacentBlocks.add(block.getRelative(0, 0, -1)); // North
        adjacentBlocks.add(block.getRelative(0, 1, 0));  // Up
        adjacentBlocks.add(block.getRelative(0, -1, 0)); // Down
        return adjacentBlocks;
    }
}
