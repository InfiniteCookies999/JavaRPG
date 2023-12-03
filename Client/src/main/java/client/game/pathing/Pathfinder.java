package client.game.pathing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import client.Constants;
import client.game.world.Location;
import client.game.world.Tile;
import client.game.world.World;

public class Pathfinder {
	
	private class PathFindNode {
		
		private int worldX, worldY;
		private int gridX, gridY;
		
		private PathFindNode parent;
		
		private int gCost, hCost;
		
		private PathFindNode[] neighbors = new PathFindNode[4];
		
		private PathFindNode(int worldX, int worldY, int gridX, int gridY) {
			this.worldX = worldX;
			this.worldY = worldY;
			this.gridX = gridX;
			this.gridY = gridY;
		}
		
		private int fCost() {
			return gCost + hCost;
		}
	}
	
	private static final int GRID_LENGTH = (Constants.PATHFIND_RADIUS*2) + 1;
	private static final PathFindNode[][] grid = new PathFindNode[GRID_LENGTH][GRID_LENGTH];
	
	private List<PathFindNode> openSet = new ArrayList<>();
	private List<PathFindNode> closedSet = new ArrayList<>();

	private void initGrid(World world, Location startLoc, Location endLoc) {
		int bottomX = startLoc.x - Constants.PATHFIND_RADIUS;
		int bottomY = startLoc.y - Constants.PATHFIND_RADIUS;
		
		for (int y = 0; y < GRID_LENGTH; y++) {
			for (int x = 0; x < GRID_LENGTH; x++) {
				int worldX = bottomX + x;
				int worldY = bottomY + y;
				
				Tile tile = world.getTile(worldX, worldY);
				if (tile == null) continue;
				
				grid[y][x] = new PathFindNode(worldX, worldY, x, y);
			}	
		}
		
		// Calculating neighbors
		for (int y = 0; y < GRID_LENGTH; y++) {
			for (int x = 0; x < GRID_LENGTH; x++) {
				PathFindNode node = grid[y][x];
				if (node == null) continue;
				
				int top    = node.gridY + 1;
				int bottom = node.gridY - 1;
				int right  = node.gridX + 1;
				int left   = node.gridX - 1;
				node.neighbors[0] = top < GRID_LENGTH    && top >= 0    ? grid[top]       [node.gridX] : null;
				node.neighbors[1] = bottom < GRID_LENGTH && bottom >= 0 ? grid[bottom]    [node.gridX] : null;
				node.neighbors[2] = right < GRID_LENGTH  && right >= 0  ? grid[node.gridY][right]      : null;
				node.neighbors[3] = left < GRID_LENGTH   && left >= 0   ? grid[node.gridY][left]       : null;
			}
		}
	}
	
	private PathFindNode findByLowestFCost() {
		PathFindNode lowestCostNode = openSet.get(0);
		
		for (PathFindNode openNode : openSet) {
			if (lowestCostNode.fCost() > openNode.fCost() ||
				(lowestCostNode.fCost() == openNode.fCost() &&
				lowestCostNode.hCost > openNode.hCost)) {
				lowestCostNode = openNode;
			}
		}
		
		return lowestCostNode;
	}
	
	private Queue<Location> toPath(PathFindNode start, PathFindNode current, boolean removeLastNode) {
		List<Location> path = new ArrayList<>();
		while (current != start) {
			path.add(new Location(current.worldX, current.worldY));
			current = current.parent;
		}
		if (removeLastNode) {
			path.remove(0);
		}
		Collections.reverse(path);
		Queue<Location> queuedPath = new LinkedList<>();
		queuedPath.addAll(path);
		return queuedPath;
	}
	
	private static int distance(PathFindNode a, PathFindNode b) {
		return Math.abs(b.worldX - a.worldX) + Math.abs(b.worldY - a.worldY);
	}
	
	public Queue<Location> findPath(World world, Location startLoc, Location endLoc, boolean removeLastNode) {
		if (startLoc.equals(endLoc)) return new LinkedList<>();
		if (!world.isTraversible(startLoc) || !world.isTraversible(endLoc)) return new LinkedList<>();
		
		openSet.clear();
		closedSet.clear();
		
		initGrid(world, startLoc, endLoc);
		
		PathFindNode start = grid[Constants.PATHFIND_RADIUS][Constants.PATHFIND_RADIUS];
		PathFindNode goal = grid[Constants.PATHFIND_RADIUS + endLoc.y - startLoc.y][Constants.PATHFIND_RADIUS + endLoc.x - startLoc.x];
		
		openSet.add(start);
		
		while (!openSet.isEmpty()) {
			PathFindNode current = findByLowestFCost();
			
			openSet.remove(current);
			closedSet.add(current);
			
			if (current == goal) {
				return toPath(start, current, removeLastNode);
			}
			
			for (PathFindNode neighbor : current.neighbors) {
				if (neighbor == null) continue;
				if (closedSet.contains(neighbor) || !world.isTraversible(neighbor.worldX, neighbor.worldY)) {
					continue;
				}
				
				
				int costToNeighbor = current.gCost + distance(current, neighbor);
				if (costToNeighbor < neighbor.gCost || !openSet.contains(neighbor)) {
					neighbor.gCost = costToNeighbor;
					neighbor.parent = current;
					
					if (!openSet.contains(neighbor)) {
						openSet.add(neighbor);
					}
				}
			}
		}
		
		// Did not find path.
		return new LinkedList<>();
	}
}
