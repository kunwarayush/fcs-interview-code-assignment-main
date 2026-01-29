# FCS Interview Code Assignment - Completed

## Assignment Overview

This repository contains the completed Java code assignment implementing a fulfillment system with Warehouses, Stores, and Products management using Quarkus framework.

**Assignment Tasks:** [CODE_ASSIGNMENT](java-assignment/CODE_ASSIGNMENT.md)  
**Answers to Questions:** [QUESTIONS.md](java-assignment/QUESTIONS.md)

## Completed Features

### Must-Have Requirements
- **Location Gateway**: Implemented `resolveByIdentifier` method
- **Store Management**: Transactional guarantee for legacy system integration
- **Warehouse Operations**: 
  - Create, Replace, Archive, and Retrieve operations
  - Business unit code verification
  - Location validation
  - Capacity and stock validations
  - Complete REST API with OpenAPI specification

### Bonus Requirements
- **Product-Warehouse-Store Fulfillment**: Association system with constraints
  - Max 2 warehouses per product-store combination
  - Max 3 warehouses per store
  - Max 5 product types per warehouse

### Additional Enhancements
- **Health Checks**: `/q/health`, `/q/health/live`, `/q/health/ready` endpoints
- **CI/CD Pipeline**: GitHub Actions workflow for automated testing
- **Comprehensive Testing**: 109 tests with 84% instruction coverage

---

## Quick Start

### Prerequisites
- JDK 17+
- Maven 3.8+

### Build & Test

```bash
cd java-assignment

# Run all tests with coverage report
./mvnw clean verify

# Start development server (no Docker required)
./mvnw quarkus:dev
```

### Access the Application

Once running, access:
- **Web UI**: http://localhost:8080/index.html
- **Health Check**: http://localhost:8080/q/health
- **Liveness**: http://localhost:8080/q/health/live
- **Readiness**: http://localhost:8080/q/health/ready

### API Endpoints

#### Products
- `GET /product` - List all products
- `POST /product` - Create product
- `PUT /product/{id}` - Update product
- `DELETE /product/{id}` - Delete product

#### Stores
- `GET /stores` - List all stores
- `POST /stores` - Create store
- `PUT /stores/{id}` - Update store
- `DELETE /stores/{id}` - Delete store

#### Warehouses
- `GET /warehouses` - List all warehouses
- `POST /warehouses` - Create warehouse
- `PUT /warehouses/{businessUnitCode}` - Replace warehouse
- `DELETE /warehouses/{businessUnitCode}` - Archive warehouse

---

## Test Coverage

**Total Tests**: 109 (100% passing)  
**Instruction Coverage**: 84% (exceeds 80% requirement)  
**Branch Coverage**: 70%

Coverage report: `java-assignment/target/site/jacoco/index.html`

---

## CI/CD

GitHub Actions workflow configured at `.github/workflows/build.yml`:
- Runs on every push/PR
- JDK 17 setup
- Maven build with all tests
- JaCoCo coverage validation

---

## About the Code Base

This implementation is based on https://github.com/quarkusio/quarkus-quickstarts with custom fulfillment system logic.


