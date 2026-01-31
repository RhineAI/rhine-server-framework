plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
    id("java-library")
    id("maven-publish")
}

group = "com.rhine"
version = "0.0.1-SNAPSHOT"
description = "rhine-server-framework"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // removed explicit Spring 5.x dependencies; rely on Spring Boot managed versions
    // Web base (RestControllerAdvice, ResponseEntity, etc.)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Redis, Hutool, Aliyun and Gson
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("cn.hutool:hutool-all:5.8.26")
    implementation("com.aliyun.oss:aliyun-sdk-oss:3.18.1")
    implementation("com.aliyun:alibabacloud-sts20150401:1.0.6")
    implementation("com.aliyun:alibabacloud-dysmsapi20170525:3.0.3")
    implementation("com.google.code.gson:gson:2.10.1")

    // AOP support
    implementation("org.springframework.boot:spring-boot-starter-aop")
    // Security support (for SecurityContextHolder)
    implementation("org.springframework.boot:spring-boot-starter-security")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.13.13")
    // Embedded H2 database for JPA tests
    testImplementation("com.h2database:h2")

    // OpenAPI models for compile-time; runtime provided by services using springdoc
    compileOnly("io.swagger.core.v3:swagger-models:2.2.21")
}

// Build as library: enable jar, disable bootJar
tasks.named<org.gradle.jvm.tasks.Jar>("jar") { enabled = true }
tasks.named("bootJar") { enabled = false }

publishing {
    publications {
        create("mavenJava", org.gradle.api.publish.maven.MavenPublication::class) {
            from(components["java"])
            groupId = group.toString()
            artifactId = "rhine-server-framework"
            version = project.version.toString()
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}