package com.moose.projecttowny.handlers;

import com.moose.projecttowny.Data;
import net.darkhax.gamestages.event.GameStageEvent;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GameStageHandler {

    /**
     * Checks if the player is a researcher and if so, adds the new game stage to municipality research.
     * Called whenever a new stage is added. I hope this is common sense?
     */
    @SubscribeEvent
    public void onStageAdd(GameStageEvent.Added event) {
        final String stage = event.getStageName();
        if (stage.endsWith("_stage")) return;

        final EntityPlayer player = event.getEntityPlayer();
        final PlayerCapability playerCap = player.getCapability(
                StatesCapabilities.PLAYER, null);
        final Municipality mun = playerCap.getMunicipality();

        if (!Data.getResearchers(playerCap.getMunicipality()).contains(player.getName())
                || Data.getTechnologies(mun).contains(stage))
            return;

        Data.getTechnologies(mun).add(stage);
        Print.chat(player, "Added " + stage + " to municipality's research.");
    }
}
