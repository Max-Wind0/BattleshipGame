package com.example.battleshipgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button easyOpponentModeButton = findViewById(R.id.easy_button);
        Button normalOpponentModeButton = findViewById(R.id.normal_button);
        Button multiplayerModeButton = findViewById(R.id.multiplayer_button);

        easyOpponentModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GameActivity.class).putExtra("mode", "easyOpponent"));
            }
        });

        normalOpponentModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GameActivity.class).putExtra("mode", "normalOpponent"));
            }
        });

        multiplayerModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GameActivity.class).putExtra("mode", "multiplayer"));
            }
        });
    }
}
