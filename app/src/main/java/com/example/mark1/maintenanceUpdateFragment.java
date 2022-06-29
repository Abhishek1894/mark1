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

        update.setOnClickListener(v ->
        {
            reference.child("apartments").child(apartmentCode).child("maintenance").setValue(updateMaintenance.getText().toString());
            Toast.makeText(getActivity(), "Maintenance updated successfully", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}