package area;

import area.map.GameMap;

import java.util.ArrayList;

public class Area {

    public static int bontarians = 0, brakmarians = 0;

    private final int id, superArea;
    private int alignement, prismId;
    private ArrayList<SubArea> subAreas = new ArrayList<>();
    public boolean canbeCapturable = false;
    private final ArrayList<Integer> AreaCapturable = new ArrayList<>();
    {
        AreaCapturable.add(20);
        AreaCapturable.add(21);
        AreaCapturable.add(22);
        AreaCapturable.add(23);
        AreaCapturable.add(13);
        AreaCapturable.add(14);
        AreaCapturable.add(47);
    }

    public Area(int id, int superArea) {
        this.id = id;
        this.superArea = superArea;
        if(AreaCapturable.contains(this.id)) {
            canbeCapturable = true;
        }
    }

    public int getId() {
        return id;
    }

    public int getSuperArea() {
        return superArea;
    }

    public int getAlignement() {
        return alignement;
    }

    public void setAlignement(int alignement) {
        if (this.alignement == 1 && alignement == -1)
            bontarians--;
        else if (this.alignement == 2 && alignement == -1)
            brakmarians--;
        else if (this.alignement == -1 && alignement == 1)
            bontarians++;
        else if (this.alignement == -1 && alignement == 2)
            brakmarians++;
        this.alignement = alignement;
    }

    public int getPrismId() {
        return prismId;
    }

    public void setPrismId(int prismId) {
        this.prismId = prismId;
    }

    public void addSubArea(SubArea subArea) {
        this.subAreas.add(subArea);
    }

    public ArrayList<SubArea> getSubAreas() {
        return subAreas;
    }


    public ArrayList<GameMap> getMaps() {
        ArrayList<GameMap> maps = new ArrayList<>();
        for (SubArea subArea : this.subAreas)
            maps.addAll(subArea.getMaps());
        return maps;
    }
}