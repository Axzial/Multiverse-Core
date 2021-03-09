/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.Core;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.commandtools.flag.CoreFlags;
import com.onarandombox.MultiverseCore.commandtools.flag.FlagGroup;
import com.onarandombox.MultiverseCore.commandtools.flag.FlagParseFailedException;
import com.onarandombox.MultiverseCore.commandtools.flag.FlagResult;
import com.pneumaticraft.commandhandler.CommandHandler;
import org.bukkit.ChatColor;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creates a new world and loads it.
 */
public class CreateCommand extends MultiverseCommand {

    private static final FlagGroup FLAG_GROUP = FlagGroup.of(
            CoreFlags.SEED,
            CoreFlags.WORLD_TYPE,
            CoreFlags.GENERATOR,
            CoreFlags.GENERATE_STRUCTURES,
            CoreFlags.SPAWN_ADJUST
    );

    private final MVWorldManager worldManager;

    public CreateCommand(MultiverseCore plugin) {
        super(plugin);
        this.setName("Create World");
        this.setCommandUsage(String.format("/mv create %s{NAME} {ENV} %s-s [SEED] -g [GENERATOR[:ID]] -t [WORLDTYPE] [-n] -a [true|false]",
                ChatColor.GREEN, ChatColor.GOLD));
        this.setArgRange(2, 11); // SUPPRESS CHECKSTYLE: MagicNumberCheck
        this.addKey("mvcreate");
        this.addKey("mvc");
        this.addKey("mv create");
        this.setPermission("multiverse.core.create", "Creates a new world and loads it.", PermissionDefault.OP);
        this.addCommandExample("/mv create " + ChatColor.GOLD + "world" + ChatColor.GREEN + " normal");
        this.addCommandExample("/mv create " + ChatColor.GOLD + "lavaland" + ChatColor.RED + " nether");
        this.addCommandExample("/mv create " + ChatColor.GOLD + "starwars" + ChatColor.AQUA + " end");
        this.addCommandExample("/mv create " + ChatColor.GOLD + "flatroom" + ChatColor.GREEN + " normal" + ChatColor.AQUA + " -t flat");
        this.addCommandExample("/mv create " + ChatColor.GOLD + "gargamel" + ChatColor.GREEN + " normal" + ChatColor.DARK_AQUA + " -s gargamel");
        this.addCommandExample("/mv create " + ChatColor.GOLD + "moonworld" + ChatColor.GREEN + " normal" + ChatColor.DARK_AQUA + " -g BukkitFullOfMoon");
        this.worldManager = this.plugin.getMVWorldManager();
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        String worldName = args.get(0);
        String env = args.get(1);

        if (this.worldManager.isMVWorld(worldName)) {
            sender.sendMessage(ChatColor.RED + "Multiverse cannot create " + ChatColor.GOLD + ChatColor.UNDERLINE
                    + "another" + ChatColor.RESET + ChatColor.RED + " world named " + worldName);
            return;
        }

        File worldFile = new File(this.plugin.getServer().getWorldContainer(), worldName);
        if (worldFile.exists()) {
            sender.sendMessage(ChatColor.RED + "A Folder/World already exists with this name!");
            sender.sendMessage(ChatColor.RED + "If you are confident it is a world you can import with /mvimport");
            return;
        }

        Environment environment = EnvironmentCommand.getEnvFromString(env);
        if (environment == null) {
            sender.sendMessage(ChatColor.RED + "That is not a valid environment.");
            EnvironmentCommand.showEnvironments(sender);
            return;
        }

        FlagResult flags;
        try {
            flags = FLAG_GROUP.calculateResult(args.subList(2, args.size()).toArray(new String[0]));
        } catch (FlagParseFailedException e) {
            sender.sendMessage(String.format("%sError: %s", ChatColor.RED, e.getMessage()));
            return;
        }

        Command.broadcastCommandMessage(sender, "Starting creation of world '" + worldName + "'...");

        if (this.worldManager.addWorld(
                worldName,
                environment,
                flags.getValue(CoreFlags.SEED),
                flags.getValue(CoreFlags.WORLD_TYPE),
                flags.getValue(CoreFlags.GENERATE_STRUCTURES),
                flags.getValue(CoreFlags.GENERATOR),
                flags.getValue(CoreFlags.SPAWN_ADJUST))) {

            Command.broadcastCommandMessage(sender, "Complete!");
            return;
        }
        Command.broadcastCommandMessage(sender, "FAILED.");
    }
}
