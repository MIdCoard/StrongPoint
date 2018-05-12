package com.focess.strongpoint.point;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.focess.strongpoint.StrongPoint;
import com.focess.team.team.Country;

public class Point {

	private static class RemoveCountry extends BukkitRunnable {

		private static List<RemoveCountry> rcs = new CopyOnWriteArrayList<>();

		private static RemoveCountry getRemoveCountry(final Point point) {
			for (final RemoveCountry rc : RemoveCountry.rcs)
				if (rc.point.getName().equals(point.getName()))
					return rc;
			return new RemoveCountry(point);
		}

		private final Point point;

		private int time = 0;

		private RemoveCountry(final Point point) {
			this.point = point;
			this.runTaskTimer(Point.strongPoint, 0, 20);
			RemoveCountry.rcs.add(this);
		}

		private void delete() {
			RemoveCountry.rcs.remove(this);
			this.cancel();
		}

		@Override
		public void run() {
			this.time++;
			if (this.time >= this.point.time) {
				this.point.setTempCountry();
				this.point.removeCountry();
				for (final World world : Bukkit.getWorlds())
					for (final Player player : world.getPlayers())
						player.sendMessage(Point.getMessage("PointUnlock").replace("%name%", this.point.getName()));
				this.delete();
			}
		}

	}

	private static int max;

	private static HashMap<String, String> messages = new HashMap<>();

	public static List<Point> points = new ArrayList<>();

	private static StrongPoint strongPoint;

	public static boolean createPoint(final String name, final Location location, final int money, final int time,
			final int random) {
		boolean flag = false;
		for (final Point point : Point.points)
			if (point.getName().equals(name) || point.location.equals(location))
				flag = true;
		if (flag)
			return false;
		new Point(name, location, money, time, random);
		return true;
	}

	private void setTempCountry() {
		this.tempCountry = this.country;
	}

	private static String getMessage(final String key) {
		return Point.messages.get(key);
	}

	public static Point getPoint(final Location location) {
		for (final Point point : Point.points)
			if (point.location.equals(location))
				return point;
		return null;
	}

	public static Point getPoint(final String name) {
		for (final Point point : Point.points)
			if (point.getName().equals(name))
				return point;
		return null;
	}

	private static void loadConfig() {
		final File message = new File(Point.strongPoint.getDataFolder(), "message.yml");
		final YamlConfiguration yml = YamlConfiguration.loadConfiguration(message);
		final Set<String> keys = yml.getKeys(false);
		for (final String key : keys)
			Point.messages.put(key, yml.getString(key));
	}

	public static void loadPoints(final StrongPoint strongPoint) {
		Point.strongPoint = strongPoint;
		Point.max = strongPoint.getConfig().getInt("max");
		Point.loadConfig();
		final File points = new File(strongPoint.getDataFolder(), "points");
		for (final File file : points.listFiles())
			if (file.getName().endsWith(".yml")) {
				final YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
				final String world = yml.getString("world");
				final int x = yml.getInt("x");
				final int y = yml.getInt("y");
				final int z = yml.getInt("z");
				final World w = Bukkit.getWorld(world);
				if (w == null)
					continue;
				final Location loc = new Location(w, x, y, z);
				final int money = yml.getInt("money");
				final int time = yml.getInt("time");
				final int random = yml.getInt("random");
				final String name = file.getName().substring(0, file.getName().length() - 4);
				new Point(name, loc, money, time, random);
			}
	}

	private Country country;

	private Country tempCountry;

	private final List<ItemStack> itemStacks = new ArrayList<>();

	private final Location location;

	private final int money;

	private final String name;

	private final int random;

	private final int time;

	private List<Player> starters;

	public Point(final String name, final Location location, final int money, final int time, final int random) {
		this.name = name;
		this.location = location;
		this.money = money;
		this.time = time;
		this.random = random;
		final File itemStacks = new File(
				Point.strongPoint.getDataFolder().getPath() + "/points/" + this.getName() + "/itemstacks.yml");
		if (itemStacks.exists()) {
			final YamlConfiguration yml = YamlConfiguration.loadConfiguration(itemStacks);
			for (final String key : yml.getKeys(false))
				this.itemStacks.add(yml.getItemStack(key));
		}
		Point.points.add(this);
	}

