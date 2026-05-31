package de.pdbm.team18.selenium.orders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.openqa.selenium.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

class SearchOrdersIT {
    private WebDriver driver;
	private WebDriver[] drivers;
	private WebDriverWait wait;
	private static final String BASE_URL = System.getProperty("app.baseUrl", "http://localhost:8080/team-18/");
	private String prefix = "ordersSearchForm:";
	
	private String user = "Jamaal Albert";
	private String product = "Electra Townie Original 7D EQ - Women's - 2016";

	@BeforeEach
	void setup() {
		ChromeOptions chromeOpts = new ChromeOptions();
		chromeOpts.addArguments("--headless=new");

		drivers = new WebDriver[]{
			new ChromeDriver(chromeOpts)
		};
	}

	@AfterEach
	void tearDown() {
        if (drivers != null) {
            for (WebDriver browser : drivers) {
                if (browser != null) {
                    browser.quit();
                }
            }
        }
	}

	private void loopBrowsers(Consumer<WebDriver> test) {
		for (WebDriver browser : drivers) {
			driver = browser;
			wait = new WebDriverWait(driver, Duration.ofSeconds(5));
			login("mireya.copeland@bikes.shop", "555-5555");
			driver.get(BASE_URL + "bestellungen.xhtml");
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.data-table")));
			test.accept(browser);
		}
	}

	private void login(String user, String password) {
		driver.get(BASE_URL + "login.xhtml");

		driver.findElement(By.cssSelector("input[type='text']")).sendKeys(user);
		driver.findElement(By.cssSelector("input[type='password']")).sendKeys(password);
		driver.findElement(By.cssSelector("input[type='submit']")).click();
	}

	private void userLookup(String username) {
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(prefix + "customerSearch")));
		driver.findElement(By.id(prefix + "customerSearch")).sendKeys(username);
	}
	private void productLookup(String productname) {
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(prefix + "productSearch")));
		driver.findElement(By.id(prefix + "productSearch")).sendKeys(productname);
	}

	private void submit() {
		WebElement searchButton = driver.findElement(By.className("search-submit"));
		wait.until(ExpectedConditions.elementToBeClickable(searchButton));
		searchButton.click();

		wait.until(ExpectedConditions.presenceOfElementLocated(
			By.cssSelector("table.data-table tbody tr")
		));
	}

	private void checkUser(String username) {
		By userLocator = By.xpath(
			"//table[contains(@class,'data-table')]//tr[td[contains(normalize-space(.),'" 
			+ username + "')]]"
		);
		wait.until(ExpectedConditions.visibilityOfElementLocated(userLocator));
		assertTrue(driver.findElement(userLocator).isDisplayed());
	}
	private void checkProduct(String productname) {
		By toggleLocator = By.cssSelector(
			"table.data-table .items-toggle"
		);
		WebElement toggle = wait.until(
			ExpectedConditions.elementToBeClickable(toggleLocator)
		);
		toggle.click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.items-table")));
		wait.until(driver -> driver.findElements(By.cssSelector("table.items-table tbody tr")).stream()
			.anyMatch(row -> row.getText().contains(productname)));
		assertTrue(driver.findElements(By.cssSelector("table.items-table tbody tr")).stream()
			.anyMatch(row -> row.getText().contains(productname)));
	}

	@Test
    void searchByUser() {
		loopBrowsers(browser -> {
			userLookup(user);
			submit();
			checkUser(user);
		});
	}

	@Test
    void searchByProduct() {
		loopBrowsers(browser -> {
			productLookup(product);
			submit();
			checkProduct(product);
		});
	}

	@Test
    void searchByBoth() {
		loopBrowsers(browser -> {
			userLookup(user);
			productLookup(product);
			submit();
			checkUser(user);
			checkProduct(product);
		});
	}
}
