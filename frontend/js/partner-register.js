const partnerRegisterForm = document.getElementById("partnerRegisterForm");
const partnerRegisterMessage = document.getElementById("partnerRegisterMessage");
partnerRegisterForm.addEventListener("submit", async event => {
    event.preventDefault();
    const body = new URLSearchParams(new FormData(partnerRegisterForm));
    partnerRegisterMessage.textContent = "Submitting organization details...";
    try {
        const response = await fetch("http://localhost:8080/api/partner/register", { method: "POST", headers: { "Content-Type": "application/x-www-form-urlencoded" }, body });
        const data = await response.json();
        partnerRegisterMessage.textContent = data.message || "Registration could not be completed.";
        partnerRegisterMessage.className = data.success ? "message success" : "message error";
        if (data.success) { partnerRegisterForm.reset(); setTimeout(() => location.href = "login.html", 1800); }
    } catch (error) { partnerRegisterMessage.textContent = "Could not connect to the Java backend."; partnerRegisterMessage.className = "message error"; }
});
