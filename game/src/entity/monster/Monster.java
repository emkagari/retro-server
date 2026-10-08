package entity.monster;

import area.map.GameCase;
import area.map.GameMap;
import client.other.Stats;
import common.Formulas;
import database.Database;
import entity.monster.boss.MaitreCorbac;
import fight.Fighter;
import fight.spells.*;
import game.world.World;
import game.world.World.Drop;
import kernel.Config;
import kernel.Constant;
import object.GameObject;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class Monster {
    private int id;
    private String name;
    private int gfxId;
    private int align;
    private String colors;
    private int ia = 27;
    private int minKamas;
    private int maxKamas;
    private Map<Integer, MobGrade> grades = new HashMap<>();
    private ArrayList<Drop> drops = new ArrayList<>();
    private boolean isCapturable;
    private int aggroDistance = 0;
    private int type;

    public Monster(int id,String name, int gfxId, int align, String colors,
                   String thisGrades, String thisSpells, String thisStats,
                   String thisStatsInfos, String thisPdvs, String thisPoints,
                   String thisInit, int minKamas, int maxKamas, String thisXp, int ia,
                   boolean capturable, int aggroDistance, int type) {
        this.id = id;
        this.name = name;
        this.gfxId = gfxId;
        this.align = align;
        this.colors = colors;
        this.minKamas = minKamas;
        this.maxKamas = maxKamas;
        this.ia = ia;
        this.isCapturable = capturable;
        this.aggroDistance = aggroDistance;
        this.type = type;
        int G = 1;

        int nbGrade = (thisGrades.split("\\|")).length;

        for (int n = 0; n < nbGrade; n++) {
            try {
                //Grades
                String[] split = thisGrades.split("\\|");
                String grade = split[n];
                String[] infos = grade.split("@");
                int level = Integer.parseInt(infos[0]);
                String resists = infos[1];
                //Stats
                String stats = thisStats.split("\\|")[n];
                //Spells
                String spells = "";
                if (!thisSpells.equalsIgnoreCase("||||")
                        && !thisSpells.equalsIgnoreCase("")
                        && !thisSpells.equalsIgnoreCase("-1")) {
                    spells = thisSpells.split("\\|")[n];
                    if (spells.equals("-1"))
                        spells = "";
                }
                //PDVMax//init
                int pdvmax = 1;
                int init = 1;

                try {
                    pdvmax = Integer.parseInt(thisPdvs.split("\\|")[n]);
                    init = Integer.parseInt(thisInit.split("\\|")[n]);
                } catch (Exception e) {
                    e.printStackTrace();
                    World.world.logger.error("#1# Erreur lors du chargement du monstre (template) : "
                             + id);
                }
                //PA / PM
                int PA = 3;
                int PM = 3;
                int xp = 10;

                try {
                    String[] pts = thisPoints.split("\\|")[n].split(";");
                    try {
                        PA = Integer.parseInt(pts[0]);
                        PM = Integer.parseInt(pts[1]);
                        xp = Integer.parseInt(thisXp.split("\\|")[n]);
                    } catch (Exception e1) {
                        World.world.logger.error("#2# Erreur lors du chargement du monstre (template) : "
                                + id);
                        e1.printStackTrace();
                    }
                } catch (Exception e) {
                    World.world.logger.error("#3# Erreur lors du chargement du monstre (template) : "
                            + id);
                    e.printStackTrace();
                }
                grades.put(G, new MobGrade(this, G, level, PA, PM, resists, stats, thisStatsInfos, spells, pdvmax, init, xp, n));
                G++;
            } catch (Exception e) {
                // ok, pour les dopeuls ...
                //TODO: Enlever toutes les erreurs
            }
        }

    }

    public void setInfos(int id,String name,int gfxId, int align, String colors,
                         String thisGrades, String thisSpells, String thisStats,
                         String thisStatsInfos, String thisPdvs, String thisPoints,
                         String thisInit, int minKamas, int maxKamas, String thisXp, int ia,
                         boolean capturable, int aggroDistance,int type) {
        this.id = id;
        this.name = name;
        this.gfxId = gfxId;
        this.align = align;
        this.colors = colors;
        this.minKamas = minKamas;
        this.maxKamas = maxKamas;
        this.ia = ia;
        this.isCapturable = capturable;
        this.aggroDistance = aggroDistance;
        this.type = type;
        int G = 1;

        grades.clear();
        String[] spellsList = thisSpells.split("\\|");
        String[] PdvMAXlist = thisPdvs.split("\\|");
        String[] initlist = thisInit.split("\\|");
        String[] Pointslist =  thisPoints.split("\\|");
        String[] XPlist =  thisXp.split("\\|");

        int sizeSpellList = spellsList.length;
        int sizePdvMAXList = PdvMAXlist.length;
        int sizeinitList = initlist.length;
        int sizePointsList = Pointslist.length;
        int sizeXPList = XPlist.length;

        int nbGrade = (thisGrades.split("\\|")).length;
        for (int n = 0; n < nbGrade; n++) {
            try {
                //Grades
                String grade = thisGrades.split("\\|")[n];
                String[] infos = grade.split("@");
                if(infos.length > 1){}else{
                    continue;
                }
                int level = Integer.parseInt(infos[0]);
                String resists = infos[1];
                //Stats
                String stats = thisStats.split("\\|")[n];
                //Spells
                String spells = "";
                //PDVMax//init
                int pdvmax = 1;
                int init = 1;
                //PA / PM
                int PA = 3;
                int PM = 3;
                int xp = 10;

                if(n < sizeSpellList) {
                    spells = spellsList[n];
                    if (spells.equals("-1"))
                        spells = "";
                }

                if(PdvMAXlist.length > n+1) {
                    pdvmax = Integer.parseInt(PdvMAXlist[n]);
                }
                if(initlist.length > n+1) {
                    pdvmax = Integer.parseInt(initlist[n]);
                }

                if(Pointslist.length > n+1 ){
                    String[] pts = Pointslist[n].split(";");
                    PA = Integer.parseInt(pts[0]);
                    PM = Integer.parseInt(pts[1]);
                }

                if(XPlist.length > n+1) {
                    xp = Integer.parseInt(XPlist[n]);
                }

                grades.put(G, new MobGrade(this, G, level, PA, PM, resists, stats, thisStatsInfos, spells, pdvmax, init, xp, n));
                G++;
            } catch (Exception e) {
                // ok pour les dopeuls
                //e.printStackTrace();
                System.out.println("Erreur avec le monstre : " + this.id +" - " + e.getMessage());
            }
        }
    }

    public void deleteDrop(int id) {
        Drop remove = null;
        for (Drop d : drops) {
            if (d.getObjectId() == id) {
                remove = d;
                break;
            }
        }
        if (remove != null) {
            /*World.world.getObjTemplate(id).delMobQueDropea(this.id)*/
            drops.remove(remove);
        }
    }

    public MobGrade getGrade(int gradevalue) {
        int graderandom = 1;
        for (Entry<Integer, MobGrade> grade : getGrades().entrySet()) {
            if (graderandom == gradevalue)
                return grade.getValue();
            else
                graderandom++;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public enum TipoGrupo {
        FIJO, NORMAL, SOLO_UNA_PELEA, HASTA_QUE_MUERA
    }
    public Boolean modifyStats(Byte grade, String packet) {
        try {
            MobGrade mob;
            if(grades.get((int)grade) != null) {
                mob = grades.get((int) grade);
            }
            else {
                return false;
            }
            String[] split = packet.split(Pattern.quote("|"));
            String[] stats = split[0].split(",");
            /*for (String stat : stats) {
                try {
                    var a = stat.split(":");
                    mob.stats.put(Integer.parseInt(a[0]), Integer.parseInt(a[1]));
                } catch (Exception ignored) {
                }
            }*/

            Monster m = World.world.getMonstre(mob.template.id);
            //mob.changeStats(stats);
            mob.changeStats(split[0]);


            mob.pdvMax = Integer.parseInt(split[1]);
            mob.baseXp = Integer.parseInt(split[2]);
            m.minKamas = Integer.parseInt(split[3]);
            m.maxKamas = Integer.parseInt(split[4]);

            String[] s = strStats4eachgrade().split("~");

            String statsStringBase = s[0];
            String statsInfoBase = s[6];
            String statsAction = s[5];
            String statInit = s[7];

            Database.getDynamics().getMonsterData().updateMonsterStats(id, statsStringBase, s[1], s[2], split[3], split[4], statsInfoBase,statsAction,statInit);


            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    public String strStats4eachgrade() { // Pour le panel de l'admin
        StringBuilder strStats = new StringBuilder();
        StringBuilder strStatsAction = new StringBuilder();
        StringBuilder strStatsInfo = new StringBuilder();
        StringBuilder strStatsInit = new StringBuilder();
        StringBuilder strPDV = new StringBuilder();
        StringBuilder strExp = new StringBuilder();
        StringBuilder strMinKamas = new StringBuilder();
        StringBuilder strMaxKamas = new StringBuilder();
        boolean e = false;
        for (int i = 1 ; i < 6; i++) {
            MobGrade mob;
            if(grades.get(i) != null) {
                mob = grades.get(i);
            }
            else{
                break;
            }
            if(e) {
                strStats.append("|");
                strStatsAction.append("|");
                strPDV.append("|");
                strExp.append("|");
                strStatsInit.append("|");
                strMinKamas.append("|");
                strMaxKamas.append("|");
            }
            else{
                strStatsInfo.append(mob.stringStatsInfoMobgrade());
            }
            strStats.append(mob.stringStatsMobgradeBase());
            strStatsAction.append(mob.stringStatsActionMobgrade());
            strStatsInit.append(mob.init);
            strPDV.append(mob.pdvMax);
            strExp.append(mob.baseXp);
            strMinKamas.append(mob.template.minKamas);
            strMaxKamas.append(mob.template.maxKamas);

            e = true;
            /*
            if (e) {
                strStats.append("|");
                strPDV.append("|");
                strExp.append("|");
                strMinKamas.append("|");
                strMaxKamas.append("|");
            }
            strStats.append(mob.stringStatsActualizado());
            strPDV.append(mob.pdvMax);
            strExp.append(mob.baseXp);
            strMinKamas.append(mob.template.minKamas);
            strMaxKamas.append(mob.template.maxKamas);
            e = true;*/
        }

        return (strStats.toString() + "~" + strPDV.toString() + "~" + strExp.toString() + "~" + strMinKamas.toString() + "~"
                + strMaxKamas.toString() + "~" + strStatsAction.toString()  + "~" + strStatsInfo.toString() + "~" + strStatsInit.toString() );
    }

    public String strStatsTodosMobs() { // Pour le panel de l'admin
        StringBuilder strStats = new StringBuilder();
        StringBuilder strPDV = new StringBuilder();
        StringBuilder strExp = new StringBuilder();
        StringBuilder strMinKamas = new StringBuilder();
        StringBuilder strMaxKamas = new StringBuilder();
        boolean e = false;
        for (int i = 1 ; i < 6; i++) {
            MobGrade mob;
            if(grades.get(i) != null) {
                mob = grades.get(i);
            }
            else{
                break;
            }
            if (e) {
                strStats.append("|");
                strPDV.append("|");
                strExp.append("|");
                strMinKamas.append("|");
                strMaxKamas.append("|");
            }
            strStats.append(mob.stringStatsActualizado());
            strPDV.append(mob.pdvMax);
            strExp.append(mob.baseXp);
            strMinKamas.append(mob.template.minKamas);
            strMaxKamas.append(mob.template.maxKamas);
            e = true;
        }
        return (strStats.toString() + "~" + strPDV.toString() + "~" + strExp.toString() + "~" + strMinKamas.toString() + "~"
                + strMaxKamas.toString());
    }

    public int getId() {
        return this.id;
    }

    public int getType() {
        return this.type;
    }

    public int getGfxId() {
        return this.gfxId;
    }

    public int getAlign() {
        return this.align;
    }

    public String getColors() {
        return this.colors;
    }

    public int getIa() {
        return this.ia;
    }

    public void setIA(int ia) {
        this.ia = ia;
    }

    public int getMinKamas() {
        return this.minKamas;
    }

    public int getMaxKamas() {
        return this.maxKamas;
    }

    public Map<Integer, MobGrade> getGrades() {
        return this.grades;
    }

    public void addDrop(Drop D) {
        this.drops.add(D);
    }

    public ArrayList<Drop> getDrops() {
        return this.drops;
    }

    public boolean isCapturable() {
        return this.isCapturable;
    }

    public int getAggroDistance() {
        return this.aggroDistance;
    }

    public MobGrade getGradeByLevel(int lvl) {
        if(this.getGrades() == null) return null;
        for (MobGrade grade : this.getGrades().values())
            if (grade != null && grade.getLevel() == lvl)
                return grade;
        return null;
    }

    public MobGrade getRandomGrade() {
        if(this.getGrades() == null) return null;
        int randomgrade = (int) (Math.random() * (6 - 1)) + 1, graderandom = 1;

        for (MobGrade grade : this.getGrades().values()) {
            if (grade != null && graderandom == randomgrade) return grade;
            else graderandom++;
        }

        return null;
    }

    public static class MobGroup {
        public final static MaitreCorbac MAITRE_CORBAC = new MaitreCorbac();

        private int id;
        private int cellId;
        private int orientation = 2;
        private int align = -1;
        private short starBonus;
        private int aggroDistance = 0;
        private int subarea = -1;
        private boolean changeAgro = false;
        private boolean isFix = false;
        private boolean isExtraGroup = false;
        private Map<Integer, MobGrade> mobs = new HashMap<Integer, MobGrade>();
        private String condition = "";
        private Timer condTimer;
        private ArrayList<GameObject> objects;
        public boolean isHotomani = false;

        public MobGroup(int Aid, int Aalign, ArrayList<MobGrade> possibles,
                        GameMap Map, int cell, int fixSize, int minSize, int maxSize,
                        MobGrade extra) {

            id = Aid;
            align = Aalign;
            //D�termination du nombre de mob du groupe
            int rand = 0;
            int nbr = 0;
            //region Nombre de monstre
            if (fixSize > 0 && fixSize < 9) {
                nbr = fixSize;
            } else if (minSize != -1 && maxSize != -1 && maxSize != 0
                    && (minSize < maxSize)) {
                if (minSize == 3 && maxSize == 8) {
                    rand = Formulas.getRandomValue(0, 99);
                    if (rand < 25) //3: 25%
                    {
                        nbr = 3;
                    } else if (rand < 48) //4:23%
                    {
                        nbr = 4;
                    } else if (rand < 51) //5:20%
                    {
                        nbr = 5;
                    } else if (rand < 85) //6:17%
                    {
                        nbr = 6;
                    } else if (rand < 95) //7:10%
                    {
                        nbr = 7;
                    } else
                    //8:5%
                    {
                        nbr = 8;
                    }
                } else if (minSize == 1 && maxSize == 3) { // 21 - normalement tout astrub
                    rand = Formulas.getRandomValue(0, 99);
                    if (rand < 40) //1: 40%
                    {
                        nbr = 1;
                    } else if (rand < 75)//2: 35%
                    {
                        nbr = 2;
                    } else
                    //3: 25%
                    {
                        nbr = 3;
                    }
                } else if (minSize == 1 && maxSize == 5) {
                    rand = Formulas.getRandomValue(0, 99);
                    if (rand < 30) //3: 30%
                    {
                        nbr = 1;
                    } else if (rand < 53) //4:23%
                    {
                        nbr = 2;
                    } else if (rand < 73) //5:20%
                    {
                        nbr = 3;
                    } else if (rand < 90) //6:17%
                    {
                        nbr = 4;
                    } else
                    //8:10%
                    {
                        nbr = 5;
                    }
                } else if (minSize == 1 && maxSize == 4) {
                    rand = Formulas.getRandomValue(0, 99);
                    if (rand < 35) //3: 35%
                    {
                        nbr = 1;
                    } else if (rand < 61) //4:26%
                    {
                        nbr = 2;
                    } else if (rand < 82) //5:21%
                    {
                        nbr = 3;
                    } else
                    //8:18%
                    {
                        nbr = 4;
                    }
                } else {
                    nbr = Formulas.getRandomValue(minSize, maxSize);
                }
            } else if (minSize == -1) {
                switch (maxSize) {
                    case 0:
                        return;
                    case 1:
                        nbr = 1;
                        break;
                    case 2:
                        nbr = Formulas.getRandomValue(1, 2); //1:50%	2:50%
                        break;
                    case 3:
                        nbr = Formulas.getRandomValue(1, 3); //1:33.3334%	2:33.3334%	3:33.3334%
                        break;
                    case 4:
                        rand = Formulas.getRandomValue(0, 99);
                        if (rand < 22) //1:22%
                            nbr = 1;
                        else if (rand < 48) //2:26%
                            nbr = 2;
                        else if (rand < 74) //3:26%
                            nbr = 3;
                        else
                            //4:26%
                            nbr = 4;
                        break;
                    case 5:
                        rand = Formulas.getRandomValue(0, 99);
                        if (rand < 15) //1:15%
                            nbr = 1;
                        else if (rand < 35) //2:20%
                            nbr = 2;
                        else if (rand < 60) //3:25%
                            nbr = 3;
                        else if (rand < 85) //4:25%
                            nbr = 4;
                        else
                            //5:15%
                            nbr = 5;
                        break;
                    case 6:
                        rand = Formulas.getRandomValue(0, 99);
                        if (rand < 10) //1:10%
                            nbr = 1;
                        else if (rand < 25) //2:15%
                            nbr = 2;
                        else if (rand < 45) //3:20%
                            nbr = 3;
                        else if (rand < 65) //4:20%
                            nbr = 4;
                        else if (rand < 85) //5:20%
                            nbr = 5;
                        else
                            //6:15%
                            nbr = 6;
                        break;
                    case 7:
                        rand = Formulas.getRandomValue(0, 99);
                        if (rand < 9) //1:9%
                            nbr = 1;
                        else if (rand < 20) //2:11%
                            nbr = 2;
                        else if (rand < 35) //3:15%
                            nbr = 3;
                        else if (rand < 55) //4:20%
                            nbr = 4;
                        else if (rand < 75) //5:20%
                            nbr = 5;
                        else if (rand < 91) //6:16%
                            nbr = 6;
                        else
                            //7:9%
                            nbr = 7;
                        break;
                    default:
                        rand = Formulas.getRandomValue(0, 99);
                        if (rand < 9) //1:9%
                            nbr = 1;
                        else if (rand < 20) //2:11%
                            nbr = 2;
                        else if (rand < 33) //3:13%
                            nbr = 3;
                        else if (rand < 50) //4:17%
                            nbr = 4;
                        else if (rand < 67) //5:17%
                            nbr = 5;
                        else if (rand < 80) //6:13%
                            nbr = 6;
                        else if (rand < 91) //7:11%
                            nbr = 7;
                        else
                            //8:9%
                            nbr = 8;
                        break;
                }
            } else {
                switch (minSize) {
                    case 1:
                        rand = Formulas.getRandomValue(1, 8);
                        switch (rand) {
                            case 1:
                                nbr = 1;
                                break;
                            case 2:
                                nbr = 2;
                                break;
                            case 3:
                                nbr = 3;
                                break;
                            case 4:
                                nbr = 4;
                                break;
                            case 5:
                                nbr = 5;
                                break;
                            case 6:
                                nbr = 6;
                                break;
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                    case 2:
                        rand = Formulas.getRandomValue(2, 8);
                        switch (rand) {
                            case 2:
                                nbr = 2;
                                break;
                            case 3:
                                nbr = 3;
                                break;
                            case 4:
                                nbr = 4;
                                break;
                            case 5:
                                nbr = 5;
                                break;
                            case 6:
                                nbr = 6;
                                break;
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                    case 3:
                        rand = Formulas.getRandomValue(3, 8);
                        switch (rand) {
                            case 3:
                                nbr = 3;
                                break;
                            case 4:
                                nbr = 4;
                                break;
                            case 5:
                                nbr = 5;
                                break;
                            case 6:
                                nbr = 6;
                                break;
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                    case 4:
                        rand = Formulas.getRandomValue(4, 8);
                        switch (rand) {
                            case 4:
                                nbr = 4;
                                break;
                            case 5:
                                nbr = 5;
                                break;
                            case 6:
                                nbr = 6;
                                break;
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                    case 5:
                        rand = Formulas.getRandomValue(5, 8);
                        switch (rand) {
                            case 5:
                                nbr = 5;
                                break;
                            case 6:
                                nbr = 6;
                                break;
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                    case 6:
                        rand = Formulas.getRandomValue(6, 8);
                        switch (rand) {
                            case 6:
                                nbr = 6;
                                break;
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                    case 7:
                        rand = Formulas.getRandomValue(7, 8);
                        switch (rand) {
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                    case 8:
                        nbr = 8;
                        break;
                    default:
                        rand = Formulas.getRandomValue(1, 8);
                        switch (rand) {
                            case 1:
                                nbr = 1;
                                break;
                            case 2:
                                nbr = 2;
                                break;
                            case 3:
                                nbr = 3;
                                break;
                            case 4:
                                nbr = 4;
                                break;
                            case 5:
                                nbr = 5;
                                break;
                            case 6:
                                nbr = 6;
                                break;
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                }
            }
            //endregion
            int guid = -1;
            boolean haveSameAlign = false;

            if (extra != null) {
                isExtraGroup = true;
                nbr--;
                this.mobs.put(guid, extra);
                guid--;
            }
            //On v�rifie qu'il existe des monstres de l'alignement demand� pour �viter les boucles infinies
            for (MobGrade mob : possibles)
                if (mob.getTemplate().getAlign() == this.align)
                    haveSameAlign = true;
            if (!haveSameAlign)
                return;//S'il n'y en a pas
            for (int a = 0; a < nbr; a++) {
                MobGrade Mob = null;
                do {
                    int random = Formulas.getRandomValue(0, possibles.size() - 1);//on prend un mob au hasard dans le tableau
                    Mob = possibles.get(random).getCopy();
                }
                while (Mob.getTemplate().getAlign() != this.align);

                this.mobs.put(guid, Mob);
                if (Mob.getTemplate().getAggroDistance() > this.aggroDistance)
                    this.aggroDistance = Mob.getTemplate().getAggroDistance();
                guid--;
            }

            this.cellId = (cell == -1 ? Map.getRandomFreeCellId() : cell);

            while (Map.containsForbiddenCellSpawn(this.cellId))
                this.cellId = Map.getRandomFreeCellId();
            if (this.cellId == 0)
                return;
            this.orientation = (Formulas.getRandomValue(0, 3) * 2) + 1;
            this.isFix = false;
            this.starBonus = 0;
        }

        public MobGroup(int id, int cellId, String groupData, String objects, short stars) {
            this.id = id;
            this.align = Constant.ALIGNEMENT_NEUTRE;
            this.cellId = cellId;
            this.isFix = false;
            this.orientation = (Formulas.getRandomValue(0, 3) * 2) + 1;
            this.starBonus = stars;

            int guid = -1;

            for (String data : groupData.split(";")) {
                if (data.equalsIgnoreCase(""))
                    continue;
                String[] infos = data.split(",");

                try {
                    int idMonster = Integer.parseInt(infos[0]);
                    int min = Integer.parseInt(infos[1]);
                    int max = Integer.parseInt(infos[2]);
                    Monster m = World.world.getMonstre(idMonster);
                    List<MobGrade> mgs = new ArrayList<MobGrade>();
                    //on ajoute a la liste les grades possibles

                    for (MobGrade MG : m.getGrades().values())
                        if (MG.level >= min && MG.level <= max)
                            mgs.add(MG);
                    if (mgs.isEmpty())
                        continue;
                    //On prend un grade au hasard entre 0 et size -1 parmis les mobs possibles
                    this.mobs.put(guid, mgs.get(Formulas.getRandomValue(0, mgs.size() - 1)));
                    if (m.getAggroDistance() > this.aggroDistance)
                        this.aggroDistance = m.getAggroDistance();
                    guid--;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for(Entry<Integer, MobGrade> mob :this.mobs.entrySet())// kralamour
                if(mob.getValue().getTemplate().getId() == 423)
                    this.orientation = 3;

            if(!objects.isEmpty()) {
                for (String value : objects.split(",")) {
                    final GameObject gameObject = World.world.getGameObject(Long.parseLong(value));
                    if (gameObject != null)
                        this.objects.add(gameObject);
                }
            }
        }

        public MobGroup(int id, int cellId, String groupData) {
            this.id = id;
            this.align = Constant.ALIGNEMENT_NEUTRE;
            this.cellId = cellId;
            this.isFix = true;
            int guid = -1;
            boolean star = false;
            for (String data : groupData.split(";")) {
                if (data.equalsIgnoreCase(""))
                    continue;
                String[] infos = data.split(",");
                try {
                    int idMonster = Integer.parseInt(infos[0]);
                    int min = Integer.parseInt(infos[1]);
                    int max = Integer.parseInt(infos[2]);
                    Monster m = World.world.getMonstre(idMonster);
                    List<MobGrade> mgs = new ArrayList<MobGrade>();
                    //on ajoute a la liste les grades possibles

                    for (MobGrade MG : m.getGrades().values()) {
                        if (MG.getBaseXp() != 0)
                            star = true;
                        if (MG.level >= min && MG.level <= max)
                            mgs.add(MG);

                        if(min >= m.getGrade(5).getLevel() && m.getId() != 394 ){
                            mgs.add(m.getGrade(5));
                            break;
                        }
                    }

                    if (mgs.isEmpty())
                        continue;
                    //On prend un grade au hasard entre 0 et size -1 parmis les mobs possibles
                    this.mobs.put(guid, mgs.get(Formulas.getRandomValue(0, mgs.size() - 1 )));


                    if (m.getAggroDistance() > this.aggroDistance)
                        this.aggroDistance = m.getAggroDistance();
                    guid--;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            this.orientation = (Formulas.getRandomValue(0, 3) * 2) + 1;
            this.starBonus = (short) (star ? 0 : -1);
        }

        public MobGroup(int id, int cellId, String groupData, int lvl) {
            this.id = id;
            this.align = Constant.ALIGNEMENT_NEUTRE;
            this.cellId = cellId;
            this.isFix = true;
            int guid = -1;
            boolean star = false;
            for (String data : groupData.split(";")) {
                if (data.equalsIgnoreCase(""))
                    continue;
                String[] infos = data.split(",");
                try {
                    int idMonster = Integer.parseInt(infos[0]);
                    Monster m = World.world.getMonstre(idMonster);
                    MobGrade mgs = m.getGrade(5);
                    //on ajoute a la liste les grades possibles
                    MobGrade mg2 = mgs.getCopy();
                    mg2.modifStatbyLvl(lvl,0);
                    mg2.baseXp = mgs.baseXp;
                    this.isHotomani = true;
                    //On prend un grade au hasard entre 0 et size -1 parmis les mobs possibles
                    this.mobs.put(guid, mg2);
                    if (m.getAggroDistance() > this.aggroDistance)
                        this.aggroDistance = m.getAggroDistance();
                    guid--;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.orientation = (Formulas.getRandomValue(0, 3) * 2) + 1;
            this.starBonus = (short) (star ? 0 : -1);
        }

        public void setSubArea(int sa) {
            this.subarea = sa;
        }

        public void changeAgro() {
            if (!changeAgro) {
                if (this.haveMineur()) {
                    // 29 : sous-terrain
                    // 96 : exploitation mini�re d'astrub
                    // 31 : passage vers brakmar
                    if (this.subarea != 29 && this.subarea != 96
                            && this.subarea != 31) {
                        this.removeAgro(118);
                    }
                }
            }
            changeAgro = true;
        }

        public void removeAgro(int id) {
            this.aggroDistance = 0;
            for (Entry<Integer, MobGrade> e : this.mobs.entrySet()) {
                MobGrade mb = e.getValue();
                if (mb.template.getId() != id) {
                    if (mb.template.getAggroDistance() > this.aggroDistance) {
                        this.aggroDistance = mb.template.getAggroDistance();
                    }
                }
            }
        }

        public boolean haveMineur() {
            for (Entry<Integer, MobGrade> e : this.mobs.entrySet()) {
                MobGrade mb = e.getValue();
                if (mb.template.getId() == 118) {
                    return true;
                }
            }
            return false;
        }

        public int getId() {
            return this.id;
        }

        public int getCellId() {
            return this.cellId;
        }

        public void setCellId(int cellId) {
            this.cellId = cellId;
        }

        public int getOrientation() {
            return this.orientation;
        }

        public void setOrientation(int orientation) {
            this.orientation = orientation;
        }

        public int getAlignement() {
            return this.align;
        }

        public void addStarBonus() {
            if(this.getStarBonus() >= 200) {
                this.starBonus = 200;
            } else {
                this.starBonus += 15;
            }
        }

        public int getStarBonus() {
            return this.starBonus == -1 ? 0 : this.starBonus;
        }

        public int getAggroDistance() {
            return this.aggroDistance;
        }

        public boolean isFix() {
            return this.isFix;
        }

        public void setIsFix(boolean isFix) {
            this.isFix = isFix;
        }

        public boolean getIsExtraGroup() {
            return this.isExtraGroup;
        }

        public Map<Integer, MobGrade> getMobs() {
            return this.mobs;
        }

        public MobGrade getMobGradeById(int id) {
            return this.mobs.get(id);
        }

        public String getCondition() {
            return this.condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public void startCondTimer() {
            this.condTimer = new Timer();
            condTimer.schedule(new TimerTask() {
                public void run() {
                    condition = "";
                }
            }, 60000 * 10);
        }

        public void stopConditionTimer() {
            try {
                this.condTimer.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ArrayList<GameObject> getObjects() {
            if(this.objects == null && Config.INSTANCE.getHEROIC())
                this.objects = new ArrayList<>();
            else if(!Config.INSTANCE.getHEROIC())
                return new ArrayList<>();
            return objects;
        }

        public String parseGM() {
            // Format 1.43.7 (vérifié dans le parser AS2 source, dofus.aks.extend.GameIn.onMovement) :
            //   +{cell};{dir};{starBonus};{groupId};{mobIDs};-3;{mobGFX};{mobLevels};{c1};{a1};{c2};{a2};...
            // Pour chaque mob i : colors à _loc10_[8 + 2i], accessories à _loc10_[9 + 2i].
            // Affichage de TOUS les mobs si option "ViewAllMonsterInGroup" activée côté client.
            // PAS de totalExp à la position 8 (c'est ce qui cassait avant).
            StringBuilder mobIDs = new StringBuilder();
            StringBuilder mobGFX = new StringBuilder();
            StringBuilder mobLevels = new StringBuilder();
            StringBuilder colorsAndAccessories = new StringBuilder();
            if (this.mobs.isEmpty())
                return "";

            boolean isFirst = true;
            for (Entry<Integer, MobGrade> entry : this.mobs.entrySet()) {
                if (!isFirst) {
                    mobIDs.append(",");
                    mobGFX.append(",");
                    mobLevels.append(",");
                    colorsAndAccessories.append(";");
                }
                MobGrade mob = entry.getValue();
                int templateId = mob.getTemplate().getId();
                mobIDs.append(templateId);
                mobGFX.append(mob.getTemplate().getGfxId()).append("^").append(mob.getSize());
                mobLevels.append(mob.getLevel());

                String mobColors = mob.getTemplate().getColors();
                if (mobColors == null || mobColors.isEmpty()) mobColors = "-1,-1,-1";
                String mobAccessories;
                if (templateId == 534 || templateId == 547) mobAccessories = "0,1C3C,1C40,0";
                else if (templateId == 1213) mobAccessories = "0,2BA,847,0";
                else mobAccessories = "0,0,0,0";
                colorsAndAccessories.append(mobColors).append(";").append(mobAccessories);

                isFirst = false;
            }

            StringBuilder toreturn = new StringBuilder();
            toreturn.append("+").append(this.cellId)
                    .append(";").append(this.orientation)
                    .append(";").append(getStarBonus())
                    .append(";").append(this.id)
                    .append(";").append(mobIDs)
                    .append(";-3;").append(mobGFX)
                    .append(";").append(mobLevels)
                    .append(";").append(colorsAndAccessories);
            return toreturn.toString();
        }

        public void setStarBonus(int i) {
            this.starBonus = (short) i;

        }

    }

    public static class MobGrade {
        private static int pSize = 2;
        private Monster template;
        private int grade;
        private int level;
        private int pdv;
        private int pdvMax;
        private int inFightId;
        private int init;
        private int pa;
        private int pm;
        private int size;
        private int baseXp = 10;
        private GameCase fightCell;
        private ArrayList<Effect> fightBuffs = new ArrayList<Effect>();
        public Map<Integer, Integer> stats = new HashMap<Integer, Integer>();
        private Map<Integer, SpellGrade> spells = new HashMap<Integer, SpellGrade>();
        public ArrayList<Integer> statsInfos = new ArrayList<Integer>();

        public MobGrade(Monster template, int grade, int level, int pa, int pm, String resists, String stats, String statsInfos, String allSpells, int pdvMax, int aInit, int xp, int n) {
            this.size = 100 + n * pSize;
            this.template = template;
            this.grade = grade;
            this.level = level;
            this.pdvMax = pdvMax;
            this.pdv = pdvMax;
            this.pa = pa;
            this.pm = pm;
            this.baseXp = xp;
            this.init = aInit;
            this.stats.clear();
            this.spells.clear();

            String[] resist = resists.split(";"), stat = stats.split(","), statInfos = statsInfos.split(";");

            for (String str : statInfos)
                this.statsInfos.add(Integer.parseInt(str));

            try {
                if(resist.length > 3) {
                    this.stats.put(EffectConstant.STATS_ADD_RP_NEU, Integer.parseInt(resist[0]));
                    this.stats.put(EffectConstant.STATS_ADD_RP_TER, Integer.parseInt(resist[1]));
                    this.stats.put(EffectConstant.STATS_ADD_RP_FEU, Integer.parseInt(resist[2]));
                    this.stats.put(EffectConstant.STATS_ADD_RP_EAU, Integer.parseInt(resist[3]));
                    this.stats.put(EffectConstant.STATS_ADD_RP_AIR, Integer.parseInt(resist[4]));
                    this.stats.put(EffectConstant.STATS_ADD_AFLEE, Integer.parseInt(resist[5]));
                    this.stats.put(EffectConstant.STATS_ADD_MFLEE, Integer.parseInt(resist[6]));
                } else {
                    String[] split = resist[0].split(",");
                    this.stats.put(-1, Integer.parseInt(split[0]));
                    this.stats.put(-100, Integer.parseInt(split[1]));
                    this.stats.put(EffectConstant.STATS_ADD_AFLEE, Integer.parseInt(resist[1]));
                    this.stats.put(EffectConstant.STATS_ADD_MFLEE, Integer.parseInt(resist[2]));
                }

                this.stats.put(EffectConstant.STATS_ADD_FORC, Integer.parseInt(stat[0]));
                this.stats.put(EffectConstant.STATS_ADD_SAGE, Integer.parseInt(stat[1]));
                this.stats.put(EffectConstant.STATS_ADD_INTE, Integer.parseInt(stat[2]));
                this.stats.put(EffectConstant.STATS_ADD_CHAN, Integer.parseInt(stat[3]));
                this.stats.put(EffectConstant.STATS_ADD_AGIL, Integer.parseInt(stat[4]));
                this.stats.put(EffectConstant.STATS_ADD_DOMA, Integer.parseInt(statInfos[0]));
                this.stats.put(EffectConstant.STATS_ADD_PERDOM, Integer.parseInt(statInfos[1]));
                this.stats.put(EffectConstant.STATS_ADD_SOIN, Integer.parseInt(statInfos[2]));
                this.stats.put(EffectConstant.STATS_CREATURE, Integer.parseInt(statInfos[3]));

            } catch (Exception e) {
                World.world.logger.error("#1# Erreur lors du chargement du grade du monstre (template) : " + template.getId());
                e.printStackTrace();
            }

            if (!allSpells.equalsIgnoreCase("")) {
                String[] spells = allSpells.split(";");

                for (String str : spells) {
                    if (str.equals("")) continue;
                    String[] spellInfo = str.split("@");
                    int id, lvl;

                    try {
                        id = Integer.parseInt(spellInfo[0]);
                        lvl = Integer.parseInt(spellInfo[1]);
                    } catch (Exception e) {
                        System.out.println("#1# Erreur lors du chargement du grade du monstre (template) : " + template.getId());
                        e.printStackTrace();
                        continue;
                    }

                    if (id == 0 || lvl == 0) continue;
                    Spell spell = World.world.getSort(id);
                    if (spell == null) continue;
                    SpellGrade spellStats = spell.getStatsByLevel(lvl);
                    if (spellStats == null) continue;
                    this.spells.put(id, spellStats);
                }
            }
        }

        public void changeStats(String statInfos) {
            //this.stats.clear();
            String[] stats = statInfos.split(",");
            for(String s : stats)
            {
                String[] details = s.split(":");
                if(Integer.parseInt(details[0]) == 111){
                    this.pa = Integer.parseInt(details[1]);
                }
                else if(Integer.parseInt(details[0]) == 128){
                    this.pm = Integer.parseInt(details[1]);
                }
                else if(Integer.parseInt(details[0]) == 174){
                    this.init = Integer.parseInt(details[1]);
                }
                else{
                    this.stats.put(Integer.parseInt(details[0]), Integer.parseInt(details[1]));
                }
            }

            /*String[] stats = statInfos.split(",");
            for(String s : stats)
            {
                String[] details = s.split(":");
                this.stats.put(Integer.parseInt(details[0]), Integer.parseInt(details[1]));
            }*/
        }


        private MobGrade(Monster template, int grade, int level, int pdv,
                         int pdvMax, int pa, int pm,
                         Map<Integer, Integer> stats,
                         ArrayList<Integer> statsInfos,
                         Map<Integer, SpellGrade> spells, int xp, int n, int init) {
            this.size = 100 + n * pSize;
            this.template = template;
            this.grade = grade;
            this.level = level;
            this.pdv = pdv;
            this.pdvMax = pdvMax;
            this.pa = pa;
            this.pm = pm;
            this.stats = stats;
            this.statsInfos = statsInfos;
            this.spells = spells;
            this.inFightId = -1;
            this.baseXp =xp;
            this.init = init;
            //this.baseXp = level*1000;
        }

        public MobGrade getCopy() {
            Map<Integer, Integer> newStats = new HashMap<Integer, Integer>();
            newStats.putAll(this.stats);
            int n = (this.size - 100) / pSize;
            return new MobGrade(this.template, this.grade, this.level, this.pdv, this.pdvMax, this.pa, this.pm, newStats, this.statsInfos, this.spells, this.baseXp, n, this.init);
        }

        public void GrowMGByDiff(int fightDiff) {
            Monster.MobGrade MG5 = this.getTemplate().getGrade(5);
            Monster.MobGrade MG4 = this.getTemplate().getGrade(4);
            Monster.MobGrade MG1 = this.getTemplate().getGrade(1);

            switch (fightDiff) {
                case 1:
                    int lvlMin = MG4.getLevel();
                    int lvlMax = MG5.getLevel();

                    int lvlmoypergrade = (lvlMax - lvlMin);
                    int LvlMG6 = 0;
                    if(lvlmoypergrade != 0) {
                        // On génére le niveau
                        LvlMG6 = lvlMax + (lvlmoypergrade * 4);
                    }
                    else {
                        LvlMG6 = lvlMax;
                    }
                    this.level = LvlMG6;
                    this.size = 115;
                    // XP
                    int XPMG6 = MG5.baseXp + (MG5.baseXp - MG1.baseXp);
                    // INIT
                    int INITMG6 = MG5.init + (MG5.init - MG1.init);
                    this.init = INITMG6;
                    this.baseXp = XPMG6;

                    break;
                case 2:
                case 3:
                case 4:
                    this.level = MG5.getLevel() + fightDiff*50 ;
                    this.size = 100 + fightDiff*15 ;
                    int XPMG7 = MG5.baseXp + (MG5.baseXp - MG1.baseXp) + (fightDiff*50 * 100);
                    this.baseXp = XPMG7;
                    break;
            }
        }

        public void replaceStatsInfos(int s) {
            statsInfos.clear();
            statsInfos.add(s);
        }

        public void refresh() {
            if (this.spells.isEmpty())
                return;
            String spells = "";

            for (Entry<Integer, SpellGrade> entry : this.spells.entrySet()) {
                spells += (spells.isEmpty() ? entry.getKey() + ","
                        + entry.getValue().getLevel() : ";" + entry.getKey()
                        + "," + entry.getValue().getLevel());
            }

            this.spells.clear();

            if (!spells.equalsIgnoreCase("")) {
                for (String split : spells.split("\\;")) {
                    int id = Integer.parseInt(split.split("\\,")[0]);
                    this.spells.put(id, World.world.getSort(id).getStatsByLevel(Integer.parseInt(split.split("\\,")[1])));
                }
            }
        }

        public int getSize() {
            return this.size;
        }

        public Monster getTemplate() {
            return this.template;
        }

        public int getGrade() {
            return this.grade;
        }

        public int getLevel() {
            return this.level;
        }

        public void setLevel(int lvl) {
            this.level = lvl;
        }

        public int getPdv() {
            return this.pdv;
        }

        public void setPdv(int pdv) {
            this.pdv = pdv;
        }

        public int getPdvMax() {
            return this.pdvMax;
        }

        public int getInFightID() {
            return this.inFightId;
        }

        public void setInFightID(int i) {
            this.inFightId = i;
        }

        public int getInit() {
            return this.init;
        }

        public int getPa() {
            return this.pa;
        }

        public int getPm() {
            return this.pm;
        }

        public int getBaseXp() {
            return this.baseXp;
        }

        public GameCase getFightCell() {
            return this.fightCell;
        }

        public void setFightCell(GameCase cell) {
            this.fightCell = cell;
        }

        public ArrayList<Effect> getBuffs() {
            return this.fightBuffs;
        }

        public Stats getStats() {
            if(this.getTemplate().getId() == 42 && !stats.containsKey(EffectConstant.STATS_CREATURE))
                stats.put(EffectConstant.STATS_CREATURE, 5);
            if(this.stats.get(-1) != null) {
                Map<Integer, Integer> LinkedHashMap = new LinkedHashMap<>();
                stats.putAll(this.stats);
                stats.remove(-1); stats.remove(-100);

                int random = Formulas.getRandomValue(210, 214);
                int one = this.stats.get(-1), all = this.stats.get(-100);

                stats.put(EffectConstant.STATS_ADD_RP_NEU, (random == EffectConstant.STATS_ADD_RP_NEU ? one : all));
                stats.put(EffectConstant.STATS_ADD_RP_TER, (random == EffectConstant.STATS_ADD_RP_TER ? one : all));
                stats.put(EffectConstant.STATS_ADD_RP_FEU, (random == EffectConstant.STATS_ADD_RP_FEU ? one : all));
                stats.put(EffectConstant.STATS_ADD_RP_EAU, (random == EffectConstant.STATS_ADD_RP_EAU ? one : all));
                stats.put(EffectConstant.STATS_ADD_RP_AIR, (random == EffectConstant.STATS_ADD_RP_AIR ? one : all));

                return new Stats(stats);
            }
            return new Stats(this.stats);
        }

        public Map<Integer, SpellGrade> getSpells() {
            return this.spells;
        }

        public void modifStatByInvocator(Fighter caster) {
            float tauxlvl = (1.0F + (caster.getLvl()) / 100.0F);
            float tauxstat = 0.25F;
            float tauxhp = 0.1F;

            float boostlife = caster.getPdvMax() * tauxhp;
            Stats casterboost = caster.getTotalStats();

            float force2 = casterboost.get(EffectConstant.STATS_ADD_FORC) * tauxstat;
            float intel2 = casterboost.get(EffectConstant.STATS_ADD_INTE) * tauxstat;
            float agili2 = casterboost.get(EffectConstant.STATS_ADD_AGIL) * tauxstat;
            float sages2 = casterboost.get(EffectConstant.STATS_ADD_SAGE) * tauxstat;
            float chanc2 = casterboost.get(EffectConstant.STATS_ADD_CHAN) * tauxstat;

            float force3 = (this.stats.get(EffectConstant.STATS_ADD_FORC) * tauxlvl) ;
            float intel3 =  (this.stats.get(EffectConstant.STATS_ADD_INTE) * tauxlvl) ;
            float agili3 = (this.stats.get(EffectConstant.STATS_ADD_AGIL) * tauxlvl) ;
            float sages3 =  (this.stats.get(EffectConstant.STATS_ADD_SAGE) * tauxlvl) ;
            float chanc3 =  (this.stats.get(EffectConstant.STATS_ADD_CHAN) * tauxlvl) ;

            int life = Math.round(this.pdvMax * tauxlvl) + Math.round(boostlife) ;
            this.pdv = life;
            this.pdvMax = life;

            int force = Math.round( force3 + force2 );
            int intel = Math.round( intel3 + intel2 );
            int agili = Math.round( agili3 + agili2 );
            int sages = Math.round( sages3 + sages2 );
            int chanc = Math.round( chanc3 + chanc2 );

            this.stats.put(EffectConstant.STATS_ADD_FORC, force);
            this.stats.put(EffectConstant.STATS_ADD_INTE, intel);
            this.stats.put(EffectConstant.STATS_ADD_AGIL, agili);
            this.stats.put(EffectConstant.STATS_ADD_SAGE, sages);
            this.stats.put(EffectConstant.STATS_ADD_CHAN, chanc);
        }

        public void modifStatbyLvl(int newlvl,int avgLvlBoost) {

            int leveldepartMonstre = this.level;

            this.level = leveldepartMonstre + newlvl;
            this.size = 100 + 4 * 15;

        }

        public double getRatioStatifExist(int stat,double taux){
            double ratio = 0;
            if(this.getStats().getEffect(stat) != 0) {
                ratio = (float)this.stats.get(stat) / this.getLevel();
            }
            else{
                ratio = taux;
            }
            if(ratio <1 ){
                ratio = 1;
            }
            return ratio;
        }

        public double getRatioPoidifExist(int stat,double ratio2){
            double ratio = 0;
            if(this.getStats().getEffect(stat) != 0) {
                 ratio = this.stats.get(stat) / (float)this.getLevel();
                 if(ratio == 0 ){
                     ratio = ratio2;
                 }
            }
            else{
                 ratio = ratio2;
            }
            return ratio;
        }

        public String stringStatsMobgradeBase() {
            StringBuilder strStats = new StringBuilder();
            strStats.append(this.stats.get(EffectConstant.STATS_ADD_FORC)).append(",");
            strStats.append(this.stats.get(EffectConstant.STATS_ADD_SAGE)).append(",");
            strStats.append(this.stats.get(EffectConstant.STATS_ADD_INTE)).append(",");
            strStats.append(this.stats.get(EffectConstant.STATS_ADD_CHAN)).append(",");
            strStats.append(this.stats.get(EffectConstant.STATS_ADD_AGIL));
            return strStats.toString();
        }

        public String stringStatsInfoMobgrade() {
            StringBuilder strStats = new StringBuilder();
            strStats.append(this.stats.get(EffectConstant.STATS_ADD_DOMA)).append(";");
            strStats.append(this.stats.get(EffectConstant.STATS_ADD_PERDOM)).append(";");
            strStats.append(this.stats.get(EffectConstant.STATS_ADD_SOIN)).append(";");
            strStats.append(this.stats.get(EffectConstant.STATS_CREATURE));
            return strStats.toString();
        }

        public String stringStatsActionMobgrade() {
            StringBuilder strStats = new StringBuilder();
            strStats.append(this.pa).append(";");
            strStats.append(this.pm);
            return strStats.toString();
        }

        /*public String stringStatsResiGradeMobgrade() {
            StringBuilder strStats = new StringBuilder();

            strStats.append(this.getLevel()).append("@");
            strStats.append(this.stats.get(Constant.STATS_ADD_RP_NEU)).append(";");
            strStats.append(this.stats.get(Constant.STATS_ADD_RP_TER)).append(";");
            strStats.append(this.stats.get(Constant.STATS_ADD_RP_FEU)).append(";");
            strStats.append(this.stats.get(Constant.STATS_ADD_RP_EAU)).append(";");
            strStats.append(this.stats.get(Constant.STATS_ADD_RP_AIR)).append(";");
            strStats.append(this.stats.get(Constant.STATS_ADD_AFLEE)).append(";");
            strStats.append(this.stats.get(Constant.STATS_ADD_MFLEE));
            return strStats.toString();
        }*/

        public String stringStatsActualizado() {
            StringBuilder strStats = new StringBuilder();
            strStats.append(EffectConstant.STATS_ADD_PA).append(":").append(getPa()).append(",");
            strStats.append(EffectConstant.STATS_ADD_PM).append(":").append(getPm()).append(",");
            stats.forEach((key, value) -> {
                switch (key) {
                    case EffectConstant.STATS_ADD_PA:
                    case EffectConstant.STATS_ADD_PM:
                    case EffectConstant.STATS_ADD_DOMA:
                    case EffectConstant.STATS_ADD_PERDOM:
                    case EffectConstant.STATS_CREATURE:
                    case EffectConstant.STATS_ADD_SAGE:
                    case EffectConstant.STATS_ADD_RP_NEU:
                    case EffectConstant.STATS_ADD_RP_TER:
                    case EffectConstant.STATS_ADD_RP_FEU:
                    case EffectConstant.STATS_ADD_RP_EAU:
                    case EffectConstant.STATS_ADD_RP_AIR:
                    case EffectConstant.STATS_ADD_MFLEE:
                    case EffectConstant.STATS_ADD_AFLEE:
                    case EffectConstant.STATS_ADD_CHAN:
                    case EffectConstant.STATS_ADD_AGIL:
                    case EffectConstant.STATS_ADD_FORC:
                    case EffectConstant.STATS_ADD_INTE :
                    case EffectConstant.STATS_ADD_INIT :
                    {
                        if (!strStats.toString().isEmpty()) {
                            strStats.append(",");
                        }
                        strStats.append(key).append(":").append(value);
                    }
                }
            });
            strStats.append(",").append(EffectConstant.STATS_ADD_INIT).append(":").append(getInit());
            return strStats.toString();
        }

        public String getStringResi() {
            String Resi ="";
            Resi = this.stats.get(EffectConstant.STATS_ADD_RP_NEU) + "," + this.stats.get(EffectConstant.STATS_ADD_RP_TER) + "," + this.stats.get(EffectConstant.STATS_ADD_RP_FEU) + "," + this.stats.get(EffectConstant.STATS_ADD_RP_EAU) + "," +this.stats.get(EffectConstant.STATS_ADD_RP_AIR);
            return Resi;
        }

        public String getResiString() {
            String Neutre = this.stats.get(EffectConstant.STATS_ADD_RP_NEU)+";" ;
            String Terre = this.stats.get(EffectConstant.STATS_ADD_RP_TER)+";";
            String Feu =this.stats.get(EffectConstant.STATS_ADD_RP_FEU)+";";
            String Eau =this.stats.get(EffectConstant.STATS_ADD_RP_EAU)+";";
            String Agi =this.stats.get(EffectConstant.STATS_ADD_RP_AIR)+";";
            String EsquiPA =this.stats.get(EffectConstant.STATS_ADD_AFLEE)+";";
            String EsquiPM =this.stats.get(EffectConstant.STATS_ADD_MFLEE) +"";

            return Neutre+Terre+Feu+Eau+Agi+EsquiPA+EsquiPM;
        }

        public String getTacle() {
            int Tacle = Math.round(this.stats.get(EffectConstant.STATS_ADD_AGIL)/10);
            String TacleString = Tacle+""  ;
            return TacleString;
        }

        public String getFuite() {
            int Tacle = Math.round(this.stats.get(EffectConstant.STATS_ADD_AGIL)/10);
            String TacleString = Tacle+""  ;
            return TacleString;
        }

    }
}
