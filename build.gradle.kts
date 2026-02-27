import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import xyz.jpenilla.resourcefactory.bukkit.Permission
import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.1"
}

group = "dev.lyphium"
version = "1.5.0"
description = "Simple Easter egg hunt plugin"

repositories {
    mavenCentral()
    maven {
        name = "papi-repo"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.12.2")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")
}

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    compileJava {
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release = 21
    }
}

paperPluginYaml {
    main = "dev.lyphium.egghunt.EggHunt"
    load = BukkitPluginYaml.PluginLoadOrder.POSTWORLD
    apiVersion = "1.21.10"
    author = "Lyphion"
    website = "https://github.com/Lyphion/EggHunt"
    dependencies.server.register("PlaceholderAPI") {
        load = PaperPluginYaml.Load.BEFORE
        required = false
    }
    permissions {
        register("egghunt.admin") {
            description = "Admin permission"
            default = Permission.Default.OP
            children("egghunt.configure", "egghunt.find", "egghunt.leaderboard",
                "egghunt.spawn", "egghunt.toggle")
        }
        register("egghunt.configure") {
            description = "Permission to configure eggs and drops"
            default = Permission.Default.OP
        }
        register("egghunt.find") {
            description = "Permission to easily find eggs"
            default = Permission.Default.OP
        }
        register("egghunt.leaderboard") {
            description = "Permission to view leaderboard"
            default = Permission.Default.NOT_OP
        }
        register("egghunt.spawn") {
            description = "Permission to spawn eggs"
            default = Permission.Default.OP
        }
        register("egghunt.toggle") {
            description = "Permission to toggle the spawning of eggs"
            default = Permission.Default.OP
        }
    }
}

