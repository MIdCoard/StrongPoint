package com.focess.strongpoint.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.focess.strongpoint.StrongPoint;
import com.focess.strongpoint.point.Point;

public class BlockBreakListener extends Permission implements Listener {

	public BlockBreakListener(final StrongPoint strongPoint) {
		super(strongPoint);
	}

	@Override
	public String getLabel() {
		return "break";
	}

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {
		for (final Point point : Point.points)
			if (point.inside(event.getBlock().getLocation()))
				if (!this.isAllowed(point)) {
					event.getPlayer().sendMessage(
							Permission.getMessage("BreakError").replace(
									"%name%", point.getName()));
					event.setCancelled(true);
				}
	}

}
