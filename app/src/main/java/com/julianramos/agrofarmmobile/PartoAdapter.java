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

public class PartoAdapter extends RecyclerView.Adapter<PartoAdapter.PartoViewHolder> {

    private List<Parto> partoList;
    private List<Parto> partoListFull;

    public PartoAdapter(List<Parto> partoList) {
        this.partoList = partoList;
        this.partoListFull = new ArrayList<>(partoList);
    }

    public void updateList(List<Parto> newList) {
        this.partoList = newList;
        this.partoListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        partoList = new ArrayList<>();
        if (text.isEmpty()) {
            partoList.addAll(partoListFull);
        } else {
            text = text.toLowerCase();
            for (Parto item : partoListFull) {
                if (item.getCodigoArete().toLowerCase().contains(text)) {
                    partoList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PartoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parto, parent, false);
        return new PartoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PartoViewHolder holder, int position) {
        Parto parto = partoList.get(position);
        holder.tvArete.setText(parto.getCodigoArete());
        holder.tvLechones.setText("Vivos: " + parto.getLechonesVivos() + " | Muertos: " + parto.getLechonesMuertos());
        holder.tvFecha.setText("📅 " + parto.getFechaParto());

        holder.btnDelete.setOnClickListener(v -> confirmDelete(holder.itemView.getContext(), parto.getId(), position));
    }

    private void confirmDelete(Context context, String id, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Parto")
                .setMessage("¿Estás seguro de eliminar este registro?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteParto(context, id, position))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteParto(Context context, String id, int position) {
        String url = "https://api-agrofarm.onrender.com/api/reproduccion/partos/" + id;
        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    partoList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                }, error -> Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() { return partoList.size(); }

    public static class PartoViewHolder extends RecyclerView.ViewHolder {
        TextView tvArete, tvLechones, tvFecha;
        ImageButton btnDelete;
        public PartoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvArete = itemView.findViewById(R.id.tvAreteParto);
            tvLechones = itemView.findViewById(R.id.tvLechonesParto);
            tvFecha = itemView.findViewById(R.id.tvFechaParto);
            btnDelete = itemView.findViewById(R.id.btnDeleteParto);
        }
    }
}