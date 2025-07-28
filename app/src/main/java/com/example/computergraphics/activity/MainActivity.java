package com.example.computergraphics.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;

import com.example.computergraphics.R;

public class MainActivity extends Activity {
    private EditText source [] = new EditText[3];
    private EditText direct [] = new EditText[3];
    private Button enterButton;
    private Button skipButton;
    public static final String EXTRA_RAY_SOURCE = "com.example.computergraphics.RAY_SOURCE";
    public static final String EXTRA_RAY_DIRECTION = "com.example.computergraphics.RAY_DIRECTION";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main_activity);
//        source[0] = findViewById(R.id.editTextX1);
//        source[1] = findViewById(R.id.editTextY1);
//        source[2] = findViewById(R.id.editTextZ1);
//        direct[0] = findViewById(R.id.editTextX2);
//        direct[1] = findViewById(R.id.editTextY2);
//        direct[2] = findViewById(R.id.editTextZ2);
//        enterButton = findViewById(R.id.enterButton);
//        skipButton = findViewById(R.id.skipButton);
//
//        enterButton.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v){
//                String SourceStr [] = new String [] {
//                        source[0].getText().toString(),
//                        source[1].getText().toString(),
//                        source[2].getText().toString(),
//                };
//                String DirectStr [] = new String [] {
//                        direct[0].getText().toString(),
//                        direct[1].getText().toString(),
//                        direct[2].getText().toString(),
//                };
//                float [] Source = new float [] {
//                        Float.parseFloat(SourceStr[0]),
//                        Float.parseFloat(SourceStr[1]),
//                        Float.parseFloat(SourceStr[2]),
//                };
//                float [] Direct = new float [] {
//                        Float.parseFloat(DirectStr[0]),
//                        Float.parseFloat(DirectStr[1]),
//                        Float.parseFloat(DirectStr[2]),
//                };
//                Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
//                intent.putExtra(EXTRA_RAY_SOURCE, Source);
//                intent.putExtra(EXTRA_RAY_DIRECTION, Direct);
//                startActivity(intent);
//            }
//        });
//        skipButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
//                intent.putExtra(EXTRA_RAY_SOURCE, new float [] { 0f, 0f, 0f });
//                intent.putExtra(EXTRA_RAY_DIRECTION, new float [] { 0f, 0f, 0f });
//                startActivity(intent);
//            }
//        });
        Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
        startActivity(intent);
    }
}


