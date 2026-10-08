package fight.ia.util;

import area.map.GameCase;
import area.map.GameMap;
import common.Formulas;
import common.PathFinding;
import common.SocketManager;
import fight.Fight;
import fight.Fighter;
import fight.spells.*;
import fight.traps.Glyph;
import game.action.GameAction;
import game.world.World;
import guild.Guild;
import kernel.Constant;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * Created by Locos on 04/10/2015.
 */
public class Function {

    private final static Function instance = new Function();

    public static Function getInstance() {
        return instance;
    }
    
    public int attackIfPossiblerat(Fight fight, Fighter fighter, Fighter target, boolean loin)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SpellGrade SS = null;
        for(Map.Entry<Integer, SpellGrade> entry : fighter.getMob().getSpells().entrySet())
        {
            SpellGrade a = entry.getValue();
            if(a.getSpellID() == 489 && loin)
                SS = a;
            if(a.getSpellID() == 646 && !loin)
                SS = a;
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public boolean attackIfPossibleTot(Fight fight, Fighter caster, Fighter target)
    {
        int pa = caster.getCurPa(fight);
        Map<Integer, SpellGrade> totSpells = caster.getMob().getSpells();
        SpellGrade Mythos = null;
        SpellGrade Destinos = null;
        for(Integer spellid : totSpells.keySet())
        {
            if(spellid == 812)
            {
                Destinos = totSpells.get(spellid);
            }
            if(spellid == 813)
            {
                Mythos = totSpells.get(spellid);
            }
        }
        for(int i = 0; i < 2; i++)
        {
            if(fight.canCastSpell1(caster, Mythos, target.getCell(), -1)) {
                fight.tryCastSpell(caster, Mythos, target.getCell().getId());
            }
        }
        ArrayList<Fighter> ennemies = fight.getEnnemiesAroundPlayerCac(caster);
        if(!ennemies.isEmpty())
        {
            if(fight.canCastSpell1(caster, Destinos, caster.getCell(), -1))
            {
                fight.tryCastSpell(caster, Destinos, caster.getCell().getId());
            }
        }
        return true;
    }

    public boolean TPIfPossiblesphinctercell(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null || target == null)
            return false;

        SpellGrade spell = null;
        for(SpellGrade s : fighter.getMob().getSpells().values()) {
            if(s.getSpellID() == 1016)
                spell = s;
        }

        if(spell != null) {
            int cell = getMaxCellForTP(fight, fighter, target, spell.getMaxPO());
            if(fight.canCastSpell1(fighter, spell, fight.getMap().getCase(cell), -1)) {
                fight.tryCastSpell(fighter, spell, cell);
                return true;
            } else {
                byte count = 0;
                List<Integer> cells = new ArrayList<>();

                while (count != 4) {
                    int nearestCell = PathFinding.getAvailableCellArround(fight, target.getCell().getId(), cells);

                    if (nearestCell == 0)
                        break;
                    if (fight.canCastSpell1(fighter, spell, fight.getMap().getCase(nearestCell), -1)) {
                        fight.tryCastSpell(fighter, spell, nearestCell);
                        return true;
                    } else {
                        cells.add(nearestCell);
                    }
                    count++;
                }
            }

        }

        return false;
    }

    public int attackIfPossiblesphinctercell(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return -1;
        SpellGrade SS = null;
        for(Map.Entry<Integer, SpellGrade> entry : fighter.getMob().getSpells().entrySet())
        {
            SpellGrade a = entry.getValue();
            if(a.getSpellID() == 1017)
                SS = a;
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public int attackIfPossiblePerco(Fight fight, Fighter fighter, Fighter target, List<SpellGrade> spells) // 0 = Rien
    {
        if(fight == null || fighter == null || target == null)
        {
            return -1;
        }
        ArrayList<SpellGrade> SpellAttack = new ArrayList<>();
        ArrayList<SpellGrade> SpellMalus = new ArrayList<>();
        for(SpellGrade spell : spells)
        {
            if(spell.getSpellID() == 455 | spell.getSpellID() == 456 | spell.getSpellID() == 457 |spell.getSpellID() == 458)
            {
                SpellAttack.add(spell);
            }
            else if(spell.getSpellID() == 462)
            {
                SpellMalus.add(spell);
            }
        }
        int dist = PathFinding.getDistanceBetweenTwoCase(fight.getMap(), fighter.getCell(), target.getCell());
        int CurPa = fighter.getCurPa(fight);
        for(SpellGrade spell : SpellMalus)
        {
            if(fight.canCastSpell1(fighter, spell, target.getCell(), -1) & CurPa >= spell.getPACost())
            {
                fight.tryCastSpell(fighter, spell, target.getCell().getId());
                CurPa -= spell.getPACost();
            }
        }
        for(SpellGrade spell : SpellAttack)
        {
            if(fight.canCastSpell1(fighter, spell, target.getCell(), -1) & CurPa >= spell.getPACost())
            {
                fight.tryCastSpell(fighter, spell, target.getCell().getId());
                CurPa -= spell.getPACost();
            }
        }
        return 0;
    }

    public int attackIfPossibleCroca(Fight fight, Fighter fighter, Fighter target) // 0 = Rien; -1 = fight ou fighter null; 555 = Sort null; 666 = target null
    {
        if(fight == null || fighter == null)
            return -1;
        SpellGrade SS = null;
        for(Map.Entry<Integer, SpellGrade> entry : fighter.getMob().getSpells().entrySet())
        {
            SpellGrade a = entry.getValue();
            if(a.getSpellID() == 971)
            {
                SS = a;
            }
        }
        if(SS == null)
        {
            return 555;
        }
        if(target == null)
        {
            return 666;
        }
        char dir = PathFinding.getDirBetweenTwoCase(fighter.getCell().getId(), target.getCell().getId(), fight.getMap(), true);
        if(PathFinding.casesAreInSameLine(fight.getMap(), fighter.getCell().getId(), target.getCell().getId(), dir, 21))
        {
            if(PathFinding.getDistanceBetweenTwoCase(fight.getMap(), fighter.getCell(), target.getCell()) <= 21)
            {
                int dist = PathFinding.getDistanceBetweenTwoCase(fight.getMap(), fighter.getCell(), target.getCell());
                if(dist > 10)
                {
                    int caseToTarget = PathFinding.getCaseIdWithPo(fighter.getCell().getId(), dir, dist);
                    if(fight.getMap().getCase(caseToTarget) != null)
                    {
                        int attack = fight.tryCastSpell(fighter, SS, caseToTarget);
                        if(attack != 0)
                        {
                            return attack;
                        }else {
                            return 0;
                        }
                    }
                }else {
                    int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
                    if (attack != 0) {
                        return attack;
                    }else {
                        return 0;
                    }
                }
            }
        }
        return 777;
    }

    public boolean tryTurtleInvocation(Fight fight, Fighter fighter) {
        if (fight == null || fighter == null)
            return false;

        SpellGrade spell = null;
        for (SpellGrade s : fighter.getMob().getSpells().values()) {
            if (s.getSpellID() == 1018) {
                spell = s;
                break;
            }
        }

        if (spell != null) {
            for (Fighter target : fight.getFighters(3)) {
                if (target.getTeam() == fighter.getTeam()) continue;
                List<Integer> cells = new ArrayList<>();
                int nearestCell = PathFinding.getAvailableCellArround(fight, target.getCell().getId(), cells);

                if (nearestCell == 0)
                    break;
                if (fight.canCastSpell1(fighter, spell, fight.getMap().getCase(nearestCell), -1)) {
                    fight.tryCastSpell(fighter, spell, nearestCell);
                    return true;
                } else {
                    cells.add(nearestCell);
                }
            }
        }

        List<SpellGrade> spells = new ArrayList<>();
        spells.add(spell);
        return Function.getInstance().invocIfPossible(fight, fighter, spells);
    }

    public Fighter getNearest(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Fighter curF = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
            if (d < dist)
            {
                dist = d;
                curF = f;
            }
        }
        return curF;
    }

    public int attackIfPossibleAll(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SpellGrade SS = getBestSpellForTarget(fight, fighter, target, fighter.getCell().getId());
        if (target == null)
            return 0;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public int moveToAttackIfPossibleAll(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return -1;
        Fighter target = getNearest(fight, fighter);
        int distMin = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), target.getCell().getId());
        ArrayList<SpellGrade> sorts = getLaunchableSort(fighter, fight, distMin);
        if (sorts == null)
            return -1;
        ArrayList<Integer> cells = PathFinding.getListCaseFromFighter(fight, fighter, fighter.getCell().getId(), sorts);
        if (cells == null)
            return -1;
        int CellDest = 0;
        SpellGrade bestSS = null;
        int[] bestInvok = {1000, 0, 0, 0, -1};
        int[] bestFighter = {1000, 0, 0, 0, -1};
        int targetCell = -1;
        for (int i : cells)
        {
            for (SpellGrade S : sorts)
            {
                if (fight.canCastSpell1(fighter, S, target.getCell(), i))
                {
                    int dist = PathFinding.getDistanceBetween(fight.getMapOld(), fighter.getCell().getId(), target.getCell().getId());
                    if (!PathFinding.isNextTo(fighter.getFight().getMap(), fighter.getCell().getId(), target.getCell().getId()))
                    {
                        if (target.isInvocation())
                        {
                            if (dist < bestInvok[0])
                            {
                                bestInvok[0] = dist;
                                bestInvok[1] = i;
                                bestInvok[2] = 1;
                                bestInvok[3] = 1;
                                bestInvok[4] = target.getCell().getId();
                                bestSS = S;
                            }

                        }
                        else
                        {
                            if (dist < bestFighter[0])
                            {
                                bestFighter[0] = dist;
                                bestFighter[1] = i;
                                bestFighter[2] = 1;
                                bestFighter[3] = 0;
                                bestFighter[4] = target.getCell().getId();
                                bestSS = S;
                            }

                        }
                    }
                    else
                    {
                        if (dist < bestFighter[0])
                        {
                            bestFighter[0] = dist;
                            bestFighter[1] = i;
                            bestFighter[2] = 1;
                            bestFighter[3] = 0;
                            bestFighter[4] = target.getCell().getId();
                            bestSS = S;
                        }
                    }
                }
            }
        }
        if (bestFighter[1] != 0)
        {
            CellDest = bestFighter[1];
            targetCell = bestFighter[4];
        }
        else if (bestInvok[1] != 0)
        {
            CellDest = bestInvok[1];
            targetCell = bestInvok[4];
        }
        else
            return -1;
        if (CellDest == 0)
            return -1;
        if (CellDest == fighter.getCell().getId())
            return targetCell + bestSS.getSpellID() * 1000;
        ArrayList<GameCase> path = new AstarPathfinding(fight.getMapOld(), fight, fighter.getCell().getId(), CellDest).getShortestPath(-1);
        if (path == null)
            return -1;
        String pathstr = "";
        try
        {
            int curCaseID = fighter.getCell().getId();
            int curDir = 0;
            path.add(fight.getMapOld().getCase(CellDest));
            for (GameCase c : path)
            {
                if (curCaseID == c.getId())
                    continue; // Emp�che le d == 0
                char d = PathFinding.getDirBetweenTwoCase(curCaseID, c.getId(), fight.getMap(), true);
                if (d == 0)
                    return -1;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (path.indexOf(c) != 0)
                        pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
                    pathstr += d;
                }
                curCaseID = c.getId();
            }
            if (curCaseID != fighter.getCell().getId())
                pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        GameAction GA = new GameAction(0, 1, "");
        GA.args = pathstr;
        fight.onFighterDeplace(fighter, GA);

        return targetCell + bestSS.getSpellID() * 1000;
    }

    public int attackIfPossiblesacrifier(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SpellGrade SS = null;
        for(Map.Entry<Integer, SpellGrade> entry : fighter.getMob().getSpells().entrySet())
        {
            SpellGrade a = entry.getValue();
            if(a.getSpellID() == 233)
                SS = a;
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public int IfPossibleRasboulvulner(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SpellGrade SS = null;
        for(Map.Entry<Integer, SpellGrade> entry : fighter.getMob().getSpells().entrySet()) {
            SpellGrade a = entry.getValue();
            if(a.getSpellID() == 1039)
                SS = a;
        }
        if (target == null)
            return 666;
        if(fighter.getPa() < 14)
            return 0;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());

        if (attack != 0) {
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 1039, target.getId() + "", target.getId() + ",+" + 1);
            return attack;
        }
        return 0;
    }

    public boolean invoctantaIfPossible(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return false;
        if (fighter.nbInvocation() >= 4)
            return false;
        Fighter nearest = getNearestEnnemy(fight, fighter);
        ArrayList<GameCase> nearestCell = new ArrayList<GameCase>();
        int limit = 2000;
        int _loc0_ = 0;
        SpellGrade spell = null;
        if (fighter.haveState(36))
        {
            spell = World.world.getSort(1110).getStatsByLevel(5);
            fighter.setState(36, 0);
        }
        if (fighter.haveState(37))
        {
            spell = World.world.getSort(1109).getStatsByLevel(5);
            fighter.setState(37, 0);
        }
        if (fighter.haveState(38))
        {
            spell = World.world.getSort(1108).getStatsByLevel(5);
            fighter.setState(38, 0);
        }
        if (fighter.haveState(35))
        {
            spell = World.world.getSort(1107).getStatsByLevel(5);
            fighter.setState(35, 0);
        }
        if(nearest == null) {
            for (Fighter target : fight.getFighters(fighter.getOtherTeam())) {
                if (target.getTeam() == fighter.getTeam()) {
                    continue;
                }
                nearestCell = PathFinding.getAvailableCellsTowardCible(fighter.getCell().getId(),fight.getMapOld(), target,fight);
                if (nearestCell.isEmpty()) {
                    break;
                }
                for (GameCase cell : nearestCell) {
                    if (fight.canCastSpell1(fighter, spell, cell, -1)) {
                        fight.tryCastSpell(fighter, spell, cell.getId());
                        return true;
                    }
                }
            }
        }
        else{
            Fighter ennemyforInvo = Function.getInstance().getNearestEnnemynbrcasemax(fight, fighter, 1, 50);
            nearestCell = PathFinding.getAvailableCellsTowardCible(fighter.getCell().getId(),fight.getMapOld(), ennemyforInvo,fight);
            if (nearestCell.isEmpty()) {
                return false;
            }
            for (GameCase cell : nearestCell) {
                if (fight.canCastSpell1(fighter, spell, cell, -1)) {
                    fight.tryCastSpell(fighter, spell, cell.getId());
                    return true;
                }
            }
        }
       /* while (_loc0_++ < limit)
        {
            nearestCell = PathFinding.get(fight.getMap(), nearestCell, nearest.getCell().getId(), null);
            if(nearestCell > -1)
            {
                _loc0_ = limit;
            }
        }
        if (nearestCell == -1) {
            nearestCell = getNearestFreeCell(fight, nearest);
            if (nearestCell == -1) {
                return false;
            }
        }
        if (spell == null)
            return false;
        int invoc = fight.tryCastSpell(fighter, spell, nearestCell);
        if (invoc != 0)
            return false;*/
        return true;
    }

    public boolean buffIfPossibleKrala(Fight fight, Fighter fighter, Fighter target)
    {
        if (fight == null || fighter == null)
            return false;
        if (target == null)
            return false;
        SpellGrade SS = null;
        if (fighter.haveState(31) && fighter.haveState(32) && fighter.haveState(33) && fighter.haveState(34))
        {
            SS = World.world.getSort(1106).getStatsByLevel(5);
        }
        if (SS == null)
            return false;
        int buff = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (buff != 0)
            return false;
        return true;
    }

    public boolean buffIfPossibleKitsou(Fight fight, Fighter fighter, Fighter target)
    {
        if (fight == null || fighter == null)
            return false;
        if (target == null)
            return false;
        SpellGrade SS = null;
        SS = World.world.getSort(521).getStatsByLevel(5);
        if (SS == null)
            return false;
        int buff = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (buff != 0)
            return false;
        return true;
    }

    public boolean buffIfPossibleTortu(Fight fight, Fighter fighter, Fighter target)
    {
        if (fight == null || fighter == null)
            return false;
        if (target == null)
            return false;
        SpellGrade SS = null;
        for(Map.Entry<Integer, SpellGrade> entry : fighter.getMob().getSpells().entrySet())
        {
            SpellGrade a = entry.getValue();
            if(a.getSpellID() == 1019)
                SS = a;
        }
        if (SS == null)
            return false;
        int buff = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (buff != 0)
            return false;
        return true;
    }

