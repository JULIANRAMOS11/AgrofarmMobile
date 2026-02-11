package com.julianramos.agrofarmmobile;

public class Parto {
    private String id;
    private String pig_id;
    private String fecha_parto;
    private int lechones_vivos;
    private int lechones_muertos;
    private double peso_promedio_lechon;
    private String observaciones;
    private String codigo_arete;

    public Parto(String id, String pig_id, String fecha_parto, int lechones_vivos, int lechones_muertos, double peso_promedio_lechon, String observaciones, String codigo_arete) {
        this.id = id;
        this.pig_id = pig_id;
        this.fecha_parto = fecha_parto;
        this.lechones_vivos = lechones_vivos;
        this.lechones_muertos = lechones_muertos;
        this.peso_promedio_lechon = peso_promedio_lechon;
        this.observaciones = observaciones;
        this.codigo_arete = codigo_arete;
    }

    public String getId() { return id; }
    public String getPigId() { return pig_id; }
    public String getFechaParto() { return fecha_parto; }
    public int getLechonesVivos() { return lechones_vivos; }
    public int getLechonesMuertos() { return lechones_muertos; }
    public double getPesoPromedioLechon() { return peso_promedio_lechon; }
    public String getObservaciones() { return observaciones; }
    public String getCodigoArete() { return codigo_arete; }
}