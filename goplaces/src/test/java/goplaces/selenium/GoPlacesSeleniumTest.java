//package goplaces.selenium;
//
//import static org.junit.Assert.*;
//
//import java.io.File;
//import java.io.IOException;
//
//import org.apache.commons.io.FileUtils;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.openqa.selenium.By;
//import org.openqa.selenium.OutputType;
//import org.openqa.selenium.TakesScreenshot;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.chrome.ChromeDriver;
//
//import com.thoughtworks.selenium.ScreenshotListener;
//
//public class GoPlacesSeleniumTest {
//
//	private String baseUrl;
//	private WebDriver driver;
//	private ScreenshotHelper screenshotHelper;
//
//	@Before
//	public void openBrowser() {
//		System.setProperty("webdriver.chrome.driver", "chromedriver");
//		driver = new ChromeDriver();
//		screenshotHelper = new ScreenshotHelper();
//	}
//
//	@After
//	public void saveScreenshotAndCloseBrowser() throws IOException {
////		screenshotHelper.saveScreenshot("screenshot.png");
//		driver.quit();
//	}
//
//	@Test
//	public void pageTitleAfterSearchShouldBeginWithDrupal() throws InterruptedException {
//		driver.get("http://go-places-ucsb.appspot.com/");
////		driver.get("http://localhost:8080/");
//		assertEquals("Go Places", driver.getTitle());
//
//		WebElement findInitialRouteTitle = driver.findElement(By.cssSelector(".places-form h2"));
//		assertEquals("1. Find the initial route", findInitialRouteTitle.getText());
//
//		WebElement insertWaypointsTitle = driver.findElement(By.cssSelector(".waypoints-form h2"));
//		assertEquals("2. Insert the waypoint categories", insertWaypointsTitle.getText());
//
//		WebElement googleMap = driver.findElement(By.cssSelector("#map .gm-style"));
//		assertTrue(googleMap.isDisplayed());
//
//		WebElement originPlace = driver.findElement(By.cssSelector(".js-places-form-origin"));
//		assertEquals("Origin place", originPlace.getAttribute("placeholder"));
//
//		WebElement destinationPlace = driver.findElement(By.cssSelector(".js-places-form-destination"));
//		assertEquals("Destination place", destinationPlace.getAttribute("placeholder"));
//
//		originPlace.sendKeys("6636 Del Playa dr, Isla Vista");
//		destinationPlace.sendKeys("Santa Barbara");
//
//		WebElement findInitialRouteSubmitBtn = driver.findElement(By.cssSelector(".js-places-form-submit"));
//		findInitialRouteSubmitBtn.click();
//
//		Thread.sleep(5000);
//
//		WebElement firstWaypointCategoryTextField = driver.findElement(By.cssSelector(".waypoints-form .js-waypoint-form-textfield"));
//		firstWaypointCategoryTextField.sendKeys("café");
//
//		WebElement waypointCategoriesSubmitBtm = driver.findElement(By.cssSelector(".js-waypoints-form-submit-btn"));
//		waypointCategoriesSubmitBtm.click();
//
////		Thread.sleep(10000);
//
//		// We should assert presence of google map markers here
//		// Find a way to retrieve the css selectors and interact with them
//		// Then we have to test the presence of the submit btn and click on it to submit the finalized route
//
//	}
//
//	private class ScreenshotHelper {
//
//		public void saveScreenshot(String screenshotFileName) throws IOException {
//			File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
//			FileUtils.copyFile(screenshot, new File(screenshotFileName));
//	    }
//	}
//}
