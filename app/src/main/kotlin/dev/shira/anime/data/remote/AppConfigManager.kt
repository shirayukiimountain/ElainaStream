package dev.shira.anime.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AppConfigManager {
    private const val TAG = "AppConfigManager"
    private const val PREFS_NAME = "anime_app_remote_config"
    private const val KEY_WHATBOX_AUTH = "whatbox_auth"
    private const val KEY_SERVER_URL = "server_url"
    private const val KEY_LAST_SYNC = "last_sync_ms"

    const val DEFAULT_WHATBOX_AUTH = "Basic YW5pbWV4bm9udG9uOmpvM1owWGhkV3BtandhZjZZVWVMVUJNNWx0WE51VXY4Qg=="
    const val DEFAULT_SERVER_URL = "https://wincamp.web.id/animexnonton/api/"
    const val CONFIG_URL = "https://wincampdotorg.github.io/config/api/animexnonton.json"

    @Volatile
    private var cachedWhatboxAuth: String? = null

    @Volatile
    private var cachedServerUrl: String? = null

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun init(context: Context) {
        getWhatboxAuth(context)
        getServerUrl(context)
    }

    fun getWhatboxAuth(context: Context): String {
        cachedWhatboxAuth?.let { return it }
        val saved = getPrefs(context).getString(KEY_WHATBOX_AUTH, null)
        val result = saved?.takeIf { it.isNotBlank() } ?: DEFAULT_WHATBOX_AUTH
        cachedWhatboxAuth = result
        return result
    }

    fun getServerUrl(context: Context): String {
        cachedServerUrl?.let { return it }
        val saved = getPrefs(context).getString(KEY_SERVER_URL, null)
        val result = saved?.takeIf { it.isNotBlank() } ?: DEFAULT_SERVER_URL
        cachedServerUrl = result
        return result
    }

    fun getBaseHost(): String? {
        cachedServerUrl?.let { url ->
            return try {
                java.net.URI(url).host
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    fun getBaseScheme(): String {
        cachedServerUrl?.let { url ->
            return try {
                java.net.URI(url).scheme ?: "https"
            } catch (e: Exception) {
                "https"
            }
        }
        return "https"
    }

    fun getBasePort(): Int {
        cachedServerUrl?.let { url ->
            return try {
                java.net.URI(url).port
            } catch (e: Exception) {
                -1
            }
        }
        return -1
    }

    suspend fun sync(context: Context, force: Boolean = false) {
        withContext(Dispatchers.IO) {
            runCatching {
                val prefs = getPrefs(context)
                val lastSync = prefs.getLong(KEY_LAST_SYNC, 0L)
                val now = System.currentTimeMillis()

                // Sync at most once every 10 minutes unless forced
                if (!force && (now - lastSync) < 10 * 60 * 1000L && prefs.contains(KEY_WHATBOX_AUTH)) {
                    return@withContext
                }

                val request = Request.Builder()
                    .url(CONFIG_URL)
                    .header("User-Agent", "okhttp/5.5.0")
                    .header("Cache-Control", "no-cache")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w(TAG, "Failed to fetch remote config: HTTP ${response.code}")
                        return@withContext
                    }

                    val responseBody = response.body?.string().orEmpty()
                    if (responseBody.isBlank()) return@withContext

                    val json = JSONObject(responseBody)
                    val hex = json.optString("hex")
                    val videoPassEncrypted = json.optString("video_pass")
                    val serverUrlEncrypted = json.optString("server_url")

                    val editor = prefs.edit()

                    if (hex.isNotBlank() && videoPassEncrypted.isNotBlank()) {
                        val decryptedAuth = CryptoUtils.decrypt(videoPassEncrypted, hex).trim()
                        if (decryptedAuth.isNotBlank() && decryptedAuth.startsWith("Basic ")) {
                            cachedWhatboxAuth = decryptedAuth
                            editor.putString(KEY_WHATBOX_AUTH, decryptedAuth)
                            Log.d(TAG, "Remote Whatbox Auth synced successfully.")
                        }
                    }

                    if (hex.isNotBlank() && serverUrlEncrypted.isNotBlank()) {
                        val decryptedServerUrl = CryptoUtils.decrypt(serverUrlEncrypted, hex).trim()
                        if (decryptedServerUrl.isNotBlank() && decryptedServerUrl.startsWith("http")) {
                            val normalized = if (decryptedServerUrl.endsWith("/")) decryptedServerUrl else "$decryptedServerUrl/"
                            cachedServerUrl = normalized
                            editor.putString(KEY_SERVER_URL, normalized)
                            Log.d(TAG, "Remote Server URL synced successfully: $normalized")
                        }
                    }

                    editor.putLong(KEY_LAST_SYNC, now).apply()
                }
            }.onFailure { e ->
                Log.w(TAG, "Remote config sync error: ${e.message}")
            }
        }
    }
}
