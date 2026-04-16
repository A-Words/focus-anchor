pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "focus-anchor"

include(":app")
include(":core:model")
include(":core:data")
include(":core:designsystem")
include(":feature:focus")
include(":feature:inbox")
include(":feature:summary")
include(":feature:history")

