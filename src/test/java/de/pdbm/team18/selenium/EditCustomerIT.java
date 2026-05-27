package de.pdbm.team18.selenium;

import de.pdbm.team18.business.customers.entity.Customer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.Objects;

public class EditCustomerIT {

    private enum UserGroup {
        ADMIN("fabiola.jackson@bikes.shop", "555-5554"),
        USER1("mireya.copeland@bikes.shop", "555-5555"),
        USER2("genna.serrano@bikes.shop", "555-5556"),;

        private final String name;
        private final String password;

        UserGroup(String name, String password) {
            this.name = name;
            this.password = password;
        }

        public String getUserName() {
            return name;
        }
        public String getPassword() {
            return password;
        }
    }

    private static final String url = "http://localhost:8080/team-18/";
    private static final String idPrefix = "loginForm:";
    private static final boolean headless = false;

    private ChromeDriver driver;

    @BeforeEach
    void setup() {
        //init driver
        ChromeOptions options = new ChromeOptions();
        if (headless) options.addArguments("--headless=new");
        driver =  new ChromeDriver(options);
        driver.manage().window().maximize();
        //Navigate to page
        driver.get(url + "kunden.xhtml");
        //Login
        login(UserGroup.ADMIN);
        //Prepare customer
        Customer customer = new Customer();
        customer.setCustomerId(1);
        customer.setFirstName("Debra");
        customer.setLastName("Burks");
        customer.setStreet("9273 Thorne Ave.");
        customer.setState("NY");
        customer.setEmail("debra.burks@yahoo.com");
        customer.setZipCode("14127");
        customer.setPhone("(123) 123-1234");
        editCustomer(readCustomer(customer.getCustomerId()), customer, true);
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    private void login(UserGroup userGroup) {
        WebElement usernameInput = driver.findElement(By.id(idPrefix + "username"));
        WebElement passwordInput = driver.findElement(By.id(idPrefix + "password"));
        WebElement submitButton = driver.findElement(By.className("login-button"));

        usernameInput.sendKeys(userGroup.getUserName());
        passwordInput.sendKeys(userGroup.getPassword());
        submitButton.click();
    }

    private Customer readCustomer(int customerId) {
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        //Workaround for StaleElementReferenceException
        boolean done = false;
        while (!done) {
            try {
                customer.setFirstName(getTableRow(customerId).findElement(By.className("firstName")).getText().strip());
                customer.setLastName(getTableRow(customerId).findElement(By.className("lastName")).getText().strip());
                customer.setEmail(getTableRow(customerId).findElement(By.className("email")).getText().strip());
                customer.setPhone(getTableRow(customerId).findElement(By.className("phone")).getText().strip());
                customer.setState(getTableRow(customerId).findElement(By.className("state")).getText().strip());
                customer.setZipCode(getTableRow(customerId).findElement(By.className("zip")).getText().strip());
                customer.setStreet(getTableRow(customerId).findElement(By.className("street")).getText().strip());
                done = true;
            } catch (StaleElementReferenceException ignored) {}
        }
        return customer;
    }

    private void editCustomer(Customer oldCustomer, Customer newCustomer, boolean saveChanges) {
        WebElement tableRow = getTableRow(oldCustomer.getCustomerId());
        WebElement editButton = tableRow.findElement(By.cssSelector("[value=Bearbeiten]"));
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(editButton));
        editButton.click();

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(By.id("editCustomerForm")));
        WebElement modalForm = driver.findElement(By.id("editCustomerForm"));
        WebElement firstNameInput = modalForm.findElement(By.id("editCustomerForm:editFirstName"));
        WebElement lastNameInput = modalForm.findElement(By.id("editCustomerForm:editLastName"));
        WebElement emailInput = modalForm.findElement(By.id("editCustomerForm:editEmail"));
        WebElement phoneInput = modalForm.findElement(By.id("editCustomerForm:editPhone"));
        WebElement stateInput = modalForm.findElement(By.id("editCustomerForm:editState"));
        WebElement zipInput = modalForm.findElement(By.id("editCustomerForm:editZip"));
        WebElement streetInput = modalForm.findElement(By.id("editCustomerForm:editStreet"));
        firstNameInput.clear();
        firstNameInput.sendKeys(newCustomer.getFirstName());
        lastNameInput.clear();
        lastNameInput.sendKeys(newCustomer.getLastName());
        emailInput.clear();
        emailInput.sendKeys(newCustomer.getEmail());
        phoneInput.clear();
        phoneInput.sendKeys(newCustomer.getPhone());
        stateInput.clear();
        stateInput.sendKeys(newCustomer.getState());
        zipInput.clear();
        zipInput.sendKeys(newCustomer.getZipCode());
        streetInput.clear();
        streetInput.sendKeys(newCustomer.getStreet());

