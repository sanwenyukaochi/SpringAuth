import com.diffplug.gradle.spotless.SpotlessExtension
import com.gorylenko.GitPropertiesPluginExtension
import org.springframework.boot.gradle.dsl.SpringBootExtension

plugins {
    java
    id("org.springframework.boot") version "4.0.3" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.gorylenko.gradle-git-properties") version "2.5.7" apply false
    id("com.diffplug.spotless") version "8.3.0" apply false
    id("checkstyle")
}

extra["springdocVersion"] = "3.0.2"
extra["commonsLangVersion"] = "3.20.0"
extra["jjwtVersion"] = "0.13.0"
extra["postgresqlVersion"] = "42.7.10"
extra["jspecifyVersion"] = "1.0.0"
extra["redissonVersion"] = "4.3.0"

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.gorylenko.gradle-git-properties")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "checkstyle")

    pluginManager.withPlugin("org.springframework.boot") {
        extensions.configure<SpringBootExtension> {
            buildInfo()
        }
    }

    pluginManager.withPlugin("com.gorylenko.gradle-git-properties") {
        extensions.configure<GitPropertiesPluginExtension> {
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
    }

    pluginManager.withPlugin("com.diffplug.spotless") {
        extensions.configure<SpotlessExtension> {
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
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.named("compileJava") {
        dependsOn(tasks.named("spotlessCheck"))
    }

    checkstyle {
        toolVersion = "13.3.0"
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        configProperties =
            mapOf(
                "baseDir" to rootProject.projectDir,
            )
        isIgnoreFailures = true
    }

    tasks.withType<Checkstyle>().configureEach {
        val checkstyleConfigFile = rootProject.file("config/checkstyle/checkstyle.xml")
        enabled = checkstyleConfigFile.exists()
        if (!enabled) {
            logger.warn(
                "Checkstyle config file not found at {}, skipping checkstyle tasks",
                checkstyleConfigFile.absolutePath,
            )
        }
    }
}
