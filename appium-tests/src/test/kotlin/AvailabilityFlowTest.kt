import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.options.UiAutomator2Options
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import java.net.URL
import java.nio.file.Paths

class AvailabilityFlowTest {
    companion object {
        private var driver: AndroidDriver? = null

        @BeforeAll
        @JvmStatic
        fun setup() {
            val appPath = findApk()
            val serverUrl = System.getProperty("appium.server.url", "http://127.0.0.1:4723")
            val udid = System.getProperty("android.udid", "")

            val options = UiAutomator2Options()
                .setDeviceName("Android Emulator")
                .setApp(appPath)
                .setAppActivity("com.example.classfinder.MainActivity")
                .setAppPackage("com.example.classfinder")
                .eventTimings()

            if (udid.isNotBlank()) {
                options.setUdid(udid)
            }

            driver = AndroidDriver(URL(serverUrl), options)
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            driver?.quit()
        }

        private fun findApk(): String {
            // Try debug APK under ../app/build/outputs/apk/debug/ (from appium-tests directory)
            val candidate = Paths.get("..", "app", "build", "outputs", "apk", "debug")
                .toFile()
                .walkTopDown()
                .firstOrNull { it.isFile && it.name.endsWith(".apk") }
            require(candidate != null) { "Debug APK not found. Build the app first with: ../gradlew :app:assembleDebug" }
            return candidate.absolutePath
        }
    }

    @Test
    fun testFindRoomsNowFlow() {
        val d = requireNotNull(driver)

    // Tap building "Samsung Building" (content-desc: building_samsung)
    val samsung = d.findElement(By.xpath("//*[@content-desc='building_samsung']"))
        samsung.click()

        // Select time mode NOW
        val now = d.findElement(By.xpath("//*[@content-desc='when_now']"))
        now.click()

        // Tap Find Rooms
        val find = d.findElement(By.xpath("//*[@content-desc='btn_find_rooms']"))
        find.click()

    // Verify at least one room result appears
        // Room items have content-desc "room_<id>"
    val anyRoom = d.findElements(By.xpath("//*[contains(@content-desc, 'room_')]"))
        assertTrue(anyRoom.isNotEmpty(), "No rooms found for NOW flow")

        // Open details of the first room
        // Each has a Details button with content-desc "btn_details_<id>"
    val detailsButtons = d.findElements(By.xpath("//*[contains(@content-desc, 'btn_details_')]"))
        if (detailsButtons.isNotEmpty()) {
            detailsButtons.first().click()
        }
    }
}
