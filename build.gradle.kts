import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    java
    id("org.springframework.boot") version "4.0.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "id.perumdamts"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Repositories
// ──────────────────────────────────────────────────────────────────────────────
repositories {
    mavenCentral()
}

// ──────────────────────────────────────────────────────────────────────────────
// Dependency Management — Spring Cloud BOM (untuk OpenFeign)
// ──────────────────────────────────────────────────────────────────────────────
extra["springCloudVersion"] = "2025.1.1"
extra["springAiVersion"] = "2.0.0-M1"

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Dependencies
// ──────────────────────────────────────────────────────────────────────────────
val jooqVersion    = "3.20.1"
val mapstructVersion = "1.6.3"
val flywayVersion  = "11.3.0"

dependencies {

    // == Web ==
    implementation("org.springframework.boot:spring-boot-starter-web")

    // == Security ==
    implementation("org.springframework.boot:spring-boot-starter-security")

    // == Bean Validation ==
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // == JPA ==
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")

    // == Flyway (MariaDB / MySQL dialect) ==
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-mysql:${flywayVersion}")

    // == JOOQ ==
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.jooq:jooq:${jooqVersion}")

    // == Redis Cache ==
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // == WebFlux — WebClient untuk AppWrite REST call ==
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // == OpenFeign — HR Service ==
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    // == MCP Server (SSE over WebMVC) ==
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")

    // == OpenAPI (Swagger UI) ==
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")

    // == Actuator ==
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // == MapStruct ==
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")

    // == Configuration Metadata ==
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // == Lombok ==
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // == Lombok MapStruct Binding — agar MapStruct bisa akses getter/setter Lombok ==
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // == Dev Tools ==
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // == Test ==
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

// ──────────────────────────────────────────────────────────────────────────────
// Tasks
// ──────────────────────────────────────────────────────────────────────────────
tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<BootJar>("bootJar") {
    archiveFileName.set("mail-service.jar")
}

tasks.named<BootRun>("bootRun") {
    jvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}
