package de.pdbm.team18.shared;

import org.htmlunit.ElementNotFoundException;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class HtmlUnitTestUtils {

    public static final String baseUrl = System.getProperty("baseUrl", "http://localhost:8080/team-18/");

    public enum UserGroup {
        ADMIN("fabiola.jackson@bikes.shop", "555-5554"),
        USER1("mireya.copeland@bikes.shop", "555-5555"),
        USER2("genna.serrano@bikes.shop", "555-5556"),;

        private final String username;
        private final String password;

        UserGroup(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUserName() {
            return username;
        }
        public String getPassword() {
            return password;
        }
    }

    public static HtmlPage changePage(HtmlPage page, int pageNumber) throws IOException {
        page.getWebClient().waitForBackgroundJavaScript(10000);
        int currentPage = Integer.parseInt(
                page.<HtmlElement>getFirstByXPath(
                        "//div[@class='pagination-info']").
                        getVisibleText().split(" ")[1]);

        HtmlElement pagination = page.getFirstByXPath("//div[@class='pagination']");

        List<HtmlElement> buttons = pagination.getByXPath(".//input[@type='submit' and contains(@class,'btn-page-nav')]");
        HtmlElement prevButton = buttons.get(0);
        HtmlElement nextButton = buttons.get(1);

        if (currentPage == pageNumber) {
            return page;
        } else if (currentPage == pageNumber-1) {
            page = nextButton.click();
            return page;
        } else if (currentPage == pageNumber+1) {
            page = prevButton.click();
            return page;
        }

        try {
            pagination.<HtmlElement>getFirstByXPath(
                            ".//input[normalize-space(@value)='" + pageNumber + "']"
                    ).click();
        } catch (ElementNotFoundException | NullPointerException e) {
            // Can't jump directly to page
            if (pageNumber < currentPage) {
                page = prevButton.click();
            } else {
                page = nextButton.click();
            }
            page.getWebClient().waitForBackgroundJavaScriptStartingBefore(2000);
            page.getWebClient().waitForBackgroundJavaScript(10000);
            //move one page and try again
            return changePage(page, pageNumber);
        }
        return page;
    }

    public static WebClient login(WebClient webClient, UserGroup userGroup) throws IOException {
        return login(webClient, userGroup.getUserName(), userGroup.getPassword());
    }

    public static WebClient login(WebClient webClient, String username, String password) throws IOException {
        HtmlPage loginPage = webClient.getPage(baseUrl + "login.xhtml");
        findInputByIdSuffix(loginPage, "username").setValueAttribute(username);
        findInputByIdSuffix(loginPage, "password").setValueAttribute(password);
        getButton(loginPage, "login").click();
        webClient.waitForBackgroundJavaScript(10000);
        return webClient;
    }

    public static HtmlElement getButton(HtmlPage page, String value) {
        List<HtmlElement> buttons = page.getByXPath("//input[@type='submit']");
        for (HtmlElement button : buttons) {
            if (button.getAttribute("value").equalsIgnoreCase(value)) {
                return button;
            }
        }
        fail("Could not find \"" + value +"\" button on " + page.getUrl());
        return null;
    }

    public static HtmlInput findInputByIdSuffix(HtmlPage page, String idSuffix) {
        List<HtmlInput> inputs = page.getByXPath("//input");
        for (HtmlInput input : inputs) {
            String id = input.getId();
            String name = input.getNameAttribute();
            if (idSuffix.equals(id) || id.endsWith(":" + idSuffix)
                    || idSuffix.equals(name) || name.endsWith(":" + idSuffix)) {
                return input;
            }
        }
        fail("Could not find input field ending with '" + idSuffix + "' on " + page.getUrl());
        return null;
    }

}
