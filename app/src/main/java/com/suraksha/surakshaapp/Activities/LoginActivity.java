package com.suraksha.surakshaapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.suraksha.surakshaapp.R;
import com.suraksha.surakshaapp.Utils.FirebaseAuthManager;
import com.suraksha.surakshaapp.Utils.PermissionManager;
import com.suraksha.surakshaapp.Utils.SharedPrefManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private FirebaseAuthManager authManager;
    private PermissionManager permissionManager;
    private SharedPrefManager prefManager;
    private FirebaseFirestore db;

    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        mAuth = FirebaseAuth.getInstance();
        authManager = new FirebaseAuthManager(this);
        permissionManager = new PermissionManager(this);
        prefManager = new SharedPrefManager(this);

        btnLogin.setOnClickListener(v -> handleLogin());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            finish();
        });
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Welcome back!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            restoreUserStateAndProceed(user);

                        } else {

                            btnLogin.setEnabled(true);
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Login failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                        btnLogin.setEnabled(true);
                    }
                });
    }
    private void restoreUserStateAndProceed(FirebaseUser user) {

        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    prefManager.setRegistrationComplete(true);

                    if (document.contains("backup_pin")) {
                        prefManager.setBackupPinSet(true);
                    }

                    db.collection("users")
                            .document(uid)
                            .collection("trusted_contacts")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {

                                if (!querySnapshot.isEmpty()) {
                                    prefManager.setAllContactsAdded(true);
                                }

                                proceedToNextScreen();
                            });
                });
    }
    private void proceedToNextScreen() {

        if (permissionManager.areAllPermissionsGranted()) {

            prefManager.setPermissionsGranted(true);

            startActivity(
                    new Intent(LoginActivity.this,
                            HomeActivity.class)
            );

        } else {

            startActivity(
                    new Intent(LoginActivity.this,
                            PermissionBlockedActivity.class)
            );
        }

        finish();
    }
}
