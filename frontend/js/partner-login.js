/**
 * Happy Tails - Minimal Partner Login Script
 */

document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("partnerLoginForm");
    const loginMessage = document.getElementById("partnerLoginMessage");
    const submitBtn = document.getElementById("submitPartnerLoginBtn");
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");
    const togglePassBtn = document.getElementById("toggleLoginPassword");

    // Toggle Password Visibility
    if (togglePassBtn && passwordInput) {
        togglePassBtn.addEventListener("click", () => {
            const isPassword = passwordInput.type === "password";
            passwordInput.type = isPassword ? "text" : "password";
            togglePassBtn.textContent = isPassword ? "🙈" : "👁️";
        });
    }

    // Form Submission
    if (loginForm) {
        loginForm.addEventListener("submit", async (event) => {
            event.preventDefault();

            const email = emailInput ? emailInput.value.trim() : "";
            const password = passwordInput ? passwordInput.value : "";

            if (!email || !password) {
                setMessage("Please enter both email and password.", "error");
                return;
            }

            setLoading(true);
            setMessage("Authenticating with Partner Portal...", "info");

            const formData = new URLSearchParams();
            formData.append("email", email);
            formData.append("password", password);

            try {
                const response = await fetch("http://localhost:8080/api/login", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    body: formData.toString()
                });

                const data = await response.json();

                if (data.success) {
                    const userId = Number(data.id);
                    const role = String(data.role || "").toUpperCase();

                    // Save session details
                    sessionStorage.clear();
                    sessionStorage.setItem("userId", String(userId));
                    sessionStorage.setItem("userName", data.name || "");
                    sessionStorage.setItem("userEmail", data.email || "");
                    sessionStorage.setItem("userRole", role);

                    setMessage("Login successful! Redirecting to dashboard...", "success");

                    setTimeout(() => {
                        if (role === "PARTNER") {
                            window.location.href = "partner-dashboard.html";
                        } else if (role === "ADMIN") {
                            window.location.href = "admin-dashboard.html";
                        } else {
                            window.location.href = "user-dashboard.html";
                        }
                    }, 600);
                } else {
                    setMessage(data.message || "Invalid email or password.", "error");
                    setLoading(false);
                }
            } catch (error) {
                console.error("Partner login error:", error);
                setMessage("Could not connect to backend server. Make sure it is running on port 8080.", "error");
                setLoading(false);
            }
        });
    }

    function setMessage(text, type) {
        if (!loginMessage) return;
        loginMessage.textContent = text;
        loginMessage.className = `pm-message message ${type}`;
    }

    function setLoading(isLoading) {
        if (!submitBtn) return;
        submitBtn.disabled = isLoading;
        submitBtn.innerHTML = isLoading 
            ? '<span>Authenticating...</span>' 
            : '<span>Log In to Dashboard</span><span aria-hidden="true">→</span>';
    }
});
