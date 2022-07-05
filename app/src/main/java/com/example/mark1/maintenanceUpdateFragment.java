package com.example.mark1;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// for fetching current date and month
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link maintenanceUpdateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class maintenanceUpdateFragment extends Fragment
{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public maintenanceUpdateFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment maintenanceUpdateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static maintenanceUpdateFragment newInstance(String param1, String param2)
    {
        maintenanceUpdateFragment fragment = new maintenanceUpdateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    String apartmentCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_maintenance_update, container, false);


        //finding the ID's of UI Components
        EditText updateMaintenance = view.findViewById(R.id.editTextUpdateMaintenance);
        Button update = view.findViewById(R.id.buttonUpdate);
        TextView currentMaintenance = view.findViewById(R.id.textViewCurrentMaintenance);

        //fetching data using bundle from HomeActivity
        Bundle bundle;
        bundle = getArguments();
        String apartmentCode = bundle.getString("userAptCode");
        String status = bundle.getString("userStatus");


        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();

        // code to fetch the current maintenance of the building
        reference.child("apartments").child(apartmentCode).child("maintenance").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    String maintenance = snapshot.getValue(String.class);
                    String c = "Current Maintenance = "+maintenance+ " Rs.";
                    currentMaintenance.setText(c);
                }
                else
                {
                    Toast.makeText(getActivity(),"Data does not exist",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(getActivity(),"Error in data fetching",Toast.LENGTH_SHORT).show();
            }
        });

        if(!status.equals("admin"))
            update.setEnabled(false);
        else
            update.setEnabled(true);

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



        // code to update maintenance cost
        update.setOnClickListener(v ->
        {
            reference.child("apartments").child(apartmentCode).child("maintenance").setValue(updateMaintenance.getText().toString());
            Toast.makeText(getActivity(), "Maintenance updated successfully", Toast.LENGTH_SHORT).show();


//            for(String data : members)
//                Toast.makeText(getActivity(),data,Toast.LENGTH_LONG).show();

            // code for updating maintenance in payment database
            LocalDate date = LocalDate.now();

            Month month = date.getMonth();

            int index = monthList.indexOf(month.toString());

            for(int i = index; i < monthList.size(); i++)
            {
                String currentMonth = monthList.get(i);

                reference.child("payments").child("APRIL").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            for(DataSnapshot data : snapshot.getChildren())
                            {
                                Record record = data.getValue(Record.class);
                                if(record.getAptcode().equals(apartmentCode))
                                {
                                    reference.child("payments").child(currentMonth).child(data.getKey()).child("amount").setValue(updateMaintenance.getText().toString());
                                    Toast.makeText(getActivity(),data.getKey(),Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }
                });
            }
        });

        return view;
    }
}