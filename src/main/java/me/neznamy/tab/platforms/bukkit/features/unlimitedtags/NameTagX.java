package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

import me.neznamy.tab.api.ArmorStand;
import me.neznamy.tab.api.ArmorStandManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.RespawnEventListener;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;

/**
 * The core class for unlimited nametag mode
 */
public class NameTagX extends NameTag implements Loadable, JoinEventListener, QuitEventListener, WorldChangeListener, RespawnEventListener {

	private final int ENTITY_TRACKING_RANGE = 48;

	private JavaPlugin plugin;
	public boolean markerFor18x;
	public boolean disableOnBoats;
	private float spaceBetweenLines;
	public List<String> dynamicLines = Arrays.asList("belowname", "nametag", "abovename");
	public Map<String, Object> staticLines = new ConcurrentHashMap<String, Object>();

	//entity id counter to pick unique entity IDs
	private int idCounter = 2000000000;

	//player data by entityId, used for better performance
	public Map<Integer, TabPlayer> entityIdMap = new ConcurrentHashMap<Integer, TabPlayer>();
	public Map<Integer, Set<Integer>> vehicles = new ConcurrentHashMap<>();
	private EventListener eventListener;

	public NameTagX(JavaPlugin plugin, NMSStorage nms, TAB tab) {
		super(tab);
		this.plugin = plugin;
		markerFor18x = tab.getConfiguration().config.getBoolean("unlimited-nametag-prefix-suffix-mode.use-marker-tag-for-1-8-x-clients", false);
		disableOnBoats = tab.getConfiguration().config.getBoolean("unlimited-nametag-prefix-suffix-mode.disable-on-boats", true);
		spaceBetweenLines = Float.parseFloat(tab.getConfiguration().getSecretOption("ntx-space", 0.22).toString());
		if (tab.getConfiguration().premiumconfig != null) {
			List<String> realList = tab.getConfiguration().premiumconfig.getStringList("unlimited-nametag-mode-dynamic-lines", Arrays.asList("abovename", "nametag", "belowname", "another"));
			dynamicLines = new ArrayList<String>();
			dynamicLines.addAll(realList);
			Collections.reverse(dynamicLines);
			staticLines = tab.getConfiguration().premiumconfig.getConfigurationSection("unlimited-nametag-mode-static-lines");
		}
		refreshUsedPlaceholders();
		eventListener = new EventListener(this);
		tab.getFeatureManager().registerFeature("nametagx-packet", new PacketListener(this, nms, tab));
	}

	@Override
	public void load() {
		Bukkit.getPluginManager().registerEvents(eventListener, plugin);
		for (TabPlayer all : tab.getPlayers()){
			entityIdMap.put(((Player) all.getPlayer()).getEntityId(), all);
			all.setTeamName(sorting.getTeamName(all));
			updateProperties(all);
			loadArmorStands(all);
			if (isDisabledWorld(all.getWorldName())) continue;
			all.registerTeam();
			loadPassengers(all);
			for (TabPlayer worldPlayer : tab.getPlayers()) {
				if (all != worldPlayer && ((Player) worldPlayer.getPlayer()).getWorld() == ((Player) all.getPlayer()).getWorld() &&
						getDistance(worldPlayer, all) <= ENTITY_TRACKING_RANGE) {
					all.getArmorStandManager().spawn(worldPlayer);
				}
			}
		}
		startRefreshingTasks();
		tab.getCPUManager().startRepeatingMeasuredTask(500, "refreshing nametag visibility", getFeatureType(), UsageType.REFRESHING_NAMETAG_VISIBILITY, new Runnable() {
			public void run() {
				for (TabPlayer p : tab.getPlayers()) {
					if (!p.isLoaded() || isDisabledWorld(p.getWorldName())) continue;
					p.getArmorStandManager().updateVisibility();
					if (!disableOnBoats) continue;
					boolean onBoat = ((Player)p.getPlayer()).getVehicle() != null && ((Player)p.getPlayer()).getVehicle().getType() == EntityType.BOAT;
					if (p.isOnBoat() != onBoat) {
						p.setOnBoat(onBoat);
						p.updateTeamData();
					}
				}
			}
		});
	}

