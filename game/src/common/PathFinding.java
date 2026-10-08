package common;

import area.map.GameCase;
import area.map.GameMap;
import client.Player;
import fight.Fight;
import fight.Fighter;
import fight.ia.util.AstarPathfinding;
import fight.spells.SpellGrade;
import fight.traps.Glyph;
import fight.traps.Trap;
import game.GameServer;
import game.world.World;
import kernel.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static area.map.GameCase.getCaseCoordonnee;

public class PathFinding {

    private static Integer nSteps = new Integer(0);

    public static int isValidPath(GameMap map, int cellID,
                                  AtomicReference<String> pathRef, Fight fight, Player perso,
                                  int targetCell) {
        synchronized (nSteps) {
            nSteps = 0;
            int newPos = cellID;
            int Steps = 0;
            String path = pathRef.get();
            String newPath = "";
            for (int i = 0; i < path.length(); i += 3) {
                String SmallPath = path.substring(i, i + 3);
                char dir = SmallPath.charAt(0);
                int dirCaseID = World.world.getCryptManager().cellCode_To_ID(SmallPath.substring(1));
                nSteps = 0;
                //Si en combat et Si Pas d�but du path, on v�rifie tacle
                if (fight != null && i != 0 && getEnemyFighterArround(newPos, map, fight) != null) {
                    pathRef.set(newPath);
                    return Steps;
                }
                //Si en combat, et pas au d�but du path
                if (fight != null && i != 0) {
                    for (Trap p : fight.getAllTraps()) {
                        int dist = getDistanceBetween(map, p.getCell().getId(), newPos);
                        if (dist <= p.getSize()) {
                            //on arrete le d�placement sur la 1ere case du piege
                            pathRef.set(newPath);
                            return Steps;
                        }
                    }
                }

                String[] aPathInfos = ValidSinglePath(newPos, SmallPath, map, fight, perso, targetCell).split(":");
                if (aPathInfos[0].equalsIgnoreCase("stop")) {
                    newPos = Integer.parseInt(aPathInfos[1]);
                    Steps += nSteps;
                    newPath += dir + World.world.getCryptManager().cellID_To_Code(newPos);
                    pathRef.set(newPath);
                    return -Steps;
                } else if (aPathInfos[0].equalsIgnoreCase("ok")) {
                    newPos = dirCaseID;
                    Steps += nSteps;
                } else if (aPathInfos[0].equalsIgnoreCase("stoptp")) {
                    newPos = Integer.parseInt(aPathInfos[1]);
                    Steps += nSteps;
                    newPath += dir + World.world.getCryptManager().cellID_To_Code(newPos);
                    pathRef.set(newPath);
                    return -Steps - 10000;
                } else {
                    pathRef.set(newPath);
                    return -1000;
                }
                newPath += dir + World.world.getCryptManager().cellID_To_Code(newPos);
            }
            pathRef.set(newPath);
            return Steps;
        }
    }

    public static boolean getcasebetwenenemie(int cellId, GameMap map, Fight fight, Fighter F)
    {
        char[] dirs = {'b', 'd', 'f', 'h'};
        for (char dir : dirs)
        {
            GameCase cell = map.getCase(GetCaseIDFromDirrection(cellId, dir, map, false));
            if (cell == null)
                continue;
            Fighter f = cell.getFirstFighter();

            if (f != null && f.getTeam2() != F.getTeam2())
                return true;
        }
        return false;
    }

    public static boolean isCellInCircle(int cellID, int centerX, int centerY, int rayon, GameMap map) {
        GameCase.Coord coord = getCaseCoordonnee(map, cellID);
        int cellX = coord.x;
        int cellY = coord.y;
        int distance = Math.abs(cellX - centerX) + Math.abs(cellY - centerY);
        return distance <= rayon;
    }

    public boolean checkAlign(GameMap map, int cellid1 ,int cellid2)
    {
        GameCase.Coord cellcoord1 = getCaseCoordonnee(map,cellid1);
        GameCase.Coord cellcoord2 = getCaseCoordonnee(map,cellid2);
        if(cellcoord1.x == cellcoord2.x)
        {
            return true;
        }
        if(cellcoord1.y == cellcoord2.y)
        {
            return true;
        }
        return false;
    }


    // Dev By Arwase
    public static boolean isValidCoordinate(GameMap map, int X ,int Y){
        int diff = Math.abs(map.getH()-map.getW());
        int maxrange = map.getH()+map.getW();
        if( (X+Y) >= 0 && ((X+Math.abs(Y))<=(maxrange)) && Y<=X && Y>(-map.getW()) && Y<map.getH()){
            if(Y <= 0 && (X+Math.abs(Y)) > (maxrange-(diff*2)) ) {
                return false;
            }
            return true;
        }
        return  false;
    }


    // Dev By Arwase
    public static List<GameCase> drawCircleBorder(int rayonduCercle, int cellIDstart, GameMap map) {
        List<GameCase> CircleBorderCase = new ArrayList<>();

        if(rayonduCercle == 0){
            CircleBorderCase.add(map.getCase(cellIDstart));
            return CircleBorderCase;
        }
        // Aucun sans que le rayon dépasse le nombre de case maximal en diagonal
        if(rayonduCercle < map.getH()+map.getW()) {
            // De Haut a gauche vers haut a droite
            GameCase.Coord coord = getCaseCoordonnee(map, cellIDstart);
            int XcircleStart = coord.x - rayonduCercle;
            int YcircleStart = coord.y;
            for (int i = 0; i < rayonduCercle; i++) {
                XcircleStart += 1;
                YcircleStart -= 1;

                if (isValidCoordinate(map, XcircleStart, YcircleStart)) {
                    int CaseIDToAdd = getCaseByPos(map, XcircleStart, YcircleStart); // You need to implement this method
                    GameCase CaseToAdd = map.getCase(CaseIDToAdd);
                    if (CaseToAdd != null) {
                        CircleBorderCase.add(CaseToAdd);
                    }
                }
            }

            // De Haut a droite vers bas a droite
            for (int i = 0; i < rayonduCercle; i++) {
                XcircleStart += 1;
                YcircleStart += 1;

                if (isValidCoordinate(map, XcircleStart, YcircleStart)) {
                    int CaseIDToAdd = getCaseByPos(map, XcircleStart, YcircleStart); // You need to implement this method
                    GameCase CaseToAdd = map.getCase(CaseIDToAdd);
                    if (CaseToAdd != null) {
                        CircleBorderCase.add(CaseToAdd);
                    }
                }
            }

            // De bas a droite vers bas a gauche
            for (int i = 0; i < rayonduCercle; i++) {
                XcircleStart -= 1;
                YcircleStart += 1;

                if (isValidCoordinate(map, XcircleStart, YcircleStart)) {
                    int CaseIDToAdd = getCaseByPos(map, XcircleStart, YcircleStart); // You need to implement this method
                    GameCase CaseToAdd = map.getCase(CaseIDToAdd);
                    if (CaseToAdd != null) {
                        CircleBorderCase.add(CaseToAdd);
                        //SocketManager.send("Arwase", "Gf901|" + CaseIDToAdd);
                    }
                }
            }

            // De bas a gauche vers haut a gauche
            for (int i = 0; i < rayonduCercle; i++) {
                XcircleStart -= 1;
                YcircleStart -= 1;

                if (isValidCoordinate(map, XcircleStart, YcircleStart)) {
                    int CaseIDToAdd = getCaseByPos(map, XcircleStart, YcircleStart); // You need to implement this method
                    GameCase CaseToAdd = map.getCase(CaseIDToAdd);
                    if (CaseToAdd != null) {
                        CircleBorderCase.add(CaseToAdd);
                    }
                }
            }

        }
        // La boucle est bouclé
        return CircleBorderCase;
    }


    public static List<GameCase> drawRectangleBorder(int var2, int var3, int var5, GameMap map) {
        List<GameCase> correctedCases = new ArrayList<>();
        int var7 = map.getW() * 2 - 1;
        int var8 = var5;

        int var9 = 0;
        while (var9 < var2) {
            if (var9 != 0) {
                var8 = var8 + 1;
            }
            GameCase var11 = map.getCase(var8); // You need to implement this method
            correctedCases.add(var11);
            var9 = var9 + 1;
        }

        var9 = var9 - 1;
        int var10 = 0;
        while (var10 < var3 - 1) {
            var8 = var8 + var7;
            GameCase var11 = map.getCase(var8); // You need to implement this method
            correctedCases.add(var11);
            var10 = var10 + 1;
        }

        var9 = var2 - 1;
        while (var9 >= 0) {
            if (var9 != var2 - 1) {
                var8 = var8 - 1;
            }
            GameCase var11 = map.getCase(var8); // You need to implement this method
            correctedCases.add(var11);
            var9 = var9 - 1;
        }

        var9 = var9 + 1;
        int var15 = var3 - 2;
        while (var15 >= 0) {
            var8 = var8 - var7;
            GameCase var11 = map.getCase(var8); // You need to implement this method
            correctedCases.add(var11);
            var15 = var15 - 1;
        }

        return correctedCases;
    }


    public static int getCaseIdWithPo(int cellIdFighter, char dir, int dist)
    {
        int result = cellIdFighter;
        char dire = 'z';
        if(dir == 'z')
        {
            Random random = new Random();
            int rand = random.nextInt(4 - 1 + 1);
            switch (rand)
            {
                case 1 :
                    dire = 'b';
                    break;
                case 2 :
                    dire = 'd';
                    break;
                case 3 :
                    dire = 'f';
                    break;
                case 4 :
                    dire = 'h';
                    break;
            }
        }
        else{
            dire = dir;
        }
        switch (dire)
        {
            case 'b' :
                for(int i = 0; i < dist; i++)
                {
                    result += 15;
                }
                break;
            case 'd' :
                for(int i = 0; i < dist; i++)
                {
                    result += 14;
                }
                break;
            case 'f' :
                for(int i = 0; i < dist; i++)
                {
                    result -= 15;
                }
                break;
            case 'h' :
                for(int i = 0; i < dist; i++)
                {
                    result -= 14;
                }
                break;
        }
        return result;
    }

    public static int getValidCaseIdWithPo(int cellIdFighter, char dir, int dist)
    {
        int result = cellIdFighter;
        char dire = 'z';
        if(dir == 'z')
        {
            Random random = new Random();
            int rand = random.nextInt(4 - 1 + 1);
            switch (rand)
            {
                case 1 :
                    dire = 'b';
                    break;
                case 2 :
                    dire = 'd';
                    break;
                case 3 :
                    dire = 'f';
                    break;
                case 4 :
                    dire = 'h';
                    break;
            }
        }
        else{
            dire = dir;
        }
        switch (dire)
        {
            case 'b' :
                for(int i = 0; i < dist; i++)
                {
                    result += 15;
                }
                break;
            case 'd' :
                for(int i = 0; i < dist; i++)
                {
                    result += 14;
                }
                break;
            case 'f' :
                for(int i = 0; i < dist; i++)
                {
                    result -= 15;
                }
                break;
            case 'h' :
                for(int i = 0; i < dist; i++)
                {
                    result -= 14;
                }
                break;
        }
        return result;
    }

    public static ArrayList<GameCase> getAvailableCellsTowardCible(int cellId, GameMap map, Fighter F,Fight fight){
        ArrayList<GameCase> path = new AstarPathfinding(fight.getMapOld(), fight, F.getCell().getId(), cellId).getShortestPath(-1);
        return path;
    }

