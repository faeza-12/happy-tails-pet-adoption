// ==========================================
// LOGIN / ROLE PROTECTION
// ==========================================

const userName =
    sessionStorage.getItem("userName");

const userRole =
    sessionStorage.getItem("userRole");

const currentPage =
    window.location.pathname;

const userId = Number(sessionStorage.getItem("userId"));


if (!userName || !["USER", "ADMIN"].includes(userRole)
    || !Number.isSafeInteger(userId) || userId <= 0) {

    window.location.href =
        "login.html";
}


if (currentPage.includes("user-dashboard")
    && userRole !== "USER") {

    window.location.href =
        "login.html";
}


if (currentPage.includes("admin-dashboard")
    && userRole !== "ADMIN") {

    window.location.href =
        "login.html";
}


// ==========================================
// DISPLAY USER NAME
// ==========================================

const nameElement =
    document.getElementById("userName");


if (nameElement && userName) {

    nameElement.textContent =
        userName;
}


// ==========================================
// LOGOUT
// ==========================================

const logoutButton =
    document.getElementById("logoutButton");


if (logoutButton) {

    logoutButton.addEventListener(
        "click",
        function () {

            sessionStorage.clear();

            window.location.href =
                "index.html";
        }
    );
}


// ==========================================
// ADMIN PET MANAGEMENT
// ==========================================

const petList =
    document.getElementById("petList");

const petForm =
    document.getElementById("petForm");

const petMessage =
    document.getElementById("petMessage");


if (petList) {

    loadAdminPets();
}


// ==========================================
// LOAD PETS FOR ADMIN
// ==========================================

async function loadAdminPets() {

    petList.innerHTML =
        "<p>Loading pets... 🐾</p>";

    try {

        const response =
            await fetch(
                "http://localhost:8080/api/pets"
            );

        const data =
            await response.json();


        if (!data.success) {

            petList.innerHTML =
                "<p>Could not load pets.</p>";

            return;
        }


        displayAdminPets(data.pets);

    } catch (error) {

        console.error(error);

        petList.innerHTML =
            "<p>Could not connect to the Java backend.</p>";
    }
}


// ==========================================
// DISPLAY PETS FOR ADMIN
// ==========================================

function displayAdminPets(pets) {

    if (!petList) {
        return;
    }


    petList.innerHTML = "";


    if (pets.length === 0) {

        petList.innerHTML =
            "<p>No pets in the database yet. 🐾</p>";

        return;
    }


    pets.forEach(function (pet) {

        const petCard =
            document.createElement("div");


        petCard.className =
            "pet-management-card";


        petCard.innerHTML = `

            <div class="pet-management-icon">
                ${escapeDashboardHtml(pet.imageUrl || "🐾")}
            </div>

            <div class="pet-management-info">

                <h3>
                    ${escapeDashboardHtml(pet.name)}
                </h3>

                <p>
                    <strong>Type:</strong>
                    ${escapeDashboardHtml(pet.type)}
                </p>

                <p>
                    <strong>Breed:</strong>
                    ${escapeDashboardHtml(pet.breed || "Not specified")}
                </p>

                <p>
                    <strong>Age:</strong>
                    ${pet.age} year(s)
                </p>

                <p>
                    <strong>Gender:</strong>
                    ${escapeDashboardHtml(pet.gender || "Not specified")}
                </p>

                <p>
                    ${escapeDashboardHtml(pet.description || "No description available.")}
                </p>

                <span class="pet-status ${pet.status === "ADOPTED" ? "adopted" : "available"}">
                    ${escapeDashboardHtml(pet.status)}
                </span>

            </div>

            <button
                class="delete-pet-button"
                onclick="deletePet(${pet.id})">

                🗑️ Delete

            </button>

        `;


        petList.appendChild(petCard);

    });
}


// ==========================================
// ADD PET
// ==========================================

