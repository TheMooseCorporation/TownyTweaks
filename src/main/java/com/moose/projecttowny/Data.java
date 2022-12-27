package com.moose.projecttowny;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.root.ExternalData;


public class Data implements ExternalData {

    public ArrayList<Integer> activeWars = new ArrayList<>();

    public static ArrayList<Integer> getActiveWars(Municipality mun) {
        Data externalData = mun.getExternalData("project_towny_tweaks");
        return externalData.activeWars;
    }

    public ArrayList<String> researchers = new ArrayList<>();

    public static ArrayList<String> getResearchers(Municipality mun) {
        Data externalData = mun.getExternalData("project_towny_tweaks");
        return externalData.researchers;
    }

    public ArrayList<String> technologies = new ArrayList<>();

    public static ArrayList<String> getTechnologies(Municipality mun) {
        Data externalData = mun.getExternalData("project_towny_tweaks");
        return externalData.technologies;
    }

    public void load(JsonElement elm){
        JsonObject obj = elm.getAsJsonObject();
        if(obj.has("activeWars")){
            JsonArray array = obj.get("activeWars").getAsJsonArray();
            for (JsonElement war : array) {
                activeWars.add(war.getAsInt());
            }
        }
        if(obj.has("researchers")){
            JsonArray array = obj.get("researchers").getAsJsonArray();
            for (JsonElement researcher : array) {
                researchers.add(researcher.getAsString());
            }
        }
        if(obj.has("technologies")){
            JsonArray array = obj.get("technologies").getAsJsonArray();
            for (JsonElement tech : array) {
                technologies.add(tech.getAsString());
            }
        }
    }

    public JsonElement save(){
        JsonObject obj = new JsonObject();
        if(!activeWars.isEmpty()){
            JsonArray array = new JsonArray();
            for (int i = 0; i < activeWars.size(); i++){
                array.add(activeWars.get(i));
            }
            obj.add("activeWars", array);
        }
        if(!researchers.isEmpty()){
            JsonArray array = new JsonArray();
            for (int i = 0; i < researchers.size(); i++){
                array.add(researchers.get(i));
            }
            obj.add("researchers", array);
        }
        if(!technologies.isEmpty()){
            JsonArray array = new JsonArray();
            for (int i = 0; i < technologies.size(); i++){
                array.add(technologies.get(i));
            }
            obj.add("technologies", array);
        }
        return obj;
    }
}
