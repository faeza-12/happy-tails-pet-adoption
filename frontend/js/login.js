const loginForm =
    document.getElementById("loginForm");

const loginMessage =
    document.getElementById("loginMessage");


loginForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();

        loginMessage.textContent = "";
        loginMessage.className = "message";


        const email =
            document
                .getElementById("email")
                .value
                .trim();


        const password =
            document
                .getElementById("password")
                .value;


        const formData =
            new URLSearchParams();


        formData.append(
            "email",
            email
        );


        formData.append(
            "password",
            password
        );


        try {

            const response =
                await fetch(
                    "http://localhost:8080/api/login",
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


            console.log(
                "Login response:",
                data
            );


            if (data.success) {

                // =====================================
                // CHECK USER ID
                // =====================================

                const userId = Number(data.id);
                const role = String(data.role || "").toUpperCase();

                if (!Number.isSafeInteger(userId) || userId <= 0
                    || !["USER", "ADMIN", "PARTNER"].includes(role)
                    || !data.name || !data.email) {

                    loginMessage.textContent =
                        "Login succeeded, but user ID was not received.";

                    loginMessage.classList.add(
                        "error"
                    );

                    console.error(
                        "User ID missing from login response:",
                        data
                    );

                    return;
                }


                // =====================================
                // SAVE LOGIN DETAILS
                // =====================================

                sessionStorage.clear();
                sessionStorage.setItem(
                    "userId",
                    String(userId)
                );


                sessionStorage.setItem(
                    "userName",
                    data.name
                );


                sessionStorage.setItem(
                    "userEmail",
                    data.email
                );


                sessionStorage.setItem(
                    "userRole",
                    role
                );


                console.log(
                    "Saved userId:",
                    sessionStorage.getItem("userId")
                );


                loginMessage.textContent =
                    "Login successful! Redirecting...";


                loginMessage.classList.add(
                    "success"
                );


                setTimeout(
                    function () {

                        if (role === "ADMIN") {

                            window.location.href =
                                "admin-dashboard.html";

                        } else if (role === "PARTNER") {
                            window.location.href = "partner-dashboard.html";
                        } else {

                            window.location.href =
                                "user-dashboard.html";
                        }

                    },
                    700
                );


            } else {

                loginMessage.textContent =
                    data.message;


                loginMessage.classList.add(
                    "error"
                );
            }


        } catch (error) {

            console.error(
                "Login error:",
                error
            );


            loginMessage.textContent =
                "Could not connect to the Java backend. Make sure the backend is running.";


            loginMessage.classList.add(
                "error"
            );
        }

    }
);
