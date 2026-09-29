package com.petadoption.server;

import com.petadoption.database.DatabaseConnection;

import com.petadoption.model.User;
import com.petadoption.model.Pet;
import com.petadoption.model.AdoptionRequest;
import com.petadoption.model.Partner;
import com.petadoption.service.PartnerService;

import com.petadoption.dao.PetDAO;

import com.petadoption.service.LoginService;
import com.petadoption.service.RegistrationService;
import com.petadoption.service.AdoptionRequestService;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetSocketAddress;
import java.net.URLDecoder;

import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.Executors;


public class PetAdoptionServer {

    private static final int PORT = 8080;

    private final RegistrationService registrationService;
    private final LoginService loginService;
    private final PetDAO petDAO;
    private final AdoptionRequestService adoptionRequestService;
    private final PartnerService partnerService;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public PetAdoptionServer() {

        registrationService =
                new RegistrationService();

        loginService =
                new LoginService();

        petDAO =
                new PetDAO();

        adoptionRequestService =
                new AdoptionRequestService();
        partnerService = new PartnerService();
    }


    // =====================================================
    // START SERVER
    // =====================================================

    public void start() throws IOException {

        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(PORT),
                        0
                );


        // =================================================
        // BASIC API ROUTES
        // =================================================

        server.createContext(
                "/api/register",
                this::handleRegister
        );


        server.createContext(
                "/api/admin/register",
                this::handleAdminRegister
        );

        server.createContext("/api/partner/", this::handlePartnerApi);
        server.createContext("/api/admin/partners", this::handleAdminPartners);


        server.createContext(
                "/api/login",
                this::handleLogin
        );


        server.createContext(
                "/api/health",
                this::handleHealth
        );


        // =================================================
        // PET API ROUTES
        // =================================================

        server.createContext(
                "/api/pets",
                this::handlePets
        );


        server.createContext(
                "/api/pets/add",
                this::handleAddPet
        );


        server.createContext(
                "/api/pets/delete",
                this::handleDeletePet
        );


        // =================================================
        // ADOPTION REQUEST API ROUTES
        // =================================================

        server.createContext(
                "/api/adoption/request",
                this::handleCreateAdoptionRequest
        );


        server.createContext(
                "/api/adoption/user",
                this::handleUserAdoptionRequests
        );


        server.createContext(
                "/api/adoption/all",
                this::handleAllAdoptionRequests
        );


        server.createContext(
                "/api/adoption/status",
                this::handleUpdateAdoptionStatus
        );


        // =================================================
        // SERVER EXECUTOR
        // =================================================

        server.setExecutor(
                Executors.newFixedThreadPool(10)
        );


        server.start();


        // =================================================
        // TEST DATABASE CONNECTION
        // =================================================

        try {

            DatabaseConnection.getConnection();

            System.out.println(
                    "MySQL Database Connection: SUCCESS"
            );

        } catch (Exception e) {

            System.out.println(
                    "MySQL Database Connection: FAILED"
            );

            e.printStackTrace();
        }


        System.out.println(
                "========================================"
        );

        System.out.println(
                "Pet Adoption Backend Started"
        );

        System.out.println(
                "Server: http://localhost:" + PORT
        );

        System.out.println(
                "Health: http://localhost:"
                        + PORT
                        + "/api/health"
        );

