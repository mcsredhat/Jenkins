# MySQL Database with Persistent Storage
## Project Overview
This project demonstrates deploying a MySQL database in a Docker container with persistent storage using Docker volumes. The setup ensures that your database data survives container restarts and removals.

## Features
- Containerized MySQL 8.0 database
- Persistent data storage using Docker volumes
- Environment variable configuration
- Docker Compose for easy deployment
- Port forwarding for external access

## Prerequisites
- Docker Engine (version 19.03 or newer)
- Docker Compose (version 1.27 or newer)
- Basic understanding of MySQL and Docker concepts

## Project Structure
```
mysql_project/
├── 📁 config/
│   ├── 📄 init.sql              # Database initialization script
│   └── 📄 my.cnf                # MySQL configuration file
├── 📁 mysql_data/               # MySQL data directory (persistent storage)
├── 📁 mysql_logs/               # MySQL log files directory
├── 🐳 Dockerfile               # Container configuration for MySQL
├── 🔧 .env                     # Environment variables (passwords, ports, etc.)
├── 🐙 docker-compose.yml       # Docker Compose configuration
└── 📚 README.md                # Project documentation
```

## Quick Start

### 1. Deploy MySQL Container
```
docker compose up -d
```
### 2. Display All Databases
```
docker exec my-mysql mysql -uroot -prootpass -e "SHOW DATABASES;"
```

### Display Tables in a Specific Database
```
docker exec my-mysql mysql -uroot -prootpass -D mydb -e "SHOW TABLES;"
```
### Display Tables with More Details
```
docker exec my-mysql mysql -uroot -prootpass -D mydb -e "SHOW TABLE STATUS;"
```
### Display Table Structure/Schema
```
docker exec my-mysql mysql -uroot -prootpass -D mydb -e "DESCRIBE tablename;
```
### Display All Tables from All Databases
```
docker exec my-mysql mysql -uroot -prootpass -e "SELECT TABLE_SCHEMA, TABLE_NAME FROM INFORMATION_SC
```

### Interactive MySQL Session (Alternative Approach)
## Instead of running individual commands, you can also start an interactive MySQL session:
```
docker exec -it my-mysql mysql -uroot -prootpass
```
### Then run SQL commands directly:
```
sqlSHOW DATABASES;
```
```
USE mydb;
```
```
SHOW TABLES;
```
```
DESCRIBE tablename;
```
```
EXIT;
```

### 3. Add Test Data
```
docker exec -it my-mysql mysql -uroot -prootpass -e "CREATE DATABASE demo;"
```
```
docker exec -it my-mysql mysql -uroot -prootpass -D demo -e "CREATE TABLE test (id INT, value VARCHAR(20));"
```
```
docker exec -it my-mysql mysql -uroot -prootpass -D demo -e "SHOW TABLES;"
```
### 3. Verify Data Persistence
Stop and restart the container, then check if data persists:
```
docker compose down
```
```
docker compose up -d
```
```
docker exec my-mysql mysql -uroot -prootpass -e "SELECT * FROM demo.test;"

```
## Configuration Files
### Environment Variables (.env)
The `.env` file contains environment variables used by MySQL:
- `MYSQL_ROOT_PASSWORD`: Root user password
- `MYSQL_DATABASE`: Default database name to be created

### Docker Compose Configuration
The Docker Compose file sets up the MySQL service with:
- Container name: mysql-demo
- Volume mapping: mysql-data -> /var/lib/mysql
- Environment variables from .env file
- Port mapping: 3306 (host) -> 3306 (container)

## Accessing MySQL
### From Host Machine
Using MySQL client:
```
mysql -h127.0.0.1 -P3306 -uroot -prootpass mydb
```

### From Inside the Container
```
docker exec -it my-mysql mysql -uroot -prootpass mydb
```

## Data Persistence
This project demonstrates data persistence through Docker volumes:
1. Docker Compose automatically creates a named volume (`mysql-data`)
2. The volume is mounted to the container's MySQL data directory
3. All changes to the database are stored in the volume
4. The data persists even when the container is stopped or removed

## Database Management

### Executing SQL Commands
```
docker exec -it my-mysql mysql -uroot -prootpass -e "SQL_COMMAND_HERE"
```

### Backing Up the Database
```
docker exec my-mysql mysqldump -uroot -prootpass mydb > backup.sql
```

### Restoring from Backup
```
cat backup.sql | docker exec -i mysql-demo mysql -uroot -prootpass mydb
```

## Managing the Volume
- List volumes: `docker volume ls`
- Inspect volume: `docker volume inspect mysql-data`
- Remove volume (will delete data): `docker volume rm mysql-data`

## Troubleshooting

### Common Issues
- **Connection refused**: Ensure the container is running and port 3306 is exposed
- **Authentication failure**: Verify the root password in the .env file
- **Missing database**: Check if the MYSQL_DATABASE environment variable is set correctly
- **Initialization errors**: Check container logs with `docker logs mysql-demo`

### Performance Tuning
For production environments, consider adding MySQL configuration settings through a custom my.cnf file mounted as a volume.

## Security Considerations
- Change the default root password in the .env file
- Use a non-root MySQL user for application access
- Consider network isolation for production deployments
- Secure volume permissions

