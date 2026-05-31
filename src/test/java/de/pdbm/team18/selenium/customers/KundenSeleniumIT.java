package de.pdbm.team18.selenium.customers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class KundenSeleniumIT {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String BASE_URL = System.getProperty("app.baseUrl", "http://localhost:8080/team-18/");
    private static final String ADMIN_USER = "fabiola.jackson@bikes.shop";
    private static final String ADMIN_PASSWORD = "555-5554";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    @Test
    void createCustomerShouldWork() {
        openBrowser();

        String email = "selenium.kunde." + System.currentTimeMillis() + "@test.com";

        openCustomerPageAsAdmin();
        int countBefore = readCustomerCount();

        openCreateCustomerModal();

        driver.findElement(By.id("kundenForm:vorname")).sendKeys("Max");
        driver.findElement(By.id("kundenForm:nachname")).sendKeys("Mustermann");
        driver.findElement(By.id("kundenForm:email")).sendKeys(email);
        driver.findElement(By.id("kundenForm:phone")).sendKeys("(123) 456-789");
        driver.findElement(By.id("kundenForm:street")).sendKeys("Musterstraße 1");
        driver.findElement(By.id("kundenForm:state")).sendKeys("Niedersachsen");
        driver.findElement(By.id("kundenForm:zipCode")).sendKeys("38100");

        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input.btn-save")));
        saveButton.click();

        waitUntilCustomerCountIs(countBefore + 1);

        assertEquals(
                countBefore + 1,
                readCustomerCount(),
                "Nach dem Speichern sollte genau ein neuer Kunde angelegt sein. E-Mail: " + email
        );
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1400,1000");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--remote-allow-origins=*");

        if (Boolean.getBoolean("headless")) {
            options.addArguments("--headless=new");
        }

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    private void openCustomerPageAsAdmin() {
        driver.get(BASE_URL + "kunden.xhtml");

        if (!isCustomerPageVisible()) {
            loginAsAdmin();
            driver.get(BASE_URL + "kunden.xhtml");
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.add-modal-btn")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".page-subtitle")));
    }

    private void loginAsAdmin() {
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm:username")));
        WebElement passwordInput = driver.findElement(By.id("loginForm:password"));

        usernameInput.clear();
        usernameInput.sendKeys(ADMIN_USER);
        passwordInput.clear();
        passwordInput.sendKeys(ADMIN_PASSWORD);

        wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:loginButton"))).click();
        wait.until(driver -> !driver.getCurrentUrl().contains("login.xhtml"));
    }

    private boolean isCustomerPageVisible() {
        try {
            return !driver.findElements(By.cssSelector("a.add-modal-btn")).isEmpty()
                    || driver.findElement(By.tagName("body")).getText().contains("Kunden");
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    private void openCreateCustomerModal() {
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.add-modal-btn")));
        addButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("kundenForm:vorname")));
    }

    private int readCustomerCount() {
        String subtitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".page-subtitle"))).getText();
        Matcher matcher = NUMBER_PATTERN.matcher(subtitle);

        if (!matcher.find()) {
            fail("Kundenanzahl konnte nicht gelesen werden. Text war: " + subtitle);
        }

        return Integer.parseInt(matcher.group());
    }

    private void waitUntilCustomerCountIs(int expectedCount) {
        try {
            wait.until(driver -> {
                try {
                    return readCustomerCount() == expectedCount;
                } catch (NoSuchElementException | StaleElementReferenceException | AssertionError ex) {
                    return false;
                }
            });
        } catch (TimeoutException ex) {
            fail("Kundenanzahl wurde nicht aktualisiert. Erwartet: "
                    + expectedCount
                    + ", aktuell: " + readCustomerCount()
                    + "\nAktuelle URL: " + driver.getCurrentUrl()
                    + "\nSeitentext:\n" + driver.findElement(By.tagName("body")).getText());
        }
    }
}
