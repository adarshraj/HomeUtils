function setExpr(v) { document.getElementById('cronExpr').value = v; describe(); }
async function describe() {
    const expression = document.getElementById('cronExpr').value.trim();
    const type  = document.getElementById('cronType').value;
    const count = document.getElementById('cronCount').value;
    const err = document.getElementById('errorArea');
    const res = document.getElementById('resultArea');
    err.classList.add('d-none'); res.classList.add('d-none');

    const resp = await fetch('/utils/cron/describe', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({expression, type, count})
    });
    const data = await resp.json();
    if (!resp.ok || data.error) { err.textContent = data.error; err.classList.remove('d-none'); return; }
    document.getElementById('description').textContent = data.description;
    const ul = document.getElementById('nextRuns');
    ul.innerHTML = data.nextRuns.map((r, i) =>
        `<li class="list-group-item d-flex justify-content-between align-items-center">
            <span>${r}</span>
            <span class="badge bg-secondary rounded-pill">#${i+1}</span>
        </li>`).join('');
    res.classList.remove('d-none');
}
document.getElementById('btnDescribe').addEventListener('click', describe);
document.querySelectorAll('.quickExpr').forEach(b => b.addEventListener('click', () => setExpr(b.dataset.expr)));
describe();
