/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.menu

import android.widget.Toast
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.metrolist.music.R
import com.metrolist.music.constants.BlockedArtistsKey
import com.metrolist.music.ui.component.Material3MenuItemData
import com.metrolist.music.utils.ArtistBlocking
import com.metrolist.music.utils.rememberPreference
import kotlinx.coroutines.launch

@Composable
fun artistBlockingMenuItem(artistId: String, artistName: String, onDismiss: () -> Unit): Material3MenuItemData {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val serializedArtists by rememberPreference(BlockedArtistsKey, "{}")
    val blockedArtists = remember(serializedArtists) { ArtistBlocking.decode(serializedArtists) }
    val isBlocked = ArtistBlocking.matchingIds(artistId, artistName, blockedArtists).isNotEmpty()

    return Material3MenuItemData(
        title = { Text(stringResource(if (isBlocked) R.string.allow_artist else R.string.dont_play_artist)) },
        icon = {
            Icon(
                painterResource(if (isBlocked) R.drawable.check else R.drawable.block),
                contentDescription = null,
            )
        },
        onClick = {
            coroutineScope.launch {
                val saved = ArtistBlocking.setBlocked(context, artistId, artistName, !isBlocked)
                Toast.makeText(
                    context,
                    if (!saved) R.string.artist_block_failed else if (isBlocked) R.string.artist_allowed else R.string.artist_blocked,
                    Toast.LENGTH_SHORT,
                ).show()
                if (saved) onDismiss()
            }
        },
    )
}
