package com.alox.myreminders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RemAdapter extends RecyclerView.Adapter<RemAdapter.ViewHolder> {

    List<String> rems;

    public RemAdapter(Set<String> rems) {
        this.rems = new ArrayList<>(rems);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rem_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String rem = rems.get(position);
        if (rem.contains("`")) {
            holder.title.setText(rem.substring(0,rem.indexOf("`")));
            holder.date.setText(rem.substring(rem.indexOf("`")+1,rem.indexOf("``")));
        }
    }

    @Override
    public int getItemCount() {
        return rems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title,date;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textView2);
            date = itemView.findViewById(R.id.dateItem);
        }
    }

    public void removeItem(int position) {
        rems.remove(position);
        notifyItemRemoved(position);
    }

    public String getData(int position) {
        System.out.println(rems);
        return rems.get(position);
    }
}
