package com.example.rishabh.you4to;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText url;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        url = (EditText)findViewById(R.id.url);
        submit = (Button) findViewById(R.id.submit_url);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.submit_url){
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, url.getText().toString());
            startActivity(intent);
        }
    }
}
