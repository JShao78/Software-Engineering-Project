package de.pdbm.team18.shared;

import org.htmlunit.BrowserVersion;
import org.htmlunit.ElementNotFoundException;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.fail;

public class HtmlUnitTestUtils {

    public static final String baseUrl = normalizeBaseUrl(
            System.getProperty("app.baseUrl", System.getProperty("baseUrl", "http://localhost:8080/team-18/"))
    );

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

    public static HtmlElement findRowByContent(HtmlPage page, String tableXpath, String... content) throws IOException {
        String xpath = ".//tr[";
        StringJoiner joiner = new StringJoiner(" and ");
        for (String element : content) {
            joiner.add("td[contains(., \"" + element + "\")]");
        }
        xpath += joiner + "]";

        HtmlElement table = null;
        int pageNumber = 1;
        HtmlElement row = null;
        while (row == null) {
            try {
                table = page.getFirstByXPath(tableXpath);
                row = table.getFirstByXPath(xpath);
            } catch (RuntimeException ignored) {}
            page = changePage(page, ++pageNumber);
        }
        return row;
    }

    public static WebClient getWebClient() {
        WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.setIncorrectnessListener((message, origin) -> {});
        return webClient;
    }

    public static WebClient createWebClient() {
        return createWebClient(false);
    }

    public static WebClient createWebClient(boolean cssEnabled) {
        WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(cssEnabled);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.setIncorrectnessListener((message, origin) -> { });
        return webClient;
    }

    public static String resolveUrl(String relativePath) {
        String path = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return baseUrl + path;
    }

    public static void waitForJs(WebClient webClient) {
        webClient.waitForBackgroundJavaScriptStartingBefore(2000);
        webClient.waitForBackgroundJavaScript(10000);
    }

    public static void waitForJs(HtmlPage page) {
        waitForJs(page.getWebClient());
    }

    public static HtmlPage changePage(HtmlPage page, int pageNumber) throws IOException {
        waitForJs(page);
        if (page.<HtmlElement>getFirstByXPath("//div[@class='pagination-info']") == null)
            return changePage(page.getWebClient().getPage(page.getUrl()), pageNumber); //Reload page
        String[] pageData = page.<HtmlElement>getFirstByXPath(
                        "//div[@class='pagination-info']")
                .getVisibleText().split(" ");
        int currentPage = Integer.parseInt(pageData[1].strip());
        int maxPage = Integer.parseInt(pageData[3].strip());

        if (pageNumber < 1 || pageNumber > maxPage)
            return page;

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
            waitForJs(page);
            //move one page and try again
            return changePage(page, pageNumber);
        }
        return page;
    }

    public static HtmlElement findRow(HtmlPage page, int id) {
        return page.getFirstByXPath("//tr[td[span[text()='" + id + "']]]");
    }

    public static WebClient login(WebClient webClient, UserGroup userGroup) throws IOException {
        return login(webClient, userGroup.getUserName(), userGroup.getPassword());
    }

    public static WebClient login(WebClient webClient, String username, String password) throws IOException {
        HtmlPage loginPage = webClient.getPage(resolveUrl("login.xhtml"));
        findInputByIdSuffix(loginPage, "username").setValueAttribute(username);
        findInputByIdSuffix(loginPage, "password").setValueAttribute(password);
        getButton(loginPage, "login").click();
        waitForJs(webClient);
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

    private static String normalizeBaseUrl(String rawBaseUrl) {
        if (rawBaseUrl.endsWith("/")) {
            return rawBaseUrl;
        }
        return rawBaseUrl + "/";
    }

}
