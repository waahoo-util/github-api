package com.github.waahoo

fun main(args: Array<String>) {
  assert(args.size >= 5)
  GitHub.downloadAsset(args[0], args[1], args[2], args[3], args[4])
}