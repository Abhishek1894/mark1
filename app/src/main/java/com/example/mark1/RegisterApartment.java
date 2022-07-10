package com.example.mark1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class RegisterApartment extends AppCompatActivity
{
    // UI components
    EditText name;
    EditText phoneNo;
    EditText email;
    EditText password;
    EditText upiId;
    EditText apartmentName;
    Button next;

    // stores unique apartment code
    String aptCode;

    // Firebase related fields
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_apartment);

        // Finding UI components of
        name = findViewById(R.id.editTextRegisterName);
        phoneNo = findViewById(R.id.editTextRegisterPhoneNo);
        email = findViewById(R.id.editTextRegisterEmail);
        password = findViewById(R.id.editTextRegisterPassword);
        next = findViewById(R.id.buttonRegisterNext);
        apartmentName = findViewById(R.id.editTextRegisterApartmentName);


        // object for action-bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setTitle("Register Apartment");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // A condition should be added to check whether all data fields are filled or not.
        next.setOnClickListener(v ->
        {
            // collecting data from UI components and storing into variables
            String userName = name.getText().toString();
            String userPhoneNo = phoneNo.getText().toString();
            String userEmail = email.getText().toString();
            String userPassword = password.getText().toString();
            String userApartmentName = apartmentName.getText().toString();

            // testcase
            if(userName.equals("") || userPhoneNo.equals("") || userEmail.equals("") || userPassword.equals("") || userApartmentName.equals(""))
            {
                Toast.makeText(RegisterApartment.this,"Enter All Input Fields",Toast.LENGTH_SHORT).show();
                return;
            }

            // testcase
            if(!userEmail.contains("@gmail.com"))
            {
                Toast.makeText(RegisterApartment.this,"Enter valid gmail account",Toast.LENGTH_SHORT).show();
                email.setText("");
                return;
            }

            // testcase
            if(password.length() < 8)
            {
                Toast.makeText(RegisterApartment.this,"Password should be atleast 8 characters long ",Toast.LENGTH_SHORT).show();
                return;
            }

            // method for registering user
            registerAdmin(userName,userPhoneNo,userEmail,userPassword,userApartmentName);

        });


    }

    // method for registering the user
    void registerAdmin(String name, String phoneNo, String email, String password,String apartmentName)
    {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // push() is used so that unique code can be generated for every apartment registered
                            aptCode = reference.push().getKey(); // very imp

                            Toast.makeText(RegisterApartment.this, "Apartment code : " + aptCode, Toast.LENGTH_SHORT).show();

                            HashMap<String,Object> user = new HashMap<>();
                            user.put("name",name);
                            user.put("phoneNo",phoneNo);
                            user.put("aptCode",aptCode);
                            user.put("status","admin");

                            HashMap<String,Object> apartment = new HashMap<>();
                            apartment.put("name",apartmentName);
                            apartment.put("balance","0");
                            apartment.put("maintenance","1");

                            // code for adding new user in real-time database
                            reference.child("users").child(email.substring(0,email.length() - 4)).setValue(user);
                            reference.child("apartments").child(aptCode).setValue(apartment);

                            // code for adding users in payment records
                            HashMap<String,Object> record = new HashMap<>();
                            record.put("name",name);
                            record.put("aptcode",aptCode);
                            record.put("amount","1");
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


                            Toast.makeText(RegisterApartment.this,"Data Saved Successfully and ",Toast.LENGTH_SHORT).show();

                            // this will take user to login screen
                            Intent intent = new Intent(RegisterApartment.this,LoginActivity.class);
                            intent.putExtra("email",email);
                            intent.putExtra("password",password);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            // toast for unsuccessful registration
                            Toast.makeText(RegisterApartment.this, "Registration unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}