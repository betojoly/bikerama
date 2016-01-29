package br.com.imcom.bikerama;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class LocationServiceActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_service);

        Button startButton = (Button)findViewById(R.id.btnStart);
        Button stopButton = (Button)findViewById(R.id.btnStop);

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                Intent startIntent = new Intent(LocationServiceActivity.this, ForegroundService.class);
                startIntent.setAction(ConstantsService.ACTION.STARTFOREGROUND_ACTION);
                startService(startIntent);
                break;
            case R.id.btnStop:
                Intent stopIntent = new Intent(LocationServiceActivity.this, ForegroundService.class);
                stopIntent.setAction(ConstantsService.ACTION.STOPFOREGROUND_ACTION);
                startService(stopIntent);
                break;

            default:
                break;
        }

    }

}
