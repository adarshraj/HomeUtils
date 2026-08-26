function render(data) {
    const r = document.getElementById('resultArea');
    r.innerHTML = '';
    Object.entries(data).forEach(([k, v]) => {
        r.insertAdjacentHTML('beforeend',
            `<div class="mb-2">
                <label class="form-label small fw-semibold mb-1">${k}</label>
                <div class="input-group">
                    <input class="form-control font-monospace small" readonly value="${v}"/>
                    <button class="btn btn-outline-secondary copyValueBtn" data-value="${v}"><i class="bi bi-clipboard"></i></button>
                </div>
            </div>`);
    });
    r.classList.remove('d-none');
}
async function fromEpoch() {
    const err = document.getElementById('errorArea'); err.classList.add('d-none');
    const resp = await fetch('/utils/timestamp/from-epoch', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            value: document.getElementById('epochValue').value,
            unit: document.getElementById('epochUnit').value,
            zone: document.getElementById('epochZone').value || 'UTC'
        })
    });
    const data = await resp.json();
    if (!resp.ok || data.error) { err.textContent = data.error || 'Error'; err.classList.remove('d-none'); return; }
    render(data);
}
async function fromDate() {
    const err = document.getElementById('errorArea'); err.classList.add('d-none');
    const resp = await fetch('/utils/timestamp/from-date', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            value: document.getElementById('dateValue').value,
            zone: document.getElementById('dateZone').value || 'UTC'
        })
    });
    const data = await resp.json();
    if (!resp.ok || data.error) { err.textContent = data.error || 'Error'; err.classList.remove('d-none'); return; }
    render(data);
}
async function useNow() {
    const resp = await fetch('/utils/timestamp/now');
    const data = await resp.json();
    document.getElementById('epochValue').value = data.epochSeconds;
    document.getElementById('epochUnit').value = 's';
    fromEpoch();
}
document.getElementById('btnNow').addEventListener('click', useNow);
document.getElementById('btnFromEpoch').addEventListener('click', fromEpoch);
document.getElementById('btnFromDate').addEventListener('click', fromDate);
document.getElementById('resultArea').addEventListener('click', (e) => {
    const btn = e.target.closest('.copyValueBtn');
    if (btn) navigator.clipboard.writeText(btn.dataset.value);
});
