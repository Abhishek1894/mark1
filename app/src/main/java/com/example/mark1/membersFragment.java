package com.example.mark1;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link membersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class membersFragment extends Fragment
{

    String aptCode; // variable for storing apt code of person

    ArrayList <User> memberList = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public membersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment membersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static membersFragment newInstance(String param1, String param2) {
        membersFragment fragment = new membersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_members, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.memberFragmentRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        MemberRecyclerAdapater memberRecyclerAdapater = new MemberRecyclerAdapater(getActivity(),memberList);

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();
        reference.child("users").child(userEmail.substring(0,userEmail.length() - 4)).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    User user = snapshot.getValue(User.class);

                    aptCode = user.getAptCode();

                    // profileProgressDialog.cancel();
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

        reference.child("users").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                memberList.clear();
                if(snapshot.exists())
                {
                    for(DataSnapshot data : snapshot.getChildren())
                    {
                        User user = data.getValue(User.class);

                        if(user.getAptCode().equals(aptCode))
                            memberList.add(user);
                    }

                    memberRecyclerAdapater.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

        recyclerView.setAdapter(memberRecyclerAdapater);

        return view;
    }
}