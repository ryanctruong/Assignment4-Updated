package com.example.assignment4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AddWord extends AppCompatActivity {
    Button switch_bt, add_bt;
    EditText enter_word_et;
    FirebaseDatabase database;
    DatabaseReference myDB;

    View.OnClickListener switchButton = v -> {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    };
    //Tony helped with this part
    View.OnClickListener addButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String s = enter_word_et.getText().toString().toLowerCase().trim();
            if (!checkWord(s)) {
                Toast.makeText(getApplicationContext(), "Error: Empty field or invalid length", Toast.LENGTH_LONG).show();
            }
            else {
                Query query = myDB.child("words").orderByValue().equalTo(s);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Log.i("EXISTS", "True");
                            Toast.makeText(getApplicationContext(), "Error: Word already exists in word bank!",
                                    Toast.LENGTH_LONG).show();
                        }
                        else {
                            Log.i("EXISTS", "False");
                            myDB.child("words").push().setValue(s);
                            Toast.makeText(getApplicationContext(), "Word has been added to word bank!",
                                    Toast.LENGTH_LONG).show();
                            enter_word_et.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        switch_bt = findViewById(R.id.back_BT);
        add_bt = findViewById(R.id.add_word_ADACT_BT);
        enter_word_et = findViewById(R.id.enter_word_ET);

        switch_bt.setOnClickListener(switchButton);
        add_bt.setOnClickListener(addButton);

        database = FirebaseDatabase.getInstance();
        myDB = database.getReference();
    }
    public boolean checkWord(String word) {
        if(word == null || word.length() != 5){
            return false;
        }
        word = word.toUpperCase();
        for(int i = 0; i < word.length(); i++){
            if(Character.isDigit(word.charAt(i))){
                return false;
            }
        }

        return true;
    }
}