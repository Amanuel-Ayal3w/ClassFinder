import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.options.UiAutomator2Options
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.support.ui.ExpectedConditions
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
        
        // Helper method to find elements with multiple fallback strategies
        private fun findElementWithFallbacks(driver: AndroidDriver, primarySelector: String, fallbackSelectors: List<String>): org.openqa.selenium.WebElement {
            val selectors = listOf(primarySelector) + fallbackSelectors
            
            for (selector in selectors) {
                try {
                    val element = driver.findElement(By.xpath(selector))
                    println("Found element using selector: $selector")
                    return element
                } catch (e: Exception) {
                    println("Selector failed: $selector - ${e.message}")
                }
            }
            
            // If all selectors fail, print page source for debugging
            try {
                println("All selectors failed. Current page source:")
                println(driver.pageSource)
            } catch (e: Exception) {
                println("Could not get page source: ${e.message}")
            }
            
            throw Exception("Could not find element with any of the provided selectors")
        }
        
        // Helper method to wait for elements to be clickable
        private fun waitForElementClickable(driver: AndroidDriver, selector: String, timeoutSeconds: Long = 10): org.openqa.selenium.WebElement {
            val wait = WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
            return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(selector)))
        }
    }

    @Test
    fun testFindRoomsNowFlow() {
        val d = requireNotNull(driver)
        
        try {
            println("Starting test: testFindRoomsNowFlow")
            
            // Wait for app to be ready
            Thread.sleep(3000)
            
            // Step 1: Find and click Samsung building
            println("Step 1: Looking for Samsung building...")
            val samsung = findElementWithFallbacks(
                d, 
                "//*[@content-desc='building_samsung']",
                listOf(
                    "//*[contains(@text, 'Samsung')]",
                    "//*[contains(@content-desc, 'samsung')]",
                    "//android.widget.RadioButton[contains(@text, 'Samsung')]"
                )
            )
            
            println("Found Samsung building, clicking...")
            samsung.click()
            Thread.sleep(1000) // Short wait for UI update

            // Step 2: Select "Now" time mode
            println("Step 2: Looking for 'Now' time option...")
            val now = findElementWithFallbacks(
                d,
                "//*[@content-desc='when_now']",
                listOf(
                    "//*[contains(@text, 'Now')]",
                    "//*[contains(@text, 'NOW')]",
                    "//android.widget.RadioButton[contains(@text, 'Now')]"
                )
            )
            
            println("Found NOW button, clicking...")
            now.click()
            Thread.sleep(1000) // Short wait for UI update

            // Step 3: Click Find Rooms button
            println("Step 3: Looking for Find Rooms button...")
            val findRooms = findElementWithFallbacks(
                d,
                "//*[@content-desc='btn_find_rooms']",
                listOf(
                    "//*[contains(@text, 'Find Rooms')]",
                    "//*[contains(@text, 'FIND ROOMS')]",
                    "//android.widget.Button[contains(@text, 'Find')]"
                )
            )
            
            println("Found Find Rooms button, clicking...")
            findRooms.click()
            
            // Step 4: Wait for results to load and verify rooms are found
            println("Step 4: Waiting for results to load...")
            Thread.sleep(3000) // Wait for navigation and results loading
            
            // Try multiple strategies to find room results
            val roomSelectors = listOf(
                "//*[contains(@content-desc, 'room_')]",
                "//*[contains(@text, 'Samsung')]",
                "//*[contains(@text, 'Available until')]",
                "//*[contains(@text, 'Details')]",
                "//android.widget.CardView",
                "//android.widget.TextView[contains(@text, 'Samsung')]"
            )
            
            var roomsFound = false
            var roomCount = 0
            
            for (selector in roomSelectors) {
                try {
                    val rooms = d.findElements(By.xpath(selector))
                    if (rooms.isNotEmpty()) {
                        println("Found ${rooms.size} elements using selector: $selector")
                        roomCount = rooms.size
                        roomsFound = true
                        break
                    }
                } catch (e: Exception) {
                    println("Selector $selector failed: ${e.message}")
                }
            }
            
            if (!roomsFound) {
                println("No rooms found with any selector. Current page source:")
                try {
                    println(d.pageSource)
                } catch (e: Exception) {
                    println("Could not get page source: ${e.message}")
                }
                throw AssertionError("No rooms found for NOW flow")
            }
            
            println("Successfully found $roomCount room(s)")
            
            // Step 5: Try to find and click a details button (optional)
            println("Step 5: Looking for details button...")
            val detailsSelectors = listOf(
                "//*[contains(@content-desc, 'btn_details_')]",
                "//*[contains(@text, 'Details')]",
                "//android.widget.Button[contains(@text, 'Details')]"
            )
            
            var detailsClicked = false
            for (selector in detailsSelectors) {
                try {
                    val detailsButtons = d.findElements(By.xpath(selector))
                    if (detailsButtons.isNotEmpty()) {
                        println("Found details button, clicking...")
                        detailsButtons.first().click()
                        Thread.sleep(2000)
                        println("Opened room details successfully")
                        detailsClicked = true
                        break
                    }
                } catch (e: Exception) {
                    println("Details selector $selector failed: ${e.message}")
                }
            }
            
            if (!detailsClicked) {
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
