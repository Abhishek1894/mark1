package com.example.mark1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment
{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2)
    {
        ProfileFragment fragment = new ProfileFragment();
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

    TextView apartmentName;
    String apartmentCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // finding ID's of all the UI components
        TextView email = view.findViewById(R.id.textViewProfileEmail);
        TextView name = view.findViewById(R.id.textViewProfileName);
        TextView phoneNo = view.findViewById(R.id.textViewProfilePhoneNo);
        apartmentName = view.findViewById(R.id.textViewProfileApartmentName);
        TextView status = view.findViewById(R.id.textViewProfileStatus);
        TextView code = view.findViewById(R.id.textViewShareAptCode);
        Button logout = view.findViewById(R.id.buttonProfileLogout);

        // code for progress bar
        final ProgressDialog profileProgressDialog;
        profileProgressDialog = new ProgressDialog(getActivity());
        profileProgressDialog.setTitle("Fetching Data");
        profileProgressDialog.setMessage("loading data from server...");
        //fto show progressDialog
        profileProgressDialog.show();

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
                    email.setText(userEmail);
                    name.setText(user.getName());
                    phoneNo.setText(user.getPhoneNo());
                    status.setText(user.getStatus());
                    apartmentCode = user.getAptCode();
                    code.setText(user.getAptCode());
                    getApartmentName(reference,user.getAptCode());
                    profileProgressDialog.cancel();
                }
                else
                {
                    Toast.makeText(getActivity(),"Data does not exist",Toast.LENGTH_SHORT).show();
                    apartmentCode = "Some error occured try again to share the code";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(getActivity(),"Error in data fetching",Toast.LENGTH_SHORT).show();
            }
        });

        logout.setOnClickListener(v->
        {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(getActivity(), OptionsActivity.class);
            startActivity(i);
            getActivity().finish();
        });


        // code to share apartment code
        ImageView shareCode = view.findViewById(R.id.imageViewShareIcon);
        // String shCode = user.getAptCode();
        // code to add clickListener to image
        shareCode.setOnClickListener(v->
        {
            Toast.makeText(getActivity(), "Sharing the code...", Toast.LENGTH_SHORT).show();
            //method to share code in whatsapp using implicit intent
            shareCode(apartmentCode);
        });



        return view;
    }

    public void getApartmentName(DatabaseReference reference,String aptCode)
    {
        reference.child("apartments").child(aptCode).child("name").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {

                    apartmentName.setText((String)snapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    //method to share the code
    private void shareCode(String code)
    {
        // Creating new intent
        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.setType("text/plain");
        intent.setPackage("com.whatsapp");

        // Give your message here
        intent.putExtra(Intent.EXTRA_TEXT, code);

        // Checking whether Whatsapp is installed or not
        if (intent.resolveActivity(getActivity().getPackageManager()) == null)
        {
            Toast.makeText(getActivity(), "Please install whatsapp first.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Starting Whatsapp
        startActivity(intent);
    }

}