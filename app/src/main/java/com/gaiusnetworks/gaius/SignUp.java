package com.gaiusnetworks.gaius;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class SignUp extends AppCompatActivity {
    private Button customSignupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.signup_activity);
        super.onCreate(savedInstanceState);

        customSignupButton = (Button) findViewById(R.id.custom_signup_button);
        customSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);

                startActivity(i);
            }
        });
    }
}

