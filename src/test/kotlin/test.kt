import com.github.waahoo.GitHub

suspend fun main() {
  val token = "0978a671206efd4d0b9e0c1e9b1e307a6317fa8b"
//  GitHub.tag(token, "waahoo-hack/checkin", "release")
  GitHub.init()
  GitHub.uploadAsset(
    token, "waahoo-hack/checkin", "release", "."
  )
  GitHub.close()
}