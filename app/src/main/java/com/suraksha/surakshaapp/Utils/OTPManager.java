package com.suraksha.surakshaapp.Utils;

import java.util.Random;

public class OTPManager {

    public String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public boolean validateOTP(String inputOTP, String generatedOTP) {
        return inputOTP.equals(generatedOTP);
    }
}
