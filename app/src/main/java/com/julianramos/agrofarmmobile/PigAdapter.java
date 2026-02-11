package com.julianramos.agrofarmmobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
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

public class PigAdapter extends RecyclerView.Adapter<PigAdapter.PigViewHolder> {

    private List<Pig> pigList;
    private List<Pig> pigListFull;

    public PigAdapter(List<Pig> pigList) {
        this.pigList = pigList;
        this.pigListFull = new ArrayList<>(pigList);
    }

    public void updateList(List<Pig> newList) {
        this.pigList = newList;
        this.pigListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        pigList = new ArrayList<>();
        if (text.isEmpty()) {
            pigList.addAll(pigListFull);
        } else {
            text = text.toLowerCase();
            for (Pig item : pigListFull) {
                if (item.getCodigoArete().toLowerCase().contains(text)) {
                    pigList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PigViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pig_card, parent, false);
        return new PigViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PigViewHolder holder, int position) {
        Pig pig = pigList.get(position);
        
        holder.tvArete.setText(pig.getCodigoArete());
        holder.tvSexo.setText(pig.getSexo());
        holder.tvPeso.setText(pig.getPesoActual() + " kg");
        holder.tvEtapa.setText("ETAPA: " + (pig.getEtapa() != null ? pig.getEtapa() : "N/A"));
        
        String estado = pig.getEstado() != null ? pig.getEstado() : "INACTIVO";
        holder.tvEstadoBadge.setText(estado.toUpperCase());
        
        if (estado.equalsIgnoreCase("ACTIVO")) {
            holder.tvEstadoBadge.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde Vibrante
        } else {
            holder.tvEstadoBadge.setBackgroundColor(Color.parseColor("#757575")); // Gris
        }

        holder.itemView.setOnClickListener(v -> openEditActivity(holder.itemView.getContext(), pig));
        holder.itemView.setOnLongClickListener(v -> {
            showOptionsDialog(holder.itemView.getContext(), pig, position);
            return true;
        });
    }

    private void openEditActivity(Context context, Pig pig) {
        Intent intent = new Intent(context, EditPigActivity.class);
        intent.putExtra("id", pig.getId());
        intent.putExtra("codigo_arete", pig.getCodigoArete());
        intent.putExtra("sexo", pig.getSexo());
        intent.putExtra("fecha_nacimiento", pig.getFechaNacimiento());
        intent.putExtra("peso_actual", pig.getPesoActual());
        intent.putExtra("estado", pig.getEstado());
        context.startActivity(intent);
    }

    private void showOptionsDialog(Context context, Pig pig, int position) {
        String[] options = {"Editar", "Eliminar"};
        new AlertDialog.Builder(context)
                .setTitle("Opciones: " + pig.getCodigoArete())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openEditActivity(context, pig);
                    } else {
                        confirmDelete(context, pig, position);
                    }
                }).show();
    }

    private void confirmDelete(Context context, Pig pig, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Cerdo")
                .setMessage("¿Estás seguro de eliminar a " + pig.getCodigoArete() + "?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> deletePig(context, pig.getId(), position))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deletePig(Context context, int id, int position) {
        String url = "https://api-agrofarm.onrender.com/api/pigs/" + id;
        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    pigList.remove(position);
                    pigListFull.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() {
        return pigList.size();
    }

    public static class PigViewHolder extends RecyclerView.ViewHolder {
        TextView tvArete, tvSexo, tvPeso, tvEtapa, tvEstadoBadge;

        public PigViewHolder(@NonNull View itemView) {
            super(itemView);
            tvArete = itemView.findViewById(R.id.tvArete);
            tvSexo = itemView.findViewById(R.id.tvSexo);
            tvPeso = itemView.findViewById(R.id.tvPeso);
            tvEtapa = itemView.findViewById(R.id.tvEtapa);
            tvEstadoBadge = itemView.findViewById(R.id.tvEstadoBadge);
        }
    }
}