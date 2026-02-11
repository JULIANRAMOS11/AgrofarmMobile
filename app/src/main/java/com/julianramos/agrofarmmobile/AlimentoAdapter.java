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

public class AlimentoAdapter extends RecyclerView.Adapter<AlimentoAdapter.AlimentoViewHolder> {

    private List<Alimento> alimentoList;
    private List<Alimento> alimentoListFull;

    public AlimentoAdapter(List<Alimento> alimentoList) {
        this.alimentoList = alimentoList;
        this.alimentoListFull = new ArrayList<>(alimentoList);
    }

    public void updateList(List<Alimento> newList) {
        this.alimentoList = newList;
        this.alimentoListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        alimentoList = new ArrayList<>();
        if (text.isEmpty()) {
            alimentoList.addAll(alimentoListFull);
        } else {
            text = text.toLowerCase();
            for (Alimento item : alimentoListFull) {
                if (item.getNombreAlimento().toLowerCase().contains(text)) {
                    alimentoList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlimentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alimento, parent, false);
        return new AlimentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlimentoViewHolder holder, int position) {
        Alimento alimento = alimentoList.get(position);
        
        holder.tvNombre.setText(alimento.getNombreAlimento());
        holder.tvTipo.setText("📦 " + alimento.getTipo() + " | 🧬 " + alimento.getProteinaPorcentaje() + "% Prot");
        holder.tvProveedor.setText("🏢 Proveedor: " + alimento.getProveedor());
        holder.tvStock.setText(alimento.getStockKg() + " kg");
        holder.tvCosto.setText("💰 $" + alimento.getCostoPorKg() + "/kg");

        // Badge color basado en stock
        double stock = 0;
        try { stock = Double.parseDouble(alimento.getStockKg()); } catch (Exception ignored) {}
        if (stock < 50) {
            holder.tvStock.setBackgroundColor(Color.parseColor("#D32F2F")); // Rojo Crítico
        } else if (stock < 200) {
            holder.tvStock.setBackgroundColor(Color.parseColor("#FF6F00")); // Naranja Bajo
        } else {
            holder.tvStock.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde Ok
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), CreateAlimentoActivity.class);
            intent.putExtra("id", alimento.getId());
            intent.putExtra("nombre", alimento.getNombreAlimento());
            intent.putExtra("tipo", alimento.getTipo());
            intent.putExtra("proteina", alimento.getProteinaPorcentaje());
            intent.putExtra("costo", alimento.getCostoPorKg());
            intent.putExtra("proveedor", alimento.getProveedor());
            intent.putExtra("stock", alimento.getStockKg());
            intent.putExtra("isEdit", true);
            holder.itemView.getContext().startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Opciones")
                    .setItems(new String[]{"Editar", "Eliminar"}, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(holder.itemView.getContext(), CreateAlimentoActivity.class);
                            intent.putExtra("id", alimento.getId());
                            intent.putExtra("nombre", alimento.getNombreAlimento());
                            intent.putExtra("isEdit", true);
                            holder.itemView.getContext().startActivity(intent);
                        } else {
                            deleteAlimento(holder.itemView.getContext(), alimento.getId(), position);
                        }
                    }).show();
            return true;
        });
    }

    private void deleteAlimento(Context context, int id, int position) {
        String url = "https://api-agrofarm.onrender.com/api/nutricion/alimentos/" + id;
        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    alimentoList.remove(position);
                    alimentoListFull.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Alimento eliminado", Toast.LENGTH_SHORT).show();
                }, error -> Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() { return alimentoList.size(); }

    public static class AlimentoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvTipo, tvProveedor, tvStock, tvCosto;
        public AlimentoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreAlimento);
            tvTipo = itemView.findViewById(R.id.tvTipoAlimento);
            tvProveedor = itemView.findViewById(R.id.tvProveedorAlimento);
            tvStock = itemView.findViewById(R.id.tvStockBadge);
            tvCosto = itemView.findViewById(R.id.tvCostoAlimento);
        }
    }
}