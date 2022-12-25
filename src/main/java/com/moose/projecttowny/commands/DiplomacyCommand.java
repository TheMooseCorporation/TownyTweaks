package com.moose.projecttowny.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.moose.projecttowny.Data;
import com.moose.projecttowny.Util;
import net.darkhax.gamestages.GameStageHelper;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.Bank.Action;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;

public class DiplomacyCommand extends CommandBase {
    private final String helpString = "/diplomacy withdraw [state/municipality] <amount> - Withdraw money from treasury\n" +
            "/diplomacy declare [war/peace] <municipality ID> - Declare war or peace\n" +
            "/diplomacy mayor - Unlock Mayoral Duties questline\n" +
            "/diplomacy online - List all online municipalities\n" +
            "/diplomacy wars - List all active wars you have\n";

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
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length == 0)
            return Arrays.asList("withdraw", "declare", "mayor", "online", "wars");

        if (args.length == 1) {
            final String arg0 = args[0].toLowerCase();
            return Util.autoComplete(Arrays.asList("withdraw", "declare", "mayor", "online", "wars"), arg0);
        }

        if (args.length == 2) {
            final String arg0 = args[0].toLowerCase();
            final String arg1 = args[1].toLowerCase();
            switch (arg0) {
                case "withdraw":
                    return Util.autoComplete(Arrays.asList("state", "municipality"), arg1);
                case "declare":
                    return Util.autoComplete(Arrays.asList("peace", "war"), arg1);
            }
        }

        return new ArrayList<>();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        PlayerCapability playerCap = ((EntityPlayer) sender).getCapability(StatesCapabilities.PLAYER, null);

        if (args.length == 0) {
            Print.chat(sender, helpString);
            return;
        }

        ArrayList<Municipality> muns;
        switch (args[0].toLowerCase()) {
            case "online":
                muns = getOnlineMunicipalities(server);
                if (muns.isEmpty()) {
                    Print.chat(sender, "No Municipality mayors are online");
                } else {
                    Print.chat(sender, "Online Municipalities:");
                    for (Municipality mun : muns)
                        Print.chat(sender, "&2-> &6" + mun.getName() + " (" + mun.getId() + ")");
                }
                return;
            case "declare":
                if (args.length < 3) {
                    Print.chat(sender, "&cUsage: /diplomacy declare [war/peace] <municipality ID>");
                    return;
                }

                try {
                    final Integer id = Integer.parseInt(args[2]);
                    muns = getOnlineMunicipalities(server);
                    switch (args[1].toLowerCase()) {
                        case "war": {
                            if (!playerCap.isMayorOf(playerCap.getMunicipality()))
                                Print.chat(sender, "Only Municipality mayors can declare war");

                            if (playerCap.getMunicipality().getId() == Integer.parseInt(args[1])) {
                                Print.chat(sender, "You can't declare war on yourself");
                                return;
                            }

                            final Optional<Municipality> mun = muns.stream().filter(x -> x.getId() == id).findFirst();
                            if (!mun.isPresent()) {
                                Print.chat(sender, "Municipality not found, or Municipality mayor offline");
                                return;
                            }

                            if (Data.getActiveWars(playerCap.getMunicipality()).contains(id)) {
                                Print.chat(sender, "You are already at war with " + mun.get().getName());
                                return;
                            }

                            final int spawnRadius = 1000000000; // This is a terrible way to do this
                            Print.chat(sender, "Declaring War against " + mun.get().getName());
                            Municipality localMun = playerCap.getMunicipality();
                            Data.getActiveWars(playerCap.getMunicipality()).add(id);

                            SPacketTitle.Type typeTitle = SPacketTitle.Type.TITLE;
                            ITextComponent warDeclaredText = new TextComponentString("War Declared");
                            SPacketTitle warDeclaredTitlePacket = new SPacketTitle(typeTitle, warDeclaredText);

                            SPacketTitle.Type typeSubtitle = SPacketTitle.Type.SUBTITLE;
                            ITextComponent warDeclaredSubText = new TextComponentString(localMun.getName()
                                    + " has declared war on " + mun.get().getName());
                            SPacketTitle warDeclaredSubtitlePacket = new SPacketTitle(typeSubtitle, warDeclaredSubText);

                            server.getPlayerList().sendPacketToAllPlayers(warDeclaredSubtitlePacket);
                            server.getPlayerList().sendPacketToAllPlayers(warDeclaredTitlePacket);
                            server.getPlayerList().sendPacketToAllPlayers(new SPacketCustomSound("minecraft:ambient.cave",
                                    SoundCategory.MASTER, 0, 0, 0, spawnRadius, (float) 1));
                            return;
                        }
                        case "peace": {
                            if (!playerCap.isMayorOf(playerCap.getMunicipality()))
                                Print.chat(sender, "Only Municipality mayors can declare war");

                            if (playerCap.getMunicipality().getId() == Integer.parseInt(args[1])) {
                                Print.chat(sender, "You aren't at war with yourself");
                                return;
                            }

                            final Optional<Municipality> mun = muns.stream().filter(x -> x.getId() == id).findFirst();
                            if (!mun.isPresent()) {
                                Print.chat(sender, "Municipality not found, or Municipality mayor offline");
                                return;
                            }

                            if (!Data.getActiveWars(playerCap.getMunicipality()).contains(id)) {
                                Print.chat(sender, "You not at war with " + mun.get().getName());
                                return;
                            }

                            Municipality localMun = playerCap.getMunicipality();

                            final int spawnRadius = 1000000000; // This is a terrible way to do this
                            Print.chat(sender, "Making peace with " + mun.get().getName());

                            SPacketTitle.Type typeTitle = SPacketTitle.Type.TITLE;
                            ITextComponent warDeclaredText = new TextComponentString("Peace Established");
                            SPacketTitle warDeclaredTitlePacket = new SPacketTitle(typeTitle, warDeclaredText);

                            SPacketTitle.Type typeSubtitle = SPacketTitle.Type.SUBTITLE;
                            ITextComponent warDeclaredSubText = new TextComponentString(localMun.getName()
                                    + " has made peace with " + mun.get().getName());
                            SPacketTitle warDeclaredSubtitlePacket = new SPacketTitle(typeSubtitle, warDeclaredSubText);

                            server.getPlayerList().sendPacketToAllPlayers(warDeclaredSubtitlePacket);
                            server.getPlayerList().sendPacketToAllPlayers(warDeclaredTitlePacket);
                            server.getPlayerList().sendPacketToAllPlayers(new SPacketCustomSound("minecraft:record.cat",
                                    SoundCategory.MASTER, 0, 0, 0, spawnRadius, 1));

                            Data.getActiveWars(playerCap.getMunicipality()).remove((Object)mun.get().getId());
                            return;
                        }
                        default:
                            Print.chat(sender, "&cUsage: /diplomacy declare [war/peace] <municipality ID>");
                            return;
                    }
                } catch (NumberFormatException nfe) {
                    Print.chat(sender, "Not a valid number");
                    return;
                }
            case "wars":
                ArrayList<Integer> activeWars = Data.getActiveWars(playerCap.getMunicipality());

                if (activeWars.size() == 0) {
                    Print.chat(sender, "Your Municipality is not currently at war");
                    return;
                }

                Print.chat(sender, "Active Wars:");
                for (Integer x : activeWars)
                    Print.chat(sender, "&2-> &6" + StateUtil.getMunicipalityName(x) + " (" + x + ")");
                return;
            case "mayor":
                if (!playerCap.isMayorOf(playerCap.getMunicipality())) {
                    Print.chat(sender, "You are not the mayor of your municipality!");
                    return;
                }

                EntityPlayer mayor = (EntityPlayer) sender;
                if (GameStageHelper.hasStage(mayor, "mayor")) {
                    Print.chat(sender, "You already unlocked Mayoral Duties.");
                    return;
                }

                // Add the required stages to unlock the necessary quest-lines
                GameStageHelper.addStage(mayor, "mayor");
                GameStageHelper.syncPlayer(mayor);
                Print.chat(sender, "You are now able to access the Mayoral Duties quest-line.");

                // Automatically add the mayor to the town's researcher pool
                ArrayList<String> munResearchers = Data.getResearchers(playerCap.getMunicipality());
                munResearchers.add(mayor.getName());
                return;
            case "withdraw":
                if (args.length < 3) {
                    Print.chat(sender, "&cUsage: /diplomacy withdraw [state/municipality] <amount>");
                    return;
                }

                switch (args[1].toLowerCase()) {
                    case "state": {
                        final Bank bank = playerCap.getState().getBank();
                        if (!playerCap.isStateLeaderOf(playerCap.getState())) {
                            Print.chat(sender, "You are not the Leader of the state");
                            return;
                        }

                        try {
                            final long amount = Long.parseLong(args[1]) * 1000;
                            if (bank.processAction(Action.TRANSFER, sender, playerCap.getState().getAccount(), amount, playerCap.getAccount()))
                                Print.chat(sender, "Withdrew $" + amount / 1000 + " from " + playerCap.getState().getName() + "'s National Treasury");
                        } catch (NumberFormatException nfe) {
                            Print.chat(sender, "Not a valid number");
                        }

                        return;
                    }
                    case "municipality": {
                        final Bank bank = playerCap.getMunicipality().getBank();

                        if (!playerCap.isMayorOf(playerCap.getMunicipality())) {
                            Print.chat(sender, "You are not the Mayor of the municipality");
                            return;
                        }

                        try {
                            final long amount = Long.parseLong(args[1]) * 1000;
                            if (bank.processAction(Action.TRANSFER, sender, playerCap.getMunicipality().getAccount(), amount, playerCap.getAccount()))
                                Print.chat(sender, "Withdrew $" + amount / 1000 + " from " + playerCap.getMunicipality().getName() + "'s Town Treasury");
                        } catch (NumberFormatException nfe) {
                            Print.chat(sender, "Not a valid number");
                        }

                        return;
                    }
                    default:
                        Print.chat(sender, "&cUsage: /diplomacy withdraw [state/municipality] <amount>");
                        return;
                }
            default:
                Print.chat(sender, helpString);
        }
    }

    private ArrayList<Municipality> getOnlineMunicipalities(MinecraftServer server) {
        ArrayList<Municipality> muns = new ArrayList<>();

        for (EntityPlayer player : server.getPlayerList().getPlayers()) {
            PlayerCapability otherPlayerCap = player.getCapability(StatesCapabilities.PLAYER, null);
            if (otherPlayerCap.getMunicipality().getId() <= 0) continue;
            if (otherPlayerCap.isMayorOf(otherPlayerCap.getMunicipality()))
                muns.add(otherPlayerCap.getMunicipality());
        }

        return muns;
    }
}
