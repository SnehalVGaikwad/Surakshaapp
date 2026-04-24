package com.suraksha.surakshaapp.Models;

import java.util.HashMap;
import java.util.Map;

public class SOSLog {
    public String timestamp;
    public String userPhone;
    public String location;
    public boolean emailSent;
    public boolean smsSent;
    public boolean whatsappOpened;
    public String status;

    public SOSLog() {}

    public SOSLog(String timestamp, String userPhone, String location) {
        this.timestamp = timestamp;
        this.userPhone = userPhone;
        this.location = location;
        this.emailSent = false;
        this.smsSent = false;
        this.whatsappOpened = false;
        this.status = "initiated";
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", timestamp);
        map.put("userPhone", userPhone);
        map.put("location", location);
        map.put("emailSent", emailSent);
        map.put("smsSent", smsSent);
        map.put("whatsappOpened", whatsappOpened);
        map.put("status", status);
        return map;
    }
}
