plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("kapt") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    application
    id("me.qoomon.git-versioning") version "6.4.4"
    id("com.gorylenko.gradle-git-properties") version "2.4.2"
    id("io.freefair.lombok") version "8.11"
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("maven-publish")
    id("jacoco")
}

group = "io.github.alkoleft"
version = "0.2.0-SNAPSHOT"

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

repositories {
    mavenCentral()
    mavenLocal()
    maven(url = "https://jitpack.io")
}

extra["springAiVersion"] = "1.0.0"

val JACKSON_VERSION = "2.15.2"

dependencies {
    // Kotlin Standard Library
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Spring Boot with Kotlin
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    
    // Spring AI MCP Server
    implementation("org.springframework.ai:spring-ai-starter-mcp-server")

    // HBK  
    implementation("com.github._1c_syntax.bsl:bsl-context:1.0-SNAPSHOT")

    // JSON/XML with Kotlin support
    implementation("com.fasterxml.jackson.core:jackson-core:$JACKSON_VERSION")
    implementation("com.fasterxml.jackson.core:jackson-databind:$JACKSON_VERSION")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$JACKSON_VERSION")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$JACKSON_VERSION")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$JACKSON_VERSION")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("org.codehaus.janino:janino:3.1.12")
    
    // Reactor Core для Spring AI MCP
    implementation("io.projectreactor:reactor-core:3.6.11")

    // ANTLR - принудительное управление версиями для исправления несоответствия
    implementation("org.antlr:antlr4-runtime:4.9.3")
    implementation("org.antlr:antlr4:4.9.3")

    // Lombok for Kotlin
    kapt("org.projectlombok:lombok:1.18.30")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.slf4j", "slf4j-log4j12", "1.7.30")
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.8.0")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
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
    mainClass.set("ru.alkoleft.context.platform.McpServerApplication")
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
            name = "monaco-bsl-context"
            url = uri("https://maven.pkg.github.com/alkoleft/monaco-bsl-context")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
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
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}
