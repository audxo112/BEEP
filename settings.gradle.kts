@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
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
        maven("https://naver.jfrog.io/artifactory/maven/")
        maven("https://devrepo.kakao.com/nexus/content/groups/public/")
    }
}

buildCache {
    local {
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}
rootProject.name = "BEEP"

include(":app")
include(":core:common")
include(":core:ui")
include(":model")
include(":theme")
include(":domain")
include(":data:data")
include(":data:remote")
include(":data:local")
include(":library:barcode")
include(":library:permission")
include(":library:recognizer")
include(":auth:auth")
include(":auth:google")
include(":auth:kakao")
include(":auth:naver")
// include(":ui:dialog:confirmation")
// include(":ui:dialog:datepicker")
// include(":ui:dialog:originimage")
// include(":ui:dialog:progress")
include(":ui:designsystem:dotindicator")
include(":ui:feature:login")
include(":ui:feature:main")

// includeProject(":common")
// includeProject(":common-android")
// includeProject(":worker")
// includeProject(":utils-location", "utils")
// includeProject(":utils-recognizer", "utils")
// includeProject(":data", "data")
// includeProject(":data-database", "data")
// includeProject(":data-remote", "data")
// includeProject(":data-preference", "data")
// includeProject(":domain")
// includeProject(":nav-app", "navs")
// includeProject(":nav-main", "navs")
// includeProject(":ui-coffee", "features")
// includeProject(":ui-common", "features")
// includeProject(":ui-intro", "features")
// includeProject(":ui-main", "features")
// includeProject(":ui-gifticonlist", "features")
// includeProject(":ui-home", "features")
// includeProject(":ui-opensourcelicense", "features")
// includeProject(":ui-personalinfopolicy", "features")
// includeProject(":ui-security", "features")
// includeProject(":ui-setting", "features")
// includeProject(":ui-termsofuse", "features")
// includeProject(":ui-usedgifticon", "features")
// includeProject(":ciphertool", "libs")
