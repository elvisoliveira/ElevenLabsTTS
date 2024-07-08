package br.com.elvisoliveira.elevenlabstts

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

data class Body(
    val text: String,
    val model_id: String
)

class ElevenLabsService(
    private val modelId: String =  "eleven_multilingual_v1",
    private val apiKey: String = "",
    private val voiceId: String = "nPczCjzI2devNBz1zQrb"
) {
    private val client = OkHttpClient().newBuilder()
        .callTimeout(10, TimeUnit.SECONDS)
        .build()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    fun textToSpeech(text: String): ByteArray {
        val url = "https://api.elevenlabs.io/v1/text-to-speech/$voiceId?optimize_streaming_latency=0"

        val bodyObj = Body(
            text = text,
            model_id = modelId
        )

        val bodyJson = moshi.adapter(Body::class.java).toJson(bodyObj)
        val body = bodyJson.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("accept", "audio/mpeg")
            .addHeader("xi-api-key", apiKey)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            return response.body!!.bytes()
        }
    }
}