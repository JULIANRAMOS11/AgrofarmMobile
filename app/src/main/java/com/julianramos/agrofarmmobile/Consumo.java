package com.julianramos.agrofarmmobile;

public class Consumo {
    private String id;
    private String pig_id;
    private String alimento_id;
    private String fecha;
    private double cantidad_kg;
    private String lote;
    private String observaciones;
    private String codigo_arete;
    private String nombre_alimento;

    public Consumo(String id, String pig_id, String alimento_id, String fecha, double cantidad_kg, String lote, String observaciones, String codigo_arete, String nombre_alimento) {
        this.id = id;
        this.pig_id = pig_id;
        this.alimento_id = alimento_id;
        this.fecha = fecha;
        this.cantidad_kg = cantidad_kg;
        this.lote = lote;
        this.observaciones = observaciones;
        this.codigo_arete = codigo_arete;
        this.nombre_alimento = nombre_alimento;
    }

    public String getId() { return id; }
    public String getPigId() { return pig_id; }
    public String getAlimentoId() { return alimento_id; }
    public String getFecha() { return fecha; }
    public double getCantidadKg() { return cantidad_kg; }
    public String getLote() { return lote; }
    public String getObservaciones() { return observaciones; }
    public String getCodigoArete() { return codigo_arete; }
    public String getNombreAlimento() { return nombre_alimento; }
}