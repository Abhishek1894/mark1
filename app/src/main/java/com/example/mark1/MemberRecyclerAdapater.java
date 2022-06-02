package com.example.mark1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MemberRecyclerAdapater extends RecyclerView.Adapter<MemberRecyclerAdapater.ViewHolder>
{
    Context context;
    ArrayList<User> list;

    public MemberRecyclerAdapater(Context c, ArrayList<User> list)
    {
        this.context = c;
        this.list = list;
    }

    @NonNull
    @Override
    public MemberRecyclerAdapater.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(context).inflate(R.layout.members,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberRecyclerAdapater.ViewHolder holder, int position)
    {
        holder.name.setText(list.get(position).getName());
        holder.phoneNo.setText(list.get(position).getPhoneNo());;
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView name;
        TextView phoneNo;

        ViewHolder(View v)
        {
            super(v);
            name = v.findViewById(R.id.memberNameTextView);
            phoneNo = v.findViewById(R.id.memberPhoneNoTextView);
        }
    }
}
