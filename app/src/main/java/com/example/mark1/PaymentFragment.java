package com.example.mark1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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


public class PaymentFragment extends Fragment  implements PaymentResultListener{



    Button paybtn;
    TextView paytext;

    EditText email , amount , name , contact;

    String userName,userEmail,userPhone,aptCode;

    public PaymentFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_payment, container, false);



        // ------------------------------------- RazorPay Code ----------------------------------------

        // To ensure faster loading of the Checkout form, call this method as early as possible in your checkout flow.
        Checkout.preload(getActivity());

        paytext=view.findViewById(R.id.paytext);
        paybtn=view.findViewById(R.id.paybtn);
        name = view.findViewById(R.id.editTextPaymentName);
        amount = view.findViewById(R.id.editTextPaymentAmount);
        contact = view.findViewById(R.id.editTextPaymentContactNo);
        email = view.findViewById(R.id.editTextPaymentEmail);

        // Bundle to fetch information of user
        Bundle bundle;
        bundle = getArguments();
        String apartmentCode = bundle.getString("userAptCode");

        userName = bundle.getString("userName");
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        userPhone = bundle.getString("userPhoneNo");

        aptCode = bundle.getString("userAptCode");

        name.setText(userName);
        email.setText(userEmail);
        contact.setText(userPhone);

        name.setEnabled(false);
        email.setEnabled(false);
        contact.setEnabled(false);
        amount.setEnabled(false);

        // code to fetch the current maintenance of the building
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();

        reference.child("apartments").child(apartmentCode).child("maintenance").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    String maintenance = snapshot.getValue(String.class);
                    amount.setText(String.valueOf(maintenance));
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


        paybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePayment();
            }
        });

        return view;
    }


    private void makePayment()
    {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_gTM9m9XQSFWHPc");//key which is generated on razorpay account

//        checkout.setImage(R.drawable.logo); // for logo of company
        // You need to pass current activity in order to let Razorpay create CheckoutActivity
        final PaymentFragment activity = this;

        try {
            JSONObject options = new JSONObject();

            options.put("name", "MarkTech");
            options.put("description", "Reference No. #123456");
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
//             options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");

            Integer paymentAmount = Integer.parseInt(amount.getText().toString());
            paymentAmount *= 100;

            options.put("amount", String.valueOf(paymentAmount));//300 X 100
            options.put("prefill.email", userEmail);
            options.put("prefill.contact",userPhone);
            checkout.open(getActivity(), options);
        }
        catch(Exception e)
        {
            Log.e("TAG", "Error in starting Razorpay Checkout", e);
        }
    }

    @Override
    public void onPaymentSuccess(String s)
    {
        String str = "Successful payment ID :"+s;
        paytext.setText(str);

        LocalDate currentdate = LocalDate.now();
        Month currentMonth = currentdate.getMonth();
        String month = currentMonth.toString();

        paybtn.setEnabled(false);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();

        reference.child("apartments").child(aptCode).child("balance").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists())
                {
                    String balance = snapshot.getValue(String.class);
                    Integer bal = Integer.parseInt(balance);
                    bal += Integer.parseInt(amount.getText().toString());
                    reference.child("apartments").child(aptCode).child("balance").setValue(String.valueOf(bal));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Toast.makeText(getActivity(),month,Toast.LENGTH_SHORT).show();
        Toast.makeText(getActivity(), "Payment Successful", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPaymentError(int i, String s)
    {
        String str = "Failed and cause is :"+s;
        paytext.setText(str);
        Toast.makeText(getActivity(),"Payment Failed",Toast.LENGTH_SHORT).show();
    }




}