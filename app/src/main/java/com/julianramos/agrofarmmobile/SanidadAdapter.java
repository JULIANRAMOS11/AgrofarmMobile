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

public class SanidadAdapter extends RecyclerView.Adapter<SanidadAdapter.SanidadViewHolder> {

    private List<Sanidad> sanidadList;
    private List<Sanidad> sanidadListFull;

    public SanidadAdapter(List<Sanidad> sanidadList) {
        this.sanidadList = sanidadList;
        this.sanidadListFull = new ArrayList<>(sanidadList);
    }

    public void updateList(List<Sanidad> newList) {
        this.sanidadList = newList;
        this.sanidadListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        sanidadList = new ArrayList<>();
        if (text.isEmpty()) {
            sanidadList.addAll(sanidadListFull);
        } else {
            text = text.toLowerCase();
            for (Sanidad item : sanidadListFull) {
                if (item.getTipoTratamiento().toLowerCase().contains(text)) {
                    sanidadList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SanidadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sanidad, parent, false);
        return new SanidadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SanidadViewHolder holder, int position) {
        Sanidad sanidad = sanidadList.get(position);
        
        holder.tvTipo.setText(sanidad.getTipoTratamiento());
        holder.tvDescripcion.setText(sanidad.getDescripcion());
        holder.tvFecha.setText("📅 " + sanidad.getFecha() + " | 👨‍⚕️ " + sanidad.getVeterinario());
        holder.tvDosis.setText(sanidad.getDosis());

        // Colores vibrantes
        String tipo = sanidad.getTipoTratamiento().toLowerCase();
        if (tipo.contains("vacuna")) {
            holder.tvDosis.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde
        } else if (tipo.contains("desparasitacion")) {
            holder.tvDosis.setBackgroundColor(Color.parseColor("#FF6F00")); // Naranja
        } else if (tipo.contains("tratamiento")) {
            holder.tvDosis.setBackgroundColor(Color.parseColor("#D32F2F")); // Rojo
        } else {
            holder.tvDosis.setBackgroundColor(Color.parseColor("#2196F3")); // Azul
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), CreateSanidadActivity.class);
            intent.putExtra("id", sanidad.getId());
            intent.putExtra("pig_id", sanidad.getPigId());
            intent.putExtra("fecha", sanidad.getFecha());
            intent.putExtra("tipo", sanidad.getTipoTratamiento());
            intent.putExtra("descripcion", sanidad.getDescripcion());
            intent.putExtra("veterinario", sanidad.getVeterinario());
            intent.putExtra("dosis", sanidad.getDosis());
            intent.putExtra("observaciones", sanidad.getObservaciones());
            intent.putExtra("isEdit", true);
            holder.itemView.getContext().startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            showOptions(holder.itemView.getContext(), sanidad, position);
            return true;
        });
    }

    private void showOptions(Context context, Sanidad sanidad, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Opciones")
                .setItems(new String[]{"Editar", "Eliminar"}, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(context, CreateSanidadActivity.class);
                        intent.putExtra("id", sanidad.getId());
                        intent.putExtra("pig_id", sanidad.getPigId());
                        intent.putExtra("fecha", sanidad.getFecha());
                        intent.putExtra("tipo", sanidad.getTipoTratamiento());
                        intent.putExtra("descripcion", sanidad.getDescripcion());
                        intent.putExtra("veterinario", sanidad.getVeterinario());
                        intent.putExtra("dosis", sanidad.getDosis());
                        intent.putExtra("observaciones", sanidad.getObservaciones());
                        intent.putExtra("isEdit", true);
                        context.startActivity(intent);
                    } else {
                        deleteSanidad(context, sanidad.getId(), position);
                    }
                }).show();
    }

    private void deleteSanidad(Context context, int id, int position) {
        String url = "https://api-agrofarm.onrender.com/api/sanidad/" + id;
        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    sanidadList.remove(position);
                    sanidadListFull.remove(position); // Importante
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                }, error -> Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() { return sanidadList.size(); }

    public static class SanidadViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo, tvDescripcion, tvFecha, tvDosis;
        public SanidadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tvTipoTratamiento);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionSanidad);
            tvFecha = itemView.findViewById(R.id.tvFechaSanidad);
            tvDosis = itemView.findViewById(R.id.tvDosisBadge);
        }
    }
}