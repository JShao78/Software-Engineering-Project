package de.pdbm.team18.selenium.staff;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class StaffCreateIT {

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
    void shouldCreateStaff() {
        long timestamp = System.currentTimeMillis();

        String firstName = "Selenium";
        String lastName = "Staff" + timestamp;
        String email = "selenium.staff." + timestamp + "@bikes.shop";
        String phone = "(123) 456-7890";

        driver.get(pageUrl("staffs.xhtml"));
        loginAsAdminIfNeeded();

        waitForStaffPage();

        int countBefore = readStaffCount();

        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a.add-modal-btn")
        ));

        click(addButton);

        wait.until(driver -> {
            Object display = ((JavascriptExecutor) driver)
                    .executeScript("return window.getComputedStyle(document.getElementById('staffModal')).display");
            return !"none".equals(display);
        });

        clearAndType(driver.findElement(By.id("staffForm:vorname")), firstName);
        clearAndType(driver.findElement(By.id("staffForm:nachname")), lastName);
        clearAndType(driver.findElement(By.id("staffForm:email")), email);
        clearAndType(driver.findElement(By.id("staffForm:phone")), phone);

        selectFirstRealOption(By.id("staffForm:createStore"));

        WebElement saveButton = driver.findElement(
                By.xpath("//form[@id='staffForm']//input[@type='submit' and @value='Mitarbeiter speichern']")
        );

        click(saveButton);

        try {
            wait.until(driver -> {
                try {
                    String bodyText = driver.findElement(By.tagName("body")).getText();

                    return readStaffCount() == countBefore + 1
                            && bodyText.contains(firstName)
                            && bodyText.contains(lastName)
                            && bodyText.contains(email);
                } catch (NoSuchElementException | StaleElementReferenceException | AssertionError ex) {
                    return false;
                }
            });
        } catch (TimeoutException ex) {
            fail("Der Mitarbeiter wurde nicht erstellt oder die Tabelle wurde nicht aktualisiert.\nAktuelle URL: "
                    + driver.getCurrentUrl()
                    + "\nSeitentext:\n"
                    + driver.findElement(By.tagName("body")).getText());
        }

        String bodyText = driver.findElement(By.tagName("body")).getText();

        assertEquals(countBefore + 1, readStaffCount());
        assertTrue(bodyText.contains(firstName));
        assertTrue(bodyText.contains(lastName));
        assertTrue(bodyText.contains(email));
    }

    private void loginAsAdminIfNeeded() {
        if (isStaffPageVisible()) {
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

        driver.get(pageUrl("staffs.xhtml"));
        waitForStaffPage();
    }

    private void waitForStaffPage() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".page-title")));
            assertEquals("Mitarbeiter", driver.findElement(By.cssSelector(".page-title")).getText().trim());
        } catch (TimeoutException ex) {
            fail("Mitarbeiterseite wurde nicht geöffnet. Wahrscheinlich Login-Problem oder falsche URL.\nAktuelle URL: "
                    + driver.getCurrentUrl()
                    + "\nSeitentext:\n"
                    + driver.findElement(By.tagName("body")).getText());
        }
    }

    private boolean isStaffPageVisible() {
        List<WebElement> titles = driver.findElements(By.cssSelector(".page-title"));

        return !titles.isEmpty()
                && titles.get(0).isDisplayed()
                && titles.get(0).getText().trim().equals("Mitarbeiter");
    }

    private int readStaffCount() {
        String text = driver.findElement(By.cssSelector(".page-subtitle")).getText();

        Matcher matcher = Pattern.compile("\\d+").matcher(text);

        assertTrue(matcher.find(), "Keine Mitarbeiteranzahl gefunden in: " + text);

        return Integer.parseInt(matcher.group());
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

        fail("In der Selectbox wurde keine gültige Filiale gefunden: " + selectLocator);
    }

    private void clearAndType(WebElement element, String text) {
        element.click();
        element.sendKeys(Keys.CONTROL, "a");
        element.sendKeys(Keys.DELETE);
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
