/**
 * Happy Tails - Minimal Partner Registration Script
 * Lightweight, accessible, validation-ready script for minimal partner registration.
 */

document.addEventListener("DOMContentLoaded", () => {
    const partnerRegisterForm = document.getElementById("partnerRegisterForm");
    const partnerRegisterMessage = document.getElementById("partnerRegisterMessage");
    const submitBtn = document.getElementById("submitPartnerBtn");

    // Form inputs
    const orgTypeSelect = document.getElementById("organizationType");
    const typePills = document.querySelectorAll(".pm-type-pill, .pr-type-card");
    const orgNameInput = document.getElementById("organizationName");
    const orgNameCounter = document.getElementById("orgNameCounter");
    const contactPersonInput = document.getElementById("contactPerson");
    const emailInput = document.getElementById("email");
    const phoneInput = document.getElementById("phone");
    const phoneCounter = document.getElementById("phoneCounter");
    const addressInput = document.getElementById("address");
    const addressCounter = document.getElementById("addressCounter");
    const descInput = document.getElementById("description");
    const descCounter = document.getElementById("descCounter");
    const passwordInput = document.getElementById("password");
    const confirmPasswordInput = document.getElementById("confirmPassword");
    const togglePassBtn = document.getElementById("togglePassword");
    const toggleConfirmPassBtn = document.getElementById("toggleConfirmPassword");

    // =========================================================================
    // 1. ORGANIZATION TYPE PILL SELECTION
    // =========================================================================
    function selectOrgType(selectedValue) {
        if (orgTypeSelect) {
            orgTypeSelect.value = selectedValue;
        }

        typePills.forEach(pill => {
            const isMatch = pill.getAttribute("data-value") === selectedValue;
            pill.classList.toggle("selected", isMatch);
            pill.setAttribute("aria-checked", isMatch ? "true" : "false");
        });
    }

    typePills.forEach(pill => {
        pill.addEventListener("click", () => {
            const val = pill.getAttribute("data-value");
            selectOrgType(val);
        });

        pill.addEventListener("keydown", (e) => {
            if (e.key === "Enter" || e.key === " ") {
                e.preventDefault();
                const val = pill.getAttribute("data-value");
                selectOrgType(val);
            }
        });
    });

    // =========================================================================
    // 2. CHARACTER COUNTERS & SANITIZATION
    // =========================================================================
    function updateCounter(input, counterElem, max) {
        if (!input || !counterElem) return;
        counterElem.textContent = `${input.value.length}/${max}`;
    }

    if (orgNameInput && orgNameCounter) {
        orgNameInput.addEventListener("input", () => updateCounter(orgNameInput, orgNameCounter, 150));
    }

    if (descInput && descCounter) {
        descInput.addEventListener("input", () => updateCounter(descInput, descCounter, 1000));
    }

    if (phoneInput && phoneCounter) {
        phoneInput.addEventListener("input", () => {
            phoneInput.value = phoneInput.value.replace(/\D/g, "");
            updateCounter(phoneInput, phoneCounter, 10);
        });
    }

    if (addressInput && addressCounter) {
        addressInput.addEventListener("input", () => updateCounter(addressInput, addressCounter, 500));
    }

    // =========================================================================
    // 3. PASSWORD VISIBILITY TOGGLES
    // =========================================================================
    function setupPasswordToggle(button, input) {
        if (!button || !input) return;
        button.addEventListener("click", () => {
            const isPassword = input.type === "password";
            input.type = isPassword ? "text" : "password";
            button.textContent = isPassword ? "🙈" : "👁️";
        });
    }

    setupPasswordToggle(togglePassBtn, passwordInput);
    setupPasswordToggle(toggleConfirmPassBtn, confirmPasswordInput);

    // =========================================================================
    // 4. FORM SUBMISSION
    // =========================================================================
    if (partnerRegisterForm) {
        partnerRegisterForm.addEventListener("submit", async (event) => {
            event.preventDefault();

            const orgName = orgNameInput ? orgNameInput.value.trim() : "";
            const contactPerson = contactPersonInput ? contactPersonInput.value.trim() : "";
            const email = emailInput ? emailInput.value.trim() : "";
            const phone = phoneInput ? phoneInput.value.trim() : "";
            const address = addressInput ? addressInput.value.trim() : "";
            const pwd = passwordInput ? passwordInput.value : "";
            const confirmPwd = confirmPasswordInput ? confirmPasswordInput.value : "";

            if (!orgName || !contactPerson || !email || !phone || !address || !pwd || !confirmPwd) {
                showMessage("Please fill in all required fields.", "error");
                return;
            }

            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
                showMessage("Please enter a valid email address.", "error");
                emailInput?.focus();
                return;
            }

            if (phone.length !== 10) {
                showMessage("Phone number must contain exactly 10 digits.", "error");
                phoneInput?.focus();
                return;
            }

            if (pwd.length < 6) {
                showMessage("Password must contain at least 6 characters.", "error");
                passwordInput?.focus();
                return;
            }

            if (pwd !== confirmPwd) {
                showMessage("Passwords do not match.", "error");
                confirmPasswordInput?.focus();
                return;
            }

            setLoading(true);
            showMessage("Submitting registration details...", "info");

            const body = new URLSearchParams(new FormData(partnerRegisterForm));

            try {
                const response = await fetch("http://localhost:8080/api/partner/register", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    body: body.toString()
                });

                const data = await response.json();

                if (data.success) {
                    showMessage(
                        "Registration submitted for admin approval! Redirecting to Partner Login...",
                        "success"
                    );
                    partnerRegisterForm.reset();
                    selectOrgType("SHELTER");

                    setTimeout(() => {
                        window.location.href = "partner-login.html";
                    }, 1800);
                } else {
                    showMessage(data.message || "Registration could not be completed.", "error");
                    setLoading(false);
                }
            } catch (error) {
                console.error("Partner registration error:", error);
                showMessage(
                    "Could not connect to the backend server (http://localhost:8080).",
                    "error"
                );
                setLoading(false);
            }
        });
    }

    function showMessage(text, type) {
        if (!partnerRegisterMessage) return;
        partnerRegisterMessage.textContent = text;
        partnerRegisterMessage.className = `pm-message message ${type}`;
    }

    function setLoading(isLoading) {
        if (!submitBtn) return;
        submitBtn.disabled = isLoading;
        submitBtn.innerHTML = isLoading 
            ? '<span>Submitting...</span>' 
            : '<span>Submit for Approval</span><span aria-hidden="true">→</span>';
    }
});
