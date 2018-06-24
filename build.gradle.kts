import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    var kotlinVersion: String by extra
    kotlinVersion = "1.2.30"

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlinModule("gradle-plugin", kotlinVersion))
    }
}

group = "me.sedlar"
version = "1.0-SNAPSHOT"

apply {
    plugin("java")
    plugin("kotlin")
}

val kotlinVersion: String by extra

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlinModule("stdlib-jdk8", kotlinVersion))
    testCompile("junit", "junit", "4.12")
    compile("org.ow2.asm", "asm-all", "6.0_BETA")
    compile("org.jgrapht", "jgrapht-core", "1.2.0")
    compile("org.jgrapht", "jgrapht-ext", "1.2.0")
    compile("org.jgrapht", "jgrapht-io", "1.2.0")
    compile("jgraph", "jgraph", "5.13.0.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}