    public int tpIfPossibleTynril(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SpellGrade SS = null;
        for(Map.Entry<Integer, SpellGrade> entry : fighter.getMob().getSpells().entrySet())
        {
            SpellGrade a = entry.getValue();
            if(a.getSpellID() == 1060)
                SS = a;
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public int pmgongon(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SpellGrade SS = null;
        for(Map.Entry<Integer, SpellGrade> entry : fighter.getMob().getSpells().entrySet())
        {
            SpellGrade a = entry.getValue();
            if(a.getSpellID() == 284)
                SS = a;
        }
        if (target == null)
            return 0;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public int tpIfPossibleRasboul(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SpellGrade SS = null;
        for(Map.Entry<Integer, SpellGrade> entry : fighter.getMob().getSpells().entrySet())
        {
            SpellGrade a = entry.getValue();
            if(a.getSpellID() == 1041)
                SS = a;
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public boolean HealIfPossiblefriend(Fight fight, Fighter f, Fighter target)//boolean pour choisir entre auto-soin ou soin alli�
    {
        if (fight == null || f == null|| target == null)
            return false;
        if (f.isDead())
            return false;
        SpellGrade SS = null;


        Fighter curF = null;
        int PDVPERmin = 100;
        SpellGrade curSS = null;
        for (Fighter F : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (F == f)
                continue;
            if (F.isDead())
                continue;
            if (F.getTeam() == f.getTeam())
            {
                int PDVPER = (F.getPdv() * 100) / F.getPdvMax();
                if (PDVPER < PDVPERmin && PDVPER < 95)
                {
                    int infl = 0;
                    if (f.isCollector())
                    {
                        for (Map.Entry<Integer, SpellGrade> ss : World.world.getGuild(f.getCollector().getGuildId()).getSpells().entrySet())
                        {
                            if (ss.getValue() == null)
                                continue;
                            if (infl < calculInfluenceHeal(ss.getValue())
                                    && calculInfluenceHeal(ss.getValue()) != 0
                                    && fight.canCastSpell1(f, ss.getValue(), F.getCell(), -1))//Si le sort est plus interessant
                            {
                                infl = calculInfluenceHeal(ss.getValue());
                                curSS = ss.getValue();
                            }
                        }
                    }
                    else
                    {
                        for (Map.Entry<Integer, SpellGrade> ss : f.getMob().getSpells().entrySet())
                        {
                            if (infl < calculInfluenceHeal(ss.getValue())
                                    && calculInfluenceHeal(ss.getValue()) != 0
                                    && fight.canCastSpell1(f, ss.getValue(), F.getCell(), -1))//Si le sort est plus interessant
                            {
                                infl = calculInfluenceHeal(ss.getValue());
                                curSS = ss.getValue();
                            }
                        }
                    }
                    if (curSS != SS && curSS != null)
                    {
                        curF = F;
                        SS = curSS;
                        PDVPERmin = PDVPER;
                    }
                }
            }
        }
        target = curF;
        if (target == null)
            return false;
        if (target.isFullPdv())
            return false;
        if (SS == null)
            return false;
        int heal = fight.tryCastSpell(f, SS, target.getCell().getId());
        if (heal != 0)
            return false;

        return true;
    }

    public int tpIfPossibleKaskargo(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SpellGrade SS = null;
        for(Map.Entry<Integer, SpellGrade> entry : fighter.getMob().getSpells().entrySet())
        {
            SpellGrade a = entry.getValue();
            if(a.getSpellID() == 445)
                SS = a;
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public int attackIfPossibleKaskargo(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null) return 0;

        SpellGrade spellStat = null;
        int cell = PathFinding.getCaseBetweenEnemy(fighter.getCell().getId(), fight.getMap(), fight);

        for (SpellGrade spellStats : fighter.getMob().getSpells().values())
            if(spellStats.getSpellID() == 949) {
                spellStat = spellStats; break; }

        int i = 10;
        while(i > 0) {
            for(Glyph glyph : fight.getAllGlyphs())
                if(glyph != null && glyph.getCell().getId() == cell)
                    cell = PathFinding.getCaseBetweenEnemy(fighter.getCell().getId(), fight.getMap(), fight);
            i--;
        }

        if (target == null) return 666;
        int attack = fight.tryCastSpell(fighter, spellStat, cell);
        if (attack != 0) return attack;
        return 0;
    }

    public int attackIfPossiblePeki(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null || target == null)
            return 0;
        SpellGrade SS = null;
        int cell = 0;
        for (Map.Entry<Integer, SpellGrade> S : fighter.getMob().getSpells().entrySet())
        {
            int cellID = target.getCell().getId();
            if(S.getValue().getSpellID() == 1280)
            {
                cell = cellID;
                SS = S.getValue();
            }
        }
        int attack = fight.tryCastSpell(fighter, SS, cell);

        if (attack != 0)
            return 2000;
        return 0;
    }

    public int attackIfPossibleRN(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null || target == null)
            return 0;
        SpellGrade SS = null;
        int cell = 0;
        for (Map.Entry<Integer, SpellGrade> S : fighter.getMob().getSpells().entrySet())
        {
            int cellID = target.getCell().getId();
            if(S.getValue().getSpellID() == 1006)
            {
                cell = cellID;
                SS = S.getValue();
            }
        }
        if(!fight.canCastSpell1(fighter, SS, fight.getMap().getCase(cell), -1))
            return 0;
        int attack = fight.tryCastSpell(fighter, SS, cell);

        if (attack != 0)
            return 2000;
        return 0;
    }

    public int attackIfPossibleBuveur(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SpellGrade SS = null;
        int cell = 0;
        for (Map.Entry<Integer, SpellGrade> S : fighter.getMob().getSpells().entrySet())
        {
            int cellID = fighter.getCell().getId();
            if(S.getValue().getSpellID() == 808)
            {
                cell = cellID;
                SS = S.getValue();
            }
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, cell);

        if (attack != 0)
            return 800;
        return 0;
    }

    public int attackIfPossibleWobot(Fight fight, Fighter fighter)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SpellGrade SS = null;
        int cell = 0;
        for (Map.Entry<Integer, SpellGrade> S : fighter.getMob().getSpells().entrySet())
        {
            int cellID = PathFinding.getCaseBetweenEnemy(fighter.getCell().getId(), fight.getMap(), fight);
            if(S.getValue().getSpellID() == 335)
            {
                cell = cellID;
                SS = S.getValue();
            }
        }
        int attack = fight.tryCastSpell(fighter, SS, cell);

        if (attack != 0)
            return 800;
        return 0;
    }

    public int attackIfPossibleTynril(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SpellGrade SS = null;
        if (target == null)
            return 666;
        if(fighter.getMob().getTemplate().getId() == 1087) {//ahuri  /faiblesse terre
            if(target.hasBuff(215)) {
                SS = findSpell(fighter, 1059);
            } else {
                SS = findSpell(fighter, 1058);
            }
        } else if(fighter.getMob().getTemplate().getId() == 1085) {//deconcerter  /faiblesse eau
            if(target.hasBuff(216)) {
                SS = findSpell(fighter, 1059);
            } else {
                SS = findSpell(fighter, 1058);
            }
        } else if(fighter.getMob().getTemplate().getId() == 1072) {//consterner  /faiblesse air
            if(target.hasBuff(217)) {
                SS = findSpell(fighter, 1059);
            } else {
                SS = findSpell(fighter, 1058);
            }
        } else if(fighter.getMob().getTemplate().getId() == 1086) {//perfide  /faiblesse feu
            if(target.hasBuff(218)) {
                SS = findSpell(fighter, 1059);
            } else {
                SS = findSpell(fighter, 1058);
            }
        }

        if(fight.canCastSpell1(fighter, SS, target.getCell(), -1)) {
            return fight.tryCastSpell(fighter, SS, target.getCell().getId());
        }
        return 0;
    }

    public SpellGrade findSpell(Fighter fighter, int id) {
        for(SpellGrade spell : fighter.getMob().getSpells().values()) {
            if(spell != null && spell.getSpellID() == id)
                return spell;
        }
        return null;
    }

    public boolean moveNearIfPossible(Fight fight, Fighter F, Fighter T)
    {
        if (fight == null)
            return false;
        if (F == null)
            return false;
        if (T == null)
            return false;
        if (F.getCurPm(fight) <= 0)
            return false;
        GameMap map = fight.getMap();
        if (map == null)
            return false;
        GameCase cell = F.getCell();
        if (cell == null)
            return false;
        GameCase cell2 = T.getCell();
        if (cell2 == null)
            return false;
        if (PathFinding.isNextTo(map, cell.getId(), cell2.getId()))
            return false;

        int cellID = PathFinding.getNearestCellAround(map, cell2.getId(), cell.getId(), null);
        //On demande le chemin plus court
        //Mais le chemin le plus court ne prend pas en compte les bords de map.
        if (cellID == -1)
        {
            Map<Integer, Fighter> ennemys = getLowHpEnnemyList(fight, F);
            for (Map.Entry<Integer, Fighter> target : ennemys.entrySet())
            {
                int cellID2 = PathFinding.getNearestCellAround(map, target.getValue().getCell().getId(), cell.getId(), null);
                if (cellID2 != -1)
                {
                    cellID = cellID2;
                    break;
                }
            }
        }
        ArrayList<GameCase> path = new AstarPathfinding(fight.getMapOld(), fight, cell.getId(), cell2.getId()).getShortestPath(-1);
        if (path == null || path.isEmpty())
            return false;
        ArrayList<GameCase> finalPath = new ArrayList<GameCase>();
        for (int a = 0; a < F.getCurPm(fight); a++)
        {
            if (path.size() == a)
                break;
            finalPath.add(path.get(a));
        }
        String pathstr = "";
        try
        {
            int curCaseID = cell.getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = PathFinding.getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
                if (d == 0)
                    return false;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
                    pathstr += d;
                }
                curCaseID = c.getId();
            }
            if (curCaseID != cell.getId())
                pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        GameAction GA = new GameAction(0, 1, "");
        GA.args = pathstr;
        boolean result = fight.onFighterDeplace(F, GA);

        return result;
    }

    public int getMaxCellForTP(Fight fight, Fighter F, Fighter T, int dist) {
        if (fight == null || F == null || T == null || dist < 1)
            return -1;

        GameMap map = fight.getMap();
        GameCase cell = F.getCell(), cell2 = T.getCell();

        if (map == null || cell == null || cell2 == null || PathFinding.isNextTo(map, cell.getId(), cell2.getId()))
            return -1;

        ArrayList<GameCase> path = new AstarPathfinding(fight.getMapOld(), fight, cell.getId(), cell2.getId()).getShortestPath(-1);

        if (path == null || path.isEmpty())
            return -1;

        int cellId = -1;

        for (int a = 0; a < dist; a++) {
            if (path.size() == a) break;
            cellId = path.get(a).getId();
        }

        return cellId;
    }

    public int attackIfPossibleDiscipleimpair(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SpellGrade SS = null;
        for(Map.Entry<Integer, SpellGrade> entry : fighter.getMob().getSpells().entrySet())
        {
            SpellGrade a = entry.getValue();
            if(a.getSpellID() == 3501)
                SS = a;
        }
        if (target == null)
            return 666;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return attack;
        return 0;
    }

    public int attackBondIfPossible(Fight fight, Fighter fighter, Fighter target)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        int cell = 0;
        SpellGrade SS2 = null;

        if(target == null)
            return 0;
        for (Map.Entry<Integer, SpellGrade> S : fighter.getMob().getSpells().entrySet())
        {
            int cellID = PathFinding.getCaseBetweenEnemy(target.getCell().getId(), fight.getMap(), fight);
            boolean effet4 = false;
            boolean effet6 = false;

            for(Effect f : S.getValue().getEffects())
            {
                if(f.getEffectID() == 4)
                    effet4 = true;
                if(f.getEffectID() == 6)
                {
                    effet6 = true;
                    effet4 = true;
                }
            }
            if(effet4 == false)
                continue;
            if(effet6 == false)
            {
                cell = cellID;
                SS2 = S.getValue();
            }else
            {
                cell = target.getCell().getId();
                SS2 = S.getValue();
            }
        }
        if (cell >= 15 && cell <= 463 && SS2 != null)
        {
            int attack = fight.tryCastSpell(fighter, SS2, cell);
            if (attack != 0)
                return SS2.getSpell().getDuration();
        }
        else
        {
            if (target == null || SS2 == null)
                return 0;
            int attack = fight.tryCastSpell(fighter, SS2, cell);
            if (attack != 0)
                return SS2.getSpell().getDuration();
        }
        return 0;
    }

    public int attackIfPossibleDisciplepair(Fight fight, Fighter fighter, Fighter target)
    {// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
        if (fight == null || fighter == null) return 0;
        SpellGrade SS = null;

        for(SpellGrade spellStats : fighter.getMob().getSpells().values())
            if(spellStats.getSpellID() == 3500)
                SS = spellStats;

        if (target == null) return 666;

        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());

        if (attack != 0) return attack;
        return 0;
    }

    public int moveFarIfPossible(Fight fight, Fighter F)
    {
        if (fight == null || F == null)
            return 0;
        if (fight.getMap() == null)
            return 0;
        int nbrcase = 0;
        //On cr�er une liste de distance entre ennemi et de cellid, nous permet de savoir si un ennemi est coll� a nous
        int dist[] = {1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}, cell[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < 10; i++)//on repete 10 fois pour les 10 joueurs ennemis potentielle
        {
            for (Fighter f : fight.getFighters(3))
            {

                if (f.isDead())
                    continue;
                if (f == F || f.getTeam() == F.getTeam())
                    continue;
                int cellf = f.getCell().getId();
                if (cellf == cell[0] || cellf == cell[1] || cellf == cell[2]
                        || cellf == cell[3] || cellf == cell[4]
                        || cellf == cell[5] || cellf == cell[6]
                        || cellf == cell[7] || cellf == cell[8]
                        || cellf == cell[9])
                    continue;
                int d = 0;
                d = PathFinding.getDistanceBetween(fight.getMap(), F.getCell().getId(), f.getCell().getId());
                if (d < dist[i])
                {
                    dist[i] = d;
                    cell[i] = cellf;
                }
                if (dist[i] == 1000)
                {
                    dist[i] = 0;
                    cell[i] = F.getCell().getId();
                }
            }
        }
        //if(dist[0] == 0)return false;//Si ennemi "coll�"

        int dist2[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int PM = F.getCurPm(fight), caseDepart = F.getCell().getId(), destCase = F.getCell().getId();
        ArrayList<Integer> caseUse = new ArrayList<Integer>();
        caseUse.add(caseDepart); // On ne revient pas a sa position de d�part
        for (int i = 0; i <= PM; i++)//Pour chaque PM on analyse la meilleur case a prendre. C'est a dire la plus �liogn�e de tous.
        {
            if (destCase > 0)
                caseDepart = destCase;
            int curCase = caseDepart;

            /** En +15 **/
            curCase += 15;
            int infl = 0, inflF = 0;
            for (int a = 0; a < 10 && dist[a] != 0; a++)
            {
                dist2[a] = PathFinding.getDistanceBetween(fight.getMap(), curCase, cell[a]);//pour chaque ennemi on calcul la nouvelle distance depuis cette nouvelle case (curCase)
                if (dist2[a] > dist[a])//Si la cellule (curCase) demander et plus distante que la pr�cedente de l'ennemi alors on dirrige le mouvement vers elle
                    infl++;
            }

            if (infl > inflF
                    && curCase >= 15
                    && curCase <= 463
                    && testCotes(destCase, curCase)
                    && fight.getMap().getCase(curCase).isWalkable(false, true, -1)
                    && fight.getMap().getCase(curCase).getFighters().isEmpty()
                    && !caseUse.contains(curCase))//Si l'influence (infl) est la plus forte en comparaison avec inflF on garde la case si celle-ci est valide
            {
                inflF = infl;
                destCase = curCase;
            }
            /** En +15 **/

            /** En +14 **/
            curCase = caseDepart + 14;
            infl = 0;

            for (int a = 0; a < 10 && dist[a] != 0; a++)
            {
                dist2[a] = PathFinding.getDistanceBetween(fight.getMap(), curCase, cell[a]);
                if (dist2[a] > dist[a])
                    infl++;
            }

            if (infl > inflF
                    && curCase >= 15
                    && curCase <= 463
                    && testCotes(destCase, curCase)
                    && fight.getMap().getCase(curCase).isWalkable(false, true, -1)
                    && fight.getMap().getCase(curCase).getFighters().isEmpty()
                    && !caseUse.contains(curCase))
            {
                inflF = infl;
                destCase = curCase;
            }
            /** En +14 **/

            /** En -15 **/
            curCase = caseDepart - 15;
            infl = 0;
            for (int a = 0; a < 10 && dist[a] != 0; a++)
            {
                dist2[a] = PathFinding.getDistanceBetween(fight.getMap(), curCase, cell[a]);
                if (dist2[a] > dist[a])
                    infl++;
            }

            if (infl > inflF
                    && curCase >= 15
                    && curCase <= 463
                    && testCotes(destCase, curCase)
                    && fight.getMap().getCase(curCase).isWalkable(false, true, -1)
                    && fight.getMap().getCase(curCase).getFighters().isEmpty()
                    && !caseUse.contains(curCase))
            {
                inflF = infl;
                destCase = curCase;
            }
            /** En -15 **/

            /** En -14 **/
            curCase = caseDepart - 14;
            infl = 0;
            for (int a = 0; a < 10 && dist[a] != 0; a++)
            {
                dist2[a] = PathFinding.getDistanceBetween(fight.getMap(), curCase, cell[a]);
                if (dist2[a] > dist[a])
                    infl++;
            }

            if (infl > inflF
                    && curCase >= 15
                    && curCase <= 463
                    && testCotes(destCase, curCase)
                    && fight.getMap().getCase(curCase).isWalkable(false, true, -1)
                    && fight.getMap().getCase(curCase).getFighters().isEmpty()
                    && !caseUse.contains(curCase))
            {
                inflF = infl;
                destCase = curCase;
            }
            /** En -14 **/
            caseUse.add(destCase);
        }
        if (destCase < 15
                || destCase > 463
                || destCase == F.getCell().getId()
                || !fight.getMap().getCase(destCase).isWalkable(false, true, -1))
            return 0;

        if (F.getPm() <= 0)
            return 0;
        ArrayList<GameCase> path = new AstarPathfinding(fight.getMap(), fight, F.getCell().getId(), destCase).getShortestPath(-1);
        if (path == null)
            return 0;
        ArrayList<GameCase> finalPath = new ArrayList<GameCase>();
        for (int a = 0; a < F.getCurPm(fight); a++)
        {
            if (path.size() == a)
                break;
            finalPath.add(path.get(a));
        }
        String pathstr = "";
        try
        {
            int curCaseID = F.getCell().getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = PathFinding.getDirBetweenTwoCase(curCaseID, c.getId(), fight.getMap(), true);
                if (d == 0)
                    return 0;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
                    pathstr += d;
                }
                curCaseID = c.getId();

                nbrcase = nbrcase + 1;
            }
            if (curCaseID != F.getCell().getId())
                pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        GameAction GA = new GameAction(0, 1, "");
        GA.args = pathstr;

        if(!fight.onFighterDeplace(F, GA))
            return 0;

        return nbrcase * 500;
    }

