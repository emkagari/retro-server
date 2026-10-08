package job.maging;

import game.world.World;

import java.util.*;

public class Rune {

    public final static List<Rune> runes = new ArrayList<>();

    public static Rune getRuneById(int id) {
        for(Rune rune : runes)
            if(rune.getId() == id)
                return rune;
        return null;
    }

    public static Rune getRuneByCharacteristic(short stat) {
        for(Rune rune : runes)
            if(rune.getCharacteristic() == stat)
                return rune;
        return null;
    }

    public static Rune getRuneByCharacteristicAndByWeight(short stat) {
        Rune valid = null;
        float weight = 999;
        for(Rune rune : runes) {
            if (rune.getCharacteristic() == stat && weight > rune.getWeight()) {
                weight = rune.getWeight();
                valid = rune;
            }
        }
        return valid;
    }

    int id;
    private int characteristic;
    private String characteristicString;
    private float weight;
    private byte bonus;

    public Rune(int i, float weight, byte bonus) {
        this.id = i;
        this.weight = weight;
        this.bonus = bonus;
        this.setCharacteristic();
        this.setCharacteristicString();
        Rune.runes.add(this);
    }



    public int getId() {
        return id;
    }

    private void setCharacteristic() {
        this.characteristic = Integer.parseInt(World.world.getObjTemplate(this.id).getStrTemplate().split("#")[0], 16);
    }

    private void setCharacteristicString() {
        this.characteristicString = World.world.getObjTemplate(this.id).getStrTemplate().split("#")[0];
    }

    public String getCharacteristicString() {
        return characteristicString;
    }

    public int getCharacteristic() {
        return characteristic;
    }

    public float getWeight() {
        return weight;
    }

    public byte getBonus() {
        return bonus;
    }

    public byte[] getChance() {
        return this.weight <= 1 ? new byte[] {66, 34, 0} : new byte[] {43, 50, 7};
    }
}