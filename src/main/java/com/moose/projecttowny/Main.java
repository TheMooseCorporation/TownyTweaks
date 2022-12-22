package com.moose.projecttowny;

import com.moose.projecttowny.commands.DiplomacyCommand;
import com.moose.projecttowny.commands.ResearchCommand;
import com.moose.projecttowny.data.ExternalDataEvents;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = Main.MODID, name = Main.NAME, version = Main.VERSION)
public class Main
{
    public static final String MODID = "projecttowny";
    public static final String NAME = "Project Towny Tweaks";
    public static final String VERSION = "0.3.2";

    @Mod.Instance
    public static Main instance;

    @EventHandler
    public static void PreInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ExternalDataEvents());
        MinecraftForge.EVENT_BUS.register(new TownyEventHandler());

    }

    @EventHandler
    public static void init(FMLInitializationEvent event) {

    }

    @EventHandler
    public static void PostInit(FMLPostInitializationEvent event) {

    }

    @EventHandler
    public static void registerCommands(FMLServerStartingEvent event) {
        event.registerServerCommand(new DiplomacyCommand());
        event.registerServerCommand(new ResearchCommand());
    }

}

