package me.kitskub.myhungergames.utils;

import me.kitskub.myhungergames.WorldNotFoundException;

import org.spout.api.geo.discrete.Point;

public class Cuboid {
	private final Point lower;
	private final Point upper;

	public Cuboid(Point lower, Point upper) {
		
		this.lower = new Point(
			lower.getWorld(),
			Math.min(lower.getX(), upper.getX()),
			Math.min(lower.getY(), upper.getY()),
			Math.min(lower.getZ(), upper.getZ())
			);
		this.upper  = new Point(
			lower.getWorld(),
			Math.max(lower.getX(), upper.getX()),
			Math.max(lower.getY(), upper.getY()),
			Math.max(lower.getZ(), upper.getZ())
			);
	}
	
	private Cuboid(Point lower, Point upper, boolean internal) {
		this.lower = lower;
		this.upper = upper;
	}
	
	public boolean isPointWithin(Point loc) {
		return (upper.getX() >  loc.getX() && lower.getX() < loc.getX()
			&& upper.getZ() > loc.getZ() && lower.getZ() < loc.getZ()
			&& upper.getY() > loc.getY() && lower.getY() < loc.getY());
	}

	public Point getLower() {
		return lower;
	}

	public Point getUpper() {
		return upper;
	}
	
	public String parseToString() {
		return GeneralUtils.parseToString(lower) + ":" + GeneralUtils.parseToString(upper);
	}
	
	public static Cuboid parseFromString(String string) {
		try {
			String[] parts = string.split(":");
			Point lower = GeneralUtils.parseToPoint(parts[0]);
			Point upper = GeneralUtils.parseToPoint(parts[1]);
			return new Cuboid(lower, upper, true);
		} catch (NumberFormatException ex) {
			return null;
		} catch (WorldNotFoundException ex) {
			return null;
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}
}
