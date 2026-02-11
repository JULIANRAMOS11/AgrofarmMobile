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

public class MontaAdapter extends RecyclerView.Adapter<MontaAdapter.MontaViewHolder> {

    private List<Monta> montaList;
    private List<Monta> montaListFull;

    public MontaAdapter(List<Monta> montaList) {
        this.montaList = montaList;
        this.montaListFull = new ArrayList<>(montaList);
    }

    public void updateList(List<Monta> newList) {
        this.montaList = newList;
        this.montaListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        montaList = new ArrayList<>();
        if (text.isEmpty()) {
            montaList.addAll(montaListFull);
        } else {
            text = text.toLowerCase();
            for (Monta item : montaListFull) {
                if (item.getCodigoArete().toLowerCase().contains(text)) {
                    montaList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MontaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_monta, parent, false);
        return new MontaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MontaViewHolder holder, int position) {
        Monta monta = montaList.get(position);
        holder.tvArete.setText(monta.getCodigoArete());
        holder.tvTipo.setText(monta.getTipoServicio());
        holder.tvFecha.setText("📅 " + monta.getFechaServicio());

        holder.btnDelete.setOnClickListener(v -> confirmDelete(holder.itemView.getContext(), monta.getId(), position));
    }

    private void confirmDelete(Context context, String id, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Monta")
                .setMessage("¿Estás seguro de eliminar este registro?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteMonta(context, id, position))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteMonta(Context context, String id, int position) {
        String url = "https://api-agrofarm.onrender.com/api/reproduccion/" + id;
        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    montaList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                }, error -> Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() { return montaList.size(); }

    public static class MontaViewHolder extends RecyclerView.ViewHolder {
        TextView tvArete, tvTipo, tvFecha;
        ImageButton btnDelete;
        public MontaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvArete = itemView.findViewById(R.id.tvAreteMonta);
            tvTipo = itemView.findViewById(R.id.tvTipoMonta);
            tvFecha = itemView.findViewById(R.id.tvFechaMonta);
            btnDelete = itemView.findViewById(R.id.btnDeleteMonta);
        }
    }
}