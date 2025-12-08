// Employee Management JavaScript
document.addEventListener('DOMContentLoaded', function() {

    // ===== TABLE FILTERING =====
    const employeesTable = document.getElementById('employeesTable');
    if (employeesTable) {
        initTableFiltering();
    }

    // ===== FORM VALIDATION =====
    const forms = document.querySelectorAll('.needs-validation');
    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });

    // ===== AUTO-GENERATE MATRICULE =====
    const roleSelect = document.getElementById('role');
    const matriculeInput = document.getElementById('matricule');

    if (roleSelect && matriculeInput) {
        roleSelect.addEventListener('change', function() {
            if (!matriculeInput.value) {
                generateMatricule(this.value);
            }
        });
    }

    // ===== EXPORT FUNCTIONALITY =====
    const exportBtn = document.getElementById('exportBtn');
    if (exportBtn) {
        exportBtn.addEventListener('click', exportEmployees);
    }

    // ===== CONFIRMATION DIALOGS =====
    initConfirmationDialogs();

    // ===== REAL-TIME SEARCH =====
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(filterTable, 300));
    }

    // ===== INIT TOOLTIPS =====
    initTooltips();

    // ===== SIDEBAR TOGGLE FOR MOBILE =====
    initMobileSidebar();
});

// ===== FUNCTIONS =====

function initTableFiltering() {
    const table = document.getElementById('employeesTable');
    const rows = table.getElementsByTagName('tbody')[0].getElementsByTagName('tr');
    const searchInput = document.getElementById('searchInput');
    const deptFilter = document.getElementById('departementFilter');
    const roleFilter = document.getElementById('roleFilter');
    const statusFilter = document.getElementById('statusFilter');

    function filterTable() {
        const searchTerm = searchInput ? searchInput.value.toLowerCase() : '';
        const deptValue = deptFilter ? deptFilter.value : '';
        const roleValue = roleFilter ? roleFilter.value : '';
        const statusValue = statusFilter ? statusFilter.value : '';

        for (let row of rows) {
            const cells = row.getElementsByTagName('td');
            let showRow = true;

            // Search filter
            if (searchTerm) {
                let found = false;
                for (let cell of cells) {
                    if (cell.textContent.toLowerCase().includes(searchTerm)) {
                        found = true;
                        break;
                    }
                }
                if (!found) showRow = false;
            }

            // Department filter
            if (deptValue && cells[4]) {
                const deptCell = cells[4];
                const deptId = deptCell.getAttribute('data-dept-id');
                if (deptId && deptId !== deptValue) {
                    showRow = false;
                }
            }

            // Role filter
            if (roleValue && cells[6]) {
                const roleCell = cells[6].querySelector('.badge');
                // Comparez avec la valeur du badge (le texte)
                if (roleCell && roleCell.textContent.trim() !== roleValue) {
                    showRow = false;
                }
            }

            // Status filter
            if (statusValue && cells[7]) {
                const statusCell = cells[7].querySelector('.badge');
                if (statusCell) {
                    if (statusValue === 'active' && !statusCell.textContent.includes('Actif')) {
                        showRow = false;
                    }
                    if (statusValue === 'inactive' && !statusCell.textContent.includes('Inactif')) {
                        showRow = false;
                    }
                }
            }

            row.style.display = showRow ? '' : 'none';
        }

        updateRowCount();
    }

    if (searchInput) searchInput.addEventListener('keyup', filterTable);
    if (deptFilter) deptFilter.addEventListener('change', filterTable);
    if (roleFilter) roleFilter.addEventListener('change', filterTable);
    if (statusFilter) statusFilter.addEventListener('change', filterTable);

    filterTable(); // Initial filter
}

function generateMatricule(role) {
    const matriculeInput = document.getElementById('matricule');
    if (!matriculeInput || matriculeInput.value) return;

    let prefix = 'EMP';
    if (role.includes('ADMIN')) prefix = 'ADM';
    else if (role === 'RH') prefix = 'RH';
    else if (role === 'MANAGER') prefix = 'MGR';

    // In a real application, you would fetch the next number from the server
    const nextNumber = Math.floor(Math.random() * 9999).toString().padStart(4, '0');
    matriculeInput.value = prefix + nextNumber;
}

