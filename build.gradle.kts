val baseVersion = providers.gradleProperty("version.base").get()
val betaVersion = providers.gradleProperty("version.beta").get().toBooleanStrict()
val betaString = if (betaVersion) "Beta-" else ""
val jenkinsBuild = providers.environmentVariable("BUILD_NUMBER").orElse("Unofficial").get()
rootProject.version = "${baseVersion}.${betaString}${jenkinsBuild}"

plugins {
    id("java")
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://nexus.sirblobman.xyz/public/")
    }

    // Each submodule may pin the Spigot API version it compiles against via its own
    // gradle.properties (version.spigot=...). Default to 1.8.8 so the aggregate plugin
    // stays compatible with 1.8.8 servers. Modules that call newer API must set their
    // own higher value and guard the calls at runtime.
    val moduleSpigotVersion: String =
        (findProperty("version.spigot") as String?) ?: "1.8.8-R0.1-SNAPSHOT"

    dependencies {
        compileOnly("org.jetbrains:annotations:26.0.2-1") // JetBrains Annotations
        compileOnly("org.spigotmc:spigot-api:$moduleSpigotVersion") // Base Spigot API
        compileOnly("com.github.sirblobman.api:core:2.9-SNAPSHOT") // BlueSlimeCore
    }

    // Spigot 1.8.8's pom references bungeecord-chat:1.8-SNAPSHOT which was purged from
    // the Spigot snapshots repo. Force a still-available version; the chat API is
    // backwards compatible so this is safe for 1.8.8 runtime.
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "net.md-5" && requested.name == "bungeecord-chat") {
                useVersion("1.16-R0.4")
                because("bungeecord-chat:1.8-SNAPSHOT no longer exists in any Maven repo")
            }
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:deprecation")
    }
}
