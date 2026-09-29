const adminRequestList = document.getElementById("adminRequestList");
const requestActionMessage = document.getElementById("requestActionMessage");

function escapeRequestHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

async function loadAdminRequests() {
    if (sessionStorage.getItem("userRole") !== "ADMIN") {
        window.location.href = "login.html";
        return;
    }

    adminRequestList.innerHTML = "<p>Loading adoption requests...</p>";
    try {
        const adminId = Number(sessionStorage.getItem("userId"));
        const response = await fetch(`http://localhost:8080/api/adoption/all?userId=${encodeURIComponent(adminId)}`);
        const data = await response.json();
        if (!response.ok || !data.success) {
            adminRequestList.textContent = data.message || "Could not load adoption requests.";
            return;
        }

        if (!data.requests || data.requests.length === 0) {
            adminRequestList.innerHTML = "<p>No adoption requests yet.</p>";
            return;
        }

        adminRequestList.replaceChildren();
        data.requests.forEach(request => {
            const card = document.createElement("article");
            card.className = "admin-request-card";
            const canReview = request.status === "PENDING";
            card.innerHTML = `
                <div class="admin-request-heading">
                    <h3>Request #${Number(request.id)} · ${escapeRequestHtml(request.petName || "Unknown pet")}</h3>
                    <span class="request-status status-${String(request.status).toLowerCase()}">${escapeRequestHtml(request.status)}</span>
                </div>
                <p><strong>Applicant:</strong> ${escapeRequestHtml(request.userName)} (${escapeRequestHtml(request.userEmail)})</p>
                <p><strong>Message:</strong> ${escapeRequestHtml(request.message || "No message")}</p>
                <p><strong>Date:</strong> ${escapeRequestHtml(request.createdAt || "N/A")}</p>
                ${canReview ? `
                    <div class="admin-request-actions">
                        <button class="primary-button" type="button" data-request-id="${Number(request.id)}" data-status="APPROVED">Approve</button>
                        <button class="delete-pet-button" type="button" data-request-id="${Number(request.id)}" data-status="REJECTED">Reject</button>
                    </div>` : ""}`;
            adminRequestList.appendChild(card);
        });
    } catch (error) {
        console.error("Admin request loading error:", error);
        adminRequestList.textContent = "Could not connect to the Java backend.";
    }
}

adminRequestList.addEventListener("click", async event => {
    const button = event.target.closest("button[data-request-id]");
    if (!button) {
        return;
    }

    button.disabled = true;
    requestActionMessage.textContent = "Updating request...";
    const formData = new URLSearchParams({
        requestId: button.dataset.requestId,
        status: button.dataset.status,
        userId: sessionStorage.getItem("userId")
    });
    try {
        const response = await fetch("http://localhost:8080/api/adoption/status", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData
        });
        const data = await response.json();
        requestActionMessage.textContent = data.message || "Could not update request.";
        requestActionMessage.className = data.success ? "message success" : "message error";
        if (data.success) {
            await loadAdminRequests();
            if (typeof loadAdminPets === "function") {
                await loadAdminPets();
            }
        }
    } catch (error) {
        console.error("Admin request update error:", error);
        requestActionMessage.textContent = "Could not connect to the Java backend.";
        requestActionMessage.className = "message error";
    } finally {
        button.disabled = false;
    }
});

document.getElementById("refreshRequests").addEventListener("click", loadAdminRequests);
loadAdminRequests();
