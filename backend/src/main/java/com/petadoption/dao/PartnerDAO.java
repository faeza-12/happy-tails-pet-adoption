
package com.petadoption.dao;

import com.petadoption.database.DatabaseConnection;
import com.petadoption.model.Partner;
import com.petadoption.model.Pet;
import com.petadoption.model.AdoptionRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartnerDAO {

    public boolean register(Partner p, String passwordHash) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int userId;

                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO users(name,email,phone,password,role) VALUES(?,?,?,?, 'PARTNER')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    s.setString(1, p.getContactPerson());
                    s.setString(2, p.getEmail());
                    s.setString(3, p.getPhone());
                    s.setString(4, passwordHash);
                    s.executeUpdate();

                    try (ResultSet keys = s.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Could not create partner user.");
                        }
                        userId = keys.getInt(1);
                    }
                }

                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO partners(user_id,organization_name,organization_type,contact_person,email,phone,address,description,verification_status) VALUES(?,?,?,?,?,?,?,?, 'PENDING')")) {
                    s.setInt(1, userId);
                    s.setString(2, p.getOrganizationName());
                    s.setString(3, p.getOrganizationType());
                    s.setString(4, p.getContactPerson());
                    s.setString(5, p.getEmail());
                    s.setString(6, p.getPhone());
                    s.setString(7, p.getAddress());
                    s.setString(8, p.getDescription());
                    s.executeUpdate();
                }

                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public Partner byUserId(int userId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "SELECT * FROM partners WHERE user_id=?")) {
            s.setInt(1, userId);
            try (ResultSet r = s.executeQuery()) {
                return r.next() ? readPartner(r) : null;
            }
        }
    }

    public List<Partner> all() throws SQLException {
        List<Partner> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "SELECT * FROM partners ORDER BY created_at DESC");
             ResultSet r = s.executeQuery()) {
            while (r.next()) {
                list.add(readPartner(r));
            }
        }
        return list;
    }

    // NEW: Only approved organizations appear in public search.
    public List<Partner> searchApproved(String keyword) throws SQLException {
        List<Partner> list = new ArrayList<>();
        String sql = """
                SELECT * FROM partners
                WHERE verification_status = 'APPROVED'
                  AND (organization_name LIKE ?
                       OR organization_type LIKE ?)
                ORDER BY organization_name
                """;

        String search = "%" + (keyword == null ? "" : keyword.trim()) + "%";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, search);
            s.setString(2, search);

            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    list.add(readPartner(r));
                }
            }
        }
        return list;
    }

    // NEW: Return available pets only when the organization is approved.
    public List<Pet> availablePetsForPartner(int partnerId) throws SQLException {
        List<Pet> list = new ArrayList<>();

        String sql = """
                SELECT p.*
                FROM pets p
                JOIN partners o ON o.id = p.partner_id
                WHERE p.partner_id = ?
                  AND p.status = 'AVAILABLE'
                  AND o.verification_status = 'APPROVED'
                ORDER BY p.id DESC
                """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, partnerId);

            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    list.add(readPet(r));
                }
            }
        }
        return list;
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "UPDATE partners SET verification_status=? WHERE id=? AND verification_status='PENDING'")) {
            s.setString(1, status);
            s.setInt(2, id);
            return s.executeUpdate() == 1;
        }
    }

    public List<Pet> pets(int partnerId) throws SQLException {
        List<Pet> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "SELECT * FROM pets WHERE partner_id=? ORDER BY id DESC")) {
            s.setInt(1, partnerId);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    list.add(readPet(r));
                }
            }
        }
        return list;
    }

    public boolean addPet(int partnerId, Pet p) throws SQLException {
        String sql = "INSERT INTO pets(name,type,breed,age,gender,description,image_url,status,partner_id) VALUES(?,?,?,?,?,?,?,'AVAILABLE',?)";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            bindPet(s, p);
            s.setInt(8, partnerId);
            return s.executeUpdate() == 1;
        }
    }

    public boolean updatePet(int partnerId, int petId, Pet p) throws SQLException {
        String sql = "UPDATE pets SET name=?,type=?,breed=?,age=?,gender=?,description=?,image_url=? WHERE id=? AND partner_id=?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            bindPet(s, p);
            s.setInt(8, petId);
            s.setInt(9, partnerId);
            return s.executeUpdate() == 1;
        }
    }

    public boolean deletePet(int partnerId, int petId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            try (PreparedStatement check = c.prepareStatement(
                    "SELECT id FROM adoption_requests WHERE pet_id=? LIMIT 1")) {
                check.setInt(1, petId);
                try (ResultSet r = check.executeQuery()) {
                    if (r.next()) return false;
                }
            }

            try (PreparedStatement s = c.prepareStatement(
                    "DELETE FROM pets WHERE id=? AND partner_id=?")) {
                s.setInt(1, petId);
                s.setInt(2, partnerId);
                return s.executeUpdate() == 1;
            }
        }
    }

    
