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
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
//    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // RCON
    implementation("com.github.WarningImHack3r:minecraft-rcon:1.0.0")
    // Query protocol
    implementation("com.github.tekgator:JAVA-QueryMinecraftServer:1.2") {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }
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
