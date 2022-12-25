package com.moose.projecttowny.commands;

import com.mojang.authlib.GameProfile;
import com.moose.projecttowny.Data;
import com.moose.projecttowny.Util;
import net.darkhax.gamestages.GameStageHelper;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.floor;
public class ResearchCommand extends CommandBase {
    private final String helpString =
            "If you left a town, you will need to sync for your new one to have them!\n" +
            "/research sync - Synchronize Municipality research\n" +
            "/research remove <username> - Removes  a researcher\n" +
            "/research add <username> - Add a researcher\n" +
            "/research list - Lists all researchers";

    // For every x citizens in a municipality there is 1 researcher slot
    int researcherRatio = 2;

    @Override
    public String getName() { return "research"; }

    @Override
    public String getUsage(ICommandSender sender) { return "/research"; }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender != null;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length == 0)
            return Arrays.asList("sync", "remove", "add", "list");

        if (args.length == 1) {
            final String arg0 = args[0].toLowerCase();
            return Util.autoComplete(Arrays.asList("sync", "remove", "add", "list"), arg0);
        }

        return new ArrayList<>();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        if (index != 1) return false;
        if (args.length >= 1) {
            final String arg0 = args[0].toLowerCase();
            if (arg0 == "remove" || arg0 == "add")
                return true;
        }

        return false;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        PlayerCapability playerCap = player.getCapability(StatesCapabilities.PLAYER, null);
        ArrayList<String> researchers = Data.getResearchers(playerCap.getMunicipality());

        if (args.length == 0) {
            Print.chat(sender, helpString);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "sync":
                List<String> technologies = Data.getTechnologies(playerCap.getMunicipality());
                Collection<String> stages = GameStageHelper.getPlayerData(player).getStages();

                // Remove mayor and research questlines if needed
                if (!playerCap.isMayorOf(playerCap.getMunicipality()) && stages.contains("mayor"))
                    GameStageHelper.removeStage(player, "mayor");

                if (!researchers.contains(player.getName()) && stages.contains("researcher"))
                    GameStageHelper.removeStage(player, "researcher");

                // Sync player's research to the municipality
                for (String stage : stages) {
                    if (technologies.contains(stage)) continue;
                    if (!stage.endsWith("_stage")) continue;
                    technologies.add(stage);
                }

                // Sync municipality's research to the player
                for (String tech : technologies) {
                    if (GameStageHelper.hasStage(player, tech)) continue;
                    GameStageHelper.addStage(player, tech);
                }

                GameStageHelper.syncPlayer(player);
                Print.chat(sender, "Successfully synchronized player and municipality research.");
                return;
            case "remove": {
                if (args.length < 2) {
                    Print.chat(sender, "&cUsage: /research remove <player>");
                    return;
                }

                if (!playerCap.isMayorOf(playerCap.getMunicipality())) {
                    Print.chat(sender, "You are not Mayor of the Municipality.");
                    return;
                }

                GameProfile gp = server.getPlayerProfileCache().getGameProfileForUsername(args[1]);

                if (gp == null || gp.getId() == null){
                    Print.chat(sender, "&cThis player never joined the server!");
                    return;
                }

                if (!researchers.contains(gp.getName())) {
                    Print.chat(sender, "That player isn't a municipality researcher.");
                    return;
                }

                researchers.remove(gp.getName());
                Print.chat(sender, "Removing " + gp.getName() + " from municipality research team.");

                EntityPlayer researcher = server.getPlayerList().getPlayerByUUID(gp.getId());
                if (GameStageHelper.hasStage(researcher, "researcher")) {
                    GameStageHelper.removeStage(researcher, "researcher");
                    GameStageHelper.syncPlayer(researcher);
                }
                return;
            }
            case "add": {
                if (args.length < 2) {
                    Print.chat(sender, "&cUsage: /research add <username>");
                    return;
                }

                if (!playerCap.isMayorOf(playerCap.getMunicipality())) {
                    Print.chat(sender, "You are not Mayor of the Municipality.");
                    return;
                }

                GameProfile gp = server.getPlayerProfileCache().getGameProfileForUsername(args[1]);

                if (gp == null || gp.getId() == null){
                    Print.chat(sender, "&cThis player never joined the server!");
                    return;
                }

                if (!playerCap.getMunicipality().getCitizen().contains(gp.getId())) {
                    Print.chat(sender, "That player isn't a member of this municipality.");
                    return;
                }

                if (researchers.contains(gp.getName())) {
                    Print.chat(sender, "That player is already a municipality researcher.");
                    return;
                }

                if (researchers.size() >= floor((playerCap.getMunicipality().getCitizen().size() / researcherRatio) + 1)) {
                    Print.chat(sender, "Your municipality research team is full! Increase your population to unlock more slots!");
                    return;
                }

                researchers.add(gp.getName());
                Print.chat(sender, "Adding " + gp.getName() + " to municipality research team.");
                EntityPlayer researcher = server.getPlayerList().getPlayerByUUID(gp.getId());
                GameStageHelper.addStage(researcher, "researcher");
                GameStageHelper.syncPlayer(researcher);
                return;
            }
            case "list":
                if (playerCap.getMunicipality() == null) {
                    Print.chat(sender, "You are not a member of a municipality.");
                    return;
                }

                Print.chat(sender, "Municipality's researchers:");
                for (String researcher : researchers)
                    Print.chat(sender, researcher);
                return;
            default:
                Print.chat(sender, helpString);
        }
    }
}
