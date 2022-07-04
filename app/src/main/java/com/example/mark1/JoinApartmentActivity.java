package com.example.mark1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class JoinApartmentActivity extends AppCompatActivity
{
    EditText name;          // edittext for name
    EditText email;         // edittext for email
    EditText phoneNo;       // edittext for phone no
    EditText password;      // edittext for password
    EditText apartmentCode; // edittext for apartment code
    Button buttonEnterJoin; // button to join a apartment

    FirebaseAuth auth = FirebaseAuth.getInstance(); // Firebase auth object for authentication purpose

    // Fields related to real-time database of firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_apartment);

        // Finding id's of all UI Components
        name = findViewById(R.id.editTextEnterName);
        email = findViewById(R.id.editTextEnterEmail);
        phoneNo = findViewById(R.id.editTextEnterPhoneNo);
        password = findViewById(R.id.editTextEnterPassword);
        apartmentCode = findViewById(R.id.editTextEnterApartmentCode);
        buttonEnterJoin = findViewById(R.id.buttonEnterJoin);

        // Sets title to actionbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setTitle("Join Apartment");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Button to join the apartment
        buttonEnterJoin.setOnClickListener(v ->
        {
            // Collecting data from all UI fields in variables
            String userName = name.getText().toString();
            String userPhoneNo = phoneNo.getText().toString();
            String userEmail = email.getText().toString();
            String userPassword = password.getText().toString();
            String userApartmentCode = apartmentCode.getText().toString();

            // testcase
            if(userName.equals("") || userPhoneNo.equals("") || userEmail.equals("") || userPassword.equals("") || userApartmentCode.equals(""))
            {
                Toast.makeText(JoinApartmentActivity.this,"Enter All Input Fields",Toast.LENGTH_SHORT).show();
                return;
            }

            // testcase
            if(!userEmail.contains("@gmail.com"))
            {
                Toast.makeText(JoinApartmentActivity.this,"Enter valid gmail account",Toast.LENGTH_SHORT).show();
                email.setText("");
                return;
            }

            // testcase
            if(password.length() < 8)
            {
                Toast.makeText(JoinApartmentActivity.this,"Password should be atleast 8 characters long ",Toast.LENGTH_SHORT).show();
                return;
            }

            // code to check weather apartment code added by the user is valid or not
            reference.child("apartments").child(userApartmentCode).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.exists())
                    {
                        // As apartment code is valid user will be added to to particular apartment
                        String maintenance = snapshot.child("maintenance").getValue(String.class);
                        Toast.makeText(JoinApartmentActivity.this,maintenance,Toast.LENGTH_LONG).show();
                        joinApartment(userName,userPhoneNo,userEmail,userPassword,userApartmentCode,maintenance);
                    }
                    else
                    {
                        Toast.makeText(JoinApartmentActivity.this, "Invalid Apartment Code", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {

                }
            });

        });
    }

    // This method will be invoked when user clicks on join apartment button
    public void joinApartment(String name, String phoneNo, String email,String password, String aptCode, String maintenance)
    {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(JoinApartmentActivity.this, "Apartment code : " + aptCode, Toast.LENGTH_SHORT).show();

                            HashMap<String,Object> user = new HashMap<>();
                            user.put("name",name);
                            user.put("phoneNo",phoneNo);
                            user.put("aptCode",aptCode);
                            user.put("status","member");


                            reference.child("users").child(email.substring(0,email.length() - 4)).setValue(user);
                            Toast.makeText(JoinApartmentActivity.this,"Data Saved Successfully",Toast.LENGTH_SHORT).show();

                            // code for adding users in payment records
                            HashMap<String,Object> record = new HashMap<>();
                            record.put("name",name);
                            record.put("aptcode",aptCode);
                            record.put("amount","0");
                            record.put("status",false);

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

                            for(String data : monthList)
                            {
                                reference.child("payments").child(data).child(email.substring(0,email.length() - 4)).setValue(record);
                            }

                            // After the data is registered user will be directed to login page
                            Intent intent = new Intent(JoinApartmentActivity.this,LoginActivity.class);
                            intent.putExtra("email",email);
                            intent.putExtra("password",password);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(JoinApartmentActivity.this, "Registration unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}