public List<AdoptionRequest> requests(int partnerId) throws SQLException {
    List<AdoptionRequest> list = new ArrayList<>();

    String sql = """
            SELECT ar.id, ar.user_id, ar.pet_id, ar.message, ar.status,
                   ar.created_at, u.name AS user_name,
                   u.email AS user_email, u.phone AS user_phone,
                   p.name AS pet_name
            FROM adoption_requests ar
            JOIN users u ON u.id = ar.user_id
            JOIN pets p ON p.id = ar.pet_id
            WHERE p.partner_id = ?
            ORDER BY ar.created_at DESC
            """;

    try (Connection c = DatabaseConnection.getConnection();
         PreparedStatement s = c.prepareStatement(sql)) {

        s.setInt(1, partnerId);

        try (ResultSet r = s.executeQuery()) {
            while (r.next()) {
                AdoptionRequest a = new AdoptionRequest(
                        r.getInt("id"),
                        r.getInt("user_id"),
                        r.getInt("pet_id"),
                        r.getString("message"),
                        r.getString("status"),
                        r.getString("user_name"),
                        r.getString("user_email"),
                        r.getString("pet_name")
                );

                a.setUserPhone(r.getString("user_phone"));

                Timestamp createdAt = r.getTimestamp("created_at");
                a.setCreatedAt(
                        createdAt == null ? "" : createdAt.toString()
                );

                list.add(a);
            }
        }
    }

    return list;
}


    private Partner readPartner(ResultSet r) throws SQLException {
        Partner p = new Partner();
        p.setId(r.getInt("id"));
        p.setUserId(r.getInt("user_id"));
        p.setOrganizationName(r.getString("organization_name"));
        p.setOrganizationType(r.getString("organization_type"));
        p.setContactPerson(r.getString("contact_person"));
        p.setEmail(r.getString("email"));
        p.setPhone(r.getString("phone"));
        p.setAddress(r.getString("address"));
        p.setDescription(r.getString("description"));
        p.setVerificationStatus(r.getString("verification_status"));
        return p;
    }

    private Pet readPet(ResultSet r) throws SQLException {
        Pet p = new Pet(
                r.getInt("id"),
                r.getString("name"),
                r.getString("type"),
                r.getString("breed"),
                r.getInt("age"),
                r.getString("gender"),
                r.getString("description"),
                r.getString("image_url"),
                r.getString("status"));

        int partnerId = r.getInt("partner_id");
        if (!r.wasNull()) p.setPartnerId(partnerId);
        return p;
    }

    private void bindPet(PreparedStatement s, Pet p) throws SQLException {
        s.setString(1, p.getName());
        s.setString(2, p.getType());
        s.setString(3, p.getBreed());
        s.setInt(4, p.getAge());
        s.setString(5, p.getGender());
        s.setString(6, p.getDescription());
        s.setString(7, p.getImageUrl());
    }
}
