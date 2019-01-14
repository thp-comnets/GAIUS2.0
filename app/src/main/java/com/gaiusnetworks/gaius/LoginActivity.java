package com.gaiusnetworks.gaius;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {
    private Button customSigninButton;
    private TextView custom_signup_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.login_activity);
        super.onCreate(savedInstanceState);

        customSigninButton = (Button) findViewById(R.id.custom_signin_button);
        customSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);

                startActivity(i);
            }
        });

        custom_signup_button = (TextView) findViewById(R.id.custom_signup_button);
        custom_signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SignUp.class);

                startActivity(i);
            }
        });
    }
}
