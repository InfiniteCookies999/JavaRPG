	package vork.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import lombok.Getter;
import vork.server.commands.CommandManager;
import vork.server.commands.PlayerCommands;
import vork.server.game.ArmorSpecsManager;
import vork.server.game.DisconnectionService;
import vork.server.game.DropTableManager;
import vork.server.game.GroundItem;
import vork.server.game.Location;
import vork.server.game.MoveController;
import vork.server.game.SelectOptions;
import vork.server.game.Shop;
import vork.server.game.container.GroundItemsList;
import vork.server.game.container.ItemBuilder;
import vork.server.game.entity.Direction;
import vork.server.game.entity.Entity;
import vork.server.game.entity.HumanRenderData;
import vork.server.game.entity.Npc;
import vork.server.game.entity.NpcLoader;
import vork.server.game.entity.NpcScriptManager;
import vork.server.game.entity.Player;
import vork.server.game.skills.Skill;
import vork.server.game.skills.SkillType;
import vork.server.game.world.Chunk;
import vork.server.game.world.World;
import vork.server.net.NetworkHandle;
import vork.server.net.NetworkManager;
import vork.server.net.in.ChangeFacingDirectionListener;
import vork.server.net.in.CharacterCreateListener;
import vork.server.net.in.ChatTextListener;
import vork.server.net.in.ContainerMoveListener;
import vork.server.net.in.DeathConfirmationListener;
import vork.server.net.in.DropItemListener;
import vork.server.net.in.EntitySelectOptionListener;
import vork.server.net.in.LoginPacketListener;
import vork.server.net.in.MoveRequestListener;
import vork.server.net.in.NpcChatListener;
import vork.server.net.in.PickupItemListener;
import vork.server.net.out.DespawnGroundItemPacketOut;
import vork.server.net.out.EntitySpawnPacketOut;

public class Server implements Runnable {
	
	public static final Server instance = new Server();
	
	public int tick = 0;

	@Getter
	private NetworkManager networkManager = new NetworkManager();
	
	private static Thread gameLoopThread;
	
	private final List<Player> playerJoinQueue = new ArrayList<>();
	
	/** Players that have successfully finished logging into the world. If the client
	 *  is in the character creation screen they are not in this list. */
	private final Player[] players = new Player[Constants.MAX_ALLOWED_CLIENTS];
	private final List<Npc> npcs = new ArrayList<>();
	
	private final GroundItemsList groundItems = new GroundItemsList();
	
	@Getter
	private World world = new World();
	
	@Getter
	private DisconnectionService disconnectionService = new DisconnectionService();
	
	@Getter
	private MoveController moveController = new MoveController();
	
	private static CommandManager commandManager = new CommandManager();
	
	private static final List<Shop> shops = new ArrayList<>();
	
	private Server() {
		
	}
	
