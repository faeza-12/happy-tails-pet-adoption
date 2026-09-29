const requestList =
    document.getElementById("requestList");


// =====================================================
// LOAD MY ADOPTION REQUESTS
// =====================================================

async function loadMyRequests() {

    const userId =
        sessionStorage.getItem("userId");

    // User is not logged in
    const numericUserId = Number(userId);
    if (!Number.isSafeInteger(numericUserId) || numericUserId <= 0
        || sessionStorage.getItem("userRole") !== "USER") {

        window.location.href =
            "login.html";

        return;
    }

    requestList.innerHTML =
        "<p>Loading your requests... 🐾</p>";

    try {

        const response =
            await fetch(
                "http://localhost:8080/api/adoption/user?userId="
                + encodeURIComponent(String(numericUserId))
            );

        const data =
            await response.json();

        if (!response.ok || !data.success) {
            showRequestError(data.message || "Could not load your requests.");
            return;
        }

        console.log("Adoption requests:", data);

        // No requests yet
        if (
            !data.requests ||
            data.requests.length === 0
        ) {

            requestList.innerHTML = `
                <div class="dashboard-card">

                    <div class="card-icon">
                        🐾
                    </div>

                    <h2>
                        No Adoption Requests Yet
                    </h2>

                    <p>
                        You haven't requested to adopt
                        any pets yet.
                    </p>

                    <button
                        class="card-button"
                        onclick="window.location.href='pets.html'">
                        Browse Pets 🐶
                    </button>

                </div>
            `;

            return;
        }


        // Clear loading message
        requestList.innerHTML = "";


        // Display every request
        data.requests.forEach(function (request) {

            const card =
                document.createElement("div");

            card.className =
                "dashboard-card request-card";


            // Status style
            let statusClass =
                "status-pending";

            if (request.status === "APPROVED") {

                statusClass =
                    "status-approved";
            }

            else if (request.status === "REJECTED") {

                statusClass =
                    "status-rejected";
            }


            card.innerHTML = `

                <div class="card-icon">
                    ❤️
                </div>

                <h2>
                    ${escapeHtml(
                        request.petName ||
                        "Unknown Pet"
                    )}
                </h2>

                <p>
                    <strong>Request ID:</strong>
                    #${request.id}
                </p>

                <p>
                    <strong>Your Message:</strong>
                    ${escapeHtml(
                        request.message ||
                        "No message"
                    )}
                </p>

                <p>
                    <strong>Date:</strong>
                    ${escapeHtml(
                        request.createdAt ||
                        "N/A"
                    )}
                </p>

                <div class="request-status ${statusClass}">
                    ${escapeHtml(request.status)}
                </div>

            `;

            requestList.appendChild(card);

        });

    }

    catch (error) {

        console.error(
            "Adoption request error:",
            error
        );

        showRequestError("Could not connect to the Java backend.", true);
    }
}

function showRequestError(message, allowRetry = false) {
    requestList.innerHTML = `
        <div class="dashboard-card">
            <div class="card-icon">⚠️</div>
            <h2>Could Not Load Requests</h2>
            <p>${escapeHtml(message)}</p>
            ${allowRetry ? '<button class="card-button" onclick="loadMyRequests()">Try Again 🔄</button>' : ""}
        </div>`;
}


// =====================================================
// HTML SAFETY
// =====================================================

function escapeHtml(value) {

    if (
        value === null ||
        value === undefined
    ) {
        return "";
    }

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}


// =====================================================
// LOGOUT
// =====================================================

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


// =====================================================
// LOAD REQUESTS WHEN PAGE OPENS
// =====================================================

loadMyRequests();
