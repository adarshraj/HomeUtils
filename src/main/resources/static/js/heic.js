// Format button toggle
document.querySelectorAll('.format-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.format-btn').forEach(b => {
            b.className = 'btn btn-outline-secondary format-btn';
        });
        btn.className = 'btn btn-primary format-btn';
        document.getElementById('formatInput').value = btn.dataset.fmt;
    });
});

// Use fetch so we can hide the spinner once the download starts
document.getElementById('convertForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.target;
    const progressArea = document.getElementById('progressArea');
    const submitBtn = document.getElementById('submitBtn');
    const errorArea = document.getElementById('errorArea');

    progressArea.classList.remove('d-none');
    errorArea.classList.add('d-none');
    submitBtn.disabled = true;

    try {
        const resp = await fetch(form.action, { method: 'POST', body: new FormData(form) });
        if (!resp.ok) {
            const msg = await resp.text();
            throw new Error(msg);
        }
        const disposition = resp.headers.get('Content-Disposition') || '';
        const match = disposition.match(/filename="([^"]+)"/);
        const filename = match ? match[1] : 'converted';
        const blob = await resp.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        a.click();
        URL.revokeObjectURL(url);
    } catch (err) {
        errorArea.textContent = 'Error: ' + err.message;
        errorArea.classList.remove('d-none');
    } finally {
        progressArea.classList.add('d-none');
        submitBtn.disabled = false;
    }
});
