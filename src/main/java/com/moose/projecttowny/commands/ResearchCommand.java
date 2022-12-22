package com.moose.projecttowny.commands;

import com.mojang.authlib.GameProfile;
import com.moose.projecttowny.data.Data;
import net.darkhax.gamestages.GameStageHelper;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;

import static java.lang.Math.floor;
public class ResearchCommand extends CommandBase {

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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        EntityPlayer player = getCommandSenderAsPlayer(sender);
        PlayerCapability playerCap = (player.getCapability(StatesCapabilities.PLAYER, null));

        Data externalData = new Data();
        ArrayList<String> munResearchers = externalData.getResearchers(playerCap.getMunicipality());

        if (args.length > 0) {

            if ("add".equalsIgnoreCase(args[0])) {

                if (args.length < 2) {
                    Print.chat(sender, "&cUsage: /research add <player>");
                    return;
                }
                if (!playerCap.isMayorOf(playerCap.getMunicipality())) {
                    Print.chat(sender, "You are not the Mayor.");
                    return;
                }

                GameProfile gp = server.getPlayerProfileCache().getGameProfileForUsername(args[1]);

                if (gp == null || gp.getId() == null){
                    Print.chat(sender, "&cPlayer not found.");
                    return;
                }

                if (!playerCap.getMunicipality().getCitizen().contains(gp.getId())) {
                    Print.chat(sender, "That player isn't a member of this municipality.");
                    return;
                }

                if (munResearchers.contains(gp.getName())) {
                    Print.chat(sender, "That player is already a municipality researcher.");
                    return;
                }

                if (munResearchers.size() < floor((playerCap.getMunicipality().getCitizen().size() / researcherRatio) + 1)) {
                    munResearchers.add(gp.getName());
                    Print.chat(sender, "Adding " + gp.getName() + " to municipality research team.");

                    EntityPlayer researcher = server.getPlayerList().getPlayerByUUID(gp.getId());
                    GameStageHelper.addStage(researcher, "researcher");
                    GameStageHelper.syncPlayer(researcher);
                }
                else {
                    Print.chat(sender, "Your municipality research team is full! Increase your population to unlock more slots!");
                }
            }

            if ("remove".equalsIgnoreCase(args[0])) {

                if (args.length < 2) {
                    Print.chat(sender, "&cUsage: /research remove <player>");
                    return;
                }
                if (!playerCap.isMayorOf(playerCap.getMunicipality())) {
                    Print.chat(sender, "You are not the Mayor.");
                    return;
                }

                GameProfile gp = server.getPlayerProfileCache().getGameProfileForUsername(args[1]);

                if (gp == null || gp.getId() == null){
                    Print.chat(sender, "&cPlayer not found.");
                    return;
                }

                if (!munResearchers.contains(gp.getName())) {
                    Print.chat(sender, "That player isn't a municipality researcher.");
                    return;
                }

                munResearchers.remove(gp.getName());
                Print.chat(sender, "Removing " + gp.getName() + " from municipality research team.");

                EntityPlayer researcher = server.getPlayerList().getPlayerByUUID(gp.getId());
                if (GameStageHelper.hasStage(researcher, "researcher")) {
                    GameStageHelper.removeStage(researcher, "researcher");
                    GameStageHelper.syncPlayer(researcher);
                }
            }

            if ("list".equalsIgnoreCase(args[0])) {

                if (playerCap.getMunicipality() == null) {
                    Print.chat(sender, "You are not apart of a municipality");
                    return;
                }

                Print.chat(sender, "Researchers:");
                for (String researcher : munResearchers) {
                    Print.chat(sender, researcher);
                }
            }

            if ("sync".equalsIgnoreCase(args[0])) {

                ArrayList<String> munTechnologies = externalData.getTechnologies(playerCap.getMunicipality());

                for (String tech : munTechnologies) {
                    if (GameStageHelper.hasStage(player, tech)) continue;
                    GameStageHelper.addStage(player, tech);
                }
                GameStageHelper.syncPlayer(player);
                Print.chat(sender, "Synced player research to municipality research.");
            }

            if ("help".equalsIgnoreCase(args[0])) {
                Print.chat(sender, "/research list\n/research sync\n/research add\n/research remove");
            }
        }
        else {
            Print.chat(sender, "/research sync\n/research list\n/research add\n/research remove");
        }
    }
}
