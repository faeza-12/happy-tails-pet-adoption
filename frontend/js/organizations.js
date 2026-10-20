
const organizationList = document.getElementById("organizationList");
const message = document.getElementById("message");
const searchForm = document.getElementById("searchForm");
const keywordInput = document.getElementById("keyword");
const searchButton = document.getElementById("searchButton");

function safeText(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

async function loadOrganizations(keyword = "") {
    message.textContent = "Searching approved organizations...";
    organizationList.replaceChildren();
    searchButton.disabled = true;

    try {
        const url = "http://localhost:8080/api/partners/search?keyword="
            + encodeURIComponent(keyword.trim());

        const response = await fetch(url);
        const data = await response.json();

        if (!response.ok || !data.success) {
            message.textContent = data.message || "Could not load organizations.";
            return;
        }

        const organizations = data.partners || [];

        if (organizations.length === 0) {
            message.textContent = "No approved organizations found. Try another search.";
            return;
        }

        message.textContent = `${organizations.length} approved organization(s) found.`;

        organizations.forEach(org => {
            const card = document.createElement("article");
            card.className = "card";

            card.innerHTML = `
                <h2>${safeText(org.organizationName)}</h2>
                <p class="muted"><strong>Type:</strong>
                    ${safeText((org.organizationType || "").replaceAll("_", " "))}
                </p>
                <p class="muted"><strong>Address:</strong>
                    ${safeText(org.address || "Not provided")}
                </p>
                <p class="muted">
                    ${safeText(org.description || "No organization description provided.")}
                </p>
                <button type="button" class="view-pets">View Available Pets</button>
                <div class="pet-results"></div>
            `;

            const viewButton = card.querySelector(".view-pets");
            const petResults = card.querySelector(".pet-results");

            viewButton.addEventListener("click", async () => {
                if (viewButton.dataset.loaded === "true") {
                    petResults.replaceChildren();
                    viewButton.dataset.loaded = "false";
                    viewButton.textContent = "View Available Pets";
                    return;
                }

                viewButton.disabled = true;
                viewButton.textContent = "Loading...";
                petResults.replaceChildren();

                try {
                    const petResponse = await fetch(
                        "http://localhost:8080/api/partners/pets?partnerId="
                        + encodeURIComponent(org.id)
                    );

                    const petData = await petResponse.json();

                    if (!petResponse.ok || !petData.success) {
                        petResults.textContent =
                            petData.message || "Could not load pets.";
                        return;
                    }

                    const pets = petData.pets || [];

                    if (pets.length === 0) {
                        petResults.textContent =
                            "This organization has no available pets right now.";
                    } else {
                        pets.forEach(pet => {
                            const petCard = document.createElement("div");
                            petCard.className = "pet";

                            petCard.innerHTML = `
                                <h3>🐾 ${safeText(pet.name)}</h3>
                                <p class="muted">
                                    ${safeText(pet.type)} ·
                                    ${safeText(pet.breed || "Mixed breed")}
                                </p>
                                <p class="muted">
                                    Age: ${safeText(pet.age)} year(s) ·
                                    Gender: ${safeText(pet.gender || "Not specified")}
                                </p>
                                <p class="muted">
                                    ${safeText(pet.description || "Looking for a loving home.")}
                                </p>
                                <form class="adoption-form">
                                    <label>Message to the organization</label>
                                    <textarea name="message" rows="3" maxlength="1000"
                                        placeholder="Tell them why you would love to adopt this pet."></textarea>
                                    <button type="submit">Request Adoption</button>
                                    <p class="small-message" role="status"></p>
                                </form>
                            `;

                            const adoptionForm = petCard.querySelector("form");
                            adoptionForm.addEventListener("submit", event => {
                                submitAdoptionRequest(event, adoptionForm, pet.id);
                            });

                            petResults.appendChild(petCard);
                        });
                    }

                    viewButton.dataset.loaded = "true";
                    viewButton.textContent = "Hide Pets";
                } catch (error) {
                    console.error(error);
                    petResults.textContent =
                        "Could not connect to the Java backend.";
                } finally {
                    viewButton.disabled = false;
                    if (viewButton.dataset.loaded !== "true") {
                        viewButton.textContent = "View Available Pets";
                    }
                }
            });

            organizationList.appendChild(card);
        });
    } catch (error) {
        console.error("Organization search error:", error);
        message.textContent = "Could not connect to the Java backend.";
    } finally {
        searchButton.disabled = false;
    }
}

async function submitAdoptionRequest(event, form, petId) {
    event.preventDefault();

    const userId = Number(sessionStorage.getItem("userId"));
    const role = sessionStorage.getItem("userRole");
    const statusMessage = form.querySelector(".small-message");
    const button = form.querySelector("button[type='submit']");

    if (!Number.isSafeInteger(userId) || userId <= 0 || role !== "USER") {
        statusMessage.textContent = "Please log in with a user account to request adoption.";
        return;
    }

    button.disabled = true;
    statusMessage.textContent = "Submitting your request...";

    const body = new URLSearchParams({
        userId: String(userId),
        petId: String(petId),
        message: form.elements.message.value.trim()
    });

    try {
        const response = await fetch(
            "http://localhost:8080/api/adoption/request",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body
            }
        );

        const data = await response.json();
        statusMessage.textContent = data.message
            || (data.success ? "Adoption request submitted." : "Could not submit request.");

        if (data.success) {
            form.reset();
        }
    } catch (error) {
        console.error("Adoption request error:", error);
        statusMessage.textContent = "Could not connect to the Java backend.";
    } finally {
        button.disabled = false;
    }
}

searchForm.addEventListener("submit", event => {
    event.preventDefault();
    loadOrganizations(keywordInput.value);
});

// Show all approved organizations when the page first opens.
loadOrganizations();
