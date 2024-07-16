import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("maven-publish")

    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.plugin.spring)
}

val artifactName = "core-environment"
java.sourceCompatibility = JavaVersion.VERSION_21

dependencies {
    // base platform libs

    // spring
    api(libs.spring.boot.starter)
    kapt(libs.spring.boot.configuration.processor)

    // kotlin
    api(libs.bundles.kotlin)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<Jar>("jar") {
    enabled = true
    archiveClassifier = ""
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks {
    val sourcesJar by creating(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    artifacts {
        add("archives", sourcesJar)
    }
}

publishing {
    repositories {
        maven {
            url = uri(project.extra["publish-repo-url"] as String)
            credentials {
                username = project.extra["publish-repo-user"] as String
                password = project.extra["publish-repo-password"] as String
            }
        }
    }
    publications {

        create<MavenPublication>("maven") {
            artifactId = artifactName

            versionMapping {
                usage("kotlin-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("kotlin-runtime") {
                    fromResolutionResult()
                }
            }

            if (!System.getenv("RELEASE_VERSION").isNullOrBlank()) {
                version = System.getenv("RELEASE_VERSION")
            }

            from(components["java"])
            artifact(tasks["sourcesJar"])
        }
    }
}
