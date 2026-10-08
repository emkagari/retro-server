package other;


public class QuickSets {
    private int id;
    private int playerId;
    private int nb;
    private String name;
    private String objects;
    private int icon;

    public QuickSets(int id, int playerId, int nb, String name, String objects, int icon) {
        this.id = id;
        this.playerId = playerId;
        this.nb = nb;
        this.name = name;
        this.objects = objects;
        this.icon = icon;
    }

    public void setPlayerId(int playerId) {
        this.playerId =playerId ;
    }
    public void setNb(int nb) {
        this.nb =nb ;
    }
    public void setName(String name) {
        this.name =name ;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setObjects(String objects) {
        this.objects = objects;
    }
    public void setIcon(int icon){
        this.icon = icon;
    }

    public Integer getId() {
        return this.id;
    }
    public Integer getPlayerId() {
        return this.playerId;
    }
    public Integer getNb() {
        return this.nb;
    }
    public Integer getIcon() {
        return this.icon;
    }
    public String getObjects() {
        return this.objects;
    }
    public String getName() {
        return this.name;
    }


}
