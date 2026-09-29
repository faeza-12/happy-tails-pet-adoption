package com.petadoption.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private PasswordUtil() {
        // Prevent object creation
    }

    public static String hashPassword(String password) {

        try {
            byte[] salt = new byte[SALT_LENGTH];

            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    ITERATIONS,
                    KEY_LENGTH
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = factory.generateSecret(spec).getEncoded();

            String saltString =
                    Base64.getEncoder().encodeToString(salt);

            String hashString =
                    Base64.getEncoder().encodeToString(hash);

            return ITERATIONS + ":" + saltString + ":" + hashString;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {

            throw new RuntimeException(
                    "Error while hashing password",
                    e
            );
        }
    }

    public static boolean verifyPassword(
            String password,
            String storedPassword
    ) {

        try {

            String[] parts = storedPassword.split(":");

            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);

            byte[] salt =
                    Base64.getDecoder().decode(parts[1]);

            byte[] storedHash =
                    Base64.getDecoder().decode(parts[2]);

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    iterations,
                    storedHash.length * 8
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            byte[] calculatedHash =
                    factory.generateSecret(spec).getEncoded();

            if (calculatedHash.length != storedHash.length) {
                return false;
            }

            int difference = 0;

            for (int i = 0; i < calculatedHash.length; i++) {
                difference |= calculatedHash[i] ^ storedHash[i];
            }

            return difference == 0;

        } catch (Exception e) {

            return false;
        }
    }
}