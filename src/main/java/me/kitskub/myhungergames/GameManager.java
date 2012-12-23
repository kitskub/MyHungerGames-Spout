package me.kitskub.myhungergames;

import com.google.common.base.Strings;
import me.kitskub.myhungergames.api.Game;

import me.kitskub.myhungergames.games.HungerGame;
import me.kitskub.myhungergames.stats.PlayerStat;
import me.kitskub.myhungergames.utils.EquatableWeakReference;

import java.lang.ref.WeakReference;
import java.util.*;

import org.spout.api.entity.Player;
import org.spout.api.geo.discrete.Point;
import org.spout.api.util.config.ConfigurationNode;


public class GameManager extends me.kitskub.myhungergames.api.GameManager {
	private static final HungerGames plugin = HungerGames.getInstance();
	public static final GameManager INSTANCE = new GameManager();
	private static Map<String, Map<EquatableWeakReference<HungerGame>, PlayerStat>> stats = new HashMap<String, Map<EquatableWeakReference<HungerGame>, PlayerStat>>();
	private static final Set<HungerGame> games = new TreeSet<HungerGame>();
	private static final Map<String, EquatableWeakReference<HungerGame>> spectators = new HashMap<String, EquatableWeakReference<HungerGame>>(); // <player, game>
	private static final Map<String, Point> frozenPlayers = new HashMap<String, Point>();
	private static final Set<String> globalSubscribedPlayers = new HashSet<String>();
	private static final Map<EquatableWeakReference<HungerGame>, Set<String>> subscribedPlayers = new HashMap<EquatableWeakReference<HungerGame>, Set<String>>();
	private static final Map<String, Point> playerBackPoints = new HashMap<String, Point>();
	
	@Override
	public boolean createGame(String name) {
	    HungerGame game = new HungerGame(name);
	    boolean attempt = games.add(game);
	    if(attempt){
		saveGames();
	    }
	    return attempt;
	}

	@Override
	public boolean createGame(String name, String setup){
	    HungerGame game = new HungerGame(name, setup);
	    boolean attempt = games.add(game);
	    if(attempt){
		saveGames();
	    }
	    return attempt;
	}
	
	@Override
	public boolean removeGame(String name) {
		HungerGame game = null;
		if (Strings.nullToEmpty(name).equals("")) return false;
		for (HungerGame g : games) {
			if (g.getName().equalsIgnoreCase(name)) {
				game = g;
			}

		}
		if(game == null) return false;
		boolean attempt = games.remove(game);
		game.delete();
		if(attempt){
			saveGames();
		}
		return attempt;
	}
		
	public PlayerStat createStat(HungerGame game, Player player) {
		PlayerStat stat = new PlayerStat(game, player);
		if (stats.get(player.getName()) == null) stats.put(player.getName(), new HashMap<EquatableWeakReference<HungerGame>, PlayerStat>());
		stats.get(player.getName()).put(new EquatableWeakReference<HungerGame>(game), stat);
		return stat;
	}
	
	public void clearGamesForPlayer(String player, HungerGame game) {
		stats.get(player).remove(new EquatableWeakReference<HungerGame>(game));
	}

	@Override
	public List<HungerGame> getRawGames() {
		return new ArrayList<HungerGame>(games);
	}

	
	@Override
	public List<EquatableWeakReference<HungerGame>> getGames() {
		List<EquatableWeakReference<HungerGame>> list = new ArrayList<EquatableWeakReference<HungerGame>>();
		for (HungerGame game : games ) {
			list.add(new EquatableWeakReference<HungerGame>(game));
		}
		return list;
	}

	@Override
	public EquatableWeakReference<HungerGame> getGame(String name) {
		HungerGame game = getRawGame(name);
		if (game != null) return new EquatableWeakReference<HungerGame>(game);
		return null;
	}

	@Override
	public HungerGame getRawGame(String name) {
		if (Strings.nullToEmpty(name).equals("")) return null;
		for (HungerGame game : games) {
			if (game.getName().equalsIgnoreCase(name)) {
				return game;
			}

		}
		return null;
	}
	
	

