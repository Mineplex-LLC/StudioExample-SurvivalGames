import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    java
    `java-library`
    id("com.mineplex.sdk.plugin") version "1.0.1"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("com.diffplug.spotless") version "6.25.0"
}

repositories {
    mavenLocal()
}

group = "com.mineplex.studio.example.survivalgames"
version = "1.0.0"
description = "SurvivalGames"

tasks {
    build {
        dependsOn(named("generatePaperPluginDescription"))
    }
}

paper {
    name = "SurvivalGames"
    version = project.version.toString()
    main = "com.mineplex.studio.example.survivalgames.SurvivalGamesPlugin"
    apiVersion = "1.20"

    serverDependencies {
        register("StudioEngine") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
    }
}

spotless {
    java {
        importOrder()
        removeUnusedImports()
        cleanthat()
        palantirJavaFormat()
        formatAnnotations()
    }

    kotlinGradle {
        ktlint()
    }

    yaml {
        target("*.yaml")
        jackson()
    }
}
