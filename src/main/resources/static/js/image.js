const qEl = document.getElementById('quality');
const qLabel = document.getElementById('qualityLabel');
const qBlock = document.getElementById('qualityBlock');
qEl.addEventListener('input', e => qLabel.textContent = e.target.value);
document.getElementById('format').addEventListener('change', e => {
    qBlock.style.display = e.target.value === 'png' ? 'none' : '';
});
document.getElementById('form').addEventListener('submit', async e => {
    e.preventDefault();
    const err = document.getElementById('errorArea'); err.classList.add('d-none');
    const fd = new FormData(e.target);
    const resp = await fetch('/utils/image/convert', { method: 'POST', body: fd });
    if (!resp.ok) { err.textContent = await resp.text(); err.classList.remove('d-none'); return; }
    const blob = await resp.blob();
    const disp = resp.headers.get('Content-Disposition') || '';
    const match = disp.match(/filename="(.+)"/);
    const filename = match ? match[1] : 'converted';
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href = url; a.download = filename; a.click();
    URL.revokeObjectURL(url);
});
