import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    alias(libs.plugins.catalog.updater)
    alias(libs.plugins.paperweight.userdev)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.plugin.yml.paper)
    alias(libs.plugins.shadow)
}

group = "com.gitlab.blockstack"
version = "3.9.30-flyncodes"
description = "Make pixel art!"

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://mvn.wesjd.net/")
    maven("https://repo.glaremasters.me/repository/towny/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    paperweight.paperDevBundle(libs.versions.minecraft.get() + ".build.+")

    implementation(libs.anvilgui)

    compileOnly(libs.towny)
    compileOnly(libs.worldguard.bukkit) {
        exclude(group = "org.bukkit")
        exclude(group = "org.bstats", module = "bstats-bukkit")
    }
    compileOnly(libs.worldedit.bukkit) {
        exclude(group = "org.bukkit")
        exclude(group = "org.bstats", module = "bstats-bukkit")
    }
    compileOnly(files("ProtocolLib.jar"))
}

val targetJavaVersion = libs.versions.jdk.get().toInt()
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

tasks {
    runServer {
        minecraftVersion(libs.versions.minecraft.get())
        systemProperty("net.kyori.adventure.text.warnWhenLegacyFormattingDetected", false)
    }

    compileJava {
        dependsOn(versionCatalogUpdate)
        options.encoding = Charsets.UTF_8.name()
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible()) {
            options.release.set(targetJavaVersion)
        }
    }

    javadoc {
        (options as StandardJavadocDocletOptions).encoding = Charsets.UTF_8.name()
    }

    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("net.wesjd.anvilgui", "me.fupery.shade")
        dependencies {
            include(dependency("com.github.Fupery:.*"))
            include(dependency("me.Fupery:DataTables:.*"))
            include(dependency("net.wesjd:anvilgui:.*"))
            include(dependency("com.gitlab.blockstack:.*"))
        }
    }

    assemble {
        dependsOn(shadowJar)
    }
}

paper {
    name = project.name
    version = project.version.toString()
    main = "me.Fupery.ArtMap.ArtMap"
    apiVersion = libs.versions.minecraft.get()
    description = project.description
    authors = listOf("Wispoffates", "(Original) Fupery")
    serverDependencies {
        register("ProtocolLib") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
        }
        register("WorldGuard") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("Towny") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
    }
    //commands {
    //    register("art") {
    //        aliases = listOf("artmap")
    //        usage = ["<command> save <title> : Save your artwork", "<command> clear : Reset the artwork on an easel", "<command> delete <title> : Remove your artwork", "<command> preview <title> : Preview an artwork", "<command> help : Display help", "<command> reload : Reloads the config", "<command> give <player> <easel|canvas|artwork:<title>> <amount> : Give a player ArtMap item(s)"]
    //    }
    //}
    permissions {
        register("artmap.region.member") {
            description = "Allows players to place/break easels in regions that they are a member of, but DO NOT own."
            default = BukkitPluginDescription.Permission.Default.OP // TRUE, FALSE, OP or NOT_OP
        }
        register("artmap.menu") {
            description = "Allows players to open the ArtMap menu"
            default = BukkitPluginDescription.Permission.Default.TRUE // TRUE, FALSE, OP or NOT_OP
        }
        register("artmap.artist") {
            description = "Allows players to paint, and place/break easels in any region they own, or are a member of."
            childrenMap = mapOf("artmap.region.member" to true)
            default = BukkitPluginDescription.Permission.Default.OP // TRUE, FALSE, OP or NOT_OP
        }
        register("artmap.artkit") {
            description = "Gives players a kit of art supplies while they use an easel"
            default = BukkitPluginDescription.Permission.Default.OP // TRUE, FALSE, OP or NOT_OP
        }
        register("artmap.admin") {
        	description = "Allows administrative commands"
            childrenMap = mapOf("artmap.artist" to true, "artmap.artkit" to true)
            default = BukkitPluginDescription.Permission.Default.OP // TRUE, FALSE, OP or NOT_OP
        }
        register("artmap.prevent.afk") {
            description = "Player will not be kicked for being AFK at easel. Supports Eseentials and CMI."
            default = BukkitPluginDescription.Permission.Default.FALSE // TRUE, FALSE, OP or NOT_OP
        }
    }
}