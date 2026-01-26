// Registration Form Handler
document.getElementById('registrationForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    // Get form values
    const userData = {
        name: document.getElementById('name').value.trim(),
        email: document.getElementById('email').value.trim(),
        password: document.getElementById('password').value,
        phone: document.getElementById('phone').value.trim(),
        hostel: document.getElementById('hostel').value,
        batch: document.getElementById('batch').value,
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
    
    // Show loading state
    const submitBtn = document.getElementById('submitBtn');
    const loadingBtn = document.getElementById('loadingBtn');
    submitBtn.style.display = 'none';
    loadingBtn.style.display = 'block';
    
    try {
        // Call API
        const response = await API.registerUser(userData);
        
        // Success!
        showAlert('Registration successful! Redirecting to auctions...', 'success');
        
        // Store user info in localStorage (simple session management)
        localStorage.setItem('currentUser', JSON.stringify(response));
        
        // Redirect after 2 seconds
        setTimeout(() => {
            window.location.href = 'auctions.html';
        }, 2000);
        
    } catch (error) {
        // Error handling
        showAlert(error.message, 'danger');
        
        // Reset button state
        submitBtn.style.display = 'block';
        loadingBtn.style.display = 'none';
    }
});
