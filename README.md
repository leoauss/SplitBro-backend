# SplitBro Backend

Spring Boot backend for the **SplitBro** expense-splitting application. Built with Java 17, Spring Boot 3, PostgreSQL, and Supabase for authentication.

## Features

- 🔐 **Supabase JWT Authentication** — Token validation via Supabase Auth API
- 💸 **Expense Splitting** — Create and manage shared transactions between friends
- 🤝 **Friend Management** — Send, accept, and manage friendship requests
- 📊 **Dashboard** — Summary of balances and transaction history
- 🔄 **Netting & Settlement** — Simplify debts with net difference calculations and bulk settlement

## Tech Stack

- **Java 17** + **Spring Boot 3.2**
- **Spring Security** + Supabase JWT
- **Spring Data JPA** + Hibernate
- **PostgreSQL** (Supabase)
- **Docker** ready

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL database (Supabase recommended)

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/leoauss/SplitBro-backend.git
cd SplitBro-backend
```

### 2. Configure Environment Variables

Copy the example environment file and fill in your values:

```bash
cp .env.example .env
```

Edit `.env` with your actual credentials:

```properties
# Database
DATABASE_HOST=your-database-host
DATABASE_PORT=5432
DATABASE_NAME=postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password

# Supabase
SUPABASE_URL=https://your-project-ref.supabase.co
SUPABASE_ANON_KEY=your_supabase_anon_key
SUPABASE_JWT_SECRET=your_jwt_secret

# CORS (comma-separated origins)
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:5174,http://localhost:8080

# Server
PORT=8080
```

### 3. Build the project

```bash
mvn clean install
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The server will start at `http://localhost:8080`

### 5. Run with Docker (optional)

```bash
docker build -t splitbro-backend .
docker run -p 8080:8080 --env-file .env splitbro-backend
```

## Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `DATABASE_HOST` | PostgreSQL host | Yes |
| `DATABASE_PORT` | PostgreSQL port (default: 5432) | No |
| `DATABASE_NAME` | Database name (default: postgres) | No |
| `DATABASE_USERNAME` | Database username | Yes |
| `DATABASE_PASSWORD` | Database password | Yes |
| `SUPABASE_URL` | Supabase project URL | Yes |
| `SUPABASE_ANON_KEY` | Supabase anonymous/public key | Yes |
| `SUPABASE_JWT_SECRET` | Supabase JWT secret for authentication | Yes |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed CORS origins | No |
| `PORT` | Server port (default: 8080) | No |

## Project Structure

```
src/
├── main/
│   ├── java/com/zetta/
│   │   ├── config/       # Security, CORS, Jackson, and Hibernate configuration
│   │   ├── controller/   # REST controllers
│   │   ├── dto/          # Data transfer objects
│   │   ├── entity/       # JPA entities
│   │   ├── repository/   # Data repositories
│   │   └── service/      # Business logic
│   └── resources/
│       └── application.properties
└── test/
    └── java/             # Unit tests
```

## Security

- **Never commit** `.env` or files with real credentials
- Use `.env.example` as a template
- All sensitive files are listed in `.gitignore`
- CORS origins are configurable via environment variables

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is open source and available under the [MIT License](LICENSE).
