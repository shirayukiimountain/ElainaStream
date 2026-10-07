package dev.shira.anime.data.remote

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.shira.anime.data.remote.dto.animein.AnimeinBaseResponseDto
import dev.shira.anime.data.remote.dto.animein.AnimeinEpisodeDto
import dev.shira.anime.data.remote.dto.animein.AnimeinEpisodeListDataDto
import dev.shira.anime.data.remote.dto.animein.AnimeinGenreDto
import dev.shira.anime.data.remote.dto.animein.AnimeinGenreListDataDto
import dev.shira.anime.data.remote.dto.animein.AnimeinMovieDetailDataDto
import dev.shira.anime.data.remote.dto.animein.AnimeinMovieListDataDto
import dev.shira.anime.data.remote.dto.animein.AnimeinStreamDataDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimeinDtoTest {

    private val gson = Gson()

    @Test
    fun testParseMovieListResponse() {
        val json = """
            {
                "status": 200,
                "error": false,
                "data": {
                    "movie": [
                        {
                            "id": "6491",
                            "title": "Kikansha no Mahou wa Tokubetsu desu 2nd Season",
                            "synopsis": "Setelah kembali 13 tahun...",
                            "image_poster": "https://xyz-api.animein.net/poster.jpg",
                            "image_cover": "https://xyz-api.animein.net/cover.jpg",
                            "type": "SERIES",
                            "year": "2026",
                            "status": "ONGOING",
                            "views": "600",
                            "favorites": "2704",
                            "key_time_update": "2026-10-08 00:35:13",
                            "genre": "Episode 1"
                        }
                    ]
                }
            }
        """.trimIndent()

        val type = object : TypeToken<AnimeinBaseResponseDto<AnimeinMovieListDataDto>>() {}.type
        val response: AnimeinBaseResponseDto<AnimeinMovieListDataDto> = gson.fromJson(json, type)

        assertEquals(200, response.status)
        assertFalse(response.error ?: true)
        val movies = response.data?.movie
        assertNotNull(movies)
        assertEquals(1, movies?.size)
        val first = movies?.first()
        assertEquals("6491", first?.id)
        assertEquals("Kikansha no Mahou wa Tokubetsu desu 2nd Season", first?.title)
        assertEquals("https://xyz-api.animein.net/poster.jpg", first?.imagePoster)
        assertEquals("https://xyz-api.animein.net/cover.jpg", first?.imageCover)
        assertEquals("ONGOING", first?.status)
    }

    @Test
    fun testParseMovieDetailResponse() {
        val json = """
            {
                "status": 200,
                "error": false,
                "data": {
                    "movie": {
                        "id": "6631",
                        "title": "Vertex Force",
                        "synopsis": "Akira Toudou...",
                        "image_poster": "https://xyz-api.animein.net/poster.jpg",
                        "status": "ONGOING",
                        "genre": "Action,Sci-Fi"
                    },
                    "episode": {
                        "id": "321326",
                        "index": "1",
                        "title": "Episode 1",
                        "views": "2198",
                        "id_movie": "6631"
                    }
                }
            }
        """.trimIndent()

        val type = object : TypeToken<AnimeinBaseResponseDto<AnimeinMovieDetailDataDto>>() {}.type
        val response: AnimeinBaseResponseDto<AnimeinMovieDetailDataDto> = gson.fromJson(json, type)

        assertEquals(200, response.status)
        assertEquals("Vertex Force", response.data?.movie?.title)
        assertEquals("321326", response.data?.episode?.id)
        assertEquals("1", response.data?.episode?.index)
    }

    @Test
    fun testParseStreamResponse() {
        val json = """
            {
                "status": 200,
                "error": false,
                "data": {
                    "episode": {
                        "id": "321326",
                        "index": "1",
                        "title": "Episode 1",
                        "id_movie": "6631"
                    },
                    "server": [
                        {
                            "id": "831966",
                            "link": "https://storages.animein.net/video-360p.mp4",
                            "quality": "360p",
                            "name": "RAPSODI",
                            "type": "direct"
                        },
                        {
                            "id": "832058",
                            "link": "https://storages.animein.net/video-1080p.mp4",
                            "quality": "1080p",
                            "name": "RAPSODI",
                            "type": "direct"
                        }
                    ]
                }
            }
        """.trimIndent()

        val type = object : TypeToken<AnimeinBaseResponseDto<AnimeinStreamDataDto>>() {}.type
        val response: AnimeinBaseResponseDto<AnimeinStreamDataDto> = gson.fromJson(json, type)

        assertEquals(200, response.status)
        val servers = response.data?.server
        assertNotNull(servers)
        assertEquals(2, servers?.size)
        assertEquals("1080p", servers?.get(1)?.quality)
        assertEquals("direct", servers?.get(1)?.type)
        assertTrue(servers?.get(1)?.link?.contains(".mp4") == true)
    }

    @Test
    fun testParseGenreListResponse() {
        val json = """
            {
                "status": 200,
                "error": false,
                "data": {
                    "genre": [
                        {
                            "id": "14",
                            "name": "Action",
                            "group": "Genre"
                        },
                        {
                            "id": "16",
                            "name": "Adventure",
                            "group": "Genre"
                        }
                    ]
                }
            }
        """.trimIndent()

        val type = object : TypeToken<AnimeinBaseResponseDto<AnimeinGenreListDataDto>>() {}.type
        val response: AnimeinBaseResponseDto<AnimeinGenreListDataDto> = gson.fromJson(json, type)

        assertEquals(200, response.status)
        val genres = response.data?.genre
        assertNotNull(genres)
        assertEquals(2, genres?.size)
        assertEquals("14", genres?.first()?.id)
        assertEquals("Action", genres?.first()?.name)
    }
}
