package com.julianramos.agrofarmmobile;

public class Produccion {
    private int id;
    private int pig_id;
    private String fecha;
    private String peso;
    private int edad_dias;
    private String ganancia_diaria;
    private String consumo_alimento_kg;
    private String conversion_alimenticia;

    public Produccion(int id, int pig_id, String fecha, String peso, int edad_dias, String ganancia_diaria, String consumo_alimento_kg, String conversion_alimenticia) {
        this.id = id;
        this.pig_id = pig_id;
        this.fecha = fecha;
        this.peso = peso;
        this.edad_dias = edad_dias;
        this.ganancia_diaria = ganancia_diaria;
        this.consumo_alimento_kg = consumo_alimento_kg;
        this.conversion_alimenticia = conversion_alimenticia;
    }

    public int getId() { return id; }
    public int getPigId() { return pig_id; }
    public String getFecha() { return fecha; }
    public String getPeso() { return peso; }
    public int getEdadDias() { return edad_dias; }
    public String getGananciaDiaria() { return ganancia_diaria; }
    public String getConsumoAlimentoKg() { return consumo_alimento_kg; }
    public String getConversionAlimenticia() { return conversion_alimenticia; }
}