plugins {

}

allprojects {
	group = "ru.nemodev.platform"
	version = "1.0.0"

	project.extra["publish-repo-url"] = System.getenv("NEXUS_URL") ?: ""
	project.extra["publish-repo-user"] = System.getenv("NEXUS_USERNAME") ?: ""
	project.extra["publish-repo-password"] = System.getenv("NEXUS_PASSWORD") ?: ""
}

subprojects {

}