if (petForm) {

    petForm.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();


            petMessage.textContent = "";

            petMessage.className =
                "message";


            const name =
                document.getElementById("petName")
                    .value.trim();


            const type =
                document.getElementById("petType")
                    .value;


            const breed =
                document.getElementById("petBreed")
                    .value.trim();


            const age =
                document.getElementById("petAge")
                    .value.trim();


            const gender =
                document.getElementById("petGender")
                    .value;


            const description =
                document.getElementById("petDescription")
                    .value.trim();


            const imageUrl =
                document.getElementById("petImage")
                    .value.trim();


            const status =
                document.getElementById("petStatus")
                    .value;


            const formData =
                new URLSearchParams();


            formData.append(
                "name",
                name
            );

            formData.append(
                "type",
                type
            );

            formData.append(
                "breed",
                breed
            );

            formData.append(
                "age",
                age
            );

            formData.append(
                "gender",
                gender
            );

            formData.append(
                "description",
                description
            );

            formData.append(
                "imageUrl",
                imageUrl
            );

            formData.append(
                "status",
                status
            );


            try {

                const response =
                    await fetch(
                        "http://localhost:8080/api/pets/add",
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/x-www-form-urlencoded"
                            },

                            body:
                                formData.toString()
                        }
                    );


                const data =
                    await response.json();


                if (data.success) {

                    petMessage.textContent =
                        "Pet added successfully! 🐾";


                    petMessage.classList.add(
                        "success"
                    );


                    petForm.reset();


                    loadAdminPets();

                } else {

                    petMessage.textContent =
                        data.message;


                    petMessage.classList.add(
                        "error"
                    );
                }


            } catch (error) {

                console.error(error);


                petMessage.textContent =
                    "Could not connect to the Java backend.";


                petMessage.classList.add(
                    "error"
                );
            }

        }
    );
}


// ==========================================
// DELETE PET
// ==========================================

async function deletePet(id) {

    const confirmed =
        confirm(
            "Are you sure you want to delete this pet? 🐾"
        );


    if (!confirmed) {
        return;
    }


    try {

        const response =
            await fetch(
                "http://localhost:8080/api/pets/delete?id="
                + id,
                {
                    method: "DELETE"
                }
            );


        const data =
            await response.json();


        if (data.success) {

            alert(
                "Pet deleted successfully! 🐾"
            );


            loadAdminPets();

        } else {

            alert(
                data.message
            );
        }


    } catch (error) {

        console.error(error);


        alert(
            "Could not connect to the Java backend."
        );
    }
}


// ==========================================
// USER PET BROWSING
// ==========================================

const userPetList =
    document.getElementById("userPetList");


if (userPetList) {

    loadUserPets();
}


// ==========================================
// LOAD PETS FOR USER
// ==========================================

async function loadUserPets() {

    if (!userPetList) {
        return;
    }


    userPetList.innerHTML =
        "<p>Finding your furry friends... 🐾</p>";


    try {

        const response =
            await fetch(
                "http://localhost:8080/api/pets"
            );


        const data =
            await response.json();


        if (!data.success) {

            userPetList.innerHTML =
                "<p>Could not load pets.</p>";

            return;
        }


        // Only show pets that are available

        const availablePets =
            data.pets.filter(
                function (pet) {

                    return pet.status === "AVAILABLE";

                }
            );


        displayUserPets(
            availablePets
        );


    } catch (error) {

        console.error(error);


        userPetList.innerHTML =
            "<p>Could not connect to the Java backend. Make sure it is running.</p>";
    }
}


// ==========================================
// DISPLAY PETS FOR USER
// ==========================================

function displayUserPets(pets) {

    if (!userPetList) {
        return;
    }


    userPetList.innerHTML = "";


    if (pets.length === 0) {

        userPetList.innerHTML = `

            <div class="dashboard-card">

                <div class="card-icon">
                    🐾
                </div>

                <h2>
                    No Pets Available
                </h2>

                <p>
                    Don't worry! New furry friends
                    may be added soon.
                </p>

            </div>

        `;

        return;
    }


    pets.forEach(function (pet) {

        const petCard =
            document.createElement("div");


        petCard.className =
            "dashboard-card";


        petCard.innerHTML = `

            <div class="card-icon">
                ${escapeDashboardHtml(pet.imageUrl || "🐾")}
            </div>

            <h2>
                ${escapeDashboardHtml(pet.name)}
            </h2>

            <p>
                <strong>
                    ${escapeDashboardHtml(pet.type)}
                </strong>
                ${pet.breed
                    ? " • " + escapeDashboardHtml(pet.breed)
                    : ""}
            </p>

            <p>
                ${pet.age} year(s)
                ${pet.gender
                    ? " • " + escapeDashboardHtml(pet.gender)
                    : ""}
            </p>

            <p>
                ${escapeDashboardHtml(pet.description
                    || "A lovely pet looking for a home.")}
            </p>

            <span class="pet-status available">
                🟢 AVAILABLE
            </span>

            <br><br>

            <button
                class="card-button"
                onclick="viewPetDetails(${pet.id})">

                ❤️ View Details

            </button>

        `;


        userPetList.appendChild(
            petCard
        );

    });
}


// ==========================================
// VIEW PET DETAILS
// ==========================================

function viewPetDetails(id) {

    window.location.href = "pets.html#pet-" + encodeURIComponent(id);
}

function escapeDashboardHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

if (currentPage.includes("pets.html") && userRole !== "USER") {
    window.location.href = "login.html";
}
