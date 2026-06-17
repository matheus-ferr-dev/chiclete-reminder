package com.chiclete.reminder.app;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.chiclete.reminder.app.api.ApiService;
import com.chiclete.reminder.app.api.RetrofitClient;
import com.chiclete.reminder.app.dto.CreateUserRequest;
import com.chiclete.reminder.app.dto.LoginRequest;
import com.chiclete.reminder.app.dto.LoginResponse;
import com.chiclete.reminder.app.dto.ReminderResponse;
import com.chiclete.reminder.app.dto.UserResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;
import java.util.StringJoiner;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "chiclete_auth";
    private static final String KEY_TOKEN = "token";

    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private TextInputEditText inputName;
    private MaterialButton btnLogin;
    private MaterialButton btnRegister;
    private MaterialButton btnList;
    private android.widget.TextView textStatus;

    private String token;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputName = findViewById(R.id.inputName);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnList = findViewById(R.id.btnListReminders);
        textStatus = findViewById(R.id.textStatus);
        api = RetrofitClient.get();
        loadToken();
        setStatus("API: " + BuildConfig.API_BASE);
        btnLogin.setOnClickListener(v -> doLogin());
        btnRegister.setOnClickListener(v -> doRegister());
        btnList.setOnClickListener(v -> loadReminders());
    }

    private void loadToken() {
        token = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_TOKEN, null);
        btnList.setEnabled(token != null);
        btnList.setText(token == null
            ? "Lembretes (precisa logar)"
            : "Carregar lembretes");
    }

    private void saveToken(String t) {
        this.token = t;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_TOKEN, t).apply();
        loadToken();
    }

    private void setStatus(String s) {
        textStatus.setText(s);
    }

    private void doRegister() {
        String name = getTextOrEmpty(inputName);
        String email = getTextOrEmpty(inputEmail);
        String password = getTextOrEmpty(inputPassword);
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || password.length() < 8) {
            toast("Preencha nome, e-mail e senha (8+).");
            return;
        }
        setStatus("Cadastrando…");
        api.register(new CreateUserRequest(name, email, password))
            .enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        setStatus("Cadastro ok: " + r.body().email + ". Agora toque em Entrar.");
                    } else {
                        setStatus("Erro " + r.code() + (r.errorBody() != null ? " (ver detalhe log)" : ""));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                    setStatus("Falha: " + t.getMessage());
                }
            });
    }

    private void doLogin() {
        String email = getTextOrEmpty(inputEmail);
        String password = getTextOrEmpty(inputPassword);
        if (email.isEmpty() || password.isEmpty()) {
            toast("E-mail e senha.");
            return;
        }
        setStatus("Entrando…");
        api.login(new LoginRequest(email, password))
            .enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        saveToken("Bearer " + r.body().token);
                        setStatus("Logado. Token salvo. Toque em Carregar lembretes.");
                    } else {
                        setStatus("Login falhou: " + r.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                    setStatus("Falha: " + t.getMessage());
                }
            });
    }

    private void loadReminders() {
        if (token == null) {
            toast("Faça login antes.");
            return;
        }
        setStatus("Buscando lembretes…");
        api.reminders(token)
            .enqueue(new Callback<List<ReminderResponse>>() {
                @Override
                public void onResponse(
                    @NonNull Call<List<ReminderResponse>> call,
                    @NonNull Response<List<ReminderResponse>> r
                ) {
                    if (r.isSuccessful() && r.body() != null) {
                        StringJoiner j = new StringJoiner("\n");
                        for (ReminderResponse m : r.body()) {
                            j.add("• " + m.title + (m.completed ? " [ok]" : "") + " " + m.scheduledAt);
                        }
                        setStatus(j.length() == 0 ? "Lista vazia." : j.toString());
                    } else {
                        setStatus("HTTP " + r.code());
                    }
                }

                @Override
                public void onFailure(
                    @NonNull Call<List<ReminderResponse>> call,
                    @NonNull Throwable t
                ) {
                    setStatus("Falha: " + t.getMessage());
                }
            });
    }

    private static String getTextOrEmpty(TextInputEditText e) {
        if (e.getText() == null) {
            return "";
        }
        return e.getText().toString().trim();
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}
