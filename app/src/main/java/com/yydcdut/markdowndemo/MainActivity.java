package com.yydcdut.markdowndemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("RxMarkdown");
        findViewById(R.id.btn_edit_show).setOnClickListener(this);
        findViewById(R.id.btn_edit_show_rx).setOnClickListener(this);
        findViewById(R.id.btn_compare).setOnClickListener(this);
        findViewById(R.id.btn_compare_rx).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_compare:
            case R.id.btn_compare_rx: {
                Intent intent = new Intent(this, CompareActivity.class);
                intent.putExtra("is_rx", v.getId() != R.id.btn_compare);
                startActivity(intent);
                break;
            }
            case R.id.btn_edit_show:
            case R.id.btn_edit_show_rx: {
                Intent intent = new Intent(this, EditActivity.class);
                intent.putExtra("is_rx", v.getId() != R.id.btn_edit_show);
                startActivity(intent);
            }
            break;
        }
    }
}
