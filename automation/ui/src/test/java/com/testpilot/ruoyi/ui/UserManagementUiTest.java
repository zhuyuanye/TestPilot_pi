package com.testpilot.ruoyi.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserManagementUiTest {
    private static final DateTimeFormatter SCREENSHOT_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private final String uiUrl = requiredEnvironment("RUOYI_UI_URL").replaceAll("/+$", "");
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private Path evidenceDirectory;
    private boolean traceStarted;
    private String createdUserName;
    private boolean testFailed;

    @BeforeEach
    void openBrowser() {
        playwright = Playwright.create();
        boolean headless = Boolean.parseBoolean(environment("HEADLESS", "true"));
        double slowMo = nonNegativeDoubleEnvironment("SLOW_MO", "0");
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setSlowMo(slowMo));
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 900));
        evidenceDirectory = Paths.get("target", "evidence",
                "TC-001-" + SCREENSHOT_TIME.format(LocalDateTime.now()));
        createDirectories(evidenceDirectory);
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
        traceStarted = true;
        page = context.newPage();
        page.setDefaultTimeout(15_000);
    }

    @AfterEach
    void cleanup() {
        try {
            if (createdUserName != null) {
                new UserCleanupClient().deleteAllExact(createdUserName);
            }
        } catch (RuntimeException | AssertionError cleanupError) {
            captureFailureScreenshot();
            if (!testFailed) {
                throw cleanupError;
            }
            System.err.println("[cleanup] UI 测试数据兜底清理失败：" + cleanupError.getMessage());
        } finally {
            stopTrace();
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
    @DisplayName("TC-001 | AC-01/19/20/21/22 | Vue 页面完成用户新增、查询和删除")
    void shouldCreateFindAndDeleteUserFromVuePage() {
        createdUserName = UiTestData.uniqueUserName();
        String nickName = UiTestData.uniqueNickName();
        String phone = UiTestData.uniquePhone();
        String password = UiTestData.uniquePassword();

        try {
            login();
            openUserManagement();
            createUser(createdUserName, nickName, phone, password);
            searchUser(createdUserName);

            Locator row = userRow(createdUserName);
            assertThat(row).hasCount(1);
            assertThat(row).containsText(createdUserName);
            assertThat(row).containsText(nickName);
            assertThat(row).containsText(phone);
            assertThat(row.getByRole(AriaRole.SWITCH)).isChecked();
            captureEvidenceScreenshot("01-user-created-and-found.png");

            deleteUser(row);
            assertThat(userNameLink(createdUserName)).hasCount(0);
            captureEvidenceScreenshot("02-user-deleted.png");
        } catch (RuntimeException | AssertionError error) {
            testFailed = true;
            captureFailureScreenshot();
            throw error;
        }
    }

    private void login() {
        page.navigate(uiUrl + "/login");
        page.getByPlaceholder("账号").fill(requiredEnvironment("RUOYI_ADMIN_USERNAME"));
        page.getByPlaceholder("密码").fill(requiredEnvironment("RUOYI_ADMIN_PASSWORD"));

        Response response = page.waitForResponse(
                candidate -> candidate.url().endsWith("/login")
                        && candidate.request().method().equals("POST"),
                () -> page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("登 录").setExact(true)).click());
        assertEquals(200, response.status(), "登录请求 HTTP 状态应为 200");
        page.waitForURL(url -> !url.contains("/login"));
    }

    private void openUserManagement() {
        page.navigate(uiUrl + "/system/user");
        assertThat(addButton()).isVisible();
    }

    private void createUser(String userName, String nickName, String phone, String password) {
        addButton().click();
        Locator dialog = addUserDialog();
        assertThat(dialog).isVisible();

        dialog.getByPlaceholder("请输入用户昵称").fill(nickName);
        dialog.getByPlaceholder("请输入手机号码").fill(phone);
        dialog.getByPlaceholder("请输入用户名称").fill(userName);
        dialog.getByPlaceholder("请输入用户密码").fill(password);

        Response response = page.waitForResponse(
                candidate -> candidate.url().contains("/system/user")
                        && candidate.request().method().equals("POST"),
                () -> dialog.getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("确 定").setExact(true)).click());
        assertEquals(200, response.status(), "新增用户请求 HTTP 状态应为 200");
        assertThat(page.getByText("新增成功", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(dialog).isHidden();
    }

    private void searchUser(String userName) {
        Locator queryForm = page.locator("form.el-form--inline");
        queryForm.getByPlaceholder("请输入用户名称").fill(userName);
        Response response = page.waitForResponse(
                candidate -> candidate.url().contains("/system/user/list")
                        && candidate.request().method().equals("GET"),
                () -> queryForm.getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName(Pattern.compile("搜索"))).click());
        assertEquals(200, response.status(), "查询用户请求 HTTP 状态应为 200");
    }

    private Locator addButton() {
        return page.locator(".mb8").getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("新增")));
    }

    private Locator addUserDialog() {
        return page.getByRole(AriaRole.DIALOG)
                .filter(new Locator.FilterOptions().setHasText("添加用户"));
    }

    private Locator userNameLink(String userName) {
        return page.locator(".el-table__body")
                .getByText(userName, new Locator.GetByTextOptions().setExact(true));
    }

    private Locator userRow(String userName) {
        return userNameLink(userName).locator("xpath=ancestor::tr");
    }

    private void deleteUser(Locator row) {
        row.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("删除"))).click();
        Locator confirmDialog = page.getByRole(AriaRole.DIALOG)
                .filter(new Locator.FilterOptions().setHasText("是否确认删除用户编号"));
        assertThat(confirmDialog).isVisible();

        Response response = page.waitForResponse(
                candidate -> candidate.url().contains("/system/user/")
                        && candidate.request().method().equals("DELETE"),
                () -> confirmDialog.getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("确定").setExact(true)).click());
        assertEquals(200, response.status(), "删除用户请求 HTTP 状态应为 200");
        assertThat(page.getByText("删除成功", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(confirmDialog).isHidden();
    }

    private void captureEvidenceScreenshot(String fileName) {
        if (page == null || evidenceDirectory == null) {
            return;
        }
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(evidenceDirectory.resolve(fileName))
                .setFullPage(true));
    }

    private void captureFailureScreenshot() {
        try {
            captureEvidenceScreenshot("failure-" + SCREENSHOT_TIME.format(LocalDateTime.now()) + ".png");
        } catch (Exception ignored) {
            // 截图失败不能覆盖原始测试异常。
        }
    }

    private void stopTrace() {
        if (!traceStarted || context == null || evidenceDirectory == null) {
            return;
        }
        try {
            context.tracing().stop(new Tracing.StopOptions()
                    .setPath(evidenceDirectory.resolve("trace.zip")));
        } catch (Exception traceError) {
            System.err.println("[evidence] Playwright trace 保存失败：" + traceError.getMessage());
        } finally {
            traceStarted = false;
        }
    }

    private static void createDirectories(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (Exception error) {
            throw new IllegalStateException("无法创建 UI 证据目录：" + directory, error);
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

    private static double nonNegativeDoubleEnvironment(String name, String defaultValue) {
        double value = Double.parseDouble(environment(name, defaultValue));
        if (value < 0) {
            throw new IllegalStateException(name + " 不能小于 0");
        }
        return value;
    }
}
