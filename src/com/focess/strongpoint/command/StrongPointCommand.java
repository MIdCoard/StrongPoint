package com.focess.strongpoint.command;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.focess.strongpoint.StrongPoint;
import com.focess.strongpoint.listener.PlayerInteractListener.CheckedBlock;
import com.focess.strongpoint.point.Point;
import com.focess.team.team.Country;

public class StrongPointCommand extends Command {

	private final HashMap<String, String> messages = new HashMap<>();

	private final StrongPoint strongPoint;

	public StrongPointCommand(final List<String> aliases,
			final StrongPoint strongPoint) {
		super("StrongPoint", "", "", aliases);
		this.strongPoint = strongPoint;
		this.loadConfig();
	}

	@Override
	public boolean execute(final CommandSender sender, final String cmd,
			final String[] args) {
		if (sender instanceof Player)
			if (args.length == 1)
				if (args[0].equalsIgnoreCase("notice")) {
					final File player = new File(this.strongPoint
							.getDataFolder().getPath()
							+ "/players/"
							+ sender.getName() + ".yml");
					if (!player.exists())
						try {
							player.createNewFile();
						} catch (final IOException e) {
							e.printStackTrace();
						}
					final YamlConfiguration yml = YamlConfiguration
							.loadConfiguration(player);
					if (yml.contains("isNotice"))
						if (yml.getBoolean("isNotice"))
							yml.set("isNotice", false);
						else
							yml.set("isNotice", true);
					else
						yml.set("isNotice", false);
					try {
						yml.save(player);
					} catch (final IOException e) {
						e.printStackTrace();
					}
					if (yml.getBoolean("isNotice"))
						sender.sendMessage(this.getMessage("Notice"));
					else
						sender.sendMessage(this.getMessage("NoticeNot"));
				} else if (args[0].equalsIgnoreCase("list"))
					if (sender.isOp()) {
						sender.sendMessage(this.getMessage("List") + ": ");
						final StringBuilder sb = new StringBuilder();
						for (final Point point : Point.points)
							sb.append(point.getName() + "  ");
						sender.sendMessage(sb.toString());
					} else
						sender.sendMessage(this.getMessage("SenderNotOp"));
				else
					sender.sendMessage(this.getMessage("CommandError"));
			else if (args.length == 2)
				if (args[0].equalsIgnoreCase("teleport"))
					if (Point.getPoint(args[1]) != null) {
						final Point point = Point.getPoint(args[1]);
						if (point.getTeleportableCountry() != null) {
							boolean flag = false;
							Country c = null;
							for (final Country country : Country
									.listCountries())
								if (country.includePlayer((Player) sender)) {
									flag = true;
									c = country;
									break;
								}
							if (!flag) {
								sender.sendMessage(this.getMessage("NoCountry"));
								return true;
							}
							if (c.isFriend(point.getTeleportableCountry())
									|| c.getName().equals(
											point.getTeleportableCountry().getName())) {
								final Location temp = point.getLocation()
										.clone();
								temp.setY(temp.getY() + 1);
								((Player) sender).teleport(temp);
								sender.sendMessage(this.getMessage("Teleport")
										.replace("%name%", point.getName()));
							}
						} else
							sender.sendMessage(this
									.getMessage("PointHaveNotPlaced"));
					} else
						sender.sendMessage(this.getMessage("PointNotFound"));
				else if (args[0].equalsIgnoreCase("list"))
					if (sender.isOp())
						if (Point.getPoint(args[1]) != null) {
							final Point point = Point.getPoint(args[1]);
							sender.sendMessage(this.getMessage("ListPoint")
									+ ": ");
							boolean isPlaced = false;
							if (point.getCountry() != null)
								isPlaced = true;
							sender.sendMessage(this.getMessage("IsPlaced")
									+ ": " + isPlaced);
							sender.sendMessage(this.getMessage("PointWorld")
									+ ": "
									+ point.getLocation().getWorld().getName());
							sender.sendMessage(this.getMessage("PointX") + ": "
									+ point.getLocation().getBlockX());
							sender.sendMessage(this.getMessage("PointY") + ": "
									+ point.getLocation().getBlockY());
							sender.sendMessage(this.getMessage("PointZ") + ": "
									+ point.getLocation().getBlockZ());
							sender.sendMessage(this.getMessage("PointMoney")
									+ ": " + point.getMoney());
							sender.sendMessage(this.getMessage("PointTime")
									+ ": " + point.getTime());
							sender.sendMessage(this.getMessage("PointRandom")
									+ ": " + point.getRandom());
						} else
							sender.sendMessage(this.getMessage("PointNotFound"));
					else
						sender.sendMessage(this.getMessage("SenderNotOp"));
				else if (args[0].equalsIgnoreCase("remove"))
					if (sender.isOp())
						if (Point.getPoint(args[1]) != null) {
							sender.sendMessage(this.getMessage("Remove")
									.replace("%name%",
											Point.getPoint(args[1]).getName()));
							Point.getPoint(args[1]).remove();
						} else
							sender.sendMessage(this.getMessage("PointNotFound"));
					else
						sender.sendMessage(this.getMessage("SenderNotOp"));
				else if (args[0].equalsIgnoreCase("open"))
					if (sender.isOp())
						if (Point.getPoint(args[1]) != null)
							Point.getPoint(args[1]).openInventory(
									(Player) sender);
						else
							sender.sendMessage(this.getMessage("PointNotFound"));
					else
						sender.sendMessage(this.getMessage("SenderNotOp"));
				else
					sender.sendMessage(this.getMessage("CommandError"));
			else if (args.length == 4)
				if (args[0].equalsIgnoreCase("per"))
					if (sender.isOp())
						if (Point.getPoint(args[1]) != null)
							if (Point.getPoint(args[1])
									.addPer(args[2], args[3]))
								sender.sendMessage(this.getMessage("AddPer"));
							else
								sender.sendMessage(this
										.getMessage("AddPerError"));
						else
							sender.sendMessage(this.getMessage("PointNotFound"));
					else
						sender.sendMessage(this.getMessage("SenderNotOp"));
				else
					sender.sendMessage(this.getMessage("CommandError"));
			else if (args.length == 5)
				if (args[0].equalsIgnoreCase("add"))
					if (sender.isOp()) {
						final String name = args[1];
						int money = 0;
						int time = 0;
						int random = 0;
						try {
							money = Integer.parseInt(args[2]);
							time = Integer.parseInt(args[3]);
							random = Integer.parseInt(args[4]);
						} catch (final Exception e) {
							sender.sendMessage(this.getMessage("ArgsNotInt"));
						}
						if (CheckedBlock.getLocation((Player) sender) != null)
							if (Point.createPoint(name,
									CheckedBlock.getLocation((Player) sender),
									money, time, random))
								sender.sendMessage(this.getMessage("AddPoint")
										.replace("%name%", name));
							else
								sender.sendMessage(this
										.getMessage("AddPointError"));
						else
							sender.sendMessage(this.getMessage("NoLocation"));
					} else
						sender.sendMessage(this.getMessage("SenderNotOp"));
				else
					sender.sendMessage(this.getMessage("CommandError"));
			else
				sender.sendMessage(this.getMessage("CommandError"));
		else
			sender.sendMessage(this.getMessage("SenderNotPlayer"));
		return true;
	}

	private String getMessage(final String key) {
		return this.messages.get(key);
	}

	private void loadConfig() {
		final File message = new File(this.strongPoint.getDataFolder(),
				"message.yml");
		final YamlConfiguration yml = YamlConfiguration
				.loadConfiguration(message);
		final Set<String> keys = yml.getKeys(false);
		for (final String key : keys)
			this.messages.put(key, yml.getString(key));
	}

}
