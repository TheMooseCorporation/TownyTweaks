package com.moose.projecttowny.commands;

import java.util.ArrayList;

import com.moose.projecttowny.data.Data;
import net.darkhax.gamestages.GameStageHelper;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.Bank.Action;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class DiplomacyCommand extends CommandBase {

    @Override
    public String getName() {
        return "diplomacy";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/diplomacy";
    }

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

        PlayerCapability playerCap = ((EntityPlayer) sender).getCapability(StatesCapabilities.PLAYER, null);
        ArrayList<Municipality> muns = new ArrayList<>();
        Data externalData = new Data();

        for (EntityPlayer player : server.getPlayerList().getPlayers()) {
            System.out.println(player);
            PlayerCapability otherPlayerCap = player.getCapability(StatesCapabilities.PLAYER, null);
            if (otherPlayerCap.getMunicipality().getId() <= 0) continue;
            if (otherPlayerCap.isMayorOf(otherPlayerCap.getMunicipality())) {
                muns.add(otherPlayerCap.getMunicipality());
                System.out.println(muns);
            }
        }

        if (args.length > 0) {

            if ("online".equalsIgnoreCase(args[0])) {

                if (muns.isEmpty()) {
                    Print.chat(sender, "No Municipalities mayors are online");
                }

                else {

                    Print.chat(sender, "Online Municipalities:");
                    for(Municipality mun : muns) {
                        Print.chat(sender, "&2-> &6" + mun.getName() + " (" + mun.getId() + ")");
                    }
                }
            }

            if ("declarewar".equalsIgnoreCase(args[0])) {

                if (playerCap.isMayorOf(playerCap.getMunicipality())) {

                    if (args.length >= 2) {

                        if (playerCap.getMunicipality().getId() != Integer.parseInt(args[1])) {
                            for(Municipality mun : muns) {

                                if (mun.getId() == Integer.parseInt(args[1])) {

                                    ArrayList<Integer> activeWars = externalData.getActiveWars(playerCap.getMunicipality());
                                    int spawnRadius = 1000000000; // This is a terrible way to do this

                                    if (!activeWars.contains(mun.getId())) {

                                        Print.chat(sender, "Declaring War against " + mun.getName());
                                        Municipality localMun = playerCap.getMunicipality();
                                        externalData.getActiveWars(playerCap.getMunicipality()).add(mun.getId());

                                        SPacketTitle.Type typeTitle = SPacketTitle.Type.TITLE;
                                        ITextComponent warDeclaredText = new TextComponentString("War Declared");
                                        SPacketTitle warDeclaredTitlePacket = new SPacketTitle(typeTitle, warDeclaredText);

                                        SPacketTitle.Type typeSubtitle = SPacketTitle.Type.SUBTITLE;
                                        ITextComponent warDeclaredSubText = new TextComponentString(localMun.getName() + " has declared war on " + mun.getName());
                                        SPacketTitle warDeclaredSubtitlePacket = new SPacketTitle(typeSubtitle, warDeclaredSubText);

                                        server.getPlayerList().sendPacketToAllPlayers(warDeclaredSubtitlePacket);
                                        server.getPlayerList().sendPacketToAllPlayers(warDeclaredTitlePacket);
                                        server.getPlayerList().sendPacketToAllPlayers(new SPacketCustomSound("minecraft:ambient.cave", SoundCategory.MASTER, 0, 0, 0, spawnRadius, (float) 1));

                                        break;
                                    }
                                    else {
                                        Print.chat(sender, "You are already at war with " + mun.getName());
                                    }
                                }
                                else {
                                    Print.chat(sender, "Municipality not found, or Muncipality mayor offline");
                                    break;
                                }
                            }
                        }
                        else {
                            Print.chat(sender, "You can't declare war on yourself");
                        }
                    }
                    else {
                        Print.chat(sender, "&cUsage: /diplomacy declarewar <municipality id>");
                    }
                }
                else {
                    Print.chat(sender, "Only Municipality mayors can declare war");
                }
            }

            if ("peace".equalsIgnoreCase(args[0])) {

                if (playerCap.isMayorOf(playerCap.getMunicipality())) {

                    if (args.length >= 2) {

                        if (playerCap.getMunicipality().getId() != Integer.parseInt(args[1])) {

                            for(Municipality mun : muns) {

                                if (mun.getId() == Integer.parseInt(args[1])) {
                                    ArrayList<Integer> activeWars = externalData.getActiveWars(playerCap.getMunicipality());
                                    int spawnRadius = 1000000000;

                                    if (activeWars.contains(mun.getId())) {

                                        Municipality localMun = playerCap.getMunicipality();

                                        Print.chat(sender, "Making peace with " + mun.getName());

                                        SPacketTitle.Type typeTitle = SPacketTitle.Type.TITLE;
                                        ITextComponent warDeclaredText = new TextComponentString("Peace Established");
                                        SPacketTitle warDeclaredTitlePacket = new SPacketTitle(typeTitle, warDeclaredText);

                                        SPacketTitle.Type typeSubtitle = SPacketTitle.Type.SUBTITLE;
                                        ITextComponent warDeclaredSubText = new TextComponentString(localMun.getName() + " has made peace with " + mun.getName());
                                        SPacketTitle warDeclaredSubtitlePacket = new SPacketTitle(typeSubtitle, warDeclaredSubText);

                                        server.getPlayerList().sendPacketToAllPlayers(warDeclaredSubtitlePacket);
                                        server.getPlayerList().sendPacketToAllPlayers(warDeclaredTitlePacket);
                                        server.getPlayerList().sendPacketToAllPlayers(new SPacketCustomSound("minecraft:record.cat", SoundCategory.MASTER, 0, 0, 0, spawnRadius, 1));


                                        for (int i = 0; i < activeWars.size(); i++) {
                                            if (activeWars.get(i) == mun.getId()) {
                                                externalData.getActiveWars(playerCap.getMunicipality()).remove(i);
                                                break;
                                            }
                                        }

                                        break;
                                    }
                                    else {
                                        Print.chat(sender, "You are currently not at war with " + mun.getName());
                                    }
                                }
                                else {
                                    Print.chat(sender, "Municipality not found, or Muncipality mayor offline");
                                    break;
                                }
                            }
                        }
                        else {
                            Print.chat(sender, "You're not at war with yourself");
                        }

                    }
                    else {
                        Print.chat(sender, "&cUsage: /diplomacy peace <municipality id>");
                    }
                }
                else {
                    Print.chat(sender, "Only Municipality mayors can make peace");
                }
            }
            if ("wars".equalsIgnoreCase(args[0])) {

                ArrayList<Integer> activeWars = externalData.getActiveWars(playerCap.getMunicipality());

                if (!(activeWars.size() == 0)) {
                    Print.chat(sender, "Active Wars:");
                    for (int i = 0; i < activeWars.size(); i++) {
                        Integer x = activeWars.get(i);
                        Print.chat(sender, "&2-> &6" + StateUtil.getMunicipalityName(x) + " (" + x + ")");
                    }
                }
                else {
                    Print.chat(sender, "Your Municipality is not currently at war");
                }


            }

            if ("help".equalsIgnoreCase(args[0])) {

                Print.chat(sender, "/diplomacy online\n/diplomacy declarewar\n/diplomacy wars\n/diplomacy peace");

            }

            if ("mayor".equalsIgnoreCase(args[0])) {

                if (!playerCap.isMayorOf(playerCap.getMunicipality())) {
                    Print.chat(sender, "You are not the mayor of your municipality!");
                    return;
                }

                EntityPlayer mayor = (EntityPlayer) sender;

                if (GameStageHelper.hasStage(mayor, "mayor")) {
                    Print.chat(sender, "You have already run this command.");
                    return;
                }

                // Add the required stages to unlock the necessary quest-lines
                GameStageHelper.addStage(mayor, "mayor");
                GameStageHelper.syncPlayer(mayor);
                Print.chat(sender, "You are now able to access the Mayoral Duties quest-line.");

                // Automatically add the mayor to the town's researcher pool
                ArrayList<String> munResearchers = externalData.getResearchers(playerCap.getMunicipality());
                munResearchers.add(mayor.getName());

            }

            if ("withdraw".equalsIgnoreCase(args[0])) {

                Bank bank = playerCap.getMunicipality().getBank();
                if (args.length >= 2) {

                    if (playerCap.isMayorOf(playerCap.getMunicipality())) {

                        // This could probably be implemented better
                        try {
                            @SuppressWarnings("unused")
                            long l = Long.parseLong(args[1]);

                        } catch (NumberFormatException nfe) {
                            Print.chat(sender, "Not a valid number");
                            return;
                        }

                        Long amount = Long.parseLong(args[1]);
                        amount = amount * 1000;

                        bank.processAction(Action.TRANSFER, sender, playerCap.getMunicipality().getAccount(), amount, playerCap.getAccount());
                        Print.chat(sender, "Withdrew $" + amount / 1000 + " from " + playerCap.getMunicipality().getName() + "'s Town Treasury");

                    }
                    else {
                        Print.chat(sender, "You are not the Mayor");
                    }
                }
                else {
                    Print.chat(sender, "&cUsage: /diplomacy withdraw <amount>");
                }
            }

            if ("statewithdraw".equalsIgnoreCase(args[0])) {

                Bank bank = playerCap.getState().getBank();
                if (args.length >= 2) {

                    if (playerCap.isStateLeaderOf(playerCap.getState())) {

                        // This could probably be implemented better
                        try {
                            @SuppressWarnings("unused")
                            long l = Long.parseLong(args[1]);

                        } catch (NumberFormatException nfe) {
                            Print.chat(sender, "Not a valid number");
                            return;
                        }

                        Long amount = Long.parseLong(args[1]);
                        amount = amount * 1000;

                        bank.processAction(Action.TRANSFER, sender, playerCap.getState().getAccount(), amount, playerCap.getAccount());
                        Print.chat(sender, "Withdrew $" + amount / 1000 + " from " + playerCap.getState().getName() + "'s National Treasury");

                    }
                    else {
                        Print.chat(sender, "You are not the Leader");
                    }
                }
                else {
                    Print.chat(sender, "&cUsage: /diplomacy statewithdraw <amount>");
                }
            }
        }
    }
}
