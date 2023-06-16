import org.gradle.api.internal.file.copy.DefaultCopySpec
import org.gradle.internal.FileUtils
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("org.jetbrains.intellij") version "1.8.0"
}

group = "com.github.arc"
version = "0.31"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
dependencies {
    implementation("org.openjdk.nashorn:nashorn-core:15.4")
    implementation("com.github.Anuken.Arc:arc-core:v144")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    //    implementation("edu.brown.cs.burlap:arc-burlap:3.0.1")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.3")
//    type.set("IC") // Target IDE Platform
    type.set("IU") // Target IDE Platform

//    plugins.set(listOf(/* Plugin Dependencies */))
    plugins.set(

        listOf(
            "java",
//            "Pythonid:222.3345.118",//231.6890.12",
            "Kotlin",
//            "org.intellij.scala:2022.2.19",//2023.1.7",
//            "JavaScript",
//            //"CSS",
//            "Dart:222.4345.14",//231.6890.12",
//            "Groovy",
////            "properties",
//            "org.jetbrains.plugins.ruby:222.3345.118",//231.6890.12",
//            "com.jetbrains.php:222.3345.118",//231.6890.12",
////            "java-i18n",
////            "DatabaseTools",
//            "org.rust.lang:0.4.186.5143-222",//0.4.188.5205-231",
////            "org.toml.lang",
//            "org.jetbrains.plugins.go:222.3345.118",//231.6890.12",
////            "nl.rubensten.texifyidea:0.7.26"
        )
    )
}//com.zelaux.numberconverter
sourceSets {
    main {
        java {
            println("srcDirs_0: ${srcDirs.joinToString()}")
            srcDirs.clear()
            mutableSetOf("src/main/java","src/main/langExtra","src/main/kotlin").forEach {
                java.srcDir(it)

            }
            println("srcDirs_1: ${srcDirs}")
        }
    }
}
tasks {


    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"


        val compilerArgs = options.compilerArgs
        compilerArgs.addAll(listOf("--add-exports", "java.desktop/sun.awt.image=ALL-UNNAMED"))
//        compilerArgs.addAll(listOf("--add-exports/java.desktop/sun.awt.image=ALL-UNNAMED"))
//        compilerArgs.addAll(listOf("--add-exports java.desktop/sun.awt.image=ALL-UNNAMED"))
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"

//        compilerArgs.addAll(listOf("-opt-in","kotlin.RequiresOptIn"))
    }
    this.getByName("setupInstrumentCode").actions.clear();
    this.getByName("instrumentCode").actions.clear();
    this.getByName("postInstrumentCode").actions.clear();
    val t_tasks = this;
    this.getByName("setupDependencies").doFirst {
        t_tasks.jar {
//            from {
            /*configurations["compileClasspath"].map { if (it.isDirectory) it else zipTree(it) }
                .forEach { from(it) }*/
//            }
        }
//    this.getByName("jar").dependsOn.clear()
    };
    patchPluginXml {
        sinceBuild.set("222.3345.118")
        untilBuild.set("232")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.7"
}