const registerForm = document.getElementById("registerForm");
const registerMessage = document.getElementById("registerMessage");

registerForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    const name = document.getElementById("name").value;
    const email = document.getElementById("email").value;
    const phone = document.getElementById("phone").value;
    const password = document.getElementById("password").value;
    const confirmPassword =
        document.getElementById("confirmPassword").value;

    registerMessage.textContent = "Creating account...";

    try {

        const response = await fetch(
            "http://localhost:8080/api/register",
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },

                body: new URLSearchParams({
                    name: name,
                    email: email,
                    phone: phone,
                    password: password,
                    confirmPassword: confirmPassword
                })
            }
        );

        const result = await response.json();

        if (result.success) {

            registerMessage.textContent =
                "Account created successfully!";

            registerMessage.className =
                "message success";

            registerForm.reset();

        } else {

            registerMessage.textContent =
                result.message || "Registration failed.";

            registerMessage.className =
                "message error";
        }

    } catch (error) {

        console.error("Registration error:", error);

        registerMessage.textContent =
            "Could not connect to the backend.";

        registerMessage.className =
            "message error";
    }

});