    public boolean testCotes(int cellWeAre, int cellWego)//Nous permet d'interdire le d�placement du bord vers des cellules hors map
    {
        if (cellWeAre == 15 || cellWeAre == 44 || cellWeAre == 73
                || cellWeAre == 102 || cellWeAre == 131 || cellWeAre == 160
                || cellWeAre == 189 || cellWeAre == 218 || cellWeAre == 247
                || cellWeAre == 276 || cellWeAre == 305 || cellWeAre == 334
                || cellWeAre == 363 || cellWeAre == 392 || cellWeAre == 421
                || cellWeAre == 450)
        {
            if (cellWego == cellWeAre + 14 || cellWego == cellWeAre - 15)
                return false;
        }
        if (cellWeAre == 28 || cellWeAre == 57 || cellWeAre == 86
                || cellWeAre == 115 || cellWeAre == 144 || cellWeAre == 173
                || cellWeAre == 202 || cellWeAre == 231 || cellWeAre == 260
                || cellWeAre == 289 || cellWeAre == 318 || cellWeAre == 347
                || cellWeAre == 376 || cellWeAre == 405 || cellWeAre == 434
                || cellWeAre == 463)
        {
            if (cellWego == cellWeAre + 15 || cellWego == cellWeAre - 14)
                return false;
        }

        if (cellWeAre >= 451 && cellWeAre <= 462)
        {
            if (cellWego == cellWeAre + 15 || cellWego == cellWeAre + 14)
                return false;
        }
        if (cellWeAre >= 16 && cellWeAre <= 27)
        {
            if (cellWego == cellWeAre - 15 || cellWego == cellWeAre - 14)
                return false;
        }
        return true;
    }

    public boolean invocIfPossible(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return false;
        if (fighter.nbInvocation() >= fighter.getTotalStats().getEffect(EffectConstant.STATS_CREATURE))
            return false;
        Fighter nearest = getNearestEnnemy(fight, fighter);
        if (nearest == null)
            return false;
        int nearestCell = fighter.getCell().getId();
        int limit = 30;
        int _loc0_ = 0;
        SpellGrade spell = null;
        while ((spell = getInvocSpell(fight, fighter, nearestCell)) == null
                && _loc0_++ < limit)
        {
            nearestCell = PathFinding.getCaseBetweenEnemy(fighter.getCell().getId(), fight.getMap(), fight);
        }
        if (nearestCell == -1)
            return false;
        if (spell == null)
            return false;
        int invoc = fight.tryCastSpell(fighter, spell, nearestCell);
        if (invoc != 0)
            return false;
        return true;
    }

    public boolean invocIfPossible(Fight fight, Fighter fighter, List<SpellGrade> Spelllist)
    {
        if (fight == null || fighter == null)
            return false;
        if (fighter.nbInvocation() >= fighter.getTotalStats().getEffect(EffectConstant.STATS_CREATURE))
            return false;
        Fighter nearest = getNearestEnnemy(fight, fighter);
        if (nearest == null)
            return false;
        int nearestCell = fighter.getCell().getId();
        int limit = 10;
        int _loc0_ = 0;
        SpellGrade spell = null;
        while ((spell = getInvocSpellDopeul(fight, fighter, nearestCell, Spelllist)) == null
                && _loc0_++ < limit)
        {
            int caseId = PathFinding.getCaseBetweenEnemy(fighter.getCell().getId(), fight.getMap(), fight, fighter);
            if(caseId != fighter.getCell().getId()) {
                nearestCell = caseId;
            }
        }

        if (nearestCell == -1)
            return false;
        if (spell == null)
            return false;
        if(nearestCell == fighter.getCell().getId())
        {
            char dir = PathFinding.getDirBetweenTwoCase(fighter.getCell().getId(), nearest.getCell().getId(), fight.getMap(), true);
            boolean finish = false;
            int po = spell.getMaxPO();
            while(!finish) {

                nearestCell = PathFinding.getCaseIdWithPo(fighter.getCell().getId(), dir, po);
                if(fight.getMap().getCase(nearestCell) != null) {
                    if (fight.getMap().getCase(nearestCell).isWalkable(false, true, -1)) {
                        finish = true;
                    } else {
                        if(po > 1) {
                            po -= 1;
                        }else{
                            finish = true;
                        }
                    }
                }
                else{
                    if(po > 1) {
                        po -= 1;
                    }else{
                        finish = true;
                    }
                }
            }
        }

        int invoc = fight.tryCastSpell(fighter, spell, nearestCell);
        if (invoc != 0)
            return false;
        return true;
    }

    public boolean invocIfPossibleloin(Fight fight, Fighter fighter, List<SpellGrade> Spelllist, Fighter Cible)
    {
        if (fight == null || fighter == null)
            return false;

        int nbinvoactu = fighter.nbInvocation();
        int nbinvoMax =  fighter.getTotalStats().getEffect(EffectConstant.STATS_CREATURE);
        if (nbinvoactu >= nbinvoMax)
            return false;

        Fighter nearest = Cible;
        if (nearest == null)
            return false;

        int nearestCell = fighter.getCell().getId();
        int limit = 30;
        int _loc0_ = 0;
        SpellGrade spell = null;

        ArrayList<GameCase> caseATester = PathFinding.getAvailableCellsTowardCible(nearestCell,fight.getMap(),nearest,fight);
        for(GameCase pos : caseATester){
            nearestCell = pos.getId();
            spell = getInvocSpellDopeul(fight, fighter, nearestCell, Spelllist);
            if(spell != null){
                break;
            }
        }
        int maxPo = 2;

        for(SpellGrade spellStats : Spelllist){
            if(spellStats.getMaxPO() > maxPo){
                // On prend que les sorts qui tape le reste on s'en fou de la PO
                if(spellStats.getTypeSwitchSpellEffects() == 2) {
                    maxPo = spellStats.getMaxPO();
                }
            }
        }

        int minPo = 1;
        for(SpellGrade spellStats : Spelllist){
            if(spellStats.getMinPO() > minPo){
                // On prend que les sorts qui tape le reste on s'en fou de la PO
                if(spellStats.getTypeSwitchSpellEffects() == 2) {
                    minPo = spellStats.getMinPO();
                }
            }
        }

        if (nearestCell == -1) {
            ArrayList<GameCase> casesEnLDV = PathFinding.getAllCellsWithLOS(fighter, fight,maxPo ,minPo);
            for(GameCase pos : casesEnLDV){
                nearestCell = pos.getId();
                spell = getInvocSpellDopeul(fight, fighter, nearestCell, Spelllist);
                if(spell != null){
                    break;
                }
            }
        }

        if(nearestCell == -1)
            return false;

        if (spell == null)
            return false;

        int invoc = fight.tryCastSpell(fighter, spell, nearestCell);
        if (invoc != 0)
            return false;
        return true;
    }

    public SpellGrade getInvocSpell(Fight fight, Fighter fighter, int nearestCell)
    {
        if (fight == null || fighter == null)
            return null;
        if (fighter.getMob() == null)
            return null;
        if (fight.getMap() == null)
            return null;
        if (fight.getMap().getCase(nearestCell) == null)
            return null;
        for (Map.Entry<Integer, SpellGrade> SS : fighter.getMob().getSpells().entrySet()) {
            if (!fight.canCastSpell1(fighter, SS.getValue(), fight.getMap().getCase(nearestCell), -1))
                continue;
            for (Effect SE : SS.getValue().getEffects())
                if (SE.getEffectID() == 181)
                    return SS.getValue();
        }
        return null;
    }

    public static SpellGrade getInvocSpellDopeul(Fight fight, Fighter fighter, int nearestCell, List<SpellGrade> Spelllist)
    {
        if (fight == null || fighter == null)
            return null;
        if (fighter.getMob() == null)
            return null;
        if (fight.getMap() == null)
            return null;
        if (nearestCell == -1)
            return null;
        if (fight.getMap().getCase(nearestCell) == null)
            return null;

        for (SpellGrade SS : Spelllist)
        {
            if (!fight.canCastSpell1(fighter, SS, fight.getMap().getCase(nearestCell), -1))
                continue;

            for (Effect SE : SS.getEffects())
            {
                if (SE.getEffectID() == 181)
                {
                    return SS;
                }
            }
        }
        return null;
    }

    public int HealIfPossible(Fight fight, Fighter f, boolean autoSoin, int PDVPERmin)//boolean pour choisir entre auto-soin ou soin alli�
    {
        if (fight == null || f == null)
            return 0;
        if (f.isDead())
            return 0;
        if (autoSoin && (f.getPdv() * 100) / f.getPdvMax() > 95)
            return 0;
        Fighter target = null;
        SpellGrade SS = null;
        if (autoSoin)
        {
            int PDVPER = (f.getPdv() * 100) / f.getPdvMax();
            if (PDVPER < PDVPERmin && PDVPER < 95)
            {
                target = f;
                SS = getHealSpell(fight, f, target);
            }
        }
        else
        //s�lection joueur ayant le moins de pv
        {
            Fighter curF = null;
            //int PDVPERmin = 100;
            SpellGrade curSS = null;
            for (Fighter F : fight.getFighters(3))
            {
                if (f.isDead())
                    continue;
                if (F == f)
                    continue;
                if (F.isDead())
                    continue;
                if (F.getTeam() == f.getTeam())
                {
                    int PDVPER = (F.getPdv() * 100) / F.getPdvMax();
                    if (PDVPER < PDVPERmin && PDVPER < 95)
                    {
                        int infl = 0;
                        if (f.isCollector())
                        {
                            for (Map.Entry<Integer, SpellGrade> ss : World.world.getGuild(f.getCollector().getGuildId()).getSpells().entrySet())
                            {
                                if (ss.getValue() == null)
                                    continue;
                                if (infl < calculInfluenceHeal(ss.getValue())
                                        && calculInfluenceHeal(ss.getValue()) != 0
                                        && fight.canCastSpell1(f, ss.getValue(), F.getCell(), -1))//Si le sort est plus interessant
                                {
                                    infl = calculInfluenceHeal(ss.getValue());
                                    curSS = ss.getValue();
                                }
                            }
                        }
                        else
                        {
                            for (Map.Entry<Integer, SpellGrade> ss : f.getMob().getSpells().entrySet())
                            {
                                if (infl < calculInfluenceHeal(ss.getValue())
                                        && calculInfluenceHeal(ss.getValue()) != 0
                                        && fight.canCastSpell1(f, ss.getValue(), F.getCell(), -1))//Si le sort est plus interessant
                                {
                                    infl = calculInfluenceHeal(ss.getValue());
                                    curSS = ss.getValue();
                                }
                            }
                        }
                        if (curSS != SS && curSS != null)
                        {
                            curF = F;
                            SS = curSS;
                            PDVPERmin = PDVPER;
                        }
                    }
                }
            }
            target = curF;
        }
        if (target == null)
            return 0;
        if (target.isFullPdv())
            return 0;
        if (SS == null)
            return 0;

        int heal = fight.tryCastSpell(f, SS, target.getCell().getId());
        if (heal != 0)
            return SS.getSpell().getDuration();

        return 0;
    }

    public int HealIfPossible(Fight fight, Fighter f)//boolean pour choisir entre auto-soin ou soin alli�
    {
        if (fight == null || f == null)
            return 0;
        if (f.isDead())
            return 0;
        Fighter target = null;
        SpellGrade SS = null;
        target = f;
        SS = World.world.getSort(587).getStatsByLevel(f.getLvl());
        if (SS == null)
            return 0;
        int heal = fight.tryCastSpell(f, SS, target.getCell().getId());
        if (heal != 0)
            return SS.getSpell().getDuration();
        return 0;
    }

    public int HealIfPossible(Fight fight, Fighter f , Fighter A, SpellGrade Spell) //boolean pour choisir entre auto-soin ou soin alli�
    {
        if (fight == null || f == null || A == null)
            return 0;
        if (f.isDead())
            return 0;
        SpellGrade SS = Spell;
        if (SS == null)
            return 0;
        int heal = fight.tryCastSpell(f, SS, A.getCell().getId());
        if (heal != 0)
            return SS.getSpell().getDuration();
        return 0;
    }

    public boolean HealIfPossible(Fight fight, Fighter f, boolean autoSoin)//boolean pour choisir entre auto-soin ou soin alli�
    {
        if (fight == null || f == null)
            return false;
        if (f.isDead())
            return false;
        if (autoSoin && (f.getPdv() * 100) / f.getPdvMax() > 95)
            return false;
        Fighter target = null;
        SpellGrade SS = null;
        if (autoSoin)
        {
            target = f;
            SS = getHealSpell(fight, f, target);
        }
        else
        //s�lection joueur ayant le moins de pv
        {
            Fighter curF = null;
            int PDVPERmin = 100;
            SpellGrade curSS = null;
            for (Fighter F : fight.getFighters(3))
            {
                if (f.isDead())
                    continue;
                if (F == f)
                    continue;
                if (F.isDead())
                    continue;
                if (F.getTeam() == f.getTeam())
                {
                    int PDVPER = (F.getPdv() * 100) / F.getPdvMax();
                    if (PDVPER < PDVPERmin && PDVPER < 95)
                    {
                        int infl = 0;
                        if (f.isCollector())
                        {
                            for (Map.Entry<Integer, SpellGrade> ss : World.world.getGuild(f.getCollector().getGuildId()).getSpells().entrySet())
                            {
                                if (ss.getValue() == null)
                                    continue;
                                if (infl < calculInfluenceHeal(ss.getValue())
                                        && calculInfluenceHeal(ss.getValue()) != 0
                                        && fight.canCastSpell1(f, ss.getValue(), F.getCell(), -1))//Si le sort est plus interessant
                                {
                                    infl = calculInfluenceHeal(ss.getValue());
                                    curSS = ss.getValue();
                                }
                            }
                        }
                        else
                        {
                            for (Map.Entry<Integer, SpellGrade> ss : f.getMob().getSpells().entrySet())
                            {
                                if (infl < calculInfluenceHeal(ss.getValue())
                                        && calculInfluenceHeal(ss.getValue()) != 0
                                        && fight.canCastSpell1(f, ss.getValue(), F.getCell(), -1))//Si le sort est plus interessant
                                {
                                    infl = calculInfluenceHeal(ss.getValue());
                                    curSS = ss.getValue();
                                }
                            }
                        }
                        if (curSS != SS && curSS != null)
                        {
                            curF = F;
                            SS = curSS;
                            PDVPERmin = PDVPER;
                        }
                    }
                }
            }
            target = curF;
        }
        if (target == null)
            return false;
        if (target.isFullPdv())
            return false;
        if (SS == null)
            return false;
        int heal = fight.tryCastSpell(f, SS, target.getCell().getId());
        if (heal != 0)
            return false;

        return true;
    }

    public boolean buffIfPossible(Fight fight, Fighter fighter, Fighter target)
    {
        if (fight == null || fighter == null)
            return false;
        if (target == null)
            return false;
        SpellGrade SS = getBuffSpell(fight, fighter, target);
        if (SS == null)
            return false;
        int buff = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (buff != 0)
            return false;
        return true;
    }

    public boolean debuffIfPossiblePerco(Fight fight, Fighter fighter, Fighter target)
    {
        if (fight == null || fighter == null)
            return false;
        if (target == null)
            return false;
        SpellGrade SS = null;
        if(target.getFightBuff().isEmpty())
        {
            return false;
        }
        Guild guild = World.world.getGuild(fighter.getCollector().getGuildId());
        Map<Integer, SpellGrade> guildSpells = guild.getSpells();
        if(guildSpells.get(460) != null)
        {
            SS = guildSpells.get(460);
        }
        if (SS == null)
            return false;
        int debuff = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (debuff != 0)
            return false;
        return true;
    }

