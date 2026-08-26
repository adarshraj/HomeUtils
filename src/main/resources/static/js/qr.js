let lastPngBase64 = '';
let lastContent = '';
let lastSize = 300;
document.getElementById('qrSize').addEventListener('input', function() {
    document.getElementById('sizeLabel').textContent = this.value;
});
async function generate() {
    const content = document.getElementById('qrContent').value.trim();
    const size = parseInt(document.getElementById('qrSize').value);
    const err = document.getElementById('errorArea');
    const res = document.getElementById('resultArea');
    err.classList.add('d-none');

    const resp = await fetch('/utils/qr/generate', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({content, size: String(size)})
    });
    const data = await resp.json();
    if (!resp.ok || data.error) { err.textContent = data.error; err.classList.remove('d-none'); return; }
    document.getElementById('qrImage').src = data.dataUrl;
    lastPngBase64 = data.pngBase64;
    lastContent = content;
    lastSize = size;
    res.classList.remove('d-none');
}
async function downloadQr() {
    const resp = await fetch('/utils/qr/download', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({content: lastContent, size: String(lastSize)})
    });
    const blob = await resp.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href = url; a.download = 'qrcode.png'; a.click();
    URL.revokeObjectURL(url);
}
document.getElementById('btnGenerate').addEventListener('click', generate);
document.getElementById('btnDownloadQr').addEventListener('click', downloadQr);
