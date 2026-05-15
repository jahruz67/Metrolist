package com.metrolist.music.utils

import android.content.Context
import com.metrolist.music.constants.BlockGuestAppearancesKey
import com.metrolist.music.constants.BlockedArtistsKey
import com.metrolist.music.constants.GuestAppearanceHandling
import com.metrolist.music.utils.dataStore

fun parseBlockedArtists(rawValue: String): Set<String> =
    rawValue
        .split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()

suspend fun Context.blockedArtistIds(): Set<String> =
    parseBlockedArtists(dataStore.get(BlockedArtistsKey, ""))

suspend fun Context.shouldBlockGuestAppearances(): Boolean =
    dataStore.get(BlockGuestAppearancesKey, GuestAppearanceHandling.BLOCK_FEATURES.name) ==
        GuestAppearanceHandling.BLOCK_FEATURES.name
