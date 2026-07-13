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

        // The `published` flavour of :app resolves the library by coordinate, the way
        // any third party does. mavenLocal is listed first so that before a release
        // `publishToMavenLocal` supplies the version under test (the packaging gate);
        // after a release the identical coordinate resolves from JitPack (the publish
        // proof). Both are restricted to the library's own group, so nothing else in
        // the build can be served from them.
        mavenLocal {
            content { includeGroup("com.github.YahiaRagae.mushaf-imad-android") }
        }
        maven("https://jitpack.io") {
            content { includeGroup("com.github.YahiaRagae.mushaf-imad-android") }
        }
    }
}

rootProject.name = "MushafImad"
include(":mushaf-core")     // Core data layer (no Compose)
include(":mushaf-ui")       // UI components (depends on mushaf-core)
include(":app")             // The Quran app - builds against the library from source OR from the published artifact
