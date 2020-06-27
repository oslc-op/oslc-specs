import org.gradle.jvm.tasks.Jar

plugins {
    java
    application
    eclipse
}

version = "0.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    sourceSets {
	    main {
	        java {
	            setSrcDirs(listOf("src/main/java"))
	        }
	        resources {
	            setSrcDirs(listOf("src/main/resources","src/main/generated"))
	        }
	    }
	}
}

application {
    mainClassName = "net.open_services.scheck.shapechecker.Main"
}

dependencies {
    implementation("org.apache.jena:jena-core:3.+")
    implementation("org.apache.jena:jena-arq:3.+")
    implementation("com.google.code.findbugs:annotations:3.+")
    implementation("org.slf4j:slf4j-simple:1.7.+")

    testImplementation("junit:junit:4.12")
}


repositories {
    jcenter()
}

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}-all"
    manifest {
        attributes["Implementation-Title"] = "ShapeChecker"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "net.open_services.scheck.shapechecker.Main"
    }
    from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}
