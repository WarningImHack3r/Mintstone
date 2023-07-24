import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spring)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
}

group = "com.warningimhack3r"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_20
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "20"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
