package com.moose.projecttowny.data;

import net.fexcraft.mod.states.events.MunicipalityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ExternalDataEvents {

    @SubscribeEvent
    public void attachMunData(MunicipalityEvent.Load event) {

        System.out.println("Attempting to create External Data");
        event.getMunicipality().setExternalData("project_towny_tweaks", new Data());
        System.out.println("External Data Created");
    }
}
