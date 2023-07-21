// ### Project Information #############################################################################################
private class ProjectInfo { // TODO change project info
    val longName: String = "Chess Statistics Service"
    val description: String = "The service which handles the user scores in the application."

    val repositoryOwner: String = "ldss-project"
    val repositoryName: String = "statistics-service"

    val artifactGroup: String = "io.github.jahrim.chess"
    val artifactId: String = project.name
    val implementationClass: String = "io.github.jahrim.chess.statistics.service.main.main"

    val license = "The MIT License"
    val licenseUrl = "https://opensource.org/licenses/MIT"

    val website = "https://github.com/$repositoryOwner/$repositoryName"
    val tags = listOf("scala3", "chess", "statistics", "scores", "leaderboard")
}
private val projectInfo: ProjectInfo = ProjectInfo()

// ### Build Configuration #############################################################################################
plugins {
    with(libs.plugins){
        `java-library`
        scala
        application
        alias(spotless)
        alias(wartremover)
        alias(git.semantic.versioning)
        alias(publish.on.central)
    }
}

repositories { mavenCentral() }

dependencies {
    compileOnly(libs.bundles.scalafmt)
    implementation(libs.scala)
    implementation(libs.scallop)
    implementation(libs.hexarc)
    implementation(libs.vertx.web)
    testImplementation(libs.scalatest)
    testImplementation(libs.scalatestplusjunit)
}

application {
    mainClass.set(projectInfo.implementationClass)

    val httpHost: String? by project
    val httpPort: String? by project
    val mongoDBConnection: String? by project
    val mongoDBDatabase: String? by project
    val mongoDBCollection: String? by project
    tasks.withType(JavaExec::class.java){
        httpHost?.apply { args("--http-host", this) }
        httpPort?.apply { args("--http-port", this) }
        mongoDBConnection?.apply { args("--mongodb-connection", this) }
        mongoDBDatabase?.apply { args("--mongodb-database", this) }
        mongoDBCollection?.apply { args("--mongodb-collection", this) }
    }
}

spotless {
    isEnforceCheck = false
    scala { scalafmt(libs.versions.scalafmt.version.get()).configFile(".scalafmt.conf") }
    tasks.compileScala.get().dependsOn(tasks.spotlessApply)
}

// ### Publishing ######################################################################################################
group = projectInfo.artifactGroup
gitSemVer {
    buildMetadataSeparator.set("-")
    assignGitSemanticVersion()
}

tasks.javadocJar {
    dependsOn(tasks.scaladoc)
    from(tasks.scaladoc.get().destinationDir)
}

publishOnCentral {
    configureMavenCentral.set(true)
    projectDescription.set(projectInfo.description)
    projectLongName.set(projectInfo.longName)
    licenseName.set(projectInfo.license)
    licenseUrl.set(projectInfo.licenseUrl)
    repoOwner.set(projectInfo.repositoryOwner)
    projectUrl.set(projectInfo.website)
    scmConnection.set("scm:git:$projectUrl")
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                developers {
                    developer {
                        name.set("Jahrim Gabriele Cesario")
                        email.set("jahrim.cesario2@studio.unibo.it")
                        url.set("https://jahrim.github.io")
                    }
                }
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
}