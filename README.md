# LabTrack UM - Local TA Lab Inventory System

A locally hosted inventory management system designed to track, organize, and monitor lab equipment used by Teaching Assistants at the University of Miami.

## Project Structure

```
labtrack-um/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/um/labtrack/
│   │   │       ├── LabTrackApplication.java          # Spring Boot main application
│   │   │       ├── ApplicationLauncher.java          # Launcher for backend + UI
│   │   │       ├── controller/
│   │   │       │   └── HelloController.java          # Sample REST controller
│   │   │       ├── entity/
│   │   │       │   └── User.java                     # Sample JPA entity
│   │   │       ├── repository/
│   │   │       │   └── UserRepository.java           # Sample JPA repository
│   │   │       ├── service/
│   │   │       │   └── UserService.java              # Sample service layer
│   │   │       └── ui/
│   │   │           └── MainFrame.java                # Swing UI main window
│   │   │       └── docs/
│   │   │           └──                               # Documents created during creation
│   │   └── resources/
│   │       └── application.properties                # Spring Boot configuration
│   └── test/
│       └── java/                                      # Test classes (to be added)
├── pom.xml                                           # Maven configuration
└── README.md                                         # This file
```

## Technology Stack

- **Backend:** Spring Boot 3.2.0
- **Database:** MySQL
- **Frontend:** Java Swing
- **Build Tool:** Maven
- **Java Version:** 17

## Prerequisites

- Java 17 or higher
- Maven 3.6+ (or use Maven Wrapper if included)

## Setup Instructions

1. **Clone or navigate to the project directory:**

   ```bash
   cd /path/to/labtrack-um
   ```

2. **Build the project:**

   ```bash
   mvn clean install
   ```

3. **Run the application:**

   ```bash
   mvn spring-boot:run
   ```

   Or run the `ApplicationLauncher` class directly from your IDE.

## Running the Application

The application will:

1. Start the Spring Boot backend server on `http://localhost:8080`
2. Automatically launch the Swing UI window once the server is ready

### Backend Endpoints

- **GET** `/api/hello` - Test endpoint that returns a JSON greeting message

## Features

### Current Implementation

- ✅ Spring Boot REST API backend
- ✅ Sample User entity, repository, and service

### Future Development

- [ ] Equipment inventory management (add, update, delete items)
- [ ] Check-in/check-out functionality
- [ ] Item status tracking (functional, broken, in repair)
- [ ] Location tracking (building, room, cabinet)
- [ ] User authentication and authorization
- [ ] Search and filter capabilities
- [ ] Reporting and analytics
- [ ] Swing UI with backend integration
- [ ] Automatic UI launch after backend startup

## Development Guidelines

### Package Structure

- `com.um.labtrack.controller` - REST API controllers
- `com.um.labtrack.entity` - JPA entities
- `com.um.labtrack.repository` - Data access repositories
- `com.um.labtrack.service` - Business logic services
- `com.um.labtrack.ui` - Swing UI components

## Configuration

Edit `src/main/resources/application.properties` to:

- Change server port
- database MySQL
- Configure logging levels
- Adjust JPA settings

## Testing

Run tests with:

```bash
mvn test
```

## Building for Production

Create an executable JAR:

```bash
mvn clean package
```

Run the JAR:

```bash
java -jar target/labtrack-um-1.0.0.jar
```

## Troubleshooting

- **Port 8080 already in use:** Change `server.port` in `application.properties`
- **UI doesn't launch:** Check console for errors, ensure backend started successfully
- **Connection refused:** Wait a few seconds after backend starts before clicking the button

## License

This project is developed for academic purposes at the University of Miami.

## Author

Herby K. Hertilien
