package ido.dotan.physicspendelumido;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SensorMeasurementFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isMeasuring = false;
    private boolean isCountingDown = false;

    private Button btnStartStop, btnSave;
    private TextView tvStatus, tvOscillationCount, tvResultPeriod, tvResultFrequency, tvResultLength;

    private final List<Long> magnitudeZeroCrossingTimes = new ArrayList<>();
    private float lastMagnitude = 0;
    private float filteredMagnitude = 0;
    private float runningAverageMagnitude = 9.8f;
    private static final float ALPHA = 0.15f;
    private static final float ADAPT_RATE = 0.01f;

    private HelperDB helperDB;
    private double lastMeasuredPeriod = 0;

    private ToneGenerator toneGenerator;
    private CountDownTimer countDownTimer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_measurement, container, false);

        btnStartStop = view.findViewById(R.id.btn_start_stop);
        btnSave = view.findViewById(R.id.btn_save_measurement);
        tvStatus = view.findViewById(R.id.tv_status);
        tvOscillationCount = view.findViewById(R.id.tv_oscillation_count);
        tvResultPeriod = view.findViewById(R.id.tv_result_period);
        tvResultFrequency = view.findViewById(R.id.tv_result_frequency);
        tvResultLength = view.findViewById(R.id.tv_result_length);

        helperDB = new HelperDB(requireContext());
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        btnStartStop.setOnClickListener(v -> {
            if (isCountingDown || isMeasuring) {
                stopEverything();
            } else {
                startCountdown();
            }
        });

        btnSave.setOnClickListener(v -> showSaveConfirmationDialog());

        return view;
    }

    private void startCountdown() {
        isCountingDown = true;
        btnStartStop.setText(R.string.stop_measurement); // Allow cancel
        btnSave.setVisibility(View.GONE);
        tvResultPeriod.setText("");
        tvResultFrequency.setText("");
        tvResultLength.setText("");
        
        countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000) + 1;
                tvStatus.setText(getString(R.string.countdown, secondsLeft));
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
            }

            @Override
            public void onFinish() {
                isCountingDown = false;
                startMeasurement();
            }
        }.start();
    }

    private void startMeasurement() {
        if (accelerometer == null) {
            Toast.makeText(getContext(), "Sensor not available", Toast.LENGTH_SHORT).show();
            return;
        }
        isMeasuring = true;
        magnitudeZeroCrossingTimes.clear();
        lastMagnitude = 0;
        filteredMagnitude = 0;
        runningAverageMagnitude = 9.8f;
        
        tvStatus.setText(R.string.measuring);
        tvOscillationCount.setText(getString(R.string.oscillation_count, 0));
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    private void stopEverything() {
        if (countDownTimer != null) countDownTimer.cancel();
        isCountingDown = false;
        isMeasuring = false;
        sensorManager.unregisterListener(this);
        btnStartStop.setText(R.string.start_measurement);
        tvStatus.setText(R.string.ready);
        calculateResults();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isMeasuring) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        float rawMagnitude = (float) Math.sqrt(x * x + y * y + z * z);

        filteredMagnitude = ALPHA * rawMagnitude + (1 - ALPHA) * filteredMagnitude;
        runningAverageMagnitude = ADAPT_RATE * filteredMagnitude + (1 - ADAPT_RATE) * runningAverageMagnitude;

        long currentTime = System.currentTimeMillis();

        if (lastMagnitude < runningAverageMagnitude && filteredMagnitude >= runningAverageMagnitude) {
            magnitudeZeroCrossingTimes.add(currentTime);
            int pendulumOscillations = magnitudeZeroCrossingTimes.size() / 2;
            tvOscillationCount.setText(getString(R.string.oscillation_count, pendulumOscillations));

            // Stop after 5 full periods (11 magnitude crossings: 0, 0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5)
            if (magnitudeZeroCrossingTimes.size() >= 11) {
                finishAutoMeasurement();
            }
        }

        lastMagnitude = filteredMagnitude;
    }

    private void finishAutoMeasurement() {
        isMeasuring = false;
        sensorManager.unregisterListener(this);
        
        // Double beep
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 400);
        
        tvStatus.setText(R.string.measurement_done);
        btnStartStop.setText(R.string.show_results);
    }

    private void calculateResults() {
        if (magnitudeZeroCrossingTimes.size() < 3) {
            return;
        }

        long totalDuration = magnitudeZeroCrossingTimes.get(magnitudeZeroCrossingTimes.size() - 1) - magnitudeZeroCrossingTimes.get(0);
        int numHalfPeriods = magnitudeZeroCrossingTimes.size() - 1;
        double avgMagPeriod = (double) totalDuration / numHalfPeriods / 1000.0;
        
        lastMeasuredPeriod = 2.0 * avgMagPeriod;
        double frequency = 1.0 / lastMeasuredPeriod;

        double g = 9.80665;
        double length = (g * lastMeasuredPeriod * lastMeasuredPeriod) / (4 * Math.PI * Math.PI);

        tvResultPeriod.setText(getString(R.string.result_period, lastMeasuredPeriod));
        tvResultFrequency.setText(getString(R.string.result_frequency, frequency));
        tvResultLength.setText(getString(R.string.result_length, length));
        tvResultLength.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);
    }

    private void showSaveConfirmationDialog() {
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userCode = userPrefs.getString("user_code", "unknown");

        double g = 9.80665;
        double l = (g * lastMeasuredPeriod * lastMeasuredPeriod) / (4 * Math.PI * Math.PI);

        String message = String.format(Locale.US, 
            "User: %s\nPeriod (T): %.4f s\nEstimated Length (L): %.4f m\nDate: %s\n\nSave to history?", 
            userCode, lastMeasuredPeriod, l, date);

        new AlertDialog.Builder(requireContext())
                .setTitle("Save Measurement")
                .setMessage(message)
                .setPositiveButton("Save", (dialog, which) -> helperDB.addCalculation(userCode, l, g, lastMeasuredPeriod, date, success -> {
                    if (success) Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT).show();
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) toneGenerator.release();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopEverything();
    }
}
