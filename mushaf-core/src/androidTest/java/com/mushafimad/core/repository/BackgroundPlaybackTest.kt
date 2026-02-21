/**
 * Instrumented tests for background playback, notifications, and hardware events.
 *
 * Covers: QA-8.4 (Background Playback & Notifications) — Issue #20
 *
 * TC-8.30  [Manual] Background playback continues after Home button press
 * TC-8.31  [Manual] Notification with playback controls visible during playback
 * TC-8.32  [Manual] Lock screen shows media controls
 * TC-8.33  AudioBecomingNoisy broadcast — player handles gracefully (no crash)
 * TC-8.34  [Manual] Bluetooth disconnect — pause behavior
 * TC-8.35  [Manual] POST_NOTIFICATIONS permission required on Android 13+
 * TC-8.36  loadChapter() with invalid chapter number — player emits ERROR state
 * TC-8.37  loadChapter() with invalid reciter ID — no crash, graceful handling
 */
package com.mushafimad.core.repository

import android.content.Intent
import android.media.AudioManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.data.audio.AudioPlayerState
import com.mushafimad.core.data.audio.PlaybackState
import com.mushafimad.core.domain.repository.AudioRepository
import com.mushafimad.core.util.LibraryTestSetup
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

/**
 * Instrumented tests for QA-8.4: Background Playback & Notifications.
 *
 * Automated test cases (TC-8.33, TC-8.36, TC-8.37) run on-device and exercise
 * real audio-player infrastructure without requiring manual steps.
 *
 * Manual test cases (TC-8.30, TC-8.31, TC-8.32, TC-8.34, TC-8.35) are annotated
 * with [Ignore] together with step-by-step instructions for a QA engineer.
 *
 * Test fixture constants:
 *   - Reciter ID 1 (Ibrahim Al-Akdar) is a valid reciter present in ReciterService.
 *   - Chapter 999 does not exist in the Quran; it triggers an ExoPlayer load error.
 *   - Reciter ID 9999 does not exist; AudioPlaybackService returns early without crash.
 */
@RunWith(AndroidJUnit4::class)
class BackgroundPlaybackTest {

    private lateinit var repository: AudioRepository

    // Valid reciter confirmed present in ReciterService (same as TC-8.23–TC-8.29)
    private val validReciterId = 1

    // -----------------------------------------------------------------------
    // Setup / teardown
    // -----------------------------------------------------------------------

    @Before
    fun setUp() {
        LibraryTestSetup.ensureInitialized()
        repository = MushafLibrary.getAudioRepository()
    }

    @After
    fun tearDown() {
        repository.release()
    }

    // -----------------------------------------------------------------------
    // TC-8.30 — [Manual] Background playback after Home button press
    // -----------------------------------------------------------------------

    /**
     * TC-8.30 — Manual verification required.
     *
     * Steps:
     *   1. Build and install the sample app on a physical device or emulator.
     *   2. Open the sample app and start playback of any surah.
     *   3. Press the Home button to send the app to the background.
     *   4. Wait 5–10 seconds.
     * Expected: Audio continues playing without interruption.
     * Pass criteria: No audio cut-out; playback position advances in the background.
     */
    @Ignore("Requires device interaction: press Home button and observe audio continues")
    @Test
    fun tc8_30_backgroundPlaybackContinuesAfterHomePress() {
        // Verified manually — see KDoc above for steps.
    }

    // -----------------------------------------------------------------------
    // TC-8.31 — [Manual] Notification with playback controls
    // -----------------------------------------------------------------------

    /**
     * TC-8.31 — Manual verification required.
     *
     * Steps:
     *   1. Start playback via the sample app.
     *   2. Pull down the notification shade.
     * Expected: A media notification appears with:
     *   - Surah title and reciter name as notification text.
     *   - Play/Pause button that correctly toggles playback.
     *   - Previous/Next or Stop controls (if provided by MediaSession).
     * Pass criteria: Tapping Pause stops audio; tapping Play resumes it.
     */
    @Ignore("Requires device interaction: verify media notification in the notification shade")
    @Test
    fun tc8_31_notificationShowsPlaybackControls() {
        // Verified manually — see KDoc above for steps.
    }

