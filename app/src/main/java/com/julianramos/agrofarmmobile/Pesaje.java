package com.julianramos.agrofarmmobile;

public class Pesaje {
    private String id;
    private String pig_id;
    private String fecha_pesaje;
    private double peso_kg;
    private double ganancia_diaria;
    private String observaciones;
    private String codigo_arete;

    public Pesaje(String id, String pig_id, String fecha_pesaje, double peso_kg, double ganancia_diaria, String observaciones, String codigo_arete) {
        this.id = id;
        this.pig_id = pig_id;
        this.fecha_pesaje = fecha_pesaje;
        this.peso_kg = peso_kg;
        this.ganancia_diaria = ganancia_diaria;
        this.observaciones = observaciones;
        this.codigo_arete = codigo_arete;
    }

    public String getId() { return id; }
    public String getPigId() { return pig_id; }
    public String getFechaPesaje() { return fecha_pesaje; }
    public double getPesoKg() { return peso_kg; }
    public double getGananciaDiaria() { return ganancia_diaria; }
    public String getObservaciones() { return observaciones; }
    public String getCodigoArete() { return codigo_arete; }
}