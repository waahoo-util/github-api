package com.github.waahoo

import com.github.waahoo.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.content
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File
import java.io.FileInputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.nio.file.Files
import kotlin.streams.toList

object GitHub {
  
  fun init() {
//    initClient(proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", 8080)))
    initClient()
  }
  
  val baseURL = url("https", "api.github.com", "repos")
  
  suspend fun tag(token: String, userRepo: String, tag: String): JsonElement? {
    val (code, result) = client.getCode(
      baseURL.url("$userRepo/releases/tags/$tag"),
      headers(
        "Authorization", "token $token",
        "Accept", "application/vnd.github.v3+json"
      )
    )
    return if (code == 200) result.json() else null
  }
  
  suspend fun getOrCreateTag(token: String, userRepo: String, tag: String): JsonElement {
    val (code, result) = client.postCode(
      baseURL.url("$userRepo/releases"),
      headers(
        "Authorization", "token $token",
        "Accept", "application/vnd.github.v3+json"
      ),
      body = json.stringify(ReleaseTag.serializer(), ReleaseTag(tag, tag, "master")),
      mediaType = "application/json"
    )
    return if (code == 201) result.json() else tag(token, userRepo, tag)!!
  }
  
  suspend fun downloadAsset(
    token: String, userRepo: String, tag: String, file: String
  ) {
    val result = client.get(
      baseURL.url("$userRepo/releases/tags/$tag"),
      headers(
        "Authorization", "token $token",
        "Accept", "application/vnd.github.v3+json"
      )
    ).json()
    result["assets"].jsonArray
      .first { it["name"].content == file }.also { asset ->
        client.download(
          File(file), asset["url"].content,
          headers = mapOf(
            "Accept" to "application/octet-stream",
            "Authorization" to "token $token"
          )
        )
      }
  }
  
  @Serializable
  data class ReleaseTag(
    val tag_name: String,
    val name: String,
    val target_commitish: String = "master",
    val body: String = "",
    val draft: Boolean = false,
    val prerelease: Boolean = false
  )
  
  suspend fun uploadAsset(
    token: String, userRepo: String, tag: String, file: String
  ) {
    val release = getOrCreateTag(token, userRepo, tag)
    var upload_url = release["upload_url"].content
    upload_url = upload_url.substring(0, upload_url.indexOf("{"))
    
    val filePath = File(file)
    val files = mutableListOf<File>()
    if (filePath.isDirectory)
      files += Files.walk(filePath.toPath(), 1).map {
        it.toFile()
      }.filter {
        println(it.toString())
        it.isFile
      }.toList()
    
    files.forEach {
      val name = it.name
      
      for (asset in release["assets"].jsonArray)
        if (asset["name"].content == name) {
          val (code, result) = client.deleteCode(
            asset["url"].content.toHttpUrl(),
            headers(
              "Authorization", "token $token",
              "Accept", "application/vnd.github.v3+json"
            )
          )
          break
        }
      
      val (code, result) = client.postCode(
        "$upload_url?name=$name".toHttpUrl(),
        headers(
          "Authorization", "token $token",
          "Accept", "application/vnd.github.v3+json"
        ),
        body = FileInputStream(it),
        mediaType = "application/octet-stream"
      )
      if (code != 201)
        error("error upload asset /$userRepo/$tag/$it $code $result")
    }
  }
  
  fun close() {
    closeClient()
  }
}