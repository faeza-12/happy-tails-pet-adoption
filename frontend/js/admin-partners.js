const adminPartnerUserId = Number(sessionStorage.getItem("userId"));
const adminPartnerList = document.getElementById("adminPartnerList");
const adminPartnerMessage = document.getElementById("partnerAdminMessage");
const safePartnerText = value => String(value ?? "").replace(/[&<>"']/g, c => ({ "&":"&amp;", "<":"&lt;", ">":"&gt;", '"':"&quot;", "'":"&#39;" }[c]));
async function loadAdminPartners() {
    adminPartnerList.textContent = "Loading organizations...";
    try {
        const query = new URLSearchParams({ userId: String(adminPartnerUserId) });
        const response = await fetch(`http://localhost:8080/api/admin/partners?${query}`); const data = await response.json();
        if (!response.ok || !data.success) throw new Error(data.message || "Could not load partners.");
        const partners = data.partners || [];
        document.getElementById("partnerSummary").innerHTML = ["TOTAL","PENDING","APPROVED","REJECTED"].map(status => `<div class="partner-summary-item"><strong>${status === "TOTAL" ? partners.length : partners.filter(p => p.verificationStatus === status).length}</strong><span>${status.charAt(0) + status.slice(1).toLowerCase()} partners</span></div>`).join("");
        adminPartnerList.replaceChildren();
        if (!partners.length) { adminPartnerList.textContent = "No partner organizations registered yet."; return; }
        for (const p of partners) { const card = document.createElement("article"); card.className = "admin-request-card"; card.innerHTML = `<div class="admin-request-heading"><h3>${safePartnerText(p.organizationName)}</h3><span class="request-status status-${safePartnerText(p.verificationStatus).toLowerCase()}">${safePartnerText(p.verificationStatus)}</span></div><p><strong>Type:</strong> ${safePartnerText(p.organizationType.replaceAll("_"," "))}</p><p><strong>Contact:</strong> ${safePartnerText(p.contactPerson)} · ${safePartnerText(p.email)} · ${safePartnerText(p.phone)}</p><p><strong>Address:</strong> ${safePartnerText(p.address)}</p><p>${safePartnerText(p.description)}</p>`;
            if (p.verificationStatus === "PENDING") { const actions=document.createElement("div");actions.className="admin-request-actions";for(const status of ["APPROVED","REJECTED"]){const button=document.createElement("button");button.className=status==="APPROVED"?"card-button":"delete-pet-button";button.textContent=status==="APPROVED"?"Approve":"Reject";button.addEventListener("click",()=>updatePartnerStatus(p.id,status));actions.append(button);}card.append(actions); }
            adminPartnerList.append(card);
        }
    } catch(error) { adminPartnerList.textContent=error.message; }
}
async function updatePartnerStatus(partnerId,status) { const body=new URLSearchParams({userId:String(adminPartnerUserId),partnerId:String(partnerId),status});try{const response=await fetch("http://localhost:8080/api/admin/partners/status",{method:"POST",headers:{"Content-Type":"application/x-www-form-urlencoded"},body});const data=await response.json();if(!response.ok||!data.success)throw new Error(data.message||"Could not update status.");adminPartnerMessage.textContent=data.message;adminPartnerMessage.className="message success";await loadAdminPartners();}catch(error){adminPartnerMessage.textContent=error.message;adminPartnerMessage.className="message error";}}
document.getElementById("refreshPartners").addEventListener("click",loadAdminPartners);
loadAdminPartners();
