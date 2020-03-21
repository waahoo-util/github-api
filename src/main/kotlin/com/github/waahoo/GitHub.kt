package com.github.waahoo

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.content
import java.io.File

object GitHub {
  
  fun downloadAsset(
    token: String,
    user: String, repo: String,
    tagName: String, file: String
  ) {
    runBlocking {
      val result = client.get(
        "https://api.github.com/repos/$user/$repo/releases",
        headers = mapOf(
          "Authorization" to "token $token",
          "Accept" to "application/vnd.github.v3.raw"
        )
      ).json()
      result.jsonArray.forEach { tag ->
        if (tag["tag_name"].content != tagName) return@forEach
        tag["assets"].jsonArray.forEach { asset ->
          if (asset["name"].content == file) {
            val id = asset["id"].content
            client.download(
              File(file), "https://api.github.com/repos/$user/$repo/releases/assets/$id",
              headers = mapOf(
                "Accept" to "application/octet-stream",
                "Authorization" to "token $token"
              )
            )
            return@runBlocking
          }
        }
      }
    }
  }
  
}