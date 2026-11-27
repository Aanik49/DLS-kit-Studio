package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class PublishedKitUrlResult(
    val success: Boolean,
    val url: String = "",
    val hostName: String = "",
    val errorMessage: String? = null
)

/**
 * Publishes rendered 512x512 PNG textures to permanent direct-image HTTPS CDNs
 * and verifies that GET returns HTTP 200 + image/png (no HTML redirects), so
 * Dream League Soccer 2026 can immediately download and apply the kit/logo in-game.
 */
object DlsKitUrlPublisher {

    // FreeImage.host public anonymous upload key (permanent iili.io CDN PNG hosting)
    private const val FREEIMAGE_PUBLIC_KEY = "6d207e02198a847aa98d0a2a901485a5"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    suspend fun publishPngForDls26(bitmap: Bitmap, fileNameBase: String): PublishedKitUrlResult =
        withContext(Dispatchers.IO) {
            val safeName = fileNameBase.lowercase()
                .replace(Regex("[^a-z0-9_]+"), "_")
                .trim('_')
                .ifBlank { "dls26_kit" }
            val fileName = "${safeName}_512.png"
            val pngBytes = ByteArrayOutputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.toByteArray()
            }

            // 1. Primary: FreeImage.host -> permanent Cloudflare CDN https://iili.io/xxxxx.png
            uploadToFreeImageHost(pngBytes)?.let { res ->
                if (verifyDirectPngUrl(res.url)) return@withContext res
            }

            // 2. Fallback: Pomf2.lain.la -> permanent direct https://pomf2.lain.la/f/xxxxx.png
            uploadToPomf2(pngBytes, fileName)?.let { res ->
                if (verifyDirectPngUrl(res.url)) return@withContext res
            }

            // 3. Fallback: Uguu.se -> direct https://d.uguu.se/xxxxx.png
            uploadToUguu(pngBytes, fileName)?.let { res ->
                if (verifyDirectPngUrl(res.url)) return@withContext res
            }

            PublishedKitUrlResult(
                success = false,
                errorMessage = "Could not reach kit image CDN. Check internet connection or use Export 512px PNG."
            )
        }

    private fun uploadToFreeImageHost(bytes: ByteArray): PublishedKitUrlResult? {
        return try {
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val formBody = FormBody.Builder()
                .add("key", FREEIMAGE_PUBLIC_KEY)
                .add("action", "upload")
                .add("source", base64)
                .add("format", "json")
                .build()
            val req = Request.Builder()
                .url("https://freeimage.host/api/1/upload")
                .header("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 14)")
                .post(formBody)
                .build()
            client.newCall(req).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (!resp.isSuccessful || raw.isBlank()) return null
                val json = JSONObject(raw)
                val statusCode = json.optInt("status_code", 0)
                val imgUrl = json.optJSONObject("image")?.optString("url").orEmpty()
                    .replace("http://", "https://")
                if (statusCode == 200 && imgUrl.startsWith("https://")) {
                    PublishedKitUrlResult(success = true, url = imgUrl, hostName = "FreeImage CDN (Permanent)")
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun uploadToPomf2(bytes: ByteArray, fileName: String): PublishedKitUrlResult? {
        return try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "files[]",
                    fileName,
                    bytes.toRequestBody("image/png".toMediaType())
                )
                .build()
            val req = Request.Builder()
                .url("https://pomf2.lain.la/upload.php")
                .header("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 14)")
                .post(body)
                .build()
            client.newCall(req).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (!resp.isSuccessful || raw.isBlank()) return null
                val json = JSONObject(raw)
                val arr = json.optJSONArray("files")
                val url = arr?.optJSONObject(0)?.optString("url").orEmpty()
                    .replace("\\/", "/")
                if (json.optBoolean("success", false) && url.startsWith("https://")) {
                    PublishedKitUrlResult(success = true, url = url, hostName = "Pomf2 Direct CDN")
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun uploadToUguu(bytes: ByteArray, fileName: String): PublishedKitUrlResult? {
        return try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "files[]",
                    fileName,
                    bytes.toRequestBody("image/png".toMediaType())
                )
                .build()
            val req = Request.Builder()
                .url("https://uguu.se/upload")
                .header("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 14)")
                .post(body)
                .build()
            client.newCall(req).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (!resp.isSuccessful || raw.isBlank()) return null
                val json = JSONObject(raw)
                val arr = json.optJSONArray("files")
                val url = arr?.optJSONObject(0)?.optString("url").orEmpty()
                    .replace("\\/", "/")
                if (json.optBoolean("success", false) && url.startsWith("https://")) {
                    PublishedKitUrlResult(success = true, url = url, hostName = "Uguu Direct CDN")
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun verifyDirectPngUrl(url: String): Boolean {
        if (!url.startsWith("https://")) return false
        return try {
            val headReq = Request.Builder()
                .url(url)
                .header("User-Agent", "UnityPlayer/2022.3 (Android)")
                .head()
                .build()
            client.newCall(headReq).execute().use { resp ->
                val contentType = resp.header("Content-Type").orEmpty().lowercase()
                resp.isSuccessful && contentType.startsWith("image/")
            }
        } catch (_: Exception) {
            false
        }
    }
}
