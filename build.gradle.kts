plugins {

}

allprojects {
	group = "ru.nemodev.platform"
	version = "1.0.0"

	project.extra["publishing-repo-url"] = System.getenv("NEXUS_URL") ?: ""
}

subprojects {

}
