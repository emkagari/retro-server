package common;

import area.map.GameMap;
import client.Player;
import fight.spells.EffectConstant;
import game.world.World.Couple;
import job.JobStat;
import kernel.Constant;
import object.GameObject;
import org.apache.commons.lang3.ArrayUtils;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import other.Action;
import quest.Quest;
import quest.QuestPlayer;
import quest.QuestStep;

import java.util.ArrayList;
import java.util.Map.Entry;

public class ConditionParser {

    public static boolean validConditions(Player perso, String req) {
        if (req == null || req.equals(""))
            return true;
        if (req.contains("BI"))
            return false;
        if (perso == null)
            return false;
        JEP jep = new JEP();
        req = req.replace("&", "&&").replace("=", "==").replace("|", "||").replace("!", "!=").replace("~", "==");
        if (req.contains("Sc"))
            return true;
        if (req.contains("Pg")) // C'est les dons que l'on gagne lors des qu�tes d'alignement, connaissance des potions etc ... ce n'est pas encore cod� !
            return false;
        if (req.contains("RA"))
            return haveRA(req, perso);
        if (req.contains("RO"))
            return haveRO(req, perso);
        if (req.contains("Mph"))
            return haveMorph(req, perso);
        if (req.contains("PO"))
            req = havePO(req, perso);
        if (req.contains("PN"))
            req = canPN(req, perso);
        if (req.contains("PJ"))
            req = canPJ(req, perso);
        if (req.contains("JOB"))
            req = haveJOB(req, perso);
        if (req.contains("NPC"))
            return haveNPC(req, perso);
        if (req.contains("QEt"))
            return haveQEt(req, perso);
        if (req.contains("QE"))
            return haveQE(req, perso);
        if (req.contains("QT"))
            return haveQT(req, perso);
        if (req.contains("Ce"))
            return haveCe(req, perso);
        if (req.contains("TiT"))
            return haveTiT(req, perso);
        if (req.contains("Ti"))
            return haveTi(req, perso);
        if (req.contains("Qa"))
            return haveQa(req, perso);
        if (req.contains("Pj"))
            return havePj(req, perso);
        if (req.contains("AM"))
            return haveMetier(req, perso);

        try {
            //Stats stuff compris
            jep.addVariable("CI", perso.getTotalStats().getEffect(EffectConstant.STATS_ADD_INTE));
            jep.addVariable("CV", perso.getTotalStats().getEffect(EffectConstant.STATS_ADD_VITA));
            jep.addVariable("CA", perso.getTotalStats().getEffect(EffectConstant.STATS_ADD_AGIL));
            jep.addVariable("CW", perso.getTotalStats().getEffect(EffectConstant.STATS_ADD_SAGE));
            jep.addVariable("CC", perso.getTotalStats().getEffect(EffectConstant.STATS_ADD_CHAN));
            jep.addVariable("CS", perso.getTotalStats().getEffect(EffectConstant.STATS_ADD_FORC));
            jep.addVariable("CM", perso.getTotalStats().getEffect(EffectConstant.STATS_ADD_PM));
            //Stats de bases
            jep.addVariable("Ci", perso.getStats().getEffect(EffectConstant.STATS_ADD_INTE));
            jep.addVariable("Cs", perso.getStats().getEffect(EffectConstant.STATS_ADD_FORC));
            jep.addVariable("Cv", perso.getStats().getEffect(EffectConstant.STATS_ADD_VITA));
            jep.addVariable("Ca", perso.getStats().getEffect(EffectConstant.STATS_ADD_AGIL));
            jep.addVariable("Cw", perso.getStats().getEffect(EffectConstant.STATS_ADD_SAGE));
            jep.addVariable("Cc", perso.getStats().getEffect(EffectConstant.STATS_ADD_CHAN));
            //Autre
            jep.addVariable("Ps", perso.get_align());//Alignement
            jep.addVariable("Pa", perso.getALvl());
            jep.addVariable("PP", perso.getGrade());//Grade
            jep.addVariable("PL", perso.getLevel());//Niveau
            jep.addVariable("PK", perso.getKamas());//Kamas
            jep.addVariable("PG", perso.getClasse());//Classe
            jep.addVariable("PS", perso.getSexe());//Sexe
            jep.addVariable("PZ", 1);//Abonnement
            jep.addVariable("PX", (perso.getGroupe() != null));//Niveau GM
            jep.addVariable("PW", perso.getMaxPod());//MaxPod
            if (perso.getCurMap().getSubArea() != null)
                jep.addVariable("PB", perso.getCurMap().getSubArea().getId());//SubArea
            jep.addVariable("PR", (perso.getWife() > 0 ? 1 : 0));//Mari� ou pas
            jep.addVariable("SI", perso.getCurMap().getId());//Mapid
            jep.addVariable("MiS", perso.getId());//Les pierres d'ames sont lancables uniquement par le lanceur.
            jep.addVariable("MA", perso.getAlignMap());//Pandala
            if(perso.getAccount().getWebAccount() != null)
                jep.addVariable("PSB", perso.getAccount().getWebAccount().getPoints());//Points Boutique
            jep.addVariable("CF", (perso.getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR) == null ? -1 : perso.getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR).getTemplate().getId()));//Personnage suiveur

