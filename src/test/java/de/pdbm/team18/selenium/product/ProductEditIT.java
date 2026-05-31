package de.pdbm.team18.selenium.product;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductEditIT {

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    private static final String ADMIN_USER = "fabiola.jackson@bikes.shop";
    private static final String ADMIN_PASSWORD = "555-5554";

    @BeforeEach
    void setUp() {
        baseUrl = System.getProperty("app.baseUrl", "http://localhost:8080/team-18/");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1400,1000");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--remote-allow-origins=*");

        if (Boolean.getBoolean("headless")) {
            options.addArguments("--headless=new");
        }

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void shouldEditProduct() {
        String newProductName = "Selenium Edited Product " + System.currentTimeMillis();

        driver.get(pageUrl("produkte.xhtml"));
        loginAsAdminIfNeeded();

        waitForProductPage();

        WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//form[@id='productsTableForm']//input[@value='Bearbeiten'])[1]")
        ));

        click(editButton);

        WebElement productNameInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("editProductForm:editProductName"))
        );

        clearAndType(productNameInput, newProductName);
        clearAndType(driver.findElement(By.id("editProductForm:editModelYear")), "2026");
        clearAndType(driver.findElement(By.id("editProductForm:editListPrice")), "888.88");

        WebElement saveButton = driver.findElement(
                By.xpath("//form[@id='editProductForm']//input[@type='submit' and @value='Änderungen speichern']")
        );

        click(saveButton);

        try {
            wait.until(driver -> {
                try {
                    return driver.findElement(By.tagName("body")).getText().contains(newProductName);
                } catch (NoSuchElementException | StaleElementReferenceException ex) {
                    return false;
                }
            });
        } catch (TimeoutException ex) {
            fail("Das Produkt wurde nicht bearbeitet oder die Tabelle wurde nicht aktualisiert.\nAktuelle URL: "
                    + driver.getCurrentUrl()
                    + "\nSeitentext:\n"
                    + driver.findElement(By.tagName("body")).getText());
        }

        assertTrue(
                driver.findElement(By.tagName("body")).getText().contains(newProductName),
                "Der neue Produktname wurde nach dem Speichern nicht in der Tabelle gefunden."
        );
    }

    private void loginAsAdminIfNeeded() {
        if (isProductPageVisible()) {
            return;
        }

        driver.get(pageUrl("login.xhtml"));

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("loginForm:username")
        ));
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("loginForm:password")
        ));
        clearAndType(usernameField, ADMIN_USER);
        clearAndType(passwordField, ADMIN_PASSWORD);

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("loginForm:loginButton")
        ));

        submitButton.click();

        driver.get(pageUrl("produkte.xhtml"));
        waitForProductPage();
    }

    private void waitForProductPage() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".page-title")));
            assertEquals("Produkte", driver.findElement(By.cssSelector(".page-title")).getText().trim());
        } catch (TimeoutException ex) {
            fail("Produktseite wurde nicht geöffnet. Wahrscheinlich Login-Problem oder falsche URL.\nAktuelle URL: "
                    + driver.getCurrentUrl()
                    + "\nSeitentext:\n"
                    + driver.findElement(By.tagName("body")).getText());
        }
    }

    private boolean isProductPageVisible() {
        List<WebElement> titles = driver.findElements(By.cssSelector(".page-title"));

        return !titles.isEmpty()
                && titles.get(0).isDisplayed()
                && titles.get(0).getText().trim().equals("Produkte");
    }

    private void clearAndType(WebElement element, String text) {
        element.clear();
        element.sendKeys(text);
    }

    private void click(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    private String pageUrl(String page) {
        if (baseUrl.endsWith("/")) {
            return baseUrl + page;
        }

        return baseUrl + "/" + page;
    }
}