    public boolean debuffIfPossible(Fight fight, Fighter fighter, Fighter target)
    {
        if (fight == null || fighter == null)
            return false;
        if (target == null)
            return false;
        SpellGrade SS = null;
        if(target.getFightBuff().isEmpty())
        {
            return false;
        }
        for(SpellGrade spell : fighter.getMob().getSpells().values()) {
            for(Effect spellEffect : spell.getEffects())
            {
                if(spellEffect.getEffectID() == 132)
                {
                    SS = spell;
                }
            }
        }
        if (SS == null)
            return false;
        int debuff = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (debuff != 0)
            return false;
        return true;
    }

    public SpellGrade getBuffSpell(Fight fight, Fighter F, Fighter T)
    {
        if (fight == null || F == null)
            return null;
        int infl = -1500000;
        SpellGrade ss = null;
        if (F.isCollector())
        {
            for (Map.Entry<Integer, SpellGrade> SS : World.world.getGuild(F.getCollector().getGuildId()).getSpells().entrySet())
            {
                if (SS.getValue() == null)
                    continue;
                if (infl < calculInfluence(SS.getValue(), F, T)
                        && calculInfluence(SS.getValue(), F, T) > 0
                        && fight.canCastSpell1(F, SS.getValue(), T.getCell(), -1))//Si le sort est plus interessant
                {
                    infl = calculInfluence(SS.getValue(), F, T);
                    ss = SS.getValue();
                }
            }
        }
        else
        {
            for (Map.Entry<Integer, SpellGrade> SS : F.getMob().getSpells().entrySet())
            {
                int inf = calculInfluence(SS.getValue(), F, T);
                if (infl < inf
                        && SS.getValue().getTypeSwitchSpellEffects() == 1
                        && fight.canCastSpell1(F, SS.getValue(), T.getCell(), -1))//Si le sort est plus interessant
                {
                    infl = calculInfluence(SS.getValue(), F, T);
                    ss = SS.getValue();
                }
            }
        }
        return ss;
    }

    public boolean buffIfPossible(Fight fight, Fighter fighter, Fighter target, List<SpellGrade> Spelllist)
    {
        if (fight == null || fighter == null)
            return false;
        if (target == null)
            return false;
        SpellGrade SS = getBuffSpellDopeul(fight, fighter, target, Spelllist);
        if (SS == null)
            return false;
        int buff = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (buff == 0)
            return true;

        return false;
    }

    public boolean mobilityIfPossible(Fight fight, Fighter fighter, Fighter Cible)
    {
        if (fight == null || fighter == null)
            return false;
        Fighter nearest = Cible;
        if (nearest == null)
            return false;
        int nearestCell = fighter.getCell().getId();

        SpellGrade spell = null;
        ArrayList<GameCase> caseATester = PathFinding.getAvailableCellsTowardCible(nearestCell,fight.getMap(),nearest,fight);
        for(GameCase pos : caseATester){
            //Cible.getPlayer().send("Gf901|" + pos.getId());
            nearestCell = pos.getId();
            spell = getTpSpell(fight, fighter, nearestCell);
            if(spell != null){
                break;
            }
        }

        if(nearestCell == -1)
            return false;

        if (spell == null)
            return false;
        int tp = fight.tryCastSpell(fighter, spell, nearestCell);
        if (tp != 0)
            return false;
        return true;
    }

    public static SpellGrade getTpSpell(Fight fight, Fighter fighter, int nearestCell)
    {
        if (fight == null || fighter == null)
            return null;
        if (fighter.getMob() == null)
            return null;
        if (fight.getMap() == null)
            return null;
        if (fight.getMap().getCase(nearestCell) == null)
            return null;
        for (Map.Entry<Integer, SpellGrade> SS : fighter.getMob().getSpells().entrySet()) {
            if(SS.getValue().getTypeSwitchSpellEffects() != 5)
                continue;
            if (!fight.canCastSpell1(fighter, SS.getValue(), fight.getMap().getCase(nearestCell), -1))
                continue;

            return SS.getValue();
        }
        return null;
    }

    public static SpellGrade getBuffSpellDopeul(Fight fight, Fighter F, Fighter T, List<SpellGrade> Spelllist)
    {
        if (fight == null || F == null)
            return null;
        int infl = -1500000;
        SpellGrade ss = null;
        for (SpellGrade SS : Spelllist)
        {
            int inf = calculInfluence(SS, F, T);

            if (infl < inf
                    && SS.getTypeSwitchSpellEffects() == 1
                    && fight.canCastSpell1(F, SS, T.getCell(), -1))//Si le sort est plus interessant
            {
                infl = calculInfluence(SS, F, T);
                ss = SS;
            }
        }
        return ss;
    }

    public SpellGrade getHealSpell(Fight fight, Fighter F, Fighter T)
    {
        if (fight == null || F == null)
            return null;
        int infl = 0;
        SpellGrade ss = null;
        if (F.isCollector())
        {
            for (Map.Entry<Integer, SpellGrade> SS : World.world.getGuild(F.getCollector().getGuildId()).getSpells().entrySet())
            {
                if (SS.getValue() == null)
                    continue;
                if (infl < calculInfluenceHeal(SS.getValue())
                        && calculInfluenceHeal(SS.getValue()) != 0
                        && fight.canCastSpell1(F, SS.getValue(), T.getCell(), -1))//Si le sort est plus interessant
                {
                    infl = calculInfluenceHeal(SS.getValue());
                    ss = SS.getValue();
                }
            }
        }
        else
        {
            for (Map.Entry<Integer, SpellGrade> SS : F.getMob().getSpells().entrySet())
            {
                if (SS.getValue() == null)
                    continue;
                if (infl < calculInfluenceHeal(SS.getValue())
                        && calculInfluenceHeal(SS.getValue()) != 0
                        && fight.canCastSpell1(F, SS.getValue(), T.getCell(), -1))//Si le sort est plus interessant
                {
                    infl = calculInfluenceHeal(SS.getValue());
                    ss = SS.getValue();
                }
            }
        }
        return ss;
    }

    public int moveautourIfPossible(Fight fight, Fighter F, Fighter T)
    {
        if (fight == null)
            return 0;
        if (F == null)
            return 0;
        if (T == null)
            return 0;
        if (F.getCurPm(fight) <= 0)
            return 0;
        GameMap map = fight.getMap();
        if (map == null)
            return 0;
        GameCase cell = F.getCell();
        if (cell == null)
            return 0;
        GameCase cell2 = T.getCell();
        if (cell2 == null)
            return 0;
        if (PathFinding.isNextTo(map, cell.getId(), cell2.getId()))
            return 0;
        int nbrcase = 0;

        int cellID = PathFinding.getNearestCellAroundGA(map, cell2.getId(), cell.getId(), null);
        //On demande le chemin plus court
        //Mais le chemin le plus court ne prend pas en compte les bords de map.
        if (cellID == -1)
        {
            Map<Integer, Fighter> ennemys = getLowHpEnnemyList(fight, F);
            for (Map.Entry<Integer, Fighter> target : ennemys.entrySet())
            {
                int cellID2 = PathFinding.getNearestCellAroundGA(map, target.getValue().getCell().getId(), cell.getId(), null);
                if (cellID2 != -1)
                {
                    cellID = cellID2;
                    break;
                }
            }
        }
        ArrayList<GameCase> path = new AstarPathfinding(fight.getMapOld(), fight, cell.getId(), cellID).getShortestPath(-1);
        if (path == null || path.isEmpty())
            return 0;

        ArrayList<GameCase> finalPath = new ArrayList<GameCase>();
        for (int a = 0; a < F.getCurPm(fight); a++)
        {
            if (path.size() == a)
                break;
            finalPath.add(path.get(a));
        }
        String pathstr = "";
        try
        {
            int curCaseID = cell.getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = PathFinding.getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
                if (d == 0)
                    return 0;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
                    pathstr += d;
                }
                curCaseID = c.getId();

                nbrcase = nbrcase + 1;
            }
            if (curCaseID != cell.getId())
                pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        GameAction GA = new GameAction(0, 1, "");
        GA.args = pathstr;
        if(!fight.onFighterDeplace(F, GA))
            return 0;

