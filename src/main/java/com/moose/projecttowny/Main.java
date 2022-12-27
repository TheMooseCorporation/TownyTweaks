package com.moose.projecttowny;

import com.moose.projecttowny.commands.DiplomacyCommand;
import com.moose.projecttowny.commands.ResearchCommand;
import com.moose.projecttowny.handlers.StatesModHandler;
import com.moose.projecttowny.handlers.GameStageHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = Main.MODID, name = Main.NAME, version = Main.VERSION)
public class Main {
    public static final String MODID = "projecttowny";
    public static final String NAME = "Project Towny Tweaks";
    public static final String VERSION = "0.3.2";

    public static Logger LOGGER;

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new StatesModHandler());
        MinecraftForge.EVENT_BUS.register(new GameStageHandler());
        LOGGER = event.getModLog();
    }

    @EventHandler
    public static void registerCommands(FMLServerStartingEvent event) {
        event.registerServerCommand(new DiplomacyCommand());
        event.registerServerCommand(new ResearchCommand());
    }
}

