package com.suraksha.surakshaapp.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String PREF_NAME = "suraksha_prefs";
    private static final String KEY_REGISTRATION_COMPLETE = "registration_complete";
    private static final String KEY_PERMISSIONS_GRANTED = "permissions_granted";
    private static final String KEY_ALL_CONTACTS_ADDED = "all_contacts_added";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_EMAIL_VERIFICATION_CODE = "email_verification_code";
    private static final String KEY_PHONE_OTP = "phone_otp";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setRegistrationComplete(boolean complete) {
        editor.putBoolean(KEY_REGISTRATION_COMPLETE, complete).apply();
    }

    public boolean isRegistrationComplete() {
        return sharedPreferences.getBoolean(KEY_REGISTRATION_COMPLETE, false);
    }

    public void setPermissionsGranted(boolean granted) {
        editor.putBoolean(KEY_PERMISSIONS_GRANTED, granted).apply();
    }

    public boolean isPermissionsGranted() {
        return sharedPreferences.getBoolean(KEY_PERMISSIONS_GRANTED, false);
    }

    public void setAllContactsAdded(boolean added) {
        editor.putBoolean(KEY_ALL_CONTACTS_ADDED, added).apply();
    }

    public boolean isAllContactsAdded() {
        return sharedPreferences.getBoolean(KEY_ALL_CONTACTS_ADDED, false);
    }

    public void setUserName(String name) {
        editor.putString(KEY_USER_NAME, name).apply();
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "User");
    }

    public void setUserPhone(String phone) {
        editor.putString(KEY_USER_PHONE, phone).apply();
    }

    public String getUserPhone() {
        return sharedPreferences.getString(KEY_USER_PHONE, "");
    }

    public void setEmailVerificationCode(String code) {
        editor.putString(KEY_EMAIL_VERIFICATION_CODE, code).apply();
    }

    public String getEmailVerificationCode() {
        return sharedPreferences.getString(KEY_EMAIL_VERIFICATION_CODE, "");
    }

    public void setPhoneOTP(String otp) {
        editor.putString(KEY_PHONE_OTP, otp).apply();
    }

    public String getPhoneOTP() {
        return sharedPreferences.getString(KEY_PHONE_OTP, "");
    }

    public void setContactEmailVerificationCode(int contactNumber, String code) {
        editor.putString("contact_" + contactNumber + "_email_code", code).apply();
    }

    public String getContactEmailVerificationCode(int contactNumber) {
        return sharedPreferences.getString("contact_" + contactNumber + "_email_code", "");
    }

    public void setContactPhoneOTP(int contactNumber, String otp) {
        editor.putString("contact_" + contactNumber + "_phone_otp", otp).apply();
    }

    public String getContactPhoneOTP(int contactNumber) {
        return sharedPreferences.getString("contact_" + contactNumber + "_phone_otp", "");
    }

    public void clearAllData() {
        editor.clear().apply();
    }
}
