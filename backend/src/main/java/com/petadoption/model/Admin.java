package com.petadoption.model;

public class Admin extends User {

    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(
            int id,
            String name,
            String email,
            String phone,
            String password
    ) {
        super(
                id,
                name,
                email,
                phone,
                password,
                "ADMIN"
        );
    }

    public String getAdminMessage() {
        return "Welcome to the Admin Dashboard";
    }
}