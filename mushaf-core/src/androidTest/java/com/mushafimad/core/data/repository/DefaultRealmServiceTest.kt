package com.mushafimad.core.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.data.local.entities.LastReadPositionEntity
import io.realm.kotlin.ext.query
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression tests for the Realm lifecycle.
 *
 * Covers the two QA-reported production bugs:
 * - RLM_ERR_MISMATCHED_CONFIG crash on a second app run
 * - user data (last read position etc.) wiped on every launch
 */
@RunWith(AndroidJUnit4::class)
class DefaultRealmServiceTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun freshInstall() {
        // Remove every realm artifact so each test starts like a fresh install
        context.filesDir.listFiles()
            ?.filter { it.name.startsWith("quran") || it.name.startsWith("userdata") }
            ?.forEach { it.deleteRecursively() }
    }

    @Test
    fun freshInstall_opensContentRealm_withQuranData() = runBlocking {
        val service = DefaultRealmService(context)
        service.initialize()

        assertThat(service.isInitialized).isTrue()
        assertThat(service.fetchAllChaptersAsync()).hasSize(114)
        assertThat(service.getVersesForPage(1)).isNotEmpty()

        service.close()
    }

    @Test
    fun restart_reopensWithoutCrash_andUserDataSurvives() = runBlocking {
        // First launch: open and store a last-read position
        val firstRun = DefaultRealmService(context)
        firstRun.initialize()
        firstRun.getUserRealm().write {
            copyToRealm(LastReadPositionEntity().apply {
                mushafType = "HAFS_1441"
                pageNumber = 250
                chapterNumber = 18
                lastReadAt = 1234L
            })
        }
        firstRun.close()

        // Second launch over the same files: this is the exact scenario that
        // crashed v0.1 with RLM_ERR_MISMATCHED_CONFIG and lost user data.
        val secondRun = DefaultRealmService(context)
        secondRun.initialize()

        assertThat(secondRun.fetchAllChaptersAsync()).hasSize(114)
        val saved = secondRun.getUserRealm()
            .query<LastReadPositionEntity>("mushafType == $0", "HAFS_1441")
            .first()
            .find()
        assertThat(saved).isNotNull()
        assertThat(saved!!.pageNumber).isEqualTo(250)
        assertThat(saved.chapterNumber).isEqualTo(18)

        secondRun.close()
    }

    @Test
    fun twoInstances_openConcurrently_withoutCrash() = runBlocking {
        // v0.1 created two service instances at startup (ServiceRegistry +
        // Koin); each deleted and recopied the database under the other.
        val a = DefaultRealmService(context)
        val b = DefaultRealmService(context)
        a.initialize()
        b.initialize()

        assertThat(a.fetchAllChaptersAsync()).hasSize(114)
        assertThat(b.fetchAllChaptersAsync()).hasSize(114)

        a.close()
        b.close()
    }

    @Test
    fun corruptContentRealm_selfHealsFromAssets() = runBlocking {
        // Open once so the on-device copy exists, then corrupt it
        val firstRun = DefaultRealmService(context)
        firstRun.initialize()
        firstRun.close()

        val contentFile = context.filesDir.listFiles()!!
            .first { it.name.startsWith("quran-v") && it.name.endsWith(".realm") }
        contentFile.writeBytes(ByteArray(1024) { 0x42 })

        val secondRun = DefaultRealmService(context)
        secondRun.initialize()
        assertThat(secondRun.fetchAllChaptersAsync()).hasSize(114)

        secondRun.close()
    }

    @Test
    fun legacyQuranRealmFile_isCleanedUp() = runBlocking {
        // Simulate leftovers from a v0.1 install
        java.io.File(context.filesDir, "quran.realm").writeBytes(ByteArray(16))
        java.io.File(context.filesDir, "quran.realm.lock").writeBytes(ByteArray(4))

        val service = DefaultRealmService(context)
        service.initialize()

        assertThat(java.io.File(context.filesDir, "quran.realm").exists()).isFalse()
        assertThat(service.fetchAllChaptersAsync()).hasSize(114)

        service.close()
    }
}
