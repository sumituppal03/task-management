# Task Management System — Capstone Project

A full-stack Task Management REST API combining JWT authentication,
task CRUD, priority management, statistics dashboard, and security.
This is my capstone project showcasing all Spring Boot skills.

## Tech Stack
- Java 21
- Spring Boot 3.2.5
- Spring Security + JWT
- Spring Data JPA
- MySQL Database
- BCrypt Password Encoding
- Bean Validation
- Lombok
- Maven

## Features

### Authentication
- User registration with email and password
- JWT-based login (token valid 24 hours)
- BCrypt password hashing
- Role-based access (USER / ADMIN)

### Task Management
- Create tasks with title, description, priority, due date
- Filter tasks by status (TODO / IN_PROGRESS / COMPLETED)
- Filter tasks by priority (LOW / MEDIUM / HIGH / URGENT)
- Search tasks by keyword
- Pagination on all task lists
- Overdue task detection
- Task statistics dashboard
- Users can only access their own tasks (security)

### Admin Features
- View all users
- View all tasks
- Delete any user

## Database Design
users table          tasks table
───────────          ───────────
id (PK)              id (PK)
name                 title
email (unique)       description
password (BCrypt)    priority (enum)
role (enum)          status (enum)
created_at           due_date
user_id (FK → users.id)
created_at
updated_at

## API Endpoints

### Auth (Public)
| Method | URL | Description |
|--------|-----|-------------|
| POST | /api/auth/register | Register |
| POST | /api/auth/login | Login → get token |

### Tasks (Requires JWT Token)
| Method | URL | Description |
|--------|-----|-------------|
| POST | /api/tasks | Create task |
| GET | /api/tasks | Get my tasks (paginated) |
| GET | /api/tasks/{id} | Get task by ID |
| PUT | /api/tasks/{id} | Update task |
| DELETE | /api/tasks/{id} | Delete task |
| GET | /api/tasks/status/{status} | Filter by status |
| GET | /api/tasks/priority/{priority} | Filter by priority |
| GET | /api/tasks/search?keyword= | Search tasks |
| GET | /api/tasks/overdue | Get overdue tasks |
| GET | /api/tasks/stats | Get statistics |

### Admin (Requires ADMIN role)
| Method | URL | Description |
|--------|-----|-------------|
| GET | /api/admin/users | Get all users |
| DELETE | /api/admin/users/{id} | Delete user |
| GET | /api/admin/tasks | Get all tasks |

## Sample Requests

**Register:**
```json
POST /api/auth/register
{
    "name": "Ravi Kumar",
    "email": "ravi@gmail.com",
    "password": "password123"
}
```

**Create Task (with JWT token):**
```json
POST /api/tasks
Headers: Authorization: Bearer <token>
{
    "title": "Complete Spring Boot project",
    "description": "Finish all 5 projects",
    "priority": "HIGH",
    "dueDate": "2026-05-01"
}
```

**Task Statistics Response:**
```json
GET /api/tasks/stats
{
    "total": 10,
    "todo": 4,
    "inProgress": 3,
    "completed": 3,
    "overdue": 1
}
```

**Task Response (with overdue flag):**
```json
{
    "id": 1,
    "title": "Fix critical bug",
    "priority": "URGENT",
    "status": "TODO",
    "dueDate": "2026-04-01",
    "ownerName": "Ravi Kumar",
    "overdue": true,
    "createdAt": "2026-03-31T14:30:00",
    "updatedAt": "2026-04-01T09:15:00"
}
```

## How to Run

1. Create MySQL database
```sql
CREATE DATABASE taskdb;
```

2. Update `application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/taskdb
spring.datasource.username=root
spring.datasource.password=yourpassword
jwt.secret=YourSecretKeyHere
jwt.expiration=86400000
```

3. Run the application
```bash
mvn spring-boot:run
```

4. Test with Postman
- Register a user
- Login to get JWT token
- Add token to Authorization header
- Start creating tasks!

## Skills Demonstrated
- Spring Boot REST API development
- JWT Authentication and Authorization
- Spring Security with role-based access
- JPA entity relationships (OneToMany, ManyToOne)
- Pagination and sorting
- Global exception handling
- Bean Validation
- DTOs (Data Transfer Objects)
- @Transactional operations
- Custom JPQL queries
- @PrePersist and @PreUpdate lifecycle hooks
- Clean 3-layer architecture
- Git and GitHub
