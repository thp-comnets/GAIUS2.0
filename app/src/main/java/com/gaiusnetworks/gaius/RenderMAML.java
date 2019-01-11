package com.gaiusnetworks.gaius;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

public class RenderMAML extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.maml_page);

        super.onCreate(savedInstanceState);

        final FrameLayout root = findViewById(R.id.root);

    }
}
