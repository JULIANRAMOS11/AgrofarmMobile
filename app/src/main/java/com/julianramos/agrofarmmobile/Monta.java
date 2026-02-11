package com.julianramos.agrofarmmobile;

public class Monta {
    private String id;
    private String pig_id;
    private String fecha_servicio;
    private String tipo_servicio;
    private String observaciones;
    private String codigo_arete;

    public Monta(String id, String pig_id, String fecha_servicio, String tipo_servicio, String observaciones, String codigo_arete) {
        this.id = id;
        this.pig_id = pig_id;
        this.fecha_servicio = fecha_servicio;
        this.tipo_servicio = tipo_servicio;
        this.observaciones = observaciones;
        this.codigo_arete = codigo_arete;
    }

    public String getId() { return id; }
    public String getPigId() { return pig_id; }
    public String getFechaServicio() { return fecha_servicio; }
    public String getTipoServicio() { return tipo_servicio; }
    public String getObservaciones() { return observaciones; }
    public String getCodigoArete() { return codigo_arete; }
}