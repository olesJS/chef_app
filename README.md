
# Chef App

An automated system for managing salad ingredients, calculating calories based on food processing states, and analyzing nutritional values.

## Quick Start Guide

The project is self-contained using Docker and Maven Wrapper. Local installation of Java or Maven is not required.

### Prerequisites
* Docker Desktop must be installed and running.

---

## Setup and Installation

### 1. Environment Configuration
Create a `.env` file in the root directory based on the `.env.example` file and fill in your credentials:

```ini
POSTGRES_HOST=localhost
POSTGRES_PORT=5433
POSTGRES_USER=your_username
POSTGRES_PASSWORD=your_password
POSTGRES_DB=chef_db

SMTP_USER=your_email@gmail.com
SMTP_PASS=your_gmail_app_password
SMTP_TO=your_destination_email@domain.com

```

### 2. Launching the Application

#### On macOS / Linux:

1. Open the terminal in the project root directory.
2. Grant execution permissions (required only once):
```bash
  chmod +x run.sh

```

```bash
  ./run.sh

```



#### On Windows:

Double-click the `run.bat` file or execute it via Command Prompt:

```cmd
run.bat

```

---

## Database Initialization

Database schema and seed data are automatically managed by Docker Compose via the `init.sql` script. The initial dataset includes:

* **30 products** distributed across 6 categories (ROOT_VEGETABLE, TUBER_VEGETABLE, LEAFY_VEGETABLE, FRUITING_VEGETABLE, DRESSING, TOPPING) with real nutritional values.
* **5 pre-configured salads** to demonstrate business logic verification, including ProcessingState coefficients, filtering, and total calorie calculation.

---
