package me.kitskub.myhungergames.games;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import me.kitskub.myhungergames.Defaults.Config;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.ItemConfig;
import me.kitskub.myhungergames.utils.ChatUtils;
import org.spout.api.Spout;
import org.spout.api.chat.ChatArguments;
import org.spout.api.chat.conversation.Conversation;
import org.spout.api.chat.conversation.ResponseHandler;
import org.spout.api.chat.style.ChatStyle;
import org.spout.api.entity.Player;
import org.spout.api.inventory.ItemStack;
import org.spout.api.scheduler.Task;
import org.spout.vanilla.component.inventory.PlayerInventory;
import org.spout.vanilla.component.substance.Item;
import org.spout.vanilla.material.enchantment.Enchantment;

public class SpectatorSponsoringRunnable implements Runnable{
	public static final int pollEveryInTicks = 20 * 30;
	private final HungerGame game;
	private static final Map<Player, Integer> spectatorTimes = new HashMap<Player, Integer>(); // <player, timeLeftInTicks>
	private Task task;
	private static final String AMOUNT = "amount";
	private static final String ITEM = "item";
	private static final String NAME = "name";
	
	
	public SpectatorSponsoringRunnable(HungerGame game) {
		this.game = game;
	}

	public void run() {
		for (Player player : spectatorTimes.keySet()) {
			int time = spectatorTimes.get(player) - pollEveryInTicks;
			if (time <= 0) {
				spectatorTimes.put(player, Config.SPECTATOR_SPONSOR_PERIOD.getInt(game.getSetup()) * 20);
				ChatUtils.send(player, ChatStyle.GOLD, "You can now sponsor a player with an item.");
				Conversation convo = new Conversation("Sponsorship", player).setResponseHandler(new SpectatorSponsorBeginPrompt(player));
				Set<ItemStack> items = ItemConfig.getAllSponsorLootWithGlobal(game.getItemSets()).keySet();
				ItemStack item = (ItemStack) (items.toArray()[HungerGames.RANDOM.nextInt(items.size())]);
				convo.getContext().put(ITEM, item);
				convo.getContext().put(AMOUNT, item.getAmount());
				convo.getContext().put(NAME, item.getMaterial().getDisplayName());
				player.setActiveChannel(convo);
			}
		}
	}
	
	public void setTask(Task task) {
		this.task = task;
	}
	
	public void cancel() {
		task.cancel();
		task = null;
		spectatorTimes.clear();
	}
	public void addSpectator(Player player) {
		spectatorTimes.put(player, Config.SPECTATOR_SPONSOR_PERIOD.getInt(game.getSetup()) * 20);
	}
	
	public void removeSpectator(Player player) {
		spectatorTimes.remove(player);
	}
	private static class SpectatorSponsorBeginPrompt extends ResponseHandler {
		Player sponsor;

		public SpectatorSponsorBeginPrompt(Player sponsor) {
			this.sponsor = sponsor;
		}

		public String getPromptText() {
			StringBuilder builder = new StringBuilder("The item is a stack of ");
			builder.append((Integer) getConversation().getContext().get(AMOUNT));
			builder.append(" ");
			builder.append((String) getConversation().getContext().get(NAME));
			builder.append(".");
			getConversation().broadcastToReceivers(new ChatArguments(ChatStyle.GOLD, builder.toString()));
			return "Please type the player you want to sponsor's name or quit.";
		}

		@Override
		public void onAttached() {
			getConversation().broadcastToReceivers(new ChatArguments(getPromptText()));
		}

		
		protected boolean isInputValid(ChatArguments args) {
			if (Spout.getEngine().getPlayer(args.asString(), false) != null) {
				return true;
			}
			return false;
		}

		protected String getFailedValidationText() {
			return "That player name is not valid. Please try again.";
		}

		protected void acceptValidatedInput(String args) {
			Player sponsee = Spout.getEngine().getPlayer(args, false);
			ItemStack item = (ItemStack) getConversation().getContext().get(ITEM);
			if (Enchantment.getEnchantments(item).isEmpty()) {
				ChatUtils.send(sponsee, "%s has sponsored you %d %s(s).",
				sponsor.getName(), item.getAmount(), item.getMaterial().getDisplayName());
			} else {
				ChatUtils.send(sponsee, "%s has sponsored you %d enchanted %s(s).",
				sponsor.getName(), item.getAmount(), item.getMaterial().getDisplayName());
			}

			if (!sponsee.get(PlayerInventory.class).add(item)) {
				Item.dropNaturally(sponsee.getTransform().getPosition(), item);
			}

			if (Enchantment.getEnchantments(item).isEmpty()) {
				ChatUtils.send(sponsee, "You have sponsored %s %d %s(s).",
				sponsor.getName(), item.getAmount(), item.getMaterial().getDisplayName());
			} else {
				ChatUtils.send(sponsee, "You have sponsored %s %d enchanted %s(s).",
				sponsor.getName(), item.getAmount(), item.getMaterial().getDisplayName());
			}
		}

		@Override
		public void onInput(ChatArguments message) {
			if (!isInputValid(message)) {
				getConversation().broadcastToReceivers(new ChatArguments(getFailedValidationText()));
			}
			acceptValidatedInput(message.asString());
			
		}
		
		
	}
}
