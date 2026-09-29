const partnerUserId = Number(sessionStorage.getItem("userId"));
if (sessionStorage.getItem("userRole") !== "PARTNER" || !Number.isSafeInteger(partnerUserId) || partnerUserId <= 0) location.href = "login.html";
const partnerForm = document.getElementById("partnerPetForm");
const partnerPetMessage = document.getElementById("partnerPetMessage");
const partnerPetList = document.getElementById("partnerPetList");
const partnerRequestList = document.getElementById("partnerRequestList");
const partnerParams = new URLSearchParams({ userId: String(partnerUserId) });
const escPartner = value => String(value ?? "").replace(/[&<>"']/g, c => ({ "&":"&amp;", "<":"&lt;", ">":"&gt;", '"':"&quot;", "'":"&#39;" }[c]));
async function partnerFetch(url, options = {}) { const response = await fetch(url, options); const data = await response.json(); if (!response.ok || !data.success) throw new Error(data.message || "Request failed."); return data; }
async function loadPartnerDashboard() {
    try {
        const data = await partnerFetch(`http://localhost:8080/api/partner/profile?${partnerParams}`);
        const p = data.partner;
        document.getElementById("organizationName").textContent = p.organizationName;
        document.getElementById("profileHeading").textContent = p.organizationName;
        document.getElementById("partnerStatus").textContent = p.verificationStatus;
        document.getElementById("partnerProfileDetails").textContent = `${p.organizationType.replaceAll("_", " ")} · ${p.contactPerson} · ${p.email} · ${p.phone} · ${p.address}`;
        const approved = p.verificationStatus === "APPROVED";
        document.getElementById("partnerStatusMessage").textContent = p.verificationStatus === "PENDING" ? "Your organization is waiting for admin approval." : p.verificationStatus === "REJECTED" ? "Your organization registration was not approved. Contact Happy Tails for help." : "Your organization is approved. You can manage your pet listings.";
        document.getElementById("partnerManagement").hidden = !approved;
        if (approved) { await Promise.all([loadPartnerPets(), loadPartnerRequests()]); }
    } catch (error) { document.getElementById("partnerStatus").textContent = "Unavailable"; document.getElementById("partnerStatusMessage").textContent = error.message; }
}
async function loadPartnerPets() {
    try { const data = await partnerFetch(`http://localhost:8080/api/partner/pets?${partnerParams}`); partnerPetList.replaceChildren();
        if (!data.pets.length) { partnerPetList.textContent = "No pets listed yet."; return; }
        for (const pet of data.pets) { const card = document.createElement("article"); card.className = "pet-management-card"; card.innerHTML = `<div class="pet-management-icon">${escPartner(pet.imageUrl || "🐾")}</div><div class="pet-management-info"><h3>${escPartner(pet.name)}</h3><p>${escPartner(pet.type)} · ${escPartner(pet.breed)} · ${escPartner(pet.age)} year(s) · ${escPartner(pet.gender)}</p><p>${escPartner(pet.description)}</p><span class="pet-status ${pet.status === "ADOPTED" ? "adopted" : "available"}">${escPartner(pet.status)}</span></div><div class="partner-form-actions"><button class="card-button" type="button" data-edit="${pet.id}">Edit</button><button class="delete-pet-button" type="button" data-delete="${pet.id}">Delete</button></div>`;
            card.querySelector("[data-edit]").addEventListener("click", () => editPartnerPet(pet)); card.querySelector("[data-delete]").addEventListener("click", () => deletePartnerPet(pet.id)); partnerPetList.append(card); }
    } catch (error) { partnerPetList.textContent = error.message; }
}
function editPartnerPet(pet) { for (const key of ["name","type","breed","age","gender","description","imageUrl"]) partnerForm.elements[key].value = pet[key] ?? ""; partnerForm.elements.petId.value = pet.id; document.getElementById("partnerPetSubmit").textContent = "Save Changes"; document.getElementById("cancelPetEdit").hidden = false; partnerForm.scrollIntoView({ behavior: "smooth" }); }
document.getElementById("cancelPetEdit").addEventListener("click", () => { partnerForm.reset(); partnerForm.elements.petId.value = ""; document.getElementById("partnerPetSubmit").textContent = "Add Pet"; document.getElementById("cancelPetEdit").hidden = true; });
partnerForm.addEventListener("submit", async event => { event.preventDefault(); const d = new URLSearchParams(new FormData(partnerForm)); d.set("userId", String(partnerUserId)); const editing = Boolean(d.get("petId")); const endpoint = editing ? "/api/partner/pets/update" : "/api/partner/pets/add"; partnerPetMessage.textContent = "Saving pet...";
    try { const result = await partnerFetch(`http://localhost:8080${endpoint}`, { method: "POST", headers: { "Content-Type": "application/x-www-form-urlencoded" }, body: d }); partnerPetMessage.textContent = result.message; partnerPetMessage.className = "message success"; partnerForm.reset(); partnerForm.elements.petId.value = ""; document.getElementById("partnerPetSubmit").textContent = "Add Pet"; document.getElementById("cancelPetEdit").hidden = true; await loadPartnerPets(); }
    catch(error) { partnerPetMessage.textContent = error.message; partnerPetMessage.className = "message error"; }
});
async function deletePartnerPet(id) { if (!confirm("Delete this pet listing?")) return; const query = new URLSearchParams({ id: String(id), userId: String(partnerUserId) }); try { await partnerFetch(`http://localhost:8080/api/partner/pets/delete?${query}`, { method: "DELETE" }); partnerPetMessage.textContent = "Pet deleted."; partnerPetMessage.className = "message success"; await loadPartnerPets(); } catch(error) { partnerPetMessage.textContent = error.message; partnerPetMessage.className = "message error"; } }
async function loadPartnerRequests() { try { const data = await partnerFetch(`http://localhost:8080/api/partner/adoption-requests?${partnerParams}`); partnerRequestList.replaceChildren(); if (!data.requests.length) { partnerRequestList.textContent = "No adoption requests for your pets yet."; return; } for (const request of data.requests) { const card = document.createElement("article"); card.className = "admin-request-card"; card.innerHTML = `<div class="admin-request-heading"><h3>${escPartner(request.petName)} · Request #${request.id}</h3><span class="request-status status-${escPartner(request.status).toLowerCase()}">${escPartner(request.status)}</span></div><p><strong>${escPartner(request.userName)}</strong> · ${escPartner(request.userEmail)}</p><p>${escPartner(request.message || "No message supplied.")}</p><p>${escPartner(request.createdAt || "")}</p>`; partnerRequestList.append(card); } } catch(error) { partnerRequestList.textContent = error.message; } }
document.getElementById("refreshPartnerRequests").addEventListener("click", loadPartnerRequests);
document.getElementById("partnerLogout").addEventListener("click", () => { sessionStorage.clear(); location.href = "index.html"; });
loadPartnerDashboard();
