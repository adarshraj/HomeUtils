async function doFormat() {
    const input = document.getElementById('fmtInput').value;
    const err = document.getElementById('fmtError');
    err.classList.add('d-none');
    const resp = await fetch('/utils/jsonformat/format', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({input})
    });
    const data = await resp.json();
    if (!resp.ok || data.error) { err.textContent = data.error; err.classList.remove('d-none'); return; }
    const isYaml = !input.trim().startsWith('{') && !input.trim().startsWith('[');
    document.getElementById('fmtLang').textContent = isYaml ? 'YAML' : 'JSON';
    document.getElementById('fmtOutput').value    = data.output;
    document.getElementById('fmtConverted').value = data.converted;
}
function clearFormat() {
    ['fmtInput','fmtOutput','fmtConverted'].forEach(id => document.getElementById(id).value = '');
    document.getElementById('fmtError').classList.add('d-none');
}
function copyFmt(which) {
    const id = which === 'output' ? 'fmtOutput' : 'fmtConverted';
    navigator.clipboard.writeText(document.getElementById(id).value);
}
function swapFmt() {
    const a = document.getElementById('fmtInput');
    const b = document.getElementById('fmtConverted');
    const tmp = a.value; a.value = b.value; b.value = tmp;
}
async function doDiff() {
    const left  = document.getElementById('diffLeft').value;
    const right = document.getElementById('diffRight').value;
    const err   = document.getElementById('diffError');
    err.classList.add('d-none');
    const resp = await fetch('/utils/jsonformat/diff', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({left, right})
    });
    const data = await resp.json();
    if (!resp.ok || data.error) { err.textContent = data.error; err.classList.remove('d-none'); return; }
    const res = document.getElementById('diffResult');
    res.innerHTML = data.html;
    res.classList.remove('d-none');
    document.getElementById('addedCount').textContent   = data.added;
    document.getElementById('removedCount').textContent = data.removed;
    document.getElementById('diffStats').classList.remove('d-none');
}

document.getElementById('btnFormat').addEventListener('click', doFormat);
document.getElementById('btnClearFormat').addEventListener('click', clearFormat);
document.getElementById('btnCopyOutput').addEventListener('click', () => copyFmt('output'));
document.getElementById('btnSwapFmt').addEventListener('click', swapFmt);
document.getElementById('btnCopyConverted').addEventListener('click', () => copyFmt('converted'));
document.getElementById('btnCompareJson').addEventListener('click', doDiff);
