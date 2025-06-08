package com.example.pratica4a;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensorLuz, sensorpProximidade;
    private float valorLuz, valorProximidade;
    private Switch switchLuz, switchVibracao;
    private LanternaHelper lanternaHelper;
    private MotorHelper motorHelper;

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_VIBRATE_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.VIBRATE}, REQUEST_VIBRATE_PERMISSION);
        }

        lanternaHelper = new LanternaHelper(this);
        motorHelper = new MotorHelper(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorLuz = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorpProximidade = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        switchLuz = findViewById(R.id.switchLanterna);
        switchVibracao = findViewById(R.id.switchVibracao);

        if(sensorLuz != null && sensorpProximidade != null) {
            float luz = sensorLuz.getMaximumRange();
            Log.i("SENSOR_LUZ", "valor: " + luz);
        } else {
            Toast.makeText(this, "Sensores inexistentes", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão concedida!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão negada!", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_VIBRATE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão de vibração concedida!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão de vibração negada!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(sensorLuz != null && sensorpProximidade != null) {
            sensorManager.registerListener(this, sensorLuz, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, sensorpProximidade, SensorManager.SENSOR_DELAY_GAME);
            Log.i("SENSOR_INICIOU", sensorLuz.getName() + " e " + sensorpProximidade.getName());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        Log.i("SENSOR_PAROU", "Parou sensores");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor s = event.sensor;
        if(s.getType() == Sensor.TYPE_LIGHT) {
            valorLuz = event.values[0];
            Log.i("SENSOR_LUZ", "Valor: " + valorLuz);
        }
        if(s.getType() == Sensor.TYPE_PROXIMITY) {
            valorProximidade = event.values[0];
            Log.i("SENSOR_PROXIMIDADE", "Valor: " + valorProximidade);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void classificarLeituras(View v) {
        Intent it = new Intent("pratica4b");
        it.putExtra("luz", valorLuz);
        it.putExtra("proximidade", valorProximidade);
        startActivityForResult(it, 10);
    }

    @Override
    protected void onActivityResult(int codigoRequisicao, int codigoResultado, Intent it) {

        if(codigoRequisicao == 10) {
            String luz = it.getStringExtra("luz");
            String proximidade = it.getStringExtra("proximidade");

            if(codigoResultado == 1) {
                if("baixa".equals(luz)) {
                    switchLuz.setChecked(true);
                    lanternaHelper.ligar();
                } else {
                    switchLuz.setChecked(false);
                    lanternaHelper.desligar();
                }
                if("distante".equals(proximidade)) {
                    switchVibracao.setChecked(true);

                    new Handler().postDelayed(() -> {
                        motorHelper.iniciarVibracao();
                        Log.i("VIBRATION", "Vibração iniciada  " + proximidade);
                    }, 300);
                } else {
                    switchVibracao.setChecked(false);
                    motorHelper.pararVibracao();
                    Log.i("VIBRATION", "Vibração iniciada " + proximidade);
                }
            }
        }
    }
}