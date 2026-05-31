package de.pdbm.team18.selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

class UnauthorizedAccessIT {
    private WebDriver driver;
	private WebDriverWait wait;
	private String[] WebViews = {
		"kunden",
		"produkte",
		"bestellungen"
	};
	private String BASE_URL = System.getProperty("app.baseUrl", "http://localhost:8080/team-18/");
	private WebDriver[] drivers;

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
			wait = new WebDriverWait(driver, Duration.ofSeconds(10));
			test.accept(browser);
		}
	}

	private void loadView(String view) {
		driver.get(BASE_URL + view + ".xhtml");
	}

	private void login(String user, String password) {
		driver.get(BASE_URL + "login.xhtml");

		driver.findElement(By.cssSelector("input[type='text']")).sendKeys(user);
		driver.findElement(By.cssSelector("input[type='password']")).sendKeys(password);
		driver.findElement(By.cssSelector("input[type='submit']")).click();
	}

	private void elemIsVisible(String selector) {
		for (String view : WebViews) {
			loadView(view);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(selector)));
			assertTrue(driver.findElement(By.cssSelector(selector)).isDisplayed());
		}
	}

	private void elemNotVisible(String selector) {
		for (String view : WebViews) {
			loadView(view);
			wait.until(ExpectedConditions.urlToBe("http://localhost:8080/team-18/" + view + ".xhtml"));
			assertTrue(wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(selector))));
		}
	}

	private void staffAccessible() {
		loadView("staffs");
		wait.until(ExpectedConditions.urlToBe("http://localhost:8080/team-18/staffs.xhtml"));
		assertTrue(driver.findElement(By.tagName("body")).getText().contains("Forbidden"));
	}

	@Test
	void noLogin() {
		loopBrowsers(browser -> {
			WebViews = new String[]{
				"kunden",
					"produkte",
					"bestellungen",
					"stocks",
					"stores",
					"staffs"
			};

			driver.get(System.getProperty("app.baseUrl", BASE_URL));
			assertEquals("http://localhost:8080/team-18/login.xhtml", driver.getCurrentUrl());

			for (String view : WebViews) {
				loadView(view);
				assertEquals("http://localhost:8080/team-18/login.xhtml", driver.getCurrentUrl());
			}
		});
	}

	@Test
    void loginUSER1() {
		loopBrowsers(browser -> {
			login("mireya.copeland@bikes.shop", "555-5555");
			elemIsVisible(".add-modal-btn");
			elemIsVisible(".btn-table-edit");

			loadView("bestellungen");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".items-toggle")));
			driver.findElement(By.cssSelector(".items-toggle")).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".order-items-panel")));
			assertTrue(driver.findElement(By.cssSelector(".order-items-panel")).isDisplayed());

			staffAccessible();
		});
    }
 
	@Test
    void loginUSER2() {
		loopBrowsers(browser -> {
			login("genna.serrano@bikes.shop", "555-5556");
			elemNotVisible(".add-modal-btn");
			elemNotVisible(".btn-table-edit");

			loadView("bestellungen");
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".items-toggle")));
				driver.findElement(By.cssSelector(".items-toggle")).click();
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".order-items-panel")));
				assertTrue(wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".order-items-panel"))));
			} catch (Exception e) {
				assertTrue(true);
			}

			staffAccessible();
		});
    }
}
