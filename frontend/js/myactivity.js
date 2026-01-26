// Global variables
let currentUser = null;

// Load on page load
document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadUserProfile();
});

// Check authentication
function checkAuth() {
    const userStr = localStorage.getItem('currentUser');
    if (!userStr) {
        alert('Please login to view your profile');
        window.location.href = 'login.html';
        return;
    }
    currentUser = JSON.parse(userStr);
}

// Load user profile
async function loadUserProfile() {
    if (!currentUser) return;
    
    // Set user info
    document.getElementById('userName').textContent = `👤 ${currentUser.name}`;
    document.getElementById('userEmail').textContent = currentUser.email;
    
    // Load bids and auctions
    await Promise.all([
        loadUserBids(),
        loadUserAuctions()
    ]);
}

// Load user's bids
async function loadUserBids() {
    const loading = document.getElementById('bidsLoading');
    const container = document.getElementById('bidsContainer');
    const noBids = document.getElementById('noBids');
    
    try {
        const bids = await API.getUserBids(currentUser.email);
        
        loading.style.display = 'none';
        
        if (bids.length === 0) {
            noBids.style.display = 'block';
            container.style.display = 'none';
            document.getElementById('bidsCount').textContent = '0';
            return;
        }
        
        document.getElementById('bidsCount').textContent = bids.length;
        container.innerHTML = '';
        container.style.display = 'block';
        noBids.style.display = 'none';
        
        // Sort by bid time (newest first)
        bids.sort((a, b) => new Date(b.bidTime) - new Date(a.bidTime));
        
        bids.forEach(bid => {
            const card = createBidCard(bid);
            container.appendChild(card);
        });
        
    } catch (error) {
        console.error('Error loading bids:', error);
        loading.style.display = 'none';
        noBids.style.display = 'block';
    }
}

// Create bid card
function createBidCard(bid) {
    const div = document.createElement('div');
    div.className = 'bid-card card';
    
    const auction = bid.auction;
    const isWinning = bid.isWinning;
    const isActive = auction.status === 'ACTIVE';
    const timeLeft = timeRemaining(auction.endTime);
    
    div.innerHTML = `
        <div class="card-body">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="d-flex align-items-start mb-2">
                        <h5 class="card-title mb-0 me-3">${auction.title}</h5>
                        <span class="badge ${isActive ? 'bg-success' : 'bg-secondary'}">${auction.status}</span>
                        ${isWinning && isActive ? '<span class="badge winning-badge ms-2">🏆 WINNING</span>' : ''}
                        ${!isWinning && isActive ? '<span class="badge lost-badge ms-2">Outbid</span>' : ''}
                    </div>
                    <p class="text-muted mb-2">${truncateText(auction.description, 100)}</p>
                    <div class="row">
                        <div class="col-6 col-md-3">
                            <small class="text-muted">Your Bid:</small>
                            <p class="fw-bold mb-0">${formatPrice(bid.amount)}</p>
                        </div>
                        <div class="col-6 col-md-3">
                            <small class="text-muted">Current Price:</small>
                            <p class="fw-bold mb-0 ${bid.amount === auction.currentPrice ? 'text-success' : 'text-danger'}">
                                ${formatPrice(auction.currentPrice)}
                            </p>
                        </div>
                        <div class="col-6 col-md-3">
                            <small class="text-muted">Bid Time:</small>
                            <p class="mb-0">${formatDate(bid.bidTime)}</p>
                        </div>
                        <div class="col-6 col-md-3">
                            <small class="text-muted">Time Left:</small>
                            <p class="mb-0 ${isActive ? 'text-danger' : ''}">${timeLeft}</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-4 text-md-end mt-3 mt-md-0">
                    <a href="auctions.html" class="btn btn-outline-primary btn-sm">View Auction</a>
                </div>
            </div>
        </div>
    `;
    
    return div;
}

