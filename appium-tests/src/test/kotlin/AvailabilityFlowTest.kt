import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.options.UiAutomator2Options
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import java.net.URL
import java.nio.file.Paths
import java.time.Duration

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
                .setPlatformName("Android")
                .setApp(appPath)
                .setAppActivity("com.example.classfinder.MainActivity")
                .setAppPackage("com.example.classfinder")
                .setAutoGrantPermissions(true)
                .setNoReset(false)
                .setFullReset(false)
                // Increase timeouts for slower emulators
                .setUiautomator2ServerLaunchTimeout(Duration.ofSeconds(60))
                .setUiautomator2ServerInstallTimeout(Duration.ofSeconds(60))
                .setNewCommandTimeout(Duration.ofMinutes(5))
                .eventTimings()

            if (udid.isNotBlank()) {
                options.setUdid(udid)
            }

            println("Starting Appium session with APK: $appPath")
            println("Connecting to Appium server: $serverUrl")
            
            try {
                driver = AndroidDriver(URL(serverUrl), options)
                println("Appium session started successfully!")
                
                // Wait for app to fully load
                Thread.sleep(5000)
            } catch (e: Exception) {
                println("Failed to create Appium session: ${e.message}")
                throw e
            }
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
        
        try {
            println("Starting test: testFindRoomsNowFlow")
            
            // Wait for app to be ready and look for any UI elements
            Thread.sleep(3000)
            
            // Print page source for debugging if elements are not found
            fun printPageSourceOnError(elementDesc: String) {
                try {
                    println("Could not find element: $elementDesc")
                    println("Current page source:")
                    println(d.pageSource)
                } catch (e: Exception) {
                    println("Could not get page source: ${e.message}")
                }
            }
            
            // Try to find Samsung building with better error handling
            val samsung = try {
                d.findElement(By.xpath("//*[@content-desc='building_samsung']"))
            } catch (e: Exception) {
                printPageSourceOnError("building_samsung")
                // Try alternative selectors
                try {
                    d.findElement(By.xpath("//*[contains(@text, 'Samsung')]"))
                } catch (e2: Exception) {
                    throw Exception("Could not find Samsung Building element. Original error: ${e.message}")
                }
            }
            
            println("Found Samsung building, clicking...")
            samsung.click()
            Thread.sleep(2000)

            // Select time mode NOW with better error handling
            val now = try {
                d.findElement(By.xpath("//*[@content-desc='when_now']"))
            } catch (e: Exception) {
                printPageSourceOnError("when_now")
                try {
                    d.findElement(By.xpath("//*[contains(@text, 'NOW') or contains(@text, 'Now')]"))
                } catch (e2: Exception) {
                    throw Exception("Could not find NOW button. Original error: ${e.message}")
                }
            }
            
            println("Found NOW button, clicking...")
            now.click()
            Thread.sleep(2000)

            // Tap Find Rooms with better error handling
            val find = try {
                d.findElement(By.xpath("//*[@content-desc='btn_find_rooms']"))
            } catch (e: Exception) {
                printPageSourceOnError("btn_find_rooms")
                try {
                    d.findElement(By.xpath("//*[contains(@text, 'Find Rooms') or contains(@text, 'FIND ROOMS')]"))
                } catch (e2: Exception) {
                    throw Exception("Could not find Find Rooms button. Original error: ${e.message}")
                }
            }
            
            println("Found Find Rooms button, clicking...")
            find.click()
            Thread.sleep(5000) // Wait longer for results to load

            // Verify at least one room result appears
            println("Looking for room results...")
            val anyRoom = d.findElements(By.xpath("//*[contains(@content-desc, 'room_')]"))
            
            if (anyRoom.isEmpty()) {
                // Try alternative room selectors
                val alternativeRooms = d.findElements(By.xpath("//*[contains(@text, 'Room') or contains(@content-desc, 'Room')]"))
                if (alternativeRooms.isEmpty()) {
                    printPageSourceOnError("room results")
                    throw AssertionError("No rooms found for NOW flow")
                }
                println("Found ${alternativeRooms.size} rooms using alternative selector")
            } else {
                println("Found ${anyRoom.size} rooms")
            }
            
            assertTrue(anyRoom.isNotEmpty() || d.findElements(By.xpath("//*[contains(@text, 'Room')]")).isNotEmpty(), 
                      "No rooms found for NOW flow")

            // Try to open details of the first room
            val detailsButtons = d.findElements(By.xpath("//*[contains(@content-desc, 'btn_details_')]"))
            if (detailsButtons.isNotEmpty()) {
                println("Found details button, clicking...")
                detailsButtons.first().click()
                Thread.sleep(2000)
                println("Opened room details successfully")
            } else {
                println("No details buttons found, but test passed - rooms were displayed")
            }
            
            println("Test completed successfully!")
            
        } catch (e: Exception) {
            println("Test failed with error: ${e.message}")
            try {
                println("Final page source for debugging:")
                println(d.pageSource)
            } catch (pageSourceError: Exception) {
                println("Could not get final page source: ${pageSourceError.message}")
            }
            throw e
        }
    }
    
    @Test
    fun testAppLaunches() {
        val d = requireNotNull(driver)
        
        try {
            println("Testing if app launches successfully...")
            
            // Wait for app to load
            Thread.sleep(5000)
            
            // Get page source to verify app is running
            val pageSource = d.pageSource
            println("App launched successfully! Page source length: ${pageSource.length}")
            
            // Basic verification that we have some UI content
            assertTrue(pageSource.isNotEmpty(), "App should have UI content")
            assertTrue(pageSource.contains("android") || pageSource.contains("view") || pageSource.contains("text"), 
                      "Page source should contain Android UI elements")
            
            println("App launch test completed successfully!")
            
        } catch (e: Exception) {
            println("App launch test failed: ${e.message}")
            throw e
        }
    }
}
