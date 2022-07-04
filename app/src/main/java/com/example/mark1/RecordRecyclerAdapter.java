package com.example.mark1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecordRecyclerAdapter extends RecyclerView.Adapter<RecordRecyclerAdapter.ViewHolder>
{
    Context context;
    ArrayList<Record> list;

    public RecordRecyclerAdapter(Context context,ArrayList<Record> list)
    {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecordRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(context).inflate(R.layout.records,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordRecyclerAdapter.ViewHolder holder, int position)
    {
        holder.name.setText(list.get(position).getName());
        holder.maintenanceCost.setText(list.get(position).getAmount());

        if(list.get(position).isStatus())
        {
            holder.status.setText("Paid");
            holder.status.setBackgroundResource(R.color.green);
        }
        else
        {
            holder.status.setText("Unpaid");
            holder.status.setBackgroundResource(R.color.red);
        }
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView name;
        TextView maintenanceCost;
        TextView status;

        ViewHolder(View v)
        {
            super(v);

            name = v.findViewById(R.id.textviewRecordName);
            maintenanceCost = v.findViewById(R.id.textViewRecordCost);;
            status = v.findViewById(R.id.textviewRecordStatus);
        }
    }
}
