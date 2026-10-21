package com.petadoption.dao;

import com.petadoption.database.DatabaseConnection;
import com.petadoption.model.AdoptionRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdoptionRequestDAO {

    // =========================================================
    // CREATE ADOPTION REQUEST
    // =========================================================

    public boolean createRequest(AdoptionRequest request) {

        String sql =
                "INSERT INTO adoption_requests " +
                "(user_id, pet_id, message, status) " +
                "VALUES (?, ?, ?, 'PENDING')";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, request.getUserId());
            statement.setInt(2, request.getPetId());
            statement.setString(3, request.getMessage());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // =========================================================
    // CHECK FOR DUPLICATE PENDING REQUEST
    // =========================================================

    public boolean hasPendingRequest(int userId, int petId) {

        String sql =
                "SELECT id FROM adoption_requests " +
                "WHERE user_id = ? " +
                "AND pet_id = ? " +
                "AND status = 'PENDING'";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, userId);
            statement.setInt(2, petId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // =========================================================
    // CHECK WHETHER PET IS AVAILABLE
    // =========================================================

    public boolean isPetAvailable(int petId) {

        String sql =
                "SELECT id FROM pets " +
                "WHERE id = ? AND status = 'AVAILABLE'";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, petId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // =========================================================
    // GET REQUESTS OF ONE USER
    // =========================================================

    public List<AdoptionRequest> getRequestsByUser(int userId) {

        List<AdoptionRequest> requests = new ArrayList<>();

        String sql =
                "SELECT ar.id, ar.user_id, ar.pet_id, " +
                "u.name AS user_name, " +
                "u.email AS user_email, " +
                "p.name AS pet_name, " +
                "ar.message, ar.status, ar.created_at " +
                "FROM adoption_requests ar " +
                "JOIN users u ON ar.user_id = u.id " +
                "JOIN pets p ON ar.pet_id = p.id " +
                "WHERE ar.user_id = ? " +
                "ORDER BY ar.id DESC";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requests.add(
                            createRequestFromResultSet(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not load user adoption requests.", e
            );
        }

        return requests;
    }


    // =========================================================
    // GET ALL REQUESTS - ADMIN
    // =========================================================

    public List<AdoptionRequest> getAllRequests() {

        List<AdoptionRequest> requests = new ArrayList<>();

        String sql =
                "SELECT ar.id, ar.user_id, ar.pet_id, " +
                "u.name AS user_name, " +
                "u.email AS user_email, " +
                "p.name AS pet_name, " +
                "ar.message, ar.status, ar.created_at " +
                "FROM adoption_requests ar " +
                "JOIN users u ON ar.user_id = u.id " +
                "JOIN pets p ON ar.pet_id = p.id " +
                "ORDER BY ar.id DESC";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                requests.add(
                        createRequestFromResultSet(resultSet)
                );
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not load adoption requests.", e
            );
        }

        return requests;
    }


    // =========================================================
    // APPROVE / REJECT REQUEST
    // =========================================================

    public boolean updateStatus(int requestId, String newStatus) {

        if (requestId <= 0 || newStatus == null) {
            return false;
        }

        newStatus = newStatus.trim().toUpperCase();

        if (!newStatus.equals("APPROVED")
                && !newStatus.equals("REJECTED")) {
            return false;
        }

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                int petId;

                // Lock the request so it cannot be processed twice.
                String findSql =
                        "SELECT pet_id, status " +
                        "FROM adoption_requests " +
                        "WHERE id = ? FOR UPDATE";

                try (PreparedStatement statement =
                             connection.prepareStatement(findSql)) {

                    statement.setInt(1, requestId);

                    try (ResultSet resultSet =
                                 statement.executeQuery()) {

                        if (!resultSet.next()) {
                            connection.rollback();
                            return false;
                        }

                        String currentStatus =
                                resultSet.getString("status");

                        petId = resultSet.getInt("pet_id");

                        // Only pending requests may be processed.
                        if (!"PENDING".equalsIgnoreCase(currentStatus)) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                // -------------------------------------------------
                // APPROVE: PET MUST STILL BE AVAILABLE
                // -------------------------------------------------

                if (newStatus.equals("APPROVED")) {

                    String updatePetSql =
                            "UPDATE pets " +
                            "SET status = 'ADOPTED' " +
                            "WHERE id = ? AND status = 'AVAILABLE'";

                    try (PreparedStatement statement =
                                 connection.prepareStatement(updatePetSql)) {

                        statement.setInt(1, petId);

                        int petsUpdated = statement.executeUpdate();

                        // If another request already adopted this pet,
                        // this update affects zero rows.
                        if (petsUpdated != 1) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                // -------------------------------------------------
                // UPDATE THE ADOPTION REQUEST
                // -------------------------------------------------

                String updateRequestSql =
                        "UPDATE adoption_requests " +
                        "SET status = ? " +
                        "WHERE id = ? AND status = 'PENDING'";

                try (PreparedStatement statement =
                             connection.prepareStatement(updateRequestSql)) {

                    statement.setString(1, newStatus);
                    statement.setInt(2, requestId);

                    int requestsUpdated = statement.executeUpdate();

                    if (requestsUpdated != 1) {
                        connection.rollback();
                        return false;
                    }
                }

                // Both changes succeed together.
                connection.commit();
                return true;

            } catch (SQLException e) {

                // Undo all changes if any database operation fails.
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // =========================================================
    // CONVERT RESULT SET INTO ADOPTION REQUEST OBJECT
    // =========================================================

    private AdoptionRequest createRequestFromResultSet(
            ResultSet resultSet
    ) throws SQLException {

        AdoptionRequest request = new AdoptionRequest();

        request.setId(resultSet.getInt("id"));
        request.setUserId(resultSet.getInt("user_id"));
        request.setPetId(resultSet.getInt("pet_id"));
        request.setUserName(resultSet.getString("user_name"));
        request.setUserEmail(resultSet.getString("user_email"));
        request.setPetName(resultSet.getString("pet_name"));
        request.setMessage(resultSet.getString("message"));
        request.setStatus(resultSet.getString("status"));
        request.setCreatedAt(resultSet.getString("created_at"));

        return request;
    }
}