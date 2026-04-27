package com.suraksha.surakshaapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.suraksha.surakshaapp.R;

import java.util.HashMap;
import java.util.Map;

public class BackupPinActivity extends AppCompatActivity {

    private EditText etPin, etConfirmPin;
    private Button btnSavePin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_pin);

        etPin = findViewById(R.id.et_backup_pin);
        etConfirmPin = findViewById(R.id.et_confirm_backup_pin);
        btnSavePin = findViewById(R.id.btn_save_pin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnSavePin.setOnClickListener(v -> saveBackupPin());
    }

    private void saveBackupPin() {
        String pin = etPin.getText().toString().trim();
        String confirmPin = etConfirmPin.getText().toString().trim();

        if (TextUtils.isEmpty(pin) || pin.length() != 4) {
            Toast.makeText(this, "Enter valid 4-digit PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pin.equals(confirmPin)) {
            Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> pinData = new HashMap<>();
        pinData.put("backup_pin", pin);

        db.collection("users")
                .document(userId)
                .set(pinData, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Backup PIN saved!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(
                            BackupPinActivity.this,
                            SuccessActivity.class
                    ));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}