            Node node = jep.parse(req);
            Double result = (Double)jep.evaluate(node);
            return result == 1.0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*public static boolean parseConditions(Player perso, String sCondition) {
        String[] var2 = { ">", "<", "=", "!" };
        String var3 = sCondition;
        List<String> var5 = new ArrayList<>();

        if (var3 == null || var3.isEmpty()) {
            return true;
        }

        String[] var4 = var3.split("&");
        int var6 = 0;

        while (var6 < var4.length) {
            var4[var6] = var4[var6].replace("(", "").replace(")", "");
            String[] var7 = var4[var6].split("\\|");

            for (int var8 = 0; var8 < var7.length; var8++) {
                String var9 = var7[var8];
                String var12 = "";
                String var13 = "";

                for (String var10 : var2) {
                    String[] var9Array = var9.split(java.util.regex.Pattern.quote(var10));

                    if (var9Array.length > 1) {
                        var12 = String.valueOf(var9Array[0]);
                        var13 = var9Array[1];
                        break;
                    }
                }

                if (var12 != null && !var12.isEmpty()) {
                    if (var12.equals("PZ")) {
                        break;
                    }

                    switch (var12) {
                        case "Ps":
                            var13 = this.api.lang.getAlignment(Integer.parseInt(var13)).n;
                            break;
                        case "PS":
                            var13 = var13.equals("1") ? this.api.lang.getText("MALE") : this.api.lang.getText("FEMALE");
                            break;
                        case "Pr":
                            var13 = this.api.lang.getAlignmentSpecialization(Integer.parseInt(var13)).n;
                            break;
                        case "Pg":
                            String[] var14 = var13.split(",");
                            if (var14.length == 2) {
                                var13 = this.api.lang.getAlignmentFeat(Integer.parseInt(var14[0])).n + " (" + Integer.parseInt(var14[1]) + ")";
                            } else {
                                var13 = this.api.lang.getAlignmentFeat(Integer.parseInt(var13)).n;
                            }
                            break;
                        default:
                            switch (var12) {
                                case "PG":
                                    var13 = this.api.lang.getClassText(Integer.parseInt(var13)).sn;
                                    break;
                                case "PJ":
                                case "Pj":
                                    String[] var15 = var13.split(",");
                                    var13 = this.api.lang.getJobText(Integer.parseInt(var15[0])).n + (var15.length > 1 ? " (" + this.api.lang.getText("LEVEL_SMALL") + " " + Integer.parseInt(var15[1]) + ")" : "");
                                    break;
                                case "PM":
                                    continue;
                                case "PO":
                                    dofus.datacenter.MyClass var16 = new dofus.datacenter.MyClass(-1, Integer.parseInt(var13), 1, 0, "", 0);
                                    var13 = var16.name;
                            }
                    }

                    var12 = new ank.utils().replace(var12, new String[] { "CS", "Cs", "CV", "Cv", "CA", "Ca", "CI", "Ci", "CW", "Cw", "CC", "Cc", "CA", "PG", "PJ", "Pj", "PM", "PA", "PN", "PE", "<NO>", "PS", "PR", "PL", "PK", "Pg", "Pr", "Ps", "Pa", "PP", "PZ", "CM" }, this.api.lang.getText("ITEM_CHARACTERISTICS").split(","));
                    boolean var17 = var10.equals("!");
                    var10 = new ank.utils().replace(var10, new String[] { "!" }, new String[] { this.api.lang.getText("ITEM_NO") });

                    switch (var12) {
                        case "BI":
                            var5.add(this.api.lang.getText("UNUSABLE"));
                            break;
                        case "PO":
                            if (var17) {
                                var5.add(this.api.lang.getText("ITEM_DO_NOT_POSSESS", var13) + " <" + var10 + ">");
                            } else {
                                var5.add(this.api.lang.getText("ITEM_DO_POSSESS", var13) + " <" + var10 + ">");
                            }
                            break;
                        default:
                            var5.add((var8 <= 0 ? "" : this.api.lang.getText("ITEM_OR") + " ") + var12 + " " + var10 + " " + var13);
                    }
                }
            }
            var6++;
        }
        return var5;
    }*/

