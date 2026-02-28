# High-Level System Architecture – LabTrack UM

## Overview
LabTrack UM follows a layered architecture to separate user interaction,
business logic, and data access concerns. This structure improves
maintainability, testability, and scalability.

## Architecture Layers

### Presentation Layer
- Java-based desktop interface (JavaFX planned)
- Provides role-based views for Admins, Lab Managers, and TAs
- Handles user input and displays system data

### Application / Service Layer
- Contains core business logic
- Coordinates workflows such as equipment checkout, return, and maintenance
- Enforces validation rules and status transitions

### Data Access Layer (DAO)
- Implements database operations using JDBC
- Encapsulates SQL queries and database interaction logic
- Examples: EquipmentDAO, CheckoutDAO, MaintenanceDAO

### Database Layer
- MySQL database (`labtrack_um`)
- Stores users, equipment, locations, checkouts, maintenance tickets, and audit logs
- Schema managed via version-controlled SQL scripts

## Cross-Cutting Concerns
- Input validation
- Error handling and logging
- Role-based access control (basic in early phases)

