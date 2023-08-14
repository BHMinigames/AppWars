package dev.fluyd.appwars.commands;

import dev.fluyd.appwars.testflow.TestFlow;
import dev.fluyd.appwars.testflow.flows.TwitterAnimate;
import dev.fluyd.appwars.utils.MessagesUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestCommand implements CommandExecutor, TabCompleter {
    static ArrayList<TestFlow> testFlows = new ArrayList<>();

    static {
        testFlows.add(new TwitterAnimate());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("sumoevent.test")) {
            MessagesUtils.sendNoPermissionError((Player) sender);
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Invalid test flow! The available test flows are: " + ChatColor.YELLOW + testFlows.stream().map(TestFlow::getName).reduce((a, b) -> a + ", " + b).orElse(""));
            return true;
        }

        boolean hasRunTestFlow = testFlows.stream().anyMatch(flow -> {
            if (flow.getName().equalsIgnoreCase(args[0])) {
                if (sender instanceof Player) {
                    flow.run((Player) sender, args);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "You must be a player to run this command!");
                    return true;
                }
            }
            return false;
        });

        if (!hasRunTestFlow)
            sender.sendMessage(ChatColor.RED + "Invalid test flow! The available test flows are: " + ChatColor.YELLOW + testFlows.stream().map(TestFlow::getName).reduce((a, b) -> a + ", " + b).orElse(""));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // If the user has typed the start of a command, filter out the matching ones
            return testFlows.stream()
                    .map(TestFlow::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();  // Return an empty list if no matches
    }
}
