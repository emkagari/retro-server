package client;

import fight.spells.EffectConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class Classe
{
    private int id;
    private String name;
    private int MapInit;
    private int CellInit;
    private int pdv;
    private ArrayList<Integer> _gfxs = new ArrayList<>(3);
    private ArrayList<Integer> _size = new ArrayList<>(3);
    private ArrayList<BoostStat> _boostForce = new ArrayList<>();
    private ArrayList<BoostStat> _boostIntel = new ArrayList<>();
    public ArrayList<BoostStat> _boostVita = new ArrayList<>();
    private ArrayList<BoostStat> _boostSage = new ArrayList<>();
    private ArrayList<BoostStat> _boostAgi = new ArrayList<>();
    private  ArrayList<BoostStat> _boostChance = new ArrayList<>();
    private HashMap<Integer, Integer> _stats = new HashMap<>();
    private HashMap<Integer, Integer> _sorts = new HashMap<>();

    public Classe(int id, String name, String gfxs, String size, int MapInit, int CellInit, int pdv, String boostVita, String
                  boostSagesse, String boostForce, String boostIntel, String boostChance, String boostAgil, String stats
    , String sorts)
    {
        this.id = id;
        this.name = name;
        this.MapInit = MapInit;
        this.CellInit = CellInit;
        this.pdv = pdv;
        for (String s : gfxs.split(",")) {
            try {
                this._gfxs.add(Integer.parseInt(s));
            } catch (Exception ignored) {
            }
        }
        for (String s : size.split(",")) {
            try {
                this._size.add(Integer.parseInt(s));
            } catch (Exception ignored) {
            }
        }

        addBoostStat(boostVita, this._boostVita);
        addBoostStat(boostSagesse, this._boostSage);
        addBoostStat(boostForce, this._boostForce);
        addBoostStat(boostIntel, this._boostIntel);
        addBoostStat(boostAgil, this._boostAgi);
        addBoostStat(boostChance, this._boostChance);
        for (String s : stats.split("\\|")) {
            try {
                this._stats.put(Integer.parseInt(s.split(",")[0]), Integer.parseInt(s.split(",")[1]));
            } catch (Exception ignored) {
            }
        }
        for (String s : sorts.split("\\|")) {
            try {
                this._sorts.put(Integer.parseInt(s.split(",")[1]), Integer.parseInt(s.split(",")[0]));
            } catch (Exception ignored) {
            }
        }
    }
    public int getId()
    {
        return this.id;
    }
    public static final BoostStat BoostDefecto = new BoostStat(0, 1, 1);
    public static class BoostStat {

        public int inicio;
        public int cost;
        public int puntos;

        public BoostStat(int inicio, int cost, int puntos)
        {
            this.inicio = inicio;
            this.cost = cost;
            this.puntos = puntos;
        }
    }
    private void addBoostStat(String sBoost,ArrayList<BoostStat> boost) {
        for (String s : sBoost.split("\\|")) {
            try {
                String[] ss = s.split(",");
                int inicio = Integer.parseInt(ss[0]);
                int coste = Integer.parseInt(ss[1]);
                int puntos = 1;
                try {
                    puntos = Integer.parseInt(ss[2]);
                } catch (Exception ignored) {
                }
                boost.add(new BoostStat(inicio, coste, puntos));
            } catch (Exception ignored) {
            }
        }
    }

    public BoostStat getBoostStat(int statID, int valorStat) {
        ArrayList<BoostStat> boosts = new ArrayList<BoostStat>();

        switch (statID) {
            case EffectConstant.STATS_ADD_VITA :
                boosts = this._boostVita;
                break;
            case EffectConstant.STATS_ADD_FORC :
                boosts =this._boostForce;
                break;
            case EffectConstant.STATS_ADD_INTE :
                boosts =this._boostIntel;
                break;
            case EffectConstant.STATS_ADD_AGIL :
                boosts =this._boostAgi;
                break;
            case EffectConstant.STATS_ADD_CHAN :
                boosts =this._boostChance;
                break;
            case EffectConstant.STATS_ADD_SAGE :
                boosts =this._boostSage;
                break;
            default :
                boosts = new ArrayList<BoostStat>();
                break;
        };
        BoostStat boost = BoostDefecto;
        int temp = -1;
        for (BoostStat b : boosts) {
            if (b.inicio >= (temp + 1) && b.inicio <= valorStat) {
                temp = b.inicio;
                boost = b;
            }
        }
        return boost;
    }

    public Boolean aprenderHechizo(Player perso, int nivel) {
    AtomicReference<Boolean> loop = new AtomicReference<>(false);
    this._sorts.forEach((key, value) ->{
        if (value == nivel) {
            perso.learnSpell(key, 1);
            loop.set(true);
        }
    });
    return loop.get();
    }

    public int getGfxs(int index)
    {
        try
        {
            return this._gfxs.get(index);
        }
        catch (Exception e)
        {
            return id * 10 + 3;
        }
    }

    public int getTallas(int index)
    {
        try
        {
            return this._size.get(index);
    }
        catch (Exception e)
        {
            return 100;
        }
    }
}