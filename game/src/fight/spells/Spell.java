package fight.spells;

import java.util.HashMap;
import java.util.Map;

public class Spell {

    private String name;
    private int spellID;
    private int spriteID;
    private String spriteInfos;
    private Map<Integer, SpellGrade> SpellGrade = new HashMap<Integer, SpellGrade>();

    /*private ArrayList<Integer> onHitEffects = new ArrayList<Integer>();
    private ArrayList<Integer> effectTargets = new ArrayList<Integer>();
    private ArrayList<Integer> CCeffectTargets = new ArrayList<Integer>();*/

    private int type, duration;

    public Spell(int aspellID, String name, int aspriteID,
                 String aspriteInfos, int type, int duration) {
        this.spellID = aspellID;
        this.name = name;
        this.spriteID = aspriteID;
        this.spriteInfos = aspriteInfos;
        this.duration = duration;
        this.type = type;
    }

    public void setInfos(int aspriteID, String aspriteInfos, int type, int duration) {
        spriteID = aspriteID;
        spriteInfos = aspriteInfos;
        this.type = type;
        this.duration = duration;
    }

    public int getSpriteID() {
        return spriteID;
    }

    public String getSpriteInfos() {
        return spriteInfos;
    }

    public int getSpellID() {
        return spellID;
    }

    public SpellGrade getStatsByLevel(int lvl) {
        return SpellGrade.get(lvl);
    }

    public String getName() {
        return name;
    }

    public Map<Integer, SpellGrade> getSortsStats() {
        return SpellGrade;
    }

    public SpellGrade getSpellGrade(int lvl) {
        return SpellGrade.get(lvl);
    }

    public void addSortStats(Integer lvl, SpellGrade stats) {
        if (SpellGrade.get(lvl) != null)
            SpellGrade.remove(lvl);
        SpellGrade.put(lvl, stats);
    }

    public int getType() {
        return type;
    }


    public void setType(int type) {
        this.type = type;
    }

    public int getDuration() {
        return duration;
    }

}
