package com.petadoption.model;

public class AdoptionRequest {

    private int id;
    private int userId;
    private int petId;

    private String message;
    private String status;
    private String createdAt;

    private String userName;
    private String userEmail;
    private String petName;
    private String userPhone;

    public AdoptionRequest() {
    }

    // Full constructor
    public AdoptionRequest(
            int id,
            int userId,
            int petId,
            String message,
            String status,
            String createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.petId = petId;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Constructor used when creating a new request
    public AdoptionRequest(
            int userId,
            int petId,
            String message
    ) {
        this.userId = userId;
        this.petId = petId;
        this.message = message;
        this.status = "PENDING";
    }

    // Constructor used by DAO when joining users + pets
    public AdoptionRequest(
            int id,
            int userId,
            int petId,
            String message,
            String status,
            String userName,
            String userEmail,
            String petName
    ) {
        this.id = id;
        this.userId = userId;
        this.petId = petId;
        this.message = message;
        this.status = status;
        this.userName = userName;
        this.userEmail = userEmail;
        this.petName = petName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }
    public String getUserPhone() {
    return userPhone;
}

public void setUserPhone(String userPhone) {
    this.userPhone = userPhone;
}
}