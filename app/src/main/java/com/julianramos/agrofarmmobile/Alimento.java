package com.julianramos.agrofarmmobile;

public class Alimento {
    private int id;
    private String nombre_alimento;
    private String tipo;
    private String proteina_porcentaje;
    private String costo_por_kg;
    private String proveedor;
    private String stock_kg;

    public Alimento(int id, String nombre_alimento, String tipo, String proteina_porcentaje, String costo_por_kg, String proveedor, String stock_kg) {
        this.id = id;
        this.nombre_alimento = nombre_alimento;
        this.tipo = tipo;
        this.proteina_porcentaje = proteina_porcentaje;
        this.costo_por_kg = costo_por_kg;
        this.proveedor = proveedor;
        this.stock_kg = stock_kg;
    }

    public int getId() { return id; }
    public String getNombreAlimento() { return nombre_alimento; }
    public String getTipo() { return tipo; }
    public String getProteinaPorcentaje() { return proteina_porcentaje; }
    public String getCostoPorKg() { return costo_por_kg; }
    public String getProveedor() { return proveedor; }
    public String getStockKg() { return stock_kg; }
}