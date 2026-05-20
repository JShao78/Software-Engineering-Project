package de.pdbm.team18.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

class KundenSeleniumIT {

    private WebDriver driver;
    private static final String BASE_URL = System.getProperty("app.baseUrl", "http://localhost:8080/team-18/");

    @Test
    void createCustomerShouldWork() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);

        driver.get(BASE_URL + "kunden.xhtml");
        driver.findElement(By.cssSelector("input[type='text']")).sendKeys("fabiola.jackson@bikes.shop");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("555-5554");
        driver.findElement(By.cssSelector("button[type='submit'], input[type='submit']")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.add-modal-btn")));

        // 3. Open modal
        driver.findElement(By.cssSelector("a.add-modal-btn")).click();

        // 4. Wait for form fields to be visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("kundenForm:vorname")));

        // 5. Fill in form
        driver.findElement(By.id("kundenForm:vorname")).sendKeys("Max");
        driver.findElement(By.id("kundenForm:nachname")).sendKeys("Mustermann");
        driver.findElement(By.id("kundenForm:email")).sendKeys("max@example.com");
        driver.findElement(By.id("kundenForm:phone")).sendKeys("0123456789");
        driver.findElement(By.id("kundenForm:street")).sendKeys("Musterstraße 1");
        driver.findElement(By.id("kundenForm:state")).sendKeys("Niedersachsen");
        driver.findElement(By.id("kundenForm:zipCode")).sendKeys("38100");

        // 6. Click Speichern (input type=submit with class btn-save)
        driver.findElement(By.cssSelector("input.btn-save")).click();

    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}