	public void setup() {
		
		try {
			
			ItemBuilder.loadItemDefinitions();
	
			Shop merchantShop = new Shop();
			merchantShop.addItem(new ItemBuilder("Steel Platebody", 3).build(), 5000);
			merchantShop.addItem(new ItemBuilder("Steel Cruisses", 3).build(), 4300);
			merchantShop.addItem(new ItemBuilder("Steel Bucket-Helm", 3).build(), 3000);
			merchantShop.setId(0);
			shops.add(merchantShop);
			
			world.loadChunks();
	
			ArmorSpecsManager.loadArmorSpecs();
			DropTableManager.loadTables();
			NpcScriptManager.loadScripts();
			
			NpcLoader.loadTemplate();
			NpcLoader.loadNpcs(npc -> {
				
				npc.setWorld(world);
				
				Chunk chunk = world.getChunkFromTileLocation(npc.currentLocation);
				chunk.addEntity(npc);

				addNpc(npc);
				
			});
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addNpc(Npc npc) {
		npc.id = Constants.MAX_ALLOWED_CLIENTS + npcs.size();
		npcs.add(npc);
	}
	
	public void removePlayer(Player player) {
		players[player.id] = null;
	}
	
	public Player getPlayer(int entityId) {
		return players[entityId];
	}
	
	public Npc getNpc(int entityId) {
		return npcs.get(entityId - Constants.MAX_ALLOWED_CLIENTS);
	}
	
	public Entity getEntity(int entityId) {
		if (entityId < Constants.MAX_ALLOWED_CLIENTS) {
			return getPlayer(entityId);
		} else {
			return getNpc(entityId);
		}
	}
	
	public boolean addGroundItem(GroundItem groundItem) {
		if (groundItems.isFull()) {
			return false;
		}
	 	groundItem.setId(groundItems.add(groundItem));
	 	return true;
	}
	
	public void removeGroundItem(int id) {
		groundItems.remove(id);
	}
	
	public GroundItem getGroundItem(int id) {
		return groundItems.get(id);
	}
	
	public Shop getShop(int shopIndex) {
		return shops.get(shopIndex);
	}
	
	public void requestPlayerJoin(NetworkHandle handle) {
		playerJoinQueue.add(new Player(handle));
	}
	
	public Player getPlayerByName(String name) {
		for (int i = 0; i < Constants.MAX_ALLOWED_CLIENTS; i++) {
			Player player = players[i];
			if (player == null) continue;
			if (player.getName().equals(name)) {
				return player;
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		
		commandManager.registerListener(new PlayerCommands());
		
		Server.instance.setup();
		
		if (!instance.networkManager.openServer((short) 6234,
				eventBus -> {
					eventBus.registerListener(new LoginPacketListener());
					eventBus.registerListener(new CharacterCreateListener());
					eventBus.registerListener(new MoveRequestListener());
					eventBus.registerListener(new ChatTextListener());
					eventBus.registerListener(new EntitySelectOptionListener());
					eventBus.registerListener(new ChangeFacingDirectionListener());
					eventBus.registerListener(new DeathConfirmationListener());
					eventBus.registerListener(new ContainerMoveListener());
					eventBus.registerListener(new DropItemListener());
					eventBus.registerListener(new PickupItemListener());
					eventBus.registerListener(new NpcChatListener());
				}
				)) {
			System.out.println("Failed to open the server. Make sure an instance is not already running.");
			return;
		}
		
		gameLoopThread = new Thread(Server.instance, "game-loop-thread");
		gameLoopThread.start();
		
		Scanner scanner = new Scanner(System.in);
		
		boolean running = true;
		while (running) {
			String command = scanner.nextLine().trim();
			if (command.equals("stop")) {
				running = false;
			} else {
				if (!commandManager.dispatch(command)) {
					System.out.println("unknown command: " + command);	
				}
			}
		}
		
		scanner.close();
		
		gameLoopThread.interrupt();
		try {
			gameLoopThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		instance.networkManager.close();
		
	}
	
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			
			commandManager.processDispatches();
			
			networkManager.getConnectionManager().processConnections();
			networkManager.getConnectionManager().processDisconnections();
			
			networkManager.getEventBus().gameThreadPublish();
			
			processPlayerJoins();
			
			for (Player player : players) {
				if (player != null) {
					player.update();
				}
			}
			for (Npc npc : npcs) {
				npc.update();
			}
			// despawning ground items if they have been on the ground too long
			for (int i = 0; i < Constants.MAX_GROUND_ITEMS; i++) {
				GroundItem groundItem = groundItems.get(i);
				if (groundItem == null) continue;
				if (groundItem.timeAlive >= Timing.minutes(2)) {
					Player owner = groundItem.getOwner();
					if (owner != null) {
						new DespawnGroundItemPacketOut(i).send(owner);
					}
					groundItems.remove(i);
				}
				++groundItem.timeAlive;
			}
			
			networkManager.flushPackets();
			
			++tick;
			
			try {
				frameLimit(Constants.TPS);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
	
	private void processPlayerJoins() {
		// By having a queue we can limit the amount of
		// join processes within a tick to be calculated
		// if it becomes too much!
		
		// Setting up the information for the joined players.
		int joinCount = 0;
		for (joinCount = 0; joinCount < Constants.MAX_PLAYER_JOIN_PROCESS && joinCount < playerJoinQueue.size(); joinCount++) {
			playerJoin(playerJoinQueue.get(joinCount));
		}
		
		// Sending the packet information about the
		// world for the players that joined.
		for (joinCount = 0; joinCount < Constants.MAX_PLAYER_JOIN_PROCESS && joinCount < playerJoinQueue.size(); joinCount++) {
			playerSendJoinPackets(playerJoinQueue.get(joinCount));
			playerJoinQueue.remove(joinCount);
		}
	}
	
	private void playerJoin(Player player) {
		// TODO: here we would load and set player information.
		NetworkHandle handle = player.getNetworkHandle();
		int networkId = handle.getId();
		handle.setPlayer(player);
		
		player.id = networkId;
		players[networkId] = player;
		
		player.setName(player.getNetworkHandle().playerName);
		player.setFacingDirection(Direction.SOUTH);
		
		player.setSpawnLocation(new Location(Constants.DEFAULT_SPAWN_LOCATION));
		player.currentLocation = new Location(Constants.DEFAULT_SPAWN_LOCATION);
		player.futureLocation = new Location(Constants.DEFAULT_SPAWN_LOCATION);
		player.setMoveSpeed(3.0f);
		player.selectOptions = SelectOptions.FOLLOW;
		
		int health = 25;
		player.setMaxHealth(health);
		player.setHealth(health);
		
		HumanRenderData renderData = new HumanRenderData();
		player.renderData = renderData;
		
		renderData.skinColor = handle.skinColor;
		renderData.eyeColor  = handle.eyeColor;
		renderData.hairStyle = handle.hairStyle;
		renderData.hairColor = handle.hairColor;
		renderData.bodyType  = handle.bodyType;
		renderData.legsType  = handle.legsType;
		
		Chunk chunk = world.getChunkFromTileLocation(player.currentLocation);
		player.setWorld(world);
		chunk.addEntity(player);
		
		player.skills[SkillType.MELEE]   = new Skill(SkillType.MELEE, player, 0);
		player.skills[SkillType.DEFENSE] = new Skill(SkillType.DEFENSE, player, 0);
		
		/*player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());
		player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());
		player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());
		player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());
		player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());
		player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());
		player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());
		player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());
		player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());
		player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());
		player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());
		player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());
		player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());
		player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());
		player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());
		player.giveItem(new ItemBuilder(0).build());
		player.giveItem(new ItemBuilder(1).build());*/
		
	}
	
	private void playerSendJoinPackets(Player player) {
		// Sending packet of their player entity to the client.
		new EntitySpawnPacketOut(player, true).send(player);
	}
	
	// Helps fine tune the tps at a steady rate
	private long variableYieldTime, lastTime;
	
	public void frameLimit(int targetFPS) throws InterruptedException {
		// How many nanoseconds should go by per tick.
		final long nanosPerTick = 1_000_000_000 / targetFPS;
		long yieldTime = Math.min(nanosPerTick, variableYieldTime + nanosPerTick % (1000 * 1000));
		long overSleptTime = 0; // How many nanoseconds were overslept.

	    while (true) {
            long deltaTime = System.nanoTime() - lastTime;

            if (deltaTime < nanosPerTick - yieldTime) {
                Thread.sleep(1);
            } else if (deltaTime < nanosPerTick) {
                // burn the last few CPU cycles to ensure accuracy
                Thread.yield();
            } else {
            	overSleptTime = deltaTime - nanosPerTick;
                break; // exitServer while loop
            }
        }
  
    	lastTime = System.nanoTime() - Math.min(overSleptTime, nanosPerTick);
    	
    	// auto tune the time sync should yield
        if (overSleptTime > variableYieldTime) {
            // increase by 200 microseconds (1/5 a ms)
            variableYieldTime = Math.min(variableYieldTime + 200 * 1000, nanosPerTick);
        } else if (overSleptTime < variableYieldTime - 200 * 1000) {
            // decrease by 2 microseconds
            variableYieldTime = Math.max(variableYieldTime - 2 * 1000, 0);
        }
	}
}