	@Override
	public void unload() {
		HandlerList.unregisterAll(eventListener);
		for (TabPlayer p : tab.getPlayers()) {
			if (!isDisabledWorld(p.getWorldName())) p.unregisterTeam();
			p.getArmorStandManager().destroy();
		}
		entityIdMap.clear();
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		entityIdMap.put(((Player) connectedPlayer.getPlayer()).getEntityId(), connectedPlayer);
		connectedPlayer.setTeamName(sorting.getTeamName(connectedPlayer));
		updateProperties(connectedPlayer);
		for (TabPlayer all : tab.getPlayers()) {
			if (!all.isLoaded()) continue; //avoiding NPE when 2 players join at once
			if (all == connectedPlayer) continue;
			if (!isDisabledWorld(all.getWorldName())) all.registerTeam(connectedPlayer);
		}
		loadArmorStands(connectedPlayer);
		if (isDisabledWorld(connectedPlayer.getWorldName())) return;
		connectedPlayer.registerTeam();
		loadPassengers(connectedPlayer);
		for (TabPlayer viewer : tab.getPlayers()) {
			if (connectedPlayer == viewer) continue; //not displaying own armorstands
			if (((Player) viewer.getPlayer()).getWorld() != ((Player) connectedPlayer.getPlayer()).getWorld()) continue;
			if (getDistance(viewer, connectedPlayer) <= ENTITY_TRACKING_RANGE) {
				connectedPlayer.getArmorStandManager().spawn(viewer);
				viewer.getArmorStandManager().spawn(connectedPlayer);
			}
		}
	}
	
	private double getDistance(TabPlayer player1, TabPlayer player2) {
		Location loc1 = ((Player) player1.getPlayer()).getLocation();
		Location loc2 = ((Player) player2.getPlayer()).getLocation();
		return Math.sqrt(Math.pow(loc1.getX()-loc2.getX(), 2) + Math.pow(loc1.getZ()-loc2.getZ(), 2));
	}

	private void loadPassengers(TabPlayer p) {
		if (((Entity) p.getPlayer()).getVehicle() == null) return;
		Entity vehicle = ((Entity) p.getPlayer()).getVehicle();
		Set<Integer> list = new HashSet<Integer>();
		for (Entity e : getPassengers(vehicle)) {
			list.add(e.getEntityId());
		}
		vehicles.put(vehicle.getEntityId(), list);
	}

	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		if (!isDisabledWorld(disconnectedPlayer.getWorldName())) disconnectedPlayer.unregisterTeam();
		invisiblePlayers.remove(disconnectedPlayer.getName());
		for (TabPlayer all : tab.getPlayers()) {
			if (all.getArmorStandManager() != null) all.getArmorStandManager().unregisterPlayer(disconnectedPlayer);
		}
		//entity destroy packet is sent too late, need to send it manually
		disconnectedPlayer.getArmorStandManager().destroy();

