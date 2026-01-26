// ========================================
// TOAST NOTIFICATION SYSTEM
// ========================================
function showToast(message, type = 'success') {
    const toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) {
        console.error('Toast container not found!');
        return;
    }
    
    const toastId = 'toast-' + Date.now();
    
    const icons = {
        'success': '✅',
        'danger': '❌',
        'warning': '⚠️',
        'info': 'ℹ️'
    };
    
    const icon = icons[type] || icons['info'];
    
    const toastHTML = `
        <div id="${toastId}" class="toast align-items-center text-white bg-${type} border-0" role="alert">
            <div class="d-flex">
                <div class="toast-body">
                    <strong>${icon}</strong> ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;
    
    toastContainer.insertAdjacentHTML('beforeend', toastHTML);
    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement, { delay: 4000 });
    toast.show();
    
    toastElement.addEventListener('hidden.bs.toast', () => toastElement.remove());
}

// ========================================
// GLOBAL VARIABLES
// ========================================

let allAuctions = [];
let currentAuction = null;
let currentAuctionId = null;
let autoRefreshInterval = null; // ✅ NEW - Store interval ID
let isAutoRefreshEnabled = true; // ✅ NEW - Toggle for auto-refresh


// ========================================
// INITIALIZATION
// ========================================
// ========================================
// INITIALIZATION (UPDATED)
// ========================================
document.addEventListener('DOMContentLoaded', () => {
    loadAuctions();
    setupEventListeners();
    startTimerUpdates();
    enableAutoRefresh(); // ✅ NEW - Start auto-refresh
});

// Stop auto-refresh when user leaves page
window.addEventListener('beforeunload', () => {
    disableAutoRefresh();
});

// Pause auto-refresh when tab is hidden (saves resources)
document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
        isAutoRefreshEnabled = false;
        console.log('⏸️ Auto-refresh paused (tab hidden)');
    } else {
        isAutoRefreshEnabled = true;
        console.log('▶️ Auto-refresh resumed (tab visible)');
    }
});


// ========================================
// EVENT LISTENERS
// ========================================
function setupEventListeners() {
    const searchBtn = document.getElementById('searchBtn');
    const searchInput = document.getElementById('searchInput');
    const categoryFilter = document.getElementById('categoryFilter');
    const refreshBtn = document.getElementById('refreshBtn');
    const bidForm = document.getElementById('bidForm');
    
    if (searchBtn) searchBtn.addEventListener('click', handleSearch);
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') handleSearch();
        });
    }
    if (categoryFilter) categoryFilter.addEventListener('change', handleCategoryFilter);
    if (refreshBtn) refreshBtn.addEventListener('click', () => {
    loadAuctions();
    showToast('Refreshing auctions...', 'info');
    // Reset auto-refresh timer
    enableAutoRefresh();
});

    if (bidForm) bidForm.addEventListener('submit', handleBidSubmit);
}

// ========================================
// LOAD AUCTIONS
// ========================================
async function loadAuctions() {
    showLoading();
    
    try {
        allAuctions = await API.getActiveAuctions();
        console.log('✅ Loaded auctions:', allAuctions);
        
        const activeCountEl = document.getElementById('activeCount');
        if (activeCountEl) {
            activeCountEl.textContent = allAuctions.length;
        }
        
        displayAuctions(allAuctions);
        
    } catch (error) {
        console.error('❌ Error loading auctions:', error);
        showToast('Failed to load auctions. Please try again.', 'danger');
        showNoAuctions();
    }
}

// ========================================
// AUTO-REFRESH FUNCTIONALITY
// ========================================
function enableAutoRefresh() {
    console.log('🔄 Auto-refresh enabled (every 30 seconds)');
    
    // Clear any existing interval
    if (autoRefreshInterval) {
        clearInterval(autoRefreshInterval);
    }
    
    // Set new interval - refresh every 30 seconds
    autoRefreshInterval = setInterval(() => {
        if (isAutoRefreshEnabled) {
            console.log('🔄 Auto-refreshing auctions...');
            refreshAuctionsQuietly();
        }
    }, 30000); // 30 seconds
}

// Silent refresh (updates data without showing loading spinner)
async function refreshAuctionsQuietly() {
    try {
        const freshAuctions = await API.getActiveAuctions();
        
        // Check if data actually changed
        const hasChanges = JSON.stringify(allAuctions) !== JSON.stringify(freshAuctions);
        
        if (hasChanges) {
            console.log('✅ New auction data detected!');
            allAuctions = freshAuctions;
            
            const activeCountEl = document.getElementById('activeCount');
            if (activeCountEl) {
                activeCountEl.textContent = allAuctions.length;
            }
            
            displayAuctions(allAuctions);
            showToast('Auctions updated with latest bids', 'info');
        } else {
            console.log('ℹ️ No changes in auction data');
        }
        
    } catch (error) {
        console.error('❌ Auto-refresh failed:', error);
        // Don't show error toast - fail silently
    }
}

// Stop auto-refresh (call when user leaves page)
function disableAutoRefresh() {
    if (autoRefreshInterval) {
        clearInterval(autoRefreshInterval);
        autoRefreshInterval = null;
        console.log('⏸️ Auto-refresh disabled');
    }
}

// Toggle auto-refresh on/off
function toggleAutoRefresh() {
    isAutoRefreshEnabled = !isAutoRefreshEnabled;
    const status = isAutoRefreshEnabled ? 'ON' : 'OFF';
    const statusEl = document.getElementById('autoRefreshStatus');
    if (statusEl) {
        statusEl.textContent = `🔄 Auto: ${status}`;
    }
    console.log(`🔄 Auto-refresh ${status}`);
    showToast(`Auto-refresh ${status}`, 'info');
}


// ========================================
// DISPLAY AUCTIONS (UPDATED - Filter Expired)
// ========================================
function displayAuctions(auctions) {
    const grid = document.getElementById('auctionsGrid');
    const loading = document.getElementById('loadingSpinner');
    const noAuctions = document.getElementById('noAuctionsMessage');
    
    if (loading) loading.style.display = 'none';
    
    // ✅ FILTER OUT EXPIRED AUCTIONS
    const activeAuctions = auctions.filter(auction => {
        if (!auction.endTime) return true; // Keep if no end time
        const endDate = new Date(auction.endTime);
        const now = new Date();
        return endDate > now; // Only show if end time is in future
    });
    
    console.log(`📊 Total auctions: ${auctions.length}, Active: ${activeAuctions.length}`);
    
    if (activeAuctions.length === 0) {
        if (grid) grid.style.display = 'none';
        if (noAuctions) noAuctions.style.display = 'block';
        return;
    }
    
    if (noAuctions) noAuctions.style.display = 'none';
    if (grid) {
        grid.style.display = 'flex';
        grid.innerHTML = '';
        
        activeAuctions.forEach(auction => {
            const card = createAuctionCard(auction);
            grid.appendChild(card);
        });
    }
}


// ========================================
// CREATE AUCTION CARD (UPDATED - Dynamic timer)
// ========================================
function createAuctionCard(auction) {
    const col = document.createElement('div');
    col.className = 'col-md-4 mb-4';
    
    const categoryEmoji = getCategoryEmoji(auction.category);
    const timeInfo = timeRemaining(auction.endTime);
    
    const isExpired = auction.endTime && new Date(auction.endTime) <= new Date();
    const statusBadge = isExpired 
        ? '<span class="badge bg-danger">ENDED</span>'
        : '<span class="badge bg-success">ACTIVE</span>';
    
    const bidButton = isExpired
        ? '<button class="btn btn-secondary" disabled>Auction Ended</button>'
        : `<button class="btn btn-primary" onclick="window.openBidModal(${auction.id})">Place Bid</button>`;
    
    col.innerHTML = `
        <div class="card auction-card h-100 shadow-sm ${isExpired ? 'opacity-75' : ''}">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-2">
                    <span class="badge bg-primary">${categoryEmoji} ${auction.category}</span>
                    ${statusBadge}
                </div>
                
                <h5 class="card-title">${auction.title}</h5>
                <p class="card-text text-muted">${truncateText(auction.description, 80)}</p>
                
                <div class="mb-2">
                    <small class="text-muted">Condition:</small>
                    <p class="mb-1"><strong>${auction.condition || 'Not specified'}</strong></p>
                </div>
                
                <div class="mb-2">
                    <small class="text-muted">Starting Price:</small>
                    <p class="fw-bold mb-1">${formatPrice(auction.startingPrice)}</p>
                </div>
                
                <div class="mb-3">
                    <small class="text-muted">Current Highest:</small>
                    <p class="text-success fw-bold mb-0" id="current-bid-${auction.id}">
                        Loading...
                    </p>
                </div>
                
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <span id="timer-badge-${auction.id}" class="badge ${timeInfo.class}">
                        ⏰ <span id="timer-${auction.id}">${timeInfo.text}</span>
                    </span>
                    <small class="text-muted" id="bid-count-${auction.id}">
                        0 bids
                    </small>
                </div>
                
                <div class="d-grid gap-2">
                    ${bidButton}
                    <button class="btn btn-outline-secondary btn-sm" onclick="window.viewAuctionDetails(${auction.id})">
                        View Details
                    </button>
                </div>
            </div>
        </div>
    `;
    
    loadAuctionDetails(auction.id);
    
    return col;
}

// ========================================
// START TIMER UPDATES (UPDATED - Trigger winner modal)
// ========================================
let announcedAuctions = new Set(); // Track already announced auctions

function startTimerUpdates() {
    setInterval(() => {
        allAuctions.forEach(auction => {
            const timerEl = document.getElementById(`timer-${auction.id}`);
            const timerBadgeEl = document.getElementById(`timer-badge-${auction.id}`);
            
            if (timerEl && timerBadgeEl) {
                const timeInfo = timeRemaining(auction.endTime);
                
                timerEl.textContent = timeInfo.text;
                timerBadgeEl.className = `badge ${timeInfo.class}`;
                
                // ✅ If auction just ended, show winner announcement
                if (timeInfo.ended && timeInfo.text === 'Ended' && !announcedAuctions.has(auction.id)) {
                    console.log('⏰ Auction ended:', auction.id, auction.title);
                    announcedAuctions.add(auction.id); // Mark as announced
                    
                    // Store current auction for details view
                    currentAuctionId = auction.id;
                    currentAuction = auction;
                    
                    // Show winner announcement after 1 second
                    setTimeout(() => {
                        showWinnerAnnouncement(auction);
                        showToast(`Auction "${auction.title}" has ended!`, 'info');
                    }, 1000);
                    
                    // Reload auctions after 5 seconds to remove from active list
                    setTimeout(() => {
                        loadAuctions();
                    }, 5000);
                }
            }
        });
    }, 1000);
}



// ========================================
// TIME REMAINING (UPDATED - Always show seconds)
// ========================================
function timeRemaining(endTime) {
    if (!endTime) return { text: 'N/A', class: 'bg-secondary', ended: true };
    
    try {
        const endDate = new Date(endTime);
        const now = new Date();
        const diff = endDate - now;
        
        if (diff <= 0) {
            return { text: 'Ended', class: 'bg-danger', ended: true };
        }
        
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((diff % (1000 * 60)) / 1000);
        
        let text = '';
        let badgeClass = '';
        
        // Color coding based on urgency
        if (diff < 3600000) { // Less than 1 hour - RED
            badgeClass = 'bg-danger text-white fw-bold';
            if (minutes > 0) {
                text = `${minutes}m ${seconds}s`;
            } else {
                text = `${seconds}s left!`;
            }
        } else if (diff < 86400000) { // Less than 1 day - YELLOW
            badgeClass = 'bg-warning text-dark';
            text = `${hours}h ${minutes}m ${seconds}s`;
        } else { // More than 1 day - GREEN
            badgeClass = 'bg-success text-white';
            // ✅ NOW SHOWING SECONDS FOR LONG DURATIONS TOO
            text = `${days}d ${hours}h ${minutes}m ${seconds}s`;
        }
        
        return { text, class: badgeClass, ended: false };
        
    } catch (error) {
        console.error('Error calculating time:', error);
        return { text: 'N/A', class: 'bg-secondary', ended: true };
    }
}



// ========================================
// LOAD AUCTION DETAILS
// ========================================
async function loadAuctionDetails(auctionId) {
    try {
        const bidCount = await API.getBidCount(auctionId);
        const bidCountEl = document.getElementById(`bid-count-${auctionId}`);
        if (bidCountEl) {
            bidCountEl.textContent = `${bidCount} bid${bidCount !== 1 ? 's' : ''}`;
        }
        
        const winningBid = await API.getWinningBid(auctionId);
        const currentBidEl = document.getElementById(`current-bid-${auctionId}`);
        
        if (currentBidEl) {
            if (winningBid && winningBid.amount) {
                currentBidEl.textContent = formatPrice(winningBid.amount);
            } else {
                currentBidEl.textContent = 'No bids yet';
            }
        }
        
    } catch (error) {
        console.error(`Error loading details for auction ${auctionId}:`, error);
    }
}

// ========================================
// OPEN BID MODAL
// ========================================
window.openBidModal = async function(auctionId) {
    try {
        console.log('🎯 Opening bid modal for auction:', auctionId);
        
        const currentUserStr = localStorage.getItem('currentUser');
        if (!currentUserStr) {
            showToast('Please login to place a bid!', 'warning');
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 2000);
            return;
        }
        
        const currentUser = JSON.parse(currentUserStr);
        currentAuction = allAuctions.find(a => a.id === auctionId);
        
        if (!currentAuction) {
            showToast('Auction not found!', 'danger');
            return;
        }
        
        if (currentUser.email === currentAuction.sellerEmail) {
            showToast('You cannot bid on your own auction!', 'danger');
            return;
        }
        
        currentAuctionId = auctionId;
        
        document.getElementById('modalAuctionTitle').textContent = currentAuction.title;
        document.getElementById('bidAuctionId').value = auctionId;
        document.getElementById('bidderEmail').value = currentUser.email;
        
        let currentBid = currentAuction.startingPrice;
        
        try {
            const winningBid = await API.getWinningBid(auctionId);
            if (winningBid && winningBid.amount) {
                currentBid = winningBid.amount;
            }
        } catch (error) {
            console.log('ℹ️ No winning bid yet, using starting price');
        }
        
        const minBid = currentBid + 50;
        
        document.getElementById('modalCurrentBid').textContent = formatPrice(currentBid);
        document.getElementById('modalMinBid').textContent = formatPrice(minBid);
        
        const bidAmountInput = document.getElementById('bidAmount');
        bidAmountInput.min = minBid;
        bidAmountInput.value = '';
        bidAmountInput.placeholder = `Min: ₹${minBid}`;
        
        const modal = new bootstrap.Modal(document.getElementById('bidModal'));
        modal.show();
        
    } catch (error) {
        console.error('❌ Error opening bid modal:', error);
        showToast('Error opening bid form. Please try again.', 'danger');
    }
}

// ========================================
// HANDLE BID SUBMIT
// ========================================
async function handleBidSubmit(e) {
    e.preventDefault();
    
    const amount = parseFloat(document.getElementById('bidAmount').value);
    const email = document.getElementById('bidderEmail').value.trim();
    
    if (!amount || amount <= 0) {
        showModalAlert('Please enter a valid bid amount', 'danger');
        return;
    }
    
    if (!email.endsWith('@kiit.ac.in')) {
        showModalAlert('Please use your KIIT email address', 'danger');
        return;
    }
    
    const placeBidBtn = document.getElementById('placeBidBtn');
    placeBidBtn.disabled = true;
    placeBidBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Placing Bid...';
    
    try {
        const winningBid = await API.getWinningBid(currentAuctionId);
        const currentBid = winningBid?.amount || currentAuction.startingPrice;
        const minBid = currentBid + 50;
        
        if (amount < minBid) {
            showModalAlert(`Bid must be at least ₹${minBid} (Current: ₹${currentBid} + ₹50 increment)`, 'danger');
            placeBidBtn.disabled = false;
            placeBidBtn.innerHTML = 'Place Bid';
            return;
        }
        
        const bidData = {
            auctionId: currentAuctionId,
            bidderEmail: email,
            amount: amount
        };
        
        const response = await fetch("https://campus-auction-production.up.railway.app/api/bids", {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(bidData)
        });
        
        const result = await response.json();
        
        if (!response.ok) {
            throw new Error(result.message || 'Failed to place bid');
        }
        
        showToast(`Bid placed successfully! 🎉 Your bid: ${formatPrice(amount)}`, 'success');
        document.getElementById('bidForm').reset();
        
        setTimeout(() => {
            bootstrap.Modal.getInstance(document.getElementById('bidModal')).hide();
            loadAuctions();
        }, 1500);
        
    } catch (error) {
        console.error('❌ Bid error:', error);
        showToast(error.message, 'danger');
        
    } finally {
        placeBidBtn.disabled = false;
        placeBidBtn.innerHTML = 'Place Bid';
    }
}

// ========================================
// VIEW AUCTION DETAILS MODAL
// ========================================
window.viewAuctionDetails = async function(auctionId) {
    try {
        console.log('🔍 View Details clicked for auction:', auctionId);
        
        const auction = allAuctions.find(a => a.id === auctionId);
        
        if (!auction) {
            showToast('Auction not found!', 'danger');
            return;
        }

        currentAuctionId = auctionId;
        currentAuction = auction;

        document.getElementById('detailsTitle').textContent = auction.title;
        document.getElementById('detailsDescription').textContent = auction.description;
        document.getElementById('detailsCategory').textContent = `${getCategoryEmoji(auction.category)} ${auction.category}`;
        document.getElementById('detailsCondition').textContent = auction.condition;
        document.getElementById('detailsStartingPrice').textContent = formatPrice(auction.startingPrice);
        document.getElementById('detailsSeller').textContent = auction.sellerEmail;
        document.getElementById('detailsEndTime').textContent = formatDate(auction.endTime);

        let currentBid = auction.startingPrice;
        try {
            const winningBid = await API.getWinningBid(auctionId);
            if (winningBid && winningBid.amount) {
                currentBid = winningBid.amount;
            }
        } catch (error) {
            console.log('No winning bid yet');
        }
        document.getElementById('detailsCurrentBid').textContent = formatPrice(currentBid);

        const modal = new bootstrap.Modal(document.getElementById('auctionDetailsModal'));
        modal.show();

        await loadBidHistory(auctionId);

    } catch (error) {
        console.error('Error loading auction details:', error);
        showToast('Failed to load auction details', 'danger');
    }
}

// ========================================
// LOAD BID HISTORY
// ========================================
async function loadBidHistory(auctionId) {
    const bidsLoading = document.getElementById('bidsLoading');
    const noBidsMessage = document.getElementById('noBidsMessage');
    const bidsList = document.getElementById('bidsList');
    const totalBidsCount = document.getElementById('totalBidsCount');

    if (bidsLoading) bidsLoading.style.display = 'block';
    if (noBidsMessage) noBidsMessage.style.display = 'none';
    if (bidsList) bidsList.style.display = 'none';

    try {
        const bids = await API.getAuctionBids(auctionId);
        console.log('📊 Loaded bids:', bids);

        if (bidsLoading) bidsLoading.style.display = 'none';

        if (totalBidsCount) {
            totalBidsCount.textContent = `${bids.length} bid${bids.length !== 1 ? 's' : ''}`;
        }

        if (!bids || bids.length === 0) {
            if (noBidsMessage) noBidsMessage.style.display = 'block';
            return;
        }

        bids.sort((a, b) => b.amount - a.amount);

        if (bidsList) {
            bidsList.style.display = 'block';
            bidsList.innerHTML = '';

            bids.forEach((bid, index) => {
            const isWinning = index === 0;
            const bidItem = document.createElement('div');
            bidItem.className = `list-group-item ${isWinning ? 'winning-bid' : ''}`;
            
            // Handle different timestamp field names
            const timestamp = bid.timestamp || bid.bidTime || bid.createdAt || bid.date || new Date();
            
            bidItem.innerHTML = `
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        ${isWinning ? '<span class="badge bg-success me-2">🏆 Winning Bid</span>' : ''}
                        <strong class="text-primary">${formatPrice(bid.amount)}</strong>
                        <div class="small text-muted">
                            by ${bid.bidderEmail || bid.email || 'Anonymous'}
                        </div>
                    </div>
                    <div class="text-end">
                        <small class="text-muted">${formatDate(timestamp)}</small>
                        ${isWinning ? '<div class="badge bg-warning text-dark mt-1">Leading</div>' : ''}
                    </div>
                </div>
            `;
            
            bidsList.appendChild(bidItem);
        });

        }

    } catch (error) {
        console.error('Error loading bid history:', error);
        if (bidsLoading) bidsLoading.style.display = 'none';
        if (noBidsMessage) {
            noBidsMessage.style.display = 'block';
            noBidsMessage.innerHTML = `
                <i class="bi bi-exclamation-triangle text-warning" style="font-size: 3rem;"></i>
                <p class="text-muted mt-2">Failed to load bid history</p>
            `;
        }
    }
}

// ========================================
// OPEN BID MODAL FROM DETAILS
// ========================================
// ========================================
// OPEN BID MODAL FROM DETAILS (UPDATED - Fix focus issue)
// ========================================
window.openBidModalFromDetails = function() {
    const detailsModal = bootstrap.Modal.getInstance(document.getElementById('auctionDetailsModal'));
    
    if (detailsModal) {
        // ✅ Remove focus from buttons before closing
        document.activeElement.blur();
        detailsModal.hide();
    }

    // Longer delay to ensure modal is fully closed
    setTimeout(() => {
        window.openBidModal(currentAuctionId);
    }, 400); // Increased from 300ms
}
// ========================================
// SHOW WINNER ANNOUNCEMENT (UPDATED - Dynamic button text)
// ========================================
// ========================================
// SHOW WINNER ANNOUNCEMENT (UPDATED - Dynamic button text)
// ========================================
async function showWinnerAnnouncement(auction) {
    try {
        console.log('🎉 Showing winner announcement for:', auction.title);
        
        const currentUserStr = localStorage.getItem('currentUser');
        const currentUser = currentUserStr ? JSON.parse(currentUserStr) : null;
        
        const winningBid = await API.getWinningBid(auction.id);
        const bidCount = await API.getBidCount(auction.id);
        
        document.getElementById('winnerAuctionTitle').textContent = auction.title;
        
        if (winningBid && winningBid.amount) {
            document.getElementById('winnerBidAmount').textContent = formatPrice(winningBid.amount);
            document.getElementById('winnerTotalBids').textContent = bidCount;
            
            const isWinner = currentUser && currentUser.email === winningBid.bidderEmail;
            
            if (isWinner) {
                // ✅ YOU WON!
                document.getElementById('winnerTitle').textContent = '🎉 Congratulations! You Won! 🎉';
                document.getElementById('winnerMessage').innerHTML = `
                    <strong class="text-success">You are the winner!</strong><br>
                    <small>Check My Activity to contact the seller</small>
                `;
                document.getElementById('winnerIcon').innerHTML = '<div class="trophy-animation">🏆</div>';
                // ✅ Customize button text for winner
                document.getElementById('winnerActionText').textContent = 'View My Wins 🏆';
                createConfetti();
            } else {
                // ❌ SOMEONE ELSE WON
                document.getElementById('winnerTitle').textContent = 'Auction Ended';
                document.getElementById('winnerMessage').innerHTML = `
                    <strong>Winner:</strong> ${winningBid.bidderEmail}<br>
                    <small class="text-muted">Better luck next time!</small>
                `;
                document.getElementById('winnerIcon').innerHTML = '<div style="font-size: 4rem;">📦</div>';
                // ✅ Customize button text for non-winner
                document.getElementById('winnerActionText').textContent = 'View My Bids';
            }
        } else {
            // 📭 NO BIDS
            document.getElementById('winnerTitle').textContent = 'Auction Ended';
            document.getElementById('winnerMessage').innerHTML = `
                <span class="text-muted">No bids were placed on this auction</span>
            `;
            document.getElementById('winnerBidAmount').textContent = 'No bids';
            document.getElementById('winnerTotalBids').textContent = '0';
            document.getElementById('winnerIcon').innerHTML = '<div style="font-size: 4rem;">📭</div>';
            // ✅ Generic button text for no bids
            document.getElementById('winnerActionText').textContent = 'View All Auctions';
        }
        
        // Store auction data
        currentAuctionId = auction.id;
        currentAuction = auction;
        
        const modal = new bootstrap.Modal(document.getElementById('winnerModal'));
        modal.show();
        
    } catch (error) {
        console.error('❌ Error showing winner announcement:', error);
        showToast('Error showing results', 'danger');
    }
}

// ========================================
// GO TO MY ACTIVITY PAGE (NEW - Better UX)
// ========================================
window.goToMyActivity = function() {
    // Close winner modal first
    const winnerModal = bootstrap.Modal.getInstance(document.getElementById('winnerModal'));
    if (winnerModal) {
        winnerModal.hide();
    }
    
    // Small delay for smooth transition
    setTimeout(() => {
        // Redirect to My Activity page
        window.location.href = 'myactivity.html';
    }, 300);
}


// Create confetti effect (optional celebration)
function createConfetti() {
    const colors = ['#ff0000', '#00ff00', '#0000ff', '#ffff00', '#ff00ff', '#00ffff'];
    
    for (let i = 0; i < 50; i++) {
        setTimeout(() => {
            const confetti = document.createElement('div');
            confetti.className = 'confetti';
            confetti.style.left = Math.random() * window.innerWidth + 'px';
            confetti.style.background = colors[Math.floor(Math.random() * colors.length)];
            confetti.style.animationDelay = Math.random() * 0.5 + 's';
            confetti.style.animationDuration = (Math.random() * 2 + 2) + 's';
            document.body.appendChild(confetti);
            
            setTimeout(() => confetti.remove(), 5000);
        }, i * 30);
    }
}



// ========================================
// SEARCH & FILTER
// ========================================
async function handleSearch() {
    const keyword = document.getElementById('searchInput')?.value.trim();
    if (!keyword) {
        displayAuctions(allAuctions);
        return;
    }
    
    showLoading();
    try {
        const results = await API.searchAuctions(keyword);
        displayAuctions(results);
        showToast(`Found ${results.length} result${results.length !== 1 ? 's' : ''}`, 'info');
    } catch (error) {
        console.error('Search error:', error);
        showToast('Search failed. Please try again.', 'danger');
        showNoAuctions();
    }
}

async function handleCategoryFilter() {
    const category = document.getElementById('categoryFilter')?.value;
    if (!category) {
        displayAuctions(allAuctions);
        return;
    }
    
    showLoading();
    try {
        const results = await API.getAuctionsByCategory(category);
        displayAuctions(results);
        showToast(`Filtered by ${category}`, 'info');
    } catch (error) {
        console.error('Filter error:', error);
        showToast('Filter failed. Please try again.', 'danger');
        showNoAuctions();
    }
}

// ========================================
// HELPER FUNCTIONS
// ========================================
function showLoading() {
    const loading = document.getElementById('loadingSpinner');
    const grid = document.getElementById('auctionsGrid');
    const noMsg = document.getElementById('noAuctionsMessage');
    
    if (loading) loading.style.display = 'block';
    if (grid) grid.style.display = 'none';
    if (noMsg) noMsg.style.display = 'none';
}

function showNoAuctions() {
    const loading = document.getElementById('loadingSpinner');
    const grid = document.getElementById('auctionsGrid');
    const noMsg = document.getElementById('noAuctionsMessage');
    
    if (loading) loading.style.display = 'none';
    if (grid) grid.style.display = 'none';
    if (noMsg) noMsg.style.display = 'block';
}

function showModalAlert(message, type) {
    const alertArea = document.getElementById('modalAlertArea');
    if (alertArea) {
        alertArea.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
    }
}

function getCategoryEmoji(category) {
    const emojis = {
        'BOOKS': '📚', 'ELECTRONICS': '💻', 'FURNITURE': '🪑',
        'CLOTHING': '👕', 'SPORTS': '⚽', 'STATIONERY': '✏️', 'OTHER': '📦'
    };
    return emojis[category] || '📦';
}

function truncateText(text, maxLength) {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

function formatPrice(price) {
    return `₹${price.toLocaleString('en-IN')}`;
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        return date.toLocaleString('en-IN', {
            day: '2-digit',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (error) {
        return 'Invalid Date';
    }
}
