plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.spring)
    application
    alias(libs.plugins.git.versioning)
    alias(libs.plugins.gradle.git.properties)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    id("maven-publish")
    id("jacoco")
    alias(libs.plugins.ktlint)
}

group = "io.github.alkoleft"
version = "0.2.2-SNAPSHOT"

gitVersioning.apply {
    refs {
        considerTagsOnBranches = true
        tag("v(?<tagVersion>[0-9].*)") {
            version = "\${ref.tagVersion}\${dirty}"
        }
        branch(".+") {
            version = "\${ref}-\${commit.short}\${dirty}"
        }
    }

    rev {
        version = "\${commit.short}\${dirty}"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-java-parameters", "-Xemit-jvm-type-annotations")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven(url = "https://jitpack.io")
}

dependencies {
    // Kotlin Standard Library
    implementation(libs.bundles.kotlin)
    implementation("org.apache.commons", "commons-compress", "1.27.1")
    implementation("com.mohamedrejeb.ksoup:ksoup-html:0.6.0")

    // Spring Boot with Kotlin
    implementation(libs.bundles.spring.boot)

    // Spring AI MCP Server
    implementation(libs.spring.ai.starter.mcp.server)

    // JSON/XML with Kotlin support
    implementation(libs.bundles.jackson)

    // Logging
    implementation(libs.bundles.logging)

    // Reactor Core для Spring AI MCP
    implementation(libs.reactor.core)

    // Tests
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.bundles.junit)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.assertj.core)
    testImplementation(libs.slf4j.log4j12)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${libs.versions.springAi.get()}")
    }
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed", "standard_error")
    }
}

tasks.jar {
    enabled = false
    archiveClassifier.set("plain")
}

tasks.bootJar {
    enabled = true
    archiveClassifier.set("")
    mainClass.set("ru.alkoleft.context.McpServerApplicationKt")
}

// Исправление зависимостей для задач распространения
tasks.named("bootDistZip") {
    dependsOn("bootJar")
}

tasks.named("bootDistTar") {
    dependsOn("bootJar")
}

tasks.named("bootStartScripts") {
    dependsOn("bootJar")
}

tasks.named("startScripts") {
    dependsOn("bootJar")
}

publishing {
    repositories {
        maven {
            name = "mcp-bsl-context"
            url = uri("https://maven.pkg.github.com/alkoleft/mcp-bsl-context")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}

// Настройка JaCoCo для генерации отчёта покрытия тестов
jacoco {
    toolVersion = libs.versions.jacoco.get()
}

ktlint {
    version = libs.versions.ktlint.get()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}
