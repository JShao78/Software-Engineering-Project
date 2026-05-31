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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class ProductCreateIT {

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
    void shouldCreateProduct() {
        String productName = "Selenium Test Produkt " + System.currentTimeMillis();

        driver.get(pageUrl("produkte.xhtml"));
        loginAsAdminIfNeeded();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".page-title")));
        assertEquals("Produkte", driver.findElement(By.cssSelector(".page-title")).getText().trim());

        int countBefore = readProductCount();

        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a.add-modal-btn[href='#add-product-modal']")
        ));

        addButton.click();

        wait.until(driver -> "#add-product-modal".equals(
                ((JavascriptExecutor) driver).executeScript("return window.location.hash")
        ));

        WebElement productNameInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("addProductForm:productName"))
        );

        productNameInput.clear();
        productNameInput.sendKeys(productName);

        WebElement modelYearInput = driver.findElement(By.id("addProductForm:modelYear"));
        modelYearInput.clear();
        modelYearInput.sendKeys("2026");

        WebElement listPriceInput = driver.findElement(By.id("addProductForm:listPrice"));
        listPriceInput.clear();
        listPriceInput.sendKeys("999.99");

        selectFirstRealOption(By.id("addProductForm:brandId"));
        selectFirstRealOption(By.id("addProductForm:categoryId"));

        WebElement saveButton = driver.findElement(
                By.xpath("//form[@id='addProductForm']//input[@type='submit' and @value='Speichern']")
        );

        saveButton.click();

        try {
            wait.until(driver -> {
                try {
                    return readProductCount() == countBefore + 1;
                } catch (NoSuchElementException | StaleElementReferenceException | AssertionError ex) {
                    return false;
                }
            });
        } catch (TimeoutException ex) {
            fail("Das Produkt wurde nicht erstellt oder die Seite wurde nicht aktualisiert.\nAktuelle URL: "
                    + driver.getCurrentUrl()
                    + "\nSeitentext:\n"
                    + driver.findElement(By.tagName("body")).getText());
        }

        assertEquals(countBefore + 1, readProductCount());
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
        usernameField.clear();
        usernameField.sendKeys(ADMIN_USER);

        passwordField.clear();
        passwordField.sendKeys(ADMIN_PASSWORD);

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("loginForm:loginButton")
        ));

        submitButton.click();

        driver.get(pageUrl("produkte.xhtml"));

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".page-title")));
        } catch (TimeoutException ex) {
            fail("Login hat nicht funktioniert oder Produktseite wurde nicht geöffnet.\nAktuelle URL: "
                    + driver.getCurrentUrl()
                    + "\nSeitentext:\n"
                    + driver.findElement(By.tagName("body")).getText());
        }
    }

    private boolean isProductPageVisible() {
        return !driver.findElements(By.cssSelector(".page-title")).isEmpty()
                && driver.findElement(By.cssSelector(".page-title")).getText().trim().equals("Produkte");
    }

    private void selectFirstRealOption(By selectLocator) {
        WebElement selectElement = wait.until(ExpectedConditions.elementToBeClickable(selectLocator));
        Select select = new Select(selectElement);

        for (WebElement option : select.getOptions()) {
            String value = option.getAttribute("value");

            if (value != null && !value.isBlank()) {
                select.selectByValue(value);
                return;
            }
        }

        fail("In der Selectbox wurde keine gültige Option gefunden: " + selectLocator);
    }

    private int readProductCount() {
        String text = driver.findElement(By.cssSelector(".page-subtitle")).getText();

        Matcher matcher = Pattern.compile("\\d+").matcher(text);

        assertTrue(matcher.find(), "Keine Produktanzahl gefunden in: " + text);

        return Integer.parseInt(matcher.group());
    }

    private String pageUrl(String page) {
        if (baseUrl.endsWith("/")) {
            return baseUrl + page;
        }

        return baseUrl + "/" + page;
    }
}
