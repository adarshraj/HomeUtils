async function run(mode) {
    const input = document.getElementById('input').value;
    const err = document.getElementById('errorArea'); err.classList.add('d-none');
    const resp = await fetch('/utils/urlcodec/' + mode, {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({input})
    });
    const data = await resp.json();
    if (!resp.ok || data.error) { err.textContent = data.error || 'Error'; err.classList.remove('d-none'); return; }
    document.getElementById('output').value = data.result;
}
function copyOut() { navigator.clipboard.writeText(document.getElementById('output').value); }

document.getElementById('btnEncode').addEventListener('click', () => run('encode'));
document.getElementById('btnDecode').addEventListener('click', () => run('decode'));
document.getElementById('btnCopyOut').addEventListener('click', copyOut);
