package com.elegance.feedback;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by root on 12/4/16.
 */
public class Take_another extends AppCompatActivity {

    Button take_another;
    Button exit;
    Boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_another);

        take_another = (Button)findViewById(R.id.bTakeAnother);
        exit = (Button)findViewById(R.id.bExit);

        take_another.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Load_Form.class);

                i.putExtra("pin", "DEFAULT");

                startActivity(i);
                finish();
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Form_Creation.Questions_Loaded.clear();
                Form_Creation.companyLoaded.clear();
                Form_Creation.User_Details.clear();
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(flag){
            super.onBackPressed();
            Form_Creation.Questions_Loaded.clear();
            Form_Creation.companyLoaded.clear();
            Form_Creation.User_Details.clear();
            finish();
        }
        else {
            Toast.makeText(getApplicationContext(), "Press again to exit",
                    Toast.LENGTH_SHORT).show();
            flag = true;
        }
    }
}
