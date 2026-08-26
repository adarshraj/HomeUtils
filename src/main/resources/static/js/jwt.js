async function decode() {
    const token = document.getElementById('tokenInput').value.trim();
    const error = document.getElementById('errorArea');
    const result = document.getElementById('resultArea');
    error.classList.add('d-none');
    result.classList.add('d-none');

    const resp = await fetch('/utils/jwt/decode', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({token})
    });
    const data = await resp.json();
    if (!resp.ok || data.error) {
        error.textContent = data.error || 'Decode failed.';
        error.classList.remove('d-none');
        return;
    }
    document.getElementById('headerOut').textContent  = data.header;
    document.getElementById('payloadOut').textContent = data.payload;
    document.getElementById('sigOut').textContent     = data.signature || '(none)';
    document.getElementById('issuedAt').textContent   = data.issuedAt || '—';
    document.getElementById('expiry').textContent     = data.expiry   || '—';

    const badge = document.getElementById('expiryBadge');
    if (data.expiry) {
        badge.innerHTML = data.expired
            ? '<span class="badge bg-danger fs-6"><i class="bi bi-x-circle me-1"></i>Token EXPIRED</span>'
            : '<span class="badge bg-success fs-6"><i class="bi bi-check-circle me-1"></i>Token valid</span>';
    } else {
        badge.innerHTML = '<span class="badge bg-secondary">No expiry claim</span>';
    }
    result.classList.remove('d-none');
}
document.getElementById('tokenInput').addEventListener('keydown', e => {
    if (e.key === 'Enter' && e.ctrlKey) decode();
});
document.getElementById('btnDecode').addEventListener('click', decode);
