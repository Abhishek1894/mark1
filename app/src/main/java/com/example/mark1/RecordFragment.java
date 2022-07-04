package com.example.mark1;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class RecordFragment extends Fragment
{
    ArrayList<Record> recordList = new ArrayList<>();

    Spinner monthSpinner;


    public RecordFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_record, container, false);

        // code for recycler view of records
        RecyclerView recyclerView = view.findViewById(R.id.recordFragmentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecordRecyclerAdapter recordRecyclerAdapter = new RecordRecyclerAdapter(getActivity(),recordList);

        //fetching data using bundle from HomeActivity
        Bundle bundle;
        bundle = getArguments();
        String aptCode = bundle.getString("userAptCode");

        monthSpinner = view.findViewById(R.id.monthSpinner);

        // adding months to arraylist
        ArrayList<String> monthList = new ArrayList<>();

        monthList.add("JANUARY");
        monthList.add("FEBRUARY");
        monthList.add("MARCH");
        monthList.add("APRIL");
        monthList.add("MAY");
        monthList.add("JUNE");
        monthList.add("JULY");
        monthList.add("AUGUST");
        monthList.add("SEPTEMBER");
        monthList.add("OCTOBER");
        monthList.add("NOVEMBER");
        monthList.add("DECEMBER");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item,monthList);
        monthSpinner.setAdapter(adapter);

        // code to show records when user selects a month
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // function is invoked to show records
                showRecords(aptCode,monthList.get(position),recordRecyclerAdapter);
                Toast.makeText(getActivity(),monthList.get(position),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        recyclerView.setAdapter(recordRecyclerAdapter);

        return view;
    }

    // method to fetch the records from database
    public void showRecords(String apartmentCode, String month, RecordRecyclerAdapter recordRecyclerAdapter)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();

        reference.child("payments").child(month).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                recordList.clear();
                if(snapshot.exists())
                {
                    for(DataSnapshot data : snapshot.getChildren())
                    {
                        Record record = data.getValue(Record.class);

//                        if(record.getApartmentCode().equals(apartmentCode))
                            recordList.add(record);
                    }

                    recordRecyclerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}