package com.focess.strongpoint.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.focess.strongpoint.point.Point;

public class InventoryCloseListener implements Listener {

	@EventHandler
	public void onInventoryClose(final InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player && event.getPlayer().isOp())
			if (event.getInventory().getName().startsWith("§a请放入为占领据点")
					&& event.getInventory().getName().endsWith("存放的物资")) {
				final String name = event.getInventory().getName()
						.replace("§a请放入为占领据点", "").replace("存放的物资", "");
				if (Point.getPoint(name) != null)
					Point.getPoint(name).setItemStacks(
							event.getInventory().getContents());
			}
	}

}