        System.out.println(
                "========================================"
        );
    }


    // =====================================================
    // HEALTH CHECK
    // =====================================================

    private void handleHealth(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if (exchange.getRequestMethod()
                .equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            sendJson(
                    exchange,
                    405,
                    jsonError(
                            "Method not allowed"
                    )
            );

            return;
        }

        sendJson(
                exchange,
                200,
                "{"
                        + "\"success\":true,"
                        + "\"message\":\"Backend is running\""
                        + "}"
        );
    }


    // =====================================================
    // NORMAL USER REGISTRATION
    // =====================================================

    private void handleRegister(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if (exchange.getRequestMethod()
                .equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendJson(
                    exchange,
                    405,
                    jsonError(
                            "Method not allowed"
                    )
            );

            return;
        }

        try {

            String body =
                    readRequestBody(exchange);

            Map<String, String> data =
                    parseFormData(body);

            String name =
                    data.getOrDefault(
                            "name",
                            ""
                    );

            String email =
                    data.getOrDefault(
                            "email",
                            ""
                    );

            String phone =
                    data.getOrDefault(
                            "phone",
                            ""
                    );

            String password =
                    data.getOrDefault(
                            "password",
                            ""
                    );

            String confirmPassword =
                    data.getOrDefault(
                            "confirmPassword",
                            ""
                    );

            String result =
                    registrationService.registerUser(
                            name,
                            email,
                            phone,
                            password,
                            confirmPassword
                    );

            if ("SUCCESS".equals(result)) {

                sendJson(
                        exchange,
                        200,
                        "{"
                                + "\"success\":true,"
                                + "\"message\":\"Registration successful\""
                                + "}"
                );

            } else {

                sendJson(
                        exchange,
                        400,
                        jsonError(result)
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(
                    exchange,
                    500,
                    jsonError(
                            "Server error during registration."
                    )
            );
        }
    }


    // =====================================================
    // ADMIN REGISTRATION
    // =====================================================

    private void handleAdminRegister(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if (exchange.getRequestMethod()
                .equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendJson(
                    exchange,
                    405,
                    jsonError(
                            "Method not allowed"
                    )
            );

            return;
        }

        try {

            String body =
                    readRequestBody(exchange);

            Map<String, String> data =
                    parseFormData(body);

            String name =
                    data.getOrDefault(
                            "name",
                            ""
                    );

            String email =
                    data.getOrDefault(
                            "email",
                            ""
                    );

            String phone =
                    data.getOrDefault(
                            "phone",
                            ""
                    );

            String password =
                    data.getOrDefault(
                            "password",
                            ""
                    );

            String confirmPassword =
                    data.getOrDefault(
                            "confirmPassword",
                            ""
                    );

            String adminCode =
                    data.getOrDefault(
                            "adminCode",
                            ""
                    );

            String result =
                    registrationService.registerAdmin(
                            name,
                            email,
                            phone,
                            password,
                            confirmPassword,
                            adminCode
                    );

            if ("SUCCESS".equals(result)) {

                sendJson(
                        exchange,
                        200,
                        "{"
                                + "\"success\":true,"
                                + "\"message\":\"Admin registration successful\""
                                + "}"
                );

            } else {

                sendJson(
                        exchange,
                        400,
                        jsonError(result)
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(
                    exchange,
                    500,
                    jsonError(
                            "Server error during admin registration."
                    )
            );
        }
    }


    // =====================================================
    // LOGIN
    // =====================================================

    private void handleLogin(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if (exchange.getRequestMethod()
                .equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendJson(
                    exchange,
                    405,
                    jsonError(
                            "Method not allowed"
                    )
            );

            return;
        }

        try {

            String body =
                    readRequestBody(exchange);

            Map<String, String> data =
                    parseFormData(body);

            String email =
                    data.getOrDefault(
                            "email",
                            ""
                    );

            String password =
                    data.getOrDefault(
                            "password",
                            ""
                    );

            if (email.isBlank()
                    || password.isBlank()) {

                sendJson(
                        exchange,
                        400,
                        jsonError(
                                "Email and password are required."
                        )
                );

                return;
            }

            User user =
                    loginService.login(
                            email,
                            password
                    );

            if (user == null) {

                sendJson(
                        exchange,
                        401,
                        jsonError(
                                "Invalid email or password."
                        )
                );

                return;
            }


            // IMPORTANT:
            // Include user ID so login.js
            // can save it in sessionStorage.

            String response =
                    "{"
                            + "\"success\":true,"
                            + "\"message\":\"Login successful\","

                            + "\"id\":"
                            + user.getId()
                            + ","

                            + "\"name\":\""
                            + escapeJson(
                                    user.getName()
                            )
                            + "\","

                            + "\"email\":\""
                            + escapeJson(
                                    user.getEmail()
                            )
                            + "\","

                            + "\"role\":\""
                            + escapeJson(
                                    user.getRole()
                            )
                            + "\""

                            + "}";


            sendJson(
                    exchange,
                    200,
                    response
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(
                    exchange,
                    500,
                    jsonError(
                            "Server error during login."
                    )
            );
        }
    }


    // =====================================================
    // GET ALL PETS
    // =====================================================

    private void handlePets(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if (exchange.getRequestMethod()
                .equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            sendJson(
                    exchange,
                    405,
                    jsonError(
                            "Method not allowed"
                    )
            );

            return;
        }

        try {

            List<Pet> pets =
                    petDAO.getAllPets();

            StringBuilder json =
                    new StringBuilder();

            json.append("{");
            json.append("\"success\":true,");
            json.append("\"pets\":[");


            for (
                    int i = 0;
                    i < pets.size();
                    i++
            ) {

                Pet pet =
                        pets.get(i);

                json.append("{");

                json.append("\"id\":")
                        .append(
                                pet.getId()
                        )
                        .append(",");

                json.append("\"name\":\"")
                        .append(
                                escapeJson(
                                        pet.getName()
                                )
                        )
                        .append("\",");

                json.append("\"type\":\"")
                        .append(
                                escapeJson(
                                        pet.getType()
                                )
                        )
                        .append("\",");

                json.append("\"breed\":\"")
                        .append(
                                escapeJson(
                                        pet.getBreed()
                                )
                        )
                        .append("\",");

                json.append("\"age\":")
                        .append(
                                pet.getAge()
                        )
                        .append(",");

                json.append("\"gender\":\"")
                        .append(
                                escapeJson(
                                        pet.getGender()
                                )
                        )
                        .append("\",");

                json.append("\"description\":\"")
                        .append(
                                escapeJson(
                                        pet.getDescription()
                                )
                        )
                        .append("\",");

                json.append("\"imageUrl\":\"")
                        .append(
                                escapeJson(
                                        pet.getImageUrl()
                                )
                        )
                        .append("\",");

                json.append("\"status\":\"")
                        .append(
                                escapeJson(
                                        pet.getStatus()
                                )
                        )
                        .append("\",");
                json.append("\"partnerId\":").append(pet.getPartnerId() == null ? "null" : pet.getPartnerId()).append(",");
                json.append("\"partnerName\":\"").append(escapeJson(pet.getPartnerName())).append("\",");
                json.append("\"partnerType\":\"").append(escapeJson(pet.getPartnerType())).append("\"");

                json.append("}");


                if (i < pets.size() - 1) {

                    json.append(",");
                }
            }


            json.append("]");
            json.append("}");


            sendJson(
                    exchange,
                    200,
                    json.toString()
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(
                    exchange,
                    500,
                    jsonError(
                            "Could not load pets."
                    )
            );
        }
    }


    // =====================================================
    // ADD PET
    // =====================================================

    private void handleAddPet(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if (exchange.getRequestMethod()
                .equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendJson(
                    exchange,
                    405,
                    jsonError(
                            "Method not allowed"
                    )
            );

            return;
        }

        try {

            String body =
                    readRequestBody(exchange);

            Map<String, String> data =
                    parseFormData(body);

            String name =
                    data.getOrDefault(
                            "name",
                            ""
                    ).trim();

            String type =
                    data.getOrDefault(
                            "type",
                            ""
                    ).trim();

            String breed =
                    data.getOrDefault(
                            "breed",
                            ""
                    ).trim();

            String ageText =
                    data.getOrDefault(
                            "age",
                            ""
                    ).trim();

            String gender =
                    data.getOrDefault(
                            "gender",
                            ""
                    ).trim();

            String description =
                    data.getOrDefault(
                            "description",
                            ""
                    ).trim();

            String imageUrl =
                    data.getOrDefault(
                            "imageUrl",
                            ""
                    ).trim();

            String status =
                    data.getOrDefault(
                            "status",
                            "AVAILABLE"
                    ).trim();


            if (name.isBlank()
                    || type.isBlank()
                    || ageText.isBlank()) {

                sendJson(
                        exchange,
                        400,
                        jsonError(
                                "Name, type and age are required."
                        )
                );

                return;
            }


            int age;

            try {

                age =
                        Integer.parseInt(
                                ageText
                        );

            } catch (NumberFormatException e) {

                sendJson(
                        exchange,
                        400,
                        jsonError(
                                "Age must be a valid number."
                        )
                );

                return;
            }


            if (age < 0) {

                sendJson(
                        exchange,
                        400,
                        jsonError(
                                "Age cannot be negative."
                        )
                );

                return;
            }

            if (!status.equals("AVAILABLE") && !status.equals("ADOPTED")) {
                sendJson(exchange, 400, jsonError("Invalid pet status."));
                return;
            }


            Pet pet =
                    new Pet(
                            0,
                            name,
                            type,
                            breed,
                            age,
                            gender,
                            description,
                            imageUrl,
                            status
                    );


            boolean added =
                    petDAO.addPet(pet);


            if (added) {

                sendJson(
                        exchange,
                        200,
                        "{"
                                + "\"success\":true,"
                                + "\"message\":\"Pet added successfully\""
                                + "}"
                );

            } else {

                sendJson(
                        exchange,
                        500,
                        jsonError(
                                "Could not add pet."
                        )
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(
                    exchange,
                    500,
                    jsonError(
                            "Server error while adding pet."
                    )
            );
        }
    }


    // =====================================================
    // DELETE PET
    // =====================================================

    private void handleDeletePet(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if (exchange.getRequestMethod()
                .equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("DELETE")) {

            sendJson(
                    exchange,
                    405,
                    jsonError(
                            "Method not allowed"
                    )
            );

            return;
        }

        try {

            String query =
                    exchange.getRequestURI()
                            .getQuery();

            Map<String, String> data =
                    parseFormData(
                            query == null
                                    ? ""
                                    : query
                    );

            String idText =
                    data.getOrDefault(
                            "id",
                            ""
                    );

            if (idText.isBlank()) {

                sendJson(
                        exchange,
                        400,
                        jsonError(
                                "Pet ID is required."
                        )
                );

                return;
            }


            int id;

            try {

                id =
                        Integer.parseInt(
                                idText
                        );

            } catch (NumberFormatException e) {

                sendJson(
                        exchange,
                        400,
                        jsonError(
                                "Invalid pet ID."
                        )
                );

                return;
            }

            if (id <= 0) {
                sendJson(exchange, 400, jsonError("Invalid pet ID."));
                return;
            }

            if (petDAO.hasAdoptionRequests(id)) {
                sendJson(exchange, 409, jsonError(
                        "This pet has adoption request history and cannot be deleted."
                ));
                return;
            }


            boolean deleted =
                    petDAO.deletePet(id);


            if (deleted) {

                sendJson(
                        exchange,
                        200,
                        "{"
                                + "\"success\":true,"
                                + "\"message\":\"Pet deleted successfully\""
                                + "}"
                );

            } else {

                sendJson(
                        exchange,
                        404,
                        jsonError(
                                "Pet not found."
                        )
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(
                    exchange,
                    500,
                    jsonError(
                            "Server error while deleting pet."
                    )
            );
        }
    }


    // =====================================================
    // CREATE ADOPTION REQUEST
    // =====================================================

    private void handleCreateAdoptionRequest(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if (exchange.getRequestMethod()
                .equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }


        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendJson(
                    exchange,
                    405,
                    jsonError(
                            "Method not allowed"
                    )
            );

            return;
        }


        try {

            String body =
                    readRequestBody(exchange);


            Map<String, String> data =
                    parseFormData(body);


            String userIdText =
                    data.getOrDefault(
                            "userId",
                            ""
                    );


            String petIdText =
                    data.getOrDefault(
                            "petId",
                            ""
                    );


            String message =
                    data.getOrDefault(
                            "message",
                            ""
                    );


            if (userIdText.isBlank()
                    || petIdText.isBlank()) {

                sendJson(
                        exchange,
                        400,
                        jsonError(
                                "User ID and Pet ID are required."
                        )
                );

                return;
            }


            int userId;
            int petId;


            try {

                userId =
                        Integer.parseInt(
                                userIdText
                        );

                petId =
                        Integer.parseInt(
                                petIdText
                        );

            } catch (NumberFormatException e) {

                sendJson(
                        exchange,
                        400,
                        jsonError(
                                "Invalid user ID or pet ID."
                        )
                );

                return;
            }

            if (userId <= 0 || petId <= 0) {
                sendJson(exchange, 400, jsonError("User ID and Pet ID must be positive."));
                return;
            }


            String result =
                    adoptionRequestService.createRequest(
                            userId,
                            petId,
                            message
                    );


            if ("SUCCESS".equals(result)) {

                sendJson(
                        exchange,
                        200,
                        "{"
                                + "\"success\":true,"
                                + "\"message\":\"Adoption request submitted successfully.\""
                                + "}"
                );

            } else {

                sendJson(
                        exchange,
                        400,
                        jsonError(result)
                );
            }


        } catch (Exception e) {

            e.printStackTrace();


            sendJson(
                    exchange,
                    500,
                    jsonError(
                            "Server error while creating adoption request."
                    )
            );
        }
    }


    // =====================================================
    // GET USER ADOPTION REQUESTS
    // =====================================================

    private void handleUserAdoptionRequests(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);


        if (exchange.getRequestMethod()
                .equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }


        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            sendJson(
                    exchange,
                    405,
                    jsonError(
                            "Method not allowed"
                    )
            );

            return;
        }


        try {

            String query =
                    exchange.getRequestURI()
                            .getQuery();


            Map<String, String> data =
                    parseFormData(
                            query == null
                                    ? ""
                                    : query
                    );


            String userIdText =
                    data.getOrDefault(
                            "userId",
                            ""
                    );


            if (userIdText.isBlank()) {

                sendJson(
                        exchange,
                        400,
                        jsonError(
                                "User ID is required."
                        )
                );

                return;
            }


            int userId;


            try {

                userId =
                        Integer.parseInt(
                                userIdText
                        );

            } catch (NumberFormatException e) {

                sendJson(
                        exchange,
                        400,
                        jsonError(
                                "Invalid user ID."
                        )
                );

                return;
            }

            if (userId <= 0) {
                sendJson(exchange, 400, jsonError("Invalid user ID."));
                return;
            }


            List<AdoptionRequest> requests =
                    adoptionRequestService
                            .getUserRequests(
                                    userId
                            );

            if (requests == null) {
                sendJson(exchange, 404, jsonError(
                        "User account was not found. Please log in again."
                ));
                return;
            }


            StringBuilder json =
                    new StringBuilder();


            json.append("{");
            json.append("\"success\":true,");
            json.append("\"requests\":[");


            for (
                    int i = 0;
                    i < requests.size();
                    i++
            ) {

                AdoptionRequest request =
                        requests.get(i);


                json.append(
                        adoptionRequestToJson(
                                request
                        )
                );


                if (
                        i < requests.size() - 1
                ) {

                    json.append(",");
                }
            }


            json.append("]");
            json.append("}");


            sendJson(
                    exchange,
                    200,
                    json.toString()
            );


        } catch (Exception e) {

            e.printStackTrace();


            sendJson(
                    exchange,
                    500,
                    jsonError(
                            "Could not load adoption requests."
                    )
            );
        }
    }


    // =====================================================
    // GET ALL ADOPTION REQUESTS
    // ADMIN
    // =====================================================

    private void handleAllAdoptionRequests(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);


        if (exchange.getRequestMethod()
                .equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }


        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            sendJson(
                    exchange,
                    405,
                    jsonError(
                            "Method not allowed"
                    )
            );

            return;
        }


        try {

            Map<String,String> query = parseFormData(exchange.getRequestURI().getRawQuery());
            int adminId = Integer.parseInt(query.getOrDefault("userId", "0"));
            partnerService.requireAdmin(adminId);

            List<AdoptionRequest> requests =
                    adoptionRequestService
                            .getAllRequests();


            StringBuilder json =
                    new StringBuilder();


            json.append("{");
            json.append("\"success\":true,");
            json.append("\"requests\":[");


            for (
                    int i = 0;
                    i < requests.size();
                    i++
            ) {

                AdoptionRequest request =
                        requests.get(i);


                json.append(
                        adoptionRequestToJson(
                                request
                        )
                );


                if (
                        i < requests.size() - 1
                ) {

                    json.append(",");
                }
            }


            json.append("]");
            json.append("}");


            sendJson(
                    exchange,
                    200,
                    json.toString()
            );


        } catch (SecurityException e) {
            sendJson(exchange, 403, jsonError(e.getMessage()));
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, jsonError("A valid admin ID is required."));
        } catch (Exception e) {

            e.printStackTrace();


            sendJson(
                    exchange,
                    500,
                    jsonError(
                            "Could not load adoption requests."
                    )
            );
        }
    }


    // =====================================================
    // UPDATE ADOPTION REQUEST STATUS
    // ADMIN
    // =====================================================

    private void handleUpdateAdoptionStatus(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);


        if (exchange.getRequestMethod()
                .equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }


        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendJson(
                    exchange,
                    405,
                    jsonError(
                            "Method not allowed"
                    )
            );

            return;
        }


        try {

            String body =
                    readRequestBody(exchange);


            Map<String, String> data =
                    parseFormData(body);

            int adminId = Integer.parseInt(data.getOrDefault("userId", "0"));
            partnerService.requireAdmin(adminId);


            String requestIdText =
                    data.getOrDefault(
                            "requestId",
                            ""
                    );


            String status =
                    data.getOrDefault(
                            "status",
                            ""
                    );


            if (requestIdText.isBlank()
                    || status.isBlank()) {

                sendJson(
                        exchange,
                        400,
                        jsonError(
                                "Request ID and status are required."
                        )
                );

                return;
            }


            int requestId;


            try {

                requestId =
                        Integer.parseInt(
                                requestIdText
                        );

            } catch (NumberFormatException e) {

                sendJson(
                        exchange,
                        400,
                        jsonError(
                                "Invalid request ID."
                        )
                );

                return;
            }

            if (requestId <= 0) {
                sendJson(exchange, 400, jsonError("Invalid request ID."));
                return;
            }


            String result =
                    adoptionRequestService
                            .updateStatus(
                                    requestId,
                                    status
                            );


            if (
                    result.equals(
                            "Request approved successfully."
                    )
                    ||
                    result.equals(
                            "Request rejected successfully."
                    )
            ) {

                sendJson(
                        exchange,
                        200,
                        "{"
                                + "\"success\":true,"
                                + "\"message\":\""
                                + escapeJson(result)
                                + "\""
                                + "}"
                );

            } else {

                sendJson(
                        exchange,
                        400,
                        jsonError(result)
                );
            }


        } catch (SecurityException e) {
            sendJson(exchange, 403, jsonError(e.getMessage()));
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, jsonError("A valid admin ID is required."));
        } catch (Exception e) {

            e.printStackTrace();


            sendJson(
                    exchange,
                    500,
                    jsonError(
                            "Server error while updating adoption request."
                    )
            );
        }
    }


    // =====================================================
    // CONVERT ADOPTION REQUEST TO JSON
    // =====================================================

    private String adoptionRequestToJson(
            AdoptionRequest request
    ) {

        return "{"

                + "\"id\":"
                + request.getId()
                + ","

                + "\"userId\":"
                + request.getUserId()
                + ","

                + "\"petId\":"
                + request.getPetId()
                + ","

                + "\"userName\":\""
                + escapeJson(
                        request.getUserName()
                )
                + "\","

                + "\"userEmail\":\""
                + escapeJson(
                        request.getUserEmail()
                )
                + "\","

                + "\"petName\":\""
                + escapeJson(
                        request.getPetName()
                )
                + "\","

                + "\"message\":\""
                + escapeJson(
                        request.getMessage()
                )
                + "\","

                + "\"status\":\""
                + escapeJson(
                        request.getStatus()
                )
                + "\","

                + "\"createdAt\":\""
                + escapeJson(
                        request.getCreatedAt()
                )
                + "\""

                + "}";
    }


    private void handlePartnerApi(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) { exchange.sendResponseHeaders(204, -1); return; }
        String path = exchange.getRequestURI().getPath();
        try {
            if (path.equals("/api/partner/register") && exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                Map<String,String> d=parseFormData(readRequestBody(exchange)); Partner p=new Partner();
                p.setOrganizationName(d.get("organizationName")); p.setOrganizationType(d.get("organizationType")); p.setContactPerson(d.get("contactPerson"));
                p.setEmail(d.get("email")); p.setPhone(d.get("phone")); p.setAddress(d.get("address")); p.setDescription(d.get("description"));
                String result=partnerService.register(p,d.get("password"),d.get("confirmPassword"));
                sendJson(exchange,"SUCCESS".equals(result)?200:400,"SUCCESS".equals(result)?"{\"success\":true,\"message\":\"Registration submitted for admin approval.\"}":jsonError(result)); return;
            }
            Map<String,String> d=exchange.getRequestMethod().equalsIgnoreCase("GET")?parseFormData(exchange.getRequestURI().getRawQuery()):parseFormData(readRequestBody(exchange));
            int userId=Integer.parseInt(d.getOrDefault("userId","0"));
            if(path.equals("/api/partner/profile")){ Partner p=partnerService.profile(userId); if(p==null){sendJson(exchange,404,jsonError("Partner profile not found."));return;} sendJson(exchange,200,"{\"success\":true,\"partner\":"+partnerToJson(p)+"}");return; }
            if(path.equals("/api/partner/pets") && exchange.getRequestMethod().equalsIgnoreCase("GET")){ List<Pet> list=partnerService.pets(userId); sendJson(exchange,200,"{\"success\":true,\"pets\":"+petsToJson(list)+"}");return; }
            if(path.equals("/api/partner/adoption-requests") && exchange.getRequestMethod().equalsIgnoreCase("GET")){ List<AdoptionRequest> list=partnerService.requests(userId); StringBuilder b=new StringBuilder("{\"success\":true,\"requests\":["); for(int i=0;i<list.size();i++){if(i>0)b.append(',');b.append(adoptionRequestToJson(list.get(i)));} b.append("]}");sendJson(exchange,200,b.toString());return; }
            if(path.equals("/api/partner/pets/add") && exchange.getRequestMethod().equalsIgnoreCase("POST")){ boolean ok=partnerService.addPet(userId,petFrom(d));sendJson(exchange,ok?200:400,ok?"{\"success\":true,\"message\":\"Pet added.\"}":jsonError("Pet could not be added."));return; }
            if(path.equals("/api/partner/pets/update") && exchange.getRequestMethod().equalsIgnoreCase("POST")){ int petId=Integer.parseInt(d.getOrDefault("petId","0"));boolean ok=partnerService.updatePet(userId,petId,petFrom(d));sendJson(exchange,ok?200:404,ok?"{\"success\":true,\"message\":\"Pet updated.\"}":jsonError("Pet not found or not owned by this organization."));return; }
            if(path.equals("/api/partner/pets/delete") && exchange.getRequestMethod().equalsIgnoreCase("DELETE")){ int petId=Integer.parseInt(d.getOrDefault("id","0"));boolean ok=partnerService.deletePet(userId,petId);sendJson(exchange,ok?200:409,ok?"{\"success\":true,\"message\":\"Pet deleted.\"}":jsonError("This pet cannot be deleted because adoption requests already exist, or it is not owned by this organization."));return; }
            sendJson(exchange,405,jsonError("Method or partner endpoint not allowed."));
        } catch(SecurityException e){sendJson(exchange,403,jsonError(e.getMessage()));}
        catch(NumberFormatException e){sendJson(exchange,400,jsonError("A valid user and pet ID are required."));}
        catch(Exception e){e.printStackTrace();sendJson(exchange,500,jsonError("Partner operation failed."));}
    }

    private void handleAdminPartners(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if(exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")){exchange.sendResponseHeaders(204,-1);return;}
        try {
            Map<String,String> d=exchange.getRequestMethod().equalsIgnoreCase("GET")?parseFormData(exchange.getRequestURI().getRawQuery()):parseFormData(readRequestBody(exchange));
            int userId=Integer.parseInt(d.getOrDefault("userId","0")); partnerService.requireAdmin(userId);
            if(exchange.getRequestMethod().equalsIgnoreCase("GET")){List<Partner> list=partnerService.all();StringBuilder b=new StringBuilder("{\"success\":true,\"partners\":[");for(int i=0;i<list.size();i++){if(i>0)b.append(',');b.append(partnerToJson(list.get(i)));}b.append("]}");sendJson(exchange,200,b.toString());return;}
            if(exchange.getRequestMethod().equalsIgnoreCase("POST")){String status=d.getOrDefault("status","").toUpperCase();if(!List.of("APPROVED","REJECTED").contains(status)){sendJson(exchange,400,jsonError("Status must be APPROVED or REJECTED."));return;}boolean ok=partnerService.updateStatus(Integer.parseInt(d.getOrDefault("partnerId","0")),status);sendJson(exchange,ok?200:409,ok?"{\"success\":true,\"message\":\"Partner status updated.\"}":jsonError("Only pending partner registrations can be updated."));return;}
            sendJson(exchange,405,jsonError("Method not allowed."));
        }catch(SecurityException e){sendJson(exchange,403,jsonError(e.getMessage()));}catch(NumberFormatException e){sendJson(exchange,400,jsonError("A valid admin and partner ID are required."));}catch(Exception e){e.printStackTrace();sendJson(exchange,500,jsonError("Partner administration failed."));}
    }

    private Pet petFrom(Map<String,String> d) {
        String name=d.getOrDefault("name","").trim(), type=d.getOrDefault("type","").trim();
        int age=Integer.parseInt(d.getOrDefault("age","-1"));
        if(name.isBlank()||name.length()>100||type.isBlank()||type.length()>50||age<0||age>100)throw new IllegalArgumentException("Invalid pet details.");
        return new Pet(0,name,type,d.getOrDefault("breed",""),age,d.getOrDefault("gender",""),d.getOrDefault("description",""),d.getOrDefault("imageUrl","🐾"),"AVAILABLE");
    }

    private String petsToJson(List<Pet> list){StringBuilder b=new StringBuilder("[");for(int i=0;i<list.size();i++){if(i>0)b.append(',');Pet p=list.get(i);b.append("{\"id\":").append(p.getId()).append(",\"name\":\"").append(escapeJson(p.getName())).append("\",\"type\":\"").append(escapeJson(p.getType())).append("\",\"breed\":\"").append(escapeJson(p.getBreed())).append("\",\"age\":").append(p.getAge()).append(",\"gender\":\"").append(escapeJson(p.getGender())).append("\",\"description\":\"").append(escapeJson(p.getDescription())).append("\",\"imageUrl\":\"").append(escapeJson(p.getImageUrl())).append("\",\"status\":\"").append(escapeJson(p.getStatus())).append("\",\"partnerId\":").append(p.getPartnerId()==null?"null":p.getPartnerId()).append("}");}return b.append(']').toString();}
    private String partnerToJson(Partner p){return "{\"id\":"+p.getId()+",\"userId\":"+p.getUserId()+",\"organizationName\":\""+escapeJson(p.getOrganizationName())+"\",\"organizationType\":\""+escapeJson(p.getOrganizationType())+"\",\"contactPerson\":\""+escapeJson(p.getContactPerson())+"\",\"email\":\""+escapeJson(p.getEmail())+"\",\"phone\":\""+escapeJson(p.getPhone())+"\",\"address\":\""+escapeJson(p.getAddress())+"\",\"description\":\""+escapeJson(p.getDescription())+"\",\"verificationStatus\":\""+escapeJson(p.getVerificationStatus())+"\"}";}

    // =====================================================
    // CORS
    // =====================================================

    private void addCorsHeaders(
            HttpExchange exchange
    ) {

        Headers headers =
                exchange.getResponseHeaders();


        headers.set(
                "Access-Control-Allow-Origin",
                "*"
        );


        headers.set(
                "Access-Control-Allow-Methods",
                "GET, POST, DELETE, OPTIONS"
        );


        headers.set(
                "Access-Control-Allow-Headers",
                "Content-Type"
        );
    }


    // =====================================================
    // READ REQUEST BODY
    // =====================================================

    private String readRequestBody(
            HttpExchange exchange
    ) throws IOException {

        InputStream inputStream =
                exchange.getRequestBody();


        return new String(
                inputStream.readAllBytes(),
                StandardCharsets.UTF_8
        );
    }


    // =====================================================
    // PARSE FORM DATA
    // =====================================================

    private Map<String, String> parseFormData(
            String body
    ) {

        Map<String, String> data =
                new HashMap<>();


        if (
                body == null
                || body.isEmpty()
        ) {

            return data;
        }


        String[] pairs =
                body.split("&");


        for (String pair : pairs) {

            String[] parts =
                    pair.split(
                            "=",
                            2
                    );


            if (parts.length == 2) {

                String key =
                        URLDecoder.decode(
                                parts[0],
                                StandardCharsets.UTF_8
                        );


                String value =
                        URLDecoder.decode(
                                parts[1],
                                StandardCharsets.UTF_8
                        );


                data.put(
                        key,
                        value
                );
            }
        }


        return data;
    }


    // =====================================================
    // SEND JSON
    // =====================================================

    private void sendJson(
            HttpExchange exchange,
            int statusCode,
            String response
    ) throws IOException {

        Headers headers =
                exchange.getResponseHeaders();


        headers.set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );


        headers.set(
                "Access-Control-Allow-Origin",
                "*"
        );


        byte[] responseBytes =
                response.getBytes(
                        StandardCharsets.UTF_8
                );


        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );


        try (
                OutputStream outputStream =
                        exchange.getResponseBody()
        ) {

            outputStream.write(
                    responseBytes
            );
        }
    }


    // =====================================================
    // JSON ERROR
    // =====================================================

    private String jsonError(
            String message
    ) {

        return "{"
                + "\"success\":false,"
                + "\"message\":\""
                + escapeJson(
                        message
                )
                + "\""
                + "}";
    }


    // =====================================================
    // ESCAPE JSON
    // =====================================================

    private String escapeJson(
            String text
    ) {

        if (text == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);

            switch (character) {
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }

        return escaped.toString();
    }
}
