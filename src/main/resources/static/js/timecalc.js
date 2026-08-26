// ---- tabs ----
document.querySelectorAll('.tc-tab').forEach(t => t.addEventListener('click', () => {
    document.querySelectorAll('.tc-tab').forEach(x => x.classList.remove('active'));
    document.querySelectorAll('.tc-panel').forEach(x => x.classList.remove('active'));
    t.classList.add('active');
    document.getElementById('panel-' + t.dataset.panel).classList.add('active');
}));
// op switches
document.querySelectorAll('[data-op]').forEach(b => b.addEventListener('click', () => {
    b.parentElement.querySelectorAll('button').forEach(x => x.classList.remove('active'));
    b.classList.add('active');
}));
document.querySelectorAll('[data-op2]').forEach(b => b.addEventListener('click', () => {
    b.parentElement.querySelectorAll('button').forEach(x => x.classList.remove('active'));
    b.classList.add('active');
}));

// ---- helpers ----
function pad(n, w=2) { return String(Math.abs(n)).padStart(w,'0'); }
function localNowString() {
    const d = new Date();
    const p = (n) => String(n).padStart(2,'0');
    return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
}
function setNow(id) { document.getElementById(id).value = localNowString(); }

function fmtDateTime(d) {
    const p = (n) => String(n).padStart(2,'0');
    return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
}

function humanDuration(ms) {
    const neg = ms < 0; ms = Math.abs(ms);
    const totalSec = Math.floor(ms/1000);
    const d = Math.floor(totalSec / 86400);
    const h = Math.floor((totalSec % 86400) / 3600);
    const m = Math.floor((totalSec % 3600) / 60);
    const s = totalSec % 60;
    let parts = [];
    if (d) parts.push(d + 'd');
    if (h) parts.push(h + 'h');
    if (m) parts.push(m + 'm');
    if (s || !parts.length) parts.push(s + 's');
    return (neg ? '-' : '') + parts.join(' ');
}

// Break "start → end" into calendar parts (Y,M,W,D...) like date-fns formatDistance
function calendarBreakdown(start, end) {
    let neg = false;
    if (end < start) { [start, end] = [end, start]; neg = true; }
    let y = end.getFullYear() - start.getFullYear();
    let mo = end.getMonth() - start.getMonth();
    let d = end.getDate() - start.getDate();
    let h = end.getHours() - start.getHours();
    let mi = end.getMinutes() - start.getMinutes();
    let s = end.getSeconds() - start.getSeconds();
    if (s < 0) { s += 60; mi--; }
    if (mi < 0) { mi += 60; h--; }
    if (h < 0) { h += 24; d--; }
    if (d < 0) {
        const prev = new Date(end.getFullYear(), end.getMonth(), 0).getDate();
        d += prev; mo--;
    }
    if (mo < 0) { mo += 12; y--; }
    return { sign: neg ? -1 : 1, y, mo, d, h, mi, s };
}

// ---- Panel 1: Date ± Duration ----
function calcAdd() {
    const err = document.getElementById('addErr'); err.textContent = '';
    try {
        const startStr = document.getElementById('a_start').value;
        if (!startStr) throw new Error('Enter a start date/time.');
        const start = new Date(startStr);
        if (isNaN(start)) throw new Error('Invalid start date/time.');
        const op = document.querySelector('[data-op].active').dataset.op === '-' ? -1 : 1;
        const Y = +document.getElementById('a_Y').value || 0;
        const M = +document.getElementById('a_M').value || 0;
        const W = +document.getElementById('a_W').value || 0;
        const D = +document.getElementById('a_D').value || 0;
        const h = +document.getElementById('a_h').value || 0;
        const m = +document.getElementById('a_m').value || 0;
        const s = +document.getElementById('a_s').value || 0;

        const r = new Date(start);
        r.setFullYear(r.getFullYear() + op*Y);
        r.setMonth(r.getMonth() + op*M);
        r.setDate(r.getDate() + op*(W*7 + D));
        r.setHours(r.getHours() + op*h);
        r.setMinutes(r.getMinutes() + op*m);
        r.setSeconds(r.getSeconds() + op*s);

        document.getElementById('addBig').textContent = fmtDateTime(r);
        const diffMs = r - start;
        const detail = document.getElementById('addDetail');
        detail.innerHTML = `
            <div class="item"><div class="k">ISO 8601</div><div class="v">${r.toISOString()}</div></div>
            <div class="item"><div class="k">Weekday</div><div class="v">${r.toLocaleDateString(undefined,{weekday:'long'})}</div></div>
            <div class="item"><div class="k">Epoch (s)</div><div class="v">${Math.floor(r.getTime()/1000)}</div></div>
            <div class="item"><div class="k">Net change</div><div class="v">${humanDuration(diffMs)}</div></div>
        `;
        document.getElementById('addResult').classList.remove('d-none');
    } catch (e) { err.textContent = e.message; document.getElementById('addResult').classList.add('d-none'); }
}

