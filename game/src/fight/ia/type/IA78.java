package fight.ia.type;

import common.PathFinding;
import database.dynamics.data.SpellData;
import fight.Fight;
import fight.Fighter;
import fight.ia.AbstractNeedSpell;
import fight.ia.util.Function;
import fight.spells.Spell;
import fight.spells.SpellGrade;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arwase on 10/12/2022.
 */
public class IA78 extends AbstractNeedSpell  {

    public IA78(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count,"IA78");
    }

    @Override
    public void apply() {
        // Difficile de faire une IA 'générique' pour ce mob
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {

            int time = 100, maxPo = 1, minPo = 1; // on set des variables un peu inutile
            boolean action = false; // En vrai on s'en fou (enfaite non c'est pour le timer des spell mais on va s'arranger) qu'il a déjà tapper dans le tour de reflexion ou non, on devrait cumuler les actions

            Map<Spell, SpellGrade> spellsdegatStat = new HashMap<>();

            // Sur tous les sors on prend la PO Max ! ici ca sera 20, Un peu inutile en soi car faudrait l'associé a la PO Min
            for(SpellGrade spellStats : this.highests){
                if(spellStats.getMaxPO() > maxPo){
                    // On prend que les sorts qui tape le reste on s'en fou de la PO
                    if(spellStats.getTypeSwitchSpellEffects() ==0) {
                        maxPo = spellStats.getMaxPO();
                        spellsdegatStat.put(spellStats.getSpell() , spellStats );
                    }
                }
            }

            Fighter L = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 8, maxPo + 1);// pomax +1;
            Fighter C = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, (2+this.fighter.getCurPm(this.fight)) );//3 = po min 1 + 2; car glouton
                // Faut un truc plus complexe pour la distance théorique avec l'attirance mais on verra plus tard

            Fighter Invo = Function.getInstance().getNearestInvoc(this.fight, this.fighter);//2 = po min 1 + 1;

            if(C != null && C.isHide()) C = null; // La c'est normal si c'est invisible ca existe pas
            if(L != null && L.isHide()) L = null; // La c'est normal si c'est invisible ca existe pas
            // Priorité 1, checker si un ennemi est a PM distance de gluton,
                if( C!= null ){
                    if(this.fighter.getDistanceBetween(C) <= 1) { // Si cac on gluton direct,
                        int value = Function.getInstance().attackIfPossible(this.fight, this.fighter, this.cacs);
                        if (value != 0) {
                            time = value;
                            action = true;
                        }
                    }
                    else{  // Si PM alors on bouge puis gluton,
                        if(this.fighter.getCurPm(this.fight) > 0) {
                            int value = Function.getInstance().movecacIfPossible(this.fight, this.fighter, C);
                            if(value != 0) {
                                time = value;
                                action = true;
                            }
                        }
                        if(this.fighter.getDistanceBetween(C) <= 1) {
                            int value = Function.getInstance().attackIfPossible(this.fight, this.fighter, this.cacs);
                            if (value != 0) {
                                time += value;
                                action = true;
                            }
                        }

                    }
                }   // Si le sort est pas dispo on bouge vers ennemi (en ligne ca serai mieux si on gère l'attirance correctement)

                // CA ON VERRA
                // Si en ligne on attire, +PM Ou non et gluton
            // Priorité 2 , Si on a moins de 40% d'HP on heal
            if(this.fighter.getCurPa(this.fight) > 0 && !action) {
                if (Function.getInstance().HealIfPossible(this.fight, this.fighter, true, 40) != 0) {
                    time = 1000;
                    action = true;
                }
            }
            // Priorité 3, Invocation poutch,
            if(this.fighter.getCurPa(this.fight) > 0 && !action ) {
                if (Function.getInstance().invocIfPossible(this.fight, this.fighter, this.invocations)) {
                    time = 2000;
                    action = true;
                }
            }
            // Priorité 4, On boost une invocation si on peut n'importe quel camps
            if( Invo != null && !action ) {
                if(this.fighter.getDistanceBetween(Invo) <= 3   ) {
                    if (this.fighter.getCurPa(this.fight) > 0) {
                        if (Function.getInstance().buffIfPossible(this.fight, this.fighter, Invo, this.buffs)) {
                            time = 400;

                        }
                    }
                }  // Si pas d'invo a coté, on regarde si y'a une invo a PM+2 + portée du spell donc 5
                else if ( this.fighter.getDistanceBetween(Invo) <= 3+this.fighter.getCurPm(this.fight) ) {
                    if(this.fighter.getCurPm(this.fight) > 0) { //Si oui, on se rapproche de l'invo
                        int value = Function.getInstance().moveautourIfPossible(this.fight, this.fighter, Invo);
                        if(value != 0) {
                            time = value;
                        }
                    }
                    if (this.fighter.getCurPa(this.fight) > 0 && !action) {
                        if (Function.getInstance().buffIfPossible(this.fight, this.fighter, Invo, this.buffs)) {
                            time = 400;
                        }
                    }
                }
            }

            // Priorité 5
                    // Dans l'idée faudrait tester si on peut attirer avant donc la c'est que fin de tour
            // On boucle dans les sorts
            if(!action) {
                for (SpellGrade spellstat : spellsdegatStat.values()) {
                    // On boucle sur le nombre de fois
                    Map<Integer, Fighter> EnnemiesInRangeNoMove = Function.getInstance().getXEnnemiesinRange(this.fight, this.fighter, spellstat.getMinPO(), spellstat.getMaxPO(), spellstat.getMaxLaunchbyTurn());

                    if(EnnemiesInRangeNoMove.size() > 0) {
                        time = Function.getInstance().attackallpossibletargetTillEnd(this.fight, this.fighter, EnnemiesInRangeNoMove, spellstat);
                    }
                    else{
                        time = 0;
                    }
                    // On s'approche ou s'éloigne si il nous reste des sort a lancer et qu'il sont dans la zone
                    Map<Integer, Fighter> EnnemiesInRangeMoveForward = Function.getInstance().getXEnnemiesinRange(this.fight, this.fighter, spellstat.getMaxPO() + 1, (spellstat.getMaxPO() + this.fighter.getCurPm(this.fight)), spellstat.getMaxLaunchbyTurn());
                    Map<Integer, Fighter> EnnemiesInRangeMoveBackward = Function.getInstance().getXEnnemiesinRange(this.fight, this.fighter, (spellstat.getMinPO() - this.fighter.getCurPm(this.fight)), spellstat.getMinPO() - 1, spellstat.getMaxLaunchbyTurn());
                    //  On bouge derrière pour tape si plus de cible potentiel
                    if (EnnemiesInRangeMoveForward.size() < EnnemiesInRangeMoveBackward.size()) {
                        if (this.fighter.getCurPm(this.fight) > 0) {
                            int value = Function.getInstance().moveFarIfPossible(this.fight, this.fighter);
                            if (value != 0) {
                                time = 800;
                                action = true;
                            }
                        }
                    } else {
                        if (this.fighter.getCurPm(this.fight) > 0) { //Si oui, on se rapproche d'un gars du groupe
                            int value = Function.getInstance().moveautourIfPossible(this.fight, this.fighter, EnnemiesInRangeMoveForward.get(0));
                            if (value != 0) {
                                time = 2000;
                                action = true;
                            }
                        }
                    }
                }
            }

            // On termine par s'approcher si on a encore des PM
            C = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 40 );
            if(this.fighter.getCurPm(this.fight) > 0 && !action && C.getDistanceBetween(this.fighter) > 1) {
                int value = Function.getInstance().moveautourIfPossible(this.fight, this.fighter, C);
                if (value != 0) {
                    time = 2000;
                }
            }
            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }



}