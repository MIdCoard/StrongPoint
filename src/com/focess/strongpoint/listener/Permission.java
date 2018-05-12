package com.focess.strongpoint.listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import com.focess.strongpoint.StrongPoint;
import com.focess.strongpoint.point.Point;

public abstract class Permission {

	public class PointPer {

		private final Point point;

		private boolean value;

		public PointPer(final Point point, final boolean value) {
			this.point = point;
			this.value = value;
		}

		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof PointPer))
				return false;
			if (((PointPer) obj).point.getName().equals(this.point.getName()))
				return true;
			return false;
		}

		public Point getPoint() {
			return this.point;
		}

		public boolean getValue() {
			return this.value;
		}
	}

	public static final int high = 0;

	public static final int low = 1;

	private static HashMap<String, String> messages = new HashMap<>();

	protected static String getMessage(final String key) {
		return Permission.messages.get(key);
	}

	protected List<PointPer> pbs = new ArrayList<>();

	private final StrongPoint strongPoint;

	public Permission(final StrongPoint strongPoint) {
		this.strongPoint = strongPoint;
		this.loadConfig();
		final File points = new File(strongPoint.getDataFolder(), "points");
		for (final File file : points.listFiles())
			if (file.getName().endsWith(".yml")) {
				final YamlConfiguration yml = YamlConfiguration
						.loadConfiguration(file);
				final String name = file.getName().substring(0,
						file.getName().length() - 4);
				if (yml.contains(this.getLabel()))
					if (yml.getBoolean(this.getLabel()))
						this.addPermission(Permission.low, Point.getPoint(name));
					else
						this.addPermission(Permission.high,
								Point.getPoint(name));
			}
	}

	public void addPermission(final int level, final Point point) {
		PointPer p = null;
		if (level == 0)
			p = new PointPer(point, false);
		else if (level == 1)
			p = new PointPer(point, true);
		for (final PointPer pp : this.pbs)
			if (pp.equals(p)) {
				pp.value = p.value;
				return;
			}
		this.pbs.add(p);
	}

	public abstract String getLabel();

	public boolean isAllowed(final Point point) {
		for (final PointPer pp : this.pbs)
			if (pp.getPoint().equals(point))
				return pp.getValue();
		return true;
	}

	private void loadConfig() {
		final File message = new File(this.strongPoint.getDataFolder(),
				"message.yml");
		final YamlConfiguration yml = YamlConfiguration
				.loadConfiguration(message);
		final Set<String> keys = yml.getKeys(false);
		for (final String key : keys)
			Permission.messages.put(key, yml.getString(key));
	}

	public void removePermission(final Point point) {
		final PointPer pp = new PointPer(point, false);
		PointPer temp = null;
		for (final PointPer p : this.pbs)
			if (p.equals(pp))
				temp = p;
		if (temp != null)
			this.pbs.remove(temp);
	}

	public void Serialize() {
		for (final PointPer pp : this.pbs) {
			final Point point = pp.point;
			final boolean per = pp.value;
			final File p = new File(this.strongPoint.getDataFolder().getPath()
					+ "/points/" + point.getName() + ".yml");
			if (!p.exists())
				try {
					p.createNewFile();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			final YamlConfiguration yml = YamlConfiguration
					.loadConfiguration(p);
			yml.set(this.getLabel(), per);
			try {
				yml.save(p);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

}
