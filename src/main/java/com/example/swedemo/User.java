package com.example.swedemo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class User {
    public String userID;
    public String email;
    private String role;
    private String encodedPasswordHash;

    User(String userID, String email, String password) {
        this.userID = userID;
        this.email = email;
        this.role = "sales-rep"; // default

        try { // password hashing using SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] passwordHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            encodedPasswordHash = Base64.getEncoder().encodeToString(passwordHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public int login(String email, String password) {
        try {
            if (!email.equals(this.email)) {
                return 1;
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] attemptHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            String encodedAttemptHash = Base64.getEncoder().encodeToString(attemptHash);
            if (encodedPasswordHash.equals(encodedAttemptHash)) {
                return 0;
            } else {
                return 2;
            }
        } catch (NoSuchAlgorithmException e) {
            return 1;
        }
    }

}
