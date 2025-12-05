pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        // Si necesitas resolver artefactos no disponibles en mavenCentral,
        // descomenta la siguiente línea para añadir JitPack temporalmente:
        // maven("https://jitpack.io")
    }
}

rootProject.name = "AppIOT1.2"
include(":app")
