package base;

import com.google.common.io.Files;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.events.EventFiringDecorator;
import org.testng.ITestResult;
import org.testng.annotations.*;

import pages.HomePage;
import utils.EventReporter;
import utils.CookieManager;
import utils.WindowManager;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

/**
 * It’s the test infrastructure Base Class that all test classes inherits from it. <br>
 * It contains:<br><hr>
 *   - Driver setup/teardown <br>
 *   - Browser options/config <br>
 *   - Hooks (@BeforeClass, @BeforeMethod, @AfterMethod) <br>
 *   - Global utilities (screenshots, listeners)
 */
public class BaseTests {
    private WebDriver driver;
    protected HomePage homePage;
    protected String domainURL = "the-internet.herokuapp.com";

    @BeforeClass
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "resources/chromedriver");
        WebDriver chromeDriver = new ChromeDriver(getChromeOptions());
        driver = new EventFiringDecorator(new EventReporter()).decorate(chromeDriver);
        configureTimeouts();
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
    }

    @BeforeMethod
    public void openHomePage() {
        driver.get("https://" + domainURL);
        homePage = new HomePage(driver);
        var cookieManager = getCookieManager();
        cookieManager.setCookie(domainURL); // Always open the page BEFORE adding cookies
    }

    /**
     * Take a screenshot when test-method failed,
     * save it to /resources/screenshots folder,
     * and rename it by the failed test-method's name.
     * @param result contains the test method result (succeeded & failed)
     */
    @AfterMethod
    public void recordFailure(ITestResult result){
        if(ITestResult.FAILURE == result.getStatus())
        {
            var camera = (TakesScreenshot)driver;
            File screenshot = camera.getScreenshotAs(OutputType.FILE);
            try{
                Files.move(screenshot, new File("resources/screenshots/" + result.getName() + ".png"));
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * A private method that sets some options to the automated chrome webdriver.<br>
     * It returns an instance of ChromeOptions class.<br>
     * Call this method when instantiating the chrome webdriver.
     * @return ChromeOptions instance.
     */
    private ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized"); // to open chrome in the maximized mode
        // options.addArguments("--headless"); // this will run the tests without opening the browser
        return options;
    }

    public CookieManager getCookieManager(){
        return new CookieManager(driver);
    }

    public WindowManager getWindowManager() {
        return new WindowManager(driver);
    }

    /**
     * This is a private method to configue the timeout throughout the whole project's tests.
     */
    private void configureTimeouts() {
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10)); // To set the amount of time to wait for a page load to complete before it throws an error.
        // driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30)); // this is on the whole project
        // driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(10)); // To set the amount of time to wait for ((asynchronous)) scripts to finish executing.
    }
}
