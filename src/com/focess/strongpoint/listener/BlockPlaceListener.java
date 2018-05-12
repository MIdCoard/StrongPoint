package com.focess.strongpoint.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.focess.strongpoint.StrongPoint;
import com.focess.strongpoint.point.Point;

public class BlockPlaceListener extends Permission implements Listener {

	public BlockPlaceListener(final StrongPoint strongPoint) {
		super(strongPoint);
	}

	@Override
	public String getLabel() {
		return "build";
	}

	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {
		for (final Point point : Point.points)
			if (point.inside(event.getBlock().getLocation()))
				if (!this.isAllowed(point)) {
					event.getPlayer().sendMessage(
							Permission.getMessage("BuildError").replace(
									"%name%", point.getName()));
					event.setCancelled(true);
				}

	}

}
