import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("kapt") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "12.0.3"
    id("org.jooq.jooq-codegen-gradle") version "3.20.11"
    id("org.springframework.boot") version "3.5.11"
}

group = "com.king250.order"
version = "1.0.0"
description = "SpringMyOrder"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("commons-codec:commons-codec")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.ktor:ktor-client-core:3.4.1")
    implementation("io.ktor:ktor-client-okhttp:3.4.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.1")
    implementation("io.ktor:ktor-serialization-jackson:3.4.1")
    implementation("io.ktor:ktor-client-logging:3.4.1")
    implementation("io.projectreactor:reactor-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jooq:jooq:3.20.11")
    implementation("org.mapstruct:mapstruct:1.6.3")
    implementation("org.openapitools:jackson-databind-nullable:0.2.9")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    runtimeOnly("org.postgresql:postgresql:42.7.10")
    jooqCodegen("org.postgresql:postgresql")
    kapt("org.mapstruct:mapstruct-processor:1.6.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:12.0.3")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

sourceSets {
    main {
        kotlin {
            srcDir("build/generated-sources/jooq")
        }
    }
}

jooq {
    configuration {
        jdbc {
            url = project.property("db_url") as String
            user = project.property("db_username") as String
            password = project.property("db_password") as String
        }
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            target {
                packageName = "com.king250.order.jooq"
            }
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "public"
                excludes = "flyway_schema_history"
                forcedTypes {
                    forcedType {
                        name = "INSTANT"
                        includeTypes = "TIMESTAMPTZ"
                    }
                }
            }
        }
    }
}

flyway {
    url = project.property("db_url") as String
    user = project.property("db_username") as String
    password = project.property("db_password") as String
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn("jooqCodegen")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
