package ido.dotan.physicspendelumido;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalculatorFragment extends Fragment {

    private static final String PREFS_NAME = "PendulumPrefs";
    private static final String KEY_LENGTH = "length";
    private static final String KEY_GRAVITY = "gravity";
    private static final String KEY_PERIOD = "period";

    private EditText editTextLength, editTextGravity, editTextPeriod;
    private RadioGroup radioGroupCalculate;
    private RadioButton radioPeriod, radioGravity, radioLength;
    private Button buttonCalculate, buttonSave;
    private SharedPreferences sharedPreferences;
    private HelperDB helperDB;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calculator, container, false);

        editTextLength = view.findViewById(R.id.editTextLength);
        editTextGravity = view.findViewById(R.id.editTextGravity);
        editTextPeriod = view.findViewById(R.id.editTextPeriod);
        radioGroupCalculate = view.findViewById(R.id.radioGroupCalculate);
        radioPeriod = view.findViewById(R.id.radio_period);
        radioGravity = view.findViewById(R.id.radio_gravity);
        radioLength = view.findViewById(R.id.radio_length);
        buttonCalculate = view.findViewById(R.id.buttonCalculate);
        buttonSave = view.findViewById(R.id.buttonSaveCalculation);

        helperDB = new HelperDB(getContext());
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        loadLastValues();

        radioGroupCalculate.setOnCheckedChangeListener((group, checkedId) -> updateInputFields());
        buttonCalculate.setOnClickListener(v -> calculate());
        buttonSave.setOnClickListener(v -> showSaveConfirmationDialog());

        updateInputFields();

        return view;
    }

    private void updateInputFields() {
        editTextPeriod.setEnabled(!radioPeriod.isChecked());
        editTextGravity.setEnabled(!radioGravity.isChecked());
        editTextLength.setEnabled(!radioLength.isChecked());
    }

    private void calculate() {
        try {
            double l, g, t;
            if (radioPeriod.isChecked()) {
                l = Double.parseDouble(editTextLength.getText().toString());
                g = Double.parseDouble(editTextGravity.getText().toString());
                t = 2 * Math.PI * Math.sqrt(l / g);
                editTextPeriod.setText(String.format(Locale.US, "%.4f", t));
            } else if (radioGravity.isChecked()) {
                l = Double.parseDouble(editTextLength.getText().toString());
                t = Double.parseDouble(editTextPeriod.getText().toString());
                g = (4 * Math.pow(Math.PI, 2) * l) / Math.pow(t, 2);
                editTextGravity.setText(String.format(Locale.US, "%.4f", g));
            } else { // radioLength
                g = Double.parseDouble(editTextGravity.getText().toString());
                t = Double.parseDouble(editTextPeriod.getText().toString());
                l = (Math.pow(t, 2) * g) / (4 * Math.pow(Math.PI, 2));
                editTextLength.setText(String.format(Locale.US, "%.4f", l));
            }
            l = Double.parseDouble(editTextLength.getText().toString());
            g = Double.parseDouble(editTextGravity.getText().toString());
            t = Double.parseDouble(editTextPeriod.getText().toString());
            saveToPrefs(l, g, t);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), R.string.please_enter_valid_values, Toast.LENGTH_SHORT).show();
        }
    }

    private void showSaveConfirmationDialog() {
        try {
            double l = Double.parseDouble(editTextLength.getText().toString());
            double g = Double.parseDouble(editTextGravity.getText().toString());
            double t = Double.parseDouble(editTextPeriod.getText().toString());
            String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

            SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String userCode = userPrefs.getString("user_code", "unknown");

            String message = String.format(Locale.US, 
                "User: %s\nLength (L): %.4f m\nGravity (g): %.4f m/s²\nPeriod (T): %.4f s\nDate: %s\n\nSave this calculation?", 
                userCode, l, g, t, date);

            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Save")
                    .setMessage(message)
                    .setPositiveButton("Save", (dialog, which) -> {
                        helperDB.addCalculation(userCode, l, g, t, date, success -> {
                            if (success) {
                                Toast.makeText(getContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Error saving data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please calculate values first", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToPrefs(double length, double gravity, double period) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(KEY_LENGTH, (float) length);
        editor.putFloat(KEY_GRAVITY, (float) gravity);
        editor.putFloat(KEY_PERIOD, (float) period);
        editor.apply();
    }

    private void loadLastValues() {
        float length = sharedPreferences.getFloat(KEY_LENGTH, 1.0f);
        float gravity = sharedPreferences.getFloat(KEY_GRAVITY, 9.81f);
        float period = sharedPreferences.getFloat(KEY_PERIOD, 0.0f);

        editTextLength.setText(String.valueOf(length));
        editTextGravity.setText(String.valueOf(gravity));
        if (period != 0.0f) {
            editTextPeriod.setText(String.valueOf(period));
        }
    }
}
