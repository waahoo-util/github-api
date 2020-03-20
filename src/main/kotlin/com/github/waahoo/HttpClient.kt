package com.github.waahoo

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

//var hostname = "127.0.0.1"
//var port = 8081
//var proxy: Proxy = Proxy(
//  Proxy.Type.SOCKS,
//  InetSocketAddress(hostname, port)
//)
val client = OkHttpClient.Builder()
//  .proxy(proxy)
  .build()

fun OkHttpClient.close() {
  dispatcher.executorService.shutdown()
  connectionPool.evictAll()
  cache?.close()
}

suspend fun OkHttpClient.get(url: String, headers: Map<String, String> = emptyMap()): String {
  getResp(url, headers).use {
    return it.body?.string() ?: ""
  }
}

suspend fun OkHttpClient.getResp(url: String, headers: Map<String, String> = emptyMap()): Response {
  val headerBuilder = headers.toHeaders()
  val request = Request.Builder().url(url).headers(headerBuilder).build()
  val response = newCall(request).await()
  if (!response.isSuccessful) throw IOException()
  return response
}

suspend fun OkHttpClient.post(
  url: String,
  headers: Map<String, String> = emptyMap(),
  form: Map<String, String> = emptyMap()
): String {
  val header = headers.toHeaders()
  val data = FormBody.Builder().apply {
    form.forEach { k, v ->
      add(k, v)
    }
  }.build()
  val request = Request.Builder().url(url).headers(header).post(data).build()
  val response = newCall(request).await()
  if (!response.isSuccessful) throw IOException()
  response.use {
    return it.body?.string() ?: ""
  }
}

suspend fun OkHttpClient.download(
  dest: File, url: String,
  headers: Map<String, String> = emptyMap(),
  block: (Long, Long, Double) -> Unit = { _, _, _ -> }
) {
  getResp(url, headers).use {
    FileOutputStream(dest).use { out ->
      it.body?.apply {
        byteStream().use {
          it.transferTo(out)
        }
      }
    }
  }
  println("downloaded to $dest")
}

/**
 * Suspend extension that allows suspend [Call] inside coroutine.
 *
 * [recordStack] enables track recording, so in case of exception stacktrace will contain call stacktrace, may be useful for debugging
 *      Not free! Creates exception on each request so disabled by default, but may be enabled using system properties:
 *
 *      ```
 *      System.setProperty(OKHTTP_STACK_RECORDER_PROPERTY, OKHTTP_STACK_RECORDER_ON)
 *      ```
 *      see [README.md](https://github.com/gildor/kotlin-coroutines-okhttp/blob/master/README.md#Debugging) with details about debugging using this feature
 *
 * @return Result of request or throw exception
 */
suspend fun Call.await(recordStack: Boolean = isRecordStack): Response {
  val callStack = if (recordStack) {
    IOException().apply {
      // Remove unnecessary lines from stacktrace
      // This doesn't remove await$default, but better than nothing
      stackTrace = stackTrace.copyOfRange(1, stackTrace.size)
    }
  } else {
    null
  }
  return suspendCancellableCoroutine { continuation ->
    enqueue(object : Callback {
      override fun onResponse(call: Call, response: Response) {
        continuation.resume(response)
      }
      
      override fun onFailure(call: Call, e: IOException) {
        // Don't bother with resuming the continuation if it is already cancelled.
        if (continuation.isCancelled) return
        callStack?.initCause(e)
        continuation.resumeWithException(callStack ?: e)
      }
    })
    
    continuation.invokeOnCancellation {
      try {
        cancel()
      } catch (ex: Throwable) {
        //Ignore cancel exception
      }
    }
  }
}

const val OKHTTP_STACK_RECORDER_PROPERTY = "ru.gildor.coroutines.okhttp.stackrecorder"

/**
 * Debug turned on value for [DEBUG_PROPERTY_NAME]. See [newCoroutineContext][CoroutineScope.newCoroutineContext].
 */
const val OKHTTP_STACK_RECORDER_ON = "on"

/**
 * Debug turned on value for [DEBUG_PROPERTY_NAME]. See [newCoroutineContext][CoroutineScope.newCoroutineContext].
 */
const val OKHTTP_STACK_RECORDER_OFF = "off"

@JvmField
val isRecordStack = when (System.getProperty(OKHTTP_STACK_RECORDER_PROPERTY)) {
  OKHTTP_STACK_RECORDER_ON -> true
  OKHTTP_STACK_RECORDER_OFF, null, "" -> false
  else -> error(
    "System property '$OKHTTP_STACK_RECORDER_PROPERTY' has unrecognized value '${System.getProperty(
      OKHTTP_STACK_RECORDER_PROPERTY
    )}'"
  )
}