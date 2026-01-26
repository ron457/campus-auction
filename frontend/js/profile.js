console.log('👤 Profile page loaded');

// ✅ CHECK LOCALSTORAGE IMMEDIATELY
console.log('🔍 Checking localStorage...');
const storedUser = localStorage.getItem('currentUser');
console.log('📦 Raw localStorage data:', storedUser);

let currentUser = null;

try {
    if (storedUser) {
        currentUser = JSON.parse(storedUser);
        console.log('✅ Parsed currentUser:', currentUser);
    } else {
        console.log('❌ No user data in localStorage');
    }
} catch (error) {
    console.error('❌ Error parsing user data:', error);
}

// Redirect to login if no user
if (!currentUser || !currentUser.id) {
    console.log('🚫 No valid user found - redirecting to login...');
    alert('⚠️ Please login first!');
    window.location.href = 'login.html';
    throw new Error('Not logged in'); // Stop execution
}

console.log('✅ User authenticated! ID:', currentUser.id, 'Email:', currentUser.email);

// Function to load user profile from backend
async function loadUserProfile() {
    try {
        console.log('🔄 Fetching profile data for user ID:', currentUser.id);
        
        const response = await fetch(`https://campus-auction-production.up.railway.app/api/users/${currentUser.id}`);
        console.log('📨 Response status:', response.status);
        
        if (!response.ok) {
            throw new Error(`Failed to fetch profile: ${response.status}`);
        }
        
        const user = await response.json();
        console.log('✅ Profile data loaded:', user);
        
        // Update avatar initial
        const avatarInitial = document.getElementById('avatar-initial');
        if (avatarInitial && user.name) {
            avatarInitial.textContent = user.name.charAt(0).toUpperCase();
        }
        
        // Update profile header
        const userName = document.getElementById('user-name');
        const userEmail = document.getElementById('user-email');
        const trustScore = document.getElementById('trust-score');
        const auctionsCount = document.getElementById('auctions-count');
        const completedCount = document.getElementById('completed-count');
        
        if (userName) userName.textContent = user.name || 'N/A';
        if (userEmail) userEmail.textContent = user.email || 'N/A';
        if (trustScore) trustScore.textContent = (user.trustScore || 0).toFixed(1);
        if (auctionsCount) auctionsCount.textContent = user.totalAuctions || 0;
        if (completedCount) completedCount.textContent = user.completedSales || 0;
        
        // Update form fields
        const nameInput = document.getElementById('name');
        const phoneInput = document.getElementById('phone');
        const branchSelect = document.getElementById('branch');
        const batchInput = document.getElementById('batch');
        const hostelInput = document.getElementById('hostel');
        
        if (nameInput) nameInput.value = user.name || '';
        if (phoneInput) phoneInput.value = user.phone || '';
        if (branchSelect) branchSelect.value = user.branch || '';
        if (batchInput) batchInput.value = user.batch || '';
        if (hostelInput) hostelInput.value = user.hostel || '';
        
        console.log('✅ Profile UI updated successfully');
        
    } catch (error) {
        console.error('❌ Error loading profile:', error);
        
        // Show error message on page
        const errorContainer = document.getElementById('error-container');
        if (errorContainer) {
            errorContainer.innerHTML = `
                <div class="error-message">
                    ⚠️ Failed to load profile data: ${error.message}
                </div>
            `;
        }
    }
}

// Load profile when page loads
document.addEventListener('DOMContentLoaded', () => {
    console.log('📄 DOM loaded - initializing profile page...');
    loadUserProfile();
});

// Handle profile update
const updateForm = document.getElementById('update-profile-form');
if (updateForm) {
    updateForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        console.log('📝 Submitting profile update...');
        
        const updatedData = {
            name: document.getElementById('name').value,
            phone: document.getElementById('phone').value,
            branch: document.getElementById('branch').value,
            batch: document.getElementById('batch').value,
            hostel: document.getElementById('hostel').value
        };
        
        console.log('📤 Update data:', updatedData);
        
        try {
            const response = await fetch(`https://campus-auction-production.up.railway.app/api/users/${currentUser.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(updatedData)
            });
            
            const result = await response.json();
            console.log('📨 Update response:', result);
            
            if (result.success) {
                console.log('✅ Profile updated successfully');
                alert('✅ Profile updated successfully!');
                
                // Update localStorage with new data
                localStorage.setItem('currentUser', JSON.stringify(result.user));
                
                // Reload profile display
                loadUserProfile();
            } else {
                console.log('❌ Update failed:', result.message);
                alert('❌ Update failed: ' + result.message);
            }
            
        } catch (error) {
            console.error('❌ Error updating profile:', error);
            alert('❌ Update failed: ' + error.message);
        }
    });
}

// Handle password change
const passwordForm = document.getElementById('change-password-form');
if (passwordForm) {
    passwordForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        console.log('🔒 Submitting password change...');
        
        const currentPassword = document.getElementById('current-password').value;
        const newPassword = document.getElementById('new-password').value;
        const confirmPassword = document.getElementById('confirm-password').value;
        
        // Validate passwords
        if (newPassword !== confirmPassword) {
            alert('❌ New passwords do not match!');
            return;
        }
        
        if (newPassword.length < 6) {
            alert('❌ Password must be at least 6 characters!');
            return;
        }
        
        try {
            const response = await fetch(`https://campus-auction-production.up.railway.app/api/users/${currentUser.id}/change-password`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    currentPassword: currentPassword,
                    newPassword: newPassword
                })
            });
            
            const result = await response.json();
            console.log('📨 Password change response:', result);
            
            if (result.success) {
                console.log('✅ Password changed successfully');
                alert('✅ Password changed successfully!');
                passwordForm.reset();
            } else {
                console.log('❌ Password change failed:', result.message);
                alert('❌ ' + result.message);
            }
            
        } catch (error) {
            console.error('❌ Error changing password:', error);
            alert('❌ Password change failed: ' + error.message);
        }
    });
}

// Logout function
function logout() {
    console.log('🚪 Logging out...');
    localStorage.removeItem('currentUser');
    alert('✅ Logged out successfully!');
    window.location.href = 'login.html';
}
