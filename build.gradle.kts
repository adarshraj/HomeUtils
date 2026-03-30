plugins {
    java
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "in.adars"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven {
        url = uri("file://${rootProject.projectDir}/libs/repo")
        name = "localLibs"
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.webjars:bootstrap:5.3.3")
    implementation("org.webjars:webjars-locator-core:0.59")

    // HEIC decoding
    implementation("com.openize:openize-heic:26.3")

    // PDF generation
    implementation("org.apache.pdfbox:pdfbox:3.0.3")

    // WebP encoding
    implementation("org.sejda.imageio:webp-imageio:0.1.6")

    // Password-protected ZIP
    implementation("net.lingala.zip4j:zip4j:2.11.5")

    // QR code generation
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")

    // Diff computation
    implementation("io.github.java-diff-utils:java-diff-utils:4.12")

    // Cron human-readable descriptions
    implementation("com.cronutils:cron-utils:9.2.1")

    // YAML support
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
