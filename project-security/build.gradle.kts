val springdocVersion: String by rootProject.extra
val commonsLangVersion: String by rootProject.extra
val jjwtVersion: String by rootProject.extra
val postgresqlVersion: String by rootProject.extra
val jspecifyVersion: String by rootProject.extra
val redissonVersion: String by rootProject.extra

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.redisson:redisson:$redissonVersion")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
    implementation("org.apache.commons:commons-lang3:$commonsLangVersion")
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")
    runtimeOnly("org.postgresql:postgresql:$postgresqlVersion")
    implementation("org.jspecify:jspecify:$jspecifyVersion")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