	@Override
	public WeakReference<HungerGame> getSession(Player player) {
		if (stats.get(player.getName()) != null) {
			for (EquatableWeakReference<HungerGame> gameGotten : stats.get(player.getName()).keySet()) {
				PlayerStat stat = stats.get(player.getName()).get(gameGotten);
				if (stat != null && stat.getState() != PlayerStat.PlayerState.DEAD && stat.getState() != PlayerStat.PlayerState.NOT_IN_GAME) return gameGotten;
			}
		}
		return null; 
	}

	@Override
	public HungerGame getRawSession(Player player) {
		WeakReference<HungerGame> session = getSession(player);
		return session == null ? null : session.get();
	}

	@Override
	public WeakReference<HungerGame> getPlayingSession(Player player) {
		if (stats.get(player.getName()) != null) {
			for (EquatableWeakReference<HungerGame> gameGotten : stats.get(player.getName()).keySet()) {
				PlayerStat stat = stats.get(player.getName()).get(gameGotten);
				if (stat != null && (stat.getState() == PlayerStat.PlayerState.PLAYING || stat.getState() == PlayerStat.PlayerState.WAITING)) return gameGotten;
			}
		}
		return null;
	}

	@Override
	public HungerGame getRawPlayingSession(Player player) {
		WeakReference<HungerGame> session = getPlayingSession(player);
		return session == null ? null : session.get();
	}

	@Override
	public boolean doesNameExist(String name) {
		return getRawGame(name) != null;
	}

	public void playerLeftServer(Player player) {
		if (spectators.containsKey(player.getName())) {
			WeakReference<HungerGame> spectated = spectators.remove(player.getName());
			if (spectated.get() == null || spectated == null) return;
			spectated.get().removeSpectator(player);
			return;
		}
		WeakReference<HungerGame> game = getSession(player);
		if (game == null || game.get() == null) return;
		game.get().leave(player, true);
	}

	public void loadGames() {
		ConfigurationNode gamesSection = Files.GAMES.getConfig().getNode("games");
		if (gamesSection == null) {
			return;
		}
		List<String> checked = new ArrayList<String>();
		for (Iterator<HungerGame> it = games.iterator(); it.hasNext();) {
			HungerGame game = it.next();
			checked.add(game.getName());
			if (gamesSection.getNode(game.getName()).getValue() != null) {
				game.loadFrom(gamesSection.getNode(game.getName()));
			}
			else {
				game.delete();
				it.remove();
			}
		}
		for (String name : gamesSection.getKeys(false)) {
			if (checked.contains(name)) continue;
			HungerGame game = new HungerGame(name);
			game.loadFrom(gamesSection.getNode(name));
			games.add(game);
		}
	}

	public void saveGames() {
		for (HungerGame game : games) {
		    saveGame(game);
		}
	}
	
	public void reloadGame(HungerGame game){
		ConfigurationNode gameSection = Files.GAMES.getConfig().getNode("games." + game.getName());
		if (gameSection.getValue() == null) {
			return;
		}
		game.loadFrom(gameSection);
		games.add(game);
	}

	public void saveGame(HungerGame game){
		ConfigurationNode section = Files.GAMES.getConfig().getNode("games");
		if(section.getValue() == null){
		    section = Files.GAMES.getConfig().addChild(section);
		}
		ConfigurationNode saveSection = section.addNode(game.getName());
		game.saveTo(saveSection);
		Files.GAMES.save();
	}
	
	@Override
	public boolean addSponsor(Player player, Player playerToBeSponsored) {
	    /*WeakReference<HungerGame> game = getPlayingSession(playerToBeSponsored);
	    if (game == null || game.get() == null) {
		    ChatUtils.error(player, player.getName() + " is not playing in a game.");
		    return false;
	    }
	    ConversationFactory convo = new ConversationFactory(plugin);
	    convo.withFirstPrompt(new SponsorBeginPrompt(game, player, playerToBeSponsored));
	    convo.withEscapeSequence("quit");
	    convo.withTimeout(120);
	    convo.thatExcludesNonPlayersWithMessage("Players only!");
	    convo.buildConversation(player).begin();
	    game.get().addSponsor(player.getName(), playerToBeSponsored.getName());*/
	    return true;
	}

