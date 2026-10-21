
"use strict";

// ==========================================
// HAPPY TAILS - PARTNER DASHBOARD
// ==========================================

const API_BASE = "http://localhost:8080/api";

const partnerUserId = Number(sessionStorage.getItem("userId"));
const partnerRole = sessionStorage.getItem("userRole");

const partnerForm = document.getElementById("partnerPetForm");
const partnerPetMessage = document.getElementById("partnerPetMessage");
const partnerPetList = document.getElementById("partnerPetList");
const partnerRequestList = document.getElementById("partnerRequestList");

const partnerParams = new URLSearchParams({
    userId: String(partnerUserId)
});

let currentPartner = null;

// ==========================================
// CHECK LOGIN
// ==========================================

if (
    partnerRole !== "PARTNER" ||
    !Number.isSafeInteger(partnerUserId) ||
    partnerUserId <= 0
) {
    window.location.href = "login.html";
}

// ==========================================
// HELPER FUNCTIONS
// ==========================================

function escapeHTML(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

async function partnerFetch(url, options = {}) {
    const response = await fetch(url, options);

    let data;

    try {
        data = await response.json();
    } catch {
        throw new Error("The server returned an invalid response.");
    }

    if (!response.ok || data.success === false) {
        throw new Error(data.message || `Request failed (${response.status}).`);
    }

    return data;
}

function setMessage(element, message, type = "") {
    element.textContent = message;
    element.className = type ? `message ${type}` : "message";
}

// ==========================================
// LOAD PARTNER PROFILE AND APPROVAL STATUS
// ==========================================

async function loadPartnerDashboard() {
    const statusElement = document.getElementById("partnerStatus");
    const statusMessage = document.getElementById("partnerStatusMessage");
    const management = document.getElementById("partnerManagement");

    statusElement.textContent = "Loading...";
    statusMessage.textContent = "";
    management.hidden = true;

    try {
        const data = await partnerFetch(
            `${API_BASE}/partner/profile?${partnerParams}`
        );

        const partner = data.partner;

        if (!partner) {
            throw new Error("Partner profile was not returned by the server.");
        }

        currentPartner = partner;

        const organizationName =
            partner.organizationName || "Your Organization";

        document.getElementById("organizationName").textContent =
            organizationName;

        document.getElementById("profileHeading").textContent =
            organizationName;

        const verificationStatus =
            String(partner.verificationStatus || "PENDING").toUpperCase();

        statusElement.textContent = verificationStatus;

        document.getElementById("partnerProfileDetails").textContent = [
            String(partner.organizationType || "").replaceAll("_", " "),
            partner.contactPerson,
            partner.email,
            partner.phone,
            partner.address
        ].filter(Boolean).join(" · ");

        if (verificationStatus === "APPROVED") {
            statusMessage.textContent =
                "Your organization is approved! You can manage your pet listings.";

            management.hidden = false;

            await Promise.all([
                loadPartnerPets(),
                loadPartnerRequests()
            ]);

        } else if (verificationStatus === "PENDING") {
            statusMessage.textContent =
                "Your registration is waiting for admin approval. " +
                "You can manage your listings after approval.";

        } else if (verificationStatus === "REJECTED") {
            statusMessage.textContent =
                "Your organization registration was not approved. " +
                "Please contact Happy Tails for assistance.";

        } else {
            statusMessage.textContent =
                "Your organization status is currently " +
                verificationStatus + ".";
        }

    } catch (error) {
        statusElement.textContent = "Unavailable";
        statusMessage.textContent =
            error.message + " Make sure the backend server is running.";
    }
}

// ==========================================
// LOAD THE PARTNER'S PET LISTINGS
// ==========================================

async function loadPartnerPets() {
    partnerPetList.replaceChildren();
    partnerPetList.textContent = "Loading your pet listings...";

    try {
        const data = await partnerFetch(
            `${API_BASE}/partner/pets?${partnerParams}`
        );

        const pets = Array.isArray(data.pets) ? data.pets : [];

        partnerPetList.replaceChildren();

        if (pets.length === 0) {
            partnerPetList.textContent = "No pets listed yet. Add your first pet!";
            return;
        }

        for (const pet of pets) {
            const card = document.createElement("article");
            card.className = "pet-management-card";

            const status = String(pet.status || "AVAILABLE").toUpperCase();

            card.innerHTML = `
                <div class="pet-management-icon">
                    ${escapeHTML(pet.imageUrl || "🐾")}
                </div>

                <div class="pet-management-info">
                    <h3>${escapeHTML(pet.name)}</h3>

                    <p>
                        ${escapeHTML(pet.type || "Pet")}
                        · ${escapeHTML(pet.breed || "Breed not specified")}
                        · ${escapeHTML(pet.age ?? "Unknown")} year(s)
                        · ${escapeHTML(pet.gender || "Not specified")}
                    </p>

                    <p>${escapeHTML(pet.description || "No description provided.")}</p>

                    <span class="pet-status ${
                        status === "ADOPTED" ? "adopted" : "available"
                    }">
                        ${escapeHTML(status)}
                    </span>
                </div>

                <div class="partner-form-actions">
                    <button
                        class="card-button"
                        type="button"
                        data-edit="${escapeHTML(pet.id)}"
                        ${status === "ADOPTED" ? "disabled" : ""}
                    >
                        Edit
                    </button>

                    <button
                        class="delete-pet-button"
                        type="button"
                        data-delete="${escapeHTML(pet.id)}"
                    >
                        Delete
                    </button>
                </div>
            `;

            const editButton = card.querySelector("[data-edit]");
            const deleteButton = card.querySelector("[data-delete]");

            editButton.addEventListener("click", () => {
                editPartnerPet(pet);
            });

            deleteButton.addEventListener("click", () => {
                deletePartnerPet(pet.id);
            });

            partnerPetList.appendChild(card);
        }

    } catch (error) {
        partnerPetList.textContent =
            "Could not load pet listings: " + error.message;
    }
}

// ==========================================
// ADD OR UPDATE A PET
// ==========================================

function resetPartnerPetForm() {
    partnerForm.reset();
    partnerForm.elements.petId.value = "";

    document.getElementById("partnerPetSubmit").textContent = "Add Pet";
    document.getElementById("cancelPetEdit").hidden = true;
}

function editPartnerPet(pet) {
    if (String(pet.status).toUpperCase() === "ADOPTED") {
        setMessage(
            partnerPetMessage,
            "Adopted pets cannot be edited.",
            "error"
        );
        return;
    }

    const fields = [
        "name",
        "type",
        "breed",
        "age",
        "gender",
        "description",
        "imageUrl"
    ];

    for (const field of fields) {
        if (partnerForm.elements[field]) {
            partnerForm.elements[field].value = pet[field] ?? "";
        }
    }

    partnerForm.elements.petId.value = pet.id;

    document.getElementById("partnerPetSubmit").textContent = "Save Changes";
    document.getElementById("cancelPetEdit").hidden = false;

    partnerForm.scrollIntoView({
        behavior: "smooth",
        block: "center"
    });
}

document.getElementById("cancelPetEdit").addEventListener("click", () => {
    resetPartnerPetForm();
    setMessage(partnerPetMessage, "");
});

partnerForm.addEventListener("submit", async event => {
    event.preventDefault();

    if (!currentPartner ||
        String(currentPartner.verificationStatus).toUpperCase() !== "APPROVED") {
        setMessage(
            partnerPetMessage,
            "Your organization must be approved before listing pets.",
            "error"
        );
        return;
    }

    const formData = new FormData(partnerForm);
    const params = new URLSearchParams();

    for (const [key, value] of formData.entries()) {
        params.set(key, String(value).trim());
    }

    params.set("userId", String(partnerUserId));

    const petId = params.get("petId");
    const isEditing = Boolean(petId);

    const endpoint = isEditing
        ? "/partner/pets/update"
        : "/partner/pets/add";

    const submitButton = document.getElementById("partnerPetSubmit");

    submitButton.disabled = true;
    setMessage(
        partnerPetMessage,
        isEditing ? "Updating pet..." : "Adding pet..."
    );

    try {
        const result = await partnerFetch(`${API_BASE}${endpoint}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: params
        });

        setMessage(
            partnerPetMessage,
            result.message || (isEditing ? "Pet updated successfully!" : "Pet added successfully!"),
            "success"
        );

        resetPartnerPetForm();

        await loadPartnerPets();

    } catch (error) {
        setMessage(partnerPetMessage, error.message, "error");
    } finally {
        submitButton.disabled = false;
    }
});

// ==========================================
// DELETE A PET LISTING
// ==========================================

async function deletePartnerPet(petId) {
    const confirmed = window.confirm(
        "Are you sure you want to delete this pet listing?"
    );

    if (!confirmed) return;

    const params = new URLSearchParams({
        id: String(petId),
        userId: String(partnerUserId)
    });

    setMessage(partnerPetMessage, "Deleting pet...");

    try {
        const result = await partnerFetch(
            `${API_BASE}/partner/pets/delete?${params}`,
            { method: "DELETE" }
        );

        setMessage(
            partnerPetMessage,
            result.message || "Pet listing deleted.",
            "success"
        );

        await loadPartnerPets();

    } catch (error) {
        setMessage(partnerPetMessage, error.message, "error");
    }
}

// ==========================================
// LOAD ADOPTION REQUESTS FOR THIS PARTNER'S PETS
// ==========================================


async function loadPartnerRequests() {
    partnerRequestList.replaceChildren();
    partnerRequestList.textContent = "Loading adoption requests...";

    try {
        const data = await partnerFetch(
            `${API_BASE}/partner/adoption-requests?${partnerParams}`
        );

        const requests = Array.isArray(data.requests)
            ? data.requests
            : [];

        partnerRequestList.replaceChildren();

        if (requests.length === 0) {
            partnerRequestList.textContent =
                "No adoption requests for your pets yet.";
            return;
        }

        for (const request of requests) {
            const card = document.createElement("article");
            card.className = "admin-request-card";

            const status = String(request.status || "PENDING").toUpperCase();
            const email = String(request.userEmail || "").trim();
            const phone = String(request.userPhone || "").trim();

            let partnerMessage;

            if (status === "APPROVED") {
                partnerMessage = "Happy Tails admin approved this request.";
            } else if (status === "REJECTED") {
                partnerMessage = "Happy Tails admin rejected this request.";
            } else {
                partnerMessage =
                    "Waiting for Happy Tails admin to review this request.";
            }

            const emailLink = email
                ? `<a href="mailto:${escapeHTML(email)}">Email adopter</a>`
                : `<span>Email not available</span>`;

            const phoneLink = phone
                ? `<a href="tel:${escapeHTML(phone)}">Call adopter</a>`
                : `<span>Phone number not available</span>`;

            card.innerHTML = `
                <div class="admin-request-heading">
                    <h3>
                        ${escapeHTML(request.petName || "Pet")}
                        · Request #${escapeHTML(request.id)}
                    </h3>

                    <span class="request-status status-${escapeHTML(status.toLowerCase())}">
                        ${escapeHTML(status)}
                    </span>
                </div>

                <p>
                    <strong>${escapeHTML(request.userName || "Adopter")}</strong>
                </p>

                <p>
                    <strong>Email:</strong>
                    ${escapeHTML(email || "Not provided")}
                </p>

                <p>
                    <strong>Phone:</strong>
                    ${escapeHTML(phone || "Not provided")}
                </p>

                <div class="partner-contact-actions">
                    ${phoneLink}
                    ·
                    ${emailLink}
                </div>

                <p>
                    <strong>Message:</strong>
                    ${escapeHTML(request.message || "No message supplied.")}
                </p>

                <p>
                    <strong>Requested on:</strong>
                    ${escapeHTML(request.createdAt || "Date unavailable")}
                </p>

                <p class="partner-request-note">
                    ${escapeHTML(partnerMessage)}
                </p>
            `;

            partnerRequestList.appendChild(card);
        }

    } catch (error) {
        partnerRequestList.textContent =
            "Could not load adoption requests: " + error.message;
    }
}


// ==========================================
// REFRESH BUTTON
// ==========================================

document.getElementById("refreshPartnerRequests")
    .addEventListener("click", async () => {
        const button = document.getElementById("refreshPartnerRequests");

        button.disabled = true;

        try {
            await loadPartnerRequests();
        } finally {
            button.disabled = false;
        }
    });

// ==========================================
// LOGOUT
// ==========================================

document.getElementById("partnerLogout").addEventListener("click", () => {
    sessionStorage.clear();
    window.location.href = "index.html";
});

// ==========================================
// START DASHBOARD
// ==========================================

loadPartnerDashboard();
