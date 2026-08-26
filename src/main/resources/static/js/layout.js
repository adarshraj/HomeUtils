(function () {
    const LS = 'homeutils.sidebar.collapsed';
    if (localStorage.getItem(LS) === '1') document.body.classList.add('sb-collapsed');
    const collapse = document.getElementById('sbCollapse');
    if (collapse) collapse.addEventListener('click', () => {
        document.body.classList.toggle('sb-collapsed');
        localStorage.setItem(LS, document.body.classList.contains('sb-collapsed') ? '1' : '0');
    });
    const open = document.getElementById('sbOpen');
    const overlay = document.getElementById('sbOverlay');
    if (open) open.addEventListener('click', () => document.body.classList.add('sb-mobile-open'));
    if (overlay) overlay.addEventListener('click', () => document.body.classList.remove('sb-mobile-open'));
    // Close drawer when clicking a link on mobile
    document.querySelectorAll('.sidebar .sb-link').forEach(a => a.addEventListener('click', () => document.body.classList.remove('sb-mobile-open')));
})();

// Cursor-tracked glow on utility cards
document.addEventListener('mousemove', (e) => {
    document.querySelectorAll('.utility-card').forEach(card => {
        const r = card.getBoundingClientRect();
        if (e.clientX >= r.left && e.clientX <= r.right && e.clientY >= r.top && e.clientY <= r.bottom) {
            card.style.setProperty('--mx', ((e.clientX - r.left) / r.width * 100) + '%');
            card.style.setProperty('--my', ((e.clientY - r.top) / r.height * 100) + '%');
        }
    });
});
