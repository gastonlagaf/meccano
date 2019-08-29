package io.zensoft.hootka.api.internal.server.nio.http.cookie

import io.zensoft.hootka.api.internal.server.nio.http.HttpRequestParser

class DefaultCookieCodec: CookieCodec {

    override fun encode(cookies: Map<String, String>): String {
        return cookies.asSequence().map { "${it.key}=${it.value}" }.joinToString("; ")
    }

    override fun decode(string: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val reader = HttpRequestParser(string)
        while (!reader.contentRead()) {
            val key = reader.readUntil('=').trim()
            result[key] = reader.readUntil(';').trim()
        }
        return result
    }

}