        if (saveChanges) {
            WebElement saveButton = modalForm.findElement(By.cssSelector("[value='Änderungen speichern']"));
            new WebDriverWait(driver, Duration.ofSeconds(1)).until(ExpectedConditions.elementToBeClickable(saveButton));
            saveButton.click();
        } else {
            WebElement cancelButton = modalForm.findElement(By.cssSelector("[value='Abbrechen']"));
            new  WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(cancelButton));
            cancelButton.click();
        }

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until((driver) -> {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    return Objects.equals(js.executeScript("return document.readyState"), "complete");
                });
    }

    private WebElement getTableRow(int customerId) {
        return driver.findElement(
                By.xpath("//table[@class='data-table']//tr[td[span[text()='" + customerId + "']]]")
        );
    }

    private void checkTableValues(Customer customer) {
        waitForAjax();
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.textToBePresentInElementLocated(
                By.xpath("//table[@class='data-table']//tr[td[span[text()='" + customer.getCustomerId() + "']]]//td[contains(@class,'firstName')]"),
                customer.getFirstName()
        ));
        //Workaround for StaleElementReferenceException
        boolean done = false;
        while (!done) {
            try {
                Assertions.assertEquals(customer.getFirstName(),
                        getTableRow(customer.getCustomerId()).findElement(By.className("firstName")).getText().strip());
                Assertions.assertEquals(customer.getLastName(),
                        getTableRow(customer.getCustomerId()).findElement(By.className("lastName")).getText().strip());
                Assertions.assertEquals(customer.getEmail(),
                        getTableRow(customer.getCustomerId()).findElement(By.className("email")).getText().strip());
                Assertions.assertEquals(customer.getPhone(),
                        getTableRow(customer.getCustomerId()).findElement(By.className("phone")).getText().strip());
                Assertions.assertEquals(customer.getState(),
                        getTableRow(customer.getCustomerId()).findElement(By.className("state")).getText().strip());
                Assertions.assertEquals(customer.getZipCode(),
                        getTableRow(customer.getCustomerId()).findElement(By.className("zip")).getText().strip());
                Assertions.assertEquals(customer.getStreet(),
                        getTableRow(customer.getCustomerId()).findElement(By.className("street")).getText().strip());
                done = true;
            } catch (StaleElementReferenceException ignored) {}
        }
    }

    private Customer createTestCustomer(int customerId) {
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setFirstName("Test");
        customer.setLastName("Test");
        customer.setStreet("TestStreet");
        customer.setEmail("Test@Mail.com");
        customer.setPhone("123456789101213");
        customer.setState("AB");
        customer.setZipCode("12345");
        return customer;
    }

    private void waitForAjax() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        ExpectedCondition<Boolean> jQueryLoad = driver -> {
            try {
                return ((Long)((JavascriptExecutor)driver).executeScript("return jQuery.active") == 0);
            }
            catch (Exception e) {
                // no jQuery present
                return true;
            }
        };

        // wait for Javascript to load
        ExpectedCondition<Boolean> jsLoad = driver -> ((JavascriptExecutor)driver).executeScript("return document.readyState")
                .toString().equals("complete");

        wait.until(jQueryLoad);
        wait.until(jsLoad);
    }

    @Test
    public void testEditCustomerSuccess() {
        Customer oldCustomer = readCustomer(1);
        oldCustomer.setCustomerId(1);

        Customer newCustomer = createTestCustomer(oldCustomer.getCustomerId());

        editCustomer(oldCustomer, newCustomer, true);
        checkTableValues(newCustomer);

        //Revert
        editCustomer(newCustomer, oldCustomer, true);
    }

    @Test
    public void testEditCustomerCancel() {
        Customer oldCustomer = readCustomer(1);
        oldCustomer.setCustomerId(1);

        Customer newCustomer = createTestCustomer(oldCustomer.getCustomerId());

        editCustomer(oldCustomer, newCustomer, false);
        checkTableValues(oldCustomer);
    }
}