    private static boolean haveMorph(String c, Player p) {
        if (c.equalsIgnoreCase(""))
            return false;
        int morph = -1;
        try {
            morph = Integer.parseInt((c.contains("==") ? c.split("==")[1] : c.split("!=")[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (p.getMorphId() == morph)
            return c.contains("==");
        else
            return !c.contains("==");
    }

    private static boolean haveMetier(String c, Player p) {
        if (p.getMetiers() == null || p.getMetiers().isEmpty())
            return false;
        for (Entry<Integer, JobStat> entry : p.getMetiers().entrySet()) {
            if (entry.getValue() != null)
                return true;
        }
        return false;
    }

    private static boolean havePj(String c, Player p) {
        if (c.equalsIgnoreCase(""))
            return false;
        for (String s : c.split("\\|\\|")) {
            String[] k = s.split("==");
            int id;
            try {
                id = Integer.parseInt(k[1]);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if (p.getMetierByID(id) != null)
                return true;
        }
        return false;
    }

    //Avoir la qu�te en cours
    private static boolean haveQa(String req, Player player) {
        int id = Integer.parseInt((req.contains("==") ? req.split("==")[1] : req.split("!=")[1]));
        Quest q = Quest.getQuestById(id);
        if (q == null)
            return (!req.contains("=="));

        QuestPlayer qp = player.getQuestPersoByQuest(q);
        if (qp == null)
            return (!req.contains("=="));

        return !qp.isFinish() || (!req.contains("=="));

    }

    // �tre � l'�tape id. Elle ne doit pas �tre valid� et celle d'avant doivent l'�tre.
    private static boolean haveQEt(String req, Player player) {
        int id = Integer.parseInt((req.contains("==") ? req.split("==")[1] : req.split("!=")[1]));
        QuestStep qe = QuestStep.getQuestStepById(id);
        if (qe != null) {
            Quest q = qe.getQuestData();
            if (q != null) {
                QuestPlayer qp = player.getQuestPersoByQuest(q);
                if (qp != null) {
                    QuestStep current = q.getCurrentQuestStep(qp);
                    if (current == null)
                        return false;
                    if (current.getId() == qe.getId())
                        return (req.contains("=="));
                }
            }
        }
        return false;
    }

    private static boolean haveTiT(String req, Player player) {
        if (req.contains("==")) {
            String split = req.split("==")[1];
            if (split.contains("&&")) {
                int item = Integer.parseInt(split.split("&&")[0]);
                int time = Integer.parseInt(split.split("&&")[1]);
                int item2 = Integer.parseInt(split.split("&&")[2]);
                if (player.hasItemTemplate(item2, 1)
                        && player.hasItemTemplate(item, 1)) {
                    long timeStamp = Long.parseLong(player.getItemTemplate(item, 1).getTxtStat().get(Constant.STATS_DATE));
                    if (System.currentTimeMillis() - timeStamp <= time)
                        return true;
                }
            }
        }
        return false;
    }

    private static boolean haveTi(String req, Player player) {
        if (req.contains("==")) {
            String split = req.split("==")[1];
            if (split.contains(",")) {
                String[] split2 = split.split(",");
                int item = Integer.parseInt(split2[0]);
                int time = Integer.parseInt(split2[1]) * 60 * 1000;
                if (player.hasItemTemplate(item, 1)) {
                    long timeStamp = Long.parseLong(player.getItemTemplate(item, 1).getTxtStat().get(Constant.STATS_DATE));
                    if (System.currentTimeMillis() - timeStamp > time)
                        return true;
                }
            }
        }
        return false;
    }

    private static boolean haveCe(String req, Player player) {
        java.util.Map<Integer, Couple<Integer, Integer>> dopeuls = Action.getDopeul();
        GameMap map = player.getCurMap();
        if (dopeuls.containsKey((int) map.getId())) {
            Couple<Integer, Integer> couple = dopeuls.get((int) map.getId());
            if (couple == null)
                return false;

            int IDmob = couple.first;
            int certificat = Constant.getCertificatByDopeuls(IDmob);

            if (certificat == -1)
                return false;

            if (player.hasItemTemplate(certificat, 1)) {
                String txt = player.getItemTemplate(certificat, 1).getTxtStat().get(Constant.STATS_DATE);
                if (txt.contains("#"))
                    txt = txt.split("#")[3];
                long timeStamp = Long.parseLong(txt);
                return System.currentTimeMillis() - timeStamp > 86400000;
            } else
                return true;
        }
        return false;
    }

    // Avoir la qu�te en cours.
    private static boolean haveQE(String req, Player player) {
        if (player == null)
            return false;
        int id = Integer.parseInt((req.contains("==") ? req.split("==")[1] : req.split("!=")[1]));
        QuestPlayer qp = player.getQuestPersoByQuestId(id);
        if (req.contains("==")) {
            return qp != null && !qp.isFinish();
        } else {
            return qp == null || qp.isFinish();
        }
    }

    private static boolean haveQT(String req, Player player) {
        int id = Integer.parseInt((req.contains("==") ? req.split("==")[1] : req.split("!=")[1]));

        QuestPlayer quest = player.getQuestPersoByQuestId(id);
        if (req.contains("=="))
            return (quest != null && quest.isFinish());
        else
            return (quest == null || !quest.isFinish());
    }

    private static boolean haveNPC(String req, Player perso) {
        switch (perso.getCurMap().getId()) {
            case 9052:
                if (perso.getCurCell().getId() == 268
                        && perso.get_orientation() == 7)//TODO
                    return true;
            case 8905:
                ArrayList<Integer> cell = new ArrayList<Integer>();
                for (String i : "168,197,212,227,242,183,213,214,229,244,245,259".split("\\,"))
                    cell.add(Integer.parseInt(i));
                if (cell.contains(perso.getCurCell().getId()))
                    return true;
        }
        return false;
    }

    private static boolean haveRO(String condition, Player player) {
        try {
            for (String cond : condition.split("&&")) {
                String[] split = cond.split("==")[1].split(",");
                int id = Integer.parseInt(split[0]), qua = Integer.parseInt(split[1]);

                if (player.hasItemTemplate(id, qua)) {
                    player.removeByTemplateID(id, qua);
                    return true;
                } else {
                    SocketManager.GAME_SEND_Im_PACKET(player, "14");
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean haveRA(String condition, Player player) {
        try {
            for (String cond : condition.split("&&")) {
                String[] split = cond.split("==")[1].split(",");
                int id = Integer.parseInt(split[0]), qua = Integer.parseInt(split[1]);

                if (!player.hasItemTemplate(id, qua))
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private static String havePO(String cond, Player perso)//On remplace les PO par leurs valeurs si possession de l'item
    {

        boolean Jump = false;
        boolean ContainsPO = false;
        boolean CutFinalLenght = true;
        String copyCond = "";
        int finalLength = 0;

        if (cond.contains("&&")) {
            for (String cur : cond.split("&&")) {
                if (cond.contains("==")) {
                    for (String cur2 : cur.split("==")) {
                        if (cur2.contains("PO")) {
                            ContainsPO = true;
                            continue;
                        }
                        if (Jump) {
                            copyCond += cur2;
                            Jump = false;
                            continue;
                        }
                        if (!cur2.contains("PO") && !ContainsPO) {
                            copyCond += cur2 + "==";
                            Jump = true;
                            continue;
                        }
                        if (cur2.contains("!="))
                            continue;
                        ContainsPO = false;
                        if (perso.hasItemTemplate(Integer.parseInt(cur2), 1)) {
                            copyCond += Integer.parseInt(cur2) + "=="
                                    + Integer.parseInt(cur2);
                        } else {
                            copyCond += Integer.parseInt(cur2) + "==" + 0;
                        }
                    }
                }
                if (cond.contains("!=")) {
                    for (String cur2 : cur.split("!=")) {
                        if (cur2.contains("PO")) {
                            ContainsPO = true;
                            continue;
                        }
                        if (Jump) {
                            copyCond += cur2;
                            Jump = false;
                            continue;
                        }
                        if (!cur2.contains("PO") && !ContainsPO) {
                            copyCond += cur2 + "!=";
                            Jump = true;
                            continue;
                        }
                        if (cur2.contains("=="))
                            continue;
                        ContainsPO = false;
                        if (perso.hasItemTemplate(Integer.parseInt(cur2), 1)) {
                            copyCond += Integer.parseInt(cur2) + "!="
                                    + Integer.parseInt(cur2);
                        } else {
                            copyCond += Integer.parseInt(cur2) + "!=" + 0;
                        }
                    }
                }
                copyCond += "&&";
            }
        } else if (cond.contains("||")) {
            for (String cur : cond.split("\\|\\|")) {
                if (cond.contains("==")) {
                    for (String cur2 : cur.split("==")) {
                        if (cur2.contains("PO")) {
                            ContainsPO = true;
                            continue;
                        }
                        if (Jump) {
                            copyCond += cur2;
                            Jump = false;
                            continue;
                        }
                        if (!cur2.contains("PO") && !ContainsPO) {
                            copyCond += cur2 + "==";
                            Jump = true;
                            continue;
                        }
                        if (cur2.contains("!="))
                            continue;
                        ContainsPO = false;
                        if (perso.hasItemTemplate(Integer.parseInt(cur2), 1)) {
                            copyCond += Integer.parseInt(cur2) + "=="
                                    + Integer.parseInt(cur2);
                        } else {
                            copyCond += Integer.parseInt(cur2) + "==" + 0;
                        }
                    }
                }
                if (cond.contains("!=")) {
                    for (String cur2 : cur.split("!=")) {
                        if (cur2.contains("PO")) {
                            ContainsPO = true;
                            continue;
                        }
                        if (Jump) {
                            copyCond += cur2;
                            Jump = false;
                            continue;
                        }
                        if (!cur2.contains("PO") && !ContainsPO) {
                            copyCond += cur2 + "!=";
                            Jump = true;
                            continue;
                        }
                        if (cur2.contains("=="))
                            continue;
                        ContainsPO = false;
                        if (perso.hasItemTemplate(Integer.parseInt(cur2), 1)) {
                            copyCond += Integer.parseInt(cur2) + "!="
                                    + Integer.parseInt(cur2);
                        } else {
                            copyCond += Integer.parseInt(cur2) + "!=" + 0;
                        }
                    }
                }
                copyCond += "||";
            }
        } else {
            CutFinalLenght = false;
            if (cond.contains("==")) {
                for (String cur : cond.split("==")) {
                    if (cur.contains("PO"))
                        continue;
                    if (cur.contains("!="))
                        continue;
                    if (perso.hasItemTemplate(Integer.parseInt(cur), 1))
                        copyCond += Integer.parseInt(cur) + "=="
                                + Integer.parseInt(cur);
                    else
                        copyCond += Integer.parseInt(cur) + "==" + 0;
                }
            }
            if (cond.contains("!=")) {
                for (String cur : cond.split("!=")) {
                    if (cur.contains("PO"))
                        continue;
                    if (cur.contains("=="))
                        continue;
                    if (perso.hasItemTemplate(Integer.parseInt(cur), 1))
                        copyCond += Integer.parseInt(cur) + "!="
                                + Integer.parseInt(cur);
                    else
                        copyCond += Integer.parseInt(cur) + "!=" + 0;
                }
            }
        }
        if (CutFinalLenght) {
            finalLength = (copyCond.length() - 2);//On retire les deux derniers carract�res (|| ou &&)
            copyCond = copyCond.substring(0, finalLength);
        }
        return copyCond;
    }

    public static String canPN(String cond, Player perso)//On remplace le PN par 1 et si le nom correspond == 1 sinon == 0
    {
        String copyCond = "";
        for (String cur : cond.split("==")) {
            if (cur.contains("PN")) {
                copyCond += "1==";
                continue;
            }
            if (perso.getName().toLowerCase().compareTo(cur) == 0)
                copyCond += "1";
            else
                copyCond += "0";
        }
        return copyCond;
    }

    public static String canPJ(String cond, Player perso)//On remplace le PJ par 1 et si le metier correspond == 1 sinon == 0
    {
        String copyCond = "";
        if (cond.contains("==")) {
            String[] cur = cond.split("==");
            if (perso.getMetierByID(Integer.parseInt(cur[1])) != null)
                copyCond = "1==1";
            else
                copyCond = "1==0";
        } else if (cond.contains(">")) {
            if (cond.contains("||")) {
                for (String cur : cond.split("\\|\\|")) {
                    if (!cur.contains(">"))
                        continue;
                    String[] _cur = cur.split(">");
                    if (!_cur[1].contains(","))
                        continue;
                    String[] m = _cur[1].split(",");
                    JobStat js = perso.getMetierByID(Integer.parseInt(m[0]));
                    if (!copyCond.equalsIgnoreCase(""))
                        copyCond += "||";
                    if (js != null)
                        copyCond += js.get_lvl() + ">" + m[1];
                    else
                        copyCond += "1==0";
                }
            } else {
                String[] cur = cond.split(">");
                String[] m = cur[1].split(",");
                JobStat js = perso.getMetierByID(Integer.parseInt(m[0]));
                if (js != null)
                    copyCond = js.get_lvl() + ">" + m[1];
                else
                    copyCond = "1==0";
            }
        }
        return copyCond;
    }

    public static String haveJOB(String cond, Player perso) {
        String copyCond = "";
        if (perso.getMetierByID(Integer.parseInt(cond.split("==")[1])) != null)
            copyCond = "1==1";
        else
            copyCond = "0==1";
        return copyCond;
    }

    public boolean stackIfSimilar(GameObject obj, GameObject newObj, boolean stackIfSimilar) {
        switch(obj.getTemplate().getId()) {
            case 10275:
            case 8378:
                if(obj.getTemplate().getId() == newObj.getTemplate().getId())
                    return false;
        }

        if( ArrayUtils.contains( Constant.ITEM_TYPE_WITH_RARITY, newObj.getTemplate().getType() ) ) {
            return obj.getTemplate().getId() == newObj.getTemplate().getId() && stackIfSimilar
                    && obj.getStats().isSameStats(newObj.getStats())
                    && obj.isSameStats(newObj)
                    && obj.getMimibiote() == newObj.getMimibiote()
                    && (obj.getRarity() == newObj.getRarity())
                    && !Constant.isIncarnationWeapon(newObj.getTemplate().getId())
                    && newObj.getTemplate().getType() != Constant.ITEM_TYPE_CERTIFICAT_CHANIL
                    && newObj.getTemplate().getType() != Constant.ITEM_TYPE_FAMILIER
                    && !(newObj.getTemplate().getType() == Constant.ITEM_TYPE_PIERRE_AME_PLEINE || newObj.getTemplate().getType() == Constant.ITEM_TYPE_PIERRE_AME_PLEINE_BOSS || newObj.getTemplate().getType() == Constant.ITEM_TYPE_PIERRE_AME_PLEINE_ARCHI)
                    && newObj.getTemplate().getType() != Constant.ITEM_TYPE_OBJET_ELEVAGE
                    && newObj.getTemplate().getType() != Constant.ITEM_TYPE_CERTIF_MONTURE
                    && newObj.getTemplate().getType() != Constant.ITEM_TYPE_OBJET_VIVANT
                    && (newObj.getTemplate().getType() != Constant.ITEM_TYPE_QUETES || Constant.isFlacGelee(obj.getTemplate().getId()) || Constant.isDoplon(obj.getTemplate().getId()))
                    && obj.getPosition() == Constant.ITEM_POS_NO_EQUIPED;
        }
        return obj.getTemplate().getId() == newObj.getTemplate().getId() && stackIfSimilar
                && obj.getStats().isSameStats(newObj.getStats())
                && obj.isSameStats(newObj)
                && !Constant.isIncarnationWeapon(newObj.getTemplate().getId())
                && newObj.getTemplate().getType() != Constant.ITEM_TYPE_CERTIFICAT_CHANIL
                && newObj.getTemplate().getType() != Constant.ITEM_TYPE_FAMILIER
                && !(newObj.getTemplate().getType() == Constant.ITEM_TYPE_PIERRE_AME_PLEINE || newObj.getTemplate().getType() == Constant.ITEM_TYPE_PIERRE_AME_PLEINE_BOSS || newObj.getTemplate().getType() == Constant.ITEM_TYPE_PIERRE_AME_PLEINE_ARCHI)
                && newObj.getTemplate().getType() != Constant.ITEM_TYPE_OBJET_ELEVAGE
                && newObj.getTemplate().getType() != Constant.ITEM_TYPE_CERTIF_MONTURE
                && newObj.getTemplate().getType() != Constant.ITEM_TYPE_OBJET_VIVANT
                && (newObj.getTemplate().getType() != Constant.ITEM_TYPE_QUETES || Constant.isFlacGelee(obj.getTemplate().getId()) || Constant.isDoplon(obj.getTemplate().getId()))
                && obj.getPosition() == Constant.ITEM_POS_NO_EQUIPED;
    }
    public boolean stackIfSimilar2(GameObject obj, GameObject newObj, boolean stackIfSimilar) {
        switch(obj.getTemplate().getId()) {
            case 10275:
            case 8378:
                if(obj.getTemplate().getId() == newObj.getTemplate().getId())
                    return false;
        }
        String stats1 = obj.parseStatsString();
        String stats2 = newObj.parseStatsString();

        if(obj.getTemplate().getId() == newObj.getTemplate().getId()
                && stackIfSimilar
                && obj.getStats().isSameStats(newObj.getStats())
                && obj.isSameStats(newObj)
                && obj.isSametxtStats(newObj)
                && obj.getRarity() == newObj.getRarity()
                && obj.getGuid() != newObj.getGuid()
                && obj.getMimibiote() == newObj.getMimibiote()
                && stats1.equals(stats2)
                && !Constant.isIncarnationWeapon(newObj.getTemplate().getId())
                && newObj.getTemplate().getType() != Constant.ITEM_TYPE_CERTIFICAT_CHANIL
                && newObj.getTemplate().getType() != Constant.ITEM_TYPE_FAMILIER
                && !(newObj.getTemplate().getType() == Constant.ITEM_TYPE_PIERRE_AME_PLEINE || newObj.getTemplate().getType() == Constant.ITEM_TYPE_PIERRE_AME_PLEINE_BOSS || newObj.getTemplate().getType() == Constant.ITEM_TYPE_PIERRE_AME_PLEINE_ARCHI)
                && newObj.getTemplate().getType() != Constant.ITEM_TYPE_OBJET_ELEVAGE
                && !Constant.isCertificatDopeuls(newObj.getTemplate().getId())
                && newObj.getTemplate().getType() != Constant.ITEM_TYPE_CERTIF_MONTURE
                && newObj.getTemplate().getType() != Constant.ITEM_TYPE_OBJET_VIVANT
                &&(newObj.getTemplate().getType() != Constant.ITEM_TYPE_QUETES || !Constant.isFlacGelee(obj.getTemplate().getId()) || !Constant.isDoplon(obj.getTemplate().getId()))
                && obj.getPosition() == Constant.ITEM_POS_NO_EQUIPED)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
