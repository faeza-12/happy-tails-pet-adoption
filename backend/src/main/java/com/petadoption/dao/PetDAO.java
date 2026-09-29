package com.petadoption.dao;

import com.petadoption.database.DatabaseConnection;
import com.petadoption.model.Pet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PetDAO {

    public List<Pet> getAllPets() {

        List<Pet> pets = new ArrayList<>();

        String sql = "SELECT p.*, o.organization_name AS partner_name, o.organization_type AS partner_type FROM pets p LEFT JOIN partners o ON o.id=p.partner_id ORDER BY p.id DESC";

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql);
            ResultSet resultSet =
                    statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Pet pet = new Pet(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("type"),
                    resultSet.getString("breed"),
                    resultSet.getInt("age"),
                    resultSet.getString("gender"),
                    resultSet.getString("description"),
                    resultSet.getString("image_url"),
                    resultSet.getString("status")
                );
                int partnerId = resultSet.getInt("partner_id");
                if (!resultSet.wasNull()) pet.setPartnerId(partnerId);
                pet.setPartnerName(resultSet.getString("partner_name"));
                pet.setPartnerType(resultSet.getString("partner_type"));
                pets.add(pet);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Could not load pets.", e);
        }

        return pets;
    }

    public boolean addPet(Pet pet) {

        String sql =
            "INSERT INTO pets " +
            "(name, type, breed, age, gender, description, image_url, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setString(1, pet.getName());
            statement.setString(2, pet.getType());
            statement.setString(3, pet.getBreed());
            statement.setInt(4, pet.getAge());
            statement.setString(5, pet.getGender());
            statement.setString(6, pet.getDescription());
            statement.setString(7, pet.getImageUrl());
            statement.setString(8, pet.getStatus());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePet(int id) {

        String sql = "DELETE FROM pets WHERE id = ?";

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasAdoptionRequests(int petId) {
        String sql = "SELECT id FROM adoption_requests WHERE pet_id = ? LIMIT 1";

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, petId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }
}
