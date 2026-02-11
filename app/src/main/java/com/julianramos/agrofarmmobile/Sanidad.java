package com.julianramos.agrofarmmobile;

public class Sanidad {
    private int id;
    private int pig_id;
    private String fecha;
    private String tipo_tratamiento;
    private String descripcion;
    private String veterinario;
    private String dosis;
    private String observaciones;

    public Sanidad(int id, int pig_id, String fecha, String tipo_tratamiento, String descripcion, String veterinario, String dosis, String observaciones) {
        this.id = id;
        this.pig_id = pig_id;
        this.fecha = fecha;
        this.tipo_tratamiento = tipo_tratamiento;
        this.descripcion = descripcion;
        this.veterinario = veterinario;
        this.dosis = dosis;
        this.observaciones = observaciones;
    }

    public int getId() { return id; }
    public int getPigId() { return pig_id; }
    public String getFecha() { return fecha; }
    public String getTipoTratamiento() { return tipo_tratamiento; }
    public String getDescripcion() { return descripcion; }
    public String getVeterinario() { return veterinario; }
    public String getDosis() { return dosis; }
    public String getObservaciones() { return observaciones; }
}