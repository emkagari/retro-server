package fight;

import client.Player;
import common.Formulas;
import common.PathFinding;
import common.SocketManager;
import fight.spells.Effect;
import fight.spells.EffectConstant;
import fight.spells.SpellGrade;
import game.GameClient;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Challenge {

    private int Type, xpWin, dropWin, Arg = 0;
    private boolean challengeAlive = false, challengeWin = false;
    private String looseBy = "", Args = "", lastActions = "";
    private Fight fight;
    private Fighter _cible;
    private List<Fighter> _ordreJeu = new ArrayList<>();

    public Challenge(Fight fight, int Type, int xp, int drop) {
        this.challengeAlive = true;
        this.fight = fight;
        this.Type = Type;
        this.xpWin = xp;
        this.dropWin = drop;
        this._ordreJeu.clear();
        this._ordreJeu.addAll(fight.getOrderPlaying());
    }

    public int getType() {
        return this.Type;
    }

    public boolean getAlive() {
        return challengeAlive;
    }

    public int getXp() {
        return xpWin;
    }

    public int getDrop() {
        return dropWin;
    }

    public boolean getWin() {
        return challengeWin;
    }

    public boolean loose() {
        return looseBy.isEmpty();
    }

    public String getPacketEndFight() {
        return (this.challengeWin ? "OK" + Type : "KO" + Type);
    }

    private void challengeWin() {
        challengeWin = true;
        challengeAlive = false;
        SocketManager.GAME_SEND_CHALLENGE_FIGHT(fight, 1, "OK" + Type);
    }

    public void challengeLoose(Fighter fighter) {
        String name = "";
        if (fighter != null && fighter.getPlayer() != null)
            name = fighter.getPlayer().getName();
        looseBy = name;
        challengeWin = false;
        challengeAlive = false;
        SocketManager.GAME_SEND_CHALLENGE_FIGHT(fight, 7, "KO" + Type);
        SocketManager.GAME_SEND_Im_PACKET_TO_CHALLENGE(fight, 1, "0188;" + name);
    }

    public void challengeSpecLoose(Player player) {
        SocketManager.GAME_SEND_CHALLENGE_PERSO(player, "KO" + Type);
        SocketManager.GAME_SEND_Im_PACKET_TO_CHALLENGE_PERSO(player, "0188;"
                + looseBy);
    }

    public String parseToPacket() {
        StringBuilder packet = new StringBuilder();
        // The 1.49.5 client's Gd: id;showTarget;targetId;category;state;xp;teamXp;drop;teamDrop.
        // Category 0: a fight challenge (orange). State: 0 running, 1 won, 2 failed.
        int state = challengeAlive ? 0 : (challengeWin ? 1 : 2);
        packet.append(Type).append(";").append(_cible != null ? "1" : "0").append(";").append(_cible != null ? (Integer.valueOf(_cible.getId())) : "").append(";");
        packet.append("0;").append(state).append(";");
        packet.append(xpWin).append(";0;").append(dropWin).append(";0");
        return packet.toString();
    }

    public void showCibleToPerso(Player p) {
        if (!challengeAlive || _cible == null || _cible.getCell() == null
                || p == null)
            return;
        ArrayList<GameClient> Pws = new ArrayList<>();
        Pws.add(p.getGameClient());
        SocketManager.GAME_SEND_FIGHT_SHOW_CASE(Pws, _cible.getId(), _cible.getCell().getId());
    }

    public void showCibleToFight() {
        if (!challengeAlive || _cible == null || _cible.getCell() == null)
            return;
        ArrayList<GameClient> Pws = new ArrayList<>();
        for (Fighter fighter : fight.getFighters(1)) {
            if (fighter.hasLeft())
                continue;
            if (fighter.getPlayer() == null
                    || !fighter.getPlayer().isOnline())
                continue;
            Pws.add(fighter.getPlayer().getGameClient());
        }
        SocketManager.GAME_SEND_FIGHT_SHOW_CASE(Pws, _cible.getId(), _cible.getCell().getId());
    }

    public void fightStart() {//D�finit les cibles au d�but du combat
        if (!challengeAlive)
            return;
        switch (Type) {
            case 3://D�sign� Volontaire
            case 4://Sursis
            case 32://Elitiste
            case 35://Tueur � gages
                if (_cible == null && _ordreJeu.size() > 0)//Si aucun cible n'est choise on en choisie une
                {
                    List<Fighter> Choix = new ArrayList<Fighter>();
                    Choix.addAll(_ordreJeu);
                    Collections.shuffle(Choix);//M�lange l'ArrayList
                    for (Fighter f : Choix) {
                        if (f.getPlayer() != null)
                            continue;
                        if (f.getMob() != null && f.getTeam2() == 2 && !f.isDead() && !f.isInvocation())
                            _cible = f;
                    }
                }
                showCibleToFight();//On le montre a tous les joueurs
                break;
            case 10://Cruel
                int levelMin = 2000;
                for (Fighter fighter : fight.getFighters(2))//La cible sera le niveau le plus faible
                {
                    if (fighter.isInvocation())
                        continue;
                    if (fighter.getPlayer() == null
                            && fighter.getMob() != null
                            && fighter.getLvl() < levelMin
                            && fighter.getInvocator() == null) {
                        levelMin = fighter.getLvl();
                        _cible = fighter;
                    }
                }
                if (_cible != null)
                    showCibleToFight();
                break;
            case 25://Ordonn�
                int levelMax = 0;
                for (Fighter fighter : fight.getFighters(2))//la cible sera le niveau le plus �lev�
                {
                    if (fighter.isInvocation() || fighter.isDouble())
                        continue;
                    if (fighter.getPlayer() == null
                            && fighter.getMob() != null
                            && fighter.getInvocator() == null
                            && fighter.getLvl() > levelMax) {
                        levelMax = fighter.getLvl();
                        this._cible = fighter;
                    }
                }
                if (_cible != null)
                    showCibleToFight();
                break;
        }
    }

    public void fightEnd() {//V�rifie la validit� des challenges en fin de combat (si n�cessaire)
        if (!challengeAlive)
            return;
        switch (Type) {
            case 44://Partage
            case 46://Chacun son monstre
                for (Fighter fighter : fight.getFighters(1)) {
                    if (!Args.contains(String.valueOf(fighter.getId())) && !(fighter.isInvocation()) ) {
                        challengeLoose(fighter);
                        return;
                    }
                }
                break;
        }
        challengeWin();
    }

    public void onFighterDie(Fighter fighter) {
        if (!challengeAlive)
            return;
        switch (Type) {
            case 33: // survivant
            case 49: // Prot�gez vos mules
                if (fighter.getPlayer() != null && !(fighter.isInvocation()))
                    challengeLoose(fight.getFighterByOrdreJeu());
                break;
            case 44://Partage
                if (fighter.getPlayer() != null && !(fighter.isInvocation()))
                    if (!Args.contains(String.valueOf(fighter.getId())))
                        challengeLoose(fighter);
                break;
        }
    }

    public void onFighterAttacked(Fighter caster, Fighter target) {
        if (!challengeAlive)
            return;
        switch (Type) {
            case 17:// Intouchable
                if (target.getTeam() == 0 && !target.isInvocation()) {
                    if (target.getBuff(9) == null) // Si d�robade
                        challengeLoose(target);
                    break;
                }
                break;
            case 31: // Focus
                if (caster.getTeam() == 0 && target.getTeam() == 1) {
                    if (Args.isEmpty())
                        Args += "" + target.getId();
                    else if (!Args.contains("" + target.getId()))
                        challengeLoose(caster);
                }
                break;
        }
    }

    public void onFightersAttacked(ArrayList<Fighter> targets, Fighter caster,
                                   Effect SE, int spellid, boolean isTrap) {
        int effectID = SE.getEffectID();
        if (!challengeAlive)
            return;

        switch (Type) {
            case 31:
                break;
            case 18: // Incurable
                if ((caster.getTeam() == 0) && !caster.isInvocation() && ArrayUtils.contains(EffectConstant.EFFECTS_HEAL,effectID))
                    targets.stream().filter(fighter -> fighter.getTeam() == 0).forEach(fighter -> challengeLoose(caster));
                break;
            case 20: // Elémentaire
                if ((!caster.isInvocation()) && (caster.getTeam() == 0) && ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_DAMMAGE,effectID) && effectID != 141) {
                    switch (spellid) {
                        case 126://Mot stimulant
                        case 149://Mutilation
                        case 106://Roue de la fortune
                        case 111://Contrecoup
                        case 108://Esprit f�lin
                        case 435://Transfert de vie
                        case 135://Mot de sacrifice
                        case 123://Mot drainant
                            return;
                    }

                    if (Arg == 0) {
                        if(!ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_NOELEM,effectID))
                            Arg = effectID;
                        break;
                    }
                    if (Arg != effectID) {
                        if(ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_EAU,Arg)
                                && ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_EAU,effectID) ) {
                            break;
                        } else if (ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_TERRE,Arg)
                                && ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_TERRE,effectID) ) {
                            break;
                        } else if (ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_AIR,Arg)
                                && ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_AIR,effectID) ) {
                            break;
                        } else if (ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_FEU,Arg)
                                && ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_FEU,effectID)) {
                            break;
                        } else if (ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_NEUTRE,Arg)
                                && ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_NEUTRE,effectID)) {
                            break;
                        }
                        else if(ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_NOELEM,effectID)){
                            break;
                        }
                        challengeLoose(caster);
                        break;
                    }
                }
                break;
            case 21: // Circulez !
                if ((!caster.isInvocation()) && (caster.getTeam() == 0) && ArrayUtils.contains(EffectConstant.EFFECTS_RETPM,effectID)) {
                    for (Fighter target : targets) {
                        if (target.getTeam() == 1) {
                            challengeLoose(caster);
                            break;
                        }
                    }
                }
                break;
            case 22: // Le temps qui court !
                if ((!caster.isInvocation()) && (caster.getTeam() == 0)
                        && ArrayUtils.contains(EffectConstant.EFFECTS_RETPA,effectID)) {
                    for (Fighter target : targets) {
                        if (target.getTeam() == 1) {
                            challengeLoose(caster);
                            break;
                        }
                    }
                }
                break;
            case 23: // Perdu de vue !
                if ((!caster.isInvocation()) && (caster.getTeam() == 0)
                        && ArrayUtils.contains(EffectConstant.EFFECTS_RET_PO,effectID)) {
                    for (Fighter target : targets) {
                        if (target.getTeam() == 1) {
                            challengeLoose(caster);
                            break;
                        }
                    }
                }
                break;
            case 32: // Elitiste
            case 34: // Impr�visible
                if ( (!caster.isInvocation()) &&  (caster.getTeam() == 0) && ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_DAMMAGE_WITHNOELEM,effectID) ) {
                    for (Fighter target : targets) {
                        if (target.getTeam() == 1) {
                            if (_cible == null || _cible.getId() != target.getId())
                                challengeLoose(caster);
                        }
                    }
                }
                break;
            case 38: // Blitzkrieg
                if ((caster.getTeam() == 0) && ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_DAMMAGE_WITHNOELEM,effectID)) {
                    for (Fighter target : targets) {
                        if (target.getTeam() == 1) {
                            StringBuilder id = new StringBuilder();
                            id.append(";").append(target.getId()).append(",");
                            if (!this.Args.contains(id.toString())) {
                                id.append(caster.getId());
                                this.Args += id.toString();
                            }
                        }
                    }
                }
                break;
            case 43: // Abn�gation
                if ((caster.getTeam() == 0) && ArrayUtils.contains(EffectConstant.EFFECTS_HEAL,effectID) && caster.getInvocator() == null)
                    for (Fighter target : targets)
                        if (target.getId() == caster.getId())
                            challengeLoose(caster);
                break;
            case 45: // Duel
            case 46: // Chacun son monstre
                if ((!caster.isInvocation()) && (caster.getTeam() == 0)
                        && ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_DAMMAGE_WITHNOELEM,effectID)) {
                    for (Fighter target : targets) {
                        if (target.getTeam() == 1) {
                            if (!Args.contains(";" + target.getId() + ","))
                                Args += ";" + target.getId() + ","
                                        + caster.getId() + ";";
                            else if (Args.contains(";" + target.getId() + ",")
                                    && !Args.contains(";" + target.getId()
                                    + "," + caster.getId() + ";"))
                                challengeLoose(target);
                        }
                    }
                }
                break;
            case 47: // Contamination
                if (ArrayUtils.contains(EffectConstant.EFFECTS_ATTAK_DAMMAGE_WITHNOELEM,effectID))
                    targets.stream().filter(target -> target.getTeam() == 0 && target.getPdv() != target.getPdvMax()).filter(target -> !Args.contains(";" + target.getId() + ",")).forEach(target -> Args += ";" + target.getId() + "," + "3;");
                break;
        }
    }

    public void onMobDie(Fighter mob, Fighter killer) {
        if (mob.getMob() == null)
            return;
        if (mob.getPlayer() != null)
            return;
        if (mob.getTeam() != 1)
            return;
        if (mob.isInvocation() && mob.getInvocator().getPlayer() != null)
            return;

        boolean isKiller = (killer.getId() != mob.getId());

        if (!challengeAlive)
            return;

        switch (Type) {
            case 3: // D�sign� Volontaire
                if (_cible == null)
                    return;
                if(mob.isInvocation()) return;

                if (mob.getInvocator() != null)
                    if (mob.getInvocator().getId() == _cible.getId())
                        return;

                if (_cible.getId() != mob.getId()) {
                    challengeLoose(fight.getFighterByOrdreJeu());
                } else {
                    challengeWin();
                }
                _cible = null;
                break;

            case 19: // Mains propres
                if ( killer.getTeam() != 0 || killer.isInvocation() || !killer.canPlay())
                    return;

                if (mob.getTeam() == 1 && !mob.isInvocation()) {
                    challengeLoose(killer);
                    break;
                }
                break;

            case 4: // Sursis
                if (_cible == null)
                    return;

                if (_cible.getId() == mob.getId() && !fight.verifIfTeamIsDead()) {
                    challengeLoose(fight.getFighterByOrdreJeu());
                }
                break;

            case 28: // Ni Pioutes ni Soumises
                if (!mob.isInvocation() && isKiller && killer.getPlayer() != null)
                    if (killer.getPlayer().getSexe() == 0) {
                        challengeLoose(fight.getFighterByOrdreJeu());
                    }
                break;

            case 29: // Ni Pious ni Soumis
                if (!mob.isInvocation() && isKiller && killer.getPlayer() != null) {
                    if (killer.getPlayer().getSexe() == 1) {
                        challengeLoose(fight.getFighterByOrdreJeu());
                    }
                }
                break;

            case 31: // Focus
                if (killer.getMob() != null || killer == mob || mob.getLevelUp() || mob.isInvocation())
                    break;
                if (Args.contains("" + mob.getId()))
                    Args = "";
                else
                    challengeLoose(killer);
                break;
            case 32: // Elitiste
                if (_cible.getId() == mob.getId())
                    challengeWin();
                break;

            case 34: // Impr�visible
                _cible = null;
                break;
            case 42: // Deux pour le prix d'un
                if (mob.isInvocation() || killer.isInvocation())
                    return;
                Args += (Args.isEmpty() ? killer.getId() : ";" + killer.getId());
                break;
            case 44: // Partage
            case 46: // Chacun son monstre
                if (isKiller) {
                    if(killer.isInvocation())
                        killer = killer.getInvocator();
                    if(!mob.isInvocation())
                        Args += (Args.isEmpty() ? killer.getId() : ";" + killer.getId());
                }
                break;
            case 30: // Les petits d'abord
            case 48: // Les mules d'abord
                if (mob.isInvocation() || mob.isDouble())
                    return;
                if (mob.getId() != killer.getId()) {
                    int lvlMin = 5000;
                    for (Fighter f : fight.getFighters2(1)) {
                        if (f.isInvocation())
                            continue;
                        if (f.getLvl() < lvlMin)
                            lvlMin = f.getLvl();
                    }
                    if (killer.getLvl() > lvlMin)
                        challengeLoose(fight.getFighterByOrdreJeu());
                }
                break;

            case 35: // Tueur � gages
                if (_cible == null)
                    return;
                if (_cible.getId() != mob.getId()) {
                    if (!mob.isInvocation() && !killer.isInvocation())
                        challengeLoose(fight.getFighterByOrdreJeu());
                } else {
                    try {
                        _cible = null;
                        ArrayList<Fighter> fighters = new ArrayList<Fighter>(fight.getFighters(2));
                        for (Iterator<Fighter> it = fighters.iterator(); it.hasNext(); ) {
                            Fighter f = it.next();
                            if (f.isInvocation() || f.isDead()
                                    || f.getPlayer() != null)
                                it.remove();
                        }
                        Collections.sort(fighters);
                        for (Fighter f : fighters) {
                            if (!f.isInvocation() && !f.isDead()
                                    && f.getPlayer() == null) {
                                _cible = f;
                                break;
                            }
                        }
                        showCibleToFight();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 10: // Cruel
                if (_cible == null)
                    return;
                if (_cible.isInvocation() || _cible.isDouble() || mob.getPlayer() != null || killer.isInvocation())
                    return;
                if (_cible.getId() != mob.getId() && _cible.getLvl() != mob.getLvl()) {
                    if (mob.getLvl() > _cible.getLvl())
                        challengeLoose(fight.getFighterByOrdreJeu());
                } else {
                    try {
                        int levelMin = 2000;
                        for (Fighter fighter : fight.getFighters(2)) {
                            if (fighter.isInvocation() || fighter.isDouble() || fighter.getPlayer() != null || fighter.isDead())
                                continue;

                            if (fighter.getPlayer() == null
                                    && fighter.getLvl() < levelMin) {
                                levelMin = fighter.getLvl();
                                _cible = fighter;
                            }
                        }
                        if (_cible != null)
                            showCibleToFight();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case 25: // Ordonn�
                if (_cible == null)
                    return;
                if (killer.isMob() || mob.isInvocation() || mob.isDouble() || mob.getPlayer() != null)
                    return;

                if (_cible.getId() != mob.getId()) {
                    if (mob.getLvl() < _cible.getLvl())
                        challengeLoose(fight.getFighterByOrdreJeu());
                } else {
                    try {
                        int levelMax = 0;
                        for (Fighter fighter : fight.getFighters(2)) {
                            if (fighter.isInvocation() || fighter.isDouble()
                                    || fighter.getPlayer() != null || fighter.isDead())
                                continue;
                            if (fighter.getLvl() > levelMax) {
                                levelMax = fighter.getLvl();
                                _cible = fighter;
                            }
                        }
                        if (_cible != null)
                            showCibleToFight();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public void onPlayerMove(Fighter fighter) {
        if (!challengeAlive)
            return;
        switch (Type) {
            case 1: // Zombie
                if (this.fight.getCurFighterUsedPm() > 1) // Si l'on a utilis� plus d'un PM
                    //if (fighter.getPm() - fighter.getCurPm(fight) > 1)
                    challengeLoose(fight.getFighterByOrdreJeu());
                break;
        }
    }

    public void onPlayerAction(Fighter fighter, int actionID) {
        if (!challengeAlive || fighter.getTeam() == 1)
            return;
        StringBuilder action = new StringBuilder();
        action.append(";").append(fighter.getId());
        action.append(",").append(actionID).append(";");
        switch (Type) {
            case 6: // Versatile
            case 5: // Econome
                if (lastActions.contains(action.toString()))
                    challengeLoose(fight.getFighterByOrdreJeu());
                lastActions += action.toString();
                break;

            case 24: // Born�
                if (!lastActions.contains(action.toString())
                        && lastActions.contains(";" + fighter.getId() + ","))
                    challengeLoose(fight.getFighterByOrdreJeu());
                lastActions += action.toString();
                break;
        }

    }

    public void onPlayerCac(Fighter fighter) {

        if (!challengeAlive)
            return;

        switch (Type) {
            case 11: // Mystique
                challengeLoose(fight.getFighterByOrdreJeu());
                break;
            case 6: // Versatile
            case 5: // Econome
                StringBuilder action = new StringBuilder();
                action.append(";").append(fighter.getId());
                action.append(",").append("cac").append(";");
                if (lastActions.contains(action.toString()))
                    challengeLoose(fight.getFighterByOrdreJeu());
                lastActions += action.toString();
                break;
        }
    }

    public void onPlayerSpell(Fighter fighter, SpellGrade spellStats) {
        if (!challengeAlive)
            return;
        if (fighter.getPlayer() == null)
            return;
        switch (Type) {
            case 9: // Barbare
                challengeLoose(fight.getFighterByOrdreJeu());
                break;
            case 14: // Casino Royal (sort #101)
                if (fighter.getPlayer() != null)
                    if (spellStats.getSpellID() == 101)
                        Args = "cast";
                break;
        }
    }

    public void onPlayerStartTurn(Fighter fighter) {
        if (!challengeAlive)
            return;
        switch (Type) {
            case 2: // Statue
                if (fighter.getPlayer() == null)
                    return;
                Arg = fighter.getCell().getId();
                break;
            case 6: // Versatile
                lastActions = "";
                break;
            case 14: // Casino Royal (sort #101)
                if (fighter.getPlayer() != null)
                    if (fighter.canLaunchSpell(101))
                        Args = "ok";
                    else Args = "cant";
                break;
            case 34: // Impr�visible
                if (fighter.getTeam() == 1 || fighter.isInvocation())
                    return;
                try {
                    int noBoucle = 0, GUID = 0;
                    _cible = null;
                    while (_cible == null) {
                        if (_ordreJeu.size() > 0) {
                            GUID = Formulas.getRandomValue(0, _ordreJeu.size() - 1);
                            Fighter f = _ordreJeu.get(GUID);
                            if (f.getPlayer() == null && !f.isDead() && !f.isInvocation())
                                _cible = f;
                            noBoucle++;
                            if (noBoucle > 150)
                                return;
                        }
                    }
                    showCibleToFight();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 38: // Blitzkrieg
                if (fighter.getTeam() == 1 && Args.contains(";" + fighter.getId() + ",")) {
                    if (fighter.isDead()) return;

                    int id = 0;

                    for (String string : this.Args.split(";")) {
                        if (string.contains("" + fighter.getId())) {
                            for (String test : string.split(","))
                                id = Integer.parseInt(test);
                            break;
                        }
                    }

                    for (Fighter target : this.fight.getFighters(1))
                        if (target.getId() == id)
                            if(fighter.getPdv() != fighter.getPdvMax())
                                challengeLoose(target);
                }
                break;
            case 47: // Contamination
                if (fighter.getTeam() == 0) {
                    String str = ";" + fighter.getId() + ",";
                    if (Args.contains(str + "1;"))
                        challengeLoose(fighter);
                    else if (Args.contains(str + "2;"))
                        Args += str + "1;";
                    else if (Args.contains(str + "3;"))
                        Args += str + "2;";
                }
                break;
        }
    }

    public void onPlayerEndTurn(Fighter fighter) {
        if (!challengeAlive)
            return;

        boolean hasFailed = false;
        ArrayList<Fighter> fighters = PathFinding.getFightersAround(fighter.getCell().getId(), fight.getMap(), fight);

        switch (Type) {
            case 1: // Zombie
                if (this.fight.getCurFighterUsedPm() <= 0) // Si l'on a pas boug�
                    challengeLoose(fighter);
                break;

            case 2: // Statue
                if (fighter.getPlayer() != null)
                    if (fighter.getCell().getId() != Arg & !fighter.getPlayer().passturn)
                        challengeLoose(fighter);
                break;

            case 7: // Jardinier (sort #367)
                if (fighter.getPlayer() != null)
                    if (fighter.canLaunchSpell(367))
                        challengeLoose(fighter);
                break;

            case 8: // Nomade
                //System.out.println(fighter.getCurPm(this.fight));
                if (!fighter.isInvocation() && fighter.getCurPm(this.fight) > 0)  //this.fight.getCurFighterPm() != 0
                    challengeLoose(fighter);
                break;

            case 12: // Fossoyeur (sort #373)
                if (fighter.getPlayer() != null)
                    if (fighter.canLaunchSpell(373))
                        challengeLoose(fighter);
                break;

            case 14: // Casino Royal (sort #101)
                if (fighter.getPlayer() != null)
                    if(Args.equals("ok"))
                        challengeLoose(fighter);
                break;

            case 15: // Araknophile (sort #370)
                if (fighter.getPlayer() != null)
                    if (fighter.canLaunchSpell(370))
                        challengeLoose(fighter);
                break;

            case 36: // Hardi
                hasFailed = true;
                if (!fighters.isEmpty())
                    for (Fighter f : fighters)
                        if (f.getTeam() != fighter.getTeam())
                            hasFailed = false;
                break;

            case 37: // Collant
                hasFailed = true;
                if (!fighters.isEmpty())
                    for (Fighter f : fighters)
                        if (f.getTeam() == fighter.getTeam())
                            hasFailed = false;
                break;

            case 39: // Anachor�te
                if (!fighters.isEmpty())
                    fighters.stream().filter(f -> f.getTeam() == fighter.getTeam()).forEach(f -> challengeLoose(fighter));
                break;

            case 40: // Pusillanime
                if (!fighters.isEmpty())
                    fighters.stream().filter(f -> f.getTeam() != fighter.getTeam()).forEach(f -> challengeLoose(fighter));
                break;

            case 41: // P�tulant
                if (!fighter.isInvocation() && fighter.getCurPa(this.fight) > 0)
                    challengeLoose(fighter);
                break;

            case 42: // Deux pour le prix d'un
                if (!Args.isEmpty())
                    if (!(Args.split(";").length % 2 == 0))
                        hasFailed = true;
                Args = "";
                break;
        }
        if (hasFailed) challengeLoose(fighter);
    }
}
