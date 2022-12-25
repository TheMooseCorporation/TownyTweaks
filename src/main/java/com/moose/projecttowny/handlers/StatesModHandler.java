package com.moose.projecttowny.handlers;

import com.moose.projecttowny.Main;
import com.moose.projecttowny.Data;
import net.fexcraft.mod.states.events.MunicipalityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class StatesModHandler {

    @SubscribeEvent
    public void loadMunicipality(MunicipalityEvent.Load event) {
        Main.LOGGER.info("Adding external data into " +  event.getMunicipality().getName() + " municipality");
        event.getMunicipality().setExternalData("project_towny_tweaks", new Data());
    }
}
