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


public class PaymentFragment extends Fragment
{

    Button paybtn;
    TextView paytext;

    TextView email , amount , name , contact;

    String userName,userEmail,userPhone,aptCode;

    Spinner monthSpinner;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference = database.getReference();

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
            public void onClick(View view)
            {
                // checking whether the month is valid or not and checking whether payment is already done or not
                LocalDate date = LocalDate.now();
                Month month = date.getMonth();
                int index1 = monthList.indexOf(month.toString());

                String selectedMonth = (String)monthSpinner.getSelectedItem();
                int index2 = monthList.indexOf(selectedMonth);

                if(index1 < index2)
                {
                    Toast.makeText(getActivity(), "You can only make payment of current and previous months", Toast.LENGTH_LONG).show();
                    return;
                }


                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                reference.child("payments").child(selectedMonth).child(email.substring(0,email.length() - 4)).child("status").addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            Boolean status = snapshot.getValue(Boolean.class);

                            if(status)
                            {
                                Toast.makeText(getActivity(),"You have already paid maintenance cost of " + selectedMonth, Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                Intent intent = new Intent(getActivity(), RazorpayPayment.class);
                                intent.putExtra("month",selectedMonth);
                                intent.putExtra("name",name.getText().toString());
                                intent.putExtra("amount",amount.getText().toString());
                                intent.putExtra("contact",contact.getText().toString());
                                startActivity(intent);
                                ((Activity) getActivity()).overridePendingTransition(0, 0);
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