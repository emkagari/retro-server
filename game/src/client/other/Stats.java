package client.other;

import client.Player;
import entity.monster.Monster;
import fight.spells.EffectConstant;
import guild.Guild;
import kernel.Constant;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Stats {

    private Map<Integer, Integer> effects = new LinkedHashMap<>();

    public Stats(Map<Integer, Integer> stats) {
        this.effects = stats;
    }

    public Stats(boolean addBases, Player player) {
        if (addBases) {
            this.effects.put(EffectConstant.STATS_ADD_PA, player.getLevel() < 100 ? 6 : 7);
            this.effects.put(EffectConstant.STATS_ADD_PM, player.getLevel() == 200 ? 4 : 3);
            this.effects.put(EffectConstant.STATS_ADD_PROS, player.getClasse() == Constant.CLASS_ENUTROF ? 120 : 100);
            this.effects.put(EffectConstant.STATS_ADD_PODS, 5000);
            this.effects.put(EffectConstant.STATS_CREATURE, 1);
            this.effects.put(EffectConstant.STATS_ADD_INIT, 1);
        }
    }

    public Stats(Map<Integer, Integer> stats, boolean addBases, Player player) {


        this.effects = stats;
        if (addBases) {
            this.effects.put(EffectConstant.STATS_ADD_PA, player.getLevel() < 100 ? 6 : 7);
            this.effects.put(EffectConstant.STATS_ADD_PM, player.getLevel() == 200 ? 4 : 3);
            this.effects.put(EffectConstant.STATS_ADD_PROS, player.getClasse() == Constant.CLASS_ENUTROF ? 120 : 100);
            this.effects.put(EffectConstant.STATS_ADD_PODS, 5000);
            this.effects.put(EffectConstant.STATS_CREATURE, 1);
            this.effects.put(EffectConstant.STATS_ADD_INIT, 1);
        }
    }

    public Stats(Monster.MobGrade monster)
    {
        this.effects.put(EffectConstant.STATS_ADD_PA, monster.getPa());
        this.effects.put(EffectConstant.STATS_ADD_PM, monster.getPm());
        this.effects.put(EffectConstant.STATS_ADD_VITA, monster.getPdv());
        this.effects.put(EffectConstant.STATS_ADD_INIT, monster.getInit());
        this.effects.putAll(monster.getStats().getEffects());
    }

    public Stats(boolean a) { // Parchotage
        this.effects.put(EffectConstant.STATS_ADD_VITA, 0);
        this.effects.put(EffectConstant.STATS_ADD_SAGE, 0);
        this.effects.put(EffectConstant.STATS_ADD_INTE, 0);
        this.effects.put(EffectConstant.STATS_ADD_FORC, 0);
        this.effects.put(EffectConstant.STATS_ADD_CHAN, 0);
        this.effects.put(EffectConstant.STATS_ADD_AGIL, 0);
    }
    public Stats() {
        this.effects = new HashMap<Integer, Integer>();
    }

    public Stats(Guild guild) { // Stats collector in fight
        if(guild != null) {
            this.effects.put(EffectConstant.STATS_ADD_SAGE, guild.getStats(EffectConstant.STATS_ADD_SAGE));
            this.effects.put(EffectConstant.STATS_ADD_FORC, guild.getLvl());
            this.effects.put(EffectConstant.STATS_ADD_INTE, guild.getLvl());
            this.effects.put(EffectConstant.STATS_ADD_CHAN, guild.getLvl());
            this.effects.put(EffectConstant.STATS_ADD_AGIL, guild.getLvl());
            int floor = (int) Math.floor(guild.getLvl() / 2);
            this.effects.put(EffectConstant.STATS_ADD_RP_NEU, floor);
            this.effects.put(EffectConstant.STATS_ADD_RP_FEU, floor);
            this.effects.put(EffectConstant.STATS_ADD_RP_EAU, floor);
            this.effects.put(EffectConstant.STATS_ADD_RP_AIR, floor);
            this.effects.put(EffectConstant.STATS_ADD_RP_TER, floor);
            this.effects.put(EffectConstant.STATS_ADD_AFLEE, floor);
            this.effects.put(EffectConstant.STATS_ADD_MFLEE, floor);
        }
    }

    public Map<Integer, Integer> getEffects() {
        return this.effects;
    }

    public int get(int id) {
        return this.effects.get(id) == null ? 0 : this.effects.get(id);
    }

    public int addOneStat(int id, int val) {
        if (id == 121) id = EffectConstant.STATS_ADD_DOMA;
            if (this.effects.get(id) == null || this.effects.get(id) == 0) {
                this.effects.put(id, val);
            } else {
                int newVal = (this.effects.get(id) + val);
                if (newVal <= 0) {
                    this.effects.remove(id);
                    return 0;
                } else this.effects.put(id, newVal);
            }
        if(this.effects.containsKey(id)) {
            return this.effects.get(id);
        }
        else{
            return 0;
        }
    }
    public boolean isSameStats(Stats other) { //Compare uniquement les stats de l'item sans les degat de l'arme

        if(this.effects.size() == 0 && other.getEffects().size() == 0)
        {
            return true;
        }
        else {
            for (Entry<Integer, Integer> entry : this.effects.entrySet()) {
                //Si la stat n'existe pas dans l'autre map
                if (other.getEffects().get(entry.getKey()) == null)
                    return false;
                //Si la stat existe mais n'a pas la m�me valeur
                if (other.getEffects().get(entry.getKey()).compareTo(entry.getValue()) != 0)
                    return false;
            }
            for (Entry<Integer, Integer> entry : other.getEffects().entrySet()) {
                //Si la stat n'existe pas dans l'autre map
                if (this.effects.get(entry.getKey()) == null)
                    return false;
                //Si la stat existe mais n'a pas la m�me valeur
                if (this.effects.get(entry.getKey()).compareTo(entry.getValue()) != 0)
                    return false;
            }
            return true;
        }
    }

    public String parseToItemSetStats() {
        StringBuilder str = new StringBuilder();
        if (this.effects.isEmpty())
            return "";
        for (Entry<Integer, Integer> entry : this.effects.entrySet()) {
            if(entry.getValue() > 0) {
                if (str.length() > 0)
                    str.append(",");
                str.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue())).append("#0#0");
            }
        }
        return str.toString();
    }

    public int getEffect(int id) {

        int val = this.effects.get(id) == null ? 0 : this.effects.get(id);

        switch (id) {
            case EffectConstant.STATS_ADD_SAGE:
                if (this.effects.get(EffectConstant.STATS_REM_SAGE) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_SAGE);
                break;
            case EffectConstant.STATS_ADD_AFLEE:
                if (this.effects.get(EffectConstant.STATS_REM_AFLEE) != null)
                    val -= getEffect(EffectConstant.STATS_REM_AFLEE);
                if (this.effects.get(EffectConstant.STATS_ADD_SAGE) != null)
                    val += getEffect(EffectConstant.STATS_ADD_SAGE) / 4;
                break;
            case EffectConstant.STATS_ADD_MFLEE:
                if (this.effects.get(EffectConstant.STATS_REM_MFLEE) != null)
                    val -= getEffect(EffectConstant.STATS_REM_MFLEE);
                if (this.effects.get(EffectConstant.STATS_ADD_SAGE) != null)
                    val += getEffect(EffectConstant.STATS_ADD_SAGE) / 4;
                break;
            case EffectConstant.STATS_ADD_INIT:
                if (this.effects.get(EffectConstant.STATS_REM_INIT) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_INIT);
                break;
            case EffectConstant.STATS_ADD_CC:
                if (this.effects.get(EffectConstant.STATS_REM_CC) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_CC);
                break;
            case EffectConstant.STATS_CREATURE:
                if (this.effects.get(EffectConstant.STATS_REM_INVO) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_INVO);
                break;
            case EffectConstant.STATS_RETDOM:
                if (this.effects.get(EffectConstant.STATS_REM_RENVOI) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_RENVOI);
                break;
            case EffectConstant.STATS_ADD_SOIN:
                if (this.effects.get(EffectConstant.STATS_REM_SOIN) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_SOIN);
                break;
            case EffectConstant.STATS_TRAPPER:
                if (this.effects.get(EffectConstant.STATS_REM_TRAPPER) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_TRAPPER);
                break;
            case EffectConstant.STATS_TRAPDOM:
                if (this.effects.get(EffectConstant.STATS_REM_TRAPDOM) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_TRAPDOM);
                break;
            case EffectConstant.STATS_ADD_AGIL:
                if (this.effects.get(EffectConstant.STATS_REM_AGIL) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_AGIL);
                break;
            case EffectConstant.STATS_ADD_FORC:
                if (this.effects.get(EffectConstant.STATS_REM_FORC) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_FORC);
                break;
            case EffectConstant.STATS_ADD_CHAN:
                if (this.effects.get(EffectConstant.STATS_REM_CHAN) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_CHAN);
                break;
            case EffectConstant.STATS_ADD_INTE:
                if (this.effects.get(EffectConstant.STATS_REM_INTE) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_INTE);
                break;
            case EffectConstant.STATS_ADD_PA:
                if (this.effects.get(EffectConstant.STATS_ADD_PA2) != null)
                    val += this.effects.get(EffectConstant.STATS_ADD_PA2);
                if (this.effects.get(EffectConstant.STATS_REM_PA) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_PA);
                if (this.effects.get(EffectConstant.STATS_REM_PA2) != null)//Non esquivable
                    val -= this.effects.get(EffectConstant.STATS_REM_PA2);
                if (this.effects.get(EffectConstant.STATS_REM_PA3) != null)//Non esquivable
                    val -= this.effects.get(EffectConstant.STATS_REM_PA3);
                break;
            case EffectConstant.STATS_ADD_PM:
                if (this.effects.get(EffectConstant.STATS_ADD_PM2) != null)
                    val += this.effects.get(EffectConstant.STATS_ADD_PM2);
                if (this.effects.get(EffectConstant.STATS_REM_PM) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_PM);
                if (this.effects.get(EffectConstant.STATS_REM_PM2) != null)//Non esquivable
                    val -= this.effects.get(EffectConstant.STATS_REM_PM2);
                break;
            case EffectConstant.STATS_ADD_EC:
                if (this.effects.get(EffectConstant.STATS_ADD_EC) != null)
                    val = this.effects.get(EffectConstant.STATS_ADD_EC);
                break;
            case EffectConstant.STATS_ADD_PO:
                if (this.effects.get(EffectConstant.STATS_REM_PO) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_PO);
                break;
            case EffectConstant.STATS_ADD_VITA:
                if (this.effects.get(EffectConstant.STATS_REM_VITA) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_VITA);
                break;
            case EffectConstant.STATS_ADD_VIE:
                val = EffectConstant.STATS_ADD_VIE;
                break;
            case EffectConstant.STATS_ADD_DOMA:
                if (this.effects.get(EffectConstant.STATS_REM_DOMA) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_DOMA);
                break;
            case EffectConstant.STATS_ADD_DOMA2:
                if (this.effects.get(EffectConstant.STATS_ADD_DOMA2) != null)
                    val += this.effects.get(EffectConstant.STATS_ADD_DOMA2);
                break;
            case EffectConstant.STATS_ADD_PODS:
                if (this.effects.get(EffectConstant.STATS_REM_PODS) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_PODS);
                break;
            case EffectConstant.STATS_ADD_PROS:
                if (this.effects.get(EffectConstant.STATS_REM_PROS) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_PROS);
                break;
            case EffectConstant.STATS_ADD_R_TER:
                if (this.effects.get(EffectConstant.STATS_REM_R_TER) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_R_TER);
                break;
            case EffectConstant.STATS_ADD_R_EAU:
                if (this.effects.get(EffectConstant.STATS_REM_R_EAU) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_R_EAU);
                break;
            case EffectConstant.STATS_ADD_R_AIR:
                if (this.effects.get(EffectConstant.STATS_REM_R_AIR) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_R_AIR);
                break;
            case EffectConstant.STATS_ADD_R_FEU:
                if (this.effects.get(EffectConstant.STATS_REM_R_FEU) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_R_FEU);
                break;
            case EffectConstant.STATS_ADD_R_NEU:
                if (this.effects.get(EffectConstant.STATS_REM_R_NEU) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_R_NEU);
                break;
            case EffectConstant.STATS_ADD_RP_TER:
                if (this.effects.get(EffectConstant.STATS_REM_RP_TER) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_RP_TER);
                break;
            case EffectConstant.STATS_ADD_RP_EAU:
                if (this.effects.get(EffectConstant.STATS_REM_RP_EAU) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_RP_EAU);
                break;
            case EffectConstant.STATS_ADD_RP_AIR:
                if (this.effects.get(EffectConstant.STATS_REM_RP_AIR) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_RP_AIR);
                break;
            case EffectConstant.STATS_ADD_RP_FEU:
                if (this.effects.get(EffectConstant.STATS_REM_RP_FEU) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_RP_FEU);
                break;
            case EffectConstant.STATS_ADD_RP_NEU:
                if (this.effects.get(EffectConstant.STATS_REM_RP_NEU) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_RP_NEU);
                break;
            case EffectConstant.STATS_ADD_FINALDMG:
                if (this.effects.get(EffectConstant.STATS_REM_FINALDMG) != null)
                    val -= this.effects.get(EffectConstant.STATS_REM_FINALDMG);
                break;
            case EffectConstant.STATS_ADD_MAITRISE:
                if (this.effects.get(EffectConstant.STATS_ADD_MAITRISE) != null)
                    val = this.effects.get(EffectConstant.STATS_ADD_MAITRISE);
                break;
            case EffectConstant.STATS_SPELL_ADD_PO:
                if(this.effects.get(EffectConstant.STATS_SPELL_ADD_PO) != null)
                    val = this.effects.get(EffectConstant.STATS_SPELL_ADD_PO);
                break;
            case EffectConstant.STATS_SPELL_REM_PA:
                if(this.effects.get(EffectConstant.STATS_SPELL_REM_PA) != null)
                    val = this.effects.get(EffectConstant.STATS_SPELL_REM_PA);
                break;
            case EffectConstant.STATS_SPELL_ADD_LAUNCH:
                if(this.effects.get(EffectConstant.STATS_SPELL_ADD_LAUNCH) != null)
                    val = this.effects.get(EffectConstant.STATS_SPELL_ADD_LAUNCH);
                break;
            case EffectConstant.STATS_SPELL_ADD_PER_TARGET:
                if(this.effects.get(EffectConstant.STATS_SPELL_ADD_PER_TARGET) != null)
                    val = this.effects.get(EffectConstant.STATS_SPELL_ADD_PER_TARGET);
                break;
        }

        return val;
    }

    public static Stats cumulStat(Stats s1, Stats s2) {
        HashMap<Integer, Integer> effets = new HashMap<>();
        for (int a = 0; a <= Constant.MAX_EFFECTS_ID; a++) {
            if (s1.effects.get(a) == null && s2.effects.get(a) == null)
                continue;

            int som = 0;
            if (s1.effects.get(a) != null)
                som += s1.effects.get(a);
            if (s2.effects.get(a) != null)
                som += s2.effects.get(a);

            effets.put(a, som);
        }
        return new Stats(effets, false, null);
    }

    public static Stats cumulStatFight(Stats s1, Stats s2) {
        HashMap<Integer, Integer> effets = new HashMap<>();
        for (int a = 0; a <= Constant.MAX_EFFECTS_ID; a++) {
            if ((s1.effects.get(a) == null || s1.effects.get(a) == 0)
                    && (s2.effects.get(a) == null || s2.effects.get(a) == 0))
                continue;
            int som = 0;
            if (s1.effects.get(a) != null)
                som += s1.effects.get(a);
            if (s2.effects.get(a) != null)
                som += s2.effects.get(a);
            effets.put(a, som);
        }
        return new Stats(effets, false, null);
    }

    public Map<Integer, Integer> getMap() {
        return this.effects;
    }

    public void equilibreStat(int statadd, int statrem, Player player ,Map<String, String> fullMorph , String test) {
        if (player.getTotalStats().getEffect(statadd) == Integer.parseInt(fullMorph.get(test)))
        {}
        else if (player.getTotalStats().getEffect(statadd) > Integer.parseInt(fullMorph.get(test))) {
            this.addOneStat(statrem, player.getTotalStats().getEffect(statadd) - Integer.parseInt(fullMorph.get(test)));
        } else {
            this.addOneStat(statadd, Integer.parseInt(fullMorph.get(test)) - player.getTotalStats().getEffect(statadd));
        }
    }
}
