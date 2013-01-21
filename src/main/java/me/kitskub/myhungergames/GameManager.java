package me.kitskub.myhungergames;

import com.google.common.base.Strings;
import me.kitskub.myhungergames.api.Game;

import me.kitskub.myhungergames.games.HungerGame;
import me.kitskub.myhungergames.stats.PlayerStat;
import me.kitskub.myhungergames.utils.EquatableWeakReference;

import java.lang.ref.WeakReference;
import java.util.*;
import me.kitskub.myhungergames.utils.ChatUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.spout.api.chat.ChatArguments;
import org.spout.api.chat.conversation.Conversation;
import org.spout.api.chat.conversation.ResponseHandler;
import org.spout.api.chat.style.ChatStyle;

import org.spout.api.entity.Player;
import org.spout.api.geo.discrete.Point;
import org.spout.api.inventory.ItemStack;
import org.spout.api.util.config.ConfigurationNode;
import org.spout.vanilla.plugin.component.inventory.PlayerInventory;
import org.spout.vanilla.plugin.component.substance.Item;
import org.spout.vanilla.plugin.material.enchantment.*;


public class GameManager extends me.kitskub.myhungergames.api.GameManager {
	private static final HungerGames plugin = HungerGames.getInstance();
	public static final GameManager INSTANCE = new GameManager();
	private static Map<Player, Map<EquatableWeakReference<HungerGame>, PlayerStat>> stats = new HashMap<Player, Map<EquatableWeakReference<HungerGame>, PlayerStat>>();
	private static final Set<HungerGame> games = new TreeSet<HungerGame>();
	private static final Map<Player, EquatableWeakReference<HungerGame>> spectators = new HashMap<Player, EquatableWeakReference<HungerGame>>(); // <player, game>
	private static final Map<Player, Point> frozenPlayers = new HashMap<Player, Point>();
	private static final Set<Player> globalSubscribedPlayers = new HashSet<Player>();
	private static final Map<EquatableWeakReference<HungerGame>, Set<Player>> subscribedPlayers = new HashMap<EquatableWeakReference<HungerGame>, Set<Player>>();
	private static final Map<Player, Point> playerBackPoints = new HashMap<Player, Point>();
	
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
		if (stats.get(player) == null) stats.put(player, new HashMap<EquatableWeakReference<HungerGame>, PlayerStat>());
		stats.get(player).put(new EquatableWeakReference<HungerGame>(game), stat);
		return stat;
	}
	
	public void clearGamesForPlayer(Player player, HungerGame game) {
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
		if (stats.get(player) != null) {
			for (EquatableWeakReference<HungerGame> gameGotten : stats.get(player).keySet()) {
				PlayerStat stat = stats.get(player).get(gameGotten);
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
		if (stats.get(player) != null) {
			for (EquatableWeakReference<HungerGame> gameGotten : stats.get(player).keySet()) {
				PlayerStat stat = stats.get(player).get(gameGotten);
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
		if (spectators.containsKey(player)) {
			WeakReference<HungerGame> spectated = spectators.remove(player);
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
	public boolean addSponsor(final Player player, Player playerToBeSponsored) {
		WeakReference<HungerGame> game = getPlayingSession(playerToBeSponsored);
		if (game == null || game.get() == null) {
			ChatUtils.error(player, player.getName() + " is not playing in a game.");
			return false;
		}
		Conversation convo = new Conversation("Sponsorship", player).setResponseHandler(new SponsorBeginPrompt(game, player, playerToBeSponsored));
		game.get().addSponsor(player, playerToBeSponsored);
		player.setActiveChannel(convo);
		return true;
	}

	@Override
	public boolean addSpectator(Player player, Game game, Player spectated) {
		if (spectators.containsKey(player)) return false;
		if (!((HungerGame) game).addSpectator(player, spectated)) return false;
		spectators.put(player, new EquatableWeakReference<HungerGame>((HungerGame) game));
		return true;
	}
	
	@Override
	public EquatableWeakReference<HungerGame> getSpectating(Player player) {
	    if (player == null) return null;    
	    if (!spectators.containsKey(player)) return null;
	    return spectators.get(player);
	}
	
	@Override
	public boolean removeSpectator(Player player) {
		WeakReference<HungerGame> game = spectators.remove(player);
		if (game != null && game.get() != null) {
			game.get().removeSpectator(player);
			return true;
		}
		return false;
	}
	
	@Override
	public void freezePlayer(Player player) {
		frozenPlayers.put(player, player.getTransform().getPosition());
	}

	@Override
	public void unfreezePlayer(Player player) {
		frozenPlayers.remove(player);
	}

	@Override
	public boolean isPlayerFrozen(Player player) {
		return frozenPlayers.containsKey(player);
	}
	
	@Override
	public Point getFrozenPoint(Player player) {
		if (!frozenPlayers.containsKey(player)) {
			return null;
		}
		return frozenPlayers.get(player);
	}
	
	@Override
	public boolean isPlayerSubscribed(Player player, Game game) {
		if (HungerGames.hasPermission(player, Defaults.Perm.USER_AUTO_SUBSCRIBE)) return true;
		if (game != null){
			if (subscribedPlayers.get(new EquatableWeakReference<HungerGame>((HungerGame) game)) == null) {
				subscribedPlayers.put(new EquatableWeakReference<HungerGame>(((HungerGame) game)), new HashSet<Player>());
			}
			if (subscribedPlayers.get(new EquatableWeakReference<HungerGame>((HungerGame) game)).contains(player)) return true;
		}
		return globalSubscribedPlayers.contains(player);
	}
	
	@Override
	public void removedSubscribedPlayer(Player player, Game game) {
		if (game != null) {
			if (subscribedPlayers.get(new EquatableWeakReference<HungerGame>((HungerGame) game)) == null) {
				subscribedPlayers.put(new EquatableWeakReference<HungerGame>(((HungerGame) game)), new HashSet<Player>());
			}
			subscribedPlayers.get(new EquatableWeakReference<HungerGame>((HungerGame) game)).remove(player);
		}
		else {
			globalSubscribedPlayers.remove(player);
		}
	}
	
	@Override
	public void addSubscribedPlayer(Player player, Game game) {
		if (game != null) {
			if (subscribedPlayers.get(new EquatableWeakReference<HungerGame>((HungerGame) game)) == null) {
				subscribedPlayers.put(new EquatableWeakReference<HungerGame>(((HungerGame) game)), new HashSet<Player>());
			}
			subscribedPlayers.get(new EquatableWeakReference<HungerGame>((HungerGame) game)).add(player);
		}
		else {
			globalSubscribedPlayers.add(player);
		}
	}
	
	public Set<Player> getSubscribedPlayers(HungerGame game) {
		Set<Player> set = new HashSet<Player>();
		if (game != null) {
			set.addAll(subscribedPlayers.get(new EquatableWeakReference<HungerGame>(game)));
		} else {
			set.addAll(globalSubscribedPlayers);
		}
		return set;
	}
	
	public void addBackPoint(Player player) {
		playerBackPoints.put(player, player.getTransform().getPosition());
	}
	
	public Point getAndRemoveBackPoint(Player player) {
		return playerBackPoints.remove(player);
	}

	private static class SponsorBeginPrompt extends ResponseHandler {
		private final String ITEMS = "items";
		WeakReference<HungerGame> game;
		Player player;
		Player beingSponsored;
		Map<ItemStack, Double> itemMap = null;
		
		private void finish() {
			getConversation().finish();
		}

		public SponsorBeginPrompt(WeakReference<HungerGame> game, Player player, Player playerToBeSponsored) {
			this.game = game;
			this.player = player;
			this.beingSponsored = playerToBeSponsored;
		}
		
		public String getPromptText() {
			if (game.get() == null) {
				finish();
				return "This game no longer exists. Reply to exit.";
			}
			List<String> itemsets = game.get().getItemSets();
			if (ItemConfig.getGlobalSponsorLoot().isEmpty() && (itemsets == null || itemsets.isEmpty())) {
				finish();
				return "No items are available to sponsor. Reply to exit.";
			}
			if (!HungerGames.isEconomyEnabled()) {
				finish();
				return "Economy is disabled. Reply to exit.";
			}
			getConversation().getParticipant().sendRawMessage("Available items to be sponsored:");
			int num = 1;
			itemMap = ItemConfig.getAllSponsorLootWithGlobal(itemsets);
			getConversation().getContext().put(ITEMS, itemMap);
			for (ItemStack item : itemMap.keySet()) {
				String mess = String.format(">> %d - %s: %d", num, item.getMaterial().getDisplayName(), item.getAmount());
				Map<Enchantment, Integer> enchants = Enchantment.getEnchantments(item);
				for (Enchantment enchant : enchants.keySet()) {
					mess += ", ";
					mess += String.format("%s: %d", enchant.getName(), enchants.get(enchant));
				}
				getConversation().getParticipant().sendRawMessage(ChatStyle.GOLD, mess);
				num++;
			}
			return "Select an item by typing the number next to it. Type /endconvo at any time to quit";
		}

		@Override
		public void onAttached() {
			getConversation().broadcastToReceivers(new ChatArguments(getPromptText()));
		}
		
		private boolean isInputValid(ChatArguments message) {
			return NumberUtils.isNumber(message.asString()) && isNumberValid(NumberUtils.createNumber(message.asString()));

		}
		
		private boolean isNumberValid(Number number) {
			if (itemMap == null) return false;
			if (number.intValue() >= itemMap.size()) return false;
			return true;
		}
		
		private String getFailedValidationText() {
			return "That is not a valid choice.";
		}

		private void acceptValidatedInput(Number number) {
			int choice = number.intValue() - 1;
			ItemStack item = new ArrayList<ItemStack>(itemMap.keySet()).get(choice);
			double price = itemMap.get(item);
			
			if (beingSponsored == null) {
				getConversation().broadcastToReceivers(new ChatArguments("Sponsee is not online anymore."));
				finish();
				return ;
			}
			if (!HungerGames.hasEnough(beingSponsored, price)) {
				getConversation().broadcastToReceivers(new ChatArguments("You do not have enough money."));
				finish();
				return ;
			}
			
			HungerGames.withdraw(player, price);
			if (Enchantment.getEnchantments(item).isEmpty()) {
				ChatUtils.send(beingSponsored, "%s has sponsored you %d %s(s).",
				player.getName(), item.getAmount(), item.getMaterial().getDisplayName());
			} else {
				ChatUtils.send(beingSponsored, "%s has sponsored you %d enchanted %s(s).",
				player.getName(), item.getAmount(), item.getMaterial().getDisplayName());
			}

			if (!beingSponsored.get(PlayerInventory.class).add(item)) {
				Item.dropNaturally(beingSponsored.getTransform().getPosition(), item);
			}

			if (Enchantment.getEnchantments(item).isEmpty()) {
				ChatUtils.send(beingSponsored, "You have sponsored %s %d %s(s) for $%.2f.",
				player.getName(), item.getAmount(), item.getMaterial().getDisplayName(), price);
			} else {
				ChatUtils.send(beingSponsored, "You have sponsored %s %d enchanted %s(s) for $%.2f.",
				player.getName(), item.getAmount(), item.getMaterial().getDisplayName(), price);
			}
		}

		@Override
		public void onInput(ChatArguments message) {
			if (!isInputValid(null)) {
				getConversation().broadcastToReceivers(new ChatArguments(getFailedValidationText()));
				ChatUtils.error(player, "That is not a valid response.");
				getConversation().setResponseHandler(this);
			}
			acceptValidatedInput(NumberUtils.createNumber(message.asString()));
			finish();
		}
	}
}
