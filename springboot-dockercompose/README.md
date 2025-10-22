# Spring Boot Employee Service (Docker + MySQL)

**Project**: A simple Spring Boot REST API for managing employees, packaged into a Docker image and run together with a MySQL database using Docker Compose.

---

## Contents of this README

* Project overview
* Key files and purpose
* Prerequisites
* How to build and run with Docker Compose
* How Dockerfile and docker-compose.yml work and why both are needed
* API endpoints (examples)
* How to verify data in the MySQL image (CLI, Docker Desktop, Adminer)

---

## Project overview

This repository contains a Spring Boot application (`Employee Service`) exposing CRUD endpoints for `Employee` entities. The project is intended to run in Docker for easy local development and consistent environment setup. The database used is MySQL and runs in its own container. Docker Compose orchestrates the application and database containers.

## Key files (what they do)

* `Dockerfile` — instructions to build a Docker image for the Spring Boot application (base image, copy jar, expose port, start command).
* `docker-compose.yml` — orchestration for multiple services (app and db), networking, volumes, environment variables and ports. Use this to start both containers together.
* `src/` — Spring Boot source code (entities, repositories, controllers, application.properties).
* `README.md` — (this file) explains how to run and inspect the app and database.

---

## Prerequisites

* Docker Desktop installed and running (Windows/Mac) or Docker & docker-compose on Linux.
* Java & Maven (only required if you want to build the JAR locally rather than using a pre-built jar image).
* (Optional) MySQL Workbench, DBeaver, or Adminer for GUI-based DB inspection.

---

## Build & Run (recommended: Docker Compose)

This approach builds the Spring Boot image (using `Dockerfile`) and starts the MySQL container — all in one command.

1. Open a terminal in the project root (where `docker-compose.yml` is located).

2. Build and start services:

```bash
# Build images and start containers (foreground)
docker-compose up --build

# Or run detached
docker-compose up --build -d
```

3. To stop and remove containers/networks (preserve volumes unless you explicitly remove them):

```bash
docker-compose down
```

4. If you want to remove volumes as well (careful — this deletes DB data):

```bash
docker-compose down -v
```

---

## Why both Dockerfile and docker-compose.yml?

* **Dockerfile** defines how to *build the image* for a single service (the Spring Boot app). It includes the base image, copies the JAR, sets environment variables/entrypoint, and exposes the port.
* **docker-compose.yml** defines *how to run multiple containers together* (Spring Boot app + MySQL), configures environment variables for each service (e.g., DB user/password), sets up volumes for persistent storage, and orchestrates networking so the app can reach the DB by service name (e.g., `db`).

You need the Dockerfile to create the app image, and you need docker-compose to wire the app image together with the database and other services in a reproducible way.

---

## Example `docker-compose.yml` (typical fields)

*(This file should already be present in the repo. The example here is for reference.)*

```yaml
version: "3.8"
services:
  app:
    build: .
    container_name: springboot-app
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/mydb
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=root
    depends_on:
      - db

  db:
    image: mysql:8
    container_name: mysql-db
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=mydb
    ports:
      - "3306:3306"
    volumes:
      - db_data:/var/lib/mysql

volumes:
  db_data:
```

> Note: Modern Compose versions detect format automatically. If you get a warning about `version` being obsolete, you may remove that top-level key.

---

## API endpoints

Assuming the app runs on port **8080** (mapped by docker-compose):

* `POST /api/employees` — Create a new employee (JSON body)
* `GET /api/employees` — Get all employees
* `GET /api/employees/{id}` — Get employee by id
* `PUT /api/employees/{id}` — Update employee
* `DELETE /api/employees/{id}` — Delete employee

### Example POST request (curl)

```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@tvs.com","department":"R&D"}'
```

---

## How to view data in the MySQL image

Below are several ways to inspect the database where your `employee` table is stored.

### Option A — Docker Desktop (GUI)

1. Open **Docker Desktop** → **Containers**.
2. Click on the MySQL container (e.g., `mysql-db`).
3. Open the **Terminal** tab.
4. Run:

```bash
mysql -u root -p
# enter the root password (as set in docker-compose)
USE mydb;           -- replace mydb if you used a different name
SHOW TABLES;
SELECT * FROM employee;
```

### Option B — Docker CLI (local terminal)

1. Find container name (if you don’t know it):

```bash
docker ps
```

2. Exec into container and run MySQL client:

```bash
docker exec -it mysql-db mysql -u root -p
# then use SQL as above
```

### Option C — GUI DB client (Workbench / DBeaver)

If the `db` service exposes `3306` to the host (ports: `"3306:3306"`), use a DB client with these connection details:

* Host: `localhost`
* Port: `3306`
* Username: `root`
* Password: `root`
* Database: `mydb`

### Option D — Web admin UI (Adminer / phpMyAdmin)

You can add a lightweight Adminer service to `docker-compose.yml`:

```yaml
adminer:
  image: adminer
  container_name: adminer
  ports:
    - "8081:8080"
  depends_on:
    - db
```

Open `http://localhost:8081` and login:

* System: MySQL
* Server: `db` (or `localhost` if client connects to mapped port)
* Username: `root`
* Password: `root`
* Database: `mydb`

---

## How to add README.md in Eclipse and push to GitHub

Follow these steps to create the README file in your Eclipse project and push to GitHub.

### 1) Add README.md in Eclipse

1. In **Package Explorer**, right-click the project root → **New → File**.
2. Name it `README.md` and paste the content (this file).
3. Save the file.

### 2) Commit & Push using EGit (Eclipse Git integration)

If your project is not yet a Git repo, initialize it and connect to GitHub first.

#### Initialize local repo (if needed)

1. Right-click project → **Team → Share Project...** → Choose **Git** → Create a new repository or choose an existing local repo.
2. Click **Finish**.

#### Commit changes

1. Right-click project → **Team → Commit...**
2. Select files (including `README.md`) → Enter commit message → **Commit**.

#### Add remote (GitHub) and push

1. Right-click project → **Team → Remote → Push...**
2. If no remote exists, add your GitHub repo URL (`https://github.com/<username>/<repo>.git`) and credentials or use SSH.
3. Select the branch (e.g., `main`) and push.

### 3) Command-line alternative (git)

From project root, you can run:

```bash
git init                # if not already a git repo
git add README.md
git commit -m "Add README"
git remote add origin https://github.com/<username>/<repo>.git
git branch -M main
git push -u origin main
```

---

## Troubleshooting

* If `docker-compose up` fails with pipe errors on Windows: ensure Docker Desktop is running and context is `default` (`docker context use default`).
* If your app cannot connect to DB: check `SPRING_DATASOURCE_URL` in environment and ensure it uses the DB service name (`db`) not `localhost`.
* If data disappears after restarting containers: ensure you have a named volume (e.g., `db_data`) to persist `/var/lib/mysql` and do not use anonymous volumes.

---

## Final notes

* Keep secrets out of `docker-compose.yml` in real projects — use environment files (`.env`) or secret managers.
* For production, consider using Kubernetes or a managed database and remove `ports` mapping for DB to avoid exposing it to host.

---

If you want, I can also: create the `adminer` service in your `docker-compose.yml`, generate a sample `Dockerfile` (if missing), or prepare a one-click `run.sh` script. Tell me which next step you'd like.
