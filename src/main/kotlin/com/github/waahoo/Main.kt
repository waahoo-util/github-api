package com.github.waahoo

import com.github.waahoo.http.closeClient
import com.github.waahoo.http.initClient

fun main(args: Array<String>) {
  initClient()
  assert(args.size >= 5)
  GitHub.downloadAsset(args[0], args[1], args[2], args[3], args[4])
  closeClient()
}