// Load user's auctions
async function loadUserAuctions() {
    const loading = document.getElementById('auctionsLoading');
    const container = document.getElementById('auctionsContainer');
    const noAuctions = document.getElementById('noAuctions');
    
    try {
        const auctions = await API.getUserAuctions(currentUser.email);
        
        loading.style.display = 'none';
        
        if (auctions.length === 0) {
            noAuctions.style.display = 'block';
            container.style.display = 'none';
            document.getElementById('auctionsCount').textContent = '0';
            return;
        }
        
        document.getElementById('auctionsCount').textContent = auctions.length;
        container.innerHTML = '';
        container.style.display = 'block';
        noAuctions.style.display = 'none';
        
        // Sort by start time (newest first)
        auctions.sort((a, b) => new Date(b.startTime) - new Date(a.startTime));
        
        auctions.forEach(auction => {
            const card = createAuctionCard(auction);
            container.appendChild(card);
        });
        
    } catch (error) {
        console.error('Error loading auctions:', error);
        loading.style.display = 'none';
        noAuctions.style.display = 'block';
    }
}

// Create auction card
function createAuctionCard(auction) {
    const div = document.createElement('div');
    div.className = 'auction-card card';
    
    const isActive = auction.status === 'ACTIVE';
    const timeLeft = timeRemaining(auction.endTime);
    const profitAmount = auction.currentPrice - auction.startingPrice;
    const profitPercent = auction.startingPrice > 0 ? 
        ((profitAmount / auction.startingPrice) * 100).toFixed(1) : 0;
    
    div.innerHTML = `
        <div class="card-body">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="d-flex align-items-start mb-2">
                        <h5 class="card-title mb-0 me-3">${auction.title}</h5>
                        <span class="badge ${isActive ? 'bg-success' : 'bg-secondary'}">${auction.status}</span>
                    </div>
                    <p class="text-muted mb-2">${truncateText(auction.description, 100)}</p>
                    <div class="row">
                        <div class="col-6 col-md-2">
                            <small class="text-muted">Starting:</small>
                            <p class="fw-bold mb-0">${formatPrice(auction.startingPrice)}</p>
                        </div>
                        <div class="col-6 col-md-2">
                            <small class="text-muted">Current:</small>
                            <p class="fw-bold mb-0 text-success">${formatPrice(auction.currentPrice)}</p>
                        </div>
                        <div class="col-6 col-md-2">
                            <small class="text-muted">Profit:</small>
                            <p class="fw-bold mb-0 ${profitAmount > 0 ? 'text-success' : ''}">${formatPrice(profitAmount)}</p>
                            <small class="text-success">${profitAmount > 0 ? `+${profitPercent}%` : ''}</small>
                        </div>
                        <div class="col-6 col-md-2">
                            <small class="text-muted">Bids:</small>
                            <p class="fw-bold mb-0">${auction.bidCount}</p>
                        </div>
                        <div class="col-12 col-md-4">
                            <small class="text-muted">Time Left:</small>
                            <p class="mb-0 ${isActive ? 'text-danger' : 'text-muted'}">${timeLeft}</p>
                            ${auction.hasWinner ? `<small class="text-success">Winner: ${auction.winnerEmail}</small>` : ''}
                        </div>
                    </div>
                </div>
                <div class="col-md-4 text-md-end mt-3 mt-md-0">
                    <button class="btn btn-outline-danger btn-sm me-2" onclick="deleteAuction(${auction.id})">
                        Delete
                    </button>
                    <a href="auctions.html" class="btn btn-outline-primary btn-sm">View</a>
                </div>
            </div>
        </div>
    `;
    
    return div;
}

// Delete auction
async function deleteAuction(auctionId) {
    if (!confirm('Are you sure you want to delete this auction?')) {
        return;
    }
    
    try {
        const response = await fetch(`http://localhost:8080/api/auctions/${auctionId}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            alert('Auction deleted successfully!');
            loadUserAuctions(); // Reload list
        } else {
            alert('Failed to delete auction');
        }
    } catch (error) {
        console.error('Error deleting auction:', error);
        alert('Error deleting auction');
    }
}

// Utility functions
function formatPrice(price) {
    return `₹${price.toLocaleString('en-IN')}`;
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('en-IN', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function timeRemaining(endTime) {
    if (!endTime) return 'N/A';
    
    try {
        const endDate = new Date(endTime);
        const now = new Date();
        const diff = endDate - now;
        
        if (diff <= 0) return 'Ended';
        
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        
        if (days > 0) return `${days}d ${hours}h left`;
        if (hours > 0) return `${hours}h ${minutes}m left`;
        return `${minutes}m left`;
        
    } catch (error) {
        return 'N/A';
    }
}

function truncateText(text, maxLength) {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

function logout() {
    localStorage.removeItem('currentUser');
    window.location.href = 'login.html';
}
