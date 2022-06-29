package com.example.mark1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.messaging.FirebaseMessaging;

public class HomeActivity extends AppCompatActivity
{
    Button logout;
    TextView text;
    Bundle bundle;//bundle to pass data from HomeActivity to other fragments.

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    BottomNavigationView bottomNavigationView;

    ProfileFragment profileFragment = new ProfileFragment();
    maintenanceUpdateFragment maintenanceUpdateFragment = new maintenanceUpdateFragment();
    membersFragment MembersFragment = new membersFragment();
    PaymentFragment paymentFragment = new PaymentFragment();
    RecordFragment recordFragment = new RecordFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // fetching the data

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
                    if(user!=null) {
                        //passing User object to other fragments
                        bundle = new Bundle();
                        bundle.putString("userName",user.getName());
                        bundle.putString("userEmail",user.getEmail());
                        bundle.putString("userAptCode",user.getAptCode());
                        bundle.putString("userPhoneNo",user.getPhoneNo());
                        bundle.putString("userStatus",user.getStatus());
                        //sending user data to maintenanceUpdateFragment
                        maintenanceUpdateFragment.setArguments(bundle);
                        MembersFragment.setArguments(bundle);
                        paymentFragment.setArguments(bundle);

//                        Toast.makeText(HomeActivity.this, "sending data userName = "+user.getName(), Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(HomeActivity.this,"Data does not exist",Toast.LENGTH_SHORT).show();
                   // apartmentCode = "Some error occured try again to share the code";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(HomeActivity.this,"Error in data fetching",Toast.LENGTH_SHORT).show();
            }
        });

        // Bottom navigation bar code
        bottomNavigationView = findViewById(R.id.bottom_navigationBar);

        // default selected item from menu
        bottomNavigationView.setSelectedItemId(R.id.adminProfile);
        changeFragment(profileFragment,true);

        // method to change the fragment based on the menu item selected
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {

                switch (item.getItemId()){

                    case R.id.adminProfile:
                        changeFragment(profileFragment,false);
                        return true;

                    case R.id.updateMaintenance:
                        changeFragment(maintenanceUpdateFragment,false);
                        return true;

                    case R.id.adminPayment:
                        changeFragment(paymentFragment,false);
                        return true;

                    case R.id.adminMaintenanceDetails:
                        changeFragment(recordFragment,false);
                        return true;

                    case R.id.adminMembers:
                        changeFragment(MembersFragment,false);
                        return true;

                }
                return false;
            }
        });

        // for notification
        FirebaseMessaging.getInstance().subscribeToTopic("notify");// to get notification using firebase.
    }

    // method to change the Fragment
    void changeFragment(Fragment fragment, boolean flag)
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        if(flag)
            ft.add(R.id.frameLayout,fragment);
        else
            ft.replace(R.id.frameLayout,fragment);

        ft.commit();
    }


    // code for back button pressed which pops up a dialog box
    @Override
    public void onBackPressed() // function for back press
    {

        // below code will show the dialog box when user back presses
        AlertDialog.Builder exitDialog = new AlertDialog.Builder(HomeActivity.this);
        exitDialog.setTitle("Exit")
                .setIcon(R.drawable.ic_baseline_exit_to_app_24)
                .setMessage("Are you sure you want to exit app ? ")
                .setPositiveButton("Yes",new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialogInterface.cancel();
                    }
                })
                .show();
    }
}