package com.petadoption.model;

public class Pet {

    private int id;
    private String name;
    private String type;
    private String breed;
    private int age;
    private String gender;
    private String description;
    private String imageUrl;
    private String status;
    private Integer partnerId;
    private String partnerName;
    private String partnerType;

    public Pet() {
    }

    public Pet(
            int id,
            String name,
            String type,
            String breed,
            int age,
            String gender,
            String description,
            String imageUrl,
            String status
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.breed = breed;
        this.age = age;
        this.gender = gender;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPartnerId() { return partnerId; }
    public void setPartnerId(Integer partnerId) { this.partnerId = partnerId; }
    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    public String getPartnerType() { return partnerType; }
    public void setPartnerType(String partnerType) { this.partnerType = partnerType; }
}
