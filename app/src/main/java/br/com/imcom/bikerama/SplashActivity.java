package br.com.imcom.bikerama;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class SplashActivity extends AppCompatActivity {

    private static int TEMPO_SPLASH = 5000; //5000 milissegundos, ou 5 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
           // Carrega a imagem

            @Override
            public void run() {
                // Este método executa por 5 segundos antes de abrir a MainActivity
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                // Fecha a Activity atual
                finish();
            }
        }, TEMPO_SPLASH);
    }
}
