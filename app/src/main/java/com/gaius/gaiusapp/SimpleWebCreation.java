package com.gaius.gaiusapp;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

class SimpleWebCreation extends AppCompatActivity implements View.OnClickListener {
    List<Item> itemList;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_content_creation);

        recyclerView = findViewById(R.id.simple_recylcerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();

        //adding the product to product list
        itemList.add(new Item (0, "text", "Hello World Hello World Hello World Hello World Hello World",null, null));
        itemList.add(new Item (1, "text", "Hello World Hello World Hello World Hello World Hello World",null, null));
        itemList.add(new Item (2, "text", "Hello World Hello World Hello World Hello World Hello World",null, null));
        itemList.add(new Item (3, "text", "Hello World Hello World Hello World Hello World Hello World",null, null));
        itemList.add(new Item (4, "text", "Hello World Hello World Hello World Hello World Hello World",null, null));
        itemList.add(new Item (5, "text", "Hello World Hello World Hello World Hello World Hello World",null, null));
        itemList.add(new Item (6, "text", "Hello World Hello World Hello World Hello World Hello World",null, null));

        //creating adapter object and setting it to recyclerview
        ItemsAdapter adapter = new ItemsAdapter(getApplication(), itemList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {

    }
}

