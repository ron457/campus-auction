// Alert Helper Function
function showAlert(message, type) {
    const alertContainer = document.getElementById('alertContainer');
    if (alertContainer) {
        alertContainer.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
    }
}

// Registration Form Handler
const registrationForm = document.getElementById('registrationForm');

if (registrationForm) {
    registrationForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        // Get form values
        const userData = {
            name: document.getElementById('name').value.trim(),
            email: document.getElementById('email').value.trim(),
            password: document.getElementById('password').value,
            phone: document.getElementById('phone').value.trim(),
            hostel: document.getElementById('hostel').value.trim(),
            batch: document.getElementById('batch').value.trim(),
            branch: document.getElementById('branch').value
        };
        
        // Validate KIIT email
        if (!userData.email.endsWith('@kiit.ac.in')) {
            showAlert('Please use your KIIT email (@kiit.ac.in)', 'danger');
            return;
        }
        
        // Validate phone number
        if (!/^\d{10}$/.test(userData.phone)) {
            showAlert('Please enter a valid 10-digit phone number', 'danger');
            return;
        }
        
        // Validate password length
        if (userData.password.length < 6) {
            showAlert('Password must be at least 6 characters long', 'danger');
            return;
        }
        
        // Show loading state
        const submitBtn = document.getElementById('submitBtn');
        const loadingBtn = document.getElementById('loadingBtn');
        submitBtn.style.display = 'none';
        loadingBtn.style.display = 'block';
        
        try {
            // Call API
            const response = await API.registerUser(userData);
            
            // Success!
            showAlert('Registration successful! Redirecting to login...', 'success');
            
            // Redirect after 2 seconds
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 2000);
            
        } catch (error) {
            // Error handling
            console.error('Registration error:', error);
            showAlert(error.message || 'Registration failed. Please try again.', 'danger');
            
            // Reset button state
            submitBtn.style.display = 'block';
            loadingBtn.style.display = 'none';
        }
    });
} else {
    console.error('Registration form not found!');
}