    public static ArrayList<GameCase> getInLineCellsWithLOS(Fighter F,Fight fight){
        ArrayList<GameCase> casesinLine = new ArrayList<>();
        ArrayList<GameCase> casesb = getAllCasesFromDir(fight,F,'b');
        ArrayList<GameCase> casesd = getAllCasesFromDir(fight,F,'d');
        ArrayList<GameCase> casesf = getAllCasesFromDir(fight,F,'f');
        ArrayList<GameCase> casesh = getAllCasesFromDir(fight,F,'h');
        for (GameCase Cell :  casesb) {
            if(checkLoSBetween2Cells(fight.getMap(), Cell.getId(), F.getCell().getId())) {
                casesinLine.add(Cell);
            }
        }
        for (GameCase Cell :  casesd) {
            if(checkLoSBetween2Cells(fight.getMap(), Cell.getId(), F.getCell().getId())) {
                casesinLine.add(Cell);
            }
        }
        for (GameCase Cell :  casesf) {
            if(checkLoSBetween2Cells(fight.getMap(), Cell.getId(), F.getCell().getId())) {
                casesinLine.add(Cell);
            }
        }
        for (GameCase Cell :  casesh) {
            if(checkLoSBetween2Cells(fight.getMap(), Cell.getId(), F.getCell().getId())) {
                casesinLine.add(Cell);
            }
        }

        return casesinLine;
    }

    public static ArrayList<GameCase> getAllCellsWithLOS(Fighter F,Fight fight, int distance){
        ArrayList<GameCase> casesinLOS = new ArrayList<>();
        List<GameCase> cases = getAllCases(fight);
        for (GameCase Cell :  cases) {
                if(Cell.isWalkable(false)) {
                    if ( getDistanceBetween(fight.getMap(), F.getCell().getId(), Cell.getId()) <= distance) {
                        if (checkLoSBetween2Cells(fight.getMap(), Cell.getId(), F.getCell().getId())) {

                            casesinLOS.add(Cell);
                        }
                    }
                }
        }

        return casesinLOS;
    }

    public static ArrayList<GameCase> getAllCellsWithLOS(Fighter F,Fight fight, int distance, int distmin){
        ArrayList<GameCase> casesinLOS = new ArrayList<>();
        List<GameCase> cases = getAllCases(fight);
        for (GameCase Cell :  cases) {
            if(Cell.isWalkable(false)) {
                if ( getDistanceBetween(fight.getMap(), F.getCell().getId(), Cell.getId()) <= distance && getDistanceBetween(fight.getMap(), F.getCell().getId(), Cell.getId()) >= distmin) {
                    if (checkLoSBetween2Cells(fight.getMap(), Cell.getId(), F.getCell().getId())) {

                        casesinLOS.add(Cell);
                    }
                }
            }
        }

        return casesinLOS;
    }

    public static boolean isCACwithEnnemy(Fighter fighter,
                                          ArrayList<Fighter> Ennemys) {
        for (Fighter f : Ennemys)
            if (isNextTo(fighter.getFight().getMap(), fighter.getCell().getId(), f.getCell().getId()))
                return true;
        return false;
    }

    public static ArrayList<Fighter> getEnemyFighterArround(int cellID,
                                                            GameMap map, Fight fight) {
        char[] dirs = {'b', 'd', 'f', 'h'};
        ArrayList<Fighter> enemy = new ArrayList<Fighter>();

        for (char dir : dirs) {
            GameCase cell = map.getCase((short) GetCaseIDFromDirrection(cellID, dir, map, false));
            if(cell != null) {
                Fighter f = cell.getFirstFighter();
                if (f != null) {
                    if (f.getFight() != fight)
                        continue;
                    if (f.getTeam() != fight.getFighterByOrdreJeu().getTeam())
                        enemy.add(f);
                }
            }
        }
        if (enemy.size() == 0 || enemy.size() == 4)
            return null;

        return enemy;
    }

    public static boolean isCacTo(GameMap map, int cellLanceur, int cellTarget)
    {
        boolean result = false;
        int caseHaut = cellLanceur - 15;
        int caseDroite = caseHaut + 1;
        int caseBas = cellLanceur + 15;
        int caseGauche = caseBas - 1;
        int[] casesID = new int[] {caseBas, caseGauche, caseDroite, caseHaut};
        for(int caseId : casesID)
        {
            if(caseId == cellTarget & map.getCase(cellTarget) != null)
            {
                result = true;
            }
        }
        return  result;
    }

    public static boolean isNextTo(GameMap map, int cell1, int cell2) {
        boolean result = false;
        if (cell1 + 14 == cell2)
            result = true;
        else if (cell1 + 15 == cell2)
            result = true;
        else
            result = cell1 - 14 == cell2 || cell1 - 15 == cell2;
        return result;
    }

    public static String ValidSinglePath(int CurrentPos, String Path, GameMap map,
                                         Fight fight, Player perso, int targetCell) {
        nSteps = 0;
        char dir = Path.charAt(0);
        int dirCaseID = World.world.getCryptManager().cellCode_To_ID(Path.substring(1)), check = ("353;339;325;311;297;283;269;255;241;227;213;228;368;354;340;326;312;298;284;270;256;242;243;257;271;285;299;313;327;341;355;369;383".contains(String.valueOf(targetCell)) ? 1 : 0);

        if (fight != null && fight.isOccuped(dirCaseID))
            return "no:";

        if(perso != null) {
            if (perso.getCases)
                if (!perso.thisCases.contains(CurrentPos))
                    perso.thisCases.add(CurrentPos);
        }
        // int oldPos = CurrentPos;
        int lastPos = CurrentPos, oldPos = CurrentPos;

        for (nSteps = 1; nSteps <= 64; nSteps++) {
            if (GetCaseIDFromDirrection(lastPos, dir, map, fight != null) == dirCaseID) {
                if (fight != null && fight.isOccuped(dirCaseID))
                    return "stop:" + lastPos;
                GameCase cell = map.getCase(dirCaseID);
                if(map.getId() == 2019) {
                    if (cell.getId() == 297 && ((cell.getPlayers() != null && cell.getPlayers().size() > 0) || perso.getSexe() == 0))
                        return "stop:" + oldPos;
                    if (cell.getId() == 282 && ((cell.getPlayers() != null && cell.getPlayers().size() > 0) || perso.getSexe() == 1))
                        return "stop:" + oldPos;
                }
                if (cell.isWalkable(true, fight != null, targetCell)) {
                    return "ok:";
                } else {
                    nSteps--;
                    return ("stop:" + lastPos);
                }
            } else {
                lastPos = GetCaseIDFromDirrection(lastPos, dir, map, fight != null);
            }

            if (fight == null) {
                if (perso.getCurMap().getId() == 9588) {/*
                    String cell = "353;339;325;311;297;283;269;255;241;227;213;228;368;354;340;326;312;298;284;270;256;242;243;257;271;285;299;313;327;341;355;369;383";
                    if (cell.contains(String.valueOf(lastPos)))
                        check++;
                    if (check > 1)
                        return "stoptp:" + lastPos;
                */}
                try {
                    if (perso.getCases)
                        if (!perso.thisCases.contains(lastPos))
                            perso.thisCases.add(lastPos);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (lastPos < 0)
                    continue;
                GameCase _case = map.getCase(lastPos);
                if (_case == null)
                    continue;
                if(map.getId() == 2019) {
                    if(_case.getId() == 297 && ((_case.getPlayers() != null && _case.getPlayers().size() > 0) || perso.getSexe() == 0))
                        return "stop:" + oldPos;
                    if(_case.getId() == 282 && ((_case.getPlayers() != null && _case.getPlayers().size() > 0) || perso.getSexe() == 1))
                        return "stop:" + oldPos;
                }
                if (_case.getOnCellStopAction())
                    return "stop:" + lastPos;
                if (map.isAggroByMob(perso, lastPos))
                    return "stop:" + lastPos;
                if(!map.getCase(lastPos).isWalkable(true, false, targetCell))
                    return "stop:" + oldPos;
                oldPos = lastPos;
            } else {
                if (fight.isOccuped(lastPos))
                    return "no:";
                if (getEnemyFighterArround(lastPos, map, fight) != null)//Si ennemie proche
                    return "stop:" + lastPos;
                for (Trap p : fight.getAllTraps()) {
                    if (getDistanceBetween(map, p.getCell().getId(), lastPos) <= p.getSize()) {//on arrete le d�placement sur la 1ere case du piege
                        return "stop:" + lastPos;
                    }
                }
            }
        }
        return "no:";
    }

    public static ArrayList<Integer> getListCaseFromFighter(Fight fight,
                                                            Fighter fighter, int cellStart, ArrayList<SpellGrade> SS) {
        int bestPo = 0;
        if (SS != null) {
            for (SpellGrade sort : SS) {
                if (sort.getMaxPO() > bestPo)
                    bestPo = sort.getMaxPO();
            }
        }
        int pmNumber = fighter.getCurPm(fight);
        /*
		 * if(fighter != fight.getCurFighter()) pmNumber = fighter.getPm();
		 */
        int cellNumber = Formulas.countCell(pmNumber + 1);
        int _loc1_ = 0;
        int _loc3_ = 0;
        char[] dirs = {'b', 'd', 'f', 'h'};
        ArrayList<Integer> cellT = new ArrayList<Integer>();
        ArrayList<Integer> cellY = new ArrayList<Integer>();
        cellT.add(cellStart);
        if (fighter.getCurPm(fight) <= 0)
            return cellT;
        ArrayList<Integer> cell = new ArrayList<Integer>();
        //int distanceMin = bestPo + 4;
        while (_loc1_++ < cellNumber) {
            int _loc2_ = 0;
            if (cellT.size() <= _loc3_ || cellT.isEmpty()) {
                //Fini de tout boucl�
                cell.addAll(cellT);
                cellT.clear();
                cellT.addAll(cellY);
                cellY.clear();
                _loc3_ = 0;
            }

            if (cellT.isEmpty() && cellY.isEmpty())
                return cell;

            _loc2_ = cellT.get(_loc3_);
            for (char dir : dirs) {
                int _loc4_ = PathFinding.getCaseIDFromDirrection(_loc2_, dir, fight.getMapOld());
                if(fight.getMap() == null) continue;
                if (_loc4_ < 0 || fight.getMap().getCase(_loc4_) == null
                        || cell.contains(_loc4_) || cellT.contains(_loc4_)
                        || cellY.contains(_loc4_))
                    continue;
                if (haveFighterOnThisCell(_loc4_, fight)
                        || !fight.getMapOld().getCase(_loc4_).isWalkable(true, true, -1))
                    continue;
                cellY.add(_loc4_);
            }
            _loc3_++;
        }
        return cell;
    }

    public static ArrayList<Integer> getListCaseFromFighter(Fight fight,
                                                            Fighter fighter, ArrayList<SpellGrade> SS, Fighter nearest) {
        int bestPo = 0;
        for (SpellGrade sort : SS) {
            if (sort.getMaxPO() > bestPo)
                bestPo = sort.getMaxPO();
        }
        int cellNumber = Formulas.countCell(fighter.getCurPm(fight) + 1);
        int _loc1_ = 0;
        int _loc3_ = 0;
        char[] dirs = {'b', 'd', 'f', 'h'};
        ArrayList<Integer> cellT = new ArrayList<>();
        ArrayList<Integer> cellY = new ArrayList<>();
        cellT.add(fighter.getCell().getId());
        ArrayList<Integer> cell = new ArrayList<>();
        while (_loc1_++ < cellNumber) {
            int _loc2_ = 0;
            if (cellT.size() <= _loc3_ || cellT.isEmpty()) {
                //Fini de tout boucl�
                cell.addAll(cellT);
                cellT.clear();
                cellT.addAll(cellY);
                cellY.clear();
                _loc3_ = 0;
            }
            if (cellT.isEmpty() && cellY.isEmpty())
                return cell;
            _loc2_ = cellT.get(_loc3_);
            for (char dir : dirs) {
                int _loc4_ = (short) PathFinding.getCaseIDFromDirrection(_loc2_, dir, fight.getMapOld());
                if (_loc4_ < 0 || fight.getMap().getCase(_loc4_) == null
                        || cell.contains(_loc4_) || cellT.contains(_loc4_)
                        || cellY.contains(_loc4_)) {
                    continue;
                }
                if (haveFighterOnThisCell(_loc4_, fight)
                        || !fight.getMapOld().getCase(_loc4_).isWalkable(true, true, -1))
                    continue;

                cellY.add(_loc4_);
            }
            _loc3_++;
        }
        return cell;
    }

    public static ArrayList<Integer> getAllCaseIdAllDirrection(int caseId, GameMap map) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        char[] dir = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        int _c = -1;
        for (char d : dir) {
            _c = GetCaseIDFromDirrection(caseId, d, map, false);
            if (_c > 0)
                list.add(_c);
        }
        return list;
    }

