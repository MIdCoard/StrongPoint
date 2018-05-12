package com.focess.strongpoint.listener;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.focess.strongpoint.StrongPoint;
import com.focess.strongpoint.point.Point;
import com.focess.team.team.Country;

public class PlayerMoveListener implements Listener {

	private final HashMap<String, String> messages = new HashMap<>();

	private final StrongPoint strongPoint;

	public PlayerMoveListener(final StrongPoint strongPoint) {
		this.strongPoint = strongPoint;
		this.loadConfig();
	}

	protected String getMessage(final String key) {
		return this.messages.get(key);
	}

	private boolean isNotice(final Player player) {
		final File p = new File(this.strongPoint.getDataFolder().getPath()
				+ "/players/" + player.getName() + ".yml");
		if (p.exists()) {
			final YamlConfiguration yml = YamlConfiguration
					.loadConfiguration(p);
			if (yml.contains("isNotice"))
				return yml.getBoolean("isNotice");
			else
				return true;
		} else
			return true;
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

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerMove(final PlayerMoveEvent event) {
		for (final Point point : Point.points)
			if (point.inside(event.getTo()) && !point.inside(event.getFrom()))
				if (point.getCountry() != null) {
					boolean flag = false;
					Country c = null;
					for (final Country country : Country.listCountries())
						if (country.includePlayer(event.getPlayer())) {
							flag = true;
							c = country;
							break;
						}
					if (!flag) {
						event.getPlayer().sendMessage(
								this.getMessage("Move")
										.replace("%name%", point.getName())
										.replace("%country%",
												point.getCountry().getName()));
						for (final String player : point.getCountry()
								.getPlayers())
							if (Bukkit.getPlayerExact(player) != null)
								if (this.isNotice(Bukkit.getPlayerExact(player)))
									Bukkit.getPlayerExact(player).sendMessage(
											this.getMessage("Enter")
													.replace(
															"%player%",
															event.getPlayer()
																	.getName())
													.replace("%name%",
															point.getName()));
					} else if (point.getCountry().isEnemy(c)) {
						event.getPlayer().sendMessage(
								this.getMessage("MoveE")
										.replace("%name%", point.getName())
										.replace("%country%",
												point.getCountry().getName()));
						for (final String player : point.getCountry()
								.getPlayers())
							if (Bukkit.getPlayerExact(player) != null)
								if (this.isNotice(Bukkit.getPlayerExact(player)))
									Bukkit.getPlayerExact(player).sendMessage(
											this.getMessage("EnterE")
													.replace(
															"%player%",
															event.getPlayer()
																	.getName())
													.replace("%name%",
															point.getName()));
					} else {
						event.getPlayer().sendMessage(
								this.getMessage("Move")
										.replace("%name%", point.getName())
										.replace("%country%",
												point.getCountry().getName()));
						for (final String player : point.getCountry()
								.getPlayers())
							if (Bukkit.getPlayerExact(player) != null)
								if (this.isNotice(Bukkit.getPlayerExact(player)))
									Bukkit.getPlayerExact(player).sendMessage(
											this.getMessage("Enter")
													.replace(
															"%player%",
															event.getPlayer()
																	.getName())
													.replace("%name%",
															point.getName()));
					}
				} else
					event.getPlayer().sendMessage(
							this.getMessage("MoveN").replace("%name%",
									point.getName()));
	}
}
