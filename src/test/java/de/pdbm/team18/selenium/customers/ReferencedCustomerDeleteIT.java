package de.pdbm.team18.selenium.customers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ReferencedCustomerDeleteIT {

    private static final String BASE_URL = System.getProperty("app.baseUrl", "http://localhost:8080/team-18/");
    private static final int CUSTOMER_ID = 1;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void referencedCustomerShouldStillExistAfterThreeDeleteAttempts() {
        loginAsAdmin();
        openCustomerTab();

        for (int attempt = 0; attempt < 3; attempt++) {
            clickDeleteForCustomer(CUSTOMER_ID);
        }

        Assertions.assertTrue(isCustomerVisible(CUSTOMER_ID),
                "Customer with id " + CUSTOMER_ID + " should still exist after three delete attempts.");
    }

    private void loginAsAdmin() {
        driver.get(BASE_URL + "login.xhtml");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm:username")))
                .sendKeys("fabiola.jackson@bikes.shop");
        driver.findElement(By.id("loginForm:password")).sendKeys("555-5554");
        driver.findElement(By.id("loginForm:loginButton")).click();
    }

    private void openCustomerTab() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Kunden"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(rowLocator(CUSTOMER_ID)));
    }

    private void clickDeleteForCustomer(int customerId) {
        By locator = rowLocator(customerId);
        WebElement row = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        WebElement deleteButton = row.findElement(By.cssSelector("td.delete input[type='submit']"));
        wait.until(ExpectedConditions.elementToBeClickable(deleteButton)).click();
        wait.until(driver -> isCustomerVisible(customerId));
    }

    private By rowLocator(int customerId) {
        return By.xpath("//table[contains(@class,'data-table')]//tr[td[span[normalize-space(text())='" + customerId + "']]]");
    }

    private boolean isCustomerVisible(int customerId) {
        try {
            return driver.findElement(rowLocator(customerId)).isDisplayed();
        } catch (NoSuchElementException ignored) {
            return false;
        }
    }
}
