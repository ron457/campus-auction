// Base API URL
const API_BASE_URL = "https://campus-auction-production.up.railway.app/api";

const API = {
    // Base URL and Headers
    BASE_URL: API_BASE_URL,
    HEADERS: {
        'Content-Type': 'application/json'
    },
   
    // Register User
    registerUser: async (userData) => {
        const response = await fetch(`${API_BASE_URL}/users/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Registration failed');
        }
        
        return await response.json();
    },

    // Users API
    getUserByEmail: async function(email) {
        const res = await fetch(`${API_BASE_URL}/users/email/${encodeURIComponent(email)}`);
        if (!res.ok) throw new Error('User not found');
        return res.json();
    },

    getAllUsers: async function() {
        const response = await fetch(`${API_BASE_URL}/users`);
        return await response.json();
    },

    getUserProfile: async function(email) {
        try {
            const response = await fetch(`${API_BASE_URL}/users/email/${encodeURIComponent(email)}`);
            if (!response.ok) throw new Error('User not found');
            return await response.json();
        } catch (error) {
            console.error('Error fetching user profile:', error);
            return {
                name: 'User',
                email: email,
                trustScore: 100.0
            };
        }
    },

    updateUserProfile: async function(email, profileData) {
        const response = await fetch(`${API_BASE_URL}/users/email/${encodeURIComponent(email)}`, {
            method: 'PUT',
            headers: this.HEADERS,
            body: JSON.stringify(profileData)
        });
        
        if (!response.ok) {
            throw new Error('Failed to update profile');
        }
        
        return await response.json();
    },
    
    // Auctions API
    createAuction: async function(auctionData) {
        const response = await fetch(`${API_BASE_URL}/auctions`, {
            method: 'POST',
            headers: this.HEADERS,
            body: JSON.stringify(auctionData)
        });
        
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to create auction');
        }
        
        return await response.json();
    },

    getAuctions: async function() {
        const response = await fetch(`${API_BASE_URL}/auctions`);
        return await response.json();
    },

    getAllAuctions: async function() {
        return this.getAuctions();
    },

    getActiveAuctions: async function() {
        const response = await fetch(`${API_BASE_URL}/auctions/active`);
        return await response.json();
    },

    getAuctionById: async function(id) {
        const response = await fetch(`${API_BASE_URL}/auctions/${id}`);
        if (!response.ok) throw new Error('Auction not found');
        return await response.json();
    },
    // Get auctions won by user
    getAuctionsWon: async function(email) {
        try {
            const response = await fetch(`${API_BASE_URL}/bids/user/email/${encodeURIComponent(email)}/won`);
            if (!response.ok) return [];
            return await response.json();
        } catch (error) {
            console.error('Error fetching won auctions:', error);
            return [];
        }
    },


    // Get user's bids
    getUserBids: async function(email) {
        try {
            const response = await fetch(`${API_BASE_URL}/bids/user/email/${encodeURIComponent(email)}`);
            if (!response.ok) return [];
            return await response.json();
        } catch (error) {
            console.error('Error fetching user bids:', error);
            return [];
        }
    },

    // Get user's auctions
    getUserAuctions: async function(email) {
        try {
            const response = await fetch(`${API_BASE_URL}/auctions/seller/email/${encodeURIComponent(email)}`);
            if (!response.ok) return [];
            return await response.json();
        } catch (error) {
            console.error('Error fetching user auctions:', error);
            return [];
        }
    },

    getAuctionsByCategory: async function(category) {
        const response = await fetch(`${API_BASE_URL}/auctions/category/${category}`);
        return await response.json();
    },

    searchAuctions: async function(keyword) {
        const response = await fetch(`${API_BASE_URL}/auctions/search?keyword=${keyword}`);
        return await response.json();
    },

    getAuctionStats: async function(auctionId) {
        const response = await fetch(`${API_BASE_URL}/auctions/${auctionId}/stats`);
        return await response.json();
    },

    deleteAuction: async function(auctionId) {
        const response = await fetch(`${API_BASE_URL}/auctions/${auctionId}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            throw new Error('Failed to delete auction');
        }
        
        return true;
    },

    // Get winning bid
    getWinningBid: async function(auctionId) {
        try {
            const response = await fetch(`${API_BASE_URL}/auctions/${auctionId}/winning`);
            const text = await response.text();
            
            if (!text || text.trim() === '' || text.startsWith('<!DOCTYPE')) {
                console.log('No winning bid found for auction', auctionId);
                return null;
            }
            
            return JSON.parse(text);
        } catch (error) {
            console.error('Error fetching winning bid:', error);
            return null;
        }
    },

    // Get bid count
    getBidCount: async function(auctionId) {
        try {
            const response = await fetch(`${API_BASE_URL}/auctions/${auctionId}/bid-count`);
            
            if (!response.ok) {
                console.log('No bids found for auction', auctionId);
                return 0;
            }
            
            const data = await response.json();
            return data.count || 0;
        } catch (error) {
            console.error('Error fetching bid count:', error);
            return 0;
        }
    },

    // Bids API
    placeBid: async function(bidData) {
        const response = await fetch(`${API_BASE_URL}/bids`, {
            method: 'POST',
            headers: this.HEADERS,
            body: JSON.stringify(bidData)
        });
        
        const result = await response.json();
        
        if (!response.ok) {
            throw new Error(result.message || 'Failed to place bid');
        }
        
        return result;
    },

    getAuctionBids: async function(auctionId) {
        const response = await fetch(`${API_BASE_URL}/bids/auction/${auctionId}`);
        return await response.json();
    },

    getBids: async function(auctionId) {
        return this.getAuctionBids(auctionId);
    }
};

// ✅ CREATE LOWERCASE ALIAS
const api = API;

// ✅ EXPOSE GLOBALLY
window.API = API;
window.api = api;

// Utility Functions
function showAlert(message, type = 'success') {
    const alertArea = document.getElementById('alertArea');
    if (!alertArea) return;

    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    alertArea.innerHTML = '';
    alertArea.appendChild(alert);
    
    setTimeout(() => {
        alert.remove();
    }, 5000);
}

function formatPrice(price) {
    return `₹${price.toLocaleString('en-IN')}`;
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-IN', { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function timeRemaining(endDate) {
    const now = new Date();
    const end = new Date(endDate);
    const diff = end - now;
    
    if (diff <= 0) return 'Ended';
    
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    
    if (days > 0) return `${days}d ${hours}h left`;
    return `${hours}h left`;
}
