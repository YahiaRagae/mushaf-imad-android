package com.mushafimad.app

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Guards against ever re-adding a library-owned permission (WAKE_LOCK,
 * FOREGROUND_SERVICE, FOREGROUND_SERVICE_MEDIA_PLAYBACK, POST_NOTIFICATIONS) to
 * this app's OWN manifest. The library declares those itself as of 0.2.2, and
 * the manifest merger silently pulls them in - a merged-manifest check alone
 * cannot tell "the app declared it" apart from "the library declared it", so
 * this test parses the checked-in source manifest directly instead.
 *
 * Re-adding one of those permissions here is exactly the WAKE_LOCK-workaround
 * mistake that let the library's own missing declaration go unnoticed in
 * 0.2.1: it must fail loudly, not silently mask a future library regression.
 */
class AppManifestPermissionsTest {

    @Test
    fun appManifestDeclaresOnlyInternetPermissions() {
        // Path is supplied by the build, which also declares the manifest as an input
        // to this task - otherwise Gradle would skip the test as UP-TO-DATE on the very
        // change it exists to catch. Falls back to the working directory for IDE runs.
        val manifestFile = System.getProperty("app.manifest.path")
            ?.let { File(it) }
            ?: File("src/main/AndroidManifest.xml")
        check(manifestFile.exists()) { "Expected to find ${manifestFile.absolutePath}" }

        val permissionRegex = Regex("""android:name="(android\.permission\.[A-Z_]+)"""")
        val declared = permissionRegex.findAll(manifestFile.readText())
            .map { it.groupValues[1] }
            .toSet()

        assertEquals(
            setOf(
                "android.permission.INTERNET",
                "android.permission.ACCESS_NETWORK_STATE"
            ),
            declared
        )
    }
}
