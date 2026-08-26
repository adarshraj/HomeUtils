function renderHashes(hashes) {
    const r = document.getElementById('results');
    r.innerHTML = '';
    Object.entries(hashes).forEach(([algo, val]) => {
        r.insertAdjacentHTML('beforeend',
            `<div class="mb-2">
                <label class="form-label fw-semibold mb-1">${algo}</label>
                <div class="input-group">
                    <input class="form-control font-monospace small" readonly value="${val}"/>
                    <button class="btn btn-outline-secondary copyHashBtn" data-value="${val}"><i class="bi bi-clipboard"></i></button>
                </div>
            </div>`);
    });
    document.getElementById('resultArea').classList.remove('d-none');
}
async function hashText() {
    const input = document.getElementById('hashInput').value;
    const err = document.getElementById('errorArea'); err.classList.add('d-none');
    const resp = await fetch('/utils/hash/text', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({input})
    });
    const data = await resp.json();
    if (!resp.ok || data.error) { err.textContent = data.error || 'Error'; err.classList.remove('d-none'); return; }
    document.getElementById('fileMeta').textContent = '';
    renderHashes(data.hashes);
}
async function hashFile() {
    const f = document.getElementById('hashFile').files[0];
    const err = document.getElementById('errorArea'); err.classList.add('d-none');
    if (!f) { err.textContent = 'Choose a file.'; err.classList.remove('d-none'); return; }
    const fd = new FormData(); fd.append('file', f);
    const resp = await fetch('/utils/hash/file', { method: 'POST', body: fd });
    const data = await resp.json();
    if (!resp.ok || data.error) { err.textContent = data.error || 'Error'; err.classList.remove('d-none'); return; }
    document.getElementById('fileMeta').textContent = `${data.filename} — ${data.size} bytes`;
    renderHashes(data.hashes);
}
document.getElementById('btnHashText').addEventListener('click', hashText);
document.getElementById('btnHashFile').addEventListener('click', hashFile);
document.getElementById('results').addEventListener('click', (e) => {
    const btn = e.target.closest('.copyHashBtn');
    if (btn) navigator.clipboard.writeText(btn.dataset.value);
});
