package com.petadoption.service;

import com.petadoption.dao.UserDAO;
import com.petadoption.model.User;
import com.petadoption.security.PasswordUtil;

public class RegistrationService {

    private final UserDAO userDAO;

    // Secret code required to create an ADMIN account
    private static final String ADMIN_CODE =
            "HAPPYTAILS-ADMIN-2026";

    public RegistrationService() {
        this.userDAO = new UserDAO();
    }

    // ==========================================
    // NORMAL USER REGISTRATION
    // ==========================================

    public String registerUser(
            String name,
            String email,
            String phone,
            String password,
            String confirmPassword
    ) {

        name = name.trim();
        email = email.trim().toLowerCase();
        phone = phone.trim();

        String validationMessage =
                validateDetails(
                        name,
                        email,
                        phone,
                        password,
                        confirmPassword
                );

        if (validationMessage != null) {
            return validationMessage;
        }

        if (userDAO.emailExists(email)) {
            return "An account with this email already exists.";
        }

        String hashedPassword =
                PasswordUtil.hashPassword(password);

        User user = new User(
                name,
                email,
                phone,
                hashedPassword,
                "USER"
        );

        boolean created =
                userDAO.createUser(user);

        if (created) {
            return "SUCCESS";
        }

        return "Registration failed. Please try again.";
    }


    // ==========================================
    // ADMIN REGISTRATION
    // ==========================================

    public String registerAdmin(
            String name,
            String email,
            String phone,
            String password,
            String confirmPassword,
            String adminCode
    ) {

        name = name.trim();
        email = email.trim().toLowerCase();
        phone = phone.trim();

        String validationMessage =
                validateDetails(
                        name,
                        email,
                        phone,
                        password,
                        confirmPassword
                );

        if (validationMessage != null) {
            return validationMessage;
        }

        // Check admin authorization code
        if (!ADMIN_CODE.equals(adminCode)) {
            return "Invalid admin authorization code.";
        }

        // Check duplicate email
        if (userDAO.emailExists(email)) {
            return "An account with this email already exists.";
        }

        String hashedPassword =
                PasswordUtil.hashPassword(password);

        User admin = new User(
                name,
                email,
                phone,
                hashedPassword,
                "ADMIN"
        );

        boolean created =
                userDAO.createUser(admin);

        if (created) {
            return "SUCCESS";
        }

        return "Admin registration failed. Please try again.";
    }


    // ==========================================
    // COMMON VALIDATION
    // ==========================================

    private String validateDetails(
            String name,
            String email,
            String phone,
            String password,
            String confirmPassword
    ) {

        if (name.isEmpty()) {
            return "Please enter your full name.";
        }

        if (email.isEmpty()) {
            return "Please enter your email.";
        }

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        )) {
            return "Please enter a valid email address.";
        }

        if (phone.isEmpty()) {
            return "Please enter your phone number.";
        }

        if (!phone.matches("\\d{10}")) {
            return "Phone number must contain exactly 10 digits.";
        }

        if (password.length() < 6) {
            return "Password must contain at least 6 characters.";
        }

        if (!password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }

        return null;
    }
}