        return nbrcase * 200;
    }

    public int moveIfPossiblecontremur(Fight fight, Fighter F, Fighter T)
    {
        if (fight == null)
            return 0;
        if (F == null)
            return 0;
        if (T == null)
            return 0;
        if (F.getCurPm(fight) <= 0)
            return 0;
        GameMap map = fight.getMap();
        if (map == null)
            return 0;
        GameCase cell = F.getCell();
        if (cell == null)
            return 0;
        GameCase cell2 = T.getCell();
        if (cell2 == null)
            return 0;
        if (PathFinding.isNextTo(map, cell.getId(), cell2.getId()))
            return 0;
        int nbrcase = 0;

        int cellID = PathFinding.getNearenemycontremur(map, cell2.getId(), cell.getId(), null);
        //On demande le chemin plus court
        //Mais le chemin le plus court ne prend pas en compte les bords de map.
        if (cellID == -1)
        {
            Map<Integer, Fighter> ennemys = getLowHpEnnemyList(fight, F);
            for (Map.Entry<Integer, Fighter> target : ennemys.entrySet())
            {
                int cellID2 = PathFinding.getNearestCellAroundGA(map, target.getValue().getCell().getId(), cell.getId(), null);
                if (cellID2 != -1)
                {
                    cellID = cellID2;
                    break;
                }
            }
        }
        ArrayList<GameCase> path = new AstarPathfinding(fight.getMapOld(), fight, cell.getId(), cellID).getShortestPath(-1);
        if (path == null || path.isEmpty())
            return 0;

        ArrayList<GameCase> finalPath = new ArrayList<GameCase>();
        for (int a = 0; a < F.getCurPm(fight); a++)
        {
            if (path.size() == a)
                break;
            finalPath.add(path.get(a));
        }
        String pathstr = "";
        try
        {
            int curCaseID = cell.getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = PathFinding.getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
                if (d == 0)
                    return 0;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
                    pathstr += d;
                }
                curCaseID = c.getId();

                nbrcase = nbrcase + 1;
            }
            if (curCaseID != cell.getId())
                pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        GameAction GA = new GameAction(0, 1, "");
        GA.args = pathstr;
        if(!fight.onFighterDeplace(F, GA))
            return 0;

        return nbrcase * 500;
    }

    public int movetoAttackwithLOS(Fight fight, Fighter F, Fighter T, int dist){
        if (fight == null)
            return 0;
        if (F == null)
            return 0;
        if (T == null)
            return 0;
        if (F.getCurPm(fight) <= 0)
            return 0;
        GameMap map = fight.getMap();
        if (map == null)
            return 0;
        GameCase cell = F.getCell();
        if (cell == null)
            return 0;
        GameCase cell2 = T.getCell();
        if (cell2 == null)
            return 0;
        if (PathFinding.isNextTo(map, cell.getId(), cell2.getId()))
            return 0;
        // Deja en position pour taper en ligne de Vue et a la distance voulue
        if (PathFinding.checkLoSBetween2Cells(fight.getMap(), cell.getId(), cell2.getId()) && PathFinding.getDistanceBetween(fight.getMap(),cell.getId(),cell2.getId()) <= dist)
            return 0;

        int nbrcase = 0;


        ArrayList<GameCase> CellsWithLos = PathFinding.getAllCellsWithLOS(T,fight,dist);
        int mpToJoin = 30;
        int cellvoulu=-1;
        ArrayList<GameCase> path = new ArrayList<>();
        for(GameCase cellinLos : CellsWithLos){
            try {
                if (cellinLos.getFighters().isEmpty()) {

                    ArrayList<GameCase> path2 = new AstarPathfinding(fight.getMapOld(), fight, F.getCell().getId(), cellinLos.getId()).getShortestPath(-1);
                    if (path2.size() < mpToJoin) {
                        mpToJoin = path2.size();
                        cellvoulu = cellinLos.getId();
                        path = path2;
                    }
                }
            }
            catch (Exception e){

            }
        }

        int cellID = cellvoulu;

        if (cellID == -1)
        {
            Map<Integer, Fighter> ennemys = getLowHpEnnemyList(fight, F);
            for (Map.Entry<Integer, Fighter> target : ennemys.entrySet())
            {
                int cellID2 = PathFinding.getNearestCellDiagGA(map, target.getValue().getCell().getId(), cell.getId(), null);
                if (cellID2 != -1)
                {
                    cellID = cellID2;
                    break;
                }
            }
        }

        if (path == null || path.isEmpty())
            return 0;

        ArrayList<GameCase> finalPath = new ArrayList<GameCase>();
        for (int a = 0; a < F.getCurPm(fight) && a < path.size() ; a++)
        { finalPath.add(path.get(a)); }
        String pathstr = "";
        try
        {
            int curCaseID = cell.getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = PathFinding.getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
                if (d == 0)
                    return 0;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
                    pathstr += d;
                }
                curCaseID = c.getId();

                nbrcase = nbrcase + 1;
            }
            if (curCaseID != cell.getId())
                pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        GameAction GA = new GameAction(0, 1, "");
        GA.args = pathstr;

        if(!fight.onFighterDeplace(F, GA))
            return 0;

        return nbrcase * 250;
    }

    public int movediagIfPossible(Fight fight, Fighter F, Fighter T)
    {
        if (fight == null)
            return 0;
        if (F == null)
            return 0;
        if (T == null)
            return 0;
        if (F.getCurPm(fight) <= 0)
            return 0;
        GameMap map = fight.getMap();
        if (map == null)
            return 0;
        GameCase cell = F.getCell();
        if (cell == null)
            return 0;
        GameCase cell2 = T.getCell();
        if (cell2 == null)
            return 0;
        if (PathFinding.isNextTo(map, cell.getId(), cell2.getId()))
            return 0;
        int nbrcase = 0;

        int cellID = PathFinding.getNearestCellDiagGA(map, cell2.getId(), cell.getId(), null);
        //On demande le chemin plus court
        //Mais le chemin le plus court ne prend pas en compte les bords de map.
        if (cellID == -1)
        {
            Map<Integer, Fighter> ennemys = getLowHpEnnemyList(fight, F);
            for (Map.Entry<Integer, Fighter> target : ennemys.entrySet())
            {
                int cellID2 = PathFinding.getNearestCellDiagGA(map, target.getValue().getCell().getId(), cell.getId(), null);
                if (cellID2 != -1)
                {
                    cellID = cellID2;
                    break;
                }
            }
        }
        ArrayList<GameCase> path = new AstarPathfinding(fight.getMapOld(), fight, cell.getId(), cellID).getShortestPath(-1);
        if (path == null || path.isEmpty())
            return 0;

        ArrayList<GameCase> finalPath = new ArrayList<GameCase>();
        for (int a = 0; a < F.getCurPm(fight); a++)
        {
            if (path.size() == a)
                break;
            finalPath.add(path.get(a));
        }
        String pathstr = "";
        try
        {
            int curCaseID = cell.getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = PathFinding.getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
                if (d == 0)
                    return 0;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
                    pathstr += d;
                }
                curCaseID = c.getId();

                nbrcase = nbrcase + 1;
            }
            if (curCaseID != cell.getId())
                pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        GameAction GA = new GameAction(0, 1, "");
        GA.args = pathstr;
        if(!fight.onFighterDeplace(F, GA))
            return 0;

        return nbrcase * 500;
    }

    public int getMaxLaunchableAttaque(Fight fight, Fighter launcher)
    {
        ArrayList<Integer> CostBySpell = new ArrayList<Integer>();
        int FighterPa = launcher.getCurPa(fight);
        List<Integer> damageID = Arrays.asList(85, 86, 87, 88, 89, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100);
        if (fight == null)
            return 0;
        if (launcher == null)
            return 0;
        for(SpellGrade s : launcher.getMob().getSpells().values())
        {
            boolean isAttackSpell = false;
            for(Effect spellEffect : s.getEffects()) {
                if (damageID.contains(spellEffect.getEffectID())) {
                    isAttackSpell = true;
                    break;
                }
            }
            if(isAttackSpell)
            {
                CostBySpell.add(s.getPACost());
            }
        }
        int count=0;
        int newPa = FighterPa;
            for(Integer i : CostBySpell)
            {
                newPa = newPa - i;
                if(newPa >= 0)
                {
                    count++;
                }
            }


        return count;
    }


    public int moveenfaceIfPossible2(Fight fight, Fighter F, Fighter T, int dist)
    {
        if (fight == null)
            return 0;
        if (F == null)
            return 0;
        if (T == null)
            return 0;
        if (F.getCurPm(fight) <= 0)
            return 0;
        GameMap map = fight.getMap();
        if (map == null)
            return 0;
        GameCase cell = F.getCell();
        if (cell == null)
            return 0;
        GameCase cell2 = T.getCell();
        if (cell2 == null)
            return 0;
        if (PathFinding.isNextTo(map, cell.getId(), cell2.getId()))
            return 0;
        int nbrcase = 0;

        int cellID = PathFinding.getNearestligneGA(fight, cell2.getId(), cell.getId(), null, dist);
        //On demande le chemin plus court
        //Mais le chemin le plus court ne prend pas en compte les bords de map.
        if (cellID == -1)
        {
            Map<Integer, Fighter> ennemys = getLowHpEnnemyList(fight, F);
            for (Map.Entry<Integer, Fighter> target : ennemys.entrySet())
            {
                int cellID2 = PathFinding.getNearestligneGA(fight, target.getValue().getCell().getId(), cell.getId(), null, dist);
                if (cellID2 != -1)
                {
                    cellID = cellID2;
                    break;
                }
            }
        }
        ArrayList<GameCase> path = new AstarPathfinding(fight.getMapOld(), fight, cell.getId(), cellID).getShortestPath(0);//0pour en ligne
        if (path == null || path.isEmpty())
            return 0;

        ArrayList<GameCase> finalPath = new ArrayList<GameCase>();
        boolean ligneok = false;
        for (int a = 0; a < F.getCurPm(fight); a++)
        {
            if (path.size() == a)
                break;
            if(ligneok == true)
                break;
            if(PathFinding.casesAreInSameLine(fight.getMap(), path.get(a).getId(), T.getCell().getId(), 'z', 70))
                ligneok = true;
            finalPath.add(path.get(a));
        }
        String pathstr = "";
        try
        {
            int curCaseID = cell.getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = PathFinding.getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
                if (d == 0)
                    return 0;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
                    pathstr += d;
                }
                curCaseID = c.getId();

                nbrcase = nbrcase + 1;
            }
            if (curCaseID != cell.getId())
                pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (nbrcase == 1)
            return 1;
        //Création d'une GameAction
        GameAction GA = new GameAction(0, 1, "");
        GA.args = pathstr;
        if(!fight.onFighterDeplace(F, GA))
            return 0;

        return nbrcase * 500;
    }

    public int moveenfaceIfPossibleWithLOS(Fight fight, Fighter F, Fighter T, int dist)
    {
        if (fight == null)
            return 0;
        if (F == null)
            return 0;
        if (T == null)
            return 0;
        if (F.getCurPm(fight) <= 0)
            return 0;
        GameMap map = fight.getMap();
        if (map == null)
            return 0;
        GameCase cell = F.getCell();
        if (cell == null)
            return 0;
        GameCase cell2 = T.getCell();
        if (cell2 == null)
            return 0;
        if (PathFinding.isNextTo(map, cell.getId(), cell2.getId()))
            return 0;

        // Deja en position pour taper en ligne de Vue et a la distance voulue
        if (PathFinding.casesAreInSameLine(fight.getMap(), cell2.getId(), cell.getId(), 'z', 70) && PathFinding.checkLoSBetween2Cells(fight.getMap(), cell.getId(), cell2.getId()) && PathFinding.getDistanceBetweenTwoCase(fight.getMap(),cell,cell2) < dist)
            return 0;

        int nbrcase = 0;
        ArrayList<GameCase> forbiddens = new ArrayList<>();
        Map<Integer, Fighter> allies = fight.getTeam(F.getTeam());
        for(Fighter alier : allies.values())
        {
            forbiddens.add(alier.getCell());
        }

        ArrayList<GameCase> InLineCells = PathFinding.getInLineCellsWithLOS(T,fight);
        int mpToJoin = 30;
        int cellvoulu=-1;
        ArrayList<GameCase> path = new ArrayList<>();
        for(GameCase cellinline : InLineCells){
            if(PathFinding.getDistanceBetweenTwoCase(fight.getMap(),cellinline,T.getCell()) < dist && cellinline.getFighters().isEmpty() ){
                //T.getPlayer().send("Gf901|" + cellinline.getId());
                ArrayList<GameCase> path2 = new AstarPathfinding(fight.getMapOld(), fight, F.getCell().getId(), cellinline.getId()).getShortestPath(-1);
                if(path2.size() < mpToJoin) {
                    mpToJoin = path2.size();
                    cellvoulu = cellinline.getId();
                    path = path2;
                }
            }
        }

        //int cellID = PathFinding.getNearestligneGA(fight, cell2.getId(), cell.getId(), forbiddens, dist);
        int cellID = cellvoulu;

        //T.getPlayer().send("Gf901|"+cellID);
        //On demande le chemin plus court
        //Mais le chemin le plus court ne prend pas en compte les bords de map.
        if (cellID == -1)
        {
            Map<Integer, Fighter> ennemys = getLowHpEnnemyList(fight, F);
            for (Map.Entry<Integer, Fighter> target : ennemys.entrySet())
            {
                int cellID2 = PathFinding.getNearestligneGA(fight, target.getValue().getCell().getId(), cell.getId(), forbiddens, dist);
                if (cellID2 != -1)
                {
                    cellID = cellID2;
                    break;
                }
            }
        }

        if (path == null || path.isEmpty())
            return 0;

        ArrayList<GameCase> finalPath = new ArrayList<GameCase>();
        for (int a = 0; a < F.getCurPm(fight) && a < path.size() ; a++)
        { finalPath.add(path.get(a)); }
        String pathstr = "";
        try
        {
            int curCaseID = cell.getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = PathFinding.getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
                if (d == 0)
                    return 0;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
                    pathstr += d;
                }
                curCaseID = c.getId();

                nbrcase = nbrcase + 1;
            }
            if (curCaseID != cell.getId())
                pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        GameAction GA = new GameAction(0, 1, "");
        GA.args = pathstr;
        //System.out.println(pathstr);

        if(!fight.onFighterDeplace(F, GA))
            return 0;

        return nbrcase * 250;
    }

    public int moveenfaceIfPossible(Fight fight, Fighter F, Fighter T, int dist)
    {
        if (fight == null)
            return 0;
        if (F == null)
            return 0;
        if (T == null)
            return 0;
        if (F.getCurPm(fight) <= 0)
            return 0;
        GameMap map = fight.getMap();
        if (map == null)
            return 0;
        GameCase cell = F.getCell();
        if (cell == null)
            return 0;
        GameCase cell2 = T.getCell();
        if (cell2 == null)
            return 0;
        if (PathFinding.isNextTo(map, cell.getId(), cell2.getId()))
            return 0;
        int nbrcase = 0;
        ArrayList<GameCase> forbiddens = new ArrayList<>();
        Map<Integer, Fighter> allies = fight.getTeam(F.getTeam());
        for(Fighter alier : allies.values())
        {
            forbiddens.add(alier.getCell());
        }

        int cellID = PathFinding.getNearestligneGA(fight, cell2.getId(), cell.getId(), forbiddens, dist);
        //On demande le chemin plus court
        //Mais le chemin le plus court ne prend pas en compte les bords de map.
        if (cellID == -1)
        {
            Map<Integer, Fighter> ennemys = getLowHpEnnemyList(fight, F);
            for (Map.Entry<Integer, Fighter> target : ennemys.entrySet())
            {
                int cellID2 = PathFinding.getNearestligneGA(fight, target.getValue().getCell().getId(), cell.getId(), forbiddens, dist);
                if (cellID2 != -1)
                {
                    cellID = cellID2;
                    break;
                }
            }
        }
        ArrayList<GameCase> path = new AstarPathfinding(fight.getMapOld(), fight, cell.getId(), cellID).getShortestPath(0);//0pour en ligne
        if (path == null || path.isEmpty())
            return 0;

        ArrayList<GameCase> finalPath = new ArrayList<GameCase>();
        boolean ligneok = false;
        for (int a = 0; a < F.getCurPm(fight); a++)
        {
            //System.out.println(path.get(a).getId());
            boolean contains1AlliesinLine = false;
            if (path.size() == a)
                break;
            if(ligneok)
                break;
            if(PathFinding.casesAreInSameLine(fight.getMap(), path.get(a).getId(), T.getCell().getId(), 'z', 70)) {
                for (Fighter allie : allies.values()) {
                    if(allie.getTeam() != T.getTeam()) {
                        //System.out.println("Etrangement je suis dans la même teams " + path.get(a).getId());
                        if (!PathFinding.casesAreInSameLine(fight.getMap(), T.getCell().getId(), allie.getCell().getId(), 'z', 70)) {
                            //System.out.println("Bonne ligne" + path.get(a).getId());
                            ligneok = true;
                        } else {
                            //System.out.println("Allié dedant + " + path.get(a).getId() + " Allié cell " + allie.getCell().getId());
                            contains1AlliesinLine = true;
                        }
                    }
                    else{
                        ligneok = true;
                    }
                }
                if(!contains1AlliesinLine) {
                    //System.out.println("soit disant on devrait" + path.get(a).getId());
                    finalPath.add(path.get(a));
                }
                else
                {
                    //finalPath.add(path.get(a));
                    return 2;
                }
            }
        }
        String pathstr = "";
        try
        {
            int curCaseID = cell.getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = PathFinding.getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
                if (d == 0)
                    return 0;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
                    pathstr += d;
                }
                curCaseID = c.getId();

                nbrcase = nbrcase + 1;
            }
            if (curCaseID != cell.getId())
                pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        GameAction GA = new GameAction(0, 1, "");
        GA.args = pathstr;

        if(!fight.onFighterDeplace(F, GA))
            return 0;

        return nbrcase * 500;
    }

    public int movecacIfPossible(Fight fight, Fighter F, Fighter T)
    {
        if (fight == null)
            return 0;
        if (F == null)
            return 0;
        if (T == null)
            return 0;
        if (F.getCurPm(fight) <= 0)
            return 0;
        GameMap map = fight.getMap();
        if (map == null)
            return 0;
        GameCase cell = F.getCell();
        if (cell == null)
            return 0;
        GameCase cell2 = T.getCell();
        if (cell2 == null)
            return 0;
        if (PathFinding.isNextTo(map, cell.getId(), cell2.getId()))
            return 0;
        int nbrcase = 0;

        int cellID = PathFinding.getNearestCellAround(map, cell2.getId(), cell.getId(), null);
        //On demande le chemin plus court
        //Mais le chemin le plus court ne prend pas en compte les bords de map.
        if (cellID == -1)
        {
            Map<Integer, Fighter> ennemys = getLowHpEnnemyList(fight, F);
            for (Map.Entry<Integer, Fighter> target : ennemys.entrySet())
            {
                int cellID2 = PathFinding.getNearestCellAround(map, target.getValue().getCell().getId(), cell.getId(), null);
                if (cellID2 != -1)
                {
                    cellID = cellID2;
                    break;
                }
            }
        }
        ArrayList<GameCase> path = new AstarPathfinding(fight.getMapOld(), fight, cell.getId(), cell2.getId()).getShortestPath(-1);
        if (path == null || path.isEmpty())
            return 0;

        ArrayList<GameCase> finalPath = new ArrayList<GameCase>();
        for (int a = 0; a < F.getCurPm(fight); a++)
        {
            if (path.size() == a)
                break;
            finalPath.add(path.get(a));
        }
        String pathstr = "";
        try
        {
            int curCaseID = cell.getId();
            int curDir = 0;
            for (GameCase c : finalPath)
            {
                char d = PathFinding.getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
                if (d == 0)
                    return 0;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (finalPath.indexOf(c) != 0)
                        pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
                    pathstr += d;
                }
                curCaseID = c.getId();

                nbrcase = nbrcase + 1;
            }
            if (curCaseID != cell.getId())
                pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        GameAction GA = new GameAction(0, 1, "");
        GA.args = pathstr;
        if(!fight.onFighterDeplace(F, GA))
            return 0;

        return nbrcase * 500;
    }

    public Fighter getNearestFriendInvoc(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Fighter curF = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.getTeam2() == fighter.getTeam2() && f.isInvocation())//Si c'est un ami et si c'est une invocation
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < dist)
                {
                    dist = d;
                    curF = f;
                }
            }
        }
        return curF;
    }

    public Fighter getNearestFighterInvo(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Fighter curF = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.getTeam2() == fighter.getTeam2() && f.isInvocation() && f.getInvocator() ==fighter)//Si c'est un ami et si c'est une invocation
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < dist)
                {
                    dist = d;
                    curF = f;
                }
            }
        }
        return curF;
    }


    public Fighter getNearestInvoc(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Fighter curF = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.isInvocation())//Si c'est un ami et si c'est une invocation
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < dist)
                {
                    dist = d;
                    curF = f;
                }
            }
        }
        return curF;
    }

    public Fighter getNearestFriendNoInvok(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Fighter curF = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.getTeam2() == fighter.getTeam2() && !f.isInvocation())//Si c'est un ami et si c'est une invocation
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < dist)
                {
                    dist = d;
                    curF = f;
                }
            }
        }
        return curF;
    }

    public Fighter getNearestFriend(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Fighter curF = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.getTeam() == fighter.getTeam())//Si c'est un ami
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < dist)
                {
                    dist = d;
                    curF = f;
                }
            }
        }
        return curF;
    }

    public ArrayList<Fighter> getAllFriend(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        ArrayList<Fighter> curF = new ArrayList<>();
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.getTeam() == fighter.getTeam())//Si c'est un ami
            {
                curF.add(f);
            }
        }
        return curF;
    }


    public Fighter getAllyClosestToEnnemy(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Fighter curF = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f.getTeam() == fighter.getTeam())//Si c'est un ami
            {
                Fighter ennemy = Function.getInstance().getNearestEnnemy(fight, f);
                int d = PathFinding.getDistanceBetween(fight.getMap(), ennemy.getCell().getId(), f.getCell().getId());
                if (d < dist)
                {
                    dist = d;
                    curF = f;
                }
            }
        }
        return curF;
    }


    public Fighter getNearestEnnemy(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Fighter curF = null;
        Fighter invoStic = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;

            if(f.getMob() != null && f.getMob().getTemplate() != null) {
                if(ArrayUtils.contains(Constant.STATIC_INVOCATIONS,f.getMob().getTemplate().getId())){
                    invoStic = f;
                    continue;
                }
            }

            if (f.getTeam2() != fighter.getTeam2())//Si c'est un ennemis
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < dist)
                {
                    dist = d;
                    curF = f;
                }
            }
        }
        if(curF == null && invoStic != null){
            curF = invoStic;
        }
        return curF;
    }

    public Fighter getNearestEnnemyWithLOS(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Fighter curF = null;
        Fighter invoStic = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;

            if(f.getMob() != null && f.getMob().getTemplate() != null) {
                if(ArrayUtils.contains(Constant.STATIC_INVOCATIONS,f.getMob().getTemplate().getId())){
                    invoStic = f;
                    continue;
                }
            }

            if(!PathFinding.checkLoSBetween2Cells(fight.getMap(), fighter.getCell().getId(), f.getCell().getId()))
                continue;

            if (f.getTeam2() != fighter.getTeam2())//Si c'est un ennemis
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < dist)
                {
                    dist = d;
                    curF = f;
                }
            }
        }
        if(curF == null && invoStic != null){
            curF = invoStic;
        }
        return curF;
    }


    public Fighter getNearestEnnemyWithExclusion(Fight fight, Fighter fighter, ArrayList<Fighter> Exclude)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Fighter curF = null;
        Fighter invoStic = null;

        for (Fighter f : fight.getFighters(3))
        {
            if(Exclude.contains(f))
                continue;

            if (f.isDead())
                continue;

            if(f.getMob() != null && f.getMob().getTemplate() != null) {
                if(ArrayUtils.contains(Constant.STATIC_INVOCATIONS,f.getMob().getTemplate().getId())){

                    invoStic = f;
                    continue;
                }
            }

            if (f.getTeam2() != fighter.getTeam2())//Si c'est un ennemis
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < dist)
                {
                    dist = d;
                    curF = f;
                }
            }
        }
        if(curF == null && invoStic != null){
            curF = invoStic;
        }
        return curF;
    }


    public Fighter getNearestAlly(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Fighter curF = null;
        Fighter invoStic = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;

            if(f.getMob() != null && f.getMob().getTemplate() != null) {
                if(ArrayUtils.contains(Constant.STATIC_INVOCATIONS,f.getMob().getTemplate().getId())){

                    invoStic = f;
                    continue;
                }
            }

            if (f.getTeam2() == fighter.getTeam2())//Si c'est un ennemis
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < dist)
                {
                    dist = d;
                    curF = f;
                }
            }
        }
        if(curF == null && invoStic != null){
            curF = invoStic;
        }
        return curF;
    }


    public static int getNearestFreeCell(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return 0;
         if(fighter.isDead())
             return 0;
        int Freecell = fight.getMap().getRandomNearFreeCellId(fighter.getCell().getId());
        return Freecell;
    }

    public static Fighter getNearestEnnemy2(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return null;
        int dist = 1000;
        Fighter curF = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f.getTeam2() != fighter.getTeam2())//Si c'est un ennemis
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < dist)
                {
                    dist = d;
                    curF = f;
                }
            }
        }
        return curF;
    }

    public Fighter getNearestEnnemynbrcasemax(Fight fight, Fighter fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Fighter curF = null;
        //Fighter invoStic = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;

            if(f.getMob() != null && f.getMob().getTemplate() != null) {
                if(ArrayUtils.contains(Constant.STATIC_INVOCATIONS,f.getMob().getTemplate().getId())){
                    continue;
                }
            }

            if (f.getTeam2() != fighter.getTeam2())//Si c'est un ennemis
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < distmax)
                {
                    if (d > distmin)
                    {
                        distmax = d;
                        curF = f;
                    }
                }
            }
        }

        return curF;
    }

    public static Fighter getNearestEnnemynbrcasemaxNoHide(Fight fight, Fighter fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Fighter curF = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if(f.getMob() != null && f.getMob().getTemplate() != null) {
                boolean ok = false;
                for (int i : Constant.STATIC_INVOCATIONS)
                    if (i == f.getMob().getTemplate().getId())
                        ok = true;
                if(ok) continue;
            }

            if (f.getTeam2() != fighter.getTeam2())//Si c'est un ennemis
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < distmax)
                {
                    if (d > distmin && !f.isHide())
                    {
                        distmax = d;
                        curF = f;
                    }
                }
            }
        }
        return curF;
    }

    public static Map<Integer, Fighter> getXEnnemiesinRange(Fight fight, Fighter fighter, int distmin, int distmax, int x){
        if (fight == null || fighter == null)
            return null;
        Map<Integer, Fighter> list = new HashMap<Integer, Fighter>();
        Map<Integer, Fighter> ennemy = new HashMap<Integer, Fighter>();

        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.getTeam2() != fighter.getTeam2())
            {
                ennemy.put(f.getId(), f);
            }
        }

        int i= 0;
        for ( Fighter e : ennemy.values())
        {
            int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), e.getCell().getId());
            if (d <= distmax) {
                if (d >= distmin) {
                    list.put(i, e);
                }
            }
            i++;
        }

        // On commence a retiré des invoc

        int initnb= list.size();

        for(int j =0; j<initnb ; j++){
            try {
                Fighter e = list.get(j);
                if(e == null)
                    continue;

                if (e.isInvocation()) {
                    list.remove(j);
                    if(list.size() <= x)
                        break;
                }
            }
            catch (Exception e){
                System.out.println(e);
            }
        }



        int initnb2 = list.size();

        for(int j =0; j < initnb2 ; j++){
            try {
                Fighter e = getStrongestinList(list);
                int key = getKey(list,e);
                list.remove(key);
            }
            catch (Exception e){
                System.out.println(e);
                e.printStackTrace();
                list.remove(j);
            }
            if(list.size() <= x)
                break;
        }

        return list;
    }

    public static <K, V> K getKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Fighter getStrongestinList(Map<Integer, Fighter> list){
        Fighter test = null;
        int higherhp = 0;
        for ( Fighter e : list.values())
        {
            int hp = e.getPdv();
            if(hp > higherhp){
                higherhp = hp;
                test = e;
            }
        }
        return test;
    }
    // CA SERAI INTERRESSANT A DEV POUR AVOIR UNE VRAI GRILLE
    // public Fighter getImportanceEnnemy(Fight fight, Fighter fighter){
    // }


    public Fighter getNearEnnemylignenbrcasemax(Fight fight, Fighter fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Fighter curF = null;

        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if(f.getTeam2() != fighter.getTeam2())
                curF = PathFinding.getNearestligneenemy(fight.getMap(), fighter.getCell().getId(), f, distmax);


        }
        return curF;
    }

    public Fighter getNearestEnnemymurnbrcasemax(Fight fight, Fighter fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Fighter curF = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f.getTeam2() != fighter.getTeam2())//Si c'est un ennemis
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < distmax)
                {
                    if (d > distmin)
                    {
                        if(PathFinding.getNearenemycontremur2(fight.getMap(), f.getCell().getId(), fighter.getCell().getId(), null, fighter) == -1)
                            continue;
                        distmax = d;
                        curF = f;
                    }
                }
            }
        }
        if(curF == null)
        {
            for (Fighter f : fight.getFighters(3))
            {
                if (f.isDead())
                    continue;
                if (f.getTeam2() != fighter.getTeam2())//Si c'est un ennemis
                {
                    int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                    if (d < distmax)
                    {
                        if (d > distmin)
                        {
                            distmax = d;
                            curF = f;
                        }
                    }
                }
            }
        }
        return curF;
    }

    public Fighter getNearestAllnbrcasemax(Fight fight, Fighter fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Fighter curF = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;

            int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
            if (d < distmax)
            {
                if (d > distmin)
                {
                    distmax = d;
                    curF = f;
                }
            }
        }
        return curF;
    }

    public Fighter getNearestAminbrcasemax(Fight fight, Fighter fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Fighter curF = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f.getTeam2() == fighter.getTeam2())//Si c'est un ennemis
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < distmax)
                {
                    if (d > distmin)
                    {
                        distmax = d;
                        curF = f;
                    }
                }
            }
        }
        return curF;
    }

    public Fighter getNearestAminoinvocnbrcasemax(Fight fight, Fighter fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Fighter curF = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f.getTeam2() == fighter.getTeam2() && f.isInvocation() == false)//Si c'est un ennemis
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < distmax)
                {
                    if (d > distmin)
                    {
                        distmax = d;
                        curF = f;
                    }
                }
            }
        }
        return curF;
    }

    public Fighter getNearestinvocateurnbrcasemax(Fight fight, Fighter fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Fighter curF = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f.getTeam2() == fighter.getTeam2() && !f.isInvocation() && f == fighter.getInvocator())//Si c'est un ennemis
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < distmax)
                {
                    if (d > distmin)
                    {
                        distmax = d;
                        curF = f;
                    }
                }
            }
        }
        if(curF == null)
            for (Fighter f : fight.getFighters(3))
            {
                if (f.isDead())
                    continue;
                if (f.getTeam2() == fighter.getTeam2() && !f.isInvocation())//Si c'est un ennemis
                {
                    int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                    if (d < distmax)
                    {
                        if (d > distmin)
                        {
                            distmax = d;
                            curF = f;
                        }
                    }
                }
            }
        return curF;
    }

    public Fighter getNearestInvocnbrcasemax(Fight fight, Fighter fighter, int distmin, int distmax)
    {
        if (fight == null || fighter == null)
            return null;
        Fighter curF = null;
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f.getTeam2() == fighter.getTeam2() && f.isInvocation())//Si c'est un ennemis
            {
                int d = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), f.getCell().getId());
                if (d < distmax)
                {
                    if (d > distmin)
                    {
                        distmax = d;
                        curF = f;
                    }
                }
            }
        }
        return curF;
    }

    public static Map<Integer, Fighter> getLowHpEnnemyList(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return null;
        Map<Integer, Fighter> list = new HashMap<Integer, Fighter>();
        Map<Integer, Fighter> ennemy = new HashMap<Integer, Fighter>();
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.getTeam2() != fighter.getTeam2())
            {
                ennemy.put(f.getId(), f);
            }
        }
        int i = 0, i2 = ennemy.size();
        int curHP = 10000;
        Fighter curEnnemy = null;

        while (i < i2)
        {
            curHP = 200000;
            curEnnemy = null;
            for (Map.Entry<Integer, Fighter> t : ennemy.entrySet())
            {
                if (t.getValue().getPdv() < curHP)
                {
                    curHP = t.getValue().getPdv();
                    curEnnemy = t.getValue();
                }
            }
            list.put(curEnnemy.getId(), curEnnemy);
            ennemy.remove(curEnnemy.getId());
            i++;
        }
        return list;
    }

    public static Map<Integer, Fighter> getLowHpAllyList(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return null;
        Map<Integer, Fighter> list = new HashMap<Integer, Fighter>();
        Map<Integer, Fighter> ennemy = new HashMap<Integer, Fighter>();
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.getTeam2() == fighter.getTeam2())
            {
                ennemy.put(f.getId(), f);
            }
        }
        int i = 0, i2 = ennemy.size();
        int curHP = 20000;
        Fighter curEnnemy = null;

        while (i < i2)
        {
            for (Map.Entry<Integer, Fighter> t : ennemy.entrySet())
            {
                if (t.getValue().getPdv() < curHP)
                {
                    curHP = t.getValue().getPdv();
                    curEnnemy = t.getValue();
                }
            }
            list.put(curEnnemy.getId(), curEnnemy);
            ennemy.remove(curEnnemy.getId());
            i++;
        }
        return list;
    }


    public int attackIfPossible(Fight fight, Fighter fighter, List<SpellGrade> Spell)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return -1;

        Map<Integer, Fighter> ennemyList = getLowHpEnnemyList(fight, fighter);
        SpellGrade SS = null;
        Fighter target = null;
        for (Map.Entry<Integer, Fighter> t : ennemyList.entrySet())
        {
            SS = getBestSpellForTargetDopeul(fight, fighter, t.getValue(), fighter.getCell().getId(), Spell);
            if (SS != null)
            {
                target = t.getValue();
                break;
            }
        }
        int curTarget = 0, cell = 0, influance = 0;
        SpellGrade SS2 = null;
        for (SpellGrade S : Spell)
        {
            int targetVal = getBestTargetZone(fight, fighter, S, fighter.getCell().getId(), false);
            if (targetVal == -1 || targetVal == 0)
                continue;
            int nbTarget = targetVal / 1000;
            int cellID = targetVal - nbTarget * 1000;

            if ((nbTarget > curTarget))
            {
                influance = Function.getInfl(fight,S,fighter);
                curTarget = nbTarget;
                cell = cellID;
                SS2 = S;
            }
            else {
                int nextInfluance = Function.getInfl(fight,S,fighter);
                if(nextInfluance > influance){
                   influance = Function.getInfl(fight,S,fighter);
                   cell = cellID;
                   SS2 = S;
               }

            }
        }

        if (curTarget > 0 && cell >= 15 && cell <= 463 && SS2 != null)
        {
            //System.out.println("CellID " + cell);
            int attack = fight.tryCastSpell(fighter, SS2, cell);
            if (attack == 0)
                return SS2.getSpell().getDuration();
        }
        else
        {
            if (SS == null)
                return -1;
            if(target != null) {
                int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
                if (attack == 0) {
                    return SS.getSpell().getDuration();
                }
            }
            else
            {
                if(fighter.getMob().getTemplate().getId() == 1090 || fighter.getMob().getTemplate().getId() == 1091 || fighter.getMob().getTemplate().getId() == 1092)
                {
                    int cellid = getNearestFreeCell(fight, fighter);
                    if(fighter.isMob() & fighter.getMob().getTemplate().getId() == 1090 & fighter.haveState(EffectConstant.ETAT_ENCRE_PRIMAIRE))
                    {
                        int attack2 = fight.tryCastSpell(fighter, SS, cellid);
                        if (attack2 == 0)
                            return SS.getSpell().getDuration();
                    }
                    else if(fighter.isMob() & fighter.getMob().getTemplate().getId() == 1091 & fighter.haveState(EffectConstant.ETAT_ENCRE_SECONDAIRE))
                    {
                        int attack2 = fight.tryCastSpell(fighter, SS, cellid);
                        if (attack2 == 0)
                            return SS.getSpell().getDuration();
                    }
                    else if(fighter.isMob() & fighter.getMob().getTemplate().getId() == 1092 & fighter.haveState(EffectConstant.ETAT_ENCRE_TERTIDIAIRE))
                    {
                        int attack2 = fight.tryCastSpell(fighter, SS, cellid);
                        if (attack2 == 0)
                            return SS.getSpell().getDuration();
                    }
                }
                else{
                    return 0;
                }
            }
        }
        return 0;
    }

    public int attackTargetIfPossible(Fight fight, Fighter fighter, Fighter target2,  SpellGrade Spell)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return -1;

        SpellGrade SS = Spell;
        Fighter target = target2;

        if (SS == null)
            return -1;
        if(target != null) {
            int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
            if (attack != 0 && attack != 10) {
                return SS.getSpell().getDuration();
            }
        }

        return 0;
    }

    public int attackallpossibletargetTillEnd(Fight fight, Fighter fighter,  Map<Integer, Fighter> EnnemiesInRangeNoMove  ,SpellGrade spellstat ) {
        int time = 100;
        // La on lance a tout bout de champs les sorts restants sur ceux a notre position
        for(  Fighter target :  EnnemiesInRangeNoMove.values() ){
            for (int i = 0; i <= spellstat.getMaxLaunchByTarget(); i++) {
                if (fighter.getCurPa(fight) > 0 ) {
                    int value = Function.getInstance().attackTargetIfPossible(fight, fighter, target, spellstat);
                    if (value != 0) {
                        time += 200;
                    }
                }
            }
        }
        return time;
    }


    public int attackIfPossibleglyph(Fight fight, Fighter fighter, Fighter f, List<SpellGrade> Spell)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null || f == null)
            return 0;
        int curTarget = 0, cell = 0;
        SpellGrade SS2 = null;
        for (SpellGrade S : Spell)
        {
            if(PathFinding.casesAreInSameLine(fight.getMap(), fighter.getCell().getId(), f.getCell().getId(), 'z', 70))
            {

                cell = PathFinding.newCaseAfterPush(fight, fighter.getCell(), f.getCell(), -1);
                if(fight.canCastSpell1(fighter, S, fight.getMap().getCase(cell), -1))
                {
                    SS2 = S;
                    curTarget = 100;
                }
            }

            if(S.getSpellID() == 2037)
            {
                cell = PathFinding.getCaseBetweenEnemy(f.getCell().getId(), fight.getMap(), fight);
                SS2 = S;
                if(fight.canCastSpell1(fighter, SS2, fight.getMap().getCase(cell), -1))
                    curTarget = 100;
            }
        }
        if (curTarget > 0 && cell >= 15 && cell <= 463 && SS2 != null)
        {
            int attack = fight.tryCastSpell(fighter, SS2, cell);
            if (attack != 0)
                return SS2.getSpell().getDuration();
        }
        return 0;
    }

    public int attackIfPossibleCMAttirance(Fight fight, Fighter fighter, int curPa)
    {
        if(curPa == 0)
        {
            return -1;
        }
        GameCase fighterCell = fighter.getCell();
        ArrayList<Fighter> fighters = fight.getFighters(fighter.getOtherTeam());
        ArrayList<Fighter> fighters_sameLine = new ArrayList<>();
        SpellGrade AttiranceSylvestre = null;
        for(SpellGrade spell : fighter.getMob().getSpells().values())
        {
            if(spell.getSpellID() == 977) {
                AttiranceSylvestre = fighter.getMob().getSpells().get(977);
            }
        }
        if(!fighters.isEmpty()) {
            for (Fighter fighter1 : fighters) {
                if(PathFinding.casesAreInSameLine(fight.getMap(), fighterCell.getId(), fighter1.getCell().getId(), 'z', 12))
                {
                    fighters_sameLine.add(fighter1);
                }
            }
            if(!fighters_sameLine.isEmpty())
            {
                if(fight.canCastSpell1(fighter, AttiranceSylvestre, fighterCell, -1))
                {
                    fight.tryCastSpell(fighter, AttiranceSylvestre, fighterCell.getId());
                    return 0;
                }
            }
            else{
                return -1;
            }
        }
        else{
            return -1;
        }
        return -1;
    }

    public boolean attackCacIfPossibleCM(Fight fight, Fighter fighter, int CurPa)
    {
        boolean result = false;
        if(CurPa < 1)
        {
            result = false;
        }
        int pa = CurPa;
        SpellGrade TornaBranches = null;
        SpellGrade Enracinement = null;
        for(SpellGrade spell : fighter.getMob().getSpells().values())
        {
            if(spell.getSpellID() == 484)
            {
                TornaBranches = spell;
            }
            else if(spell.getSpellID() == 1021)
            {
                Enracinement = spell;
            }
        }
        if(TornaBranches == null | Enracinement == null)
        {
            result = false;
        }
        ArrayList<Fighter> ennemies = new ArrayList<>();
        for(Fighter ennemie : fight.getFighters(fighter.getOtherTeam()))
        {
            if(PathFinding.isCacTo(fight.getMap(), fighter.getCell().getId(), ennemie.getCell().getId()))
            {
                ennemies.add(ennemie);
            }
        }
        if(!ennemies.isEmpty())
        {
            if(fight.canCastSpell1(fighter, Enracinement, fighter.getCell(), -1))
            {
                fight.tryCastSpell(fighter, Enracinement, fighter.getCell().getId());
                pa -= Enracinement.getPACost();
                result = true;
            }
            if(pa >= TornaBranches.getPACost())
            {
                if(fight.canCastSpell1(fighter, TornaBranches, fighter.getCell(), -1))
                {
                    fight.tryCastSpell(fighter, TornaBranches, fighter.getCell().getId());
                    result = true;
                }
            }
        }
        return result;
    }

    public int attackIfPossibleCM1(Fight fight, Fighter fighter,List<SpellGrade> Spell)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        int curTarget = 0, cell = 0;
        SpellGrade SS2 = null;
        Map<Integer, Fighter> ennemyList = getLowHpEnnemyList(fight, fighter);
        for (SpellGrade S : Spell)
        {
            if(S.getSpellID() == 483)
                continue;

            if(S.getSpellID() == 977)
                for (Map.Entry<Integer, Fighter> f : ennemyList.entrySet())
                    if(f.getValue() != null)
                        if(!f.getValue().isInvocation())
                            if(PathFinding.casesAreInSameLine(fight.getMap(), fighter.getCell().getId(), f.getValue().getCell().getId(), 'z', 12))
                            {

                                cell = fighter.getCell().getId();
                                if(fight.canCastSpell1(fighter, S, fight.getMap().getCase(cell), -1))
                                {
                                    SS2 = S;
                                    curTarget = 100;
                                }
                            }
            if(S.getSpellID() == 484)
                for (Map.Entry<Integer, Fighter> f : ennemyList.entrySet())
                    if(f.getValue() != null)
                        if(!f.getValue().isInvocation())
                            if(PathFinding.casesAreInSameLine(fight.getMap(), fighter.getCell().getId(), f.getValue().getCell().getId(), 'z', 4))
                            {

                                cell = fighter.getCell().getId();
                                if(fight.canCastSpell1(fighter, S, fight.getMap().getCase(cell), -1))
                                {
                                    SS2 = S;
                                    curTarget = 100;
                                }
                            }
        }
        if (curTarget > 0 && cell >= 15 && cell <= 463 && SS2 != null)
        {
            int attack = fight.tryCastSpell(fighter, SS2, cell);
            if (attack != 0)
                return SS2.getSpell().getDuration();
        }
        return 0;
    }

    public int attackIfPossiblevisee(Fight fight, Fighter fighter, Fighter target, List<SpellGrade> Spell)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        SpellGrade SS = null;

        SS = getBestSpellForTargetDopeul(fight, fighter, target, fighter.getCell().getId(), Spell);
        if (target == null || SS == null)
            return 0;
        int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
        if (attack != 0)
            return SS.getSpell().getDuration();
        return 0;
    }

    public int attackAllIfPossible(Fight fight, Fighter fighter, List<SpellGrade> Spell)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
    {
        if (fight == null || fighter == null)
            return 0;
        Fighter ennemy = getNearestAllnbrcasemax(fight, fighter, 0, 2);
        SpellGrade SS = null;
        Fighter target = null;
        SS = getBestSpellForTargetDopeul(fight, fighter, ennemy, fighter.getCell().getId(), Spell);

        if (SS != null)
        {
            target = ennemy;
        }
        int curTarget = 0, cell = 0;
        SpellGrade SS2 = null;

        for (SpellGrade S : Spell)
        {
            int targetVal = getBestTargetZone(fight, fighter, S, fighter.getCell().getId(), false);
            if (targetVal == -1 || targetVal == 0)
                continue;
            int nbTarget = targetVal / 1000;
            int cellID = targetVal - nbTarget * 1000;
            if (nbTarget > curTarget)
            {
                curTarget = nbTarget;
                cell = cellID;
                SS2 = S;
            }
        }
        if (curTarget > 0 && cell >= 15 && cell <= 463 && SS2 != null)
        {
            int attack = fight.tryCastSpell(fighter, SS2, cell);
            if (attack != 0)
                return SS2.getSpell().getDuration();
        }
        else
        {
            if (target == null || SS == null)
                return 0;
            int attack = fight.tryCastSpell(fighter, SS, target.getCell().getId());
            if (attack != 0)
                return SS.getSpell().getDuration();
        }
        return 0;
    }

    public int moveToAttackIfPossible(Fight fight, Fighter fighter)
    {
        if (fight == null || fighter == null)
            return -1;
        GameMap m = fight.getMap();
        if (m == null)
            return -1;

        GameCase _c = fighter.getCell();
        if (_c == null)
            return -1;

        Fighter ennemy = getNearestEnnemy(fight, fighter);
        if (ennemy == null)
            return -1;

        int distMin = PathFinding.getDistanceBetween(m, _c.getId(), ennemy.getCell().getId());
        ArrayList<SpellGrade> sorts = getLaunchableSort(fighter, fight, distMin);
        if (sorts == null)
            return -1;
        ArrayList<Integer> cells = PathFinding.getListCaseFromFighter(fight, fighter, fighter.getCell().getId(), sorts);
        if (cells == null)
            return -1;
        ArrayList<Fighter> targets = getPotentialTarget(fight, fighter, sorts);
        if (targets == null)
            return -1;
        int CellDest = 0;
        SpellGrade bestSS = null;
        int[] bestInvok = {1000, 0, 0, 0, -1};
        int[] bestFighter = {1000, 0, 0, 0, -1};
        int targetCell = -1;
        for (int i : cells)
        {
            for (SpellGrade S : sorts)
            {
                int targetVal = getBestTargetZone(fight, fighter, S, i, false);
                if (targetVal > 0)
                {
                    int nbTarget = targetVal / 1000;
                    int cellID = targetVal - nbTarget * 1000;
                    if (fight.getMapOld().getCase(cellID) != null
                            && nbTarget > 0)
                    {
                        if (fight.canCastSpell1(fighter, S, fight.getMapOld().getCase(cellID), i))
                        {
                            int dist = PathFinding.getDistanceBetween(fight.getMapOld(), fighter.getCell().getId(), i);
                            if (dist < bestFighter[0]
                                    || bestFighter[2] < nbTarget)
                            {

                                bestFighter[0] = dist;
                                bestFighter[1] = i;
                                bestFighter[2] = nbTarget;
                                bestFighter[4] = cellID;
                                bestSS = S;
                            }
                        }
                    }
                }
                else
                {
                    for (Fighter T : targets)
                    {
                        if (fight.canCastSpell1(fighter, S, T.getCell(), i))
                        {
                            int dist = PathFinding.getDistanceBetween(fight.getMapOld(), fighter.getCell().getId(), T.getCell().getId());
                            if (!PathFinding.isCACwithEnnemy(fighter, targets))
                            {
                                if (T.isInvocation())
                                {
                                    if (dist < bestInvok[0])
                                    {
                                        bestInvok[0] = dist;
                                        bestInvok[1] = i;
                                        bestInvok[2] = 1;
                                        bestInvok[3] = 1;
                                        bestInvok[4] = T.getCell().getId();
                                        bestSS = S;
                                    }

                                }
                                else
                                {
                                    if (dist < bestFighter[0])
                                    {
                                        bestFighter[0] = dist;
                                        bestFighter[1] = i;
                                        bestFighter[2] = 1;
                                        bestFighter[3] = 0;
                                        bestFighter[4] = T.getCell().getId();
                                        bestSS = S;
                                    }

                                }
                            }
                            else
                            {
                                if (dist < bestFighter[0])
                                {
                                    bestFighter[0] = dist;
                                    bestFighter[1] = i;
                                    bestFighter[2] = 1;
                                    bestFighter[3] = 0;
                                    bestFighter[4] = T.getCell().getId();
                                    bestSS = S;
                                }
                            }
                        }
                        //}
                    }
                }
            }
        }
        if (bestFighter[1] != 0)
        {
            CellDest = bestFighter[1];
            targetCell = bestFighter[4];
        }
        else if (bestInvok[1] != 0)
        {
            CellDest = bestInvok[1];
            targetCell = bestInvok[4];
        }
        else
            return -1;
        if (CellDest == 0)
            return -1;
        if (CellDest == fighter.getCell().getId())
            return targetCell + bestSS.getSpellID() * 1000;
        ArrayList<GameCase> path = new AstarPathfinding(fight.getMapOld(), fight, fighter.getCell().getId(), CellDest).getShortestPath(-1);
        if (path == null)
            return -1;
        String pathstr = "";
        try
        {
            int curCaseID = fighter.getCell().getId();
            int curDir = 0;
            path.add(fight.getMapOld().getCase(CellDest));
            for (GameCase c : path)
            {
                if (curCaseID == c.getId())
                    continue; // Emp�che le d == 0
                char d = PathFinding.getDirBetweenTwoCase(curCaseID, c.getId(), m, true);
                if (d == 0)
                    return -1;//Ne devrait pas arriver :O
                if (curDir != d)
                {
                    if (path.indexOf(c) != 0)
                        pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
                    pathstr += d;
                }
                curCaseID = c.getId();
            }
            if (curCaseID != fighter.getCell().getId())
                pathstr += World.world.getCryptManager().cellID_To_Code(curCaseID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Cr�ation d'une GameAction
        GameAction GA = new GameAction(0, 1, "");
        GA.args = pathstr;
        fight.onFighterDeplace(fighter, GA);

        return targetCell + bestSS.getSpellID() * 1000;
    }

    public ArrayList<SpellGrade> getLaunchableSort(Fighter fighter, Fight fight, int distMin)
    {
        if (fight == null || fighter == null)
            return null;
        ArrayList<SpellGrade> sorts = new ArrayList<>();
        if (fighter.getMob() == null)
            return null;
        if(fighter.getCollector() != null)
        {
            for (Map.Entry<Integer, SpellGrade> S : World.world.getGuild(fighter.getCollector().getGuildId()).getSpells().entrySet())
            {
                if(S == null)
                    continue;
                if (S.getValue().getSpellID() == 479)
                    continue;
                if (S.getValue().getPACost() > fighter.getCurPa(fight))//si PA insuffisant
                    continue;
                //if(S.getValue().getMaxPO() + fighter.getCurPM(fight) < distMin && S.getValue().getMaxPO() != 0)// si po max trop petite
                //continue;
                if (!LaunchedSpell.cooldownGood(fighter, S.getValue().getSpellID()))// si cooldown ok
                    continue;
                if (S.getValue().getMaxLaunchbyTurn()
                        - LaunchedSpell.getNbLaunch(fighter, S.getValue().getSpellID()) <= 0
                        && S.getValue().getMaxLaunchbyTurn() > 0)// si nb tours ok
                    continue;
                if (S.getValue().getTypeSwitchSpellEffects() != 0)// si sort pas d'attaque
                    continue;
                sorts.add(S.getValue());
            }
        }
        else{
            for (Map.Entry<Integer, SpellGrade> S : fighter.getMob().getSpells().entrySet())
            {
                if (S.getValue().getSpellID() == 479)
                    continue;
                if (S.getValue().getPACost() > fighter.getCurPa(fight))//si PA insuffisant
                    continue;
                //if(S.getValue().getMaxPO() + fighter.getCurPM(fight) < distMin && S.getValue().getMaxPO() != 0)// si po max trop petite
                //continue;
                if (!LaunchedSpell.cooldownGood(fighter, S.getValue().getSpellID()))// si cooldown ok
                    continue;
                if (S.getValue().getMaxLaunchbyTurn()
                        - LaunchedSpell.getNbLaunch(fighter, S.getValue().getSpellID()) <= 0
                        && S.getValue().getMaxLaunchbyTurn() > 0)// si nb tours ok
                    continue;
                if (S.getValue().getTypeSwitchSpellEffects() != 0)// si sort pas d'attaque
                    continue;
                sorts.add(S.getValue());
            }
        }
        ArrayList<SpellGrade> finalS = TriInfluenceSorts(fighter, sorts, fight);

        return finalS;
    }

    public ArrayList<SpellGrade> TriInfluenceSorts(Fighter fighter, ArrayList<SpellGrade> sorts, Fight fight)
    {
        if (fight == null || fighter == null)
            return null;
        if (sorts == null)
            return null;


        // Custom comparator based on return values of yourFunction
        Comparator<SpellGrade> comparator = Comparator.comparingInt(a -> getInfl(fight, a,fighter));

        // Sorting the list using the custom comparator
        Collections.sort(sorts, comparator);
        return sorts;
    }

    public ArrayList<Fighter> getPotentialTarget(Fight fight, Fighter fighter, ArrayList<SpellGrade> sorts)
    {
        if (fight == null || fighter == null)
            return null;
        ArrayList<Fighter> targets = new ArrayList<Fighter>();
        int distMax = 0;
        for (SpellGrade S : sorts)
        {
            if (S.getMaxPO() > distMax)
                distMax = S.getMaxPO();
        }
        distMax += fighter.getCurPm(fight) + 3;
        Map<Integer, Fighter> potentialsT = getLowHpEnnemyList(fight, fighter);
        for (Map.Entry<Integer, Fighter> T : potentialsT.entrySet())
        {
            int dist = PathFinding.getDistanceBetween(fight.getMap(), fighter.getCell().getId(), T.getValue().getCell().getId());
            if (dist < distMax)
            {

                targets.add(T.getValue());
            }
        }
        return targets;
    }

    private ArrayList<Fighter> getAllies(Fight fight, Fighter fighter){
        ArrayList<Fighter> allies = new ArrayList<>();
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.getTeam2() == fighter.getTeam2())
            {
                allies.add(f);
            }
        }
        return allies;
    }

    private ArrayList<Fighter> getEnnemies(Fight fight, Fighter fighter){
        ArrayList<Fighter> Ennemies = new ArrayList<>();
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.getTeam2() != fighter.getTeam2())
            {
                Ennemies.add(f);
            }
        }
        return Ennemies;
    }

    public ArrayList<Fighter> getAlliesToHeal(Fight fight, Fighter fighter,int PDVPERmin){

        ArrayList<Fighter> AlliesNotFullHP = new ArrayList<>();
        for (Fighter f : getAllies(fight,fighter))
        {
            int PERCENT = (f.getPdv() * 100) / f.getPdvMax();
            if( PERCENT < PDVPERmin && PERCENT < 95 ){
               AlliesNotFullHP.add(f);
            }
        }
        return AlliesNotFullHP;
    }

    public Fighter getAllyToBuff(Fight fight, Fighter fighter){
        Fighter BestAlly = null;
        int lvlTobeat = 1;
        for (Fighter f : getAllies(fight,fighter))
        {
            if( f.getLvl() < lvlTobeat  ){
                BestAlly = f;
                lvlTobeat = f.getLvl();
            }
        }
        return BestAlly;
    }


    private Map<Integer, Fighter> getBestLvlAllyList(Fight fight, Fighter fighter) {
        if (fight == null || fighter == null)
            return null;
        Map<Integer, Fighter> list = new HashMap<Integer, Fighter>();
        Map<Integer, Fighter> ennemy = new HashMap<Integer, Fighter>();
        for (Fighter f : fight.getFighters(3))
        {
            if (f.isDead())
                continue;
            if (f == fighter)
                continue;
            if (f.getTeam2() == fighter.getTeam2())
            {
                ennemy.put(f.getId(), f);
            }
        }
        int i = 0, i2 = ennemy.size();
        int curLvl = 1;
        Fighter curEnnemy = null;

        while (i < i2)
        {
            for (Map.Entry<Integer, Fighter> t : ennemy.entrySet())
            {
                if (t.getValue().getLvl() < curLvl)
                {
                    curLvl = t.getValue().getLvl();
                    curEnnemy = t.getValue();
                }
            }
            list.put(curEnnemy.getId(), curEnnemy);
            ennemy.remove(curEnnemy.getId());
            i++;
        }
        return list;
    }


    public static int getInfl(Fight fight, SpellGrade SS, Fighter fighter)
    {
        if (fight == null)
            return 0;
        int inf = 0;
        for (Effect SE : SS.getEffects())
        {
            int elem =-1,statToCheck =-1, factor=0;
            switch (SE.getEffectID())
            {
                case 5 :
                    inf += 1000;
                    break;
                case 77://Vol de PM
                case 127://Retrait PM
                case 169://Perte PM non esquivable
                    inf += 3000 * Formulas.getMiddleJet(SE); // RetPM
                    break;
                case 84://Vol de PA
                case 101://Retrait PA
                case 168://Perte PA non esquivable
                    inf += 4000 * Formulas.getMiddleJet(SE); // RetPA
                    break;
                case 116://Malus PO
                case 320://Vol de PO
                    inf += 2500 * Formulas.getMiddleJet(SE); // RetPO
                    break;
                case 85:
                case 86:
                case 87:
                case 88:
                case 89:
                    factor = fighter.getPdv();
                    inf += (factor+100) * Formulas.getMiddleJet(SE); // Dommage en % de vie
                    break;
                case 82://Vol de Vie fixe
                case 91:
                case 92:
                case 93:
                case 94:
                case 95:
                    elem = EffectConstant.getElemSwitchEffect(SE.getEffectID());
                    statToCheck = EffectConstant.getStatIDSwitchElem(elem);
                    if(statToCheck != -1){
                        factor = fighter.getTotalStats().getEffect(statToCheck);
                    }
                    inf += (200+factor) * Formulas.getMiddleJet(SE); // Vol de vie elementaire
                    break;
                case 96:
                case 97:
                case 98:
                case 99:
                case 100:
                    elem = EffectConstant.getElemSwitchEffect(SE.getEffectID());
                    statToCheck = EffectConstant.getStatIDSwitchElem(elem);
                    if(statToCheck != -1){
                       factor = fighter.getTotalStats().getEffect(statToCheck);
                    }
                    inf += (100+factor) * Formulas.getMiddleJet(SE); // Dommage elementaire
                    break;
                // Les effets speciaux
                case 131://Poison : X Pdv  par PA
                    inf += 300 * Formulas.getMiddleJet(SE); // Dommage elementaire
                    break;
                case 132://Enleve les envoutements
                    inf += 5000;
                    break;
                case 140://Passer le tour
                    inf += 50000;
                    break;
                case 141://Tue la cible
                    inf += 60000;
                    break;
                case 279: // Dommage en % de vie restant
                    factor = fighter.getPdvMax() - fighter.getPdv();
                    inf += (factor+100) * Formulas.getMiddleJet(SE);
                    break;
                default:
                    inf += Formulas.getMiddleJet(SE);
                    break;
            }
        }
        return inf;
    }

    public SpellGrade getBestSpellForTarget(Fight fight, Fighter F, Fighter T, int launch)
    {
        if (fight == null || F == null || T == null)
            return null;
        int inflMax = 0;
        SpellGrade ss = null;
        if (F.isCollector())
        {
            for (Map.Entry<Integer, SpellGrade> SS : World.world.getGuild(F.getCollector().getGuildId()).getSpells().entrySet())
            {
                if (SS.getValue() == null)
                    continue;
                int curInfl = 0, Infl1 = 0, Infl2 = 0;
                int PA = 6;
                int usedPA[] = {0, 0};
                if (!fight.canCastSpell1(F, SS.getValue(), F.getCell(), T.getCell().getId()))
                    continue;
                curInfl = calculInfluence(SS.getValue(), F, T);
                if (curInfl == 0)
                    continue;
                if (curInfl > inflMax)
                {
                    ss = SS.getValue();
                    usedPA[0] = ss.getPACost();
                    Infl1 = curInfl;
                    inflMax = Infl1;
                }



                for (Map.Entry<Integer, SpellGrade> SS2 : World.world.getGuild(F.getCollector().getGuildId()).getSpells().entrySet())
                {
                    if (SS2.getValue() == null)
                        continue;
                    if ((PA - usedPA[0]) < SS2.getValue().getPACost())
                        continue;
                    if (!fight.canCastSpell1(F, SS2.getValue(), F.getCell(), T.getCell().getId()))
                        continue;
                    curInfl = calculInfluence(SS2.getValue(), F, T);
                    if (curInfl == 0)
                        continue;
                    if ((Infl1 + curInfl) > inflMax)
                    {
                        ss = SS.getValue();
                        usedPA[1] = SS2.getValue().getPACost();
                        Infl2 = curInfl;
                        inflMax = Infl1 + Infl2;
                    }
                    for (Map.Entry<Integer, SpellGrade> SS3 : World.world.getGuild(F.getCollector().getGuildId()).getSpells().entrySet())
                    {
                        if (SS3.getValue() == null)
                            continue;
                        if ((PA - usedPA[0] - usedPA[1]) < SS3.getValue().getPACost())
                            continue;
                        if (!fight.canCastSpell1(F, SS3.getValue(), F.getCell(), T.getCell().getId()))
                            continue;
                        curInfl = calculInfluence(SS3.getValue(), F, T);
                        if (curInfl == 0)
                            continue;
                        if ((curInfl + Infl1 + Infl2) > inflMax)
                        {
                            ss = SS.getValue();
                            inflMax = curInfl + Infl1 + Infl2;
                        }
                    }
                }
            }
        }

        else
        {
            for (Map.Entry<Integer, SpellGrade> SS : F.getMob().getSpells().entrySet())
            {
                if(SS == null)
                    continue;
                if (SS.getValue().getTypeSwitchSpellEffects() != 0)
                    continue;
                int curInfl = 0, Infl1 = 0, Infl2 = 0;
                int PA = F.getMob().getPa();
                int usedPA[] = {0, 0};
                if (!fight.canCastSpell1(F, SS.getValue(), T.getCell(), launch))
                    continue;
                curInfl = getInfl(fight, SS.getValue(),F);
                //if(curInfl == 0)continue;
                if (curInfl > inflMax)
                {
                    ss = SS.getValue();
                    usedPA[0] = ss.getPACost();
                    Infl1 = curInfl;
                    inflMax = Infl1;
                }

                for (Map.Entry<Integer, SpellGrade> SS2 : F.getMob().getSpells().entrySet())
                {
                    if (SS2.getValue().getTypeSwitchSpellEffects() != 0)
                        continue;
                    if ((PA - usedPA[0]) < SS2.getValue().getPACost())
                        continue;
                    if (!fight.canCastSpell1(F, SS2.getValue(), T.getCell(), launch))
                        continue;
                    curInfl = getInfl(fight, SS2.getValue(),F);
                    //if(curInfl == 0)continue;
                    if ((Infl1 + curInfl) > inflMax)
                    {
                        ss = SS.getValue();
                        usedPA[1] = SS2.getValue().getPACost();
                        Infl2 = curInfl;
                        inflMax = Infl1 + Infl2;
                    }
                    for (Map.Entry<Integer, SpellGrade> SS3 : F.getMob().getSpells().entrySet())
                    {
                        if (SS3.getValue().getTypeSwitchSpellEffects() != 0)
                            continue;
                        if ((PA - usedPA[0] - usedPA[1]) < SS3.getValue().getPACost())
                            continue;
                        if (!fight.canCastSpell1(F, SS3.getValue(), T.getCell(), launch))
                            continue;

                        curInfl = getInfl(fight, SS3.getValue(),F);
                        //if(curInfl == 0)continue;
                        if ((curInfl + Infl1 + Infl2) > inflMax)
                        {
                            ss = SS.getValue();
                            inflMax = curInfl + Infl1 + Infl2;
                        }
                    }
                }
            }
        }
        return ss;
    }

    public static SpellGrade getBestSpellForTargetDopeul(Fight fight, Fighter F, Fighter T, int launch, List<SpellGrade> listspell)
    {
        if (fight == null || F == null)
            return null;
        int inflMax = 0;
        SpellGrade ss = null;


        for (SpellGrade SS : listspell)
        {
            if (SS.getTypeSwitchSpellEffects() != 0)
                continue;

            int curInfl = 0, Infl1 = 0, Infl2 = 0;
            int PA = F.getMob().getPa();
            int usedPA[] = {0, 0};

            if (!fight.canCastSpell1(F, SS, T.getCell(), launch))
                continue;

            curInfl = getInfl(fight, SS,F);
            if(curInfl == 0)continue;

            if (curInfl > inflMax)
            {
                ss = SS;
                usedPA[0] = ss.getPACost();
                Infl1 = curInfl;
                inflMax = Infl1;
            }
            /*for (SortStats SS2 : listspell)
            {
                if (SS2.getSpell().getType() != 0)
                    continue;
                if ((PA - usedPA[0]) < SS2.getPACost())
                    continue;
                if (!fight.canCastSpell1(F, SS2, T.getCell(), launch))
                    continue;
                curInfl = getInfl(fight, SS2);
                System.out.println(SS2.getSpellID() + " Infuence " + curInfl + "/"+ inflMax);
                if(curInfl == 0)continue;
                if ((Infl1 + curInfl) > inflMax)
                {
                    ss = SS;
                    System.out.println("On remplace " + ss.getSpellID() + " par " + SS.getSpellID());
                    usedPA[1] = SS2.getPACost();
                    Infl2 = curInfl;
                    inflMax = Infl1 + Infl2;
                }
                for (SortStats SS3 : listspell)
                {
                    if (SS3.getSpell().getType() != 0)
                        continue;
                    if ((PA - usedPA[0] - usedPA[1]) < SS3.getPACost())
                        continue;
                    if (!fight.canCastSpell1(F, SS3, T.getCell(), launch))
                        continue;

                    curInfl = getInfl(fight, SS3);
                    if(curInfl == 0)continue;
                    System.out.println(SS3.getSpellID() + " Infuence " + curInfl + "/"+ inflMax);
                    if ((curInfl + Infl1 + Infl2) > inflMax)
                    {
                        System.out.println("On remplace " + ss.getSpellID() + " par " + SS.getSpellID());
                        ss = SS;
                        inflMax = curInfl + Infl1 + Infl2;
                    }
                }
            }*/
        }
        return ss;
    }

    public SpellGrade getBestSpellForTargetDopeulglyph(Fight fight, Fighter F, Fighter T, int launch, Map<Integer, SpellGrade> listspell)
    {
        if (fight == null || F == null)
            return null;
        int inflMax = 0;
        SpellGrade ss = null;
        launch = PathFinding.getRandomcelllignepomax(fight.getMap(), F.getCell().getId(), T.getCell().getId(), null, 5);

        for (Map.Entry<Integer, SpellGrade> SS : listspell.entrySet())
        {
            if (SS.getValue().getTypeSwitchSpellEffects() != 0)
                continue;
            int curInfl = 0, Infl1 = 0, Infl2 = 0;
            int PA = F.getMob().getPa();
            int usedPA[] = {0, 0};
            if (!fight.canCastSpell1(F, SS.getValue(), T.getCell(), launch))
                continue;
            curInfl = getInfl(fight, SS.getValue(),F);
            if(curInfl == 0)continue;
            if (curInfl > inflMax)
            {
                ss = SS.getValue();
                usedPA[0] = ss.getPACost();
                Infl1 = curInfl;
                inflMax = Infl1;
            }

            for (Map.Entry<Integer, SpellGrade> SS2 : listspell.entrySet())
            {
                if (SS2.getValue().getTypeSwitchSpellEffects() != 0)
                    continue;
                if ((PA - usedPA[0]) < SS2.getValue().getPACost())
                    continue;
                if (!fight.canCastSpell1(F, SS2.getValue(), T.getCell(), launch))
                    continue;
                curInfl = getInfl(fight, SS2.getValue(),F);
                if(curInfl == 0)continue;
                if ((Infl1 + curInfl) > inflMax)
                {
                    ss = SS.getValue();
                    usedPA[1] = SS2.getValue().getPACost();
                    Infl2 = curInfl;
                    inflMax = Infl1 + Infl2;
                }
                for (Map.Entry<Integer, SpellGrade> SS3 : listspell.entrySet())
                {
                    if (SS3.getValue().getTypeSwitchSpellEffects() != 0)
                        continue;
                    if ((PA - usedPA[0] - usedPA[1]) < SS3.getValue().getPACost())
                        continue;
                    if (!fight.canCastSpell1(F, SS3.getValue(), T.getCell(), launch))
                        continue;

                    curInfl = getInfl(fight, SS3.getValue(),F);
                    if(curInfl == 0)continue;
                    if ((curInfl + Infl1 + Infl2) > inflMax)
                    {
                        ss = SS.getValue();
                        inflMax = curInfl + Infl1 + Infl2;
                    }
                }
            }
        }
        return ss;
    }

    //TODO A REPRENDRE
    public static int getBestTargetZone(Fight fight, Fighter fighter, SpellGrade spell, int launchCell, boolean line)
    {
        if (fight == null || fighter == null)
            return 0;

        if (spell.isLineLaunch() && line == false)
        {
            if (!PathFinding.casesAreInSameLine(fight.getMap(), fighter.getCell().getId(), launchCell, 'z', 70) && !PathFinding.isNextTo(fight.getMap(), fighter.getCell().getId(), launchCell))
                return 0;
        }

        ArrayList<GameCase> possibleLaunch = new ArrayList<GameCase>();
        int CellF = -1;
        if (spell.getMaxPO() != 0)
        {
            char arg1 = 'C';
            char[] table = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v'};
            char arg2 = 'a';
            if (spell.getMaxPO() > 20)
            {
                arg2 = 'u';
            }
            else
            {
                arg2 = table[spell.getMaxPO()];
            }
            String args = Character.toString(arg1) + Character.toString(arg2);
            possibleLaunch = PathFinding.getCellListFromAreaString(fight.getMap(), launchCell, launchCell, args);
        }
        else
        {
            possibleLaunch.add(fight.getMap().getCase(launchCell));
        }

        if (possibleLaunch == null)
        {
            return -1;
        }
        int nbTarget = 0;
        for (GameCase cell : possibleLaunch)
        {
            try
            {
                if (!fight.canCastSpell1(fighter, spell, cell, launchCell))
                    continue;
                int curTarget = 0;
                int cellsnb = 0;
                ArrayList<GameCase> cells = new ArrayList<>();
                for (Effect se : spell.getEffects()) {
                    if(ArrayUtils.contains(EffectConstant.IS_DIRECTDAMMAGE_EFFECT,se.getEffectID()) ) {
                        ArrayList<GameCase> celltemp = PathFinding.getCellListFromAreaString(fight.getMap(), cell.getId(), launchCell, se.getAreaEffect());
                        if (celltemp.size() > cellsnb) {
                            cellsnb = celltemp.size();
                            cells = celltemp;
                        }
                    }
                }

                for (GameCase c : cells)
                {
                    if (c == null)
                        continue;
                    if (c.getFirstFighter() == null)
                        continue;
                    if (c.getFirstFighter().getTeam2() != fighter.getTeam2())
                        curTarget++;
                }
                if (curTarget > nbTarget)
                {
                    nbTarget = curTarget;
                    CellF = cell.getId();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        if (nbTarget > 0 && CellF != -1)
            return CellF + nbTarget * 1000;
        else
            return 0;
    }

    public int calculInfluenceHeal(SpellGrade ss)
    {

        int inf = 0;
        for (Effect SE : ss.getEffects())
        {
            if (SE.getEffectID() != 108)
                continue;//return 0; // TOTALEMENT DEBILE, SI LE HEAL FAIT PLUSIEURS CHOSE

            inf += 100 * Formulas.getMiddleJet(SE); // ADD GESTION HEAL FIXE
        }

        return inf;
    }

    public static int calculInfluence(SpellGrade ss, Fighter C, Fighter T)
    {
        int infTot = 0;
        for (Effect SE : ss.getEffects())
        {
            int inf = 0;
            switch (SE.getEffectID())
            {
                case 5:
                    inf = 500 * Formulas.getMiddleJet(SE);
                    break;
                case 89:
                    inf = 200 * Formulas.getMiddleJet(SE);
                    break;
                case 91:
                case 95:
                case 94:
                case 93:
                case 92:
                    inf = 150 * Formulas.getMiddleJet(SE);
                    break;
                case 96:
                case 98:
                case 97:
                case 99:
                case 100:
                    inf = 100 * Formulas.getMiddleJet(SE);
                    break;
                case 101:
                case 127:
                case 169:
                case 168:
                    inf = 1000 * Formulas.getMiddleJet(SE);
                    break;
                case 84:
                case 77:
                    inf = 1500 * Formulas.getMiddleJet(SE);
                    break;
                case 111:
                case 128:
                    inf = -1000 * Formulas.getMiddleJet(SE);
                    break;
                case 121:
                    inf = -100 * Formulas.getMiddleJet(SE);
                    break;
                case 131:
                case 215:
                case 216:
                case 217:
                case 218:
                case 219:
                    inf = 300 * Formulas.getMiddleJet(SE);
                    break;
                case 132:
                    inf = 2000;
                    break;
                case 138:
                    inf = -50 * Formulas.getMiddleJet(SE);
                    break;
                case 150:
                    inf = -2000;
                    break;
                case 210:
                case 211:
                case 212:
                case 213:
                case 214:
                    inf = -300 * Formulas.getMiddleJet(SE);
                    break;
                case 265:
                    inf = -250 * Formulas.getMiddleJet(SE);
                case 765://Sacrifice
                    inf = -1000;
                    break;
                case 786://Arbre de vie
                    inf = -1000;
                    break;
                case 106: // Renvoie de sort
                    inf = -900;
                    break;
            }

            if (C.getTeam() == T.getTeam())
                infTot -= inf;
            else
                infTot += inf;
        }
        return infTot;
    }

    public Fighter getEnnemyWithBuff(Fight fight, Fighter fighter) {
        Map<Integer, Fighter> ennemies = fight.getTeam(fighter.getOtherTeam());
        Map<Integer, Fighter> buffNumber = new HashMap<>();
        int nbBuffMax = 0;
        Fighter ennemieWithMaxBuff = null;
        for (Fighter ennemie : ennemies.values())
        {
            if(ennemie.getFightBuff().size() > 0)
            {
                buffNumber.put(ennemie.getFightBuff().size(), ennemie);
            }
        }
        for (Integer nbBuff : buffNumber.keySet())
        {
            if(nbBuffMax < nbBuff)
            {
                nbBuffMax = nbBuff;
            }
        }
        ennemieWithMaxBuff = buffNumber.get(nbBuffMax);
        return ennemieWithMaxBuff;
    }


    public int getMaxPoUsableSpell(Fighter fighter,List<SpellGrade> spells) {
        int maxPo = 0;
        for(SpellGrade S : spells) {
            if (S != null && S.getMaxPO() > maxPo) {
                if (S.getTypeSwitchSpellEffects() == 0 && LaunchedSpell.cooldownGood(fighter, S.getSpellID())) {
                    maxPo = S.getMaxPO();

                    if(S.isModifPO())
                        maxPo = maxPo + fighter.getBuffValue(EffectConstant.STATS_ADD_PO) - fighter.getBuffValue(EffectConstant.STATS_REM_PO);

                }
            }
        }

        return maxPo;
    }



}