    public static int GetCaseIDFromDirrection(int CaseID, char Direction,
                                              GameMap map, boolean Combat) {
        if (map == null)
            return -1;
        switch (Direction) {
            case 'a':
                return Combat ? -1 : CaseID + 1;
            case 'b':
                return CaseID + map.getW();
            case 'c':
                return Combat ? -1 : CaseID + (map.getW() * 2 - 1);
            case 'd':
                return CaseID + (map.getW() - 1);
            case 'e':
                return Combat ? -1 : CaseID - 1;
            case 'f':
                return CaseID - map.getW();
            case 'g':
                return Combat ? -1 : CaseID - (map.getW() * 2 - 1);
            case 'h':
                return CaseID - map.getW() + 1;
        }
        return -1;
    }


    public static int getDistanceBetween(GameMap map, int id1, int id2) {
        if (id1 == id2)
            return 0;
        if (map == null)
            return 0;

        int diffX = Math.abs(getCellXCoord(map, id1) - getCellXCoord(map, id2));
        int diffY = Math.abs(getCellYCoord(map, id1) - getCellYCoord(map, id2));
        return (diffX + diffY);
    }

    public static Fighter getEnemyAround(int cellId, GameMap map, Fight fight) {
        char[] dirs = {'b', 'd', 'f', 'h'};
        for (char dir : dirs) {
            GameCase cell = map.getCase(GetCaseIDFromDirrection(cellId, dir, map, false));
            if (cell == null)
                continue;
            Fighter f = cell.getFirstFighter();

            if (f != null)
                if (f.getFight() == fight)
                    if (f.getTeam() != fight.getFighterByOrdreJeu().getTeam())
                        return f;
        }
        return null;
    }

    public static int newCaseAfterPush(final Fight fight, final GameCase CCase, final GameCase TCase, int value) {
        GameMap map = fight.getMap();
        if(map == null)
            return 0;

        if (CCase.getId() == TCase.getId())
            return 0;

        char c = getDirBetweenTwoCase(CCase.getId(), TCase.getId(), map, true);
        int id = TCase.getId();

        if (value < 0) {
            c = getOpositeDirection(c);
            value = -value;
        }
        boolean b = false;
        for (int a = 0; a < value; a++) {
            int nextCase = GetCaseIDFromDirrection(id, c, map, true);
            for (final Trap trap : fight.getAllTraps()) {
                final GameCase nextCell = map.getCase(nextCase);
                if (PathFinding.getDistanceBetween(fight.getMap(), trap.getCell().getId(), nextCell.getId()) <= trap.getSize())
                    b = true;
            }
            if (map.getCase(nextCase) != null && map.getCase(nextCase).isWalkable(true) && map.getCase(nextCase).getFirstFighter() == null) {
                id = nextCase;
                if(b) break;
            }
            else return -(value - a);
        }

        if (id == TCase.getId()) id = 0;
        return id;
    }


    /*public static int newCaseAfterPush(Fight fight, GameCase currentCell, GameCase targetCell, int value, boolean piege) {
        GameMap map = fight.getMap();

        if (currentCell.getId() == targetCell.getId())
            return 0;
        char dir = getDirBetweenTwoCase(currentCell.getId(), targetCell.getId(), map, true);
        int id = targetCell.getId();

        if (value < 0) {
            dir = getOpositeDirection(dir);
            value = -value;
        }

        boolean b = false;
        for (int a = 0; a < value; a++) {
            int nextCase = GetCaseIDFromDirrection(id, dir, map, true);

            for (Trap trap : fight.getAllTraps()) {
                int val = getDistanceBetweenTwoCase(map, trap.getCell(), map.getCase(nextCase));
                if (getDistanceBetweenTwoCase(map, trap.getCell(), map.getCase(nextCase)) >= trap.getSize()) {
                    id = nextCase;
                    b = true;
                }
            }

            if (b) break;

            if (map.getCase(nextCase) != null && map.getCase(nextCase).isWalkable(false) && map.getCase(nextCase).getFighters().isEmpty())
                id = nextCase;
            else
                return -(value - a);
        }

        if (id == targetCell.getId())
            return 0;
        return id;
    }*/

    public static int getDistanceBetweenTwoCase(GameMap map, GameCase c1, GameCase c2) {
        int dist = 0;
        if (c1 == null || c2 == null) {
            return dist;
        }
        if (c1.getId() == c2.getId())
            return dist;
        int id = c1.getId();
        char c = getDirBetweenTwoCase(c1.getId(), c2.getId(), map, true);

        while (c2 != map.getCase(id)) {
            id = GetCaseIDFromDirrection(id, c, map, true);
            if (map.getCase(id) == null) {
                return dist;
            }
            dist++;
        }
        return dist;
    }

    public static char getOpositeDirection(char c) {
        switch (c) {
            case 'a':
                return 'e';
            case 'b':
                return 'f';
            case 'c':
                return 'g';
            case 'd':
                return 'h';
            case 'e':
                return 'a';
            case 'f':
                return 'b';
            case 'g':
                return 'c';
            case 'h':
                return 'd';
        }
        return 0x00;
    }

