async function textOp(op) {
    const input = document.getElementById('textInput').value;
    const err = document.getElementById('textError');
    err.classList.add('d-none');
    const resp = await fetch('/utils/base64/' + op + '-text', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({input})
    });
    const data = await resp.json();
    if (!resp.ok || data.error) { err.textContent = data.error; err.classList.remove('d-none'); return; }
    document.getElementById('textOutput').value = data.result;
}
function swapText() {
    const a = document.getElementById('textInput');
    const b = document.getElementById('textOutput');
    const tmp = a.value; a.value = b.value; b.value = tmp;
}
function copyText() { navigator.clipboard.writeText(document.getElementById('textOutput').value); }
function copyFileEncode() { navigator.clipboard.writeText(document.getElementById('fileEncodeOutput').value); }
async function encodeFile() {
    const file = document.getElementById('encodeFile').files[0];
    const err = document.getElementById('fileEncodeError');
    err.classList.add('d-none');
    if (!file) { err.textContent = 'Select a file first.'; err.classList.remove('d-none'); return; }
    const fd = new FormData(); fd.append('file', file);
    const resp = await fetch('/utils/base64/encode-file', {method: 'POST', body: fd});
    const data = await resp.json();
    if (!resp.ok || data.error) { err.textContent = data.error; err.classList.remove('d-none'); return; }
    document.getElementById('fileEncodeOutput').value = data.result;
    document.getElementById('fileEncodeResult').classList.remove('d-none');
}

document.getElementById('btnEncodeText').addEventListener('click', () => textOp('encode'));
document.getElementById('btnDecodeText').addEventListener('click', () => textOp('decode'));
document.getElementById('btnSwapText').addEventListener('click', swapText);
document.getElementById('btnCopyText').addEventListener('click', copyText);
document.getElementById('btnEncodeFile').addEventListener('click', encodeFile);
document.getElementById('btnCopyFileEncode').addEventListener('click', copyFileEncode);
