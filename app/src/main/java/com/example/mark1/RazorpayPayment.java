package com.example.mark1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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
import com.razorpay.Checkout;
import com.razorpay.ExternalWalletListener;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultListener;
import com.razorpay.PaymentResultWithDataListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.time.LocalDate;
import java.time.Month;

public class RazorpayPayment extends AppCompatActivity implements PaymentResultListener
{
    TextView paytext;

    String name,phoneNo,userEmail,aptcode,amount,status,balance,updatedBalance;

    // variable to store month selected by the user
    String selectedMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_razorpay_payment);

        // To ensure faster loading of the Checkout form, call this method as early as possible in your checkout flow.
        Checkout.preload(getApplicationContext());

        paytext=(TextView)findViewById(R.id.paytext);

        // getting data from previous fragment
        Intent intent = getIntent();
        selectedMonth = intent.getStringExtra("month");
        amount = intent.getStringExtra("amount");

        Toast.makeText(RazorpayPayment.this,selectedMonth + " " + amount,Toast.LENGTH_LONG).show();


        // fetching user data from database
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

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
                    if(user!=null)
                    {
                        //passing User object to other fragments
                        name = user.getName();
                        // userEmail = user.getEmail();
                        phoneNo = user.getPhoneNo();
                        status = user.getStatus();
                        aptcode = user.getAptCode();

                    }
                }
                else
                {
                    Toast.makeText(RazorpayPayment.this,"Data does not exist",Toast.LENGTH_SHORT).show();
                    // apartmentCode = "Some error occured try again to share the code";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(RazorpayPayment.this,"Error in data fetching",Toast.LENGTH_SHORT).show();
            }
        });

        makepayment();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    // code for razorpay payment
    private void makepayment()
    {

        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_gTM9m9XQSFWHPc");//key which is generated on razorpay account

//        checkout.setImage(R.drawable.logo); // for logo of company
        // You need to pass current activity in order to let Razorpay create CheckoutActivity
        final Activity activity = this;

        try {
            JSONObject options = new JSONObject();

            options.put("name", "MarkTech");
            options.put("description", "Reference No. #123456");
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
//             options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");

            Integer paymentAmount = Integer.parseInt(amount);
            paymentAmount *= 100;

            options.put("amount", String.valueOf(paymentAmount));//300 X 100
            options.put("prefill.email", userEmail);
            options.put("prefill.contact",phoneNo);
            checkout.open(activity, options);
        } catch(Exception e)
        {
            Log.e("TAG", "Error in starting Razorpay Checkout", e);
        }
    }


    @Override
    public void onPaymentSuccess(String s)
    {
        paytext.setBackgroundResource(R.color.green);
        paytext.setText("Payment Successful");
        Toast.makeText(this, "Successful payment ID :"+s, Toast.LENGTH_SHORT).show();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();

        // updating the balance of the apartment
        reference.child("apartments").child(aptcode).child("balance").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists())
                {
                    balance = snapshot.getValue(String.class);

                    // updates the balance when user make payments
                    setBalance(balance);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onPaymentError(int i, String s)
    {
        paytext.setBackgroundResource(R.color.red);
        paytext.setText("Payment Failed");
    }


    // method to increment the balance in database
    private void setBalance(String balance)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();

        int bal = Integer.parseInt(balance);
        bal = bal + Integer.parseInt(amount);
        updatedBalance = String.valueOf(bal);

        Toast.makeText(this,updatedBalance,Toast.LENGTH_LONG).show();

        reference.child("apartments").child(aptcode).child("balance").setValue(updatedBalance);

        // updating the flag value to indicate maintenance is paid
        reference.child("payments").child(selectedMonth).child(userEmail.substring(0,userEmail.length() - 4)).child("status").setValue(true);
    }

}