		for (TabPlayer all : tab.getPlayers()) {
			if (all == disconnectedPlayer) continue;
			all.showNametag(disconnectedPlayer.getUniqueId()); //clearing memory from API method
		}
		entityIdMap.remove(((Player) disconnectedPlayer.getPlayer()).getEntityId());
	}

	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		if (!p.isLoaded()) return;
		updateProperties(p);
		if (isDisabledWorld(p.getWorldName()) && !isDisabledWorld(disabledWorlds, from)) {
			p.unregisterTeam();
		} else if (!isDisabledWorld(p.getWorldName()) && isDisabledWorld(disabledWorlds, from)) {
			p.registerTeam();
		} else {
			p.updateTeam();
			p.getArmorStandManager().refresh();
			fixArmorStandHeights(p);
		}
	}

	public void loadArmorStands(TabPlayer pl) {
		pl.setArmorStandManager(new ArmorStandManager());
		pl.setProperty("nametag", pl.getProperty("tagprefix").getCurrentRawValue() + pl.getProperty("customtagname").getCurrentRawValue() + pl.getProperty("tagsuffix").getCurrentRawValue());
		double height = -spaceBetweenLines;
		for (String line : dynamicLines) {
			Property p = pl.getProperty(line);
			if (p.getCurrentRawValue().length() == 0) continue;
			pl.getArmorStandManager().addArmorStand(line, new BukkitArmorStand(idCounter++, pl, p, height+=spaceBetweenLines, false));
		}
		for (Entry<String, Object> line : staticLines.entrySet()) {
			Property p = pl.getProperty(line.getKey());
			if (p.getCurrentRawValue().length() == 0) continue;
			pl.getArmorStandManager().addArmorStand(line.getKey(), new BukkitArmorStand(idCounter++, pl, p, Double.parseDouble(line.getValue()+""), true));
		}
		fixArmorStandHeights(pl);
	}

	public void fixArmorStandHeights(TabPlayer p) {
		p.getArmorStandManager().refresh();
		double currentY = -spaceBetweenLines;
		for (ArmorStand as : p.getArmorStandManager().getArmorStands()) {
			if (as.hasStaticOffset()) continue;
			if (as.getProperty().get().length() != 0) {
				currentY += spaceBetweenLines;
				as.setOffset(currentY);
			}
		}
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (isDisabledWorld(refreshed.getWorldName())) return;
		boolean refresh;
		if (force) {
			updateProperties(refreshed);
			refresh = true;
		} else {
			boolean prefix = refreshed.getProperty("tagprefix").update();
			boolean suffix = refreshed.getProperty("tagsuffix").update();
			refresh = prefix || suffix;
		}
		if (refresh) refreshed.updateTeam();
		if (force) {
			refreshed.getArmorStandManager().destroy();
			loadArmorStands(refreshed);
			if (((Entity) refreshed.getPlayer()).getVehicle() != null) {
				Entity vehicle = ((Entity) refreshed.getPlayer()).getVehicle();
				Set<Integer> list = new HashSet<Integer>();
				for (Entity e : getPassengers(vehicle)) {
					list.add(e.getEntityId());
				}
				vehicles.put(vehicle.getEntityId(), list);
			}
			for (TabPlayer viewer : tab.getPlayers()) {
				if (viewer == refreshed) continue;
				if (viewer.getWorldName().equals(refreshed.getWorldName())) {
					refreshed.getArmorStandManager().spawn(viewer);
				}
			}
		} else {
			boolean fix = false;
			for (ArmorStand as : refreshed.getArmorStandManager().getArmorStands()) {
				if (as.getProperty().update()) {
					as.refresh();
					fix = true;
				}
			}
			if (fix) fixArmorStandHeights(refreshed);
		}
	}

	private void updateProperties(TabPlayer p) {
		p.loadPropertyFromConfig("tagprefix");
		p.loadPropertyFromConfig("customtagname", p.getName());
		p.loadPropertyFromConfig("tagsuffix");
		p.setProperty("nametag", p.getProperty("tagprefix").getCurrentRawValue() + p.getProperty("customtagname").getCurrentRawValue() + p.getProperty("tagsuffix").getCurrentRawValue());
		for (String property : dynamicLines) {
			if (!property.equals("nametag")) p.loadPropertyFromConfig(property);
		}
		for (String property : staticLines.keySet()) {
			if (!property.equals("nametag")) p.loadPropertyFromConfig(property);
		}
	}

	@SuppressWarnings("deprecation")
	public List<Entity> getPassengers(Entity vehicle){
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 11) {
			return vehicle.getPassengers();
		} else {
			if (vehicle.getPassenger() != null) {
				return Lists.newArrayList(vehicle.getPassenger()); 
			} else {
				return new ArrayList<Entity>();
			}
		}
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = tab.getConfiguration().config.getUsedPlaceholderIdentifiersRecursive("tagprefix", "customtagname", "tagsuffix");
		for (String line : dynamicLines) {
			usedPlaceholders.addAll(tab.getConfiguration().config.getUsedPlaceholderIdentifiersRecursive(line));
		}
		for (String line : staticLines.keySet()) {
			usedPlaceholders.addAll(tab.getConfiguration().config.getUsedPlaceholderIdentifiersRecursive(line));
		}
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.NAMETAGX;
	}

	@Override
	public void onRespawn(TabPlayer respawned) {
		if (isDisabledWorld(respawned.getWorldName())) return;
		respawned.getArmorStandManager().teleport();
	}
}