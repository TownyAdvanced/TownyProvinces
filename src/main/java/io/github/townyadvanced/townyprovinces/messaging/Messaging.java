package io.github.townyadvanced.townyprovinces.messaging;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.util.Colors;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class Messaging {

    final static String prefix = Translatable.of("townyprovinces_plugin_prefix").translate(Locale.ROOT);
	
    public static void sendErrorMsg(CommandSender sender, String message) {
        //Ensure the sender is not null (i.e. is an online player who is not an npc)
        if(sender != null)
            sender.sendMessage(prefix + Colors.Red + message);
    }

    public static void sendMsg(CommandSender sender, String message) {
        //Ensure the sender is not null (i.e. is an online player who is not an npc)
        if(sender != null)
            sender.sendMessage(prefix + Colors.White + message);
    }
    
    public static void sendGlobalMessage(String message) {
        TownyProvinces.info(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null && TownyAPI.getInstance().isTownyWorld(player.getWorld()))
                sendMsg(player, message);
        }
    }

	public static void sendErrorMsg(CommandSender sender, Translatable message) {
		// Ensure the sender is not null (i.e. is an online player who is not an npc)
		if (sender != null)
			sender.sendMessage(prefix + Colors.Red + message.forLocale(sender));
	}

	public static void sendMsg(CommandSender sender, Translatable message) {
		// Ensure the sender is not null (i.e. is an online player who is not an npc)
		if (sender != null)
			sender.sendMessage(prefix + Colors.White + message.forLocale(sender));
	}

	public static void sendGlobalMessage(Translatable message) {
		TownyProvinces.info(message.defaultLocale());
		Bukkit.getOnlinePlayers().stream()
			.filter(p -> p != null)
			.filter(p -> TownyAPI.getInstance().isTownyWorld(p.getLocation().getWorld()))
			.forEach(p -> sendMsg(p, message));
	}

}