    public static int getNearenemycontremur(GameMap map, int startCell,
                                            int endCell, ArrayList<GameCase> forbidens)
    {
        //On prend la cellule autour de la cible, la plus proche
        int dist = 1000;
        int cellID = startCell;
        if (forbidens == null)
            forbidens = new ArrayList<GameCase>();
        char[] dirs = {'b', 'd', 'f', 'h'};
        GameCase hd = null, bg = null, hg = null, bd = null;
        for (char d : dirs)
        {
            if(d == 'b')//En Haut à Droite.
            {
                int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                hd = map.getCase(c);
            }
            else if(d == 'f')//En Bas à Gauche.
            {
                int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                bg = map.getCase(c);
            }
            else if(d == 'd')//En Haut à Gauche.
            {
                int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                hg = map.getCase(c);
            }
            else if(d == 'h')//En Bas à Droite.
            {
                int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                bd = map.getCase(c);
            }
        }

        GameCase[] tab = {hd,bg,hg,bd};
        for(GameCase c : tab)
        {
            if(c == null)
                continue;
            if(c == hd)
            {
                if(!c.isWalkable(false) && bg != null || c.getFirstFighter() != null && bg != null)
                {
                    // On cherche la distance entre
                    int dis = PathFinding.getDistanceBetween(map, endCell, bg.getId());
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && !forbidens.contains(bg))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = bg.getId();
                    }
                }
            }
            else if(c == bg)
            {
                if(!c.isWalkable(false) && hd != null || c.getFirstFighter() != null && hd != null)
                {
                    // On cherche la distance entre
                    int dis = PathFinding.getDistanceBetween(map, endCell, hd.getId());
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && !forbidens.contains(hd))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = hd.getId();
                    }
                }
            }
            else if(c == bd)
            {
                if(!c.isWalkable(false) && hg != null || c.getFirstFighter() != null && hg != null)
                {
                    // On cherche la distance entre
                    int dis = PathFinding.getDistanceBetween(map, endCell, hg.getId());
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && !forbidens.contains(hg))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = hg.getId();
                    }
                }
            }
            else if(c == hg)
            {
                if(!c.isWalkable(false) && bd != null || c.getFirstFighter() != null && bd != null)
                {
                    // On cherche la distance entre
                    int dis = PathFinding.getDistanceBetween(map, endCell, bd.getId());
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && !forbidens.contains(bd))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = bd.getId();
                    }
                }
            }
        }

        //On renvoie -1 si pas trouvé
        return cellID == startCell ? -1 : cellID;
    }
    
    public static int getCaseBetweenEnemy(int cellId, GameMap map, Fight fight)
    {
        if(map == null) return 0;
        char[] dirs = {'f', 'd', 'b', 'h'};
        for (char dir : dirs)
        {
            int id = GetCaseIDFromDirrection(cellId, dir, map, false);
            GameCase cell = map.getCase(id);
            if (cell == null)
                continue;
            Fighter f = cell.getFirstFighter();

            if (f == null && cell.isWalkable(false))
                return cell.getId();
        }
        return 0;
    }
    public static int getCaseBetweenEnemy(int cellId, GameMap map, Fight fight, Fighter caster)
    {
        if(map == null) return 0;
        char[] dirs = {'f', 'd', 'b', 'h'};
        for (char dir : dirs)
        {
            int id = GetCaseIDFromDirrection(cellId, dir, map, false);
            GameCase cell = map.getCase(id);
            if (cell == null)
                continue;
            Fighter f = cell.getFirstFighter();

            if (f == null && caster.getCell() != cell && cell.isWalkable(false))
                return cell.getId();
        }
        return 0;
    }

    public static int getAvailableCellArround(Fight fight, int cellId, List<Integer> cellsUnavailable) {
        if(fight == null) return 0;
        char[] dirs = {'f', 'd', 'b', 'h'};

        for (char dir : dirs) {
            int id = GetCaseIDFromDirrection(cellId, dir, fight.getMap(), false);
            GameCase cell = fight.getMap().getCase(id);

            if (cell != null) {
                Fighter fighter = cell.getFirstFighter();
                if (fighter == null && cell.isWalkable(false)) {
                    if(cellsUnavailable != null && cellsUnavailable.contains(cell.getId()))
                        continue;
                    return cell.getId();
                }
            }
        }
        return 0;
    }

    public static ArrayList<GameCase> getAvailableCellNearPlayer(Fight fight, int cellId)
    {
        ArrayList<GameCase> cellAroundPlayer = new ArrayList<GameCase>();
        int cellHaut = cellId -19;
        int cellBas = cellId + 19;
        int cellDroite = cellHaut + 1;
        int cellGauche = cellBas - 1;
        GameMap map = fight.getMap();
        GameCase cellDuHaut = map.getCase(cellHaut);
        GameCase cellDuBas = map.getCase(cellBas);
        GameCase cellDuDroite = map.getCase(cellDroite);
        GameCase cellDuGauche = map.getCase(cellGauche);
        if(cellDuHaut != null)
        {
            if(cellDuHaut.isWalkable(false) & cellDuHaut.getFirstFighter() == null)
            {
                cellAroundPlayer.add(cellDuHaut);
            }
        }
        if(cellDuBas != null)
        {
            if(cellDuBas.isWalkable(false) & cellDuBas.getFirstFighter() == null)
            {
                cellAroundPlayer.add(cellDuBas);
            }
        }
        if(cellDuDroite != null)
        {
            if(cellDuDroite.isWalkable(false) & cellDuDroite.getFirstFighter() == null)
            {
                cellAroundPlayer.add(cellDuDroite);
            }
        }
        if(cellDuGauche != null)
        {
            if(cellDuGauche.isWalkable(false) & cellDuGauche.getFirstFighter() == null)
            {
                cellAroundPlayer.add(cellDuGauche);
            }
        }
        return cellAroundPlayer;
    }

    public static int getNearestligneGA(Fight fight, int startCell,
                                        int endCell, ArrayList<GameCase> forbidens, int distmin)
    {

        GameMap map = fight.getMap();
        ArrayList<Glyph> glyphs = new ArrayList<Glyph>();//Copie du tableau
        glyphs.addAll(fight.getAllGlyphs());
        int dist = 1000;
        int j = 0;
        //On prend la cellule autour de la cible, la plus proche
        int cellID = startCell;
        if (forbidens == null)
            forbidens = new ArrayList<>();
        char[] dirs = {'b', 'd', 'f', 'h'};
        for (char d : dirs)
        {

            int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
            if (map.getCase(c) == null)
                continue;
            int dis = PathFinding.getDistanceBetween(map, endCell, c);
            int dis2 = PathFinding.getDistanceBetween(map, startCell, c);
            // Si la distance est strictement inférieur à 1000 et que la case
            // est marchable et que personne ne
            // se trouve dessus et que la case n'est pas interdite
            if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                    && map.getCase(c).getFirstFighter() == null
                    && !forbidens.contains(map.getCase(c)))
            {
                boolean ok1 = true;
                for(Glyph g : glyphs)
                {
                    if(PathFinding.getDistanceBetween(map,c , g.getCell().getId()) <= g.getSize() && g.getSpell() != 476)
                        ok1 = false;
                }

                if(!ok1)
                    continue;
                // On crée la distance
                dist = dis;
                // On modifie la cellule
                cellID = c;
            }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                    && map.getCase(c).getFirstFighter() == null
                    && !forbidens.contains(map.getCase(c)))
            {
                boolean ok1 = true;
                for(Glyph g : glyphs)
                {
                    if(PathFinding.getDistanceBetween(map,c , g.getCell().getId()) <= g.getSize() && g.getSpell() != 476)
                        ok1 = false;
                }

                if(!ok1)
                    continue;
                // On crée la distance
                dist = dis;
                // On modifie la cellule
                cellID = c;
            }
            boolean ok = false;
            while(!ok && j < 500)
            {
                j++;

                int h = PathFinding.GetCaseIDFromDirrection(c, d, map, true);
                if (map.getCase(h) == null)
                    ok = true;

                dis = PathFinding.getDistanceBetween(map, endCell, c);
                dis2 = PathFinding.getDistanceBetween(map, startCell, c);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    boolean ok1 = true;
                    for(Glyph g : glyphs)
                    {
                        if(PathFinding.getDistanceBetween(map,c , g.getCell().getId()) <= g.getSize() && g.getSpell() != 476)
                            ok1 = false;
                    }

                    if(!ok1)
                        continue;
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                } else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    boolean ok1 = true;
                    for(Glyph g : glyphs)
                    {
                        if(PathFinding.getDistanceBetween(map,c , g.getCell().getId()) <= g.getSize() && g.getSpell() != 476)
                            ok1 = false;
                    }

                    if(!ok1)
                        continue;
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }
                c = h;
            }


        }

        return cellID == startCell ? -1 : cellID;
    }

    public static Fighter getNearestligneenemy(GameMap map, int startCell,
                                               Fighter f, int dist)
    {
        //On prend la cellule autour de la cible, la plus proche
        Fighter E = null;
        char[] dirs = {'b', 'd', 'f', 'h'};
        int endCell = f.getCell().getId();
        for (char d : dirs)
        {
            int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
            if (map.getCase(c) == null)
                continue;
            int dis = PathFinding.getDistanceBetween(map, endCell, c);
            // Si la distance est strictement inférieur à 1000 et que la case
            // est marchable et que personne ne
            // se trouve dessus et que la case n'est pas interdite
            if (dis < dist && map.getCase(c).getFirstFighter() != null)
            {
                if(map.getCase(c).getFirstFighter().getTeam2() != f.getTeam2())
                    E = map.getCase(c).getFirstFighter();
            }
            boolean ok = false;
            while(!ok)
            {
                int h = PathFinding.GetCaseIDFromDirrection(c, d, map, true);
                if (map.getCase(h) != null)
                {
                    dis = PathFinding.getDistanceBetween(map, endCell, h);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && map.getCase(c).getFirstFighter() != null)
                    {
                        if(map.getCase(c).getFirstFighter().getTeam2() != f.getTeam2())
                            E = map.getCase(c).getFirstFighter();
                    }
                }else
                    ok = true;
                c = h;
            }


        }
        //On renvoie null si pas trouvé
        return E;
    }

    public static int getNearenemycontremur2(GameMap map, int startCell, int endCell, ArrayList<GameCase> forbidens, Fighter F) {
        //On prend la cellule autour de la cible, la plus proche
        int dist = 1000;
        int cellID = startCell;
        if (forbidens == null)
            forbidens = new ArrayList<>();
        char[] dirs = {'b', 'd', 'f', 'h'};
        char perso = ' ';
        for (char d : dirs) {
            int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
            if (map.getCase(c) == null)
                continue;
            if(map.getCase(c) == F.getCell())
                perso = d;
        }
        
        for (char d : dirs) {
            if(getOpositeDirection(perso) == d) {
                int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null) continue;
                if(!map.getCase(c).isWalkable(false) || map.getCase(c).getFirstFighter() != null) {
                    int dis = PathFinding.getDistanceBetween(map, endCell, map.getCase(c).getId());
                    if (dis < dist && !forbidens.contains(map.getCase(c)) && F.getCell() != map.getCase(c)) {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = map.getCase(c).getId();
                    }
                }
            }
        }

        //On renvoie -1 si pas trouvé
        return cellID == startCell ? -1 : cellID;
    }
    
    public static int getRandomcelllignepomax(GameMap map, int startCell,
                                              int endCell, ArrayList<GameCase> forbidens, int distmin)
    {
        int dist = 1000;
        //On prend la cellule autour de la cible, la plus proche
        int cellID = startCell;
        if (forbidens == null)
            forbidens = new ArrayList<>();
        char[] dirs = {'b', 'd', 'f', 'h'};
        for (char d : dirs)
        {
            if(d == 'b')//En Haut à Droite.
            {

                int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                int dis = PathFinding.getDistanceBetween(map, endCell, c);
                int dis2 = PathFinding.getDistanceBetween(map, startCell, c);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }
                boolean ok = false;
                while(ok == false)
                {
                    int h = PathFinding.GetCaseIDFromDirrection(c, d, map, true);
                    if (map.getCase(h) == null)
                        ok = true;
                    dis = PathFinding.getDistanceBetween(map, endCell, c);
                    dis2 = PathFinding.getDistanceBetween(map, startCell, c);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }
                    c = h;
                }

            }
            else if(d == 'f')//En Bas à Gauche.
            {
                int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                int dis = PathFinding.getDistanceBetween(map, endCell, c);
                int dis2 = PathFinding.getDistanceBetween(map, startCell, c);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }
                boolean ok = false;
                while(ok == false)
                {
                    int h = PathFinding.GetCaseIDFromDirrection(c, d, map, true);
                    if (map.getCase(h) == null)
                        ok = true;
                    dis = PathFinding.getDistanceBetween(map, endCell, c);
                    dis2 = PathFinding.getDistanceBetween(map, startCell, c);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }
                    c = h;
                }
            }
            else if(d == 'd')//En Haut à Gauche.
            {
                int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                int dis = PathFinding.getDistanceBetween(map, endCell, c);
                int dis2 = PathFinding.getDistanceBetween(map, startCell, c);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }
                boolean ok = false;
                while(!ok)
                {
                    int h = PathFinding.GetCaseIDFromDirrection(c, d, map, true);
                    if (map.getCase(h) == null)
                        ok = true;
                    dis = PathFinding.getDistanceBetween(map, endCell, c);
                    dis2 = PathFinding.getDistanceBetween(map, startCell, c);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }
                    c = h;
                }
            }
            else if(d == 'h')//En Bas à Droite.
            {
                int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                int dis = PathFinding.getDistanceBetween(map, endCell, c);
                int dis2 = PathFinding.getDistanceBetween(map, startCell, c);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                        && map.getCase(c).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(c)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = c;
                }
                boolean ok = false;
                while(!ok)
                {
                    int h = PathFinding.GetCaseIDFromDirrection(c, d, map, true);
                    if (map.getCase(h) == null)
                        ok = true;
                    dis = PathFinding.getDistanceBetween(map, endCell, c);
                    dis2 = PathFinding.getDistanceBetween(map, startCell, c);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && dis2 <= distmin && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }else if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                            && map.getCase(c).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(c)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = c;
                    }
                    c = h;
                }
            }
        }

        return cellID == startCell ? -1 : cellID;
    }

    public static int getNearestCellDiagGA(GameMap map, int startCell,
                                           int endCell, ArrayList<GameCase> forbidens)
    {
        //On prend la cellule autour de la cible, la plus proche
        int dist = 1000;
        int cellID = startCell;
        if (forbidens == null)
            forbidens = new ArrayList<GameCase>();
        char[] dirs = {'b', 'd', 'f', 'h'};
        GameCase hd = null;
        GameCase bg = null;
        GameCase hg = null;
        GameCase bd = null;
        for (char d : dirs)
        {
            if(d == 'b')//En Haut à Droite.
            {
                int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                hd = map.getCase(c);
            }
            else if(d == 'f')//En Bas à Gauche.
            {
                int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                bg = map.getCase(c);
            }
            else if(d == 'd')//En Haut à Gauche.
            {
                int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                hg = map.getCase(c);
            }
            else if(d == 'h')//En Bas à Droite.
            {
                int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
                if (map.getCase(c) == null)
                    continue;
                bd = map.getCase(c);
            }
        }

        GameCase[] tab = {hd,bg,hg,bd};
        for(GameCase c : tab)
        {
            if(c == null)
                continue;
            if(c == hd)//En Haut à Droite.
            {
                if(hd.getFirstFighter() == null && hd.blockLoS() == true)
                {
                    int p = PathFinding.GetCaseIDFromDirrection(c.getId(), 'b', map, true);
                    if (map.getCase(p) == null)
                        continue;
                    // On cherche la distance entre
                    int dis = PathFinding.getDistanceBetween(map, endCell, p);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && map.getCase(p).isWalkable(true, true, -1)
                            && map.getCase(p).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(p)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = p;
                    }
                }
                int p = PathFinding.GetCaseIDFromDirrection(c.getId(), 'h', map, true);
                if (map.getCase(p) == null)
                    continue;
                // On cherche la distance entre
                int dis = PathFinding.getDistanceBetween(map, endCell, p);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && map.getCase(p).isWalkable(true, true, -1)
                        && map.getCase(p).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(p)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = p;
                }

                int m = PathFinding.GetCaseIDFromDirrection(c.getId(), 'd', map, true);
                if (map.getCase(m) == null)
                    continue;
                // On cherche la distance entre
                dis = PathFinding.getDistanceBetween(map, endCell, m);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && map.getCase(m).isWalkable(true, true, -1)
                        && map.getCase(m).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(m)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = m;
                }
            }
            else if(c == bg)//En Bas à Gauche.
            {
                if(bg.getFirstFighter() == null && bg.blockLoS() == true)
                {
                    int p = PathFinding.GetCaseIDFromDirrection(c.getId(), 'f', map, true);
                    if (map.getCase(p) == null)
                        continue;
                    // On cherche la distance entre
                    int dis = PathFinding.getDistanceBetween(map, endCell, p);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && map.getCase(p).isWalkable(true, true, -1)
                            && map.getCase(p).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(p)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = p;
                    }
                }
                int p = PathFinding.GetCaseIDFromDirrection(c.getId(), 'h', map, true);
                if (map.getCase(p) == null)
                    continue;
                // On cherche la distance entre
                int dis = PathFinding.getDistanceBetween(map, endCell, p);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && map.getCase(p).isWalkable(true, true, -1)
                        && map.getCase(p).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(p)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = p;
                }

                int m = PathFinding.GetCaseIDFromDirrection(c.getId(), 'd', map, true);
                if (map.getCase(m) == null)
                    continue;
                // On cherche la distance entre
                dis = PathFinding.getDistanceBetween(map, endCell, m);
                // Si la distance est strictement inférieur à 1000 et que la case
                // est marchable et que personne ne
                // se trouve dessus et que la case n'est pas interdite
                if (dis < dist && map.getCase(m).isWalkable(true, true, -1)
                        && map.getCase(m).getFirstFighter() == null
                        && !forbidens.contains(map.getCase(m)))
                {
                    // On crée la distance
                    dist = dis;
                    // On modifie la cellule
                    cellID = m;
                }
            }
            else if(c == hg)//En Haut à Gauche.
            {
                if(hg.getFirstFighter() == null && hg.blockLoS() == true)
                {
                    int p = PathFinding.GetCaseIDFromDirrection(c.getId(), 'd', map, true);
                    if (map.getCase(p) == null)
                        continue;
                    // On cherche la distance entre
                    int dis = PathFinding.getDistanceBetween(map, endCell, p);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && map.getCase(p).isWalkable(true, true, -1)
                            && map.getCase(p).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(p)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = p;
                    }
                }
            }
            else if(c == bd)//En Haut à Gauche.
            {
                if(bd.getFirstFighter() == null && bd.blockLoS() == true)
                {
                    int p = PathFinding.GetCaseIDFromDirrection(c.getId(), 'h', map, true);
                    if (map.getCase(p) == null)
                        continue;
                    // On cherche la distance entre
                    int dis = PathFinding.getDistanceBetween(map, endCell, p);
                    // Si la distance est strictement inférieur à 1000 et que la case
                    // est marchable et que personne ne
                    // se trouve dessus et que la case n'est pas interdite
                    if (dis < dist && map.getCase(p).isWalkable(true, true, -1)
                            && map.getCase(p).getFirstFighter() == null
                            && !forbidens.contains(map.getCase(p)))
                    {
                        // On crée la distance
                        dist = dis;
                        // On modifie la cellule
                        cellID = p;
                    }
                }
            }
        }
        return cellID == startCell ? -1 : cellID;
    }

    public static boolean casesAreInSameLine(GameMap map, int c1, int c2, char dir, int max) {
        if (c1 == c2)
            return true;

        if (dir != 'z')//Si la direction est d�finie
        {
            for (int a = 0; a < max; a++) {
                if (GetCaseIDFromDirrection(c1, dir, map, true) == c2)
                    return true;
                if (GetCaseIDFromDirrection(c1, dir, map, true) == -1)
                    break;
                c1 = GetCaseIDFromDirrection(c1, dir, map, true);
            }
        } else
        //Si on doit chercher dans toutes les directions
        {
            char[] dirs = {'b', 'd', 'f', 'h'};
            for (char d : dirs) {
                int c = c1;
                for (int a = 0; a < max; a++) {
                    if (GetCaseIDFromDirrection(c, d, map, true) == c2)
                        return true;
                    c = GetCaseIDFromDirrection(c, d, map, true);
                }
            }
        }
        return false;
    }

    public static ArrayList<GameCase> getAllCasesFromDir(Fight fight, Fighter caster, char dir)
    {
        ArrayList<GameCase> cases = new ArrayList<>();
        if(fight == null | caster == null)
        {
            return null;
        }
        GameMap map = fight.getMap();
        boolean finish = false;
        int cellId = caster.getCell().getId();
        switch(dir)
        {
            case 'b':
                while(!finish)
                {
                    cellId += 15;
                    if(map.getCase(cellId) != null & map.getCase(cellId).isWalkable(false))
                    {
                        cases.add(map.getCase(cellId));
                    }
                    else{
                        finish = true;
                    }
                }
                break;
            case 'd':
                while(!finish)
                {
                    cellId += 14;
                    if(map.getCase(cellId) != null & map.getCase(cellId).isWalkable(false))
                    {
                        cases.add(map.getCase(cellId));
                    }
                    else{
                        finish = true;
                    }
                }
                break;
            case'f':
                while(!finish)
                {
                    cellId -= 15;
                    if(map.getCase(cellId) != null & map.getCase(cellId).isWalkable(false))
                    {
                        cases.add(map.getCase(cellId));
                    }
                    else{
                        finish = true;
                    }
                }
                break;
            case'h':
                while(!finish)
                {
                    cellId -= 14;
                    if(map.getCase(cellId) != null & map.getCase(cellId).isWalkable(false))
                    {
                        cases.add(map.getCase(cellId));
                    }
                    else{
                        finish = true;
                    }
                }
                break;
        }
        return cases;
    }

    public static List<GameCase> getAllCases(Fight fight) {
        GameMap map = fight.getMap();
        List<GameCase> cases = map.getCases();
        return cases;
    }

    public static ArrayList<GameCase> getAllCasesFromDir(Fight fight, int cellId, char dir)
    {
        ArrayList<GameCase> cases = new ArrayList<>();
        GameMap map = fight.getMap();
        boolean finish = false;

        switch(dir)
        {
            case 'b':
                while(!finish)
                {
                    cellId += 15;
                    if(map.getCase(cellId) != null)
                    {
                        cases.add(map.getCase(cellId));
                    }
                    else{
                        finish = true;
                    }
                }
                break;
            case 'd':
                while(!finish)
                {
                    cellId += 14;
                    if(map.getCase(cellId) != null)
                    {
                        cases.add(map.getCase(cellId));
                    }
                    else{
                        finish = true;
                    }
                }
                break;
            case'f':
                while(!finish)
                {
                    cellId -= 15;
                    if(map.getCase(cellId) != null)
                    {
                        cases.add(map.getCase(cellId));
                    }
                    else{
                        finish = true;
                    }
                }
                break;
            case'h':
                while(!finish)
                {
                    cellId -= 14;
                    if(map.getCase(cellId) != null)
                    {
                        cases.add(map.getCase(cellId));
                    }
                    else{
                        finish = true;
                    }
                }
                break;
        }
        return cases;
    }


    public static ArrayList<Fighter> getCiblesByZoneByWeapon(Fight fight,
                                                             int type, GameCase cell, int castCellID) {
        ArrayList<Fighter> cibles = new ArrayList<>();
        char c = getDirBetweenTwoCase(castCellID, cell.getId(), fight.getMap(), true);
        if (c == 0) {
            //On cible quand meme le fighter sur la case
            if (cell.getFirstFighter() != null)
                cibles.add(cell.getFirstFighter());
            return cibles;
        }

        switch (type) {
            //Cases devant celle ou l'on vise
            case Constant.ITEM_TYPE_MARTEAU:
                Fighter f = getFighter2CellBefore(castCellID, c, fight.getMap());
                if (f != null)
                    cibles.add(f);
                Fighter g = get1StFighterOnCellFromDirection(fight.getMap(), castCellID, (char) (c - 1));
                if (g != null)
                    cibles.add(g);//Ajoute case a gauche
                Fighter h = get1StFighterOnCellFromDirection(fight.getMap(), castCellID, (char) (c + 1));
                if (h != null)
                    cibles.add(h);//Ajoute case a droite
                Fighter i = cell.getFirstFighter();
                if (i != null)
                    cibles.add(i);
                break;
            case Constant.ITEM_TYPE_BATON: // TODO : a reprendre marche pas avec les batons distances (RAMBOTON)
                int dist = PathFinding.getDistanceBetween(fight.getMap(), cell.getId(), castCellID);
                int newCell = PathFinding.getCaseIDFromDirrection(castCellID, c, fight.getMap());

                Fighter j = get1StFighterOnCellFromDirection(fight.getMap(), (dist > 1 ? newCell : castCellID), (char) (c - 1));
                if (j != null)
                    cibles.add(j);//Ajoute case a gauche
                Fighter k = get1StFighterOnCellFromDirection(fight.getMap(), (dist > 1 ? newCell : castCellID), (char) (c + 1));
                if (k != null)
                    cibles.add(k);//Ajoute case a droite
                Fighter l = cell.getFirstFighter();
                if (l != null)
                    cibles.add(l);//Ajoute case cible
                break;
            case Constant.ITEM_TYPE_PIOCHE:
            case Constant.ITEM_TYPE_EPEE:
            case Constant.ITEM_TYPE_FAUX:
            case Constant.ITEM_TYPE_DAGUES:
            case Constant.ITEM_TYPE_BAGUETTE:
            case Constant.ITEM_TYPE_PELLE:
            case Constant.ITEM_TYPE_ARC:
            case Constant.ITEM_TYPE_HACHE:
            case Constant.ITEM_TYPE_OUTIL:
                Fighter m = cell.getFirstFighter();
                if (m != null)
                    cibles.add(m);
                break;
        }
        return cibles;
    }

    private static Fighter get1StFighterOnCellFromDirection(GameMap map, int id,
                                                            char c) {
        if (c == (char) ('a' - 1))
            c = 'h';
        if (c == (char) ('h' + 1))
            c = 'a';
        return map.getCase(GetCaseIDFromDirrection(id, c, map, false)).getFirstFighter();
    }

    private static Fighter getFighter2CellBefore(int CellID, char c, GameMap map) {
        int new2CellID = GetCaseIDFromDirrection(GetCaseIDFromDirrection(CellID, c, map, false), c, map, false);
        return map.getCase(new2CellID).getFirstFighter();
    }

    public static char getDirBetweenTwoCase(int cell1ID, int cell2ID, GameMap map,
                                            boolean Combat) {
        ArrayList<Character> dirs = new ArrayList<Character>();
        dirs.add('b');
        dirs.add('d');
        dirs.add('f');
        dirs.add('h');
        if (!Combat) {
            dirs.add('a');
            dirs.add('b');
            dirs.add('c');
            dirs.add('d');
        }
        for (char c : dirs) {
            int cell = cell1ID;
            for (int i = 0; i <= 64; i++) {
                if (GetCaseIDFromDirrection(cell, c, map, Combat) == cell2ID)
                    return c;
                cell = GetCaseIDFromDirrection(cell, c, map, Combat);
            }
        }
        return 0;
    }
    public static byte[] getCoordByDir(final char c) {
        byte[][] f = { { 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 }, { -1, -1 }, { 0, -1 } };
        byte i = 0;
        char[] allDirs = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' };
        for (char a : allDirs) {
            if (c == a)
                return f[i];
            i++;
        }
        return null;
    }

    private static ArrayList<Short> casesByLines(final GameCase caseI, final GameMap mapI, final int distance, char dir) {
        ArrayList<Short> allCases = new ArrayList<Short>();
        if (dir == 0) {
            return allCases;
        }
        int x = getCellXCoord(mapI,caseI.getId()),
                y = getCellYCoord(mapI,caseI.getId());

        byte[] b = getCoordByDir(dir);
        for (int x2 = distance; x2 >= 0; x2--) {
            int CellId = getCaseByPos(mapI,(x + (b[0] * x2)), (y + (b[1] * x2)));
            GameCase cell = mapI.getCase(CellId);
            if (cell != null) {
                if (!allCases.contains(cell.getId()))
                    allCases.add((short)cell.getId());
            }
        }
        return allCases;
    }

    /**
     * One step in a direction, or {@code -1} when the step leaves the board.
     *
     * <p>{@link #GetCaseIDFromDirrection} only adds an offset to the identifier.
     * At the end of a row that offset names a perfectly live cell on the far
     * side of the map, so a cross or a line cast near an edge used to strike
     * cells nowhere near it. {@code Function.testCotes} guards against exactly
     * this, but is only ever called from the movement code.
     *
     * <p>Checking the coordinates costs two subtractions and closes the hole.
     */
    public static int stepOrOffBoard(int cellID, char direction, GameMap map) {
        int next = GetCaseIDFromDirrection(cellID, direction, map, true);
        if (next < 0 || map.getCase(next) == null) {
            return -1;
        }
        int dx = Math.abs(getCellXCoord(map, next) - getCellXCoord(map, cellID));
        int dy = Math.abs(getCellYCoord(map, next) - getCellYCoord(map, cellID));
        return (dx + dy == 1) ? next : -1;
    }

    public static ArrayList<GameCase> getCellListFromAreaString(GameMap map,
                                                            int cellID, int castCellID, String zoneStr) {
        ArrayList<GameCase> cases = new ArrayList<GameCase>();

        if (map.getCase(cellID) == null)
            return cases;

        int taille = World.world.getCryptManager().getIntByHashedValue(zoneStr.charAt(1));
        switch (zoneStr.charAt(0)) {
            case 'C':// Cercle
                int i= 0;
                while ( i <= taille) {
                    cases.addAll(drawCircleBorder(i, cellID, map));
                    i += 1;
                }
                break;
            case 'X':// Croix
                cases.add(map.getCase(cellID));
                char[] dirs = {'b', 'd', 'f', 'h'};
                for (char d : dirs) {
                    int cID = cellID;
                    for (int a = 0; a < taille; a++) {
                        int next = stepOrOffBoard(cID, d, map);
                        if (next == -1)
                            break;
                        cases.add(map.getCase(next));
                        cID = next;
                    }
                }
                break;
            case 'R': //Supposed to be Rectangle, is broken
            {
                List<GameCase> casesID =  drawRectangleBorder(taille,taille,cellID,map);

                char[] dirs3= { 'h', 'd' };
                for(char d : dirs3)
                {
                    int cID=cellID;
                    for(int a=0;a<taille;a++)
                    {
                        if(!cases.contains(map.getCase(GetCaseIDFromDirrection(cID,d,map,true))))
                            cases.add(map.getCase(GetCaseIDFromDirrection(cID,d,map,true)));
                        cID=GetCaseIDFromDirrection(cID,d,map,true);
                    }
                }

                char[] dirss= { 'b', 'f' };
                for(char d : dirss)
                {
                    int cID=cellID;
                    for(int a=0;a<taille;a++)
                    {
                        if(!cases.contains(map.getCase(GetCaseIDFromDirrection(cID,d,map,true))))
                            cases.add(map.getCase(GetCaseIDFromDirrection(cID,d,map,true)));
                        int tempcID=map.getCase(GetCaseIDFromDirrection(cID,d,map,true)).getId();
                        cID=map.getCase(GetCaseIDFromDirrection(cID,d,map,true)).getId();
                        char[] dirs2= { 'h', 'd' };
                        for(char ch : dirs2)
                        {
                            int cID2=cID;
                            for(int a2=0;a2<taille;a2++)
                            {
                                if(!cases.contains(map.getCase(GetCaseIDFromDirrection(cID2,d,map,true))))
                                    cases.add(map.getCase(GetCaseIDFromDirrection(cID2,ch,map,true)));
                                cID2=map.getCase(GetCaseIDFromDirrection(cID2,ch,map,true)).getId();
                            }
                        }
                        cID=tempcID;
                    }
                }
                break;
            }
            case 'D':// Damier
                int pair = taille % 2 == 0 ? 0 : 1;
                while (pair < taille) {
                    cases.addAll(drawCircleBorder(pair, cellID, map));
                    pair += 2;
                }
                break;
            case 'O':
                cases.add(map.getCase(cellID));
                for (final short celda2 : casesByDistances(map.getCase(cellID), map, taille)) {
                    GameCase celda = map.getCase(celda2);
                    if (!cases.contains(celda)) {
                        cases.add(celda);
                    }
                }
                break;
            case 'A':// Croix
                cases.add(map.getCase(cellID));
                for (int a = taille; a >= 0; a--) {
                    for (final int caseII : casesByCroix(map.getCase(castCellID), map, a)) {
                        GameCase caseI = map.getCase(caseII);
                        if (!cases.contains(caseI)) {
                            cases.add(caseI);
                        }
                    }
                }
                break;
            case 'T':
                cases.add(map.getCase(cellID));
                final char dir2 = getDirBetweenTwoCase( (short)castCellID,(short)cellID,map, true);
                for (final short caseII : casesByLines(map.getCase(cellID), map, taille,
                        correctDir((char) (dir2 - 2)))) {
                    GameCase caseI = map.getCase(caseII);
                    if (!cases.contains(caseI)) {
                        cases.add(caseI);
                    }
                }
                for (final short celda2 : casesByLines(map.getCase(cellID), map, taille,
                        correctDir((char) (dir2 + 2)))) {
                    GameCase caseI = map.getCase(celda2);
                    if (!cases.contains(caseI)) {
                        cases.add(caseI);
                    }
                }
                if (!cases.contains(map.getCase(cellID))) {
                    cases.add(map.getCase(cellID));
                }
                break;
            case 'L':// Ligne
                cases.add(map.getCase(cellID));
                char dir = PathFinding.getDirBetweenTwoCase(castCellID, cellID, map, true);
                for (int a = 0; a < taille; a++) {
                    int next = stepOrOffBoard(cellID, dir, map);
                    if (next == -1)
                        break;
                    cases.add(map.getCase(next));
                    cellID = next;
                }
                break;

            case 'P':// Pointer
                cases.add(map.getCase(cellID));
                break;

            default:
                cases.add(map.getCase(cellID));
                System.out.println("Zone d'effet Inconnu ! " + zoneStr);
                GameServer.a("Zone d'effet inconnue " + zoneStr.charAt(0) + " - " + map.getId());
                break;
        }

        /*for(GameCase cells : cases){
            SocketManager.send("Arwase","Gf901|"+cells.getId());
        }*/
        return cases;
    }

    private static char correctDir(char dir) {
        switch (dir) {
            case 'h' - 8:
                return 'h';
            case 'g' - 8:
                return 'g';
            case 'f' - 8:
                return 'f';
            case 'e' - 8:
                return 'e';
            case 'd' - 8:
                return 'd';
            case 'c' - 8:
                return 'c';
            case 'b' - 8:
                return 'b';
            case 'a' - 8:
                return 'a';
            case 'h' + 8:
                return 'h';
            case 'g' + 8:
                return 'g';
            case 'f' + 8:
                return 'f';
            case 'e' + 8:
                return 'e';
            case 'd' + 8:
                return 'd';
            case 'c' + 8:
                return 'c';
            case 'b' + 8:
                return 'b';
            case 'a' + 8:
                return 'a';
        }
        return dir;
    }

    private static ArrayList<Integer> casesByCroix(final GameCase caseI, final GameMap mapI, final int disatnce) {
        ArrayList<Integer> allCases = new ArrayList<Integer>();
        int x = caseI.getCoordX(), y = caseI.getCoordY();
        byte[][] coordArround = { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 } };
        for (byte[] b : coordArround) {
            int CellId = getCaseByPos(mapI,(x + (b[0] * disatnce)), (y + (b[1] * disatnce)));
            GameCase cell = mapI.getCase(CellId);
            if (cell != null) {
                if (!allCases.contains(cell.getId()))
                    allCases.add(cell.getId());
            }
        }
        return allCases;
    }


    public static ArrayList<Short> casesByDistances(final GameCase caseI, final GameMap mapI, final int distance) {
        ArrayList<Short> allCases = new ArrayList<Short>();

        GameCase.Coord coord = getCaseCoordonnee(mapI,caseI.getId());
        int x = coord.x,y = coord.y;

        byte[][] f = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
        for (int x2 = 0; x2 <= distance; x2++) {
            int y2 = distance - x2;
            for (byte[] b : f) {
                int CellId = getCaseByPos(mapI,(x + (b[0] * x2)), (y + (b[1] * y2)));
                GameCase cell = mapI.getCase(CellId);
                if (cell != null) {

                    if (!allCases.contains(cell.getId()))
                        allCases.add((short)cell.getId());
                }

            }
        }
        return allCases;
    }

    public static int getCellXCoord(GameMap map, int cellID) {
        if (map == null)
            return 0;
        int w = map.getW();
        return ((cellID - (w - 1) * getCellYCoord(map, cellID)) / w);
    }

    public static int getCellYCoord(GameMap map, int cellID) {
        int w = map.getW();
        int loc5 = cellID / ((w * 2) - 1);
        int loc6 = cellID - loc5 * ((w * 2) - 1);
        int loc7 = loc6 % w;
        return (loc5 - loc7);
    }

    public static int getNearestCellAround(GameMap map, int startCell, int endCell,
                                           ArrayList<GameCase> forbidens) {
        if (map == null)
            return -1;
        // On prend la cellule autour de la cible, la plus proche
        int dist = 1000;
        int cellID = startCell;
        if (forbidens == null)
            forbidens = new ArrayList<GameCase>();
        char[] dirs = {'b', 'd', 'f', 'h'};
        for (char d : dirs) {

            // On cherche la celluleID correspondant ï¿½ la direction associï¿½
            int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
            if (map.getCase(c) == null)
                continue;
            // On cherche la distance entre
            int dis = PathFinding.getDistanceBetween(map, endCell, c);
            // Si la distance est strictement infï¿½rieur ï¿½ 1000 et que la case
            // est marchable et que personne ne
            // se trouve dessus et que la case n'est pas interdite
            if (dis < dist && map.getCase(c).isWalkable(true, true, -1)
                    && map.getCase(c).getFirstFighter() == null
                    && !forbidens.contains(map.getCase(c))) {
                // On crï¿½e la distance
                dist = dis;
                // On modifie la cellule
                cellID = c;
            }
        }
        // On renvoie -1 si pas trouvï¿½
        return cellID == startCell ? -1 : cellID;
    }

    public static int getNearestCellAroundGA(GameMap map, int startCell,
                                             int endCell, ArrayList<GameCase> forbidens) {
        //On prend la cellule autour de la cible, la plus proche
        int dist = 1000;
        int cellID = startCell;
        if (forbidens == null)
            forbidens = new ArrayList<GameCase>();
        char[] dirs = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        for (char d : dirs) {
            int c = PathFinding.GetCaseIDFromDirrection(startCell, d, map, true);
            int dis = PathFinding.getDistanceBetween(map, endCell, c);
            if (map.getCase(c) == null)
                continue;
            if (dis < dist && map.getCase(c).isWalkable(true)
                    && map.getCase(c).getFirstFighter() == null
                    && !forbidens.contains(map.getCase(c))) {
                dist = dis;
                cellID = c;
            }
        }

        //On renvoie -1 si pas trouv�
        return cellID == startCell ? -1 : cellID;
    }

    public static ArrayList<GameCase> getShortestPathBetween(GameMap map, int start,
                                                         int dest, int distMax) {
        ArrayList<GameCase> curPath = new ArrayList<GameCase>();
        ArrayList<GameCase> curPath2 = new ArrayList<GameCase>();
        ArrayList<GameCase> closeCells = new ArrayList<GameCase>();
        int limit = 1000;
        //int oldCaseID = start;
        GameCase curCase = map.getCase(start);
        int stepNum = 0;
        boolean stop = false;


        while (!stop && stepNum++ <= limit) {
            int nearestCell = getNearestCellAround(map, curCase.getId(), dest, closeCells);
            if (nearestCell == -1) {
                closeCells.add(curCase);
                if (curPath.size() > 0) {
                    curPath.remove(curPath.size() - 1);
                    if (curPath.size() > 0)
                        curCase = curPath.get(curPath.size() - 1);
                    else
                        curCase = map.getCase(start);
                } else {
                    curCase = map.getCase(start);
                }
            } else if (distMax == 0 && nearestCell == dest) {
                curPath.add(map.getCase(dest));
                break;
            } else if (distMax > PathFinding.getDistanceBetween(map, nearestCell, dest)) {
                curPath.add(map.getCase(dest));
                break;
            } else
            //on continue
            {
                curCase = map.getCase(nearestCell);
                closeCells.add(curCase);
                curPath.add(curCase);
            }
        }

        curCase = map.getCase(start);
        closeCells.clear();
        if (!curPath.isEmpty()) {
            closeCells.add(curPath.get(0));
        }

        while (!stop && stepNum++ <= limit) {
            int nearestCell = getNearestCellAround(map, curCase.getId(), dest, closeCells);
            if (nearestCell == -1) {
                closeCells.add(curCase);
                if (curPath2.size() > 0) {
                    curPath2.remove(curPath2.size() - 1);
                    if (curPath2.size() > 0)
                        curCase = curPath2.get(curPath2.size() - 1);
                    else
                        curCase = map.getCase(start);
                } else
                //Si retour a zero
                {
                    curCase = map.getCase(start);
                }
            } else if (distMax == 0 && nearestCell == dest) {
                curPath2.add(map.getCase(dest));
                break;
            } else if (distMax > PathFinding.getDistanceBetween(map, nearestCell, dest)) {
                curPath2.add(map.getCase(dest));
                break;
            } else
            //on continue
            {
                curCase = map.getCase(nearestCell);
                closeCells.add(curCase);
                curPath2.add(curCase);
            }
        }

        if ((curPath2.size() < curPath.size() && curPath2.size() > 0)
                || curPath.isEmpty())
            curPath = curPath2;
        return curPath;
    }

    public static String getShortestStringPathBetween(GameMap map, int start,
                                                      int dest, int distMax) {
        if (start == dest)
            return null;
        ArrayList<GameCase> path = getShortestPathBetween(map, start, dest, distMax);
        if (path == null)
            return null;
        String pathstr = "";
        int curCaseID = start;
        char curDir = '\000';
        for (GameCase c : path) {
            char d = getDirBetweenTwoCase(curCaseID, c.getId(), map, true);
            if (d == 0)
                return null;
            if (curDir != d) {
                if (path.indexOf(c) != 0)
                    pathstr = pathstr + World.world.getCryptManager().cellID_To_Code(curCaseID);
                pathstr = pathstr + d;
                curDir = d;
            }
            curCaseID = c.getId();
        }
        if (curCaseID != start) {
            pathstr = pathstr + World.world.getCryptManager().cellID_To_Code(curCaseID);
        }
        if (pathstr.isEmpty())
            return null;
        return "a" + World.world.getCryptManager().cellID_To_Code(start) + pathstr;
    }

    public static boolean isBord1(int id) {
        int[] bords = {1, 30, 59, 88, 117, 146, 175, 204, 233, 262, 291, 320, 349, 378, 407, 436, 465, 15, 44, 73, 102, 131, 160, 189, 218, 247, 276, 305, 334, 363, 392, 421, 450, 479};
        ArrayList<Integer> test = new ArrayList<Integer>();
        for (int i : bords) {
            test.add(i);
        }

        return test.contains(id);
    }

    public static boolean isBord2(int id) {
        int[] bords = {16, 45, 74, 103, 132, 161, 190, 219, 248, 277, 306, 335, 364, 393, 422, 451, 29, 58, 87, 116, 145, 174, 203, 232, 261, 290, 319, 348, 377, 406, 435, 464};
        ArrayList<Integer> test = new ArrayList<Integer>();
        for (int i : bords) {
            test.add(i);
        }

        return test.contains(id);
    }

    private ArrayList<Character> listaDirEntreDosCeldas(GameMap mapa, int celdaInicio, int celdaDestino) {
        if (celdaInicio == celdaDestino || mapa == null) {
            return null;
        }
        ArrayList abc = new ArrayList<Character>(4);
        ArrayList<Byte> b = listaDirEntreDosCeldas2(mapa, celdaInicio, celdaDestino, (-1));
        for (int i = 0; i < 4; i++) {
            switch (b.get(i)) {
                case 0 :
                    abc.add(i, 'b');
                    break;
                case 1 :
                    abc.add(i,'d');
                    break;
                case 2 :
                    abc.add(i,'f');
                    break;
                case 3 :
                    abc.add(i,'h');
                    break;
            }
        }
        return abc;
    }

    private ArrayList<Byte> listaDirEntreDosCeldas2( GameMap mapa, int celdaInicio, int celdaDestino,int celdaAnterior ) {
        if (celdaInicio == celdaDestino || mapa == null) {
            return null;
        }
        GameCase cInicio = mapa.getCase(celdaInicio);
        GameCase cDestino = mapa.getCase(celdaDestino);
        int difX = cDestino.getCoordX() - cInicio.getCoordX();
        int difY = cDestino.getCoordY() - cInicio.getCoordY();
        if (Math.abs(difY) == Math.abs(difX) && celdaAnterior > 0) {
            return listaDirEntreDosCeldas2(mapa, celdaAnterior, celdaDestino, -1);
        } else if (Math.abs(difY) > Math.abs(difX)) {
            Integer[] c1 = new Integer[]{difX, 0, 2};
            Integer[] c2 = new Integer[]{difY, 1, 3};
            ArrayList<Integer[]> c = new ArrayList<Integer[]>();
            c.add(c1);
            c.add(c2);
           return formulaDireccion(c);
        } else {
            Integer[] c1 = new Integer[]{difY, 1, 3};
            Integer[] c2 = new Integer[]{difX, 0, 2};
            ArrayList<Integer[]> c = new ArrayList<Integer[]>();
            c.add(c1);
            c.add(c2);
            return formulaDireccion(c);
        }
    }

    private ArrayList<Byte> formulaDireccion(ArrayList<Integer[]> c) {
        ArrayList<Byte> abc = new ArrayList<Byte>(4);
        for (int i = 0; i < 2; i++) {
            Integer dif = c.get(i)[0];
            int p = i;
            if (dif < 0) {
                p = Math.abs(3 - i);
            }
            abc.add(p, Byte.parseByte(c.get(i)[1].toString()));
            abc.add(Math.abs(3 - p), Byte.parseByte(c.get(i)[2].toString()));
        }
        return abc;
    }

    public static int getXFromCellID(int cellID, int mapWidth) {
        return cellID % mapWidth;
    }

    public static int getYFromCellID(int cellID, int mapWidth) {
        return (cellID - getXFromCellID(cellID, mapWidth)) / mapWidth;
    }

    public static int getCellIDFromXY(int x, int y, int mapWidth) {
        return y * mapWidth + x;
    }

    public static boolean checkView(GameMap mapHandler, int cellId1, int cellId2) {
        GameCase.Coord startCoord = getCaseCoordonnee(mapHandler, cellId1);
        GameCase.Coord endCoord = getCaseCoordonnee(mapHandler, cellId2);
        GameCase startCellData = mapHandler.getCase(cellId1);
        GameCase endCellData = mapHandler.getCase(cellId2);
        /*if(startCellData.getId() == endCellData.getId()){
            return true;
        }*/

        double startHeightModifier = (startCellData.isLoS()) ? 1.5 : 0;
        double endHeightModifier = (endCellData.isLoS()) ? 1.5 : 0;
        startHeightModifier += startCellData.blockLoS() ? 1.5 : 0;
        endHeightModifier += endCellData.blockLoS() ? 1.5 : 0;
        startCoord.z = (int) (startCellData.getHeight()  + startHeightModifier);
        endCoord.z = (int) (endCellData.getHeight() + endHeightModifier);

        double heightDifference = endCoord.z - startCoord.z;
        int maxDistance = Math.max(Math.abs(startCoord.y - endCoord.y), Math.abs(startCoord.x - endCoord.x));
        double slope = (double) (startCoord.y - endCoord.y) / (startCoord.x - endCoord.x);
        double intercept = startCoord.y - slope * startCoord.x;
        int xDirection = (endCoord.x - startCoord.x >= 0) ? 1 : -1;
        int yDirection = (endCoord.y - startCoord.y >= 0) ? 1 : -1;
        double currentY = startCoord.y;
        double currentX = startCoord.x;
        int endX = endCoord.x * xDirection;
        int endY = endCoord.y * yDirection;
        double currentXWithOffset = startCoord.x + 0.5 * xDirection;

        while (currentXWithOffset * xDirection <= endX) {
            double currentYOnSlope = slope * currentXWithOffset + intercept;
            double roundUpY;
            double roundDownY;
            if (yDirection > 0) {
                roundUpY = Math.round(currentYOnSlope);
                roundDownY = Math.ceil(currentYOnSlope - 0.5);
            } else {
                roundUpY = Math.ceil(currentYOnSlope - 0.5);
                roundDownY = Math.round(currentYOnSlope);
            }
            double currentYToCheck = currentY;

            while (currentYToCheck * yDirection <= roundDownY * yDirection) {

                if (!checkCellView(mapHandler, currentXWithOffset - ((double)xDirection / 2.0), currentYToCheck, false, startCoord, endCoord, heightDifference, maxDistance)) {
                    return false;
                }
                currentYToCheck += yDirection;
            }

            currentY = roundUpY;
            currentXWithOffset += xDirection;
        }

        double currentYToCheck = currentY;
        while (currentYToCheck * yDirection <= endCoord.y * yDirection) {
            if (!checkCellView(mapHandler, currentXWithOffset - (0.5 * (double)xDirection), currentYToCheck, false, startCoord, endCoord, heightDifference, maxDistance)) {
                return false;
            }
            currentYToCheck += yDirection;
        }

        if (!checkCellView(mapHandler, currentXWithOffset - (0.5 * (double)xDirection), (currentYToCheck - (double)yDirection), true, startCoord, endCoord, heightDifference, maxDistance)) {
            return false;
        }

        return true;
    }

    public static int getCaseByPos(GameMap mapHandler, int x, int y) {
        int width = mapHandler.getW();
        return x * width + y * (width - 1);
    }


    public static boolean checkCellView(GameMap mapHandler, double x, double y, boolean hasSpriteOnId, GameCase.Coord startCoord, GameCase.Coord endCoord, double heightDifference, int maxDistance) {
        int cellNum = getCaseByPos(mapHandler, (int)x, (int)y);
        GameCase cellData = mapHandler.getCase(cellNum);
        int maxCoordDifference = Math.max(Math.abs(startCoord.y - (int) y), Math.abs(startCoord.x - (int) x));
        double coordRatio = 0;
        try {
            if(maxDistance != 0 && heightDifference != 0 ) {
                coordRatio = (maxCoordDifference / (double) maxDistance * heightDifference) + startCoord.z;
            }
            else
                coordRatio = startCoord.z;
        }
        catch (Exception ignored){
            ignored.printStackTrace();
            coordRatio =0;
        }
        double cellHeight = cellData.getHeight();

        boolean isBlocked = !(cellData.isEmpty() == null || (maxCoordDifference == 0 || (hasSpriteOnId || endCoord.x == (int) x && endCoord.y == (int) y)));
        if (cellData.isLoS() && cellData.isActivate() && (cellHeight <= coordRatio && !isBlocked) ) {
            return true;
        }

        if (hasSpriteOnId) {
            return true;
        }

        return false;
    }


    public static boolean checkLoS(GameMap map, int cell1, int cell2,
                                   Fighter fighter, boolean isPeur) {

        if (fighter == null) // on ne rev�rifie pas (en plus du client) pour les joueurs
            return false;

        /*
        if (fighter.getPlayer() != null) // on ne rev�rifie pas (en plus du client) pour les joueurs
            return true;
            */

        ArrayList<Integer> CellsToConsider = new ArrayList<Integer>();
        CellsToConsider = getLoSBotheringIDCases(map, cell1, cell2, true);
        if (CellsToConsider == null) {
            return true;
        }

        for (Integer cellID : CellsToConsider) {
            if (map.getCase(cellID) != null)
                if (!map.getCase(cellID).blockLoS()
                        || (!map.getCase(cellID).isWalkable(false) && isPeur)) {
                    return false;
                }
        }
        return true;
    }

    public static boolean checkLoSBetween2Cells(GameMap map, int cell1, int cell2) {
        return checkView(map, cell1, cell2);
    }

    private static ArrayList<Integer> getLoSBotheringIDCases(GameMap map,
                                                             int cellID1, int cellID2, boolean Combat) {
        ArrayList<Integer> toReturn = new ArrayList<Integer>();
        int consideredCell1 = cellID1;
        int consideredCell2 = cellID2;
        char dir = 'b';
        int diffX = 0;
        int diffY = 0;
        int compteur = 0;
        ArrayList<Character> dirs = new ArrayList<Character>();
        dirs.add('b');
        dirs.add('d');
        dirs.add('f');
        dirs.add('h');

        while (getDistanceBetween(map, consideredCell1, consideredCell2) > 2
                && compteur < 600) {
            diffX = getCellXCoord(map, consideredCell1)
                    - getCellXCoord(map, consideredCell2);
            diffY = getCellYCoord(map, consideredCell1)
                    - getCellYCoord(map, consideredCell2);
            if (Math.abs(diffX) > Math.abs(diffY)) { // si il ya une plus grande diff�rence pour la premi�re coordonn�e
                if (diffX > 0)
                    dir = 'f';
                else
                    dir = 'b';
                consideredCell1 = GetCaseIDFromDirrection(consideredCell1, dir, map, Combat); // on avance le chemin d'obstacles possibles
                consideredCell2 = GetCaseIDFromDirrection(consideredCell2, getOpositeDirection(dir), map, Combat); // des deux c�t�s
                toReturn.add(consideredCell1); // la liste des cases potentiellement obstacles
                toReturn.add(consideredCell2); // la liste des cases potentiellement obstacles
            } else if (Math.abs(diffX) < Math.abs(diffY)) { // si il y a une plus grand diff�rence pour la seconde
                if (diffY > 0) // d�termine dans quel sens
                    dir = 'h';
                else
                    dir = 'd';
                consideredCell1 = GetCaseIDFromDirrection(consideredCell1, dir, map, Combat); // on avance le chemin d'obstacles possibles
                consideredCell2 = GetCaseIDFromDirrection(consideredCell2, getOpositeDirection(dir), map, Combat); // des deux c�t�s
                toReturn.add(consideredCell1); // la liste des cases potentiellement obstacles
                toReturn.add(consideredCell2); // la liste des cases potentiellement obstacles
            } else {
                if (compteur == 0) { // si on est en diagonale parfaite
                    return getLoSBotheringCasesInDiagonal(map, cellID1, cellID2, diffX, diffY);
                }
                if (dir == 'f' || dir == 'b') // on change la direction dans le cas o� on se retrouve en diagonale
                    if (diffY > 0)
                        dir = 'h';
                    else
                        dir = 'd';
                else if (dir == 'h' || dir == 'd')
                    if (diffX > 0)
                        dir = 'f';
                    else
                        dir = 'b';
                consideredCell1 = GetCaseIDFromDirrection(consideredCell1, dir, map, Combat); // on avance le chemin d'obstacles possibles
                consideredCell2 = GetCaseIDFromDirrection(consideredCell2, getOpositeDirection(dir), map, Combat); // des deux c�t�s
                toReturn.add(consideredCell1); // la liste des cases potentiellement obstacles
                toReturn.add(consideredCell2); // la liste des cases potentiellement obstacles
            }
            compteur++;
        }
        if (getDistanceBetween(map, consideredCell1, consideredCell2) == 2) {
            dir = 0;
            diffX = getCellXCoord(map, consideredCell1)
                    - getCellXCoord(map, consideredCell2);
            diffY = getCellYCoord(map, consideredCell1)
                    - getCellYCoord(map, consideredCell2);
            if (diffX == 0)
                if (diffY > 0)
                    dir = 'h';
                else
                    dir = 'd';
            if (diffY == 0)
                if (diffX > 0)
                    dir = 'f';
                else
                    dir = 'b';
            if (dir != 0)
                toReturn.add(GetCaseIDFromDirrection(consideredCell1, dir, map, Combat));
        }
        return toReturn;
    }


    private static ArrayList<Integer> getLoSBotheringCasesInDiagonal(GameMap map,
                                                                     int cellID1, int cellID2, int diffX, int diffY) {

        ArrayList<Integer> toReturn = new ArrayList<Integer>();
        char dir = 'a';
        if (diffX > 0 && diffY > 0)
            dir = 'g';
        if (diffX > 0 && diffY < 0)
            dir = 'e';
        if (diffX < 0 && diffY > 0)
            dir = 'a';
        if (diffX < 0 && diffY < 0)
            dir = 'c';

        int consideredCell = cellID1, compteur = 0;
        while (consideredCell != -1 && compteur < 300) {
            consideredCell = GetCaseIDFromDirrection(consideredCell, dir, map, false);

            if (consideredCell == cellID2)
                return toReturn;
            toReturn.add(consideredCell);
            compteur++;
        }
        return toReturn;
    }

    public static ArrayList<Fighter> getFightersAround(int cellID, GameMap map,
                                                       Fight fight) {
        char[] dirs = {'b', 'd', 'f', 'h'};
        ArrayList<Fighter> fighters = new ArrayList<>();

        for (char dir : dirs) {
            GameCase gameCase = map.getCase(GetCaseIDFromDirrection(cellID, dir, map, false));
            if(gameCase == null) continue;
            Fighter f = gameCase.getFirstFighter();
            if (f != null)
                fighters.add(f);
        }
        return fighters;
    }

    public static char getDirBetweenCells(GameMap map, int id1, int id2) {
        if (id1 == id2)
            return 0;
        if (map == null)
            return 0;
        int difX = (getCellXCoord(map, id1) - getCellXCoord(map, id2));
        int difY = (getCellYCoord(map, id1) - getCellYCoord(map, id2));
        int difXabs = Math.abs(difX);
        int difYabs = Math.abs(difY);
        if (difXabs > difYabs) {
            if (difX > 0)
                return 'f';
            else
                return 'b';
        } else {
            if (difY > 0)
                return 'h';
            else
                return 'd';
        }
    }

    public static int getCellArroundByDir(int cellId, char dir,
                                          GameMap map) {
        if (map == null)
            return -1;

        switch (dir) {
            case 'b':
                return cellId + map.getW();//En Haut � Droite.
            case 'd':
                return cellId + (map.getW() - 1);//En Haut � Gauche.
            case 'f':
                return cellId - map.getW();//En Bas � Gauche.
            case 'h':
                return cellId - map.getW() + 1;//En Bas � Droite.
        }
        return -1;
    }

    public static GameCase checkIfCanPushEntity(Fight fight, int startCell,
                                            int endCell, char direction) {
        GameMap map = fight.getMap();
        GameCase cell = map.getCase(getCellArroundByDir(startCell, direction, map));
        GameCase oldCell = cell;
        GameCase actualCell = cell;

        while (actualCell.getId() != endCell) {
            actualCell = map.getCase(getCellArroundByDir(actualCell.getId(), direction, map));
            if (!actualCell.getFighters().isEmpty()
                    || !actualCell.isWalkable(true))
                return oldCell;

            for (Trap trap : fight.getAllTraps()) {

                if (PathFinding.getDistanceBetween(fight.getMap(), trap.getCell().getId(), actualCell.getId()) <= trap.getSize())
                    return actualCell;
            }

            oldCell = actualCell;
        }

        return null;
    }

    public static boolean haveFighterOnThisCell(int cell, Fight fight) {
        for (Fighter f : fight.getFighters(3)) {
            if (f.getCell().getId() == cell && !f.isDead())
                return true;
        }
        return false;
    }

    public static int getCaseIDFromDirrection(int CaseID, char Direccion,
                                              GameMap map) {
        switch (Direccion) {// mag.get_w() = te da el ancho del mapa
            case 'b':
                return CaseID + map.getW(); // diagonale droite vers le bas
            case 'd':
                return CaseID + (map.getW() - 1); // diagonale gauche vers le bas
            case 'f':
                return CaseID - map.getW(); // diagonale gauche vers le haut
            case 'h':
                return CaseID - map.getW() + 1;// diagonale droite vers le haut
        }
        return -1;
    }

    public static boolean cellArroundCaseIDisOccuped(Fight fight, int cell) {
        char[] dirs = {'b', 'd', 'f', 'h'};
        ArrayList<Integer> Cases = new ArrayList<Integer>();

        for (char dir : dirs) {
            int caseID = PathFinding.GetCaseIDFromDirrection(cell, dir, fight.getMap(), true);
            Cases.add(caseID);
        }
        int ha = 0;
        for (int o = 0; o < Cases.size(); o++) {
            if (fight.getMap().getCase(Cases.get(o)).getFirstFighter() != null)
                ha++;
        }
        return ha != 4;

    }




}
