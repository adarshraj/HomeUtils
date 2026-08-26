document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => {
        try { new bootstrap.Tooltip(el, { delay: { show: 250, hide: 50 }, container: 'body' }); } catch (e) { console.error(e); }
    });
});
(function () {
    const input = document.getElementById('utilSearch');
    const tiles = document.querySelectorAll('#utilGrid > .util-tile');
    const countEl = document.getElementById('visibleCount');
    const empty = document.getElementById('emptyState');
    input.addEventListener('input', () => {
        const q = input.value.trim().toLowerCase();
        let visible = 0;
        tiles.forEach(t => {
            const haystack = t.dataset.search || '';
            const match = !q || haystack.includes(q);
            t.style.display = match ? '' : 'none';
            if (match) visible++;
        });
        countEl.textContent = visible;
        empty.style.display = visible === 0 ? '' : 'none';
    });
})();
