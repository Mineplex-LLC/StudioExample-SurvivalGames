rootProject.name = "survivalgames"

pluginManagement {
    val process =
        ProcessBuilder()
            .command(
                "aws",
                "codeartifact",
                "get-authorization-token",
                "--domain",
                "mineplex",
                "--domain-owner",
                "003539567218",
                "--query",
                "authorizationToken",
                "--output",
                "text",
                "--profile",
                "mineplex-partner",
            )
            .start()
    process.waitFor(60, TimeUnit.SECONDS)
    val codeartifactToken = process.inputStream.bufferedReader().readText()

    if (process.exitValue() != 0) {
        throw RuntimeException("Failed to retrieve CodeArtifact token.")
    }

    repositories {
        maven {
            name = "mineplex-studio-partners"
            url =
                uri("https://mineplex-003539567218.d.codeartifact.us-east-1.amazonaws.com/maven/mineplex-studio-partners/")
            credentials {
                username = "aws"
                password = codeartifactToken
            }
        }
        mavenLocal()
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}
