// Auto-fill email on page load
document.addEventListener('DOMContentLoaded', () => {
    const currentUser = localStorage.getItem('currentUser');
    if (currentUser) {
        try {
            const user = JSON.parse(currentUser);
            const emailField = document.getElementById('sellerEmail');
            if (emailField) {
                emailField.value = user.email;
                emailField.readOnly = true;
            }
        } catch (e) {
            console.log('No user session found');
        }
    }
});

// Create Auction Form Handler
document.getElementById('createAuctionForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    console.log('✅ Form submitted!');
    
    // Get all form values with null checks
    const sellerEmail = document.getElementById('sellerEmail')?.value.trim();
    const title = document.getElementById('title')?.value.trim();
    const description = document.getElementById('description')?.value.trim();
    const category = document.getElementById('category')?.value;
    const condition = document.getElementById('condition')?.value;
    const startingPrice = document.getElementById('startingPrice')?.value;
    const auctionType = document.getElementById('auctionType')?.value;
    const hostelPreference = document.getElementById('hostelPreference')?.value;
    
    // Validate all required fields
    if (!sellerEmail || !title || !description || !category || !condition || !startingPrice) {
        showAlert('❌ Please fill in all required fields', 'danger');
        return;
    }
    
    // Validate email format
    if (!sellerEmail.endsWith('@kiit.ac.in')) {
        showAlert('❌ Please use your KIIT email (@kiit.ac.in)', 'danger');
        return;
    }
    
    // Show loading state
    const submitBtn = document.getElementById('submitBtn');
    const loadingBtn = document.getElementById('loadingBtn');
    if (submitBtn) submitBtn.style.display = 'none';
    if (loadingBtn) loadingBtn.style.display = 'block';
    
    try {
        // Step 1: Get user by email
        console.log('📡 Fetching user by email:', sellerEmail);
        const user = await API.getUserByEmail(sellerEmail);
        console.log('✅ User found:', user);
        
        if (!user || !user.id) {
            throw new Error('User not found. Please register first.');
        }
        
        // Step 2: Prepare auction data
        const auctionData = {
        title: document.getElementById('title').value.trim(),
        description: document.getElementById('description').value.trim(),
        category: document.getElementById('category').value,
        condition: document.getElementById('condition').value,  // ✅ Changed from itemCondition
        startingPrice: parseFloat(document.getElementById('startingPrice').value),
        currentPrice: parseFloat(document.getElementById('startingPrice').value),  // ✅ Changed from currentHighest
        sellerEmail: user.email,
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString(),
        status: 'ACTIVE',
        isQuickAuction: false
    };

        
        console.log('📤 Creating auction with data:', auctionData);
        
        // Validate starting price
        if (auctionData.startingPrice < 1) {
            throw new Error('Starting price must be at least ₹1');
        }
        
        // Step 3: Create auction
        const response = await API.createAuction(auctionData);
        console.log('✅ Auction created:', response);
        
        // Success!
        showAlert('✅ Auction created successfully! 🎉 Redirecting...', 'success');
        
        // Reset form
        document.getElementById('createAuctionForm').reset();
        
        // Redirect after 2 seconds
        setTimeout(() => {
            window.location.href = 'auctions.html';
        }, 2000);
        
    } catch (error) {
        console.error('❌ Error creating auction:', error);
        showAlert(error.message || 'Failed to create auction', 'danger');
        
    } finally {
        // Reset button state
        if (submitBtn) submitBtn.style.display = 'block';
        if (loadingBtn) loadingBtn.style.display = 'none';
    }
});

function showAlert(message, type) {
    const alertArea = document.getElementById('alertArea');
    if (alertArea) {
        alertArea.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
    }
}
