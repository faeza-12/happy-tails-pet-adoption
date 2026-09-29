package com.petadoption.dao;

import com.petadoption.database.DatabaseConnection;
import com.petadoption.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public boolean userExists(int userId) {
        String sql = "SELECT id FROM users WHERE id = ?";

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean emailExists(String email) {

        String sql = "SELECT id FROM users WHERE email = ?";

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, email);

            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createUser(User user) {

        String sql = "INSERT INTO users (name, email, phone, password, role) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setString(4, user.getPassword());
            statement.setString(5, user.getRole());

            int rowsInserted = statement.executeUpdate();

            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User findByEmail(String email) {

        String sql = "SELECT * FROM users WHERE email = ?";

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, email);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                User user = new User();

                user.setId(resultSet.getInt("id"));
                user.setName(resultSet.getString("name"));
                user.setEmail(resultSet.getString("email"));
                user.setPhone(resultSet.getString("phone"));
                user.setPassword(resultSet.getString("password"));
                user.setRole(resultSet.getString("role"));

                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
