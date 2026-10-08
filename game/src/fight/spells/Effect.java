package fight.spells;

import area.map.GameCase;
import client.Player;
import common.Formulas;
import common.PathFinding;
import common.SocketManager;
import entity.monster.Monster;
import fight.Fight;
import fight.Fighter;
import fight.traps.Glyph;
import fight.traps.Trap;
import game.GameServer;
import game.world.World;
import kernel.Constant;
import org.apache.commons.lang3.ArrayUtils;
import util.TimerWaiter;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Effect  {
    // Parametre
    private int id; // ID de l'effet
    private int effectID; // ID de l'effet
    private boolean isCCEffect; // Si c'est un effet de CC
    private int effectTarget = 0; // Les targets que c'est sensé touché -
    private String areaEffect; // La zone d'impact de l'effet
    private GameCase cell = null; // La cellule de lancement de l'effet
    private int args1; // Souvent min (fixe quand args2 =-1)
    private int args2; // Souvent max (peut être a -1)
    private int args3; // Souvent information complémentaire (peut etre vide)
    private Fighter caster = null;
    //private int randomJet = 0;
    // Paramètre optionnel
    private String jet; // le jet en dé (peut etre vide) (d'ailleurs on va le déprécier je pense, Inutile)
    private int type = EffectConstant.EFFECT_TYPE_SPELL; // 0 = sort, 1 = Cac, (Ajouter d'autre ??), 2 = consommables
    private int elem=-1; // element de l'effet (-1 : SANS ELEM, 0 : NEUTRE, 1 : TERRE, 2 : EAU, 3 : FEU, 4 : AIR)

    // Pour type Spell
    private boolean isSpell = false;
    private int spellID=-1;  // ID du spell
    private int gradeSpell; // Grade du spell
    private int chanceToLaunch=0;
    private int turn=0;
    public int trigger =-1;
    public EffectTrigger onHitTrigger;  //  A terme faudrait changer le IsOnHitEffect par un 'Declanchement' On aurait 0 = debut de tour, 1= fin de tour, 2 = OnHit (Car y'a des poison en début et en fin en dehors des cas particulier type Nervure)
    private boolean isBuff = false;

    // Pour Cac
    private int weaponID;
    private int weaponType;

    // Pour les buffs
    private boolean isUnbuffable = true;
    private int finalValue = 0;
    private int duration = 0;
    public boolean isNewTurnEffect = false;
    public boolean isEndTurnEffect = false;
    public boolean isDirectEffect = false;
    public boolean isOnHitEffect = false;
    private boolean isPoison = false;

    // Pour les effetsOnHit
    public int impactedTarget = 0; // 0 - l'effetOnHit se lance sur celui qui possède l'effet, 1 - se lance sur celui qui 'proc' l'effet

    // Un constructeur pour les Effets de Spell au chargement de la BDD
    public Effect(int ID, int spellID, int spellGrade,int args1,int args2,int args3,String areaEffect, int chanceTolaunch, int turn, boolean isCCeffet,String jet,int effectTarget,int trigger,int triggerID){
        this.effectID = ID;
        this.jet = jet;
        this.areaEffect = areaEffect;
        this.effectTarget = effectTarget;
        this.spellID = spellID;
        this.gradeSpell = spellGrade;
        this.args1 = args1; // Souvent min (fixe quand args2 =-1)
        this.args2 = args2; // Souvent max
        this.args3 = args3; // Souvent information complémentaire
        this.turn = turn;
        if( turn > 0 )
            this.isBuff = true;

        if(chanceTolaunch != 0 && chanceTolaunch !=100)
            this.chanceToLaunch = chanceTolaunch;

        this.isCCEffect = isCCeffet;
        this.trigger = trigger;
        switch (this.trigger){
            case 1 :
                this.isOnHitEffect = true;
                break;
            case 2 :
                this.isNewTurnEffect = true;
                break;
            case 3 :
                this.isEndTurnEffect = true;
                break;
            case -1 :
            default :
                this.isDirectEffect = true;
                break;
        }

        if(this.isOnHitEffect && triggerID !=-1){
            this.onHitTrigger = World.world.getEffectTrigger(triggerID);
        }
    }

    // Un constructeur pour les Copy
    public Effect(int effectID, int spellID, int spellGrade,int args1,int args2,int args3,String areaEffect, int turn, boolean isCCeffet,String jet,int effectTarget, Fighter caster, int type, int WeaponType, int WeaponID, int chanceTolaunch,int trigger,EffectTrigger onHitTrigger){
        this.effectID = effectID;
        this.jet = jet;
        this.areaEffect = areaEffect;
        this.effectTarget = effectTarget;
        this.spellID = spellID;
        this.gradeSpell = spellGrade;
        this.args1 = args1; // Souvent min (fixe quand args2 =-1)
        this.args2 = args2; // Souvent max
        this.args3 = args3; // Souvent information complémentaire
        this.turn = turn;
        this.duration = turn;
        this.isCCEffect = isCCeffet;
        this.caster = caster;
        this.type = type;
        this.weaponType = WeaponType;
        this.weaponID = WeaponID;
        if( turn > 0 )
            this.isBuff = true;

        if(chanceTolaunch != 0 && chanceTolaunch !=100)
            this.chanceToLaunch = chanceTolaunch;

        this.isCCEffect = isCCeffet;
        this.trigger = trigger;
        switch (this.trigger){
            case 1 :
                this.isOnHitEffect = true;
                break;
            case 2 :
                this.isNewTurnEffect = true;
                break;
            case 3 :
                this.isEndTurnEffect = true;
                break;
            case -1 :
            default :
                this.isDirectEffect = true;
                break;
        }

        if(this.isOnHitEffect && onHitTrigger != null){
            this.onHitTrigger = onHitTrigger;
        }

    }

    // Un constructeur pour les effets d'armes
    public Effect(int effectID, int WeaponID, int WeaponType,String jet, int args1, int args2){
        this.effectID = effectID;
        this.jet = jet;
        this.weaponID = WeaponID;
        this.weaponType = WeaponType;
        this.args1 = args1; // minimum de dégat
        this.args2 = args2; // maximum de dégat
        this.args3 = 0;
        this.isCCEffect = false;
        this.turn = 0;

        //this.areaEffect = PathFinding.getCiblesByZoneByWeapon(this, WeaponType, getMap().getCase(cellID), caster.getCell().getId());
        this.type = EffectConstant.EFFECT_TYPE_CAC;
        // Faire une fonction pour avoir la zone selon l'arme utilisée
        //this.areaEffect = getAreaEffect();
    }

    // Ca c'est un constructeur pour les Objets consommables
	public Effect(int aID, String aArgs, int aSpell, int aSpellLevel,boolean transform) {
		effectID = aID;
		String argsblur = aArgs;
		spellID = aSpell;
		gradeSpell = aSpellLevel;
		turn = 0;
        type = EffectConstant.EFFECT_TYPE_CONSOMMABLE;

		try {
		    if(transform) {
                args1 = Integer.parseInt(argsblur.split(";")[0], 16);
                args2 = Integer.parseInt(argsblur.split(";")[1], 16);
                args3 = Integer.parseInt(argsblur.split(";")[2], 16);
            }
		    else{
                args1 = Integer.parseInt(argsblur.split(";")[0]);
                args2 = Integer.parseInt(argsblur.split(";")[1]);
                args3 = Integer.parseInt(argsblur.split(";")[2]);
            }

            if(argsblur.split(";").length >= 4)
                jet = argsblur.split(";")[3];
		} catch (Exception ignored) {

		}
	}

    // Le fameux constructeur pour les buffs sur les Perso (a voir comment on fait)
    public Effect (int ID, int buffValue, int duration, int args1, int args2, int args3, Fighter caster, int spellID, boolean isUnbuffable){
        this.effectID = ID;
        this.finalValue = buffValue;
        this.spellID = spellID;
        this.args1 = args1; // Souvent min (fixe quand args2 =-1)
        this.args2 = args2; // Souvent max
        this.args3 = args3; // Souvent information complémentaire
        this.duration = duration;
        this.caster = caster;
        this.isUnbuffable = isUnbuffable;
        this.areaEffect = "Pa";
    }

    // Un constructeur pour les buffsOnHit, oui car pas de value c'est un buff caché
    public Effect (int ID, int duration, int args1, int args2, int args3, int chance, Fighter caster, int spellID){
        this.effectID = ID;
        this.spellID = spellID;
        this.args1 = args1; // Souvent min (fixe quand args2 =-1)
        this.args2 = args2; // Souvent max
        this.args3 = args3; // Souvent information complémentaire
        this.chanceToLaunch = chance;
        this.duration = duration;
        this.caster = caster;
    }

    public static List<Effect> CopyAllEffect(List<Effect> effects){
        List<Effect> copy = new ArrayList<>();
        for(Effect effect : effects){
            Effect newEffect = effect.getCopy();
            copy.add(newEffect);
        }
        return copy;
    }

    public Effect getCopy() {
        return new Effect(this.effectID, this.spellID, this.gradeSpell,this.args1,this.args2,this.args3,this.areaEffect, this.turn, this.isCCEffect,this.jet,this.effectTarget, this.caster,this.type,this.weaponType,this.weaponID,this.chanceToLaunch, this.trigger,this.onHitTrigger);
    }

    // Une fonction qui va venir appeler tous les Effects d'un spell 1 par 1
    public static void applyAllEffectFromList(Fight fight,List<Effect> Effets, boolean isCC, Fighter spellCaster, GameCase cellSelected){
        //On calcul la chance au cas ou c'est un sort avec de la chance
        int jetChance = Formulas.getRandomValue(0, 99);

        int curMin = 0;
        int num = 0;

        boolean hasLaunchChanceEffect = false;

        List<EffectApplication> AllImpactedFighter = new ArrayList<>();

        List<Effect> copyOfEffects = CopyAllEffect(Effets);
        // La on fait une map de toutes les cibles qui vont etre touché par les différents effets afin d'eviter le cas d'un déplacement qui va pas appliqué les dégats a la bonne cible.
        for(Effect SE : copyOfEffects){
            SE.cell = cellSelected;
            SE.caster = spellCaster;

            // On cible que les CCs ou non CCs
            ArrayList<Fighter> ImpactedFighterForThisEffect;
            if (SE.type != EffectConstant.EFFECT_TYPE_CAC && (isCC && !SE.IsCCEffet()) || (!isCC && SE.IsCCEffet() ))
                continue;

            // Si le combat est déja terminé osef
            if (fight.getState() >= Constant.FIGHT_STATE_FINISHED)
                return;

            // Si l'effet a une chance de se produire
            if (SE.getChance() != 0 && SE.getChance() != 100)// Si pas 100%
            {
                if ((jetChance <= curMin || jetChance >= (SE.getChance() + curMin)) || hasLaunchChanceEffect) {
                    curMin += SE.getChance();
                    num++;
                    continue;
                }
                hasLaunchChanceEffect = true;
                curMin += SE.getChance();
            }

            if( SE.type != EffectConstant.EFFECT_TYPE_CAC) {
                ArrayList<GameCase> finalCells = SE.getCasesImpactedInArea(fight);
                ImpactedFighterForThisEffect = GameCase.getTargets(finalCells);

                // Cas particulier DO POU on trie les cible dans l'ordre pour etre sur que les effets se passe correctement
                switch (SE.spellID) {
                    case 73://Piége répulsif
                    case 418://Fléche de dispersion
                    case 151://Soufle
                    case 165://FlÃ¨che enflammé
                        ImpactedFighterForThisEffect = SE.trierCibles(ImpactedFighterForThisEffect, fight);
                        break;
                }

            }
            else{
                ImpactedFighterForThisEffect = PathFinding.getCiblesByZoneByWeapon(fight, SE.weaponType, SE.cell, SE.caster.getCell().getId());
            }

                if(ArrayUtils.contains(EffectConstant.NONEEDTARGET_EFFECT,SE.getEffectID())){
                    EffectApplication EA = new EffectApplication(SE);
                    AllImpactedFighter.add(EA);
                }
                else{
                    if(!ImpactedFighterForThisEffect.isEmpty()){
                        EffectApplication EA = new EffectApplication(SE);
                        for (Fighter cible : ImpactedFighterForThisEffect) {
                            EA.addTarget(cible);
                        }
                        AllImpactedFighter.add(EA);
                    }
                }

        }

        // On parcour le tableau précédemment créé pour s'assurer de ne pas perdre d'information
        for (EffectApplication SA : AllImpactedFighter) {
            try {
                Effect test = SA.getEffect();
                int dmge = Formulas.getRandomJet(test.args1, test.args2, test.caster);
                if(!SA.getTargets().isEmpty()) {

                    // Ici on check les challenges
                    ArrayList<Fighter> cibleChall = new ArrayList<>();
                    cibleChall.addAll(SA.getTargets());
                    if (fight.getType() != Constant.FIGHT_TYPE_CHALLENGE && fight.getAllChallenges().size() > 0) {
                        fight.getAllChallenges().values().stream().filter(challenge -> challenge != null).forEach(challenge -> challenge.onFightersAttacked(cibleChall, test.caster, test, test.spellID, false));
                    }

                    // Si le sort a un effet qui s'applique sur toutes les personnes directement (don de vie)
                    if(ArrayUtils.contains(EffectConstant.NEEDALLTARGET_EFFECT,test.getEffectID())){
                        test.applyEffectOnAllTargetDirectly(fight,cibleChall);
                    }
                    else{
                        for (Fighter target : SA.getTargets()) {
                            test.applyEffectOnTarget(fight, target, dmge);

                            // Si la cible est morte après l'effet
                            /*if (target.getPdv() <= 0 && !target.isDead()) {
                                target.setPdv(0);
                                fight.onFighterDie(target, test.caster);
                            }*/
                        }
                    }
                }
                else{
                    // Si le sort a un effet qui s'applique sur toutes les personnes mais en recaultant différentes chose a chaque fois
                    test.applyEffectWithoutTarget(fight,dmge);
                }

                // Si la cible est morte après l'effet
                if (test.caster.getPdv() <= 0 && !test.caster.isDead()) {
                    test.caster.setPdv(0);
                    fight.onFighterDie(test.caster, test.caster);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    // Le switch pour les effets sans cible necessaire
    public void applyEffectWithoutTarget(Fight fight, int dmge){
        switch (this.getEffectID()){
            case 4://Téléportation (Fuite/Bond du félin/ Bond du iop)
                this.applyEffect_4(fight);
                break;
            case 50://Porter
                this.applyEffect_50(fight);
                break;
            case 51://jeter
                this.applyEffect_51(fight);
                break;
            case 109://Dommage pour le lanceur
                this.applyEffect_109(fight,dmge);
                break;
            case 120://Bonus PA au lanceur
                this.applyEffect_AddAP(this.caster, fight,dmge);
                break;
            case 180://Double du sram
                this.applyEffect_180(fight);
                break;
            case 181://Invoque une créature
            case 200: //Contrôle Invocation
                this.applyEffect_181(fight);
                break;
            case 185://Invoque une creature statique
                this.applyEffect_185(fight);
                break;
            case 400://Créer un  piège
                this.applyEffect_400(fight);
                break;
            case 401://Créer une glyphe
            case 402://Glyphe des Blop
                this.applyEffect_402(fight);
                break;
            case 780://laisse spirituelle
                this.applyEffect_780(fight);
                break;
            case 783://Pousse jusqu'a la case visé sans do pou
                this.applyEffect_783(fight);
                break;
            case 784://Raulebaque
                this.applyEffect_784(fight);
                break;
            default:
                GameServer.a("Pas d'effet de spell " + this.effectID + " Dev pour le spell " + this.spellID + " sans target necessaire");
                break;
        }
    }

    // Le switch pour les effets avec toutes les cibles directement necessaire
    public void applyEffectOnAllTargetDirectly(Fight fight, ArrayList<Fighter> cibles){
        switch (this.getEffectID()){
            case 90://Transfert de vie
                applyEffect_90(cibles,fight);
                break;
            default:
                GameServer.a("Pas d'effet de spell " + this.getEffectID() + " Dev pour le spell " + this.spellID + " qui necessite toutes les cibles d'un coup");
                break;
        }
    }

    // L'enorme switch Case pour les différents effet de sorts qui necessite la cible
    public void applyEffectOnTarget(Fight fight, Fighter target, int dmge){
        boolean isCac = false;
        if(this.type == 1){
            isCac = true;
        }

        if(target.isDead())
            return;

        this.elem = EffectConstant.getElemSwitchEffect(this.getEffectID());

        // Cas particuliers si la cible a des effets spécifique (type sacrifice)
        if (ArrayUtils.contains(EffectConstant.IS_DIRECTDAMMAGE_EFFECT, this.effectID) && this.turn == 0) {
            // On débuff invisibilité si c'est un sort de dégat direct
            if (caster.isHide())
                caster.unHide(this.spellID);
            // On gère les cas spécifique de protection de target
            target = getRealTargetSwitchBuff(fight,target);

            if(!this.isPoison) {
                // On gère les cas spécifique type Dérobade
                getRealEffectSwitchBuff(fight, target);
            }

        }
        if(!this.isOnHitEffect) {
            switch (this.getEffectID()) {
                // Les effets de déplacement avec cible
                case 5://Repousse de X case
                    applyEffect_5(target, fight);
                    break;
                case 6://Attire de X case
                    applyEffect_6(target, fight);
                    break;
                case 2127://s'Attire de X case (on verra plus tard pour inverser)
                    applyEffect_6(target, fight);
                    break;
                case 8://Echange les place de 2 joueur
                    applyEffect_8(target, fight);
                    break;
                // Les effets spéciaux de MP
                case 77://Vol de PM
                    applyEffect_77(target, fight, dmge);
                    break;
                case 78://Bonus PM
                    applyEffect_AddMP(target, fight, false, dmge);
                    break;
                case 127://Retrait PM
                    applyEffect_RetMP(target, fight, dmge);
                    break;
                case 128://+PM
                    applyEffect_AddMP(target, fight, true, dmge);
                    break;
                case 169://Perte PM non esquivable
                    applyEffect_169(target, fight, dmge);
                    break;
                // Les effets spéciaux de PA
                case 84://Vol de PA
                    applyEffect_84(target, fight, dmge);
                    break;
                case 101://Retrait PA
                    applyEffect_RetAP(target, fight, dmge);
                    break;
                case 111://+ X PA
                    applyEffect_AddAP(target, fight, dmge);
                    break;
                case 168://Perte PA non esquivable
                    applyEffect_168(target, fight, dmge);
                    break;
                // Les effets de Heal
                case 81:// Cura, PDV devueltos
                    applyEffect_81(target, fight, isCac, dmge);
                    break;
                case 108://Soin
                    applyEffect_108(target, fight, isCac, dmge);
                    break;
                case 143:// PDV rendu (soin sans boost de intel mais juste de +soin ???)
                    applyEffect_143(target, fight, dmge);
                    break;
                // Les effets de dégat
                case 82://Vol de Vie fixe
                    applyEffect_82(target, fight, dmge);
                    break;
                case 85://Dommage Eau %vie
                case 86://Dommage Terre %vie
                case 87://Dommage Air %vie
                case 88://Dommage feu %vie
                case 89://Dommage neutre %vie
                    applyEffect_PerVitaMaxDmg(target, fight, dmge);
                    break;
                case 91://Vol de Vie Eau
                case 92://Vol de Vie Terre
                case 93://Vol de Vie Air
                case 94://Vol de Vie feu
                case 95://Vol de Vie neutre
                    applyEffect_LifeSteal(target, fight, isCac, dmge);
                    break;
                case 96://Dommage Eau
                case 97://Dommage Terre
                case 98://Dommage Air
                case 99://Dommage feu
                case 100://Dommage neutre
                    applyEffect_DamageElem(target, fight, isCac, dmge);
                    break;
                case 275: //Dommages : X% de la vie manquante de l'attaquant (Eau) - A la cellule visé (Jamais utilise)
                case 276: //Dommages : X% de la vie manquante de l'attaquant (Terre) - A la cellule visé (Jamais utilise)
                case 277: //Dommages : X% de la vie manquante de l'attaquant (Air) - A la cellule visé (Jamais utilise)
                case 278: //Dommages : X% de la vie manquante de l'attaquant (feu) - A la cellule visé (Jamais utilise)
                case 279: //Dommages : X% de la vie manquante de l'attaquant (neutre) - A la cellule visé
                    applyEffect_PerVitaLostDmg(target, fight, dmge);
                    break;
                case 671://Dommages : X% de la vie de l'attaquant (neutre) - Juste au lanceur non ???
                    applyEffect_671(target, fight, dmge);
                    break;
                case 672://Dommages : X% de la vie de l'attaquant (neutre) - A la cellule visé
                    applyEffect_672(target, fight);
                    break;
                // Les effets de portée
                case 116://Malus PO
                case 117://Bonus PO
                    applyEffect_PO(target, fight, dmge);
                    break;
                case 320://Vol de PO
                    applyEffect_320(fight, target, dmge);
                    break;
                // Les boost en valeur définie au lancement
                case 105://Dommages réduits de X (Non Elementaire)
                case 110://+ X vie
                case 112://+Dom
                case 114://Multiplie les dommages par X
                case 115://+Cc
                case 118://Bonus force
                case 119://Bonus Agilité
                case 121://+Dom
                case 122://+EC
                case 123://+Chance
                case 124://+Sagesse
                case 125://+Vitalité
                case 126://+Intelligence
                case 138://%dom
                case 142://Dommages physique
                case 144:// - Dommages (pas bosté)
                case 145://Malus Dommage
                case 152:// - Chance
                case 153:// - Vita
                case 154:// - Agi
                case 155:// - Intel
                case 156:// - Sagesse
                case 157:// - Force
                case 160:// + Esquive PA
                case 161:// + Esquive PM
                case 162:// - Esquive PA
                case 163:// - Esquive PM
                case 171://Malus CC
                case 176:// + prospection
                case 177:// - prospection
                case 178:// + soin
                case 179:// - soin
                case 182://+ Crea Invoc
                case 183://Resist Magique
                case 184://Resist Physique
                case 186://Diminue les dommages %
                case 210://Resist % terre
                case 211://Resist % eau
                case 212://Resist % air
                case 213://Resist % feu
                case 214://Resist % neutre
                case 215://Faiblesse % terre
                case 216://Faiblesse % eau
                case 217://Faiblesse % air
                case 218://Faiblesse % feu
                case 219://Faiblesse % neutre
                case 240://Resistance fix terre
                case 241://Resistance fix eau
                case 242://Resistance fix air
                case 243://Resistance fix feu
                case 244://Resistance fix neutre
                case 245://Faiblesse fix terre
                case 246://Faiblesse fix eau
                case 247://Faiblesse fix air
                case 248://Faiblesse fix feu
                case 249://Faiblesse fix neutre
                case 265://Reduit les Dom de X (Elementaire)
                    applyEffect_Buff(target, fight, true, dmge);
                    break;
                // Les effets speciaux
                case 130://Vol de kamas
                    applyEffect_130(fight, target, dmge);
                    break;
                case 131://Poison : X Pdv  par PA
                    applyEffect_131(target);
                    break;
                case 132://Enleve les envoutements
                    applyEffect_132(target, fight);
                    break;
                case 140://Passer le tour
                    applyEffect_140(target);
                    break;
                case 141://Tue la cible
                    applyEffect_141(fight, target);
                    break;
                case 149://Change l'apparence
                    applyEffect_149(fight, target);
                    break;
                case 150://Invisibilité
                    applyEffect_150(fight, target);
                    break;
                case 164:// Daños reducidos en x%
                case 220:// Renvoie dommage
                    applyEffect_BuffMinValue(target);
                    break;
                case 165:// Maîtrises
                    applyEffect_165();
                    break;
                case 202://Perception
                    applyEffect_202(fight, target);
                    break;
                case 266://Vol Chance
                    applyEffect_266(fight, target, dmge);
                    break;
                case 267://Vol vitalité
                    applyEffect_267(fight, target, dmge);
                    break;
                case 268://Vol agitlité
                    applyEffect_268(fight, target, dmge);
                    break;
                case 269://Vol intell
                    applyEffect_269(fight, target, dmge);
                    break;
                case 270://Vol sagesse
                    applyEffect_270(fight, target, dmge);
                    break;
                case 271://Vol force
                    applyEffect_271(fight, target, dmge);
                    break;
                // Tout ca c'est des boost sans valeur, juste un sort avec effet pris en compte plus tard
                case 9://Esquive une attaque en reculant de 1 case
                case 79:// + X chance(%) dommage subis * Y sinon soigné de dommage *Z
                case 106://Renvoie de sort
                case 107://Renvoie de dom
                case 765://Sacrifice
                case 281://Augmente la portée du sort
                case 282://Rend la portée du sort #1 modifiable
                case 283://+#3 de dommages sur le sort #1
                case 284://+#3 de soins sur le sort #1
                case 285://Réduit de #3 le coût en PA du sort #1
                case 286://Réduit de #3 le délai de relance du sort #1
                case 287://#3 aux CC sur le sort #1
                case 288://Désactive le lancer en ligne du sort #1
                case 289://Désactive la ligne de vue du sort #1
                case 290://Augmente de #3 le nombre de lancer maximal par tour du sort #1
                case 291://Augmente de #3 le nombre de lancer maximal par cible du sort #1
                case 292://Fixe à #3 le délai de relance du sort #1
                case 294://Diminue la portée du sort #1 de #3
                case 750://Capture d'ame
                case 776://%erosion
                case 781://Minimize les effets aléatoires
                case 782://Maximise les effets aléatoires
                    applyEffect_SpellBuff(target);
                    break;
                case 293://Boost les dégats d'un sort apres lancement (géré autre part également).
                    applyEffect_SpellBuffUnbuffable(target);
                    break;
                case 405://tue une invoque pour en ajouter une
                    applyEffect_405(target, fight);
                    break;
                case 666://Pas d'effet complémentaire
                    break;
                case 670: // TODO : pas dev mais jamais utilisé par dofus
                    break;
                case 786://EFFET : Soin le caster après l'attaque
                    applyEffect_786(fight);
                    break;
                case 787://Applique un sort sur la cible
                    //applyEffect_787(target, fight);
                    break;
                case 788://Chatiment de X sur Y tours
                    applyEffect_788(target);
                    break;
                case 950://Applique Etat X
                    applyEffect_950(fight, target);
                    break;
                case 951://Enleve Etat X
                    applyEffect_951(fight, target);
                    break;
                default:
                    GameServer.a("Pas d'effet de spell " + effectID + " Dev pour le spell " + spellID);
                    break;
            }
        }
        else{
            // On va créé un nouvel effet sans la condition
            // On créé un buff caché qui se lance quand tapé
            Effect CopySpell = null;
            switch (effectID){
                case 89 :
                    CopySpell = new Effect(this.effectID,this.spellID,this.gradeSpell,this.args1,this.args2,this.args3,this.areaEffect,0,this.isCCEffect,this.jet,this.effectTarget,this.caster,this.type,-1,-1,this.chanceToLaunch,-1,null);
                    target.addBuff(this.effectID,0,this.turn,this.args1,this.args2,this.args3,true,this.spellID,this.caster, true);
                    break;
                case EffectConstant.EFFECTID_PASSTURN :
                    CopySpell = new Effect(this.effectID,this.spellID,this.gradeSpell,this.args1,this.args2,this.args3,this.areaEffect,1,this.isCCEffect,this.jet,this.effectTarget,this.caster,this.type,-1,-1,this.chanceToLaunch,-1,null);
                    break;
                case 108 : // Les soins en OnHit se lance tous directement ?
                    CopySpell = new Effect(this.effectID,this.spellID,this.gradeSpell,this.args1,this.args2,this.args3,this.areaEffect,0,this.isCCEffect,this.jet,this.effectTarget,this.caster,this.type,-1,-1,this.chanceToLaunch,-1,null);
                    break;
                case 950 :
                case 951 :
                    CopySpell = new Effect(this.effectID,this.spellID,this.gradeSpell,this.args1,this.args2,this.args3,this.areaEffect,this.turn,this.isCCEffect,this.jet,this.effectTarget,this.caster,this.type,-1,-1,this.chanceToLaunch,-1,null);
                    break;
                default:
                    CopySpell = new Effect(this.effectID,this.spellID,this.gradeSpell,this.args1,this.args2,this.args3,this.areaEffect,this.turn,this.isCCEffect,this.jet,this.effectTarget,this.caster,this.type,-1,-1,this.chanceToLaunch,-1,null);
                    //target.addBuff(this.effectID,0,this.turn,this.args1,this.args2,this.args3,true,this.spellID,this.caster, true);
                    break;
            }
            int duration = this.turn;
            if(( caster == target) && target.canPlay())
                duration += 1;
            target.addBuffOnHit(this.effectID,this.args1,this.args2,this.args3,this.chanceToLaunch,duration,this.onHitTrigger,CopySpell,this.caster, this.spellID, this.impactedTarget);
            // On rajoute quand meme le buff pour l'avoir dans la barre d'effet
        }

        // Si la cible est morte après l'effet
        if (target.getPdv() <= 0 && !target.isDead()) {
            target.setPdv(0);
            fight.onFighterDie(target, this.caster);
        }

    }

    // Nouvelle fonction pour appliquer les buffs onHit, Donc la on fait un truc qui va juste checker les BuffOnHit rempli dans la liste et si le déclanchement est validé en fonction de l'effect ID lancé
    private void applyEffectAfterHit(Fight fight, Fighter target, Fighter caster, int damageDone) {
        if(this.isPoison) // Les poisons n'appliquent pas les Effet On Hit
            return;

        Collections.sort(target.getOnHitBuff(), Comparator.comparingInt(BuffOnHitEffect::getEffectID));
        Collections.reverse(target.getOnHitBuff());

        // Nouvelle gestion des effect OnHit
        for ( BuffOnHitEffect onHitEffect : target.getOnHitBuff() ) {
            // gestion de la chance de l'effet onHit (Oui ca existe sur le Ougah)
            if (onHitEffect.getChance() != 0 && onHitEffect.getChance() != 100) {
                Random r = new Random();
                int randomNumber = r.nextInt(100) + 1;
                if (randomNumber >= onHitEffect.getChance()) {
                    continue;
                }
            }

            // On check si l'effect_ID est dans les IDs de déclanchement voulu
            if(onHitEffect.getBuffOnHitConditions().getEffectsTriggering().contains(this.effectID)) {
                //System.out.println("Il y en a un " + this.effectID + " / " + onHitEffect.getSpellEffectToApply().getEffectID() );
                Effect toApply = onHitEffect.getSpellEffectToApply();
                switch(onHitEffect.getEffectID()){
                    case 786 : // Arbre de Vie, soin celui qui le tape
                        if(damageDone < 1){
                            continue;
                        }
                        int healFinal = damageDone;
                        int pdvMax = caster.getPdvMax();
                        if ((healFinal + caster.getPdv()) > pdvMax)
                            healFinal = pdvMax - caster.getPdv();
                        if (healFinal < 1)
                            healFinal = 0;

                        caster.removePdv(caster, -healFinal);
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 108, target.getId() + "", caster.getId() + "," + healFinal, this.effectID);
                        break;
                    case 788 :
                        if(damageDone<1){
                            continue;
                        }
                        int taux = (caster.getPlayer() == null ? 1 : 2), gain = damageDone / taux, max = 0;
                        int stat = toApply.getEffectID();
                        try {
                            max = toApply.getArgs1();
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }

                        //on retire au max possible la valeur déjà gagné sur le chati
                        int oldValue = (target.getChatiValue().get(stat) == null ? 0 : target.getChatiValue().get(stat));
                        max -= oldValue;
                        //Si gain trop grand, on le reduit au max
                        if (gain > max) gain = max;

                        if(gain == 0)
                            continue;

                        //On met a jour les valeurs des chatis
                        int newValue = oldValue + gain;

                        if (stat == 108) {
                            // Chatiment vitalesque sans doute a reprendre
                            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, stat, target.getId() + "", target.getId() + "," + gain + "," + 5, elem);
                            target.setPdv(target.getPdv() + gain);
                            if(target.getPlayer() != null) SocketManager.GAME_SEND_STATS_PACKET(target.getPlayer());
                        } else {
                            target.getChatiValue().put(stat, newValue);
                            target.addBuff(stat, gain, toApply.getDuration(), toApply.getArgs1(), -1, -1, true, toApply.getSpell(), target, true);
                            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, stat, target.getId() + "", target.getId() + "," + gain + "," + 5, elem);
                        }
                        break;
                    case 950 : // on ajoute un état
                        int etatId = toApply.getArgs3();
                        target.setState(etatId, toApply.getDuration());
                        target.addBuff(toApply.getEffectID(), etatId, toApply.getDuration(), toApply.getArgs1(), -1, -1, false, toApply.getSpell(), caster, true);
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, onHitEffect.getEffectID(), target.getId() + "", target.getId() + "," + etatId + ",1" );
                        break;
                    case 951 : // on supprime un état
                        if (!target.haveState(toApply.getArgs3()))
                            continue;
                        //on enleve l'�tat
                        int etatId2 = toApply.getArgs3();
                        target.setState(etatId2, 0);
                        target.debuffState(etatId2);
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, target.getId() + "", target.getId() + "," + etatId2 + ",0");
                        break;
                    default :
                        int dmg = Formulas.getRandomJet(onHitEffect.getSpellEffectToApply().getArgs1(),onHitEffect.getSpellEffectToApply().getArgs2(),caster);
                        if(onHitEffect.getBuffOnHitConditions().getTarget() == 1) {
                            //System.out.println("On hit sur target" + onHitEffect.getSpellEffectToApply().effectID);
                            onHitEffect.getSpellEffectToApply().applyEffectOnTarget(fight,caster,dmg);
                        }
                        else {
                            //System.out.println("On hit sur caster" + onHitEffect.getSpellEffectToApply().effectID);
                            onHitEffect.getSpellEffectToApply().applyEffectOnTarget(fight,target,dmg);
                        }
                        break;
                }
            }
        }
    }

    private void applyEffect_PO(Fighter target, Fight fight,int dmg)//Malus PO
    {
        int val = Formulas.getAlteredJet(dmg,args2, target,caster);
        target.addBuff(effectID, val, turn, args1,args2,args3, true, spellID, caster, true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()+ "", target.getId() + "," + val + "," + turn, this.effectID);

        // TODO : on est sur que c'est utile ????
		/*if (target.canPlay() && target == caster)
			target.getTotalStats().addOneStat(effectID, val);*/
    }

    private void applyEffect_320(Fight fight, Fighter target,int dmg) {
        int value = Formulas.getAlteredJet(dmg,args2, target,caster);

        target.addBuff(EffectConstant.STATS_REM_PO, value, turn, args1,args2,args3, true, spellID, caster, true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_REM_PO, caster.getId()+ "", target.getId() + "," + value + "," + turn, this.effectID);

        int num = value;
        if (num != 0) {
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_ADD_PO, caster.getId() + "", caster.getId() + "," + num + "," + turn, this.effectID);
            caster.addBuff(EffectConstant.STATS_ADD_PO, num, turn, args1,args2,args3, true, spellID, caster, true);
            //Gain de PO pendant le tour de jeu
            if (caster.canPlay())
                caster.getTotalStats().addOneStat(EffectConstant.STATS_ADD_PO, num);
        }
    }

    //Buff avec valeur a calculer car variable
    private int applyEffect_Buff(Fighter target, Fight fight,boolean isUnbuffable,int dmg)
    {
        int val = Formulas.getAlteredJet(dmg,args2, target,caster);
        target.addBuff(effectID, val, turn, args1,args2,args3, isUnbuffable, spellID, caster, true);
        sendClientBuff(fight,effectID,val,target.getId(),false);
        return val;
    }

    // On rajouter un buff boost
    private void applyEffect_SpellBuff(Fighter target) {
        target.addBuff(effectID, -1, turn, args1,args2,args3, true, spellID, caster, true);
    }

    // On rajouter un buff boost
    private void applyEffect_SpellBuffUnbuffable(Fighter target) {
        target.addBuff(effectID, -1, turn, args1,args2,args3, false, spellID, caster, true);
    }


    // Teleportation
    private void applyEffect_4(Fight fight) { // Teleportation
        if (cell.isWalkable(true) && !fight.isOccuped(cell.getId()))//Si la case est prise, on va �viter que les joueurs se montent dessus *-*
        {
            caster.getCell().getFighters().clear();
            caster.setCell(cell);
            caster.getCell().addFighter(caster);
            this.sendClientAction(fight,caster.getId(),caster.getId(),cell.getId(),this.effectID);
            this.checkTraps(fight, caster, (short) 1200);
        } else {
            GameServer.a("Case déjà occupé");
        }
        return;
    }

    // On pousse
    private void applyEffect_5(Fighter target, Fight fight) {
        // si c'est une invo statique ca bouge pas
        if (target.getMob() != null){
            if(ArrayUtils.contains(Constant.STATIC_INVOCATIONS,target.getMob().getTemplate().getId()))
                return;
        }

        // Si on a l'état enraciné on bouge pas
        if (target.haveState(EffectConstant.ETAT_ENRACINE))
            return;

        GameCase cell = this.cell;
        if (target.getCell().getId() == this.cell.getId() || spellID == 73)
            cell = caster.getCell();

        int newCellId = PathFinding.newCaseAfterPush(fight, cell, target.getCell(), args1);

        if (newCellId == 0)
            return;

        if (newCellId < 0) {
            int a = -newCellId, factor = Formulas.getRandomJet(Constant.DO_POU_DOMMAGE);
            double b = (caster.isInvocation() ? caster.getInvocator().getLvl() : caster.getLvl()) / 50;
            if (b < 0.1) b = 0.1;

            int finalDmg = (int) (factor * b * a);

            if (finalDmg < 1) finalDmg = 1;
            if (finalDmg > target.getPdv()) finalDmg = target.getPdv();

            if (target.hasBuff(184)) {
                finalDmg = finalDmg - target.getBuff(184).getFixvalue();//Réduction physique
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(184).getFixvalue());
            }
            if (target.hasBuff(105)) {
                finalDmg = finalDmg - target.getBuff(105).getFixvalue();//Immu
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(105).getFixvalue());
            }
            if (finalDmg > 0) {
                if(finalDmg > 200) finalDmg = Formulas.getRandomValue(189, 211);
                target.removePdv(caster, finalDmg);
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId() + "", target.getId() + ",-" + finalDmg);
                if (target.getPdv() <= 0) {
                    fight.onFighterDie(target, caster);
							/*if (target.canPlay() && target.getPlayer() != null) fight.endTurn(false);
							else if (target.canPlay()) target.setCanPlay(false);*/
                    return;
                }
            }
            a = args1 - a;
            newCellId = PathFinding.newCaseAfterPush(fight, cell, target.getCell(), a);

            char dir = PathFinding.getDirBetweenTwoCase(cell.getId(), target.getCell().getId(), fight.getMap(), true);
            GameCase nextCase = fight.getMap().getCase(PathFinding.GetCaseIDFromDirrection(target.getCell().getId(), dir, fight.getMap(), true));

            if (nextCase != null && nextCase.getFirstFighter() != null) {
                Fighter wallTarget = nextCase.getFirstFighter();
                finalDmg = finalDmg / 2;
                if (finalDmg < 1) finalDmg = 1;
                if (finalDmg > 0) {
                    wallTarget.removePdv(caster, finalDmg);
                    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId() + "", wallTarget.getId() + ",-" + finalDmg);
                    if (wallTarget.getPdv() <= 0)
                        fight.onFighterDie(wallTarget, caster);
                }
            }

            if (newCellId == 0)
                return;
            if (fight.getMap().getCase(newCellId) == null)
                return;
        }

        target.getCell().getFighters().clear();
        target.setCell(fight.getMap().getCase(newCellId));
        target.getCell().addFighter(target);

        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, caster.getId() + "", target.getId() + "," + newCellId);
        Trap.doTraps(fight, target);
        //this.cell = target.getCell();

        return;
    }

    // On attire
    private void applyEffect_6(Fighter target, Fight fight) {

        if (target.haveState(EffectConstant.ETAT_ENRACINE))
            return;

        GameCase eCell = cell;
        //Si meme case
        if (target.getCell().getId() == cell.getId()) {
            //on prend la cellule caster
            eCell = caster.getCell();
        }
        int newCellID = PathFinding.newCaseAfterPush(fight, eCell, target.getCell(), -args1);
        if (newCellID == 0)
            return ;

        if (newCellID < 0)//S'il a �t� bloqu�
        {
            int a = -(args1 + newCellID);
            newCellID = PathFinding.newCaseAfterPush(fight, caster.getCell(), target.getCell(), a);
            if (newCellID == 0)
                return ;

            if (fight.getMap().getCase(newCellID) == null)
                return ;
        }

        target.getCell().getFighters().clear();
        target.setCell(fight.getMap().getCase(newCellID));
        target.getCell().addFighter(target);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, caster.getId() + "", target.getId() + "," + newCellID);
        this.checkTraps(fight, target, (short) 1500);
        //this.cell = target.getCell();
        return ;
    }

    // On échange de place
    private void applyEffect_8(Fighter target, Fight fight) {

        if (target == null)
            return;//ne devrait pas arriver
        if (target.haveState(EffectConstant.ETAT_ENRACINE))
            return;//Stabilisation
        if (caster.haveState(EffectConstant.ETAT_PESANTEUR))
            return;//Pesanteur
        if (target.haveState(EffectConstant.ETAT_PESANTEUR))
            return;//Pesanteur
        if (target.haveState(EffectConstant.ETAT_PORTE)) {
            if(caster.getPlayer()!= null) {
                caster.getPlayer().sendMessage("Il n'est pas possible d'utiliser ce sort avec quelqu'un qui est dans l'état porté");
            }
            return;
        }
        if (target.haveState(EffectConstant.ETAT_PORTEUR)) {
            if(caster.getPlayer()!= null) {
                caster.getPlayer().sendMessage("Il n'est pas possible d'utiliser ce sort avec quelqu'un qui est dans l'état porteur");
            }
            return;
        }
        if (caster.haveState(EffectConstant.ETAT_PORTE)){
            if(caster.getPlayer()!= null) {
                caster.getPlayer().sendMessage("Il n'est pas possible d'utiliser ce sort car tu es dans l'état porté");
            }
            return;
        }
        if (caster.haveState(EffectConstant.ETAT_PORTEUR)){
            if(caster.getPlayer()!= null) {
                caster.getPlayer().sendMessage("Il n'est pas possible d'utiliser ce sort car tu es dans l'état porteur");
            }
            return;
        }

        //on enleve les persos des cases
        target.getCell().getFighters().clear();
        caster.getCell().getFighters().clear();
        //on retient les cases
        GameCase exTarget = target.getCell();
        GameCase exCaster = caster.getCell();
        //on �change les cases
        target.setCell(exCaster);
        caster.setCell(exTarget);
        //on ajoute les fighters aux cases
        target.getCell().addFighter(target);
        caster.getCell().addFighter(caster);

        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, caster.getId()
                + "", target.getId() + "," + exCaster.getId());
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, caster.getId()
                + "", caster.getId() + "," + exTarget.getId());

        this.checkTraps(fight, target, (short) 1500);
        this.checkTraps(fight, caster, (short) 1500);
        //this.cell = target.getCell();
        return;
    }

    // Dérobade
    private void applyEffect_9(Fighter target) {
        target.addBuff(effectID, -1, turn, args1,args2,args3, true, spellID, caster, true);
    }

    // Porter
    private void applyEffect_50(Fight fight) {
        //Porter
        Fighter target = cell.getFirstFighter();
        if (target == null) return;
        if (target.getMob() != null)
            for (int i : Constant.STATIC_INVOCATIONS)
                if (i == target.getMob().getTemplate().getId())
                    return;
        if (target.haveState(6)) return;//Stabilisation

        //on enleve le porté de sa case
        target.getCell().getFighters().clear();
        //on lui définie sa nouvelle case
        target.setCell(caster.getCell());

        //on applique les états
        target.setState(EffectConstant.ETAT_PORTE, 1);
        caster.setState(EffectConstant.ETAT_PORTEUR, 1);
        //on lie les 2 Fighter
        target.setHoldedBy(caster);
        caster.setIsHolding(target);

        //on envoie les packets
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 50, caster.getId() + "", "" + target.getId(), this.effectID);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, target.getId() + "", target.getId() + "," + EffectConstant.ETAT_PORTE + ",1");
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId() + "", caster.getId() + "," + EffectConstant.ETAT_PORTEUR + ",1");
        TimerWaiter.addNext(() -> fight.setCurAction(""), 1500, TimeUnit.MILLISECONDS);
    }

    // Jeter
    private void applyEffect_51(Fight fight) {
        //Si case pas libre
        if (!cell.isWalkable(true) || cell.getFighters().size() > 0) return;
        Fighter target = caster.getIsHolding();
        if (target == null) return;

        //on ajoute le porté a sa case
        target.setCell(cell);
        target.getCell().addFighter(target);

        //on enleve les états
        target.setState(EffectConstant.ETAT_PORTE,0,caster.getId()); //infinite duration
        caster.setState(EffectConstant.ETAT_PORTEUR,0,caster.getId()); //infinite duration

        //on dé-lie les 2 Fighter
        target.setHoldedBy(null);
        caster.setIsHolding(null);

        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 51, caster.getId() + "", cell.getId() + "");
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, target.getId() + "", target.getId() + "," + EffectConstant.ETAT_PORTE + ",0");
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId() + "", caster.getId() + "," + EffectConstant.ETAT_PORTEUR + ",0");

        this.checkTraps(fight, target, (short) 500);
    }

    // Vol PM - Fini !
    private void applyEffect_77(Fighter target, Fight fight, int dmge) {
        int value = applyEffect_RetMP(target,fight,dmge);

        if (value < 1)
            return;

        if (value != 0) {
            if(turn != 0)
                caster.addBuff(EffectConstant.STATS_ADD_PM2, value, turn, args1,args2,args3, false, spellID, caster, false);

            sendClientBuff(fight,EffectConstant.STATS_ADD_PM2,value,caster.getId(),false);
            if (caster.canPlay())
                caster.setCurPm(fight, value);
        }
    }

    //Bonus PM (debuffable) (Presque pas utilisé) - Fini !
    private void applyEffect_AddMP(Fighter target, Fight fight,boolean isUnbuffable,int dmg) {
        int val = applyEffect_Buff(target,fight,isUnbuffable, dmg);
        if (target.canPlay())
            target.setCurPm(fight, val);

        //sendClientBuff(fight,effectID,val,target.getId(),false);
    }

    //Retrait PM (debuffable) - Fini !
    private int applyEffect_RetMP(Fighter target, Fight fight,int dmg) {
        int value = Formulas.getAlteredJet(dmg,args2, target,caster);
        int retrait = Formulas.getPointsLost('m', value, caster, target);

        if ((value - retrait) > 0)
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 309, caster.getId()+ "", target.getId() + "," + (value - retrait), this.effectID);

        if (retrait <= 0)
            return 0;

        if(turn != 0) {
            target.addBuff(effectID, retrait, turn, args1, args2, args3, true, spellID, caster, true);
            sendClientBuff(fight, EffectConstant.STATS_REM_PM, retrait, target.getId(), true);
        }
        else{
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_REM_PM, caster.getId()+ "", target.getId() + "," + -(retrait), this.effectID);
            // Si le prochain tour du joueurs c'est le tour actuel on le place directement,
            if(fight.getFighterByOrdreJeu() == target) {
                target.setCurPm(fight, -retrait);
            }
            else{ // sinon on le prépare pour son debut de prochain tour prochain
                target.setCurRemovedPm(target.getCurRemovedPm() + retrait);
            }
        }

        applyEffectAfterHit(fight,target,caster,retrait);
        return value;
    }

    // Refait a tester // - PA, no esquivables
    private void applyEffect_168(Fighter cible, Fight fight,int dmg) {
        int value = Formulas.getAlteredJet(dmg,args2, cible,caster);
        cible.addBuff(effectID, value, turn, args1,args2,args3, true, spellID, caster, true);

        //if (turn <= 1 )
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_REM_PA, cible.getId()+ "", cible.getId() + ",-" + value, this.effectID);

        if (fight.getFighterByOrdreJeu() == cible)
            fight.setCurFighterPa(fight.getCurFighterPa() - value);

        applyEffectAfterHit(fight,cible,caster,value);
    }

    // Refait a tester // - PM, no esquivables
    private void applyEffect_169(Fighter cible, Fight fight,int dmg) {
        int value = Formulas.getAlteredJet(dmg,args2, cible,caster);

        // Si c'est un buff c'est pour plusieurs tours ou infini si -1 (et potentiellement débuffable)
        if (turn != 0 ) {
            cible.addBuff(effectID, value, turn, args1, args2, args3, true, spellID, caster, true);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_REM_PM, cible.getId() + "", cible.getId() + ",-" + value, this.effectID);
        } // Si c'est juste pour le prochain tour du joueur,
        else{
            // Si le prochain tour du joueurs c'est le tour actuel on le place directement,
            if(fight.getFighterByOrdreJeu() == cible) {
                fight.setCurFighterPm(fight.getCurFighterPm() - value);
            }
            else{ // sinon on le prépare pour son debut de prochain tour prochain
                cible.setCurRemovedPm(cible.getCurRemovedPm() + value);
            }
        }

        // C'est c'est juste qu'on vérifie si la cible a un effet on Hit qui proc sur du retrait PM potentiellement
        applyEffectAfterHit(fight,cible,caster,value);
    }

    // Heal (PV rendu)
    private void applyEffect_81(Fighter target, Fight fight, boolean isCaC,int dmg) {
        if (turn == 0) {
            int heal = Formulas.getAlteredJet(dmg,args2, target,caster);

            // Why ??????   heal = getMaxMinSpell(cible, heal);
            int pdvMax = target.getPdvMax();
            int healFinal = Formulas.calculFinalHeal(caster, heal, isCaC,spellID);

            if ((healFinal + target.getPdv()) > pdvMax)
                healFinal = pdvMax - target.getPdv();
            if (healFinal < 1)
                healFinal = 0;

            target.addPdv(caster, healFinal);
            sendClientHeal(fight,target.getId(),healFinal);
            applyEffectAfterHit(fight,target,caster,healFinal);
        } else {
            target.addBuff(effectID, 0, turn, args1,args2,args3, true, spellID, caster, true);
        }
    }

    // Vol de vie (valeur fixe)
    private void applyEffect_82(Fighter target, Fight fight,int dmg) {
        if (turn == 0) {
            int finalDommage = Formulas.getAlteredJet(dmg,args2, target,caster);
            //finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constant.ELEMENT_NULL);//S'il y a des buffs sp�ciaux
            if (finalDommage > target.getPdv())
                finalDommage = target.getPdv();//Target va mourrir

            target.removePdv(caster, finalDommage);
            sendClientDamage(fight,target.getId(),finalDommage);

            applyEffectAfterHit(fight,target,caster,finalDommage);

            //Vol de vie
            int heal = Math.abs(finalDommage) / 2;
            if ((caster.getPdv() + heal) > caster.getPdvMax())
                heal = caster.getPdvMax() - caster.getPdv();

            caster.addPdv(caster, heal);
            sendClientHeal(fight,caster.getId(),heal);
        } else {
            target.addBuff(effectID, 0, turn, args1,args2,args3, true, spellID, caster, true);
        }
    }

    // Vol PA corrigé !
    private void applyEffect_84(Fighter target, Fight fight,int dmg) {
        int value = Formulas.getAlteredJet(dmg,args2, target,caster);
        int val = Formulas.getPointsLost('a', value, caster, target);
        if (val < value)
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 308, caster.getId() + "", target.getId() + "," + (value - val), this.effectID);

        if (val < 1)
            return;

        if(turn != 0) {
            target.addBuff(effectID, val, turn, args1, args2, args3, true, spellID, caster, true);
            sendClientBuff(fight, EffectConstant.STATS_ADD_PA, val, target.getId(), true);
        }
        else{
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_REM_PA, target.getId() + "", target.getId() + ",-" + val, this.effectID);
            // Si le prochain tour du joueurs c'est le tour actuel on le place directement,
            if(fight.getFighterByOrdreJeu() == target) {
                fight.setCurFighterPa(fight.getCurFighterPm() - value);
            }
            else{ // sinon on le prépare pour son debut de prochain tour prochain
                target.setCurRemovedPa(target.getCurRemovedPa() + value);
            }
        }

        applyEffectAfterHit(fight,target,caster,val);

        int num = val;
        if (num >= 1) {
            sendClientBuff(fight,EffectConstant.STATS_ADD_PA2,val,caster.getId(),false);
            //SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 120, caster.getId() + "", caster.getId() + "," + num + "," + turns, this.effectID);

            caster.addBuff(EffectConstant.STATS_ADD_PA2, num, turn, args1,args2,args3, true, spellID, caster, true);

            //Gain de PA pendant le tour de jeu
            if (caster.canPlay())
                caster.setCurPa(fight, num);
        }
    }

    // Dommage %vita (Type Furie, Mot Stimulant)
    private void applyEffect_PerVitaMaxDmg(Fighter target, Fight fight, int dmge) {
        if (turn == 0) {
            int resP =0;
            int resF =0;

            // Gestion des rési selon l'element
            switch (elem){
                case EffectConstant.ELEMENT_NEUTRE: // Neutre
                    resP = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_NEU);
                    resF = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_NEU);
                    if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
                    {
                        resP += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_PVP_NEU);
                        resF += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_PVP_NEU);
                    }
                    break;
                case EffectConstant.ELEMENT_TERRE : // Terre
                    resP = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_TER);
                    resF = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_TER);
                    if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
                    {
                        resP += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_PVP_TER);
                        resF += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_PVP_TER);
                    }
                    break;
                case EffectConstant.ELEMENT_EAU:  // ELEMENT_EAU
                    resP = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_EAU);
                    resF = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_EAU);
                    if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
                    {
                        resP += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_PVP_EAU);
                        resF += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_PVP_EAU);
                    }
                    break;
                case EffectConstant.ELEMENT_FEU : //
                    resP = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_FEU);
                    resF = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_FEU);
                    if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
                    {
                        resP += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_PVP_FEU);
                        resF += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_PVP_FEU);
                    }
                    break;
                case EffectConstant.ELEMENT_AIR:
                    resP = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_AIR);
                    resF = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_AIR);
                    if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
                    {
                        resP += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_PVP_AIR);
                        resF += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_PVP_AIR);
                    }
                    break;
                default:
                    break;

            }

            int dmg = Formulas.getAlteredJet(dmge,args2, target,caster);//%age de pdv inflig�
            int val = Math.round(caster.getPdv() / 100 * dmg);//Valeur des d�gats
            //retrait de la r�sist fixe
            val -= resF;
            int reduc = (int) (((float) val) / (float) 100) * resP;//Reduc %resis
            val -= reduc;
            int armor = 0;

            // On calcul ici les reduction TODO a revoir c'est chelou
            for (Effect SE : target.getBuffsByEffectID(EffectConstant.EFFECTID_REDUCEDAMAGE)) {
                int intell = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_INTE);
                int carac = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_FORC);
                int value = SE.getFixvalue();
                int a = value* (100 + (int) (intell / 2) + (int) (carac / 2))/ 100;
                armor += a;
            }



            if (armor > 0) {
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.EFFECTID_REDUCEDAMAGE, caster.getId()+ "", target.getId() + "," + armor, this.effectID);
                val = val - armor;
            }
            if (val < 0)
                val = 0;



            if (val > target.getPdv())
                val = target.getPdv();//Target va mourrir

            target.removePdv(target, val);
            sendClientDamage(fight,target.getId(),val);

        } else {    // Est-ce que ca au final c'est pas que des OnHit Et je crois que oui donc on va faire ca plutot
            target.addBuff(effectID, args1, turn, args1,args2,args3, true, spellID, caster, true);//on applique un buff
        }
    }

    // Dommage %vita pour le lanceur (Type Furie, Mot Stimulant)
    private void applyEffect_PerVitaLostDmg(Fighter target, Fight fight, int dmge) {
        if (turn == 0) {
            int resP =0;
            int resF =0;

            // Gestion des rési selon l'element
            switch (elem){
                case EffectConstant.ELEMENT_NEUTRE: // Neutre
                    resP = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_NEU);
                    resF = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_NEU);
                    if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
                    {
                        resP += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_PVP_NEU);
                        resF += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_PVP_NEU);
                    }
                    break;
                case EffectConstant.ELEMENT_TERRE : // Terre
                    resP = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_TER);
                    resF = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_TER);
                    if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
                    {
                        resP += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_PVP_TER);
                        resF += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_PVP_TER);
                    }
                    break;
                case EffectConstant.ELEMENT_EAU:  // ELEMENT_EAU
                    resP = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_EAU);
                    resF = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_EAU);
                    if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
                    {
                        resP += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_PVP_EAU);
                        resF += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_PVP_EAU);
                    }
                    break;
                case EffectConstant.ELEMENT_FEU : //
                    resP = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_FEU);
                    resF = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_FEU);
                    if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
                    {
                        resP += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_PVP_FEU);
                        resF += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_PVP_FEU);
                    }
                    break;
                case EffectConstant.ELEMENT_AIR:
                    resP = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_AIR);
                    resF = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_AIR);
                    if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
                    {
                        resP += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_PVP_AIR);
                        resF += target.getTotalStats().getEffect(EffectConstant.STATS_ADD_R_PVP_AIR);
                    }
                    break;
                default:
                    break;

            }

            //int dmg = Formulas.getAlteredJet(dmge,args2, target);//%age de pdv inflig�
            int val = Math.round( (caster.getPdvMax() - caster.getPdv()) / 100 * dmge);//Valeur des d�gats
            //retrait de la r�sist fixe
            val -= resF;
            int reduc = (int) (((float) val) / (float) 100) * resP;//Reduc %resis
            val -= reduc;
            int armor = 0;

            // On calcul ici les reduction TODO a revoir c'est chelou
            for (Effect SE : target.getBuffsByEffectID(EffectConstant.EFFECTID_REDUCEDAMAGE)) {
                int intell = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_INTE);
                int carac = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_FORC);
                int value = SE.getFixvalue();
                int a = value* (100 + (int) (intell / 2) + (int) (carac / 2))/ 100;
                armor += a;
            }

            if (armor > 0) {
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.EFFECTID_REDUCEDAMAGE, caster.getId()+ "", target.getId() + "," + armor, this.effectID);
                val = val - armor;
            }
            if (val < 0)
                val = 0;



            if (val > target.getPdv())
                val = target.getPdv();//Target va mourrir

            target.removePdv(target, val);
            sendClientDamage(fight,target.getId(),val);

        } else {    // Est-ce que ca au final c'est pas que des OnHit Et je crois que oui donc on va faire ca plutot
            target.addBuff(effectID, args1, turn, args1,args2,args3, true, spellID, caster, true);//on applique un buff
        }
    }


    // Don de vie
    private void applyEffect_90(ArrayList<Fighter> cibles, Fight fight) {

        int value = Formulas.getRandomJet(args1,args2,caster);
        int val = Math.round(value * (caster.getPdv() / 100));

        //Calcul des Doms recus par le lanceur
        int finalDommage = val;//S'il y a des buffs sp�ciaux

        if (finalDommage > caster.getPdv())
            finalDommage = caster.getPdv();//Caster va mourrir

        caster.removePdv(caster, finalDommage);

        finalDommage = -(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()+ "", caster.getId() + "," + finalDommage, this.effectID);

        //Application du soin
        for (Fighter target : cibles) {
            if(target.isDead())
                continue;
            // Si le soin est au dessus
            if ((val + target.getPdv()) > target.getPdvMax())
                val = target.getPdvMax() - target.getPdv();


            target.addPdv(caster, val);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId() + "", target.getId() + ",+" + val, 108);
            //applyEffectAfterHit(fight,target,caster,finalDommage);
        }



        // On tue le lanceur si il avait plus assez de vie ? (étrange car c'est impossible on prend un pourcentage de la vie actuel)
        if (caster.getPdv() <= 0)
            fight.onFighterDie(caster, caster);

    }

    // Du soin fixe qui ne prend en compte que les bonus /malus en soin (sans l'intel)
    private void applyEffect_143(Fighter cible, Fight fight,int dmge) {
        if (turn <= 0) {
            int val = Formulas.getAlteredJet(dmge,args2, cible,caster);
            if(val == -1)return;

            int heals = caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_SOIN) - caster.getTotalStats().getEffect(EffectConstant.STATS_REM_SOIN);
            int healfinal = val + heals;
            if (healfinal < 1)
                healfinal = 0;
            if ((healfinal + cible.getPdv()) > cible.getPdvMax())
                healfinal = cible.getPdvMax() - cible.getPdv();
            if (healfinal < 1)
                healfinal = 0;

            cible.addPdv(caster, healfinal);
            sendClientHeal(fight,cible.getId(),healfinal);
            applyEffectAfterHit(fight,cible,caster,healfinal);

        } else {
            cible.addBuff(effectID, 0, turn, args1,args2,args3, true, spellID, caster, true);//on applique un buff
        }
    }

    //Vol de vie (tout elem confondu)
    private void applyEffect_LifeSteal(Fighter target, Fight fight,
                                       boolean isCaC,int dmge) {
        if (!isBuff) {
            int dmg = Formulas.getAlteredJet(dmge,args2, target,caster);
            int finalDommage = this.calculFinalDommage(fight, caster, target, this.elem, dmg, false, isCaC, spellID);
            finalDommage = applySpecificCasesBeforeDamage(finalDommage,target,caster,fight,this.elem);
            //Peut etre du Heal si Chance d'eca du coup

            if(finalDommage >= 0) {
                if (finalDommage > target.getPdv())
                    finalDommage = target.getPdv();//Target va mourrir

                target.removePdv(caster, finalDommage);
                sendClientDamage(fight, target.getId(), finalDommage);

                int steal = Math.round(finalDommage/2);
                if ((steal + caster.getPdv()) > caster.getPdvMax())
                    steal = caster.getPdvMax() - caster.getPdv();

                sendClientHeal(fight, caster.getId(), steal);
                caster.addPdv(caster, steal);
            }

            applyEffectAfterHit(fight,target,caster,finalDommage);

        } else {
            // valeur pas déterminée a l'avance a calculer a chaque proc (degat x à X pendant x tour)
            target.addBuff(effectID, 0, turn, args1,args2,args3, true, spellID, caster,true);//on applique un buff
        }
    }

    //Attaque elementaire basique
    private void applyEffect_DamageElem(Fighter target, Fight fight,
                                        boolean isCaC,int dmge)
    {
        if (turn == 0) {
            int dmg = Formulas.getAlteredJet(dmge,args2, target,caster);
            int finalDommage = this.calculFinalDommage(fight, this.caster, target, this.elem, dmg, false, isCaC, spellID);
            finalDommage = applySpecificCasesBeforeDamage(finalDommage,target,this.caster,fight,this.elem);

            if(finalDommage >= 0) {
                if (finalDommage > target.getPdv())
                    finalDommage = target.getPdv();//Target va mourrir
                target.removePdv(caster, finalDommage);
                sendClientDamage(fight, target.getId(), finalDommage);

            }

            applyEffectAfterHit(fight,target,this.caster,finalDommage);

        } else {
            target.addBuff(this.effectID, 0, this.turn, this.args1,this.args2,this.args3, true, this.spellID, this.caster,true);//on applique un buff
        }
    }

    private void applyEffect_RetAP(Fighter target, Fight fight,int dmge) {
        int value = Formulas.getAlteredJet(dmge,args2, target,caster);
        int remove = Formulas.getPointsLost('a', value, caster, target);

        if ((value - remove) > 0)
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 308, caster.getId() + "", target.getId() + "," + (value - remove), this.effectID);

        if(remove < 0)
            return;



        if(turn != 0) {
            target.addBuff(effectID, remove, turn, args1, args2, args3, true, spellID, caster, true);
            sendClientBuff(fight, EffectConstant.STATS_REM_PA, remove, target.getId(), true);
        }
        else{
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_REM_PA, target.getId() + "", target.getId() + ",-" + remove, this.effectID);
            // Si le prochain tour du joueurs c'est le tour actuel on le place directement,
            if(fight.getFighterByOrdreJeu() == target) {
                fight.setCurFighterPa(fight.getCurFighterPa() - remove);
            }
            else{ // sinon on le prépare pour son debut de prochain tour prochain
                target.setCurRemovedPa(target.getCurRemovedPa() + remove);
            }
        }

        applyEffectAfterHit(fight,target,caster,remove);
    }

    // Heal  TODO : Why ????   if (isCaC) return;
    private void applyEffect_108(Fighter cible, Fight fight, boolean isCaC,int dmge) {
        if (turn <= 0) {
            int heal = Formulas.getAlteredJet(dmge,args2, cible,caster);

            // Why ??????   heal = getMaxMinSpell(cible, heal);
            int pdvMax = cible.getPdvMax();
            int healFinal = Formulas.calculFinalHeal(caster, heal, isCaC,spellID);

            if ((healFinal + cible.getPdv()) > pdvMax)
                healFinal = pdvMax - cible.getPdv();
            if (healFinal < 1)
                healFinal = 0;
            cible.removePdv(caster, -healFinal);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 108, caster.getId() + "", cible.getId() + "," + healFinal, this.effectID);
            applyEffectAfterHit(fight,cible,caster,healFinal);
        } else {
            cible.addBuff(effectID, 0, turn, args1,args2,args3, true, spellID, caster,true);//on applique un buff
        }
    }

    //Dommage pour le lanceur (pas tout le temps fixe) - Ca n'existe pas en buff
    private void applyEffect_109(Fight fight,int dmge)
    {
            int dmg = Formulas.getAlteredJet(dmge,args2, caster,caster);
            int finalDommage = calculFinalDommage(fight, caster, caster, -1, dmg, false, false, spellID);
            //finalDommage = applyOnHitBuffs(finalDommage, caster, caster, fight, Constant.ELEMENT_NULL);//S'il y a des buffs sp�ciaux

            if (finalDommage > caster.getPdv())
                finalDommage = caster.getPdv();//Caster va mourrir
            caster.removePdv(caster, finalDommage);

            sendClientDamage(fight, caster.getId(), finalDommage);

    }

    // Vol de kamas ca n'existe pas en buff
    private void applyEffect_130(Fight fight, Fighter target,int dmge) {
        int kamas = Formulas.getAlteredJet(dmge,args2, target,caster);
        if (caster.getPlayer() == null) return;

        if (target.getPlayer() != null) {
            target.getPlayer().addKamas(-kamas);
            if (target.getPlayer().getKamas() < 0)
                target.getPlayer().setKamas(0);
        }
        else {
            if (target.getMob() != null) {
                return;
            }

            if (target.isInvocation()) {
                return;
            }
        }

        caster.getPlayer().addKamas(kamas);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 130, caster.getId() + "", kamas + "", this.effectID);
    }

    // Poison : X Pdv  par PA
    private void applyEffect_131(Fighter target) {
        target.addBuff(effectID, args1, turn, args1,args2,args3, true, spellID, caster, true);
        //target.addBuff(effectID, minvalue, turns, 1, true, spell, args, caster, true);
    }

    // On Unbuff
    private void applyEffect_132(Fighter target, Fight fight) {
        target.debuff();
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 132, caster.getId() + "", target.getId() + "", this.effectID);
        target.rebuff();
        //target.toRebuff =true;
    }

    // Passe le prochain tour
    private void applyEffect_140(Fighter target) {
        target.addBuff(effectID, 0, 1, args1,args2,args3, false, spellID, caster, true);
        //target.addBuff(effectID, 0, 1, 1, true, spell, args, caster, true);
    }

    // On tue tout simplement
    private void applyEffect_141(Fight fight, Fighter target) {
        fight.onFighterDie(target, caster);
    }

    // Change l'apparence
    private void applyEffect_149(Fight fight, Fighter target) {
        int id = -1;

        try {
            id = args3;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (spellID == 686) {
            if (target.getPlayer() != null
                    && target.getPlayer().getSexe() == 1
                    || target.getMob() != null
                    && target.getMob().getTemplate().getId() == 547)
                id = 8011;
        }

        if (id == -1)
            id = target.getDefaultGfx();

        target.addBuff(effectID, id, turn, args1,args2,args3, true, spellID, caster, true);
        int defaut = target.getDefaultGfx();
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()+ "", target.getId() + "," + defaut + "," + id + ","+ (target.canPlay() ? turn + 1 : turn), this.effectID);
    }

    // BUFF NO VALUE NEEDED
    private void applyEffect_150(Fight fight, Fighter target) {
        if (turn == 0)
            return;

        target.addBuff(effectID, 0, turn, args1,args2,args3, true, spellID, caster, true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId() + "", target.getId() + "," + (turn - 1), this.effectID);

    }

    // BUFF MIN VALUE NEEDED ONLY
    private void applyEffect_BuffMinValue(Fighter target) {
        int val = args1;
        if (val == -1) return;
        target.addBuff(effectID, args1, turn, args1,args2,args3, true, spellID, caster, true);
    }

    // BUFF MAX VALUE NEEDED ONLY
    private void applyEffect_165() {
        int value = args2;
        if (value == -1) return;
        caster.addBuff(effectID, args2, turn, args1,args2,args3, true, spellID, caster, true);
    }

    // Don PA
    private void applyEffect_AddAP(Fighter target, Fight fight,int dmge) {
        int val = applyEffect_Buff(target,fight,true,dmge);
        //Gain de PA pendant le tour de jeu
        if (target.canPlay() && target == caster)
            target.setCurPa(fight, val);
    }

    // Double/Clone
    private void applyEffect_180(Fight fight)
    {
        int cell = this.cell.getId();
        if (!this.cell.getFighters().isEmpty())
            return;

        int id = fight.getNextLowerFighterGuid();
        Player clone = Player.ClonePerso(caster.getPlayer(), -id - 10000, (caster.getPlayer().getMaxPdv() - ((caster.getLvl() - 1) * 5 + 50)));
        clone.setFight(fight);

        Fighter fighter = new Fighter(fight, clone);
        fighter.fullPdv();
        fighter.setTeam(caster.getTeam());
        fighter.setInvocator(caster);

        fight.getMap().getCase(cell).addFighter(fighter);
        fighter.setCell(fight.getMap().getCase(cell));

        fight.getOrderPlaying().add((fight.getOrderPlaying().indexOf(caster) + 1), fighter);
        fight.addFighterInTeam(fighter, caster.getTeam());

        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 180, caster.getId() + "", fighter.getGmPacket('+', true).substring(3), this.effectID);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", fight.getGTL(), this.effectID);

        Trap.doTraps(fight, fighter);
    }

    // Invocation
    private void applyEffect_181(Fight fight)
    {
        Fighter current = this.caster;
        int cell = this.cell.getId();
        if(cell == current.getCell().getId())
        {
            int cellToCheck1 = PathFinding.getCaseIdWithPo(cell, 'b', 1);
            int cellToCheck2 = PathFinding.getCaseIdWithPo(cell, 'd', 1);
            int cellToCheck3 = PathFinding.getCaseIdWithPo(cell, 'f', 1);
            int cellToCheck4 = PathFinding.getCaseIdWithPo(cell, 'h', 1);
            if(fight.getMap().getCase(cellToCheck1) != null & fight.getMap().getCase(cellToCheck1).getFighters().isEmpty() & fight.getMap().getCase(cellToCheck1).isWalkable(false, true, -1))
            {
                cell = cellToCheck1;
            }else if(fight.getMap().getCase(cellToCheck2) != null & fight.getMap().getCase(cellToCheck2).getFighters().isEmpty() & fight.getMap().getCase(cellToCheck2).isWalkable(false, true, -1))
            {
                cell = cellToCheck2;
            } else if(fight.getMap().getCase(cellToCheck3) != null & fight.getMap().getCase(cellToCheck3).getFighters().isEmpty() & fight.getMap().getCase(cellToCheck3).isWalkable(false, true, -1))
            {
                cell = cellToCheck3;
            } else if(fight.getMap().getCase(cellToCheck4) != null & fight.getMap().getCase(cellToCheck4).getFighters().isEmpty() & fight.getMap().getCase(cellToCheck4).isWalkable(false, true, -1))
            {
                cell = cellToCheck4;
            }
        }
        if (!fight.getMap().getCase(cell).getFighters().isEmpty())
            return;

        int id = -1, level = -1;

        try {
            String mobs = args1+"", levels = args2+"";

            if (mobs.contains(":")) {
                String[] split = mobs.split(":");
                id = Integer.parseInt(split[Formulas.getRandomValue(0, split.length - 1)]);
            } else {
                id = Integer.parseInt(mobs);
            }

            if (levels.contains(":")) {
                String[] split = levels.split(":");
                level = Integer.parseInt(split[Formulas.getRandomValue(0, split.length - 1)]);
            } else {
                level = Integer.parseInt(levels);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Monster.MobGrade MG = null;
        Monster monster = World.world.getMonstre(id);
        if(monster == null) return;
        try {
            MG = monster.getGradeByLevel(level);
            if(MG == null) {
                MG = monster.getRandomGrade();
                if(MG == null) return;
            }
            MG = MG.getCopy();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        if (id == -1 || level == -1 || MG == null)
            return;

        if(fight.getType() == Constant.FIGHT_TYPE_PVM && caster.getMob() != null && caster.getTeam() == 1) {
            if(fight.getdifficulty() > 0)
                MG.GrowMGByDiff(fight.getdifficulty());
        }
        MG.setInFightID(fight.getNextLowerFighterGuid());

        Fighter F;
        if(caster.getPlayer() != null) {
            boolean leadercontrolinvo = false;
            if (caster.getPlayer().getSlaveLeader() != null) {
                if (fight.isInFight(caster.getPlayer().getSlaveLeader())) {
                    leadercontrolinvo = caster.getPlayer().getSlaveLeader().controleinvo;
                }
            }
            if ( (caster.getPlayer().controleinvo || leadercontrolinvo )  &&  id != 285 && id != 262) {
                Player mobControlable = Player.createInvoControlable(caster.getFight().getSigIDFighter(), MG, caster);
                F = new Fighter(fight, mobControlable);
                F.setControllable(true);
                F.setTeam(caster.getTeam());
                caster.getPlayer().addCompagnon(F.getPlayer());
                mobControlable.setFight(fight);
            } else{
                MG.modifStatByInvocator(caster);
                F = new Fighter(fight, MG);
                F.setTeam(caster.getTeam());
            }
        }
        else{
            F = new Fighter(fight, MG);
            F.setTeam(caster.getTeam());
        }

        F.setInvocator(caster);
        fight.getMap().getCase(cell).addFighter(F);
        F.setCell(fight.getMap().getCase(cell));
        F.fullPdv();
        fight.getOrderPlaying().add((fight.getOrderPlaying().indexOf(caster) + 1), F);
        fight.addFighterInTeam(F, caster.getTeam());
        String gm = F.getGmPacket('+', true).substring(3);
        String gtl = fight.getGTL();
        try {
            if (this.caster.getMob() != null) {
                Thread.sleep(1000);
                if(fight.getdifficulty() > 1 && fight.getType() == Constant.FIGHT_TYPE_PVM && caster.getTeam() == 1) {
                    F.buffMobByDiff(fight.getdifficulty()-1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 181, caster.getId() + "", gm, this.effectID);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", gtl, this.effectID);
        caster.nbrInvoc++;

        this.checkTraps(fight, F, (short) 1200);

    }

    // Invocation Statique
    private void applyEffect_185(Fight fight) {
        int monster = -1, level = -1;

        try {
            monster = args1;
            level = args2;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Monster.MobGrade mobGrade;

        try {
            mobGrade = World.world.getMonstre(monster).getGradeByLevel(level).getCopy();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (monster == -1 || level == -1 || mobGrade == null)
            return;

        if (this.caster.getPlayer() != null)
            mobGrade.modifStatByInvocator(this.caster);

        int id = fight.getNextLowerFighterGuid();
        mobGrade.setInFightID(id);

        Fighter fighter = new Fighter(fight, mobGrade);
        fighter.setTeam(this.caster.getTeam());
        fighter.setInvocator(this.caster);

        fight.getMap().getCase(this.cell.getId()).addFighter(fighter);
        fighter.setCell(fight.getMap().getCase(this.cell.getId()));
        fight.addFighterInTeam(fighter, this.caster.getTeam());
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 181, this.caster.getId() + "", fighter.getGmPacket('+', true).substring(3), this.effectID);
    }

    // unhide des personnages et pieges / Perception
    private void applyEffect_202(Fight fight, Fighter target) {
        if (target.isHide()) {
            if (target != caster)
                target.unHide(spellID);
        }
        for (Trap p : fight.getAllTrapsinAera(cell.getId(), this.areaEffect)) {
            p.setIsUnHide(caster);
            p.appear(caster);
        }
    }

    // On vol des stats brute EAU
    private void applyEffect_266(Fight fight, Fighter target,int dmge) {
        int val = Formulas.getAlteredJet(dmge,args2, target,caster);
        int vol = 0;
        target.addBuff(EffectConstant.STATS_REM_CHAN, val, turn, args1,args2,args3, true, spellID, caster, true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_REM_CHAN, caster.getId() + "", target.getId() + "," + val + "," + turn, this.effectID);

        vol += val;
        if (vol == 0)
            return;
        //on ajoute le buff
        caster.addBuff(EffectConstant.STATS_ADD_CHAN, vol, turn, args1,args2,args3, true, spellID, caster, true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_ADD_CHAN, caster.getId()+ "", caster.getId() + "," + vol + "," + turn, this.effectID);
    }

    // On vol des stats brute VIE
    private void applyEffect_267(Fight fight, Fighter target,int dmge) {
        int val = Formulas.getAlteredJet(dmge,args2, target,caster);
        int vol = 0;
        target.addBuff(EffectConstant.STATS_REM_VITA, val, turn, args1,args2,args3, true, spellID, caster, true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_REM_VITA, caster.getId() + "", target.getId() + "," + val + "," + turn, this.effectID);

        vol += val;
        if (vol == 0)
            return;
        //on ajoute le buff
        caster.addBuff(EffectConstant.STATS_ADD_VITA, vol, turn, args1,args2,args3, true, spellID, caster, true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_ADD_VITA, caster.getId()+ "", caster.getId() + "," + vol + "," + turn, this.effectID);
    }

    // On vol des stats brute AGI
    private void applyEffect_268(Fight fight, Fighter target,int dmge) {
        int val = Formulas.getAlteredJet(dmge,args2, target,caster);
        int vol = 0;
        target.addBuff(EffectConstant.STATS_REM_AGIL, val, turn, args1,args2,args3, true, spellID, caster, true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_REM_AGIL, caster.getId() + "", target.getId() + "," + val + "," + turn, this.effectID);

        vol += val;
        if (vol == 0)
            return;
        //on ajoute le buff
        caster.addBuff(EffectConstant.STATS_ADD_AGIL, vol, turn, args1,args2,args3, true, spellID, caster, true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_ADD_AGIL, caster.getId()+ "", caster.getId() + "," + vol + "," + turn, this.effectID);
    }

    // On vol des stats brute INT
    private void applyEffect_269(Fight fight, Fighter target,int dmge) {
        int val = Formulas.getAlteredJet(dmge,args2, target,caster);
        int vol = 0;
        target.addBuff(EffectConstant.STATS_REM_INTE, val, turn, args1,args2,args3, true, spellID, caster, true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_REM_INTE, caster.getId() + "", target.getId() + "," + val + "," + turn, this.effectID);

        vol += val;
        if (vol == 0)
            return;
        //on ajoute le buff
        caster.addBuff(EffectConstant.STATS_ADD_INTE, vol, turn, args1,args2,args3, true, spellID, caster, true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_ADD_INTE, caster.getId()+ "", caster.getId() + "," + vol + "," + turn, this.effectID);
    }

    // On vol des stats brute SAG
    private void applyEffect_270(Fight fight, Fighter target,int dmge) {
        int val = Formulas.getAlteredJet(dmge,args2, target,caster);
        int vol = 0;
        target.addBuff(EffectConstant.STATS_REM_SAGE, val, turn, args1,args2,args3, true, spellID, caster, true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_REM_SAGE, caster.getId() + "", target.getId() + "," + val + "," + turn, this.effectID);

        vol += val;
        if (vol == 0)
            return;
        //on ajoute le buff
        caster.addBuff(EffectConstant.STATS_ADD_SAGE, vol, turn, args1,args2,args3, true, spellID, caster, true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_ADD_SAGE, caster.getId()+ "", caster.getId() + "," + vol + "," + turn, this.effectID);
    }

    // On vol des stats brute FOR
    private void applyEffect_271(Fight fight, Fighter target,int dmge) {
        int val = Formulas.getAlteredJet(dmge,args2, target,caster);
        int vol = 0;
        target.addBuff(EffectConstant.STATS_REM_FORC, val, turn, args1,args2,args3, true, spellID, caster, true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_REM_FORC, caster.getId() + "", target.getId() + "," + val + "," + turn, this.effectID);

        vol += val;
        if (vol == 0)
            return;
        //on ajoute le buff
        caster.addBuff(EffectConstant.STATS_ADD_FORC, vol, turn, args1,args2,args3, true, spellID, caster, true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.STATS_ADD_FORC, caster.getId()+ "", caster.getId() + "," + vol + "," + turn, this.effectID);
    }

    //Créer un  piège
    private void applyEffect_400(Fight fight) {
        if (!cell.isWalkable(true))
            return;//Si case pas marchable
        if (cell.getFirstFighter() != null && !cell.getFirstFighter().isHide())
            return;//Si la case est prise par un joueur

        //Si la case est prise par le centre d'un piege
        for (Trap p : fight.getAllTraps()) {
            if (p.getCell().getId() == cell.getId())
                return;
        }

        int spellID = args1;
        int level = args2;
        String po = this.getAreaEffect();
        byte size = (byte) World.world.getCryptManager().getIntByHashedValue(po.charAt(1));
        SpellGrade TS = World.world.getSort(spellID).getStatsByLevel(level);

        final Trap g = new Trap(fight, caster, cell, size, TS, spellID, (byte) level,args3);
        fight.getAllTraps().add(g);
        int team = caster.getTeam() + 1;
        String str = "GDZ+" + cell.getId() + ";" + size + ";" + args3;
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, team, 999, caster.getId() + "", str, this.effectID);
        str = "GDC" + cell.getId() + ";Haaaaaaaaz3005;";
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, team, 999, caster.getId() + "", str, this.effectID);
    }

    /*private void applyEffect_401(Fight fight) {
        if (!cell.isWalkable(false))
            return;//Si case pas marchable
        if (cell.getFirstFighter() != null && caster != cell.getFirstFighter())
            return;//Si la case est prise par un joueur

        int spellID = this.args1;
        int level = this.args2;
        int color = this.args3;
        byte duration = (byte) this.turn;
        SpellGrade TS = World.world.getSort(spellID).getStatsByLevel(level);

        Glyph g = new Glyph(fight, caster, cell, this.areaEffect, TS, duration, spellID,color);
        fight.getAllGlyphs().add(g);
        g.appear();
    }*/

    // Ajout d'une glyphe
    private void applyEffect_402(Fight fight) {
        if (!cell.isWalkable(true))
            return;//Si case pas marchable

        int spellID = this.args1;
        int level = this.args2;
        int color = this.args3;
        byte duration = (byte) this.turn;
        SpellGrade TS = World.world.getSort(spellID).getStatsByLevel(level);

        Glyph g = new Glyph(fight, caster, cell, this.areaEffect, TS, duration, this.spellID,color);
        fight.getAllGlyphs().add(g);
        g.appear();
    }

    // D'abord il tue puis l'invoque si il y en a une dans la zone et la remplace par un invoque
    private void applyEffect_405(Fighter target,Fight fight) {
        fight.onFighterDie(target, caster);
        applyEffect_181(fight);
    }

    // Dommage %vita pour le lanceur mais non typé (donc prend pas en compte les résistances)
    private void applyEffect_671(Fighter target, Fight fight,int dmge) {
        applyEffect_PerVitaMaxDmg(target, fight,dmge);
    }

    //Punition (donc dégat en fonction du % de PV restant)
    private void applyEffect_672(Fighter target, Fight fight) {

        //Formule de barge ? :/ Clair que ca punie ceux qui veulent l'utiliser x_x
        double val = ((double) args1 / (double) 100);
        int pdvMax = caster.getPdvMaxOutFight();
        double pVie = (double) caster.getPdv() / (double) caster.getPdvMax();
        double rad = (double) 2 * Math.PI * (double) (pVie - 0.5);
        double cos = Math.cos(rad);
        double taux = (Math.pow((cos + 1), 2)) / (double) 4;
        double dgtMax = val * pdvMax;
        int dgt = (int) (taux * dgtMax);

        int finalDommage = applySpecificCasesBeforeDamage(dgt, target, caster, fight, Constant.ELEMENT_NEUTRE);//S'il y a des buffs sp�ciaux



        int resi = target.getTotalStats().getEffect(EffectConstant.STATS_ADD_RP_NEU);
        if(finalDommage > 0) {
            // TODO : pk on prend pas les rési fix, il faudrait
            if (resi > 0) {
                finalDommage =  finalDommage - ( finalDommage * Math.abs(resi / 100) );
            }
            else{
                finalDommage =  finalDommage + ( finalDommage * Math.abs(resi / 100) );
            }
        }
        else{
            if (resi > 0) {
                finalDommage =  finalDommage + ( finalDommage * Math.abs(resi / 100) );
            }
            else{
                finalDommage =  finalDommage - ( finalDommage * Math.abs(resi / 100) );
            }
        }

        int finaldmg = caster.getTotalStats().getEffect(EffectConstant.STATS_ADD_FINALDMG);
        if (finaldmg < 0) {
            finalDommage = (int) Math.round(finalDommage - (finalDommage * ((double)Math.abs(finaldmg) / 100.0 )));
        } else {
            finalDommage = (int) Math.round(finalDommage + (finalDommage * ((double)Math.abs(finaldmg) / 100.0 )));
        }


        if (finalDommage > target.getPdv())
            finalDommage = target.getPdv();//Target va mourrir
        target.removePdv(caster, finalDommage);

        if(finalDommage >= 0) {
            sendClientDamage(fight, target.getId(), finalDommage);
        }
        else{
            this.effectID = 108;
            sendClientHeal(fight, target.getId(), finalDommage);
        }
        applyEffectAfterHit(fight,target,caster,finalDommage);

    }

    // On ajoute le buff
    private void applyEffect_765(Fighter target) {
        target.addBuff(effectID, -1, turn, args1,args2,args3, true, spellID, caster, true);
    }

    // On ajoute un buff d'effet, qui ne boost pas
    private void applyEffect_AddBuffFix(Fighter target) {
        target.addBuff(effectID, -1, turn, args1,args2,args3, true, spellID, caster, true);
    }


    // On applique l'effet Sacrifice
    private void applyEffect_765B(Fight fight, Fighter target) {
        Fighter sacrified = target.getBuff(EffectConstant.EFFECTID_SACRIFICE).getCaster();
        GameCase cell1 = sacrified.getCell();
        GameCase cell2 = target.getCell();

        sacrified.getCell().getFighters().clear();
        target.getCell().getFighters().clear();
        sacrified.setCell(cell2);
        sacrified.getCell().addFighter(sacrified);
        target.setCell(cell1);
        target.getCell().addFighter(target);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, target.getId() + "", target.getId() + "," + cell1.getId(), this.effectID);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, sacrified.getId() + "", sacrified.getId() + "," + cell2.getId(), this.effectID);
    }

    // On ramene a la vie
    private void applyEffect_780(Fight fight) {
        Fighter target = null;
        Fighter lastinvo = null;
        Fighter lastplayer = null;
        // Si la liste des mort est vide on fait rien
        if(fight.getDeadList().size() == 0)
            return;


        for (Fighter fighter : fight.getDeadList().values()) {
            if (!fighter.hasLeft() && fighter.getTeam() == caster.getTeam()) {
                if(fighter.getPlayer() != null ) {
                    if(fighter.getPlayer().isInvocControlable) {
                        lastinvo = fighter;
                    }
                    else {
                        lastplayer = fighter;
                    }
                }
                else{
                    Fighter invocator = fighter.getInvocator();

                    if ( !(invocator.isInvocation() || invocator.isControllable() ) )
                        lastinvo = fighter;
                }
            }
        }

        // On prend le dernier allié si ca existe sinon dernier invo
        if(lastplayer != null) {
            target = lastplayer;
        }
        else{
            target = lastinvo;
        }

        // Si le dernier mort est null on fait rien
        if (target == null) return;

        fight.addFighterInTeam(target, target.getTeam());
        target.setIsDead(false);
        target.getFightBuff().clear();

        if (target.isInvocation() && !target.isControllable() )
            fight.getOrderPlaying().add((fight.getOrderPlaying().indexOf(caster) + 1), target);

        target.setCell(cell);
        target.getCell().addFighter(target);

        target.fullPdv();
        int percent = (100 - args1) * target.getPdvMax() / 100;
        target.removePdv(caster, percent);

        String gm = target.getGmPacket('+', true).substring(3);
        String gtl = fight.getGTL();
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 181, target.getId() + "", gm, this.effectID);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, target.getId() + "", gtl, this.effectID);
        if (!target.isInvocation())
            SocketManager.GAME_SEND_STATS_PACKET(target.getPlayer());

        fight.removeDead(target);
    }

    // POUSSE NO DO POU
    private void applyEffect_783(Fight fight) {


        GameCase ccase = caster.getCell();
        //On calcule l'orientation entre les 2 cases
        char dir = PathFinding.getDirBetweenTwoCase(ccase.getId(), cell.getId(), fight.getMap(), true);
        //On calcule l'id de la case a coté du lanceur dans la direction obtenue
        int tcellID = PathFinding.GetCaseIDFromDirrection(ccase.getId(), dir, fight.getMap(), true);
        //on prend la case corespondante
        GameCase tcase = fight.getMap().getCase(tcellID);

        if (tcase == null)
            return;
        //S'il n'y a personne sur la case, on arrete
        if (tcase.getFighters().isEmpty())
            return;
        //On prend le Fighter ciblé
        Fighter target = tcase.getFirstFighter();
        //On verifie qu'il peut aller sur la case ciblé en ligne droite
        int c1 = tcellID, limite = 0;
        if (target.getMob() != null)
            for (int i : Constant.STATIC_INVOCATIONS)
                if (i == target.getMob().getTemplate().getId())
                    return;

        while (true) {
            if (PathFinding.GetCaseIDFromDirrection(c1, dir, fight.getMap(), true) == cell.getId())
                break;
            if (PathFinding.GetCaseIDFromDirrection(c1, dir, fight.getMap(), true) == -1)
                return;
            c1 = PathFinding.GetCaseIDFromDirrection(c1, dir, fight.getMap(), true);
            limite++;
            if (limite > 50)
                return;
        }
        GameCase newCell = PathFinding.checkIfCanPushEntity(fight, ccase.getId(), cell.getId(), dir);

        if (newCell != null)
            cell = newCell;

        target.getCell().getFighters().clear();
        target.setCell(cell);
        target.getCell().addFighter(target);

        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, caster.getId() + "", target.getId() + "," + cell.getId(), this.effectID);
        Trap.doTraps(fight, target);
    }

    // Rollback
    private void applyEffect_784(Fight fight) {
        Map<Integer, GameCase> origPos = fight.getRholBack(); // les positions de début de combat

        ArrayList<Fighter> list = fight.getFighters(3); // on copie la liste des fighters
        for (int i = 1; i < list.size(); i++)   // on boucle si tout le monde est à la place
            if (!list.isEmpty())                 // d'un autre
                for (Fighter F : list) {
                    if (F == null || F.isDead() || !origPos.containsKey(F.getId())) {
                        continue;
                    }
                    if (F.getCell().getId() == origPos.get(F.getId()).getId()) {
                        continue;
                    }
                    if (origPos.get(F.getId()).getFirstFighter() == null) {
                        F.getCell().getFighters().clear();
                        F.setCell(origPos.get(F.getId()));
                        F.getCell().addFighter(F);
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, F.getId() + "", F.getId() + "," + F.getCell().getId(), this.effectID);
                    }
                }
    }

    // TODO Nope ca c'est arbre de soin
    private void applyEffect_786(Fight fight) {


    }

    // TODO a reprendre
    // applique un sort sur la cible
    /*private void applyEffect_787(Fighter target, Fight pelea) {
        int hechizoID = -1;
        int hechizoNivel = -1;
        try {
            hechizoID = Integer.parseInt(args.split(";")[0]);
            hechizoNivel = Integer.parseInt(args.split(";")[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Spell hechizo = World.world.getSort(hechizoID);
        ArrayList<Effect> EH = hechizo.getStatsByLevel(hechizoNivel).getEffects();
        for (SpellEffect eh : EH) {
            target.addBuff(eh.effectID, minvalue, 1, 1, true, eh.spell, eh.args, caster, true);
        }
    }*/

    // Chatiment avec le nouveau Système OnHit
    private void applyEffect_788(Fighter target) {
        // On créé le buff pour l'affichage en jeu
        target.addBuff(effectID, args1, turn, args1,args2,args3, false, spellID, target, true);

        // On créé un buff caché qui se lance quand tapé
        Effect chatiment = new Effect(args1,0,args3,args2,0,0,caster,spellID,true); // On créer un tout nouveau buff pour ce chatiment

        // On ajoute a la liste des buffOnHit de la target
        target.addBuffOnHit(effectID,args1,args2,args3,0,turn,World.world.getEffectTrigger(5), chatiment,caster,spellID,0);
    }

    // Applique un état
    private void applyEffect_950(Fight fight, Fighter target) {
        int etatId = this.getArgs3();
        if (turn == 0) {
            target.setState(etatId, turn);
            //target.addBuff(this.getEffectID(), etatId, turn, args1, -1, -1, false, spellID, caster, true);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, this.getEffectID(), target.getId()+ "", target.getId() + "," + etatId + ",1", this.effectID);
        }
        else{
            target.setState(etatId, turn);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, this.getEffectID(), target.getId() + "", target.getId() + "," + etatId + ",1", this.effectID);
            target.addBuff(this.getEffectID(), etatId, turn, args1, -1, -1, false, spellID, caster, true);
        }
    }

    // Retire un état
    private void applyEffect_951(Fight fight, Fighter target) {
        if (!target.haveState(this.getArgs3()))
            return;

        //on enleve l'�tat
        int etatId = this.getArgs3();

        target.setState(etatId, 0);
        target.debuffState(etatId);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, target.getId() + "", target.getId() + "," + etatId + ",0", this.getEffectID());
    }


    public void applyBeginingBuff(Fight fight, Fighter fighter) {
        this.turn = 0;
        this.isPoison = true;
        // TODO : pas sur la a controller du coup non
        int dmge = Formulas.getRandomJet(args1,args2,caster);
        this.applyEffectOnTarget(fight,fighter, dmge);

        //this.applyEffectToFight(fight, this.caster, fighter.getCell());
    }

    public int applySpecificCasesBeforeDamage(int finalDommage, Fighter target, Fighter caster, Fight fight, int elementId) {
        for (int id : Constant.ON_HIT_BUFFS) {
            for (Effect buff : target.getBuffsByEffectID(id)) {
                switch (id) {
                    case EffectConstant.EFFECTID_REVERSECHANCE://chance éca
                        try {
                            int coefDom = buff.getArgs1();
                            int coefHeal = buff.getArgs2();
                            int chance = buff.getArgs3();
                            int jet = Formulas.getRandomValue(0, 99);
                            if (jet < chance)//Soin
                            {
                                finalDommage = -(finalDommage * coefHeal);
                                if (-finalDommage > (target.getPdvMax() - target.getPdv()))
                                    finalDommage = -(target.getPdvMax() - target.getPdv());

                                target.removePdv(caster, finalDommage);
                                sendClientHeal(fight, target.getId(), finalDommage);
                            } else//Dommage
                                finalDommage = finalDommage * coefDom;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case EffectConstant.EFFECTID_RETURNDAMAGE://renvoie Dom
                        if (target.getId() == caster.getId()) break;

                        if (caster.hasBuff(EffectConstant.EFFECTID_SACRIFICE))//sacrifice
                        {
                            if (caster.getBuff(EffectConstant.EFFECTID_SACRIFICE) != null && !caster.getBuff(EffectConstant.EFFECTID_SACRIFICE).getCaster().isDead()) {
                                buff.applyEffect_765B(fight, caster);
                                caster = caster.getBuff(EffectConstant.EFFECTID_SACRIFICE).getCaster();
                            }
                        }

                        float coef = 1 + (target.getTotalStats().getEffect(EffectConstant.STATS_ADD_SAGE) / 100);
                        int renvoie = 0;
                        try {
                            if (buff.args2 != -1) {
                                renvoie = (int) (coef * Formulas.getRandomValue(buff.args1, buff.args2));
                            } else {
                                renvoie = (int) (coef * buff.args1);
                            }
                        } catch (Exception e) {
                            return finalDommage;
                        }
                        if (renvoie > finalDommage) renvoie = finalDommage;
                        finalDommage -= renvoie;
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 107, "-1", target.getId() + "," + renvoie, elementId);
                        if (renvoie > caster.getPdv()) renvoie = caster.getPdv();
                        if (finalDommage < 0) finalDommage = 0;
                        caster.removePdv(caster, renvoie);
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId() + "", caster.getId() + ",-" + renvoie, elementId);
                        break;

                    /*case EffectConstant.EFFECTID_DEROBADE:
                        if (this.isPoison)
                            continue;
                        if (caster == target || Formulas.getRandomValue(0, 99) + 1 >= buff.getArgs1() )
                            continue;

                        int distance = PathFinding.getDistanceBetween(fight.getMap(), target.getCell().getId(), caster.getCell().getId());
                        int nbCell = buff.getArgs2();

                        if (distance > 1) {
                            continue;
                        }
                        if (nbCell == 0)
                            continue;
                        int exCase = target.getCell().getId(), newCellId = PathFinding.newCaseAfterPush(fight, caster.getCell(), target.getCell(), nbCell);

                        if (newCellId < 0) { // blocked
                            int a = -newCellId;
                            a = nbCell - a;
                            newCellId = PathFinding.newCaseAfterPush(fight, caster.getCell(), target.getCell(), a);
                            if (newCellId == 0) {
                                finalDommage = Formulas.getRandomValue(28, 33);
                                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, target.getId() + "", target.getId() + "," + newCellId);
                                break;
                            }
                            if (fight.getMap().getCase(newCellId) == null)
                                continue;
                        }
                        if (newCellId == 0) continue;

                        GameCase cacheCell = target.getCell();
                        cacheCell.getFighters().clear();

                        target.setCell(fight.getMap().getCase(newCellId));
                        target.getCell().addFighter(target);
                        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, target.getId() + "", target.getId() + "," + newCellId);

                        Fighter holding = target.getIsHolding();
                        if (holding != null) {
                            holding.setState(EffectConstant.ETAT_PORTE, 0);
                            target.setState(EffectConstant.ETAT_PORTEUR, 0);
                            holding.setHoldedBy(null);
                            target.setIsHolding(null);
                            holding.setCell(cacheCell);
                            cacheCell.addFighter(holding);
                            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, target.getId() + "", target.getId() + "," + EffectConstant.ETAT_PORTEUR + ",0");
                            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, holding.getId() + "", holding.getId() + "," + EffectConstant.ETAT_PORTE + ",0");
                            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 51, target.getId() + "", cacheCell.getId() + "");
                        }

                        this.checkTraps(fight, target, (short) 1200);
                        if (exCase != newCellId)
                            finalDommage = 0;
                        break;*/

                }
            }
        }
        return finalDommage;
    }

    public void applyEffect_EndingTurnBuff(Fight fight, Fighter fighter) {
        // Pour le moment que le poison PA je crois
            int paUse = this.getArgs1();
            int val = this.getArgs2();
            int ukw = this.getArgs3(); // pas encore trouvé l'utilité mais ca change apparemment

            if (val == -1)
                return;

            int nbr = (int) Math.floor((double) fight.getCurFighterUsedPa()
                    / (double) paUse);
            int dgt = val * nbr;

            int stat = 0;
            switch (this.getSpell()){
                // Si poison paralysant stat intel
                case 200 :
                case 2143 :
                    stat = this.getCaster().getTotalStats().getEffect(EffectConstant.STATS_ADD_INTE);
                    if (stat < 0)
                        stat = 0;
                    break;
                // Default dégat neutre
                default :
                    stat = this.getCaster().getTotalStats().getEffect(EffectConstant.STATS_ADD_FORC);
                    if (stat < 0)
                        stat = 0;
                    break;
            }

            // on applique le boost
            int pdom = this.getCaster().getTotalStats().getEffect(EffectConstant.STATS_ADD_PERDOM);
            if (pdom < 0)
                pdom = 0;

            dgt = (int) Math.round( ((1 + (stat/100.0D) + (pdom/100.0D) )) * dgt );

            if (fighter.hasBuff(184)) {
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, fighter.getId() + "", fighter.getId() + "," + fighter.getBuff(184).getFixvalue());
                dgt = dgt - fighter.getBuff(184).getFixvalue();// R�duction physique
            }
            if (fighter.hasBuff(105)) {
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, fighter.getId() + "", fighter.getId() + "," + fighter.getBuff(105).getFixvalue());
                dgt = dgt - fighter.getBuff(105).getFixvalue();// Immu
            }

            if (dgt <= 0)
                return;
            if (dgt > fighter.getPdv())
                dgt = fighter.getPdv();// va mourrir

            fighter.removePdv(fighter, dgt);
            dgt = -(dgt);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, this.getCaster().getId() + "", fighter.getId() + "," + dgt);

            // Si la cible est morte après l'effet
            if (fighter.getPdv() <= 0 && !fighter.isDead()) {
                fighter.setPdv(0);
                fight.onFighterDie(fighter, fighter);
            }


    }

    private Fighter getRealTargetSwitchBuff(Fight fight,Fighter target){
        Fighter finalTarget = target;
        // Si la cible a sacrifice on échange la place avec celui qui sacrifie et on le place en target voulu
        if (target.hasBuff(EffectConstant.EFFECTID_SACRIFICE))//sacrifice
        {
            if (target.getBuff(EffectConstant.EFFECTID_SACRIFICE) != null && !target.getBuff(EffectConstant.EFFECTID_SACRIFICE).getCaster().isDead()) {
                applyEffect_765B(fight, target);
                target = target.getBuff(EffectConstant.EFFECTID_SACRIFICE).getCaster();
            }
        }

        // Si la cible a Renvoi de sorts on le place en target voulu
        if (type == EffectConstant.EFFECT_TYPE_SPELL) {
            //si la cible a le buff renvoie de sort et que le sort peut etre renvoyer
            if (target.hasBuff(EffectConstant.EFFECTID_RETURNSPELL) && this.spellID != 0) {
                Effect rSBuff = target.getBuff(EffectConstant.EFFECTID_RETURNSPELL);
                if(rSBuff.getArgs2() >= this.gradeSpell) {
                    // On rajoute la notion de chance de renvoi
                    if (rSBuff.getArgs3() != 0 && rSBuff.getArgs3() != 100) {
                        int jet = Formulas.getRandomValue(0, 99);
                        if(jet>=rSBuff.getArgs3())
                            return target;
                    }
                    //le lanceur devient donc la cible
                    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, EffectConstant.EFFECTID_RETURNSPELL, target.getId() + "", target.getId() + ",1", this.getEffectID());
                    target = caster;
                }
            }
        }
        return target;
    }

    private void getRealEffectSwitchBuff(Fight fight,Fighter target){

        // Si il a dérobade on change l'effet pour dégat de poussée ?
        if(target.hasBuff(EffectConstant.EFFECTID_DEROBADE)){
            Effect buff = target.getBuff(EffectConstant.EFFECTID_DEROBADE);
            int distance = PathFinding.getDistanceBetween(fight.getMap(), target.getCell().getId(), caster.getCell().getId());
            int nbCell = buff.getArgs2();

            if (distance > 1) {
                return;
            }
            if (nbCell == 0)
                return;

            this.effectID = 5;
            this.chanceToLaunch = buff.args1;
            this.args1 = buff.args2;
        }


    }


    private ArrayList<GameCase> getCasesImpactedInArea(Fight fight){
        ArrayList<GameCase> cells = PathFinding.getCellListFromAreaString(fight.getMap(), cell.getId(), caster.getCell().getId(), this.getAreaEffect());
        ArrayList<GameCase> finalCells = new ArrayList<GameCase>();
        int TE = this.getEffectTarget();
        for (GameCase C : cells) {
            if (C == null)
                continue;
            Fighter F = C.getFirstFighter();
            if (F == null)
                continue;
            // Ne touches pas les alli�s : 1
            if (((TE & 1) == 1) && (F.getTeam() == caster.getTeam()))
                continue;
            // Ne touche pas le lanceur : 2
            if ((((TE >> 1) & 1) == 1) && (F.getId() == caster.getId()))
                continue;
            // Ne touche pas les ennemies : 4
            if ((((TE >> 2) & 1) == 1) && (F.getTeam() != caster.getTeam()))
                continue;
            // Ne touche pas les combatants (seulement invocations) : 8
            if ((((TE >> 3) & 1) == 1) && (!F.isInvocation()))
                continue;
            // Ne touche pas les invocations : 16
            if ((((TE >> 4) & 1) == 1) && (F.isInvocation()))
                continue;
            // N'affecte que le lanceur : 32
            if ((((TE >> 5) & 1) == 1) && (F.getId() != caster.getId()))
                continue;
            // N'affecte que les alliés (pas le lanceur) : 64
            if ((((TE >> 6) & 1) == 1) && (F.getTeam() != caster.getTeam() || F.getId() == caster.getId()))
                continue;
            // N'affecte PERSONNE : 1024
            if ((((TE >> 10) & 1) == 1))
                continue;
            // Si pas encore eu de continue, on ajoute la case, tout le monde : 0
            finalCells.add(C);
        }

        // Si le sort n'affecte que le lanceur et que le lanceur n'est pas dans la zone (ca peut être une idée pour colère de Iop, Fleche punitive, Etc)
        if (((TE >> 5) & 1) == 1) {
            if (!finalCells.contains(caster.getCell()))
                finalCells.add(caster.getCell());
        }

        return finalCells;
    }

    private int calculLifeSteal(Fighter caster,int damage){
        int heal = (int) Math.floor ((damage) / 2);
        if ((caster.getPdv() + heal) > caster.getPdvMax())
            heal = caster.getPdvMax() - caster.getPdv();

        return heal;
    }

    public int calculFinalDommage(Fight fight, Fighter caster, Fighter target, int statID, int jet, boolean isHeal, boolean isCaC, int spellid) {
        int value = Formulas.calculFinalDommagee(fight, caster, target, statID, jet, isHeal, isCaC, spellid);
        return value;
    }

    private void sendClientBuff(Fight fight,int action ,int val ,int cibleID, boolean negative){
        int value;
        if(negative){
            value = -(Math.abs(val));
        }
        else{
            value = (Math.abs(val));
        }
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, action, caster.getId() + "", cibleID + "," + value + "," + turn, this.effectID);
        //SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effetID, casterID + "", cibleID + "," + cellID);
    }

    /*private void sendClientDebuff(Fight fight,int cibleID,int action ,int val ,int targetID){
        int damage = -(Math.abs(val));
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, action, caster.getId() + "", targetID + ",-" + val + "," + turn, this.effectID);
        //SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effetID, casterID + "", cibleID + "," + cellID);
    }*/

    private void sendClientAction(Fight fight,int cibleID,int casterID,int cellID,int effetID){
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effetID, casterID + "", cibleID + "," + cellID);
    }

    private void sendClientDamage(Fight fight, int targetId, int value){
        int damage = -(Math.abs(value));
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId() + "", targetId + "," + damage, effectID);
    }

    private void sendClientHeal(Fight fight, int targetId, int value){
        int heal = Math.abs(value);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 108, caster.getId() + "", targetId + "," + heal, 108);
    }

    public boolean IsCCEffet (){
        return this.isCCEffect;
    }

    public int getChance() {
        return this.chanceToLaunch;
    }

    public String getAreaEffect() {
        return this.areaEffect;
    }

    public int getEffectTarget() {
        return this.effectTarget;
    }

    public void setEffectTarget(int effectTarget) {
        this.effectTarget = effectTarget;
    }

    public int getEffectID() {
        return this.effectID;
    }

    public void setEffectID(int eID) {
        this.effectID = eID;
    }

    public int getSpell() {
        return this.spellID;
    }

    public int getArgs1(){
        return args1;
    }
    public int getArgs2(){
        return args2;
    }
    public int getArgs3(){ return args3; }

    public void setArgs1(int arg){
        this.args1= arg;
    }
    public void setArgs2(int arg){
        this.args2= arg;
    }
    public void setArgs3(int arg){
        this.args3= arg;
    }

    public String getJet(){
        return jet;
    }

    public void setJet(String jet){
        this.jet = jet;
    }

    public boolean isUnbuffable() {
        return this.isUnbuffable;
    }

    public int getFixvalue() {
        return this.finalValue;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setTurn(int turn) {
		this.turn = turn;
	}


    public Fighter getCaster() {
        return this.caster;
    }

    public GameCase getCell() {
        return this.cell;
    }

    public int decrementDuration() {
        if(duration != 0)
            duration -= 1;
        return duration;
    }

    private ArrayList<Fighter> trierCibles(ArrayList<Fighter> cibles, Fight fight) {
        ArrayList<Fighter> array = new ArrayList<>();
        int max = -1;
        int distance;

        for (Fighter f : cibles) {
            distance = PathFinding.getDistanceBetween(fight.getMap(), this.cell.getId(), f.getCell().getId());
            if (distance > max)
                max = distance;
        }

        for (int i = max; i >= 0; i--) {
            Iterator<Fighter> it = cibles.iterator();
            while (it.hasNext()) {
                Fighter f = it.next();
                distance = PathFinding.getDistanceBetween(fight.getMap(), this.cell.getId(), f.getCell().getId());
                if (distance == i) {
                    array.add(f);
                    it.remove();
                }
            }
        }
        return array;
    }

    /**
     * Cette fonction doit etre utilise <b>uniquement lorsque</b> il faut attendre la fin d'une animation niveau client.<br>
     * Exemple une <b>teleportation , attirance , invocation ou bien le fait d'etre jete par un panda.</b><br>
     * Lorsque quelqu'un est pousse il n'est pas necessaire d'appeler cette fonction car le client prend en charge la possibilite de pousser a plusieurs repprises.
     *
     * @param fight L'instance de la class Fight
     * @param fighter Le joueur qu'il faut appliquer le reseau de pieges ou bien juste les pieges
     * @param time Temps que mets l'animation de tp/jeter/attirer niveau client
     *
     * @author Sarazar928Ghost
     */
    private void checkTraps(Fight fight, Fighter fighter, short time) {
        // Il est sur un piege qui pousse ?
        final boolean isPushing = Trap.checkPushingTraps(fight, fighter);

        // Si il n'est pas dans un reseau
        if(!isPushing) {
            Trap.doTraps(fight, fighter);
            return;
        }

        // Il est dans un reseau
        fight.setTraped(true);

        TimerWaiter.addNext(() -> {
            Trap.doTraps(fight, fighter);
            fight.removeTraped();
        }, time, TimeUnit.MILLISECONDS);
    }



}

class EffectApplication {
    private Effect effect;
    private List<Fighter> targets;

    public EffectApplication(Effect effect) {
        this.effect = effect;
        this.targets = new ArrayList<>();
    }

    public void addTarget(Fighter target) {
        this.targets.add(target);
    }

    public Effect getEffect() {
        return effect;
    }

    public List<Fighter> getTargets() {
        return targets;
    }
}


