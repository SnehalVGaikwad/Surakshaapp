package com.suraksha.surakshaapp.Models;

import java.util.HashMap;
import java.util.Map;

public class UserProfile {
    public String name;
    public String email;
    public String phone;
    public String uid;
    public long createdAt;

    public UserProfile() {}

    public UserProfile(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.createdAt = System.currentTimeMillis();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("phone", phone);
        map.put("createdAt", createdAt);
        return map;
    }
}
