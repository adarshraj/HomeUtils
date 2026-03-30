# HomeUtils

A self-hosted collection of everyday utilities, built with Spring Boot and Thymeleaf.

## Utilities

| Utility | Description |
|---------|-------------|
| **Base64** | Encode or decode text and files using Base64 |
| **Cron Helper** | Parse cron expressions — human-readable description and next run times |
| **HEIC Converter** | Convert HEIC/HEIF images to JPEG or PNG |
| **Images to PDF** | Combine multiple images (JPEG, PNG, WebP, HEIC) into a single PDF |
| **PDF to Images** | Extract each page of a PDF as a PNG image |
| **JSON / YAML** | Format, validate, convert between JSON and YAML, and diff two documents |
| **JWT Decoder** | Decode and inspect JWT tokens — header, payload, expiry |
| **QR Code Generator** | Generate QR codes from any text or URL |

## Requirements

- Java 21+
- Maven 3.9+ (or use the included Gradle wrapper)

## Run locally

```bash
./mvnw spring-boot:run
```

The app starts on [http://localhost:8730](http://localhost:8730).

## Run with Docker

```bash
docker compose up -d
```

## Tech stack

- Spring Boot 3.4
- Thymeleaf + Bootstrap
- Apache PDFBox, ZXing, openize-heic
