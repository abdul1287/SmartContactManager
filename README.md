# Smart Contact Manager

### A Secure Full-Stack Contact Management Application

**Smart Contact Manager** is a full-stack web application designed to simplify and secure personal contact management. Built using Java and Spring Boot, it provides an authenticated environment where users can manage their contacts, search records dynamically, upload profile images, and recover forgotten passwords through OTP-based email verification.

The application combines Spring Security, Hibernate, Spring Data JPA, Thymeleaf, and MySQL to deliver a structured and database-driven contact management solution.

---

## Key Features

### Authentication & Security
- User registration and secure authentication.
- Login and logout functionality using Spring Security.
- Password encryption using BCrypt.
- OTP-based password recovery through email verification.
- User-specific access to contact records.

### Contact Management
- Add new contacts with relevant personal information.
- Update existing contact details.
- Delete contacts.
- Store contact information such as name, nickname, email, phone number, work, and description.
- Upload and manage contact profile images.

### Search & Navigation
- Dynamic contact search using AJAX and JavaScript Fetch API.
- Search contacts by name, email, and phone number.
- Server-side pagination for efficient contact navigation.
- User-specific contact listing.

### User Experience
- Responsive interface using Bootstrap.
- Server-side rendering with Thymeleaf.
- Interactive search functionality.
- Form validation and error handling.

---

## Technology Stack

| Category | Technologies |
|---|---|
| Programming Language | Java 17 |
| Backend Framework | Spring Boot 4.0.6 |
| Security | Spring Security |
| ORM | Hibernate |
| Data Access | Spring Data JPA |
| Database | MySQL |
| Frontend | HTML, CSS, JavaScript |
| UI Framework | Bootstrap |
| Template Engine | Thymeleaf |
| Asynchronous Requests | AJAX, Fetch API |
| Email Service | Spring Boot Starter Mail |
| Build Tool | Maven |
| Development IDE | Visual Studio Code |

---

## Application Architecture

The application follows a layered Spring Boot architecture to maintain separation of concerns and improve code organization.

**High-Level Application Flow**

```text
        User Interface
      (HTML, CSS, Bootstrap)
               |
               v
          Thymeleaf
               |
               v
       Spring MVC Controllers
               |
       +-------+-------+
       |               |
       v               v
 Spring Security   Application
                   Logic
                       |
                       v
               Spring Data JPA
                       |
                       v
                   Hibernate
                       |
                       v
                  MySQL DB
```

Additional integrations:
- Spring Boot Mail for OTP-based password recovery.
- JavaScript Fetch API for dynamic contact search.
- File handling for contact image uploads.

---

## Project Setup & Installation

Follow these steps to run Smart Contact Manager locally.

### 1. Prerequisites

Make sure you have installed:

- Java JDK 17 or later
- MySQL Server
- Visual Studio Code or another Java IDE
- Git

### 2. Clone the Repository

```bash
git clone https://github.com/abdul1287/SmartContactManager.git
```

Navigate to the project directory:

```bash
cd SmartContactManager
```

### 3. Configure MySQL Database

Start your MySQL server and create a database:

```sql
CREATE DATABASE smartcontact;
```

Update the database name if your existing application configuration uses a different schema.

### 4. Configure Environment Variables

The application uses environment variables to keep sensitive credentials outside the source code.

Configure the following variables:

| Variable | Description |
|---|---|
| `DB_URL` | MySQL JDBC connection URL |
| `DB_USERNAME` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `MAIL_USERNAME` | Email address used for sending OTPs |
| `MAIL_PASSWORD` | Email account app password |

Example configuration for Windows PowerShell:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/smartcontact"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"

$env:MAIL_USERNAME="your_email@gmail.com"
$env:MAIL_PASSWORD="your_email_app_password"
```

**Note:** Replace the example values with your actual credentials. For Gmail SMTP, use an appropriate Google App Password rather than your regular account password.

### 5. Run the Application

Using Maven Wrapper on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On Linux/macOS:

```bash
./mvnw spring-boot:run
```

Alternatively, import the project into your IDE as a Maven project and run the main Spring Boot application class.

### 6. Access the Application

Once the application starts successfully, open:

**http://localhost:8080**

---

## Security Considerations

Security is an important part of this application.

- Spring Security manages authentication and authorization.
- BCrypt is used for password hashing.
- Contact records are associated with individual users.
- Database and email credentials are configured through environment variables.
- OTP-based verification supports password recovery.

Never commit actual passwords, API keys, email credentials, or other sensitive configuration values to GitHub.

---

## Future Enhancements

Potential improvements planned for future development:

- RESTful API integration.
- Advanced contact filtering and sorting.
- Contact import and export functionality.
- Cloud-based image storage.
- Docker-based deployment.
- Automated testing and CI/CD integration.
- Production deployment with cloud database support.

---

## Learning Outcomes

This project provided practical experience with:

- Developing full-stack applications using Java and Spring Boot.
- Implementing authentication and authorization using Spring Security.
- Working with Hibernate and Spring Data JPA.
- Designing database-driven applications using MySQL.
- Implementing AJAX-based dynamic search.
- Managing file uploads and email integration.
- Handling pagination and form validation.
- Managing application configuration securely.

---

## Author

**Abdul Ahmad**

B.Tech Information Technology Student | Java Full-Stack Developer

- GitHub: [@abdul1287](https://github.com/abdul1287)
- Project Repository: [Smart Contact Manager](https://github.com/abdul1287/SmartContactManager)

---

*Built with Java, Spring Boot, and a focus on secure and efficient contact management.*
