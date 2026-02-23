# PhotoApp

A full-stack social photo-sharing web application built with Spring Boot and Thymeleaf. Users can upload photos to AWS S3, interact through likes, comments, friend requests, real-time chat, and create community pages — all managed through a server-rendered UI.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Database Schema Overview](#database-schema-overview)
- [API Endpoints](#api-endpoints)

---

## Features

### User Management
- **Registration** with email confirmation code verification
- **Login / Logout** with password hashing (Spring Security Crypto)
- **Forgot Password** flow with email-based confirmation codes
- **Change Password** (from profile or via forgot-password)
- **Edit Profile** — name, country, education, birthdate
- **Profile Picture** — upload and change via S3
- **Role-based Access** — `USER` and `ADMIN` roles

### Photos
- **Upload Photos** to Amazon S3 with public/private status
- **Like / Unlike** photos
- **Comment** on photos
- **Report Photos** (reasons: inappropriate content, spam, copyright violation)
- **Remove Photos** — delete from S3 and database
- **Feed** — browse photos from all users with "people you may know" and "pages you may like" suggestions

### Social
- **Friend Requests** — send, accept, reject
- **Friends List** — view and remove friends
- **People You May Know** — suggestions based on country
- **Real-time Chat** — send and receive messages between friends via REST polling
- **Notifications** — in-app notification system

### Pages (Community Pages)
- **Create Pages** with name, description, website, and public/private visibility
- **Page Profile Pictures** — upload and change
- **Upload Photos to Pages**
- **Like / Unlike Pages**
- **Edit Page** details
- **Pages You May Like** — discovery suggestions

### Admin Panel
- **View All Users** and user profiles
- **View All Photos** across the platform
- **View All Pages**
- **Review Reported Photos** — inspect reports with reasons
- **Ban Users** — issue time-based bans with reason
- **Remove Photos** — delete reported/inappropriate content

### Other
- **Email Service** via SMTP (Gmail) for confirmation codes and password resets
- **Logging** — file-based rolling logs (`logs/log.log`) with hourly rotation
- **Health Monitoring** — Spring Boot Actuator with detailed health endpoints
- **Startup Initializer** — auto-creates default profile picture and roles on first run

---

## Tech Stack

| Layer          | Technology                                      |
|----------------|--------------------------------------------------|
| **Framework**  | Spring Boot 3.0.6                                |
| **Language**   | Java 17                                          |
| **Template**   | Thymeleaf                                        |
| **Database**   | MySQL                                            |
| **ORM**        | Spring Data JPA / Hibernate                      |
| **Storage**    | Amazon S3 (AWS SDK 1.12.232)                     |
| **Security**   | Spring Security Crypto (BCrypt password hashing) |
| **Validation** | Jakarta Bean Validation                          |
| **Email**      | Spring Boot Mail (SMTP)                          |
| **Monitoring** | Spring Boot Actuator                             |
| **Build**      | Maven                                            |
| **Utilities**  | Lombok, Commons IO                               |

---

## Project Structure

```
src/main/java/com/example/photoapp/
├── PhotoAppApplication.java          # Application entry point
├── config/
│   ├── Config.java                   # General configuration
│   ├── S3Config.java                 # AWS S3 client bean
│   ├── StartupDataInitializer.java   # Seeds default data (roles, profile pic)
│   └── ByteArrayMultipartFile.java   # MultipartFile adapter for byte arrays
├── controller/
│   ├── LoginController.java          # Login, registration, confirmation, forgot password
│   ├── ChatController.java           # REST endpoints for chat messaging
│   ├── user/
│   │   ├── UserController.java       # Friends, password, profile, pages
│   │   └── UserPhotoController.java  # Photo upload, like, comment, report, pages
│   └── admin/
│       └── AdminController.java      # Admin: users, photos, reports, bans
├── entity/
│   ├── User.java                     # User account with friends, bans, messages
│   ├── Photo.java                    # Photo with likes, comments, status
│   ├── Page.java                     # Community page with owner, likes
│   ├── ChatMessage.java              # Chat message between two users
│   ├── FriendRequest.java            # Friend request with status tracking
│   ├── Notification.java             # User notification
│   ├── PhotoComment.java             # Comment on a photo
│   ├── PhotoReport.java              # Photo report with reason
│   ├── UserBan.java                  # Timed user ban issued by admin
│   ├── UserConfirmation.java         # Email confirmation code
│   ├── Role.java                     # User role (USER / ADMIN)
│   ├── Response.java                 # Generic API response wrapper
│   ├── ChatMessageRequest.java       # Chat send request body
│   └── dto/                          # Data Transfer Objects
│       ├── UserDto.java
│       ├── LoginUserDto.java
│       ├── ChangePasswordDto.java
│       ├── ConfirmationDto.java
│       ├── FriendDto.java
│       ├── FriendRequestDto.java
│       ├── PhotoDto.java
│       ├── PhotoCommentDto.java
│       ├── PhotoReportDto.java
│       ├── PageDto.java
│       ├── ChatMessageDTO.java
│       └── UserBanDto.java
├── enums/
│   ├── RoleEnum.java                 # ROLE_USER, ROLE_ADMIN
│   ├── RegistrationStatusEnum.java   # PENDING, CONFIRMED
│   ├── FriendRequestStatusEnum.java  # PENDING, ACCEPTED, REJECTED
│   ├── CodeConfirmationEnum.java     # REGISTRATION, CHANGE_PASSWORD
│   ├── CodeConfirmationStatusEnum.java
│   ├── ChangePasswordEnum.java       # CHANGE_PASSWORD, FORGOT_PASSWORD
│   └── ReportReasonEnum.java         # INAPPROPRIATE_CONTENT, SPAM, COPYRIGHT_VIOLATION
├── repository/                       # Spring Data JPA repositories
│   ├── UserRepository.java
│   ├── PhotoRepository.java
│   ├── PageRepository.java
│   ├── ChatMessageRepository.java
│   ├── FriendRequestRepository.java
│   ├── NotificationRepository.java
│   ├── PhotoCommentRepository.java
│   ├── PhotoReportRepository.java
│   ├── RoleRepository.java
│   ├── UserBanRepository.java
│   └── UserConfirmationRepository.java
├── service/
│   ├── UserService.java              # User service interface
│   ├── PhotoService.java             # Photo service interface
│   ├── ChatService.java              # Chat service interface
│   ├── EmailService.java             # Email service interface
│   └── impl/                         # Service implementations
│       ├── UserServiceImpl.java
│       ├── PhotoServiceImpl.java
│       ├── ChatServiceImpl.java
│       └── EmailServiceImpl.java
└── util/
    └── CurrentUser.java              # Utility for tracking the logged-in user

src/main/resources/
├── application.properties            # App configuration
├── default_profile_picture.jpg       # Default avatar
└── templates/                        # Thymeleaf HTML templates
    ├── login.html
    ├── registration.html
    ├── codeConfirmation.html
    ├── forgotPasswordEmail.html
    ├── changePassword.html
    ├── uploadForm.html               # Main feed / home page
    ├── uploadPhoto.html
    ├── removePhoto.html
    ├── profile.html
    ├── myProfile.html
    ├── changeProfilePicture.html
    ├── addFriend.html
    ├── showFriend.html
    ├── friendRequests.html
    ├── page.html
    ├── showPages.html
    ├── reportedPhoto.html
    ├── banUser.html
    └── nav.html                      # Shared navigation bar fragment
```

---

## Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **MySQL 8.0+**
- **AWS Account** with an S3 bucket

---

## Configuration

Edit `src/main/resources/application.properties` before running:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/photos
spring.datasource.username=<your-db-username>
spring.datasource.password=<your-db-password>

# AWS S3
cloud.aws.bucketName=<your-s3-bucket>
cloud.aws.accessKey=<your-aws-access-key>
cloud.aws.secretKey=<your-aws-secret-key>
cloud.aws.region=<your-aws-region>

# Email (SMTP)
mail.host=smtp.gmail.com
mail.port=587
mail.username=<your-email>
mail.password=<your-app-password>
```

> **Note:** Create the MySQL database `photos` before starting the app. The schema is auto-generated by Hibernate (`ddl-auto=update`).

---

## Running the Application

```bash
# Clone the repository
git clone <repository-url>
cd PhotoApp

# Build
./mvnw clean package -DskipTests

# Run
./mvnw spring-boot:run
```

The application starts at **http://localhost:8080**. The root URL (`/`) redirects to the login page.

### First Run

On startup, the application automatically:
1. Uploads a **default profile picture** to S3 (if not already present)
2. Creates **ROLE_USER** and **ROLE_ADMIN** roles in the database (if empty)

---

## Database Schema Overview

| Entity             | Description                                        |
|--------------------|----------------------------------------------------|
| `User`             | Accounts with name, email, country, education, birthdate, role, friends list |
| `Photo`            | Uploaded photos with filename, status, likes, comments |
| `Page`             | Community pages with owner, description, website, likes |
| `ChatMessage`      | Direct messages between users with delivery tracking |
| `FriendRequest`    | Friend requests with PENDING/ACCEPTED/REJECTED status |
| `Notification`     | In-app notifications per user                      |
| `PhotoComment`     | Comments attached to photos                        |
| `PhotoReport`      | Reports on photos with reason enum                 |
| `UserBan`          | Admin-issued timed bans with reason                |
| `UserConfirmation` | Email confirmation codes for registration/password reset |
| `Role`             | User roles (USER, ADMIN)                           |

---

## API Endpoints

### Public
| Method | Path                | Description                  |
|--------|---------------------|------------------------------|
| GET    | `/login`            | Login page                   |
| POST   | `/login`            | Authenticate user            |
| GET    | `/registration`     | Registration page            |
| POST   | `/registration`     | Register new user            |
| GET    | `/confirm`          | Email confirmation page      |
| POST   | `/confirm`          | Verify confirmation code     |
| GET    | `/forgot-password`  | Forgot password page         |
| POST   | `/forgot-password`  | Send password reset email    |
| GET    | `/logout`           | Logout                       |

### User
| Method | Path                          | Description                        |
|--------|-------------------------------|------------------------------------|
| GET    | `/uploadForm`                 | Main feed with photos              |
| GET    | `/profile?email=`             | View user profile                  |
| POST   | `/upload`                     | Upload photo to S3                 |
| POST   | `/like-photo`                 | Like a photo                       |
| POST   | `/unlike-photo`               | Unlike a photo                     |
| POST   | `/add-comment`                | Comment on a photo                 |
| POST   | `/report-photo`               | Report a photo                     |
| GET    | `/upload-photo`               | Upload photo page                  |
| GET    | `/remove-photo`               | Manage own photos                  |
| POST   | `/remove-photo`               | Delete a photo                     |
| GET    | `/change-profile-picture`     | Change profile picture page        |
| POST   | `/change-profile-picture`     | Upload new profile picture         |
| GET    | `/show-pages`                 | View own pages                     |
| POST   | `/create-page`                | Create a new page                  |
| GET    | `/page?name=`                 | View a page                        |
| POST   | `/upload-photo-page`          | Upload photo to a page             |
| POST   | `/change-profile-picture-page`| Change page profile picture        |

### User — Social (`/user`)
| Method | Path                      | Description                |
|--------|---------------------------|----------------------------|
| GET    | `/user/add-friend`        | Browse non-friend users    |
| POST   | `/user/add-friend`        | Send friend request        |
| GET    | `/user/show-friend`       | View friends list          |
| POST   | `/user/remove-friend`     | Remove a friend            |
| GET    | `/user/friend-requests`   | View pending requests      |
| POST   | `/user/confirm-friend`    | Accept friend request      |
| POST   | `/user/reject-friend`     | Reject friend request      |
| GET    | `/user/change-password`   | Change password page       |
| POST   | `/user/change-password`   | Submit password change     |
| POST   | `/user/edit`              | Edit user profile          |
| POST   | `/user/edit-page`         | Edit page details          |
| POST   | `/user/like-page`         | Like a page                |
| POST   | `/user/unlike-page`       | Unlike a page              |
| GET    | `/user/get-friends`       | REST: get friends list     |

### Chat (`/chat`)
| Method | Path                  | Description                     |
|--------|-----------------------|---------------------------------|
| GET    | `/chat/messages`      | Get chat history with a friend  |
| GET    | `/chat/new-messages`  | Poll for new messages           |
| POST   | `/chat/send`          | Send a chat message             |

### Admin (`/admin`)
| Method | Path                    | Description                   |
|--------|-------------------------|-------------------------------|
| GET    | `/admin/all-user`       | View all users                |
| GET    | `/admin/all-photo`      | View all photos               |
| GET    | `/admin/all-pages`      | View all pages                |
| GET    | `/admin/reported-photo` | View reported photos          |
| GET    | `/admin/ban-user`       | Ban user form                 |
| POST   | `/admin/ban-user`       | Issue a user ban              |
| POST   | `/admin/remove-photo`   | Remove a reported photo       |

---

## License

This project is for educational / personal use.
