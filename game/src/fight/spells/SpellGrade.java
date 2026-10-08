package fight.spells;

import area.map.GameCase;
import common.Formulas;
import fight.Fight;
import fight.Fighter;
import game.world.World;
import kernel.Constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpellGrade {

    private int spellID;
    private int level;
    private int PACost;
    private int minPO;
    private int maxPO;
    private int TauxCC;
    private int TauxEC;
    private boolean isLineLaunch;
    private boolean hasLDV;
    private boolean isEmptyCell;
    private boolean isModifPO;
    private int maxLaunchbyTurn;
    private int maxLaunchbyByTarget;
    private int coolDown;
    private int reqLevel;
    private boolean isEcEndTurn;
    //private ArrayList<Effect> effects = new ArrayList<>();
    //private ArrayList<Effect> CCeffects  = new ArrayList<>();
    private ArrayList<Effect> effectsSpell  = new ArrayList<>();
    private ArrayList<Integer> statesForbidden;
    private int stateRequire;
    private int type;

    public SpellGrade(int spellid, int gradeid, int paCost, int poMin, int poMax, int ratioCC, int ratioEC, boolean isLine, boolean needLOS, boolean needEmptyC, boolean isPoModif, int maxByTurn, int maxByTarget, int cd, int lvlLearn, boolean endTurn, ArrayList<Integer> forbiddenStates, int stateNeed) {
        this.spellID = spellid;
        this.level = gradeid;
        this.PACost = paCost;
        this.minPO = poMin;
        this.maxPO = poMax;
        this.TauxCC = ratioCC;
        this.TauxEC = ratioEC;
        this.isLineLaunch = isLine;
        this.hasLDV = needLOS;
        this.isEmptyCell = needEmptyC;
        this.isModifPO = isPoModif;
        this.maxLaunchbyTurn = maxByTurn;
        this.maxLaunchbyByTarget = maxByTarget;
        this.coolDown = cd;
        this.reqLevel = lvlLearn;
        this.isEcEndTurn = endTurn;
        this.statesForbidden = forbiddenStates;
        this.stateRequire = stateNeed;
    }

    /*public void addEffect(Effect sp){
        this.effects.add(sp);
    }*/

    public void addEffectSpell(Effect se) {
        this.effectsSpell.add(se);
    }

    /*public void addCCEffect(Effect sp){
        this.CCeffects.add(sp);
    }*/

    public int getSpellID() {
        return spellID;
    }

    public int getStateRequire() {
        return stateRequire;
    }

    public ArrayList<Integer> getStatesForbidden() {
        return statesForbidden;
    }

    public Spell getSpell() {
        return World.world.getSort(spellID);
    }

    public int getSpriteID() {
        return getSpell().getSpriteID();
    }

    public String getSpriteInfos() {
        return getSpell().getSpriteInfos();
    }

    public int getLevel() {
        return level;
    }

    public int getPACost() {
        return PACost;
    }

    public int getMinPO() {
        return minPO;
    }

    public int getMaxPO() {
        return maxPO;
    }

    public int getTauxCC() {
        return TauxCC;
    }

    public int getTauxEC() {
        return TauxEC;
    }

    public boolean isLineLaunch() {
        return isLineLaunch;
    }

    public boolean hasLDV() {
        return hasLDV;
    }

    public boolean isEmptyCell() {
        return isEmptyCell;
    }

    public boolean isModifPO() {
        return isModifPO;
    }

    public int getMaxLaunchbyTurn() {
        return maxLaunchbyTurn;
    }

    public int getMaxLaunchByTarget() {
        return maxLaunchbyByTarget;
    }

    public int getCoolDown() {
        return coolDown;
    }

    public int getReqLevel() {
        return reqLevel;
    }

    public boolean isEcEndTurn() {
        return isEcEndTurn;
    }

    public ArrayList<Effect> getEffects() {
        return effectsSpell;
    }

  /* public ArrayList<Effect> getCCeffects() {
        return CCeffects;
    }*/

    public int getTypeSwitchSpellEffects() {
        /*String stype = "";
        switch (this.type){
            case 0: stype= "offensif";
                break;
            case 1: stype= "buff";
                break;
            case 2: stype= "invocation";
                break;
            case 3: stype= "heal";
                break;
            case 4: stype= "trap";
                break;
            case 5: stype= "mouvement";
                break;
            case 6: stype= "useless";
                break;
        }
        System.out.println("Le systeme a décidé que le sort " + getSpell().getName() + " était majoritairement de type " +  stype);*/
        return this.type;
    }

    public void setTypeSwitchSpellEffects() {
        int majorType = 0;
        int[] tableau = new int[7];
        if(this.getSpell().getType() == -1) {

            for (Effect effect : this.getEffects()) {
                // Effet de buff
                if (Arrays.stream(Constant.SPELLEFFECT_BUFF).anyMatch(value -> value == effect.getEffectID())) {
                    if (effect.getEffectID() == 950 || effect.getEffectID() == 951) {
                        tableau[1] += 6000;
                    }
                    tableau[1] += 10 + Formulas.getMiddleJet(effect);
                    continue;
                } else if (Arrays.stream(Constant.SPELLEFFECT_DEBUFF).anyMatch(value -> value == effect.getEffectID())) {
                    if (effect.getEffectID() == 140) {
                        tableau[0] += 1000;
                    }
                    tableau[0] += 10 + Formulas.getMiddleJet(effect);
                    continue;
                } else if (Arrays.stream(Constant.SPELLEFFECT_DAMMAGE).anyMatch(value -> value == effect.getEffectID())) {
                    if (effect.getEffectID() == 141) {
                        tableau[0] += 10000;
                    }
                    tableau[0] += 10 + Formulas.getMiddleJet(effect);
                    continue;
                } else if (Arrays.stream(Constant.SPELLEFFECT_HEAL).anyMatch(value -> value == effect.getEffectID())) {
                    tableau[3] += 10 + Formulas.getMiddleJet(effect);
                    continue;
                } else if (Arrays.stream(Constant.SPELLEFFECT_INVO).anyMatch(value -> value == effect.getEffectID())) {
                    tableau[2] += 15000 + Formulas.getMiddleJet(effect);
                    continue;
                } else if (Arrays.stream(Constant.SPELLEFFECT_MOUVEMENT).anyMatch(value -> value == effect.getEffectID())) {
                    tableau[5] += 10000 + Formulas.getMiddleJet(effect);
                    continue;
                } else if (Arrays.stream(Constant.SPELLEFFECT_TRAP).anyMatch(value -> value == effect.getEffectID())) {
                    tableau[4] += 5000 + Formulas.getMiddleJet(effect);
                    continue;
                } else if (Arrays.stream(Constant.SPELLEFFECT_USELESS).anyMatch(value -> value == effect.getEffectID())) {
                    tableau[6] += 0;
                    continue;
                } else {
                    System.out.println(effect.getEffectID() + " pas pris en compte dans le calcul car pas typé");
                    continue;
                }
            }

            int idMaxValue = -1;
            int maxValue = Integer.MIN_VALUE;

            for (int id = 0; id < 7; id++) {
                if (tableau[id] > maxValue) {
                    maxValue = tableau[id];
                    idMaxValue = id;
                }
            }
            majorType = idMaxValue;

            this.type = majorType;
        }
        else{
            this.type = this.getSpell().getType();
        }
    }


    public void applySpellEffectToFight(Fight fight, Fighter perso,
                                        GameCase cell, boolean isCC, boolean isTrap) {
        // On rempli avec tous les effets du sortGrade on exclura les CC plus tard
        List<Effect> effets = new ArrayList<Effect>();
        effets.addAll(effectsSpell);
        Effect.applyAllEffectFromList(fight,effets,isCC,perso,cell);
    }


}