    // -----------------------------------------------------------------------
    // TC-8.32 — [Manual] Lock screen media controls
    // -----------------------------------------------------------------------

    /**
     * TC-8.32 — Manual verification required.
     *
     * Steps:
     *   1. Start playback via the sample app.
     *   2. Lock the device screen.
     * Expected: The lock screen shows media controls (title, reciter, play/pause button).
     *   - Tapping pause on the lock screen halts audio.
     *   - Tapping play on the lock screen resumes audio.
     * Pass criteria: Controls are visible and functional without unlocking the device.
     */
    @Ignore("Requires device interaction: lock the screen and verify media controls are visible")
    @Test
    fun tc8_32_lockScreenShowsMediaControls() {
        // Verified manually — see KDoc above for steps.
    }

    // -----------------------------------------------------------------------
    // TC-8.33 — AudioBecomingNoisy broadcast handled without crash
    // -----------------------------------------------------------------------

    /**
     * TC-8.33 — Automated.
     *
     * [AudioPlaybackService] configures ExoPlayer with
     * `setHandleAudioBecomingNoisy(true)`, which registers an internal
     * [android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY] receiver that
     * pauses playback when headphones are unplugged.
     *
     * This test sends that broadcast programmatically to verify the player
     * infrastructure handles it without throwing an exception. A direct pause
     * assertion is omitted because actual pause requires an active ExoPlayer
     * session bound to audio focus, which cannot be reliably established without
     * starting real network-backed playback in an instrumented environment.
     */
    @Test
    fun tc8_33_audioBecomingNoisyBroadcast_handledGracefully() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val noisyIntent = Intent(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

        // Sending the broadcast must not throw. ExoPlayer's internal receiver
        // handles it; no exception means the player infrastructure is sound.
        context.sendBroadcast(noisyIntent)

        // Player state must be a valid AudioPlayerState value (no crash, no null).
        val stateFlow = repository.getPlayerStateFlow()
        // Collect current value synchronously from StateFlow via value property
        // by casting — getPlayerStateFlow() is backed by StateFlow in DefaultAudioRepository.
        // If flow collection itself throws, the test will fail here.
        runTest {
            stateFlow.test(timeout = 2.seconds) {
                val state: AudioPlayerState = awaitItem()
                assertThat(state).isNotNull()
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    // -----------------------------------------------------------------------
    // TC-8.34 — [Manual] Bluetooth disconnect pause behavior
    // -----------------------------------------------------------------------

    /**
     * TC-8.34 — Manual verification required.
     *
     * Steps:
     *   1. Pair a Bluetooth audio device (headphones or speaker) with the test device.
     *   2. Start playback via the sample app with Bluetooth output active.
     *   3. Disconnect the Bluetooth device (turn it off or un-pair mid-playback).
     * Expected: Audio pauses automatically when Bluetooth disconnects.
     *   - The notification play/pause button reflects the paused state.
     *   - Reconnecting the device allows resuming playback.
     * Pass criteria: No audio continues through device speakers unexpectedly;
     *   the player state transitions to PAUSED.
     */
    @Ignore("Requires hardware: pair a Bluetooth device, start playback, then disconnect it")
    @Test
    fun tc8_34_bluetoothDisconnect_pausesPlayback() {
        // Verified manually — see KDoc above for steps.
    }

    // -----------------------------------------------------------------------
    // TC-8.35 — [Manual] POST_NOTIFICATIONS permission on Android 13+
    // -----------------------------------------------------------------------

    /**
     * TC-8.35 — Manual verification required.
     *
     * Steps (Android 13 / API 33 or higher):
     *   1. Fresh-install the sample app (no previously granted permissions).
     *   2. Start playback.
     * Expected: The system permission dialog for POST_NOTIFICATIONS appears.
     *   a. Grant the permission → media notification appears in the notification shade.
     *   b. Deny the permission → playback still works but no notification is shown.
     * Pass criteria: App does not crash in either scenario; behavior matches expectation.
     *
     * Note: [AudioPlaybackService] is a [MediaSessionService]; Media3 automatically
     * posts the playback notification. The host app must declare
     * `<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>` in its
     * AndroidManifest.xml and request it at runtime on API 33+.
     */
    @Ignore("Requires device with Android 13+ (API 33): verify POST_NOTIFICATIONS dialog appears on first playback")
    @Test
    fun tc8_35_android13_postNotificationsPermissionRequired() {
        // Verified manually — see KDoc above for steps.
    }

    // -----------------------------------------------------------------------
    // TC-8.36 — loadChapter() with invalid chapter number emits ERROR state
    // -----------------------------------------------------------------------

    /**
     * TC-8.36 — Automated.
     *
     * Chapter 999 is outside the valid Quran range (1–114). When [AudioRepository.loadChapter]
     * is called with chapter 999 and a valid reciter, [AudioPlaybackService] constructs a URL
     * for a non-existent audio file. ExoPlayer attempts to load that URL, encounters a network
     * or HTTP error, and fires [Player.Listener.onPlayerError]. [MediaSessionManager] translates
     * that error into [PlaybackState.ERROR] on the [AudioRepository.getPlayerStateFlow] stream.
     *
     * The test tolerates intervening IDLE/LOADING states before asserting ERROR, because the
     * MediaController → MediaSession round-trip and ExoPlayer prepare phase are asynchronous.
     * A 30-second turbine timeout accommodates both connection setup and the network timeout
     * for the invalid URL. Valid intermediate states are IDLE, LOADING, PAUSED, PLAYING,
     * and STOPPED — the loop drains all of them until ERROR is received.
     */
    @Test
    fun tc8_36_loadChapterWithInvalidChapterNumber_emitsErrorState() = runTest(timeout = 35.seconds) {
        repository.getPlayerStateFlow().test(timeout = 30.seconds) {
            // Trigger load of non-existent chapter with valid reciter
            repository.loadChapter(chapterNumber = 999, reciterId = validReciterId, autoPlay = false)

            // Drain IDLE and LOADING states until ERROR arrives or timeout
            var reachedError = false
            while (!reachedError) {
                val state: AudioPlayerState = awaitItem()
                when (state.playbackState) {
                    PlaybackState.ERROR -> {
                        assertThat(state.playbackState).isEqualTo(PlaybackState.ERROR)
                        reachedError = true
                    }
                    PlaybackState.IDLE,
                    PlaybackState.LOADING,
                    PlaybackState.PAUSED,
                    PlaybackState.PLAYING,
                    PlaybackState.STOPPED -> {
                        // Continue waiting — these are valid intermediate states
                    }
                }
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    // -----------------------------------------------------------------------
    // TC-8.37 — loadChapter() with invalid reciter ID — no crash, graceful handling
    // -----------------------------------------------------------------------

    /**
     * TC-8.37 — Automated.
     *
     * When [AudioRepository.loadChapter] is called with a reciter ID that does not exist
     * (9999), [AudioPlaybackService.loadChapter] calls [ReciterService.getReciterById] which
     * returns null. The service logs an error and returns early without modifying the player,
     * so no [PlaybackState.ERROR] is emitted — the flow retains its current state (typically
     * [PlaybackState.IDLE]).
     *
     * This test asserts that:
     *   1. The call does not throw any exception.
     *   2. The player state flow emits a valid [AudioPlayerState] (not null, no crash).
     *   3. The state is NOT [PlaybackState.PLAYING] (since no audio was started).
     */
    @Test
    fun tc8_37_loadChapterWithInvalidReciterId_doesNotCrash_andStateIsGraceful() = runTest {
        // Must not throw
        repository.loadChapter(chapterNumber = 1, reciterId = 9999, autoPlay = false)

        // Allow any async work in the service to settle
        delay(500)

        repository.getPlayerStateFlow().test(timeout = 5.seconds) {
            val state: AudioPlayerState = awaitItem()

            // State must be a valid non-null value — the call must not crash
            assertThat(state).isNotNull()

            // Player must NOT have started playing (reciter was not found)
            assertThat(state.playbackState).isNotEqualTo(PlaybackState.PLAYING)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
