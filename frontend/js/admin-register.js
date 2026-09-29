const adminRegisterForm =
    document.getElementById("adminRegisterForm");

const adminRegisterMessage =
    document.getElementById("adminRegisterMessage");


adminRegisterForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();

        adminRegisterMessage.textContent = "";
        adminRegisterMessage.className = "message";


        const name =
            document.getElementById("name").value.trim();

        const email =
            document.getElementById("email").value.trim();

        const phone =
            document.getElementById("phone").value.trim();

        const password =
            document.getElementById("password").value;

        const confirmPassword =
            document.getElementById("confirmPassword").value;

        const adminCode =
            document.getElementById("adminCode").value;


        // Basic frontend validation

        if (password !== confirmPassword) {

            adminRegisterMessage.textContent =
                "Passwords do not match.";

            adminRegisterMessage.classList.add("error");

            return;
        }


        const formData =
            new URLSearchParams();

        formData.append("name", name);
        formData.append("email", email);
        formData.append("phone", phone);
        formData.append("password", password);
        formData.append(
            "confirmPassword",
            confirmPassword
        );
        formData.append(
            "adminCode",
            adminCode
        );


        try {

            const response =
                await fetch(
                    "http://localhost:8080/api/admin/register",
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

                adminRegisterMessage.textContent =
                    "Admin account created successfully! Redirecting to login...";

                adminRegisterMessage.classList.add(
                    "success"
                );


                setTimeout(
                    function () {

                        window.location.href =
                            "login.html";

                    },
                    1200
                );

            } else {

                adminRegisterMessage.textContent =
                    data.message;

                adminRegisterMessage.classList.add(
                    "error"
                );
            }

        } catch (error) {

            console.error(error);

            adminRegisterMessage.textContent =
                "Could not connect to the Java backend. Make sure the backend is running.";

            adminRegisterMessage.classList.add(
                "error"
            );
        }

    }
);