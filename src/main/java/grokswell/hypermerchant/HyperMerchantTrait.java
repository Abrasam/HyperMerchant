package grokswell.hypermerchant;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.speech.SimpleSpeechController;

import regalowl.hyperconomy.HyperAPI;
import regalowl.hyperconomy.HyperObjectAPI;
import regalowl.hyperconomy.HyperPlayer;


public class HyperMerchantTrait extends Trait {
	HyperAPI hyperAPI = new HyperAPI();
	HyperObjectAPI hoAPI = new HyperObjectAPI();
	static ArrayList<String> shoplist;
	HashMap<String,ShopMenu> customer_menus = new HashMap<String,ShopMenu>();
	final HyperMerchantPlugin plugin;
	
	public DataKey trait_key;
	
	String farewellMsg;
	String welcomeMsg;
	String denialMsg;
	String closedMsg;
	double comission;
	boolean offduty;
	String shop_name;

	public HyperMerchantTrait() {
		super("hypermerchant");
		plugin = (HyperMerchantPlugin) Bukkit.getServer().getPluginManager().getPlugin("HyperMerchant");

	    
		farewellMsg = plugin.settings.FAREWELL;
		welcomeMsg = plugin.settings.WELCOME;
		denialMsg = plugin.settings.DENIAL;
		closedMsg = plugin.settings.CLOSED;
		comission = plugin.settings.NPC_COMMISSION;
		offduty = plugin.settings.OFFDUTY;
		shop_name = hyperAPI.getGlobalShopAccount();
	}

	@Override
	public void load(DataKey key) {
		this.trait_key = key;
		this.shop_name = key.getString("shop_name");

		// Override defaults if they exist

		if (key.keyExists("welcome.default"))
			this.welcomeMsg = key.getString("welcome.default");
		if (key.keyExists("farewell.default"))
			this.farewellMsg = key.getString("farewell.default");
		if (key.keyExists("denial.default"))
			this.denialMsg = key.getString("denial.default");
		if (key.keyExists("closed.default"))
			this.closedMsg = key.getString("closed.default");
		if (key.keyExists("comission.default"))
			this.comission = key.getDouble("comission.default");
		if (key.keyExists("offduty.default"))
			this.offduty = key.getBoolean("offduty.default");

	}
	
    class RemoveCustomerCooldown extends BukkitRunnable {
    	String playername;
        public RemoveCustomerCooldown(String plynam) {
        	playername = plynam;
        }
        public void run() {
            // What you want to schedule goes here
            plugin.customer_cooldowns.remove(playername);
        }
    }
    
	@EventHandler
	public void onRightClick(net.citizensnpcs.api.event.NPCRightClickEvent event) {
		if(this.npc!=event.getNPC()) return;
		
		Player player = event.getClicker();
		
		if (plugin.customer_cooldowns.contains(player.getName())){
			event.setCancelled(true);
			return;
		}
		plugin.customer_cooldowns.add(player.getName());
		new RemoveCustomerCooldown(player.getName()).runTaskLater(this.plugin, 60);
		
		if ((player.getGameMode().compareTo(GameMode.CREATIVE) == 0) && 
		   (!player.hasPermission("creative.hypermerchant"))) {
			
			event.setCancelled(true);
			player.sendMessage(ChatColor.YELLOW+"You may not interact with merchants while in creative mode.");
			return;
    	} 
		
		
		
		if (!player.hasPermission("hypermerchant.npc")) {
			if (!this.denialMsg.isEmpty()) {
				SpeechContext message = new SpeechContext(this.npc, this.denialMsg, player);
				new SimpleSpeechController(this.npc).speak(message);
			}
			return;
		}
		
		HyperPlayer hp = hoAPI.getHyperPlayer(player.getName());
			
		if (!hp.hasBuyPermission(hyperAPI.getShop(this.shop_name))) {
			if (!this.denialMsg.isEmpty()) {
				SpeechContext message = new SpeechContext(this.npc, this.denialMsg, player);
				new SimpleSpeechController(this.npc).speak(message);
			}
			return;
			
		} else if (this.offduty) {
			if (!this.closedMsg.isEmpty()) {
				SpeechContext message = new SpeechContext(this.npc, this.closedMsg, player);
				new SimpleSpeechController(this.npc).speak(message);
			}
			return;
			
		} else {
			shoplist = hyperAPI.getPlayerShopList();
			shoplist.addAll(hyperAPI.getServerShopList());
			if (shoplist.contains(this.shop_name)) {
				if  (!this.welcomeMsg.isEmpty()) {
					SpeechContext message = new SpeechContext(this.npc, this.welcomeMsg, player);
					new SimpleSpeechController(this.npc).speak(message);
				}
				//shopstock.pages is ArrayList<ArrayList<String>> shopstock.items_count is int
				this.customer_menus.put(player.getName(), new ShopMenu(this.shop_name, 54, plugin, player, player, this.npc));
				//ecoMan=null;
				//hc=null;
				return;
				
			} else {
				if  (!this.closedMsg.isEmpty()) {
					SpeechContext message = new SpeechContext(this.npc, this.closedMsg, player);
					new SimpleSpeechController(this.npc).speak(message);
				}
				plugin.getLogger().info("npc #"+this.npc.getId()+" is assigned to a shop named "+
						shop_name+". This shop does not exist.");
				return;
			}
		}
	}
	

	@Override
	public void save(DataKey key) {
		key.setString("shop_name", this.shop_name);
		key.setString("farewell.default", this.farewellMsg);
		key.setString("denial.default", this.denialMsg);
		key.setString("welcome.default", this.welcomeMsg);
		key.setString("closed.default", this.closedMsg);
		key.setDouble("comission.default", this.comission);
		key.setBoolean("offduty.default", this.offduty);
		
	}
	
	@Override
	public void onAttach() {
	}
	
	public void onFarewell(Player player) {
		if (!this.farewellMsg.isEmpty()) {
			SpeechContext message = new SpeechContext(this.npc, this.farewellMsg, player);
			new SimpleSpeechController(this.npc).speak(message);
		}
	}

}