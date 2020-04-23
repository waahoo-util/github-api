import com.github.waahoo.GitHub

suspend fun main() {
  val token = "d050a1cdbf0b7697f068d7566d41259419065967"
//  GitHub.tag(token, "waahoo-hack/checkin", "release")
  
  GitHub.uploadAsset(
    token, "waahoo-hack/checkin", "test-release3", "test.txt"
  )
  GitHub.close()
}