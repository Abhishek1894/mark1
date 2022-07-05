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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.ArrayList;


public class PaymentFragment extends Fragment  implements PaymentResultListener{



    Button paybtn;
    TextView paytext;

    TextView email , amount , name , contact;

    String userName,userEmail,userPhone,aptCode;

    Spinner monthSpinner;

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
        monthSpinner = view.findViewById(R.id.spinnerSelectMonth);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();

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

        ArrayAdapter<String> monthSpinnerAdapter = new ArrayAdapter<>(getActivity(), com.razorpay.R.layout.support_simple_spinner_dropdown_item,monthList);
        monthSpinner.setAdapter(monthSpinnerAdapter);

        // to show the correct maintenance cost of particular month
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                reference.child("payments").child(monthList.get(i)).child(mail.substring(0,mail.length() - 4)).child("amount").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            String amountCost = snapshot.getValue(String.class);
                            amount.setText(amountCost);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });


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

        paybtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

            Intent intent= new Intent(getActivity(),RazorpayPayment.class);
            startActivity(intent);
                ((Activity) getActivity()).overridePendingTransition(0, 0);

//                makePayment();
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

        // paybtn.setEnabled(false);

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

        LocalDate currentdate = LocalDate.now();
        Month currentMonth = currentdate.getMonth();
        String month = currentMonth.toString();

        // paybtn.setEnabled(false);

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
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }




}