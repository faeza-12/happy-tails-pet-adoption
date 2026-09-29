package com.petadoption.service;

import com.petadoption.dao.AdoptionRequestDAO;
import com.petadoption.dao.UserDAO;
import com.petadoption.model.AdoptionRequest;

import java.util.List;

public class AdoptionRequestService {

    private final AdoptionRequestDAO adoptionRequestDAO;
    private final UserDAO userDAO;

    public AdoptionRequestService() {
        adoptionRequestDAO = new AdoptionRequestDAO();
        userDAO = new UserDAO();
    }

    // Create a new adoption request
    public String createRequest(
            int userId,
            int petId,
            String message
    ) {

        if (userId <= 0) {
            return "Invalid user ID.";
        }

        if (!userDAO.userExists(userId)) {
            return "User account was not found. Please log in again.";
        }

        if (petId <= 0) {
            return "Invalid pet.";
        }

        if (message == null) {
            message = "";
        }

        message = message.trim();

        if (message.length() > 1000) {
            return "Message is too long.";
        }

        if (!adoptionRequestDAO.isPetAvailable(petId)) {
            return "This pet is no longer available.";
        }

        if (adoptionRequestDAO.hasPendingRequest(userId, petId)) {
            return "You already have a pending request for this pet.";
        }

        AdoptionRequest request =
                new AdoptionRequest(
                        userId,
                        petId,
                        message
                );

        boolean created =
                adoptionRequestDAO.createRequest(request);

        if (created) {
            return "SUCCESS";
        }

        return "Could not submit adoption request.";
    }

    // Get requests submitted by a user
    public List<AdoptionRequest> getUserRequests(
            int userId
    ) {

        if (userId <= 0 || !userDAO.userExists(userId)) {
            return null;
        }

        return adoptionRequestDAO
                .getRequestsByUser(userId);
    }

    // Get all requests for admin
    public List<AdoptionRequest> getAllRequests() {

        return adoptionRequestDAO
                .getAllRequests();
    }

    // Approve or reject request
    public String updateStatus(
            int requestId,
            String status
    ) {

        if (requestId <= 0) {
            return "Invalid request ID.";
        }

        if (status == null) {
            return "Invalid status.";
        }

        status = status.trim().toUpperCase();

        if (!status.equals("APPROVED")
                && !status.equals("REJECTED")) {

            return "Invalid status.";
        }

        boolean updated =
                adoptionRequestDAO.updateStatus(
                        requestId,
                        status
                );

        if (updated) {

            if (status.equals("APPROVED")) {
                return "Request approved successfully.";
            }

            return "Request rejected successfully.";
        }

        return "Could not update request.";
    }
}
