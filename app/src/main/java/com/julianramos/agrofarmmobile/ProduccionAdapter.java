package com.julianramos.agrofarmmobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

public class ProduccionAdapter extends RecyclerView.Adapter<ProduccionAdapter.ProduccionViewHolder> {

    private List<Produccion> produccionList;
    private List<Produccion> produccionListFull;

    public ProduccionAdapter(List<Produccion> produccionList) {
        this.produccionList = produccionList;
        this.produccionListFull = new ArrayList<>(produccionList);
    }

    public void updateList(List<Produccion> newList) {
        this.produccionList = newList;
        this.produccionListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        produccionList = new ArrayList<>();
        if (text.isEmpty()) {
            produccionList.addAll(produccionListFull);
        } else {
            text = text.toLowerCase();
            for (Produccion item : produccionListFull) {
                if (item.getFecha().toLowerCase().contains(text)) {
                    produccionList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProduccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_produccion, parent, false);
        return new ProduccionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProduccionViewHolder holder, int position) {
        Produccion p = produccionList.get(position);
        
        holder.tvPeso.setText("⚖️ " + p.getPeso() + " kg");
        holder.tvGanancia.setText("📈 Ganancia: " + p.getGananciaDiaria() + " kg/día | 🎂 " + p.getEdadDias() + " días");
        holder.tvFecha.setText("📅 " + p.getFecha() + " | 🍽️ Consumo: " + p.getConsumoAlimentoKg() + " kg");
        holder.tvConversion.setText("C.A: " + p.getConversionAlimenticia());

        // Badge color basado en conversión (valor típico deseado < 3.0)
        double conv = 0;
        try { conv = Double.parseDouble(p.getConversionAlimenticia()); } catch (Exception ignored) {}
        if (conv > 3.5) {
            holder.tvConversion.setBackgroundColor(Color.parseColor("#D32F2F")); // Rojo: Ineficiente
        } else if (conv > 3.0) {
            holder.tvConversion.setBackgroundColor(Color.parseColor("#FF6F00")); // Naranja: Regular
        } else {
            holder.tvConversion.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde: Eficiente
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), CreateProduccionActivity.class);
            intent.putExtra("id", p.getId());
            intent.putExtra("pig_id", p.getPigId());
            intent.putExtra("fecha", p.getFecha());
            intent.putExtra("peso", p.getPeso());
            intent.putExtra("edad_dias", p.getEdadDias());
            intent.putExtra("ganancia", p.getGananciaDiaria());
            intent.putExtra("consumo", p.getConsumoAlimentoKg());
            intent.putExtra("conversion", p.getConversionAlimenticia());
            intent.putExtra("isEdit", true);
            holder.itemView.getContext().startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Opciones")
                    .setItems(new String[]{"Editar", "Eliminar"}, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(holder.itemView.getContext(), CreateProduccionActivity.class);
                            intent.putExtra("id", p.getId());
                            intent.putExtra("isEdit", true);
                            holder.itemView.getContext().startActivity(intent);
                        } else {
                            deleteProduccion(holder.itemView.getContext(), p.getId(), position);
                        }
                    }).show();
            return true;
        });
    }

    private void deleteProduccion(Context context, int id, int position) {
        String url = "https://api-agrofarm.onrender.com/api/produccion/" + id;
        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    produccionList.remove(position);
                    produccionListFull.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Reporte eliminado", Toast.LENGTH_SHORT).show();
                }, error -> Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() { return produccionList.size(); }

    public static class ProduccionViewHolder extends RecyclerView.ViewHolder {
        TextView tvPeso, tvGanancia, tvFecha, tvConversion;
        public ProduccionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPeso = itemView.findViewById(R.id.tvPesoProduccion);
            tvGanancia = itemView.findViewById(R.id.tvGananciaProduccion);
            tvFecha = itemView.findViewById(R.id.tvFechaProduccion);
            tvConversion = itemView.findViewById(R.id.tvConversionBadge);
        }
    }
}