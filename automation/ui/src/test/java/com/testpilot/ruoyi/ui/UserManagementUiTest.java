package com.testpilot.ruoyi.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class UserManagementUiTest {
    private final String uiUrl = environment("RUOYI_UI_URL", "http://localhost:8081");
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private String createdUserName;

    @BeforeEach
    void openBrowser() {
        playwright = Playwright.create();
        boolean headless = Boolean.parseBoolean(environment("HEADLESS", "true"));
        double slowMo = Double.parseDouble(environment("SLOW_MO", "0"));
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setSlowMo(slowMo));
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 900));
        page = context.newPage();
        page.setDefaultTimeout(15_000);
    }

    @AfterEach
    void cleanup() {
        try {
            if (createdUserName != null) {
                new UserCleanupClient().deleteIfPresent(createdUserName);
            }
        } finally {
            if (context != null) {
                context.close();
            }
            if (browser != null) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
        }
    }

    @Test
    @DisplayName("TC-001：Vue 页面完成用户新增、查询和删除（AC-01/19/20/21/22）")
    void shouldCreateFindAndDeleteUserFromVuePage() {
        createdUserName = UiTestData.uniqueUserName();
        String phone = UiTestData.uniquePhone();

        try {
            login();
            openUserManagement();
            createUser(createdUserName, phone);
            searchUser(createdUserName);

            Locator row = userRow(createdUserName);
            assertThat(row).hasCount(1);
            assertThat(row).containsText(createdUserName);

            deleteUser(row);
            assertThat(userRow(createdUserName)).hasCount(0);
            createdUserName = null;
        } catch (RuntimeException | AssertionError error) {
            captureFailureScreenshot();
            throw error;
        }
    }

    private void login() {
        page.navigate(uiUrl + "/login");
        page.getByPlaceholder("账号").fill(requiredEnvironment("RUOYI_ADMIN_USERNAME"));
        page.getByPlaceholder("密码").fill(requiredEnvironment("RUOYI_ADMIN_PASSWORD"));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("登 录").setExact(true)).click();
        page.waitForURL(url -> !url.contains("/login"));
    }

    private void openUserManagement() {
        page.navigate(uiUrl + "/system/user");
        assertThat(addButton()).isVisible();
    }

    private void createUser(String userName, String phone) {
        addButton().click();
        Locator dialog = page.locator(".el-dialog:visible");
        assertThat(dialog).containsText("添加用户");

        dialog.getByPlaceholder("请输入用户昵称").fill("UI自动化用户");
        dialog.getByPlaceholder("请输入手机号码").fill(phone);
        dialog.getByPlaceholder("请输入用户名称").fill(userName);
        dialog.getByPlaceholder("请输入用户密码").fill("Test12345");

        dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("确 定").setExact(true)).click();
        assertThat(page.getByText("新增成功", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(dialog).isHidden();
    }

    private void searchUser(String userName) {
        Locator queryInput = page.getByPlaceholder("请输入用户名称").first();
        queryInput.fill(userName);
        page.waitForResponse(
                response -> response.url().contains("/system/user/list")
                        && response.request().method().equals("GET"),
                () -> page.locator(".el-form button")
                        .filter(new Locator.FilterOptions().setHasText("搜索")).click());
    }

    private Locator addButton() {
        return page.locator("button.el-button")
                .filter(new Locator.FilterOptions().setHasText("新增")).first();
    }

    private Locator userRow(String userName) {
        return page.locator(".el-table__body tr").filter(new Locator.FilterOptions().setHasText(userName));
    }

    private void deleteUser(Locator row) {
        row.locator("button").filter(new Locator.FilterOptions().setHasText("删除")).click();
        Locator confirm = page.locator(".el-message-box:visible");
        confirm.locator("button").filter(new Locator.FilterOptions().setHasText("确定")).click();
        assertThat(page.getByText("删除成功", new Page.GetByTextOptions().setExact(true))).isVisible();
    }

    private void captureFailureScreenshot() {
        try {
            Path directory = Paths.get("target", "screenshots");
            Files.createDirectories(directory);
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(directory.resolve("user-management-failure.png"))
                    .setFullPage(true));
        } catch (Exception ignored) {
            // 截图失败不能覆盖原始测试异常。
        }
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("缺少环境变量：" + name);
        }
        return value;
    }

    private static String environment(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
