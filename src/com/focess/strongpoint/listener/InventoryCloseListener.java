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
			if (event.getInventory().getName().startsWith("��a�����Ϊռ��ݵ�")
					&& event.getInventory().getName().endsWith("��ŵ�����")) {
				final String name = event.getInventory().getName()
						.replace("��a�����Ϊռ��ݵ�", "").replace("��ŵ�����", "");
				if (Point.getPoint(name) != null)
					Point.getPoint(name).setItemStacks(
							event.getInventory().getContents());
			}
	}

}
