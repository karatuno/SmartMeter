package com.example.smartmete;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sp = getApplicationContext().getSharedPreferences("ID",0);
        String currentUser = sp.getString("id",null);
        if(currentUser != null) {
            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
            i.putExtra("id", currentUser);
            startActivity(i);
            finish();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText id = findViewById(R.id.id_editText);
        final EditText mobileno = findViewById(R.id.mobilenumber_editText);
        final Button login_button = findViewById(R.id.login_button);
        final ProgressDialog progressDialog = new ProgressDialog(this);

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String idstring = id.getText().toString();
                final String mobilestring = mobileno.getText().toString();

                if(TextUtils.isEmpty(idstring) || TextUtils.isEmpty(mobilestring)) {
                    Toast.makeText(LoginActivity.this, "All fields must be filled!", Toast.LENGTH_SHORT).show();
                }

                else {

                    login_button.setBackgroundColor(getResources().getColor(android.R.color.white));
                    login_button.setTextColor(getResources().getColor(R.color.colorPrimary));


                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            progressDialog.setTitle("Logging in!");
                            progressDialog.show();
                            String vmobile = String.valueOf(dataSnapshot.child(idstring).child("VerifiedMobile No").getValue());
                            String name = String.valueOf(dataSnapshot.child(idstring).child("Name").getValue());

                            if(vmobile.matches("")) {

                                Toast.makeText(LoginActivity.this, "Invalid id!", Toast.LENGTH_SHORT).show();

                            }
                            else {
                                if (vmobile.matches(mobilestring)) {
                                    SharedPrefesSAVE(idstring);
                                    progressDialog.dismiss();
                                    Toast.makeText(LoginActivity.this, "Login successful!  WELCOME "+name, Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                                    i.putExtra("id", idstring);
                                    startActivity(i);
                                    finish();
                                }

                                else {
                                    progressDialog.dismiss();
                                    Toast.makeText(LoginActivity.this, "Mobile and id doesnt match!!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    public void SharedPrefesSAVE(String Name){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("ID", 0);
        SharedPreferences.Editor prefEDIT = prefs.edit();
        prefEDIT.putString("id", Name);
        prefEDIT.apply();
        prefEDIT.commit();
    }
}
