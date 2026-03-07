plugins {
    java
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.gorylenko.gradle-git-properties") version "2.5.7"
    id("com.diffplug.spotless") version "8.3.0"
}
group = "com.spring.security"
version = "0.0.1-SNAPSHOT"
description = "project-security"

val springCloudVersion by extra("2025.1.0")
val springdocVersion by extra("3.0.2")
val commonsLangVersion by extra("3.20.0")
val jjwtVersion by extra("0.13.0")
val postgresqlVersion by extra("42.7.10")
val hutoolVersion by extra("5.8.41")
val jspecifyVersion by extra("1.0.0")
val flywayVersion by extra("12.0.3")
val lombokVersion by extra("1.18.42")
val cosidVersion by extra("3.0.3")
val redissonVersion by extra("4.3.0")
val jooqVersion by extra("3.20.10")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.redisson:redisson:$redissonVersion")
    implementation("me.ahoo.cosid:cosid-spring-boot-starter:$cosidVersion")
    implementation("me.ahoo.cosid:cosid-spring-redis:$cosidVersion")
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

springBoot {
    buildInfo()
}

gitProperties {
    failOnNoGitDirectory = false
    keys =
        listOf(
            "git.branch",
            "git.build.host",
            "git.build.user.email",
            "git.build.user.name",
            "git.build.version",
            "git.closest.tag.commit.count",
            "git.closest.tag.name",
            "git.commit.id",
            "git.commit.id.abbrev",
            "git.commit.id.describe",
            "git.commit.message.full",
            "git.commit.message.short",
            "git.commit.time",
            "git.commit.user.email",
            "git.commit.user.name",
            "git.dirty",
            "git.remote.origin.url",
            "git.tags",
            "git.total.commit.count",
        )
}

spotless {
    encoding("UTF-8")
    java {
        palantirJavaFormat()
        importOrder()
        removeUnusedImports()
        formatAnnotations()
        trimTrailingWhitespace()
        endWithNewline()
        toggleOffOn()
    }

    kotlin {
        ktlint()
    }

    kotlinGradle {
        ktlint()
    }

    format("misc") {
        target(
            "**/*.md",
            "**/*.properties",
            "**/*.yml",
            "**/*.yaml",
            "**/*.sh",
            "**/.gitignore",
        )
        targetExclude("**/build/**", "**/build-*/**")
        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
        endWithNewline()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("compileJava") {
    dependsOn(tasks.named("spotlessCheck"))
}
