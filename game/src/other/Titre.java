package other;


import java.util.List;

public class Titre {

    private int id;
    private String name;
    private int price;
    private String conditions;

    public Titre(int id, String name, int price, String conditions) {
        this.setId(id);
        this.setName(name);
        this.setPrice(price);
        this.setConditions(conditions);
    }

    private void setName(String name) {
        this.name =name ;
    }

    private void setId(int id) {
        this.id = id;
    }

    private void setPrice(int price) {
        this.price = price;
    }

    private void setConditions(String conditions){
        this.conditions = conditions;
    }

    public Integer getId() {
        return this.id;
    }
    public Integer getPrice() {
        return this.price;
    }

    public String getConditions() {
        return this.conditions;
    }

    public String getName() {
        return this.name;
    }


}
