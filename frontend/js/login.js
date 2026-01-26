// API Base URL
const API_BASE_URL = 'https://campus-auction-production.up.railway.app/api';

document.addEventListener('DOMContentLoaded', () => {
    console.log('✅ Login page loaded');
    console.log('📡 API URL:', API_BASE_URL);
    
    const loginForm = document.getElementById('loginForm');
    const errorMessage = document.getElementById('errorMessage');
    
    if (!loginForm) {
        console.error('❌ Login form not found!');
        return;
    }
    
    console.log('✅ Form elements found');
    
    // Handle form submission
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        console.log('🚀 Form submitted');
        
        // Reset message styling
        errorMessage.textContent = '';
        errorMessage.style.color = '#e74c3c'; // Default red for errors
        errorMessage.style.fontWeight = '600';
        
        // Get form values
        const emailInput = document.getElementById('email');
        const passwordInput = document.getElementById('password');
        const email = emailInput.value.trim();
        const password = passwordInput.value;
        
        // ===== CLIENT-SIDE VALIDATIONS =====
        
        // 1. Check if fields are empty
        if (!email || !password) {
            errorMessage.textContent = '❌ Please enter both email and password';
            return;
        }
        
        // 2. Validate email format
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            errorMessage.textContent = '❌ Please enter a valid email address';
            return;
        }
        
        // 3. Validate KIIT email format (optional - if you want to enforce)
        if (!email.endsWith('@kiit.ac.in')) {
            errorMessage.textContent = '❌ Please use your KIIT email (@kiit.ac.in)';
            return;
        }
        
        // 4. Check password length
        if (password.length < 4) {
            errorMessage.textContent = '❌ Password must be at least 4 characters long';
            return;
        }
        
        // ===== API CALL =====
        
        try {
            console.log('📡 Calling API:', `${API_BASE_URL}/users/login`);
            
            const response = await fetch(`${API_BASE_URL}/users/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, password })
            });
            
            console.log('📥 Response status:', response.status);
            
            const data = await response.json();
            console.log('📦 Response data:', data);
            
            // ===== HANDLE RESPONSE =====
            
        if (response.ok && data.success) {
            // ✅ LOGIN SUCCESS
            const userData = data.user;
            
            if (!userData || !userData.name) {
                errorMessage.textContent = '❌ Login error: User data not received';
                return;
            }
            
            // Store user data
            localStorage.setItem('currentUser', JSON.stringify(userData));
            localStorage.setItem('userId', userData.id);
            localStorage.setItem('userEmail', userData.email);
            localStorage.setItem('userName', userData.name);
            
            // Show SUCCESS message in GREEN
            errorMessage.style.color = '#10b981';
            errorMessage.textContent = `✅ Welcome back, ${userData.name}!`;
            
            // Redirect after delay
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 1200);
            
        } else {
            // ❌ LOGIN FAILED - Show specific error messages
            
            if (response.status === 404) {
    errorMessage.innerHTML = `
        ❌ Account not found.<br>
        <a href="register.html" style="color: #667eea; font-weight: 700; text-decoration: underline;">
            ➡️ Register a new account
        </a>
    `;            
}             else if (response.status === 401) {
                errorMessage.textContent = '❌ Incorrect password. Please try again.';
            } else if (data.message) {
                errorMessage.textContent = `❌ ${data.message}`;
            } else {
                errorMessage.textContent = '❌ Login failed. Please check your credentials.';
            }
        }

            
        } catch (error) {
            console.error('💥 Login error:', error);
            errorMessage.textContent = '❌ Unable to connect to server. Please check if backend is running.';
        }
    });
});
