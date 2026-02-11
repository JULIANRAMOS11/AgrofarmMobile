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
import java.util.List;

public class PesajeAdapter extends RecyclerView.Adapter<PesajeAdapter.PesajeViewHolder> {

    private List<Pesaje> pesajeList;
    private List<Pesaje> pesajeListFull;

    public PesajeAdapter(List<Pesaje> pesajeList) {
        this.pesajeList = pesajeList;
        this.pesajeListFull = new ArrayList<>(pesajeList);
    }

    public void updateList(List<Pesaje> newList) {
        this.pesajeList = newList;
        this.pesajeListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        pesajeList = new ArrayList<>();
        if (text.isEmpty()) {
            pesajeList.addAll(pesajeListFull);
        } else {
            text = text.toLowerCase();
            for (Pesaje item : pesajeListFull) {
                if (item.getCodigoArete().toLowerCase().contains(text)) {
                    pesajeList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PesajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_produccion, parent, false);
        return new PesajeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PesajeViewHolder holder, int position) {
        Pesaje p = pesajeList.get(position);
        holder.tvPeso.setText(p.getPesoKg() + " kg");
        holder.tvFecha.setText(p.getFechaPesaje());
        holder.tvGanancia.setText("Ganancia: " + p.getGananciaDiaria() + " kg/día");

        holder.btnDelete.setOnClickListener(v -> confirmDelete(holder.itemView.getContext(), p.getId(), position));
    }

    private void confirmDelete(Context context, String id, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Pesaje")
                .setMessage("¿Estás seguro?")
                .setPositiveButton("Eliminar", (dialog, which) -> deletePesaje(context, id, position))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deletePesaje(Context context, String id, int position) {
        String url = "https://api-agrofarm.onrender.com/api/produccion/" + id;
        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    pesajeList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show();
                }, error -> Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() { return pesajeList.size(); }

    public static class PesajeViewHolder extends RecyclerView.ViewHolder {
        TextView tvPeso, tvFecha, tvGanancia;
        ImageButton btnDelete;
        public PesajeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPeso = itemView.findViewById(R.id.tvPesoProduccion);
            tvFecha = itemView.findViewById(R.id.tvFechaProduccion);
            tvGanancia = itemView.findViewById(R.id.tvGananciaProduccion);
            btnDelete = itemView.findViewById(R.id.btnDeletePesaje);
        }
    }
}