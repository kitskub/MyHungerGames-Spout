package me.kitskub.myhungergames;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class Logging {
	private static final Logger logger = Logger.getLogger("MyHungerGames");

	public static void log(Level level, String record) {
		logger.log(level, record);		
	}

	public static void log(Level level, String record, String... strings) {
		logger.log(level, record, strings);		
	}

	static {
		try {
			HungerGames instance = HungerGames.getInstance();
			instance.getDataFolder().mkdirs();
			File file = Files.LOG.getFile();
			FileHandler handler = new FileHandler(file.getPath(), true);
			handler.setFormatter(new SimpleFormatter());
			logger.addHandler(handler);
			logger.setLevel(Level.FINEST);
			Logger parent = Logger.getLogger("Minecraft");
			logger.setParent(parent);
		} catch (IOException ex) {
		}

	}

	public static String getLogPrefix() {
		return String.format("[%s] %s - ", HungerGames.getInstance().getName(), HungerGames.getInstance().getDescription().getVersion());
	}

	public static void info(String format, Object... args) {
		log(Level.INFO, getLogPrefix() + String.format(format, args));
	}

	public static void info(String mess) {
		log(Level.INFO, getLogPrefix() + mess);
	}

	public static void warning(String format, Object... args) {
		log(Level.WARNING, getLogPrefix() + String.format(format, args));
	}

	public static void warning(String mess) {
		log(Level.WARNING, getLogPrefix() + mess);
	}

	public static void severe(String format, Object... args) {
		log(Level.SEVERE, getLogPrefix() + String.format(format, args));
	}

	public static void severe(String mess) {
		log(Level.SEVERE, getLogPrefix() + mess);
	}

	public static void debug(String mess, Object... args) {
		log(Level.FINEST, getLogPrefix() + String.format(mess, args));
	}

	public static void debug(String mess) {
		log(Level.FINEST, getLogPrefix() + mess);
	}
	
	/*public static class LogCommandSource implements CommandSource {
		String who = "";
		
		public LogCommandSource(String who) {
			this.who = who;
		}
		
		public void sendMessage(String string) {
			log(Level.INFO, "CS for " + who + ": " + string);
		}

		public void sendMessage(String[] strings) {
			for (String string : strings) {
				log(Level.INFO, "CS for " + who + ": " + string);
			}
		}

		public Server getServer() {
			return HungerGames.getInstance().getServer();
		}

		public String getName() {
			return "MyHungerGames Logger for " + who;
		}

		public boolean isPermissionSet(String string) {
			return true;
		}

		public boolean isPermissionSet(Permission prmsn) {
			return true;
		}

		public boolean hasPermission(String string) {
			return true;
		}

		public boolean hasPermission(Permission prmsn) {
			return true;
		}

		public boolean sendMessage(Object... message) {
			for (Object obj : message) {
				log(Level.INFO, "CS for " + who + ": " + obj);
			}
			return true;
		}

		public void sendCommand(String command, ChatArguments arguments) {
		}

		public void processCommand(String command, ChatArguments arguments) {
		}

		public boolean sendMessage(ChatArguments message) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean sendRawMessage(Object... message) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean sendRawMessage(ChatArguments message) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Locale getPreferredLocale() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean hasPermission(World world, String node) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isInGroup(String group) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isInGroup(World world, String group) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String[] getGroups() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String[] getGroups(World world) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public ValueHolder getData(String node) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public ValueHolder getData(World world, String node) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean hasData(String node) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean hasData(World world, String node) {
			throw new UnsupportedOperationException("Not supported yet.");
		}
		
	}*/
}
