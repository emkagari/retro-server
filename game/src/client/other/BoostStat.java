package client.other;

public final class BoostStat {
    private int inicio;
    private int cost;
    private int puntos;

    private static int _inicio = 0;
    private  static int _cost = 1;
    private  static int _puntos = 1;
    public static final BoostStat BoostDefecto = new BoostStat(_inicio, _cost, _puntos);
    /*public BoostDefecto getBoostDefecto()
    {
        return client.other.BoostStat.BoostDefecto;
    }*/
    public static final class BoostDefecto {
        private int inicio;
        private int cost;
        private int puntos;
        public BoostDefecto(int inicio, int cost, int puntos) {
            this.inicio = inicio;
            this.cost = cost;
            this.puntos = puntos;
        }
    }
    public BoostStat(int inicio, int cost, int puntos)
    {
        this.inicio = inicio;
        this.cost = cost;
        this.puntos = puntos;

    }

    public int getInicio() {
        return inicio;
    }

    public void setInicio(int inicio) {
        this.inicio = inicio;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

}
