package com.suraksha.surakshaapp.Models;

import java.util.HashMap;
import java.util.Map;

public class TrustedContact {
    public String name;
    public String email;
    public String phone;
    public boolean emailVerified;
    public boolean phoneVerified;

    public TrustedContact() {}

    public TrustedContact(String name, String email, String phone,
                          boolean emailVerified, boolean phoneVerified) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("phone", phone);
        map.put("emailVerified", emailVerified);
        map.put("phoneVerified", phoneVerified);
        return map;
    }
}
