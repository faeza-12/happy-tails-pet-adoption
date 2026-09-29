package com.petadoption.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/pet_adoption_system?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";

    private static final String USERNAME = "root";

    private static final String PASSWORD = "faezafarha";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}