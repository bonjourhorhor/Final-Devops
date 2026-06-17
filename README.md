# ID Card Management System

A comprehensive Spring Boot application for managing ID cards for students, employees, and users. Features include CRUD operations, PDF generation, QR code integration, barcode support, and batch card generation.

## 🎯 Features

### Core Features (5 points)

- **CRUD for Profiles**: Create, read, update, and delete user profiles used in ID cards
- **Photo Upload Handling (3 points)**: Accept images (JPEG/PNG), validate file size/type
- **ID Card Template Engine (2 points)**: Support multiple card templates with customizable colors and layouts

### Advanced Features

- **Live Preview (5 points)**: Generate instant preview of ID cards
- **Unique ID Generation (2 points)**: Auto-generate registration numbers with format: YEAR-DEPT-UUID
- **PDF Export (5 points)**: Generate individual and batch PDF ID cards using iText
- **Batch ID Card Generation (2 points)**: Create multiple ID cards at once
- **QR Code Integration (3 points)**: Generate QR codes for card verification
- **Barcode Support (3 points)**: Support for Code-128 and EAN-13 barcodes

## 🏗️ Architecture

### Models

- **Profile**: User/Student/Employee profile data
- **Template**: ID card template with customizable styling
- **ProfileType**: Enum (STUDENT, EMPLOYEE, USER)
- **BarcodeType**: Enum (CODE_128, EAN_13)

### Repositories

- **ProfileRepository**: JPA repository for Profile entities
- **TemplateRepository**: JPA repository for Template entities

### Services

- **ProfileService**: Business logic for profile management, PDF/QR generation

### Controllers

- **ProfileController**: REST API endpoints for profile operations
- **TemplateController**: REST API endpoints for template management

### Utilities

- **QRCodeUtil**: QR code generation using ZXing
- **PDFUtil**: PDF generation using iText

## 🛠️ Technology Stack

- **Framework**: Spring Boot 4.1.0
- **Language**: Java 25
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA with Hibernate
- **PDF Generation**: iText 5.5.13
- **QR Code**: ZXing 3.5.2
- **Testing**: JUnit 5, Mockito
- **Build**: Maven 3.8+

## 📋 Project Structure

```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── controller/
│   │   │   ├── ProfileController.java
│   │   │   └── TemplateController.java
│   │   ├── model/
│   │   │   ├── Profile.java
│   │   │   ├── Template.java
│   │   │   ├── ProfileType.java
│   │   │   └── BarcodeType.java
│   │   ├── repository/
│   │   │   ├── ProfileRepository.java
│   │   │   └── TemplateRepository.java
│   │   ├── service/
│   │   │   └── ProfileService.java
│   │   ├── util/
│   │   │   ├── PDFUtil.java
│   │   │   └── QRCodeUtil.java
│   │   └── DemoApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/example/demo/
        ├── controller/
        │   └── ProfileControllerTest.java
        ├── repository/
        │   └── ProfileRepositoryTest.java
        └── service/
            └── ProfileServiceTest.java
```

## 🚀 Getting Started

### Prerequisites

- Java 25+
- Maven 3.8+
- MySQL 8.0+
- Git

### Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/yourusername/id-card-manager.git
   cd id-card-manager
   ```

2. **Configure MySQL Database**
   - Create a MySQL database: `id_card_db`
   - Update `src/main/resources/application.properties`:
     ```properties
     spring.datasource.username=root
     spring.datasource.password=your_password
     ```

3. **Build the project**

   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The application will start at `http://localhost:8080`

## 📡 API Endpoints

### Profile Endpoints

#### Get All Profiles

```http
GET /api/profiles
```

#### Get Profile by ID

```http
GET /api/profiles/{id}
```

#### Create Profile

```http
POST /api/profiles
Content-Type: multipart/form-data

Parameters:
- fullName (required)
- email (required)
- department (required)
- type (required): STUDENT, EMPLOYEE, USER
- title (optional)
- phone (optional)
- photo (optional): JPEG or PNG file
```

#### Update Profile

```http
PUT /api/profiles/{id}
Content-Type: multipart/form-data
```

#### Delete Profile

```http
DELETE /api/profiles/{id}
```

#### Search Profiles

```http
GET /api/profiles/search?keyword=John
```

#### Get Profiles by Type

```http
GET /api/profiles/type/STUDENT
```

#### Generate PDF ID Card

```http
GET /api/profiles/{id}/pdf
```

#### Generate QR Code

```http
GET /api/profiles/{id}/qrcode
```

#### Generate Batch PDF

```http
POST /api/profiles/batch/pdf
Content-Type: application/json

Body: [1, 2, 3]  // Profile IDs
```

