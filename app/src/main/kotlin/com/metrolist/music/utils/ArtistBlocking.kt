/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import android.content.Context
import com.metrolist.music.constants.BlockedArtistsKey
import com.metrolist.music.models.MediaMetadata
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ArtistBlocking {
    fun decode(value: String): Map<String, String> =
        try {
            Json.decodeFromString<Map<String, String>>(value)
        } catch (e: IllegalArgumentException) {
            reportException(e)
            emptyMap()
        }

    fun isBlocked(metadata: MediaMetadata?, blockedArtists: Map<String, String>): Boolean =
        metadata?.artists?.any { matchingIds(it.id, it.name, blockedArtists).isNotEmpty() } == true

    fun matchingIds(artistId: String?, artistName: String, blockedArtists: Map<String, String>): Set<String> =
        blockedArtists.filter { (id, name) ->
            id == artistId || ((artistId.isNullOrBlank() || artistId.startsWith("LA") || id.startsWith("LA")) &&
                artistName.isNotBlank() &&
                ArtistNameAliases.resolve(id, name).trim().equals(
                    ArtistNameAliases.resolve(artistId, artistName).trim(), ignoreCase = true,
                ))
        }.keys

    suspend fun setBlocked(context: Context, artistId: String, artistName: String, blocked: Boolean): Boolean {
        if (artistId.isBlank()) return false
        return context.safeDataStoreEdit { preferences ->
            val artists = decode(preferences[BlockedArtistsKey] ?: "{}")
            preferences[BlockedArtistsKey] = Json.encodeToString(
                if (blocked) artists + (artistId to artistName) else artists - matchingIds(artistId, artistName, artists),
            )
        }
    }
}
