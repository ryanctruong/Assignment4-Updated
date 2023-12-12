package com.example.assignment4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoseScreen extends AppCompatActivity {

    TextView text;
    Button button;
    View.OnClickListener switchButton = v -> {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lose_screen);

        text = findViewById(R.id.text90);
        button = findViewById(R.id.button);

        button.setOnClickListener(switchButton);
    }
}