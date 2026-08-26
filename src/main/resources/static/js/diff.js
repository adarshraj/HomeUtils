function esc(s) { return (s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
async function run() {
    const left = document.getElementById('left').value;
    const right = document.getElementById('right').value;
    const resp = await fetch('/utils/diff/compare', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({left, right})
    });
    const data = await resp.json();
    document.getElementById('added').textContent = data.added;
    document.getElementById('removed').textContent = data.removed;
    document.getElementById('changed').textContent = data.changed;

    const leftLines = data.leftLines;
    const deltas = data.deltas;
    let out = '';
    let cursor = 0;
    deltas.forEach(d => {
        for (let i = cursor; i < d.sourceStart; i++) out += '  ' + esc(leftLines[i]) + '\n';
        d.sourceLines.forEach(line => out += '<span class="bg-danger bg-opacity-25">- ' + esc(line) + '</span>\n');
        d.targetLines.forEach(line => out += '<span class="bg-success bg-opacity-25">+ ' + esc(line) + '</span>\n');
        cursor = d.sourceStart + d.sourceLines.length;
    });
    for (let i = cursor; i < leftLines.length; i++) out += '  ' + esc(leftLines[i]) + '\n';
    document.getElementById('diffView').innerHTML = out || '<em class="text-muted">No differences.</em>';
    document.getElementById('resultArea').classList.remove('d-none');
}
document.getElementById('btnCompare').addEventListener('click', run);
