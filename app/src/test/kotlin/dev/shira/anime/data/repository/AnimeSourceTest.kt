package dev.shira.anime.data.repository

import dev.shira.anime.domain.model.AnimeSourceType
import org.junit.Assert.assertEquals
import org.junit.Test

class AnimeSourceTest {

    @Test
    fun testAnimeSourceTypeLookup() {
        assertEquals(AnimeSourceType.ANIMEX, AnimeSourceType.fromId("animex"))
        assertEquals(AnimeSourceType.ANIMEKU, AnimeSourceType.fromId("animeku"))
        assertEquals(AnimeSourceType.ANIMEIN, AnimeSourceType.fromId("animein"))
        assertEquals(AnimeSourceType.ANIMEX, AnimeSourceType.fromId("unknown"))
        assertEquals(AnimeSourceType.ANIMEX, AnimeSourceType.fromId(null))
    }

    @Test
    fun testAnimeSourceDisplayName() {
        assertEquals("Anime X Nonton", AnimeSourceType.ANIMEX.displayName)
        assertEquals("Animeku", AnimeSourceType.ANIMEKU.displayName)
        assertEquals("AnimeIN", AnimeSourceType.ANIMEIN.displayName)
    }

    @Test
    fun testAnimeinSourceType() {
        assertEquals("animein", AnimeSourceType.ANIMEIN.id)
        assertEquals("Server 3", AnimeSourceType.ANIMEIN.serverBadge)
    }
}
