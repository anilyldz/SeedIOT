package com.seediot.iot.seediot;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText username_et = findViewById(R.id.username_et);
        final EditText password_et = findViewById(R.id.password_et);
        final Button login_btn = findViewById(R.id.login_btn);
        final TextView error_tv = findViewById(R.id.error_tv);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = username_et.getText().toString();
                String password = password_et.getText().toString();
                if (username.equals("anil") &&  password.equals("1234")   ){

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
                else {
                    error_tv.setText("Başarısız");
                }
            }
        });



    }
}
