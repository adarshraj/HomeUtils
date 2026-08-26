function hexToRgb(h) {
    let s = h.replace('#', '').trim();
    if (s.length === 3) s = s.split('').map(c => c + c).join('');
    if (!/^[0-9a-f]{6}$/i.test(s)) return null;
    return { r: parseInt(s.slice(0,2),16), g: parseInt(s.slice(2,4),16), b: parseInt(s.slice(4,6),16) };
}
function rgbToHex({r,g,b}) { return '#' + [r,g,b].map(v => v.toString(16).padStart(2,'0')).join(''); }
function rgbToHsl({r,g,b}) {
    r/=255; g/=255; b/=255;
    const max = Math.max(r,g,b), min = Math.min(r,g,b);
    let h, s, l = (max+min)/2;
    if (max === min) { h = s = 0; }
    else {
        const d = max-min;
        s = l > 0.5 ? d/(2-max-min) : d/(max+min);
        switch(max) {
            case r: h = (g-b)/d + (g<b ? 6 : 0); break;
            case g: h = (b-r)/d + 2; break;
            case b: h = (r-g)/d + 4; break;
        }
        h *= 60;
    }
    return { h: Math.round(h), s: Math.round(s*100), l: Math.round(l*100) };
}
function relLum({r,g,b}) {
    const f = v => { v /= 255; return v <= 0.03928 ? v/12.92 : Math.pow((v+0.055)/1.055, 2.4); };
    return 0.2126*f(r) + 0.7152*f(g) + 0.0722*f(b);
}
function contrast(a, b) {
    const l1 = relLum(a), l2 = relLum(b);
    return (Math.max(l1,l2) + 0.05) / (Math.min(l1,l2) + 0.05);
}
function sync(from) {
    let rgb = hexToRgb(document.getElementById('hex').value);
    if (from === 'picker') rgb = hexToRgb(document.getElementById('picker').value);
    if (!rgb) return;
    const hex = rgbToHex(rgb);
    const hsl = rgbToHsl(rgb);
    document.getElementById('hex').value = hex;
    document.getElementById('picker').value = hex;
    document.getElementById('rgb').value = `rgb(${rgb.r}, ${rgb.g}, ${rgb.b})`;
    document.getElementById('hsl').value = `hsl(${hsl.h}, ${hsl.s}%, ${hsl.l}%)`;
    document.getElementById('swatch').style.background = hex;
}
document.getElementById('picker').addEventListener('input', () => sync('picker'));
document.getElementById('hex').addEventListener('input', () => sync('hex'));

function updateContrast() {
    const fg = hexToRgb(document.getElementById('fg').value);
    const bg = hexToRgb(document.getElementById('bg').value);
    if (!fg || !bg) return;
    const r = contrast(fg, bg);
    document.getElementById('ratio').textContent = r.toFixed(2);
    const mark = (ok) => ok ? '<span class="text-success">✓ Pass</span>' : '<span class="text-danger">✗ Fail</span>';
    document.getElementById('aaNormal').innerHTML = mark(r >= 4.5);
    document.getElementById('aaLarge').innerHTML  = mark(r >= 3);
    document.getElementById('aaaNormal').innerHTML = mark(r >= 7);
    const p = document.getElementById('preview');
    p.style.color = document.getElementById('fg').value;
    p.style.background = document.getElementById('bg').value;
}
['fg','bg'].forEach(id => document.getElementById(id).addEventListener('input', updateContrast));
updateContrast();