	@Override
	public boolean addSpectator(Player player, Game game, Player spectated) {
		if (spectators.containsKey(player.getName())) return false;
		if (!((HungerGame) game).addSpectator(player, spectated)) return false;
		spectators.put(player.getName(), new EquatableWeakReference<HungerGame>((HungerGame) game));
		return true;
	}
	
	@Override
	public EquatableWeakReference<HungerGame> getSpectating(Player player) {
	    if (player == null) return null;    
	    if (!spectators.containsKey(player.getName())) return null;
	    return spectators.get(player.getName());
	}
	
	@Override
	public boolean removeSpectator(Player player) {
		WeakReference<HungerGame> game = spectators.remove(player.getName());
		if (game != null && game.get() != null) {
			game.get().removeSpectator(player);
			return true;
		}
		return false;
	}
	
	@Override
	public void freezePlayer(Player player) {
		frozenPlayers.put(player.getName(), player.getTransform().getPosition());
	}

	@Override
	public void unfreezePlayer(Player player) {
		frozenPlayers.remove(player.getName());
	}

	@Override
	public boolean isPlayerFrozen(Player player) {
		return frozenPlayers.containsKey(player.getName());
	}
	
	@Override
	public Point getFrozenPoint(Player player) {
		if (!frozenPlayers.containsKey(player.getName())) {
			return null;
		}
		return frozenPlayers.get(player.getName());
	}
	
	@Override
	public boolean isPlayerSubscribed(Player player, Game game) {
		if (HungerGames.hasPermission(player, Defaults.Perm.USER_AUTO_SUBSCRIBE)) return true;
		if (game != null){
			if (subscribedPlayers.get(new EquatableWeakReference<HungerGame>((HungerGame) game)) == null) {
				subscribedPlayers.put(new EquatableWeakReference<HungerGame>(((HungerGame) game)), new HashSet<String>());
			}
			if (subscribedPlayers.get(new EquatableWeakReference<HungerGame>((HungerGame) game)).contains(player.getName())) return true;
		}
		return globalSubscribedPlayers.contains(player.getName());
	}
	
	@Override
	public void removedSubscribedPlayer(Player player, Game game) {
		if (game != null) {
			if (subscribedPlayers.get(new EquatableWeakReference<HungerGame>((HungerGame) game)) == null) {
				subscribedPlayers.put(new EquatableWeakReference<HungerGame>(((HungerGame) game)), new HashSet<String>());
			}
			subscribedPlayers.get(new EquatableWeakReference<HungerGame>((HungerGame) game)).remove(player.getName());
		}
		else {
			globalSubscribedPlayers.remove(player.getName());
		}
	}
	
	@Override
	public void addSubscribedPlayer(Player player, Game game) {
		if (game != null) {
			if (subscribedPlayers.get(new EquatableWeakReference<HungerGame>((HungerGame) game)) == null) {
				subscribedPlayers.put(new EquatableWeakReference<HungerGame>(((HungerGame) game)), new HashSet<String>());
			}
			subscribedPlayers.get(new EquatableWeakReference<HungerGame>((HungerGame) game)).add(player.getName());
		}
		else {
			globalSubscribedPlayers.add(player.getName());
		}
	}
	
	public Set<String> getSubscribedPlayers(HungerGame game) {
		Set<String> set = new HashSet<String>();
		if (game != null) {
			set.addAll(subscribedPlayers.get(new EquatableWeakReference<HungerGame>(game)));
		} else {
			set.addAll(globalSubscribedPlayers);
		}
		return set;
	}
	
	public void addBackPoint(Player player) {
		playerBackPoints.put(player.getName(), player.getTransform().getPosition());
	}
	
