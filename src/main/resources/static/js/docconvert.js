// Load .md file into textarea
document.getElementById('mdFile').addEventListener('change', function() {
    const file = this.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = e => document.getElementById('mdInput').value = e.target.result;
    reader.readAsText(file);
});

let lastHtml = '';

async function mdConvert(format) {
    const md = document.getElementById('mdInput').value.trim();
    const err = document.getElementById('mdError');
    err.classList.add('d-none');
    if (!md) { err.textContent = 'Please enter some Markdown.'; err.classList.remove('d-none'); return; }

    if (format === 'html') {
        const resp = await fetch('/utils/docconvert/md-to-html', {
            method: 'POST', headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({markdown: md})
        });
        const data = await resp.json();
        if (data.error) { err.textContent = data.error; err.classList.remove('d-none'); return; }
        lastHtml = data.html;
        document.getElementById('htmlRendered').innerHTML = data.html;
        document.getElementById('htmlRaw').textContent = data.html;
        document.getElementById('htmlPreview').classList.remove('d-none');
    } else {
        const resp = await fetch('/utils/docconvert/md-to-' + format, {
            method: 'POST', headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({markdown: md})
        });
        if (!resp.ok) { err.textContent = 'Conversion failed.'; err.classList.remove('d-none'); return; }
        const blob = await resp.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'document.' + format;
        a.click();
        URL.revokeObjectURL(url);
    }
}

function copyHtml() {
    navigator.clipboard.writeText(lastHtml);
}

async function docxConvert(format) {
    const fileInput = document.getElementById('docxFile');
    const err = document.getElementById('docxError');
    err.classList.add('d-none');
    if (!fileInput.files[0]) { err.textContent = 'Please select a .docx file.'; err.classList.remove('d-none'); return; }

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    if (format === 'md') {
        const resp = await fetch('/utils/docconvert/docx-to-md', { method: 'POST', body: formData });
        const data = await resp.json();
        if (data.error) { err.textContent = data.error; err.classList.remove('d-none'); return; }
        document.getElementById('mdResultText').value = data.markdown;
        document.getElementById('mdResult').classList.remove('d-none');
    } else {
        const resp = await fetch('/utils/docconvert/docx-to-pdf', { method: 'POST', body: formData });
        if (!resp.ok) { err.textContent = 'Conversion failed.'; err.classList.remove('d-none'); return; }
        const blob = await resp.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url; a.download = 'document.pdf'; a.click();
        URL.revokeObjectURL(url);
    }
}

function copyMdResult() {
    navigator.clipboard.writeText(document.getElementById('mdResultText').value);
}

function downloadMdResult() {
    const text = document.getElementById('mdResultText').value;
    const blob = new Blob([text], {type: 'text/markdown'});
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href = url; a.download = 'document.md'; a.click();
    URL.revokeObjectURL(url);
}

document.getElementById('btnPreviewHtml').addEventListener('click', () => mdConvert('html'));
document.getElementById('btnDownloadPdf').addEventListener('click', () => mdConvert('pdf'));
document.getElementById('btnDownloadDocx').addEventListener('click', () => mdConvert('docx'));
document.getElementById('btnCopyHtml').addEventListener('click', copyHtml);
document.getElementById('btnDocxToMd').addEventListener('click', () => docxConvert('md'));
document.getElementById('btnDocxToPdf').addEventListener('click', () => docxConvert('pdf'));
document.getElementById('btnCopyMdResult').addEventListener('click', copyMdResult);
document.getElementById('btnDownloadMdResult').addEventListener('click', downloadMdResult);
