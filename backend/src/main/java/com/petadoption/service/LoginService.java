package com.petadoption.service;

import com.petadoption.dao.UserDAO;
import com.petadoption.model.Admin;
import com.petadoption.model.User;
import com.petadoption.security.PasswordUtil;

public class LoginService {

    private final UserDAO userDAO;

    public LoginService() {
        this.userDAO = new UserDAO();
    }

    public User login(
            String email,
            String password
    ) {

        email = email.trim().toLowerCase();

        User user =
                userDAO.findByEmail(email);

        if (user == null) {
            return null;
        }

        boolean passwordCorrect =
                PasswordUtil.verifyPassword(
                        password,
                        user.getPassword()
                );

        if (!passwordCorrect) {
            return null;
        }

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {

            return new Admin(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getPassword()
            );
        }

        return user;
    }
}