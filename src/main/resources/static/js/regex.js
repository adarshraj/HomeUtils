function escapeHtml(s) { return s.replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
function highlight(input, matches) {
    if (matches.length === 0) return escapeHtml(input);
    let out = ''; let cursor = 0;
    matches.forEach(m => {
        out += escapeHtml(input.slice(cursor, m.start));
        out += '<mark class="bg-warning">' + escapeHtml(input.slice(m.start, m.end)) + '</mark>';
        cursor = m.end;
    });
    out += escapeHtml(input.slice(cursor));
    return out;
}
async function run() {
    const err = document.getElementById('errorArea'); err.classList.add('d-none');
    const res = document.getElementById('resultArea'); res.classList.add('d-none');
    const flags = ['i','m','s','x','u'].filter(f => document.getElementById('flag-' + f).checked);
    const replacement = document.getElementById('replacement').value;
    const body = {
        pattern: document.getElementById('pattern').value,
        input: document.getElementById('input').value,
        flags,
        ...(replacement ? { replacement } : {})
    };
    const resp = await fetch('/utils/regex/test', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(body)
    });
    const data = await resp.json();
    if (!resp.ok || data.error) { err.textContent = data.error || 'Error'; err.classList.remove('d-none'); return; }
    document.getElementById('count').textContent = data.count;
    document.getElementById('highlighted').innerHTML = highlight(body.input, data.matches);
    const list = document.getElementById('matchList'); list.innerHTML = '';
    data.matches.forEach((m, i) => {
        const groups = m.groups.length ? '<div class="small text-muted">Groups: ' + m.groups.map(g => '<code>' + escapeHtml(g ?? 'null') + '</code>').join(', ') + '</div>' : '';
        list.insertAdjacentHTML('beforeend',
            `<div class="border rounded p-2 mb-1 small">
                <strong>#${i+1}</strong> <code>${escapeHtml(m.match)}</code>
                <span class="text-muted">[${m.start}–${m.end}]</span>
                ${groups}
            </div>`);
    });
    const ra = document.getElementById('replacedArea');
    if ('replaced' in data) {
        document.getElementById('replaced').value = data.replaced;
        ra.classList.remove('d-none');
    } else ra.classList.add('d-none');
    res.classList.remove('d-none');
}
document.getElementById('btnRun').addEventListener('click', run);