	public Point getAndRemoveBackPoint(Player player) {
		return playerBackPoints.remove(player.getName());
	}
	/*
	private static class SponsorBeginPrompt extends NumericPrompt {
		WeakReference<HungerGame> game;
		Player player;
		Player beingSponsored;
		Map<ItemStack, Double> itemMap = null;
		
		public SponsorBeginPrompt(WeakReference<HungerGame> game, Player player, Player playerToBeSponsored) {
			this.game = game;
			this.player = player;
			this.beingSponsored = playerToBeSponsored;
		}
		
		public String getPromptText(ConversationContext cc) {
			if (game.get() == null) {
				cc.setSessionData("cancelled", true);
				return "This game no longer exists. Reply to exit.";
			}
			List<String> itemsets = game.get().getItemSets();
			if (ItemConfig.getGlobalSponsorLoot().isEmpty() && (itemsets == null || itemsets.isEmpty())) {
				cc.setSessionData("cancelled", true);
				return "No items are available to sponsor. Reply to exit.";
			}
			if (!HungerGames.isEconomyEnabled()) {
				cc.setSessionData("cancelled", true);
				return "Economy is disabled. Reply to exit.";
			}
			cc.getForWhom().sendRawMessage("Available items to be sponsored:");
			int num = 1;
			itemMap = ItemConfig.getAllSponsorLootWithGlobal(itemsets);
			cc.setSessionData("items", itemMap);
			for (ItemStack item : itemMap.keySet()) {
				String mess = String.format(">> %d - %s: %d", num, item.getType().name(), item.getAmount());
				Set<Enchantment> enchants = item.getEnchantments().keySet();
				for (Enchantment enchant : enchants) {
					mess += ", ";
					mess += String.format("%s: %d", enchant.getName(), item.getEnchantmentLevel(enchant));
				}
				cc.getForWhom().sendRawMessage(ChatStyle.GOLD + mess);
				num++;
			}
			return "Select an item by typing the number next to it. Type quit at any time to quit";
		}

		@Override
		protected boolean isInputValid(ConversationContext cc, String string) {
			if (cc.getSessionData(cc) != null && (Boolean) cc.getSessionData(cc) == true) {
				return true;
			}
			return super.isInputValid(cc, string);

		}
		
		@Override
		protected boolean isNumberValid(ConversationContext cc, Number number) {
			if (itemMap == null) return false;
			if (number.intValue() >= itemMap.size()) return false;
			return true;
		}
		
		@Override
		protected String getFailedValidationText(ConversationContext context, String invalidInput) {
			return "That is not a valid choice.";
		}

		@Override
		protected String getInputNotNumericText(ConversationContext context, String invalidInput) {
			return "That is not a valid number.";
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext cc, Number number) {
			if (cc.getSessionData("cancelled") != null && (Boolean) cc.getSessionData("cancelled") == true) {
				return END_OF_CONVERSATION;
			}
			
			int choice = number.intValue() - 1;
			ItemStack item = new ArrayList<ItemStack>(itemMap.keySet()).get(choice);
			double price = itemMap.get(item);
			
			if (beingSponsored == null) {
				cc.getForWhom().sendRawMessage("Sponsee is not online anymore.");
				return END_OF_CONVERSATION;
			}
			if (!HungerGames.hasEnough(beingSponsored, price)) {
				cc.getForWhom().sendRawMessage("You do not have enough money.");
				return END_OF_CONVERSATION;
			}
			
			HungerGames.withdraw(player, price);
			if (item.getEnchantments().isEmpty()) {
				ChatUtils.send(beingSponsored, "%s has sponsored you %d %s(s).",
				player.getName(), item.getAmount(), item.getType().name());
			} else {
				ChatUtils.send(beingSponsored, "%s has sponsored you %d enchanted %s(s).",
				player.getName(), item.getAmount(), item.getType().name());
			}

			for (ItemStack drop : beingSponsored.getInventory().addItem(item).values()) {
				beingSponsored.getWorld().dropItem(beingSponsored.getPoint(),drop);
			}

			if (item.getEnchantments().isEmpty()) {
				ChatUtils.send(beingSponsored, "You have sponsored %s %d %s(s) for $%.2f.",
					player.getName(), item.getAmount(), item.getType().name(), price);
			} else {
				ChatUtils.send(beingSponsored, "You have sponsored %s %d enchanted %s(s) for $%.2f.",
					player.getName(), item.getAmount(), item.getType().name(), price);
			}
			return END_OF_CONVERSATION;
		}
	}*/
}