#### Generate Batch PDF by Type

```http
GET /api/profiles/batch/pdf/type/STUDENT
```

### Template Endpoints

#### Get All Templates

```http
GET /api/templates
```

#### Get Template by ID

```http
GET /api/templates/{id}
```

#### Get Active Templates

```http
GET /api/templates/active
```

#### Search Templates

```http
GET /api/templates/search?keyword=Professional
```

#### Create Template

```http
POST /api/templates
Content-Type: application/json
```

#### Update Template

```http
PUT /api/templates/{id}
```

#### Delete Template

```http
DELETE /api/templates/{id}
```

## 🧪 Testing

Run all tests:

```bash
mvn test
```

Run specific test class:

```bash
mvn test -Dtest=ProfileServiceTest
```

Run with coverage:

```bash
mvn test jacoco:report
```

### Test Coverage

- **Unit Tests**: Service and utility classes
- **Integration Tests**: Controller endpoints with MockMvc
- **Repository Tests**: Database query methods with H2

## 📊 Database Schema

### profiles table

- `id`: Primary key
- `uuid`: Unique public identifier
- `registration_number`: Human-friendly ID (YEAR-DEPT-###)
- `type`: Profile type enum
- `full_name`: User name
- `department`: Department name
- `title`: Job/academic title
- `email`: Email address
- `phone`: Phone number
- `blood_group`: Blood type
- `date_of_birth`: DOB
- `issue_date`: Card issue date
- `expiry_date`: Card expiry date
- `photo_file_name`: Photo filename
- `photo_content_type`: MIME type
- `template_id`: Template foreign key
- `barcode_type`: Barcode type enum
- `created_at`: Creation timestamp
- `updated_at`: Last update timestamp

### templates table

- `id`: Primary key
- `code`: Template code
- `name`: Template name
- `organization_name`: Organization name
- `layout`: VERTICAL or HORIZONTAL
- `primary_color`: Hex color code
- `secondary_color`: Hex color code
- `text_color`: Hex color code
- `tagline`: Template tagline
- `active`: Boolean flag
- `created_at`: Creation timestamp
- `updated_at`: Last update timestamp

## 🔒 Security Considerations

- File upload validation for image types and sizes
- Input validation on all API endpoints
- CORS enabled for development (configure for production)
- No sensitive data stored in QR codes (only verification URL)

## 📝 Example Usage

### Create a Profile

```bash
curl -X POST http://localhost:8080/api/profiles \
  -F "fullName=John Doe" \
  -F "email=john@example.com" \
  -F "department=Engineering" \
  -F "type=EMPLOYEE" \
  -F "photo=@/path/to/photo.jpg"
```

### Generate PDF Card

```bash
curl -X GET http://localhost:8080/api/profiles/1/pdf \
  -H "Accept: application/pdf" \
  -o id-card-1.pdf
```

### Generate QR Code

```bash
curl -X GET http://localhost:8080/api/profiles/1/qrcode \
  -H "Accept: image/png" \
  -o qr-code-1.png
```

## 📚 Dependencies

- **Spring Boot Starters**: web, data-jpa, thymeleaf
- **Database**: mysql-connector-java, h2 (testing)
- **PDF**: itextpdf 5.5.13
- **QR Code**: zxing-core, zxing-javase 3.5.2
- **Image Processing**: commons-imaging 1.0-alpha3
- **JSON**: gson
- **Lombok**: For getters/setters
- **Testing**: junit-jupiter, mockito

## 🐛 Troubleshooting

### Database Connection Issues

- Ensure MySQL server is running
- Check username/password in application.properties
- Verify database `id_card_db` exists

### Photo Upload Errors

- Only JPEG and PNG formats are supported
- Maximum file size is 5MB
- Check file permissions in upload directory

### PDF Generation Issues

- Ensure iText library is in classpath
- Check PDF template syntax
- Verify profile data is complete

## 📄 License

This project is licensed under the MIT License - see LICENSE file for details.

## 👥 Contributors

- DevOps Team - ITC Year 4, Semester 2

## 📞 Support

For issues and questions, please open an issue on GitHub.

## 🔄 Continuous Integration

The project includes:

- Maven build configuration
- JUnit test suite
- Integration tests with H2 in-memory database

## 🚧 Future Enhancements

- [ ] Web UI with Thymeleaf templates
- [ ] Advanced barcode types (EAN, Code 39, etc.)
- [ ] Cloud storage for photos (AWS S3)
- [ ] Email integration for card delivery
- [ ] Multi-language support
- [ ] Card validation API
- [ ] Analytics and reporting
- [ ] Card printing service integration
