val springdocVersion: String by rootProject.extra
val commonsLangVersion: String by rootProject.extra

val jspecifyVersion: String by rootProject.extra

dependencies {
    implementation("org.springframework:spring-core")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
    implementation("org.springframework.security:spring-security-oauth2-authorization-server")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
    implementation("org.apache.commons:commons-lang3:$commonsLangVersion")

    implementation("org.jspecify:jspecify:$jspecifyVersion")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
