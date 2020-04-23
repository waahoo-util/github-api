package com.github.waahoo

import com.github.waahoo.http.closeClient

fun errorIf(condition: Boolean, msg: () -> String) {
  if (condition)
    throw Exception(msg())
}

suspend fun main(args: Array<String>) {
  errorIf(args.isNotEmpty()) { "args shouldn't be empty" }
  GitHub.init()
  try {
    when (args[0]) {
      "download" -> {
        errorIf(args.size != 5) { "usage: download <token> <user_repo> <tag> <file>" }
        GitHub.downloadAsset(args[1], args[2], args[3], args[4])
      }
      "upload" -> {
        errorIf(args.size != 5) { "usage: upload <token> <user_repo> <tag> <file>" }
        GitHub.uploadAsset(args[1], args[2], args[3], args[4])
      }
      else -> error("unknown command")
    }
  } catch (e: Exception) {
    throw e
  } finally {
    closeClient()
  }
}