function exportEmployees() {
    // Show loading
    showLoading();

    // In a real application, this would make an API call to export data
    setTimeout(() => {
        hideLoading();

        // Create a simple CSV export
        const table = document.getElementById('employeesTable');
        const rows = table.querySelectorAll('tbody tr:not([style*="display: none"])');
        const headers = table.querySelectorAll('thead th');

        let csv = [];

        // Add headers
        let headerRow = [];
        headers.forEach(header => {
            if (!header.querySelector('.actions-header')) {
                headerRow.push(`"${header.textContent.trim()}"`);
            }
        });
        csv.push(headerRow.join(','));

        // Add data rows
        rows.forEach(row => {
            const cells = row.querySelectorAll('td');
            let dataRow = [];

            cells.forEach((cell, index) => {
                if (index !== cells.length - 1) { // Skip actions column
                    let text = cell.textContent.trim();
                    // Remove badge content if present
                    const badge = cell.querySelector('.badge');
                    if (badge) {
                        text = badge.textContent.trim();
                    }
                    dataRow.push(`"${text}"`);
                }
            });

            csv.push(dataRow.join(','));
        });

        // Download CSV
        const csvContent = csv.join('\n');
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);

        link.setAttribute('href', url);
        link.setAttribute('download', `employees_${new Date().toISOString().split('T')[0]}.csv`);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

        // Show success message
        showToast('Export réussi', 'Les données ont été exportées avec succès.', 'success');
    }, 1500);
}

function initConfirmationDialogs() {
    const deleteForms = document.querySelectorAll('form[onsubmit*="confirm"]');
    deleteForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const message = this.getAttribute('data-confirm') ||
                'Êtes-vous sûr de vouloir effectuer cette action ?';

            if (!confirm(message)) {
                e.preventDefault();
                return false;
            }
        });
    });
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function initTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

function initMobileSidebar() {
    const menuToggle = document.querySelector('.menu-toggle');
    const sidebar = document.querySelector('.sidebar');

    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', function(e) {
            e.preventDefault();
            sidebar.classList.toggle('active');

            // Add overlay
            if (sidebar.classList.contains('active')) {
                const overlay = document.createElement('div');
                overlay.className = 'sidebar-overlay';
                overlay.style.cssText = `
                    position: fixed;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: rgba(0,0,0,0.5);
                    z-index: 999;
                `;
                overlay.addEventListener('click', function() {
                    sidebar.classList.remove('active');
                    this.remove();
                });
                document.body.appendChild(overlay);
            } else {
                const overlay = document.querySelector('.sidebar-overlay');
                if (overlay) overlay.remove();
            }
        });
    }
}

function updateRowCount() {
    const table = document.getElementById('employeesTable');
    if (!table) return;

    const visibleRows = table.querySelectorAll('tbody tr:not([style*="display: none"])').length;
    const totalRows = table.querySelectorAll('tbody tr').length;

    const countElement = document.getElementById('rowCount');
    if (countElement) {
        countElement.textContent = `${visibleRows} sur ${totalRows} employés`;
    }
}

function showLoading() {
    const overlay = document.createElement('div');
    overlay.className = 'spinner-overlay';
    overlay.innerHTML = '<div class="spinner"></div>';
    document.body.appendChild(overlay);
}

function hideLoading() {
    const overlay = document.querySelector('.spinner-overlay');
    if (overlay) overlay.remove();
}

function showToast(title, message, type = 'info') {
    // Create toast container if not exists
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            min-width: 300px;
        `;
        document.body.appendChild(container);
    }

    // Create toast
    const toastId = 'toast-' + Date.now();
    const toast = document.createElement('div');
    toast.id = toastId;
    toast.className = `toast show`;
    toast.style.cssText = `
        margin-bottom: 10px;
        border: none;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    `;

    const bgColor = type === 'success' ? '#d1e7dd' :
        type === 'error' ? '#f8d7da' :
            type === 'warning' ? '#fff3cd' : '#cfe2ff';

    const borderColor = type === 'success' ? '#badbcc' :
        type === 'error' ? '#f5c2c7' :
            type === 'warning' ? '#ffecb5' : '#b6d4fe';

    const textColor = type === 'success' ? '#0f5132' :
        type === 'error' ? '#842029' :
            type === 'warning' ? '#664d03' : '#084298';

    toast.innerHTML = `
        <div class="toast-header" style="background-color: ${bgColor}; border-bottom-color: ${borderColor}; color: ${textColor};">
            <strong class="me-auto">${title}</strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast"></button>
        </div>
        <div class="toast-body" style="background-color: ${bgColor}; color: ${textColor};">
            ${message}
        </div>
    `;

    container.appendChild(toast);

    // Auto remove after 5 seconds
    setTimeout(() => {
        const toastElement = document.getElementById(toastId);
        if (toastElement) {
            toastElement.remove();
        }
    }, 5000);
}

// Keyboard shortcuts
document.addEventListener('keydown', function(e) {
    // Ctrl + F for search
    if (e.ctrlKey && e.key === 'f') {
        e.preventDefault();
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.focus();
        }
    }

    // Ctrl + N for new employee
    if (e.ctrlKey && e.key === 'n') {
        e.preventDefault();
        const newBtn = document.querySelector('a[href*="/admin/employes/creer"]');
        if (newBtn) {
            window.location.href = newBtn.href;
        }
    }

    // Escape to clear search
    if (e.key === 'Escape') {
        const searchInput = document.getElementById('searchInput');
        if (searchInput && document.activeElement === searchInput) {
            searchInput.value = '';
            filterTable();
        }
    }
});