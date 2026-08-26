document.getElementById('img2pdfForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const progress = document.getElementById('progressArea');
    const error = document.getElementById('errorArea');
    const btn = document.getElementById('submitBtn');
    progress.classList.remove('d-none'); error.classList.add('d-none'); btn.disabled = true;
    try {
        const resp = await fetch(e.target.action, {method: 'POST', body: new FormData(e.target)});
        if (!resp.ok) { throw new Error(await resp.text()); }
        const disposition = resp.headers.get('Content-Disposition') || '';
        const match = disposition.match(/filename="([^"]+)"/);
        const blob = await resp.blob();
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = match ? match[1] : 'combined.pdf';
        a.click();
    } catch(err) {
        error.textContent = err.message; error.classList.remove('d-none');
    } finally {
        progress.classList.add('d-none'); btn.disabled = false;
    }
});
