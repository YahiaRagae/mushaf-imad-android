package com.mushafimad.app

import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Replaces the deleted `consumer-check` module's PublishedArtifactTest.
 *
 * This app's own AndroidManifest.xml declares nothing beyond INTERNET and
 * ACCESS_NETWORK_STATE (enforced by [AppManifestPermissionsTest], a plain unit
 * test that parses the checked-in manifest source directly). So if any of the
 * permissions below show up in the installed package's merged manifest, they
 * can only have come from the library's own AAR manifest via manifest merger.
 *
 * Every assertion here corresponds to a bug that shipped in 0.2.1 because the
 * old bundled sample app declared these permissions ITSELF, masking the fact
 * that the library's AAR never declared them. Fixed in mushaf-core /
 * mushaf-ui 0.2.2, which now declare these permissions themselves.
 */
@RunWith(AndroidJUnit4::class)
class LibraryManifestContractTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    private val declaredPermissions: Set<String>
        get() = context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions
            .orEmpty()
            .toSet()

    /**
     * 0.2.1 shipped without WAKE_LOCK, yet AudioPlaybackService sets
     * WAKE_MODE_NETWORK on the player. Consumers died with a SecurityException
     * the moment audio started.
     */
    @Test
    fun libraryProvidesTheWakeLockItNeedsForAudio() {
        assertTrue(
            "expected WAKE_LOCK to be merged in from the library's AAR manifest, got $declaredPermissions",
            declaredPermissions.contains(android.Manifest.permission.WAKE_LOCK)
        )
    }

    /** The other permissions the library's foreground media service needs. */
    @Test
    fun libraryProvidesItsForegroundMediaServicePermissions() {
        assertTrue(declaredPermissions.contains("android.permission.FOREGROUND_SERVICE"))
        assertTrue(declaredPermissions.contains("android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"))
    }

    @Test
    fun libraryProvidesThePostNotificationsPermission() {
        assertTrue(declaredPermissions.contains("android.permission.POST_NOTIFICATIONS"))
    }
}