	public boolean addPer(final String key, final String value) {
		final boolean flag = Boolean.parseBoolean(value);
		return Point.strongPoint.addPer(this, key, flag);
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Point))
			return false;
		if (((Point) obj).getName().equals(this.getName()))
			return true;
		return false;
	}

	public Country getCountry() {
		return this.country;
	}

	public ItemStack[] getItemStacks() {
		return this.itemStacks.toArray(new ItemStack[this.itemStacks.size()]);
	}

	public Location getLocation() {
		return this.location;
	}

	public int getMoney() {
		return this.money;
	}

	public String getName() {
		return this.name;
	}

	public int getRandom() {
		return this.random;
	}

	public int getTime() {
		return this.time;
	}

	public boolean includePlayer(final Country country) {
		final List<Player> players = new ArrayList<>();
		final List<Player> temp = new ArrayList<>();
		for (final Entity entity : this.location.getWorld().getEntities())
			if (entity instanceof Player)
				if (country.includePlayer((Player) entity))
					players.add((Player) entity);
		for (final Player player : players)
			if (player.getLocation().getBlockX() < this.location.getBlockX() + this.random
					&& player.getLocation().getBlockX() > this.location.getBlockX() - this.random
					&& player.getLocation().getBlockZ() < this.location.getBlockZ() + this.random
					&& player.getLocation().getBlockZ() > this.location.getBlockZ() - this.random)
				temp.add(player);
		if (temp.size() > Point.max - 1) {
			this.starters = temp;
			return true;
		}
		return false;
	}

	public boolean inside(final Location loc) {
		final Location[] ls = new Location[2];
		final Location ls1 = this.location.clone();
		final Location ls2 = this.location.clone();
		ls1.setX(ls1.getX() + this.random);
		ls1.setZ(ls1.getZ() + this.random);
		ls1.setY(1);
		ls2.setX(ls2.getX() - this.random);
		ls2.setZ(ls2.getZ() - this.random);
		ls2.setY(255);
		ls[0] = ls1;
		ls[1] = ls2;
		if (ls[0] == null || ls[1] == null)
			return false;
		if (loc.getWorld().equals(ls[0].getWorld())) {
			if (ls[0].getBlockX() > ls[1].getBlockX()) {
				if (loc.getBlockX() > ls[0].getBlockX() || loc.getBlockX() < ls[1].getBlockX())
					return false;
			} else if (loc.getBlockX() < ls[0].getBlockX() || loc.getBlockX() > ls[1].getBlockX())
				return false;
			if (ls[0].getBlockY() > ls[1].getBlockY()) {
				if (loc.getBlockY() > ls[0].getBlockY() || loc.getBlockY() < ls[1].getBlockY())
					return false;
			} else if (loc.getBlockY() < ls[0].getBlockY() || loc.getBlockY() > ls[1].getBlockY())
				return false;
			if (ls[0].getBlockZ() > ls[1].getBlockZ()) {
				if (loc.getBlockZ() > ls[0].getBlockZ() || loc.getBlockZ() < ls[1].getBlockZ())
					return false;
			} else if (loc.getBlockZ() < ls[0].getBlockZ() || loc.getBlockZ() > ls[1].getBlockZ())
				return false;
		}
		return true;
	}

	public void openInventory(final Player player) {
		final Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST,
				"§a请放入为占领据点" + this.getName() + "存放的物资");
		final List<ItemStack> temp = new ArrayList<>();
		for (final ItemStack itemStack : this.itemStacks)
			if (itemStack != null)
				temp.add(itemStack);
		inventory.addItem(temp.toArray(new ItemStack[temp.size()]));
		player.openInventory(inventory);
	}

	public void remove() {
		final File point = new File(Point.strongPoint.getDataFolder().getPath() + "/points/" + this.getName() + ".yml");
		point.delete();
		Point.points.remove(this);
		if (RemoveCountry.getRemoveCountry(this) != null)
			RemoveCountry.getRemoveCountry(this).delete();
		try {
			this.finalize();
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}

	public void removeCountry() {
		this.country = null;
	}

	public void Serialize() {
		final File point = new File(Point.strongPoint.getDataFolder().getPath() + "/points/" + this.name + ".yml");
		final YamlConfiguration yml = YamlConfiguration.loadConfiguration(point);
		yml.set("world", this.location.getWorld().getName());
		yml.set("x", this.location.getBlockX());
		yml.set("y", this.location.getBlockY());
		yml.set("z", this.location.getBlockZ());
		yml.set("money", this.money);
		yml.set("time", this.time);
		yml.set("random", this.random);
		try {
			yml.save(point);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		final File itemStacks = new File(
				Point.strongPoint.getDataFolder().getPath() + "/points/" + this.getName() + "/itemstacks.yml");
		if (!itemStacks.exists())
			try {
				itemStacks.createNewFile();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		else {
			itemStacks.delete();
			try {
				itemStacks.createNewFile();
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
		}
		final YamlConfiguration yml2 = YamlConfiguration.loadConfiguration(itemStacks);
		for (int i = 0; i < this.itemStacks.size(); i++)
			if (this.itemStacks.get(i) != null)
				yml2.set(i + "", this.itemStacks.get(i));
		try {
			yml2.save(itemStacks);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setCountry(final Country country,boolean flag) {
		if (flag)
			this.setCountry(country);
		else {
			RemoveCountry.getRemoveCountry(this).delete();
			this.country = country;
		}
	}

	@SuppressWarnings("deprecation")
	public void setCountry(final Country country) {
		if (this.country != null) {
			RemoveCountry.getRemoveCountry(this).delete();
			for (final String player : this.country.getPlayers())
				if (Bukkit.getPlayerExact(player) != null)
					Bukkit.getPlayerExact(player).sendMessage(Point.getMessage("EnemyPlacedPoint")
							.replace("%name%", this.getName()).replace("%country%", country.getName()));
		}
		this.country = country;
		for (final Player player : this.starters) {
			player.sendMessage(Point.getMessage("PlacedPoint").replace("%name%", this.getName()));
			player.getInventory().addItem(this.getItemStacks());
			player.updateInventory();
			Point.strongPoint.economy.depositPlayer(player, this.money);
		}
		for (final String friend : country.getFriends())
			for (final String player : Country.getCountry(friend).getPlayers())
				if (Bukkit.getPlayerExact(player) != null){
					Bukkit.getPlayerExact(player)
							.sendMessage(Point.getMessage("PlacedPoint").replace("%name%", this.getName()));
					if (this.inside(Bukkit.getPlayerExact(player).getLocation()))
							Point.strongPoint.economy.depositPlayer(Bukkit.getPlayerExact(player), this.money);
				}
	new RemoveCountry(this);
	}

	public void setItemStacks(final ItemStack... itemStacks) {
		this.itemStacks.clear();
		for (final ItemStack itemStack : itemStacks)
			this.itemStacks.add(itemStack);
	}

	public Country getTeleportableCountry() {
		return this.country == null ? this.tempCountry : this.country;
	}

}
