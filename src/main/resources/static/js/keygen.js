function isSsh(type) { return type.startsWith('ssh-'); }

function onTypeChange() {
    const type = document.getElementById('keyType').value;
    document.getElementById('randomOptions').style.display = type === 'random' ? '' : 'none';
    document.getElementById('countSection').style.display = isSsh(type) ? 'none' : '';
}
onTypeChange();

async function generate() {
    const type = document.getElementById('keyType').value;
    const err = document.getElementById('errorArea');
    err.classList.add('d-none');
    document.getElementById('resultArea').classList.add('d-none');
    document.getElementById('sshResultArea').classList.add('d-none');

    if (isSsh(type)) {
        const algorithm = type.replace('ssh-', '');
        const resp = await fetch('/utils/keygen/generate-ssh', {
            method: 'POST', headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({algorithm})
        });
        const data = await resp.json();
        if (!resp.ok || data.error) { err.textContent = data.error; err.classList.remove('d-none'); return; }
        document.getElementById('sshPublicKey').value = data.publicKey;
        document.getElementById('sshPrivateKey').value = data.privateKey;
        document.getElementById('sshResultArea').classList.remove('d-none');
        return;
    }

    const count = parseInt(document.getElementById('keyCount').value) || 1;
    const encoding = document.getElementById('keyEncoding').value;
    const bytes = parseInt(document.getElementById('keyBytes').value);

    const resp = await fetch('/utils/keygen/generate', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({type, count: String(count), encoding, bytes: String(bytes)})
    });
    const data = await resp.json();
    if (!resp.ok || data.error) { err.textContent = data.error; err.classList.remove('d-none'); return; }

    const list = document.getElementById('keyList');
    list.innerHTML = '';
    data.keys.forEach((key) => {
        const row = document.createElement('div');
        row.className = 'input-group mb-2';
        row.innerHTML = `
            <input type="text" class="form-control font-monospace" value="${key}" readonly/>
            <button class="btn btn-outline-secondary copyOneBtn" title="Copy">
                <i class="bi bi-clipboard"></i>
            </button>`;
        list.appendChild(row);
    });
    document.getElementById('resultArea').classList.remove('d-none');
}

function copyOne(btn) {
    const input = btn.closest('.input-group').querySelector('input');
    navigator.clipboard.writeText(input.value);
    btn.innerHTML = '<i class="bi bi-check"></i>';
    setTimeout(() => btn.innerHTML = '<i class="bi bi-clipboard"></i>', 1500);
}

function copyAll() {
    const inputs = document.querySelectorAll('#keyList input');
    const text = Array.from(inputs).map(i => i.value).join('\n');
    navigator.clipboard.writeText(text);
}

function copySshField(id, btn) {
    navigator.clipboard.writeText(document.getElementById(id).value);
    const orig = btn.innerHTML;
    btn.innerHTML = '<i class="bi bi-check me-1"></i>Copied!';
    setTimeout(() => btn.innerHTML = orig, 1500);
}

async function quickGen(preset) {
    let body;
    if (preset === 'session') body = {type: 'random', count: '1', encoding: 'base64', bytes: '64'};
    else body = {type: preset, count: '1', encoding: 'base64', bytes: '32'};

    const resp = await fetch('/utils/keygen/generate', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(body)
    });
    const data = await resp.json();
    if (data.keys && data.keys.length) {
        const el = document.getElementById('quick-' + preset);
        el.textContent = data.keys[0];
        el.style.cursor = 'pointer';
        el.onclick = () => { navigator.clipboard.writeText(data.keys[0]); el.textContent = 'Copied!'; setTimeout(() => el.textContent = data.keys[0], 1500); };
    }
}

document.getElementById('keyType').addEventListener('change', onTypeChange);
document.getElementById('btnGenerate').addEventListener('click', generate);
document.getElementById('btnCopyAll').addEventListener('click', copyAll);
document.getElementById('keyList').addEventListener('click', (e) => {
    const btn = e.target.closest('.copyOneBtn');
    if (btn) copyOne(btn);
});
document.querySelectorAll('.copySshFieldBtn').forEach(btn => {
    btn.addEventListener('click', () => copySshField(btn.dataset.target, btn));
});
document.querySelectorAll('.quickGenBtn').forEach(btn => {
    btn.addEventListener('click', () => quickGen(btn.dataset.preset));
});
