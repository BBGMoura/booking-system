## Running Steps

1. Configure Database Password:

The database password must be set and changed in both the _docker-compose.yaml_ and _application-local.yml_ files.

If you change the `POSTGRES_PASSWORD` in _docker-compose.yaml_, you must also update the `spring.datasource.password` in _application-local.yml_ to match.

2. Start the Database:
The _docker-compose.yaml_ file is located in the _local_ folder. Navigate to this folder.
Run the following command to start the PostgreSQL database container: 

```bash
cd ./local
docker-compose up -d postgres
```

3. Run the Application:

Use the provided IntelliJ run configuration file _BookingSystemApplication.run.xml_ to run the application.

Alternatively, you can build and run the application using Maven from the command line:

```bash
mvn clean install

java -Dspring.profiles.active=local -jar target/booking-system-image.jar.
```
