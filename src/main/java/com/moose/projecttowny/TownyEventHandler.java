package com.moose.projecttowny;

import com.moose.projecttowny.data.Data;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.event.GameStageEvent;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collection;

public class TownyEventHandler {

    /*
        Checks if the player is a researcher and if so, adds every game stage they have to the municipality research
        Called whenever a new stage is added
     */
    @SubscribeEvent
    public void onStageAdd(GameStageEvent.Added event) {

        EntityPlayer player = event.getEntityPlayer();
        PlayerCapability playerCap = (player.getCapability(StatesCapabilities.PLAYER, null));

        Data externalData = new Data();

        ArrayList<String> munTechnologies = externalData.getTechnologies(playerCap.getMunicipality());
        ArrayList<String> munResearchers = externalData.getResearchers(playerCap.getMunicipality());

        // If the player is a researcher
        if (munResearchers.contains(player.getName())) {
            Collection<String> researcherStages = GameStageHelper.getPlayerData(player).getStages();
            // Loop through every stage the researcher has and if it's not already in the municipality research add it
            for (String stage : researcherStages) {
                // If the game stage is already in the municipality's research, move to the next game stage
                if (munTechnologies.contains(stage)) continue;
                // If the game stage is not a technology stage, then don't add it (technology stages end with _stage)
                if (!stage.contains("stage")) continue;
                munTechnologies.add(stage);
                Print.chat(player, "Added " + stage + " to municipality's research.");
            }
        }
    }
}
