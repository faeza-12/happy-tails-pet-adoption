const petsContainer = document.getElementById("petsContainer");
const petsMessage = document.getElementById("petsMessage");
const refreshPets = document.getElementById("refreshPets");

function escapePetHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

async function loadPets() {
    const userId = Number(sessionStorage.getItem("userId"));
    if (!Number.isSafeInteger(userId) || userId <= 0
        || sessionStorage.getItem("userRole") !== "USER") {
        window.location.href = "login.html";
        return;
    }

    petsMessage.textContent = "Loading available pets...";
    petsContainer.replaceChildren();

    try {
        const response = await fetch("http://localhost:8080/api/pets");
        const data = await response.json();
        if (!response.ok || !data.success) {
            petsMessage.textContent = data.message || "Could not load pets.";
            return;
        }

        const availablePets = (data.pets || []).filter(
            pet => pet.status === "AVAILABLE"
        );
        petsMessage.textContent = availablePets.length
            ? ""
            : "No pets are currently available for adoption.";

        availablePets.forEach(pet => {
            const card = document.createElement("article");
            card.className = "pet-card-user";
            card.id = "pet-" + Number(pet.id);
            card.innerHTML = `
                <div class="pet-emoji">${escapePetHtml(pet.imageUrl || "🐾")}</div>
                <h2>${escapePetHtml(pet.name)}</h2>
                <p><strong>${escapePetHtml(pet.type)}</strong> · ${escapePetHtml(pet.breed || "Mixed breed")}</p>
                <p>${escapePetHtml(pet.age)} year(s) · ${escapePetHtml(pet.gender || "Not specified")}</p>
                <p class="pet-description">${escapePetHtml(pet.description || "A lovely pet looking for a caring home.")}</p>
                <span class="pet-status-user">Available for adoption</span>
                <p class="listing-organization"><strong>Listed by:</strong> ${escapePetHtml(pet.partnerName || "Happy Tails community listing")}${pet.partnerType ? ` · ${escapePetHtml(pet.partnerType.replaceAll("_", " "))}` : ""}</p>
                <form class="adoption-request-form" data-pet-id="${Number(pet.id)}">
                    <label for="message-${Number(pet.id)}">Message to the organization</label>
                    <textarea id="message-${Number(pet.id)}" name="message" maxlength="1000" rows="3" placeholder="I would love to give this pet a caring home."></textarea>
                    <button class="hero-button primary pet-details-button" type="submit">Request Adoption</button>
                </form>`;

            const form = card.querySelector("form");
            form.addEventListener("submit", event => submitAdoptionRequest(event, form));
            petsContainer.appendChild(card);
        });

        if (window.location.hash) {
            document.getElementById(window.location.hash.slice(1))?.scrollIntoView();
        }
    } catch (error) {
        console.error("Pet loading error:", error);
        petsMessage.textContent = "Could not connect to the Java backend.";
    }
}

async function submitAdoptionRequest(event, form) {
    event.preventDefault();
    const userId = Number(sessionStorage.getItem("userId"));
    const petId = Number(form.dataset.petId);
    const button = form.querySelector("button[type='submit']");
    const formData = new URLSearchParams({
        userId: String(userId),
        petId: String(petId),
        message: form.elements.message.value.trim()
    });

    button.disabled = true;
    petsMessage.textContent = "Submitting your request...";
    try {
        const response = await fetch("http://localhost:8080/api/adoption/request", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData
        });
        const data = await response.json();
        petsMessage.textContent = data.message || (data.success
            ? "Adoption request submitted."
            : "Could not submit your request.");
        petsMessage.className = data.success ? "message success" : "message error";
        if (data.success) {
            form.reset();
        }
    } catch (error) {
        console.error("Adoption request error:", error);
        petsMessage.textContent = "Could not connect to the Java backend.";
        petsMessage.className = "message error";
    } finally {
        button.disabled = false;
    }
}

refreshPets.addEventListener("click", loadPets);
loadPets();
