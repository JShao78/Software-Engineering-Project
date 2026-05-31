package de.pdbm.team18.selenium.staff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.openqa.selenium.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

class EditStaffIT {
    private WebDriver driver;
	private WebDriver[] drivers;
	private WebDriverWait wait;
    private static final String BASE_URL = System.getProperty("app.baseUrl", "http://localhost:8080/team-18/");

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
			wait = new WebDriverWait(driver, Duration.ofSeconds(5));
			test.accept(browser);
		}
	}

	private void login(String username, String password) {
		driver.get(BASE_URL);

		driver.findElement(By.cssSelector("input[type='text']")).sendKeys(username);
		driver.findElement(By.cssSelector("input[type='password']")).sendKeys(password);
		driver.findElement(By.cssSelector("input[type='submit']")).click();

		driver.get(BASE_URL + "staffs.xhtml");
		assertEquals("http://localhost:8080/team-18/staffs.xhtml", driver.getCurrentUrl());
	}

    private WebElement getTableRow(int staffId) {
		return driver.findElement(By.xpath("//table[@class='data-table']//tr[td[span[text()='" + staffId + "']]]"));
    }

	private WebElement openForm() {
		WebElement tr = getTableRow(2);
		WebElement editButton = tr.findElement(By.cssSelector(".btn-table-edit"));
		wait.until(ExpectedConditions.elementToBeClickable(editButton));
		editButton.click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#editStaffForm")));
		assertTrue(driver.findElement(By.cssSelector("#editStaffForm")).isDisplayed());

		return driver.findElement(By.cssSelector("#editStaffForm"));
	}

	private void fillForm(
				WebElement form,
				String first_name,
				String last_name,
				String mail,
				String phonenumber,
				String is_active,
				String storename
			) {
		String prefix = "editStaffForm:";
		WebElement firstName = form.findElement(By.id(prefix + "editVorname"));
		WebElement lastName = form.findElement(By.id(prefix + "editNachname"));
		WebElement email = form.findElement(By.id(prefix + "editEmail"));
		WebElement phone = form.findElement(By.id(prefix + "editPhone"));
		Select active = new Select(form.findElement(By.id(prefix + "editActive")));
		Select store = new Select(form.findElement(By.id(prefix + "editStore")));

		firstName.clear();
		lastName.clear();
		email.clear();
		phone.clear();

		firstName.sendKeys(first_name);
		lastName.sendKeys(last_name);
		email.sendKeys(mail);
		phone.sendKeys(phonenumber);

        String isActive = "Aktiv".equals(is_active) ? "1" : "0";
		String storeName = "";
		switch(storename) {
			case "Santa Cruz Bikes":
				storeName = "1";
				break;
			case "Baldwin Bikes":
				storeName = "2";
				break;
			case "Rowlett Bikes":
				storeName = "3";
				break;
			default:
				break;
		};

		active.selectByValue(isActive);
		store.selectByValue(storeName);

		WebElement saveButton = form.findElement(By.className("create-save-btn"));
		wait.until(ExpectedConditions.elementToBeClickable(saveButton));
		saveButton.click();

		checkValues(
			first_name,
			last_name,
			mail,
			phonenumber,
			is_active,
			storename
		);
	}

	private void checkValues(String firstName, String lastName, String email, String phone, String active, String store) {
		wait.until(ExpectedConditions.textToBePresentInElementLocated(
			By.xpath("//table[@class='data-table']//tr[td[span[text()='2']]]//td[contains(@class,'firstName')]"),
			firstName
		));
		WebElement tr = getTableRow(2);
		assertEquals(firstName, tr.findElement(By.cssSelector(".firstName")).getText());
		assertEquals(lastName, tr.findElement(By.cssSelector(".lastName")).getText());
		assertEquals(email, tr.findElement(By.cssSelector(".email")).getText());
		assertEquals(phone, tr.findElement(By.cssSelector(".phone")).getText());
		assertEquals(active, tr.findElement(By.cssSelector(".status")).getText());
		assertEquals(store, tr.findElement(By.cssSelector(".storeName")).getText());
	}

	private void isOriginal() {
		checkValues(
			"Mireya",
			"Copeland",
			"mireya.copeland@bikes.shop",
			"(831) 555-5555",
			"Aktiv",
			"Santa Cruz Bikes"
		);
	}

	@Test
    void editTest() {
		loopBrowsers(browser -> {
			login("fabiola.jackson@bikes.shop", "555-5554");
			isOriginal();
			fillForm(
				openForm(),
				"Test",
				"Case",
				"Test@Mail.com",
				"(831) 444-4444",
				"Inaktiv",
				"Rowlett Bikes"
			);
			fillForm(
				openForm(),
				"Mireya",
				"Copeland",
				"mireya.copeland@bikes.shop",
				"(831) 555-5555",
				"Aktiv",
				"Santa Cruz Bikes"
			);
		});
	}

	@Test
    void cancelEditTest() {
		loopBrowsers(browser -> {
			login("fabiola.jackson@bikes.shop", "555-5554");
			WebElement staffForm = openForm();

			WebElement cancelButton = staffForm.findElement(By.className("create-reset-btn"));
			wait.until(ExpectedConditions.elementToBeClickable(cancelButton));
			cancelButton.click();

			wait.until(driver -> {
				try {
					return driver.findElements(By.cssSelector("#editStaffModal")).stream()
						.noneMatch(WebElement::isDisplayed);
				} catch (StaleElementReferenceException ignored) {
					return false;
				}
			});

			isOriginal();
		});
	}
}
