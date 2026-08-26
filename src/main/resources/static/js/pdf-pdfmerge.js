document.getElementById('form').addEventListener('submit', async e => {
    e.preventDefault();
    const err = document.getElementById('errorArea'); err.classList.add('d-none');
    const fd = new FormData(e.target);
    const resp = await fetch('/utils/pdfmerge/merge', { method: 'POST', body: fd });
    if (!resp.ok) { err.textContent = await resp.text(); err.classList.remove('d-none'); return; }
    const blob = await resp.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href = url; a.download = 'merged.pdf'; a.click();
    URL.revokeObjectURL(url);
});
