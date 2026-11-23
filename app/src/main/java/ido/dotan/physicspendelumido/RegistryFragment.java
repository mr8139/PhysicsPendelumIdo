package ido.dotan.physicspendelumido;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RegistryFragment extends Fragment {

    private EditText codeEditText;
    private EditText passwordEditText;
    private Button registerButton;
    private TextView messageTextView;
    private HelperDB helperDB;
    private int failedAttempts = 0;
    private OnRegistrationSuccessListener registrationSuccessListener;

    public interface OnRegistrationSuccessListener {
        void onRegistrationSuccess();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            registrationSuccessListener = (OnRegistrationSuccessListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnRegistrationSuccessListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registry, container, false);

        codeEditText = view.findViewById(R.id.code_edittext);
        passwordEditText = view.findViewById(R.id.password_edittext);
        registerButton = view.findViewById(R.id.register_button);
        messageTextView = view.findViewById(R.id.message_textview);
        helperDB = new HelperDB(getContext());

        registerButton.setOnClickListener(v -> registerUser());

        return view;
    }

    private void registerUser() {
        String code = codeEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (code.isEmpty()) {
            messageTextView.setText(R.string.username_cannot_be_empty);
            return;
        }

        if (password.length() < 6) {
            messageTextView.setText(R.string.password_too_short);
            return;
        }

        helperDB.isCodeExists(code, exists -> {
            if (!exists) {
                helperDB.addUser(code, password, success -> {
                    if (success) {
                        SharedPreferences sharedPreferences = requireActivity().
                                getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("user_code", code);
                        editor.apply();

                        Toast.makeText(getContext(), R.string.registration_successful, Toast.LENGTH_SHORT).show();
                        registrationSuccessListener.onRegistrationSuccess();
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new LogInFragment())
                                .commit();
                    } else {
                        messageTextView.setText(R.string.registration_failed);
                    }
                });
            } else {
                messageTextView.setText(R.string.code_already_exists);
                handleFailedAttempt();
            }
        });
    }

    private void handleFailedAttempt() {
        failedAttempts++;
        if (failedAttempts >= 3) {
            if(getActivity() != null) {
                getActivity().finish();
            }
        }
    }
}
