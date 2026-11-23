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

public class LogInFragment extends Fragment {

    private EditText codeEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView messageTextView;
    private HelperDB helperDB;
    private OnLoginSuccessListener loginSuccessListener;

    public interface OnLoginSuccessListener {
        void onLoginSuccess();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            loginSuccessListener = (OnLoginSuccessListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnLoginSuccessListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        codeEditText = view.findViewById(R.id.code_edittext);
        passwordEditText = view.findViewById(R.id.password_edittext);
        loginButton = view.findViewById(R.id.login_button);
        messageTextView = view.findViewById(R.id.message_textview);
        helperDB = new HelperDB(getContext());

        loginButton.setOnClickListener(v -> loginUser());

        return view;
    }

    private void loginUser() {
        String code = codeEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (password.isEmpty()) {
            messageTextView.setText(R.string.please_enter_password);
            return;
        }

        helperDB.checkUser(code, password, success -> {
            if (success) {
                SharedPreferences sharedPreferences = requireActivity().
                        getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("user_code", code);
                editor.apply();

                Toast.makeText(getContext(), R.string.login_successful, Toast.LENGTH_SHORT).show();
                loginSuccessListener.onLoginSuccess();
            } else {
                messageTextView.setText(R.string.invalid_code_or_password);
            }
        });
    }
}
