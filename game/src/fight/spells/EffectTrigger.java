package fight.spells;

import java.util.ArrayList;

public class EffectTrigger {
    private int triggerID; // ID du trigger
    private ArrayList<Integer> effectsTriggering = new ArrayList<>(); //L'ensembles des effets qui peuvent trigger l'effect OnHit
    private int target = 0; // Soit sa touche celui qui a l'effet, soit celui qui a déclanché l'effet (1: c'est le déclancheur de l'effet)
    private boolean isunbuffable = false; // Ca c'est juste pour gérer si l'effet est débuffable
    private String description; // La zone d'impact de l'effet

    // Un constructeur pour les Effets de Spell au chargement de la BDD
    public EffectTrigger(int triggerID, String effectsTriggering, int target,int isunbuffable,String description) {
        this.triggerID = triggerID;
        if(effectsTriggering.contains(",")) {
            for (String effetString: effectsTriggering.split(",")) {
                try{
                    int effetID = Integer.parseInt(effetString);
                    this.effectsTriggering.add(effetID);
                }
                catch (Exception e){

                }
            }
        }
        else{
            try{
                int effetID = Integer.parseInt(effectsTriggering);
                this.effectsTriggering.add(effetID);
            }
            catch (Exception e){

            }
        }
        this.target = target;
        if(isunbuffable==1){
            this.isunbuffable = true;
        }
        this.description = description;
    }

    public Integer getTriggerID() {
        return this.triggerID;
    }

    public Integer getTarget() {
        return this.target;
    }

    public boolean getUnbuffable() {
        return this.isunbuffable;
    }

    public String getTriggerDesc() {
        return this.description;
    }

    public ArrayList<Integer> getEffectsTriggering() {
        return this.effectsTriggering;
    }

    public void setInfos(String effectsTriggering, int target, int isunbuffable, String description) {
        this.effectsTriggering.clear();
        if(effectsTriggering.contains(",")) {
            for (String effetString: effectsTriggering.split(",")) {
                try{
                    int effetID = Integer.parseInt(effetString);
                    this.effectsTriggering.add(effetID);
                }
                catch (Exception e){

                }
            }
        }
        else{
            try{
                int effetID = Integer.parseInt(effectsTriggering);
                this.effectsTriggering.add(effetID);
            }
            catch (Exception e){

            }
        }
        this.target = target;
        if(isunbuffable==1){
            this.isunbuffable = true;
        }
        this.description = description;
    }

}
