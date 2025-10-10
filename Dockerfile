# Usa un'immagine base con Java 17
FROM eclipse-temurin:17-jdk-alpine

# Imposta la directory di lavoro
WORKDIR /app

# Copia il file Maven wrapper e i pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Scarica le dipendenze (cache layer)
RUN ./mvnw dependency:go-offline

# Copia il codice sorgente
COPY src ./src

# Compila il progetto
RUN ./mvnw clean package -DskipTests

# Espone la porta usata da Spring Boot
EXPOSE 8080

# Esegui il JAR (sostituisci con il nome effettivo se diverso)
CMD ["java", "-jar", "target/funkard-api-0.0.1-SNAPSHOT.jar"]