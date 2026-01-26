// Check if user is logged in
function checkAuth() {
    const user = localStorage.getItem('currentUser');
    return user ? JSON.parse(user) : null;
}

// Render navigation
function renderNavigation() {
    const navbarNav = document.querySelector('#navbarNav');
    if (!navbarNav) return;

    const user = checkAuth();
    const currentPage = window.location.pathname.split('/').pop() || 'index.html';

    if (user) {
        // Logged-in navigation
        navbarNav.innerHTML = `
            <ul class="navbar-nav ms-auto">
                <li class="nav-item">
                    <a class="nav-link ${currentPage === 'index.html' ? 'active' : ''}" href="index.html">🏠 Home</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${currentPage === 'auctions.html' ? 'active' : ''}" href="auctions.html">🏷️ Auctions</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${currentPage === 'create-auction.html' ? 'active' : ''}" href="create-auction.html">➕ Create Auction</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${currentPage === 'profile.html' ? 'active' : ''}" href="profile.html">👤 Profile</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${currentPage === 'myactivity.html' ? 'active' : ''}" href="myactivity.html">📊 My Activity</a>
                </li>
                <li class="nav-item">
                    <span class="nav-link text-light">👋 ${user.name}</span>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#" onclick="logout()">🚪 Logout</a>
                </li>
            </ul>
        `;
    } else {
        // Guest navigation
        navbarNav.innerHTML = `
            <ul class="navbar-nav ms-auto">
                <li class="nav-item">
                    <a class="nav-link ${currentPage === 'index.html' ? 'active' : ''}" href="index.html">🏠 Home</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${currentPage === 'auctions.html' ? 'active' : ''}" href="auctions.html">🏷️ Auctions</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${currentPage === 'login.html' ? 'active' : ''}" href="login.html">🔐 Login</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${currentPage === 'register.html' ? 'active' : ''}" href="register.html">📝 Register</a>
                </li>
            </ul>
        `;
    }
}

// Logout function
function logout() {
    if (confirm('Are you sure you want to logout?')) {
        localStorage.clear();
        window.location.href = 'login.html';
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', renderNavigation);
