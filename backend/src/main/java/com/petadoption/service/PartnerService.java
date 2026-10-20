
package com.petadoption.service;

import com.petadoption.dao.PartnerDAO;
import com.petadoption.database.DatabaseConnection;
import com.petadoption.model.Partner;
import com.petadoption.model.Pet;
import com.petadoption.model.AdoptionRequest;
import com.petadoption.security.PasswordUtil;

import java.sql.*;
import java.util.List;

public class PartnerService {

    private final PartnerDAO dao = new PartnerDAO();

    // Register a new organization.
    public String register(Partner p, String password, String confirm)
            throws SQLException {

        p.setOrganizationName(clean(p.getOrganizationName()));
        p.setContactPerson(clean(p.getContactPerson()));
        p.setEmail(clean(p.getEmail()).toLowerCase());
        p.setPhone(clean(p.getPhone()));
        p.setAddress(clean(p.getAddress()));
        p.setDescription(clean(p.getDescription()));

        if (p.getOrganizationName().isBlank()
                || p.getContactPerson().isBlank()
                || p.getEmail().isBlank()
                || p.getAddress().isBlank()) {
            return "Organization, contact person, email and address are required.";
        }

        if (!p.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return "Please enter a valid email address.";
        }

        if (!p.getPhone().matches("\\d{10}")) {
            return "Phone number must contain exactly 10 digits.";
        }

        if (p.getOrganizationType() == null
                || !List.of(
                        "SHELTER",
                        "RESCUE_CENTER",
                        "PET_SHOP",
                        "ANIMAL_WELFARE_ORGANIZATION"
                ).contains(p.getOrganizationType())) {
            return "Select a valid organization type.";
        }

        if (password == null || password.length() < 6) {
            return "Password must contain at least 6 characters.";
        }

        if (!password.equals(confirm)) {
            return "Passwords do not match.";
        }

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "SELECT id FROM users WHERE email = ?")) {

            s.setString(1, p.getEmail());

            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    return "An account with this email already exists.";
                }
            }
        }

        dao.register(p, PasswordUtil.hashPassword(password));

        return "SUCCESS";
    }

    // Search approved organizations by name or organization type.
    public List<Partner> searchApproved(String keyword)
            throws SQLException {
        return dao.searchApproved(keyword);
    }

    // Get available pets belonging to an approved organization.
    public List<Pet> availablePetsForPartner(int partnerId)
            throws SQLException {

        if (partnerId <= 0) {
            throw new IllegalArgumentException("Invalid organization ID.");
        }

        return dao.availablePetsForPartner(partnerId);
    }

    // Get an organization's profile using its user ID.
    public Partner profile(int id) throws SQLException {
        requireRole(id, "PARTNER");
        return dao.byUserId(id);
    }

    // Ensure the organization has been approved before managing pets.
    public Partner requireApproved(int id) throws SQLException {
        Partner p = profile(id);

        if (p == null) {
            throw new SecurityException("Partner profile was not found.");
        }

        if (!"APPROVED".equals(p.getVerificationStatus())) {
            throw new SecurityException(
                    "Your organization must be approved before managing pets."
            );
        }

        return p;
    }

    // Verify the user's role.
    public void requireRole(int id, String role) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "SELECT role FROM users WHERE id = ?")) {

            s.setInt(1, id);

            try (ResultSet r = s.executeQuery()) {
                if (!r.next() || !role.equals(r.getString(1))) {
                    throw new SecurityException("Access denied.");
                }
            }
        }
    }

    // Verify administrator access.
    public void requireAdmin(int id) throws SQLException {
        requireRole(id, "ADMIN");
    }

    // Get all organizations for the admin dashboard.
    public List<Partner> all() throws SQLException {
        return dao.all();
    }

    // Approve or reject a pending organization.
    public boolean updateStatus(int id, String status)
            throws SQLException {
        return dao.updateStatus(id, status);
    }

    // Get pets managed by a partner.
    public List<Pet> pets(int id) throws SQLException {
        return dao.pets(requireApproved(id).getId());
    }

    // Add a pet for a partner.
    public boolean addPet(int id, Pet p) throws SQLException {
        return dao.addPet(requireApproved(id).getId(), p);
    }

    // Update a pet belonging to a partner.
    public boolean updatePet(int id, int petId, Pet p)
            throws SQLException {
        return dao.updatePet(requireApproved(id).getId(), petId, p);
    }

    // Delete a pet belonging to a partner.
    public boolean deletePet(int id, int petId)
            throws SQLException {
        return dao.deletePet(requireApproved(id).getId(), petId);
    }

    // Get adoption requests for a partner's pets.
    public List<AdoptionRequest> requests(int id)
            throws SQLException {
        return dao.requests(requireApproved(id).getId());
    }

    // Handle null strings safely during registration.
    private String clean(String s) {
        return s == null ? "" : s.trim();
    }
}
