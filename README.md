Campus Auction Platform

A full-stack web application enabling KIIT students to buy and sell campus items through real-time auctions with competitive bidding, automated winner selection, and email notifications.


**Try it now:** https://campus-auction-kiit.netlify.app/ 🚀


> ⏳ Note: First load may take 30-60 seconds (free tier cold start)


✨ Features

- 🔐 User authentication (register/login with JWT)
- 📝 Create, view, update, and delete auctions
- ⏱️ Real-time countdown timers for active auctions
- 💰 Competitive bidding with server-side validation
- 🏆 Automatic winner detection when auction ends
- 📧 Email notifications to winning bidders
- 📱 Responsive UI built with Bootstrap 5
- 🔄 Auto-refresh polling (30-second intervals)
- 🎉 Animated winner announcement modals 


🛠️ Tech Stack

 Backend
- **Java 17** - Core language
- **Spring Boot 4.0.1** - Application framework
- **Spring Data JPA** - Database ORM with Hibernate
- **Spring Security** - Authentication & authorization
- **MySQL** - Production database
- **H2** - Development/testing database
- **Maven** - Build tool
- **Lombok** - Reduce boilerplate code
- **Bean Validation** - Input validation

Frontend
- **HTML5, CSS3, JavaScript** - Core web technologies
- **Bootstrap 5** - Responsive UI framework
- **Fetch API** - HTTP client for REST APIs

Tools & Deployment
- **Git & GitHub** - Version control
- **Postman** - API testing
- **VS Code** - Development IDE
- **Render** - Backend hosting
- **Netlify/Vercel** - Frontend hosting

---

📂 Project Structure

campus-auction/
├── src/
│ ├── main/
│ │ ├── java/com/kiit/campus_auction/
│ │ │ ├── config/ # CORS, Security configs
│ │ │ ├── controller/ # REST API endpoints
│ │ │ ├── dto/ # Request/Response DTOs
│ │ │ ├── model/ # JPA Entities (User, Auction, Bid)
│ │ │ ├── repository/ # JPA Repositories
│ │ │ └── service/ # Business logic layer
│ │ └── resources/
│ │ ├── application.properties # Config file
│ │ ├── static/ # Static resources
│ └── test/ # Unit & integration tests
├── frontend/
│ ├── css/ # Stylesheets
│ │ ├── global.css
│ │ ├── navbar.css
│ │ └── style.css
│ ├── js/ # JavaScript files
│ │ ├── api.js
│ │ ├── auctions.js
│ │ ├── create-auction.js
│ │ ├── login.js
│ │ ├── myactivity.js
│ │ ├── navbar.js
│ │ ├── profile.js
│ │ └── register.js
│ └── *.html # HTML pages
├── pom.xml # Maven dependencies
└── README.md


🗄️ Database Schema

 Core Entities

**User**
- id, username, email, password (hashed), created_at

**Auction**
- id, title, description, base_price, current_price, start_time, end_time, status, seller_id, winner_id, created_at

**Bid**
- id, auction_id, bidder_id, amount, timestamp

**Category** (Optional)
- id, name, description

 Relationships
- User ↔ Auction: One-to-Many (seller)
- User ↔ Auction: One-to-Many (winner)
- User ↔ Bid: One-to-Many (bidder)
- Auction ↔ Bid: One-to-Many

---

🔌 API Endpoints

Authentication
POST /api/auth/register - Register new user
POST /api/auth/login - Login user (returns JWT)
GET /api/auth/profile - Get current user profile


Auctions
GET /api/auctions - Get all active auctions
GET /api/auctions/{id} - Get auction by ID
POST /api/auctions - Create new auction
PUT /api/auctions/{id} - Update auction
DELETE /api/auctions/{id} - Delete auction
GET /api/auctions/my - Get user's created auctions


Bids
GET /api/bids/auction/{id} - Get all bids for an auction
POST /api/bids - Place a bid
GET /api/bids/my - Get user's bid history


⚙️ Running Locally (For Developers)

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Node.js (optional, for frontend dev server)

1. Clone Repository
git clone https://github.com/rhitav457/campus-auction.git
cd campus-auction

2. Configure Database
Create MySQL database:

sql
CREATE DATABASE campus_auction;
Update src/main/resources/application.properties:

text
spring.datasource.url=jdbc:mysql://localhost:3306/campus_auction
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
server.port=8080

3. Run Backend
bash
mvn clean install
mvn spring-boot:run
Backend runs at: http://localhost:8080

4. Run Frontend
Option A - Direct browser:
Open index.html in browser

Option B - Dev server:
cd frontend
npx serve . or python -m http.server 3000
Make sure frontend/js/api.js points to the correct backend URL, e.g.:
const API_BASE_URL = "http://localhost:8080";

🧪 Testing
Manual Testing
Use Postman collection (import from /postman/collection.json if available)

Test all CRUD operations

Verify authentication flow

Test bidding logic and winner selection

Unit Tests:
bash
mvn test


🛣️ Roadmap / Future Enhancements
 WebSocket support for real-time bid updates (no page refresh)

 Image upload for auction items (AWS S3/Cloudinary)

 Payment gateway integration (Razorpay/Stripe sandbox)

 Advanced search and filtering

 User ratings and reviews

 Email/SMS notifications via Twilio

 Admin dashboard for monitoring

 Mobile app (React Native/Flutter)

🐛 Known Issues
Cold start delay on Render free tier (~30-60 seconds)

Auto-refresh polling (considering WebSocket upgrade)

Email sending limited on free tier SMTP

🤝 Contributing
Contributions welcome! Please:

Fork the repository

Create feature branch (git checkout -b feature/AmazingFeature)

Commit changes (git commit -m 'Add AmazingFeature')

Push to branch (git push origin feature/AmazingFeature)

Open Pull Request

📄 License
This project is open source and available under the MIT License.

👨‍💻 Author
Rhitav Gangopadhyay
Final Year B.Tech CSE, KIIT Bhubaneswar

📧 Email: rhitav28@gmail.com

💼 LinkedIn: linkedin.com/in/rhitav-gangopadhyay

🐙 GitHub: github.com/rhitav28

🙏 Acknowledgments

->KIIT University for project inspiration
->Spring Boot community for excellent documentation
->Bootstrap team for responsive UI framework
->Stack Overflow community for debugging help

📸 Screenshots
Add screenshots here once deployed:

Homepage

Auction listing

Auction details with bidding

User dashboard

Winner announcement modal

⭐ Star this repo if you found it helpful!
