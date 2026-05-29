package de.pdbm.team18.selenium.product;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ProductPaginationIT {

    private static final By PAGINATION_INFO = By.cssSelector("form[id$='paginationForm'] .pagination-info");
    private static final By PAGINATION_NAV_BUTTONS = By.cssSelector("form[id$='paginationForm'] input.btn-page-nav[type='submit']");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("\\d+");

    private static final String ADMIN_USER = "fabiola.jackson@bikes.shop";
    private static final String ADMIN_PASSWORD = "555-5554";

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

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
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void shouldNotGoBeyondMinAndMaxPageOnProductsView() {
        loginAsAdmin();
        driver.get(pageUrl("produkte.xhtml"));
        waitForProductPage();

        assumeTrue(
                hasPagination(),
                "Für diesen Test werden mehrere Produktseiten benötigt. Aktuelle Produktanzahl: " + readProductCount()
        );

        PageState initialState = readPageState();
        assumeTrue(
                initialState.totalPages() > 1,
                "Für diesen Test werden mehrere Produktseiten benötigt. Aktuelle Seitenanzahl: " + initialState.totalPages()
        );
        assertEquals(1, initialState.currentPage(), "Pagination muss auf Seite 1 starten.");

        WebElement previousOnFirstPage = findPreviousButton();
        assertTrue(isDisabled(previousOnFirstPage), "Zurück-Button muss auf Seite 1 deaktiviert sein.");
        click(previousOnFirstPage);
        waitForCurrentPage(1);

        int maxPage = initialState.totalPages();
        for (int expectedPage = 2; expectedPage <= maxPage; expectedPage++) {
            WebElement nextButton = findNextButton();
            assertFalse(
                    isDisabled(nextButton),
                    "Weiter-Button darf vor der letzten Seite nicht deaktiviert sein."
            );

            click(nextButton);
            waitForCurrentPage(expectedPage);
        }

        assertEquals(maxPage, readPageState().currentPage(), "Die letzte Seite wurde nicht erreicht.");

        WebElement nextOnLastPage = findNextButton();
        assertTrue(isDisabled(nextOnLastPage), "Weiter-Button muss auf letzter Seite deaktiviert sein.");
        click(nextOnLastPage);
        waitForCurrentPage(maxPage);

        int finalPage = readPageState().currentPage();
        assertTrue(
                finalPage >= 1 && finalPage <= maxPage,
                "Aktuelle Seite muss zwischen 1 und " + maxPage + " bleiben, war aber " + finalPage + "."
        );
    }

    private boolean hasPagination() {
        return !driver.findElements(PAGINATION_INFO).isEmpty()
                && driver.findElements(PAGINATION_NAV_BUTTONS).size() >= 2;
    }

    private WebElement findPreviousButton() {
        return findNavigationButton(0);
    }

    private WebElement findNextButton() {
        return findNavigationButton(1);
    }

    private WebElement findNavigationButton(int index) {
        List<WebElement> buttons = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(PAGINATION_NAV_BUTTONS, 1));
        assertTrue(buttons.size() > index, "Erwarteter Navigationsbutton wurde nicht gefunden (Index " + index + ").");
        return buttons.get(index);
    }

    private boolean isDisabled(WebElement element) {
        return element.getAttribute("disabled") != null || !element.isEnabled();
    }

    private void waitForCurrentPage(int expectedPage) {
        try {
            wait.until(driver -> {
                try {
                    return readPageState().currentPage() == expectedPage;
                } catch (RuntimeException ex) {
                    return false;
                }
            });
        } catch (TimeoutException ex) {
            fail("Seitenwechsel auf Seite " + expectedPage + " wurde nicht erreicht.");
        }
    }

    private PageState readPageState() {
        String text = wait.until(ExpectedConditions.visibilityOfElementLocated(PAGINATION_INFO)).getText();
        Matcher matcher = INTEGER_PATTERN.matcher(text);
        List<Integer> numbers = new ArrayList<>(2);

        while (matcher.find() && numbers.size() < 2) {
            numbers.add(Integer.parseInt(matcher.group()));
        }

        assertTrue(numbers.size() == 2, "Pagination-Text konnte nicht gelesen werden: " + text);

        int currentPage = numbers.get(0);
        int totalPages = numbers.get(1);
        return new PageState(currentPage, totalPages);
    }

    private int readProductCount() {
        String text = driver.findElement(By.cssSelector(".page-subtitle")).getText();
        Matcher matcher = INTEGER_PATTERN.matcher(text);
        assertTrue(matcher.find(), "Keine Produktanzahl gefunden in: " + text);
        return Integer.parseInt(matcher.group());
    }

    private void loginAsAdmin() {
        driver.get(pageUrl("login.xhtml"));

        WebElement usernameField = findFirstVisibleElement(
                By.cssSelector("input[name='j_username']"),
                By.cssSelector("input[name='username']"),
                By.cssSelector("input[name='email']"),
                By.cssSelector("input[id*='username']"),
                By.cssSelector("input[id*='user']"),
                By.cssSelector("input[id*='email']"),
                By.cssSelector("input[type='email']"),
                By.cssSelector("input[type='text']")
        );

        WebElement passwordField = findFirstVisibleElement(
                By.cssSelector("input[name='j_password']"),
                By.cssSelector("input[name='password']"),
                By.cssSelector("input[id*='password']"),
                By.cssSelector("input[id*='passwort']"),
                By.cssSelector("input[type='password']")
        );

        usernameField.clear();
        usernameField.sendKeys(ADMIN_USER);

        passwordField.clear();
        passwordField.sendKeys(ADMIN_PASSWORD);

        WebElement submitButton = findFirstVisibleElement(
                By.cssSelector("input[type='submit']"),
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(., 'Login')]"),
                By.xpath("//button[contains(., 'Einloggen')]"),
                By.xpath("//input[contains(@value, 'Login')]"),
                By.xpath("//input[contains(@value, 'Einloggen')]")
        );

        click(submitButton);

        try {
            wait.until(driver -> !driver.getCurrentUrl().contains("login.xhtml"));
        } catch (TimeoutException ex) {
            fail("Login als Admin hat nicht funktioniert. Aktuelle URL: " + driver.getCurrentUrl());
        }
    }

    private void waitForProductPage() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".page-title")));
            assertEquals("Produkte", driver.findElement(By.cssSelector(".page-title")).getText().trim());
        } catch (TimeoutException ex) {
            fail("Produktseite wurde nicht geöffnet. Aktuelle URL: " + driver.getCurrentUrl());
        }
    }

    private WebElement findFirstVisibleElement(By... locators) {
        for (By locator : locators) {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement element : elements) {
                if (element.isDisplayed()) {
                    return element;
                }
            }
        }

        fail("Element wurde nicht gefunden. Aktuelle URL: " + driver.getCurrentUrl());
        return null;
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

    private record PageState(int currentPage, int totalPages) {
    }
}
