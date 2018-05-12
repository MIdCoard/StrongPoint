package com.focess.strongpoint.listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.focess.strongpoint.StrongPoint;
import com.focess.strongpoint.point.Point;
import com.focess.team.team.Country;

public class PlayerInteractListener extends Permission implements Listener {

	public static class CheckedBlock {

		private static List<CheckedBlock> cbs = new ArrayList<>();

		public static void addBlock(final Player player, final Block block) {
			boolean flag = false;
			CheckedBlock c = null;
			for (final CheckedBlock cb : CheckedBlock.cbs)
				if (cb.player.equals(player)) {
					flag = true;
					c = cb;
					break;
				}
			if (flag)
				c.addBlock(block.getLocation());
			else
				new CheckedBlock(player).addBlock(block.getLocation());
		}

		public static Location getLocation(final Player player) {
			for (final CheckedBlock cb : CheckedBlock.cbs)
				if (cb.player.equals(player))
					return cb.loc;
			return null;
		}

		private Location loc;

		private final Player player;

		public CheckedBlock(final Player player) {
			this.player = player;
			CheckedBlock.cbs.add(this);
		}

		public void addBlock(final Location loc) {
			this.loc = loc;
			this.player.sendMessage(Permission.getMessage("CheckedBlock"));
		}
	}

	private int max = 0;

	public PlayerInteractListener(final StrongPoint strongPoint) {
		super(strongPoint);
		this.max = strongPoint.getConfig().getInt("max");
	}

	@Override
	public String getLabel() {
		return "use";
	}

	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event) {
		if (event.getPlayer().isOp())
			if (event.getItem() != null)
				if (event.getClickedBlock() != null)
					if (event.getItem().getType().equals(Material.SULPHUR)) {
						CheckedBlock.addBlock(event.getPlayer(),
								event.getClickedBlock());
						event.setCancelled(true);
					}
		if (event.getClickedBlock() != null)
			if (Point.getPoint(event.getClickedBlock().getLocation()) != null) {
				event.setCancelled(true);
				if (!this.isAllowed(Point.getPoint(event.getClickedBlock()
						.getLocation()))) {
					event.getPlayer().sendMessage(
							Permission.getMessage("UseError").replace(
									"%name%",
									Point.getPoint(
											event.getClickedBlock()
													.getLocation()).getName()));
					return;
				}
				final Point point = Point.getPoint(event.getClickedBlock()
						.getLocation());
				boolean isCountry = false;
				Country c = null;
				for (final Country country : Country.listCountries())
					if (country.includePlayer(event.getPlayer())) {
						isCountry = true;
						c = country;
						break;
					}
				if (!isCountry) {
					event.getPlayer().sendMessage(
							Permission.getMessage("NoCountry"));
					return;
				}
				if (Point.getPoint(event.getClickedBlock().getLocation())
						.getCountry() == null
						|| Point.getPoint(event.getClickedBlock().getLocation())
								.getCountry().isEnemy(c))
					if (point.includePlayer(c))
						point.setCountry(c);
					else
						event.getPlayer().sendMessage(
								Permission.getMessage("NotEnoughPlayers")
										.replace("%max%", this.max + ""));
				else
					event.getPlayer().sendMessage(
							Permission.getMessage("PointHavePlaced"));
			}
	}

}
