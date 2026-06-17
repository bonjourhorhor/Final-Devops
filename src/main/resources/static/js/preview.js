/**
 * ID Card Manager – Live Preview & UI Interactions
 * Drives the real-time card preview on the profile-form page.
 */

/* ─── Live Preview ───────────────────────────────────────────────────────── */

/**
 * Called oninput from form fields; re-renders the preview card.
 */
function updatePreview() {
    const name       = _val('fullName')   || 'Your Full Name';
    const dept       = _val('department') || '';
    const title      = _val('title')      || '';
    const email      = _val('email')      || '';
    const phone      = _val('phone')      || '';
    const blood      = _val('bloodGroup') || '';
    const typeVal    = _val('type')       || 'EMPLOYEE';

    // Name & initials
    _set('prev-name',  name);
    _set('prev-initials', name.trim().charAt(0).toUpperCase() || '?');

    // Title
    const titleEl = document.getElementById('prev-title');
    if (titleEl) titleEl.textContent = title;

    // Fields
    _setField('prev-dept',  dept  ? 'DEPT: '  + dept  : '');
    _setField('prev-email', email ? 'EMAIL: ' + email : '');
    _setField('prev-phone', phone ? 'PHONE: ' + phone : '');
    _setField('prev-blood', blood ? 'BLOOD: ' + blood : '');

    // Type badge
    const typeBadge = document.getElementById('prev-type');
    if (typeBadge) typeBadge.textContent = (typeVal || 'EMPLOYEE') + ' ID CARD';

    // Registration placeholder
    const regEl = document.getElementById('prev-reg');
    if (regEl) {
        const year = new Date().getFullYear();
        const deptShort = dept ? dept.replace(/\s+/g, '').toUpperCase().substring(0, 6) : 'GEN';
        regEl.textContent = 'ID: ' + year + '-' + deptShort + '-' + '?????';
    }

    // Animate card
    const card = document.getElementById('live-card');
    if (card) {
        card.style.transition = 'transform .15s ease';
        card.style.transform  = 'scale(0.98)';
        setTimeout(() => { card.style.transform = ''; }, 150);
    }
}

/* ─── Photo Upload Handling ──────────────────────────────────────────────── */

function handlePhotoChange(input) {
    if (!input.files || !input.files[0]) return;
    const file = input.files[0];

    // Validate client-side
    const allowed = ['image/jpeg', 'image/png'];
    if (!allowed.includes(file.type)) {
        showToast('Only JPEG and PNG images are allowed.', 'error');
        input.value = '';
        return;
    }
    if (file.size > 5 * 1024 * 1024) {
        showToast('File exceeds the 5 MB limit.', 'error');
        input.value = '';
        return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
        const dataUrl = e.target.result;

        // Show in upload zone
        const previewImg = document.getElementById('photo-preview-img');
        const placeholder = document.getElementById('upload-placeholder');
        if (previewImg) {
            previewImg.src = dataUrl;
            previewImg.style.display = 'block';
        }
        if (placeholder) placeholder.style.display = 'none';

        // Show in live card preview
        const prevPhoto       = document.getElementById('prev-photo');
        const prevPlaceholder = document.getElementById('prev-photo-placeholder');
        if (prevPhoto) {
            prevPhoto.src = dataUrl;
            prevPhoto.style.display = 'block';
        }
        if (prevPlaceholder) prevPlaceholder.style.display = 'none';
    };
    reader.readAsDataURL(file);
}

/* ─── Drag & Drop on photo zone ─────────────────────────────────────────── */

document.addEventListener('DOMContentLoaded', () => {
    const zone  = document.getElementById('photo-zone');
    const input = document.getElementById('photo');
    if (!zone || !input) return;

    zone.addEventListener('dragover', (e) => {
        e.preventDefault();
        zone.classList.add('drag-over');
    });
    zone.addEventListener('dragleave', () => zone.classList.remove('drag-over'));
    zone.addEventListener('drop', (e) => {
        e.preventDefault();
        zone.classList.remove('drag-over');
        const files = e.dataTransfer.files;
        if (files.length) {
            input.files = files;
            handlePhotoChange(input);
        }
    });

    // Initial render
    updatePreview();
});

/* ─── Toast notification ─────────────────────────────────────────────────── */

function showToast(msg, type = 'info') {
    const toast = document.createElement('div');
    toast.textContent = msg;
    toast.style.cssText = `
        position: fixed; bottom: 5rem; right: 1.5rem; z-index: 9999;
        background: ${type === 'error' ? '#ef4444' : '#6366f1'};
        color: #fff; padding: .75rem 1.25rem; border-radius: 8px;
        font-size: .875rem; font-family: Inter,sans-serif;
        box-shadow: 0 4px 20px rgba(0,0,0,.4);
        animation: slideIn .3s ease;
    `;
    const style = document.createElement('style');
    style.textContent = `@keyframes slideIn { from { opacity:0; transform:translateY(20px); } to { opacity:1; transform:translateY(0); } }`;
    document.head.appendChild(style);
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
}

/* ─── Auto-dismiss alerts ─────────────────────────────────────────────────── */

document.addEventListener('DOMContentLoaded', () => {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity .5s ease';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });
});

/* ─── Helpers ─────────────────────────────────────────────────────────────── */

function _val(id) {
    const el = document.getElementById(id);
    return el ? el.value.trim() : '';
}

function _set(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text;
}

function _setField(id, text) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = text;
    el.style.display = text ? 'block' : 'none';
}
