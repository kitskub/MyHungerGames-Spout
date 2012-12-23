package me.kitskub.myhungergames.stats;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;

import me.kitskub.myhungergames.Defaults;
import me.kitskub.myhungergames.Logging;
import me.kitskub.myhungergames.games.HungerGame;
import me.kitskub.myhungergames.utils.ConnectionUtils;
import me.kitskub.myhungergames.stats.PlayerStat.PlayerState;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
* Players
*   name
*   lastLogin
*   totalGames
*   wins
*   kills
*   deaths
* Kills
*   killer
*   kill
*   day
*/
public class StatHandler {
	/**
	 * {"requestType"; "updateGames"}
	 * {"startTime"; game's initial start time divided by 1000 (it was in milliseconds, we want seconds)}
	 * {"totalPlayers"; number of players in game}
	 * {"winner"; name of winner or "N/A" if none}
	 * {"players"; "{player1}{player2}{etc.}"}
	 * {"totalDuration"; sum of all times divided by 1000}
	 * {"sponsors"; "{sponsor1:{sponsee1}{sponsee2}}{sponsor2:{sponsee1}{etc.}}{etc.}"}
	 */
	public static void updateGame(HungerGame game) {
		String urlString = Defaults.Config.WEBSTATS_IP.getGlobalString();
		if ("0.0.0.0".equals(urlString)) return;
		Map<String, String> map = new HashMap<String, String>();
		map.put("requestType", "updateGames");
		map.put("startTime", new Date(game.getInitialStartTime()).toString());

		map.put("totalPlayers", String.valueOf(game.getAllPlayers().size()));
		if (game.getRemainingPlayers().size() != 1) {
			map.put("winner", "N/A");

		}
		else {
			map.put("winner", game.getRemainingPlayers().get(0).getName());
		}
		StringBuilder playersSB = new StringBuilder();
		for (String s : game.getAllPlayers()) {
			playersSB.append("{").append(s).append("}");
		}
		map.put("players", playersSB.toString());
		long totalDuration = 0;
		for (int i = 0; i < Math.min(game.getEndTimes().size(), game.getStartTimes().size()); i++) {
			totalDuration += game.getEndTimes().get(i) - game.getStartTimes().get(i);
		}
		map.put("totalDuration", new Time(totalDuration).toString());
		StringBuilder sponsorsSB = new StringBuilder();
		for (String s : game.getSponsors().keySet()) {
			sponsorsSB.append("{").append(s).append(":");
			for (String sponsee : game.getSponsors().get(s)) sponsorsSB.append("{").append(sponsee).append("}");
			sponsorsSB.append("}");
		}
		map.put("sponsors", sponsorsSB.toString());
		
		try {
			ConnectionUtils.post(urlString, map);
		} catch (ParserConfigurationException ex) {
			Logging.debug("Error when updating games: " + ex.getMessage());
		} catch (SAXException ex) {
		} catch (IOException ex) {
			Logging.debug("Error when updating games: " + ex.getMessage());
		}
	}

	/**
	 * {"requestType"; "updatePlayers"}
	 * {"playerName"; player name}
	 * {"totalTime"; total time (in hh:mm:ss)}
	 * {"wins"; 0 if lost, 1 if won}
	 * {"kills"; number of kills}
	 * {"deaths"; number of deaths}
	 */
	public static void updateStat(PlayerStat stat) {
		String urlString = Defaults.Config.WEBSTATS_IP.getGlobalString();
		if ("0.0.0.0".equals(urlString)) return;
		Map<String, String> map = new HashMap<String, String>();
		map.put("requestType", "updatePlayers");
		map.put("playerName", stat.getPlayer().getName());
		map.put("totalTime", new Time(stat.getTime()).toString());
		String wins;
		if (stat.getState() == PlayerState.DEAD) {
			wins = "0";
		}
		else {
			wins = "1";
		}
		map.put("wins", wins);
		map.put("kills", String.valueOf(stat.getNumKills()));
		map.put("deaths", String.valueOf(stat.getNumDeaths()));
		try {
			ConnectionUtils.post(urlString, map);
		} catch (ParserConfigurationException ex) {
			Logging.debug("Error when updating stat: " + ex.getMessage());
		} catch (SAXException ex) {
		} catch (IOException ex) {
			Logging.debug("Error when updating stat: " + ex.getMessage());
		}
	}

	public static SQLStat getStat(String s) {
		String urlString = Defaults.Config.WEBSTATS_IP.getGlobalString();
		if ("0.0.0.0".equals(urlString)) return null;
		Map<String, String> map = new HashMap<String, String>();
		map.put("requestType", "requestPlayer");
		map.put("playerName", s);
		Document doc;
		try {
			doc = ConnectionUtils.postWithRequest(urlString, map);
		} catch (ParserConfigurationException ex) {
			Logging.debug("Error when getting stat: " + ex.getMessage());
			return null;
		} catch (SAXException ex) {
			Logging.debug("Error when getting stat: " + ex.getMessage());
			return null;
		} catch (IOException ex) {
			Logging.debug("Error when getting stat: " + ex.getMessage());
			return null;
		}
		SQLStat stat = null;
		Element rootEle = doc.getDocumentElement();
		NodeList globalElems = rootEle.getElementsByTagName("global");
		if (globalElems.getLength() > 0) {
			stat = new SQLStat();
			Node node = globalElems.item(0);
			stat.rank = getIntValue(node, "rank");
			stat.deaths = getIntValue(node, "deaths");
			stat.kills = getIntValue(node, "kills");
			stat.lastLogin = getDateValue(node, "lastLogin");
			stat.totalGames = getIntValue(node, "totalGames");
			stat.totalTime = getTimeValue(node, "totalTime");
			stat.wins = getIntValue(node, "wins");
		}
		if (stat == null) return null;
		NodeList gamesElems = rootEle.getElementsByTagName("game");
		for (int i = 0; i < gamesElems.getLength(); i++) {
			Node node = gamesElems.item(i);
			SQLStat.SQLGameStat gameStat = stat.new SQLGameStat();
			gameStat.startTime = getDateValue(node, "startTime");
			gameStat.totalDuration = getTimeValue(node, "totalDuration");
			gameStat.totalPlayers = getIntValue(node, "totalPlayers");
			gameStat.winner = getTextValue(node, "winner");
			NodeList playersElems = ((Element) node).getElementsByTagName("player");
			for (int j = 0; j < playersElems.getLength(); j++) {
				gameStat.players.add(playersElems.item(j).getNodeValue());
			}
			NodeList sponsorsElems = ((Element) node).getElementsByTagName("sponsor");
			for (int j = 0; j < playersElems.getLength(); j++) {
				gameStat.players.add(sponsorsElems.item(j).getNodeValue());
			}
		}
		return stat;
	}
	
	private static String getTextValue(Node node, String tagName) {
		String textVal = null;
		Element ele = (Element) node;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}
	
	private static Integer getIntValue(Node node, String tagName) {
		String textVal = null;
		Element ele = (Element) node;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		try {
			return Integer.getInteger(textVal);
		} catch (NumberFormatException numberFormatException) {
			return null;
		}
	}
	
	private static Date getDateValue(Node node, String tagName) {
		String textVal = null;
		Element ele = (Element) node;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}


		try {
			return Date.valueOf(textVal);
		} catch (Exception exception) {
			return null;
		}
	}
	
	private static Time getTimeValue(Node node, String tagName) {
		String textVal = null;
		Element ele = (Element) node;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		try {
			return Time.valueOf(textVal);
		} catch (Exception exception) {
			return null;
		}
	}
}