// ---- Panel 2: Date - Date ----
function calcDiff() {
    const err = document.getElementById('diffErr'); err.textContent = '';
    try {
        const a = document.getElementById('d_start').value;
        const b = document.getElementById('d_end').value;
        if (!a || !b) throw new Error('Enter both start and end.');
        const start = new Date(a), end = new Date(b);
        if (isNaN(start) || isNaN(end)) throw new Error('Invalid date/time.');
        const ms = end - start;
        const c = calendarBreakdown(start, end);
        const parts = [];
        if (c.y)  parts.push(c.y  + 'y');
        if (c.mo) parts.push(c.mo + 'mo');
        if (c.d)  parts.push(c.d  + 'd');
        if (c.h)  parts.push(c.h  + 'h');
        if (c.mi) parts.push(c.mi + 'm');
        if (c.s || !parts.length) parts.push(c.s + 's');
        document.getElementById('diffBig').textContent = (c.sign<0?'-':'') + parts.join(' ');

        const absSec = Math.abs(Math.floor(ms/1000));
        document.getElementById('diffDetail').innerHTML = `
            <div class="item"><div class="k">Total seconds</div><div class="v">${absSec.toLocaleString()}</div></div>
            <div class="item"><div class="k">Total minutes</div><div class="v">${(absSec/60).toLocaleString(undefined,{maximumFractionDigits:2})}</div></div>
            <div class="item"><div class="k">Total hours</div><div class="v">${(absSec/3600).toLocaleString(undefined,{maximumFractionDigits:3})}</div></div>
            <div class="item"><div class="k">Total days</div><div class="v">${(absSec/86400).toLocaleString(undefined,{maximumFractionDigits:4})}</div></div>
            <div class="item"><div class="k">Total weeks</div><div class="v">${(absSec/604800).toLocaleString(undefined,{maximumFractionDigits:4})}</div></div>
            <div class="item"><div class="k">HH:MM:SS</div><div class="v">${(c.sign<0?'-':'')}${pad(Math.floor(absSec/3600))}:${pad(Math.floor((absSec%3600)/60))}:${pad(absSec%60)}</div></div>
        `;
        document.getElementById('diffResult').classList.remove('d-none');
    } catch (e) { err.textContent = e.message; document.getElementById('diffResult').classList.add('d-none'); }
}

// ---- Panel 3: Duration ± Duration ----
function durToSec(D, h, m, s) { return D*86400 + h*3600 + m*60 + s; }
function calcDur() {
    const err = document.getElementById('durErr'); err.textContent = '';
    try {
        const op = document.querySelector('[data-op2].active').dataset.op2 === '-' ? -1 : 1;
        const a = durToSec(+document.getElementById('x_D').value||0, +document.getElementById('x_h').value||0, +document.getElementById('x_m').value||0, +document.getElementById('x_s').value||0);
        const b = durToSec(+document.getElementById('y_D').value||0, +document.getElementById('y_h').value||0, +document.getElementById('y_m').value||0, +document.getElementById('y_s').value||0);
        const total = a + op*b;
        const abs = Math.abs(total);
        const d = Math.floor(abs/86400);
        const h = Math.floor((abs%86400)/3600);
        const m = Math.floor((abs%3600)/60);
        const s = abs%60;
        const sign = total<0 ? '-' : '';
        document.getElementById('durBig').textContent = `${sign}${d}d ${h}h ${m}m ${s}s`;
        document.getElementById('durDetail').innerHTML = `
            <div class="item"><div class="k">HH:MM:SS</div><div class="v">${sign}${pad(Math.floor(abs/3600))}:${pad(Math.floor((abs%3600)/60))}:${pad(abs%60)}</div></div>
            <div class="item"><div class="k">Total seconds</div><div class="v">${total.toLocaleString()}</div></div>
            <div class="item"><div class="k">Total minutes</div><div class="v">${(total/60).toLocaleString(undefined,{maximumFractionDigits:3})}</div></div>
            <div class="item"><div class="k">Total hours</div><div class="v">${(total/3600).toLocaleString(undefined,{maximumFractionDigits:4})}</div></div>
            <div class="item"><div class="k">ISO 8601</div><div class="v">${(total<0?'-':'')}P${d?d+'D':''}${(h||m||s)?'T':''}${h?h+'H':''}${m?m+'M':''}${s?s+'S':''}</div></div>
        `;
        document.getElementById('durResult').classList.remove('d-none');
    } catch (e) { err.textContent = e.message; document.getElementById('durResult').classList.add('d-none'); }
}

document.getElementById('btnCalcAdd').addEventListener('click', calcAdd);
document.getElementById('btnNowAStart').addEventListener('click', () => setNow('a_start'));
document.getElementById('btnCalcDiff').addEventListener('click', calcDiff);
document.getElementById('btnNowDStart').addEventListener('click', () => setNow('d_start'));
document.getElementById('btnNowDEnd').addEventListener('click', () => setNow('d_end'));
document.getElementById('btnCalcDur').addEventListener('click', calcDur);

// default values
setNow('a_start');
setNow('d_start');
setNow('d_end');
