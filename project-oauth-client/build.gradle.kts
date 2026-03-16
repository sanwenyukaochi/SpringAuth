val jspecifyVersion: String by rootProject.extra

dependencies {
    implementation("org.springframework:spring-core")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    implementation("org.jspecify:jspecify:$jspecifyVersion")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
