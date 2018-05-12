package com.focess.strongpoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.focess.strongpoint.command.StrongPointCommand;
import com.focess.strongpoint.listener.BlockBreakListener;
import com.focess.strongpoint.listener.BlockPlaceListener;
import com.focess.strongpoint.listener.InventoryCloseListener;
import com.focess.strongpoint.listener.Permission;
import com.focess.strongpoint.listener.PlayerInteractListener;
import com.focess.strongpoint.listener.PlayerMoveListener;
import com.focess.strongpoint.point.Point;

public class StrongPoint extends JavaPlugin {

	private Permission blockbreak;

	private Permission build;

	private CommandMap commandMap;

	public Economy economy;

	private Permission use;

	{
		try {
			this.getCommandMap();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public boolean addPer(final Point point, final String key,
			final boolean value) {
		if (key.equalsIgnoreCase("use"))
			if (value)
				this.use.addPermission(Permission.low, point);
			else
				this.use.addPermission(Permission.high, point);
		else if (key.equalsIgnoreCase("break"))
			if (value)
				this.blockbreak.addPermission(Permission.low, point);
			else
				this.blockbreak.addPermission(Permission.high, point);
		else if (key.equalsIgnoreCase("build"))
			if (value)
				this.build.addPermission(Permission.low, point);
			else
				this.build.addPermission(Permission.high, point);
		else
			return false;
		return true;
	}

	private void getCommandMap() throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		final Class<?> c = Bukkit.getServer().getClass();
		for (final Method method : c.getDeclaredMethods())
			if (method.getName().equals("getCommandMap"))
				this.commandMap = (CommandMap) method.invoke(this.getServer(),
						new Object[0]);
	}

	private void loadConfig() {
		if (!this.getDataFolder().exists())
			this.getDataFolder().mkdir();
		final File file = new File(this.getDataFolder(), "config.yml");
		if (!file.exists())
			this.saveDefaultConfig();
		this.reloadConfig();
		final File points = new File(this.getDataFolder(), "points");
		if (!points.exists())
			points.mkdir();
		final File players = new File(this.getDataFolder(), "players");
		if (!players.exists())
			players.mkdir();
		this.loadFile(new File(this.getDataFolder(), "message.yml"),
				"message.yml");
	}

	private void loadFile(final File targetFile, final String loadingFile) {
		if (targetFile.exists())
			return;
		String jarFilePath = this.getClass().getProtectionDomain()
				.getCodeSource().getLocation().getFile();
		try {
			jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
			final JarFile jar = new JarFile(jarFilePath);
			InputStream is = null;
			final Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				final JarEntry entry = entries.nextElement();
				if (entry.getName().equals(loadingFile)) {
					is = jar.getInputStream(entry);
					break;
				}
			}
			final FileOutputStream out = new FileOutputStream(targetFile);
			int c = 0;
			while ((c = is.read()) != -1)
				out.write(c);
			out.close();
			jar.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		this.getLogger().info("StrongPoint插件载出成功");
		for (final Point point : Point.points)
			point.Serialize();
		this.use.Serialize();
		this.blockbreak.Serialize();
		this.build.Serialize();
	}

	@Override
	public void onEnable() {
		this.getLogger().info("StrongPoint插件载入成功");
		if (Bukkit.getPluginManager().getPlugin("Vault") != null)
			System.out.println(this.setupEconomy());
		this.loadConfig();
		Point.loadPoints(this);
		Bukkit.getPluginManager().registerEvents(
				(Listener) (this.use = new PlayerInteractListener(this)), this);
		Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(this),
				this);
		Bukkit.getPluginManager().registerEvents(
				(Listener) (this.blockbreak = new BlockBreakListener(this)),
				this);
		Bukkit.getPluginManager().registerEvents(
				(Listener) (this.build = new BlockPlaceListener(this)), this);
		Bukkit.getPluginManager().registerEvents(new InventoryCloseListener(),
				this);
		final List<String> aliases = new ArrayList<>();
		aliases.add("strp");
		this.commandMap.register(this.getDescription().getName(),
				new StrongPointCommand(aliases, this));
	}

	private boolean setupEconomy() {
		final RegisteredServiceProvider<Economy> economyProvider = Bukkit
				.getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null)
			this.economy = economyProvider.getProvider();
		return this.economy != null;
	}

}
