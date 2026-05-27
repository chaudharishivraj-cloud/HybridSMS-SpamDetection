package com.example.smishingsmskn;

public class SmsResult {
    private String message;
    private String result;
    private String reason;

    // Constructor for backward compatibility (without reason)
    public SmsResult(String message, String result) {
        this.message = message;
        this.result = result;
        this.reason = "";
    }

    // New constructor with reason
    public SmsResult(String message, String result, String reason) {
        this.message = message;
        this.result = result;
        this.reason = reason != null ? reason : "";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    // Helper methods for status checking
    public boolean isSpam() {
        return "Threat Detected".equals(result);
    }

    public boolean isSecure() {
        return "Secure".equals(result);
    }

    public boolean isError() {
        return "Analysis Failed".equals(result) || "Connection Error".equals(result);
    }
}