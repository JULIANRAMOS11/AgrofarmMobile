package com.julianramos.agrofarmmobile;

public class Pig {
    private int id;
    private String codigo_arete;
    private String sexo;
    private String fecha_nacimiento;
    private String estado;
    private String peso_actual; // Ahora es String según el nuevo formato
    private String lote;        // Antes lote_nombre
    private String etapa;       // Antes etapa_nombre
    private String created_at;

    public Pig(int id, String codigo_arete, String sexo, String fecha_nacimiento, String estado, String peso_actual, String lote, String etapa, String created_at) {
        this.id = id;
        this.codigo_arete = codigo_arete;
        this.sexo = sexo;
        this.fecha_nacimiento = fecha_nacimiento;
        this.estado = estado;
        this.peso_actual = peso_actual;
        this.lote = lote;
        this.etapa = etapa;
        this.created_at = created_at;
    }

    public int getId() { return id; }
    public String getCodigoArete() { return codigo_arete; }
    public String getSexo() { return sexo; }
    public String getFechaNacimiento() { return fecha_nacimiento; }
    public String getEstado() { return estado; }
    public String getPesoActual() { return peso_actual; }
    public String getLote() { return lote; }
    public String getEtapa() { return etapa; }
    public String getCreatedAt() { return created_at; }
}