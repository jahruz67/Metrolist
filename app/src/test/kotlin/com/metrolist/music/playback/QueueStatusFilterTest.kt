package com.metrolist.music.playback

import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.models.MediaMetadata
import com.metrolist.music.playback.queues.Queue
import com.metrolist.music.playback.queues.filterBlockedArtists
import com.metrolist.music.utils.ArtistBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = android.app.Application::class)
class QueueStatusFilterTest {
    private fun item(id: String, explicit: Boolean = false) =
        MediaMetadata(id = id, title = id, artists = emptyList(), duration = 0, explicit = explicit).toMediaItem()

    private val items = listOf(item("a", explicit = true), item("b"), item("c", explicit = true), item("d"))

    @Test
    fun `filtering keeps the selected song as the start item`() {
        val status = Queue.Status(null, items, mediaItemIndex = 3, position = 500L).filterExplicit()

        assertEquals(listOf("b", "d"), status.items.map { it.mediaId })
        assertEquals("d", status.items[status.mediaItemIndex].mediaId)
        assertEquals(500L, status.position)
    }

    @Test
    fun `filtered start song falls through to the next kept song`() {
        val status = Queue.Status(null, items, mediaItemIndex = 2, position = 500L).filterExplicit()

        assertEquals("d", status.items[status.mediaItemIndex].mediaId)
        assertEquals(0L, status.position)
    }

    private fun artistItem(id: String, vararg artists: MediaMetadata.Artist) =
        MediaMetadata(id = id, title = id, artists = artists.toList(), duration = 0).toMediaItem()

    @Test
    fun `blocking an artist preserves the selected allowed track and position`() {
        val blocked = artistItem("blocked", MediaMetadata.Artist("UCblocked", "Blocked"))
        val allowed = artistItem("allowed", MediaMetadata.Artist("UCallowed", "Allowed"))
        val status = Queue.Status(null, listOf(blocked, allowed), 1, 500L)
            .filterBlockedArtists(mapOf("UCblocked" to "Blocked"))

        assertEquals(listOf("allowed"), status.items.map { it.mediaId })
        assertEquals(0, status.mediaItemIndex)
        assertEquals(500L, status.position)
    }

    @Test
    fun `a blocked collaboration skips to the next allowed track from the beginning`() {
        val collaboration = artistItem(
            "collaboration",
            MediaMetadata.Artist("UCallowed", "Allowed"),
            MediaMetadata.Artist("UCblocked", "Blocked"),
        )
        val allowed = artistItem("allowed", MediaMetadata.Artist("UCallowed", "Allowed"))
        val status = Queue.Status(null, listOf(collaboration, allowed), 0, 500L)
            .filterBlockedArtists(mapOf("UCblocked" to "Blocked"))

        assertEquals(listOf("allowed"), status.items.map { it.mediaId })
        assertEquals(0L, status.position)
    }

    @Test
    fun `name fallback covers missing and generated IDs without blocking a distinct known artist`() {
        val items = listOf(
            artistItem("name-only", MediaMetadata.Artist(null, " blocked ")),
            artistItem("library", MediaMetadata.Artist("LA123", "Blocked")),
            artistItem("different-artist", MediaMetadata.Artist("UCdifferent", "Blocked")),
        ).filterBlockedArtists(mapOf("UCblocked" to "Blocked"))

        assertEquals(listOf("different-artist"), items.map { it.mediaId })
    }

    @Test
    fun `an entirely blocked queue is empty with its position reset`() {
        val blocked = artistItem("blocked", MediaMetadata.Artist("UCblocked", "Blocked"))
        val status = Queue.Status(null, listOf(blocked), 0, 500L)
            .filterBlockedArtists(mapOf("UCblocked" to "Blocked"))

        assertEquals(emptyList<String>(), status.items.map { it.mediaId })
        assertEquals(0, status.mediaItemIndex)
        assertEquals(0L, status.position)
    }

    @Test
    fun `generated and canonical artist entries share block state`() {
        val blocked = mapOf("UCblocked" to "Blocked", "LA123" to "Blocked")
        assertEquals(blocked.keys, ArtistBlocking.matchingIds("LA123", "Blocked", blocked))
        assertEquals(blocked.keys, ArtistBlocking.matchingIds("UCblocked", "Blocked", blocked))
    }
}
