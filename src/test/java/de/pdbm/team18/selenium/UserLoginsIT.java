package de.pdbm.team18.selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

class UserLoginsIT {
    private WebDriver driver;
    private static final String BASE_URL = System.getProperty("app.baseUrl", "http://localhost:8080/team-18/login.xhtml");
	private String expectedTitle = "Home – Vertriebsunterstützung";

	@BeforeEach
	void setup() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless=new");
		driver = new ChromeDriver(options);
	}

	@AfterEach
	void tearDown() { driver.quit(); }

	private void performLogin(String username, String password) {
		driver.get(BASE_URL);

        driver.findElement(By.cssSelector("input[type='text']")).sendKeys(username);
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys(password);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    	wait.until(ExpectedConditions.titleIs(expectedTitle));

		assertEquals(expectedTitle, driver.getTitle());
	}

	@Test
    void loginADMINshouldWork() {
		performLogin("fabiola.jackson@bikes.shop", "555-5554");
	}

	@Test
    void loginUSER1shouldWork() {
		performLogin("mireya.copeland@bikes.shop", "555-5555");
    }

    @Test
    void loginUSER2shouldWork() {
		performLogin("genna.serrano@bikes.shop", "555-5556");
    }
}
