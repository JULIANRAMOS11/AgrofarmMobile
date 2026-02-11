package com.julianramos.agrofarmmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsumoAdapter extends RecyclerView.Adapter<ConsumoAdapter.ConsumoViewHolder> {

    private List<Consumo> consumoList;
    private final String token;

    public ConsumoAdapter(List<Consumo> consumoList, String token) {
        this.consumoList = consumoList;
        this.token = token;
    }

    public void updateList(List<Consumo> newList) {
        this.consumoList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConsumoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consumo, parent, false);
        return new ConsumoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsumoViewHolder holder, int position) {
        Consumo consumo = consumoList.get(position);
        holder.tvArete.setText(consumo.getCodigoArete());
        holder.tvAlimento.setText(consumo.getNombreAlimento());
        
        String detalle = "📅 " + consumo.getFecha() + " | ⚖️ " + consumo.getCantidadKg() + " kg";
        if (consumo.getLote() != null && !consumo.getLote().isEmpty()) {
            detalle += " | 📦 Lote: " + consumo.getLote();
        }
        holder.tvDetalle.setText(detalle);

        holder.btnDelete.setOnClickListener(v -> confirmDelete(holder.itemView.getContext(), consumo.getId(), position));
    }

    private void confirmDelete(Context context, String id, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Consumo")
                .setMessage("¿Estás seguro de eliminar este registro?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteConsumo(context, id, position))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteConsumo(Context context, String id, int position) {
        String url = "https://api-agrofarm.onrender.com/api/nutricion/consumos/" + id;
        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    consumoList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                }, error -> Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() { return consumoList.size(); }

    public static class ConsumoViewHolder extends RecyclerView.ViewHolder {
        TextView tvArete, tvAlimento, tvDetalle;
        ImageButton btnDelete;
        public ConsumoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvArete = itemView.findViewById(R.id.tvAreteConsumo);
            tvAlimento = itemView.findViewById(R.id.tvAlimentoConsumo);
            tvDetalle = itemView.findViewById(R.id.tvDetalleConsumo);
            btnDelete = itemView.findViewById(R.id.btnDeleteConsumo);
        }
    }
}