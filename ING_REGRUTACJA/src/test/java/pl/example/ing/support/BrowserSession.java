package pl.example.ing.support;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;

public final class BrowserSession implements AutoCloseable {

    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private final Page page;
    private final Path artifacts;

    public BrowserSession(String browserName) throws IOException {
        artifacts = Path.of("target", "artifacts", browserName);
        Files.createDirectories(artifacts);
        playwright = Playwright.create();

        try {
            browser = launchBrowser(browserName);
            context = browser.newContext(new Browser.NewContextOptions()
                    .setLocale("pl-PL")
                    .setTimezoneId("Europe/Warsaw")
                    .setViewportSize(1440, 900));
            context.setDefaultTimeout(30_000);
            context.setDefaultNavigationTimeout(60_000);
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
            page = context.newPage();
        } catch (RuntimeException | Error failure) {
            try {
                playwright.close();
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    public Page page() {
        return page;
    }

    @Override
    public void close() throws IOException {
        try (playwright; browser; context) {
            try {
                saveCookies();
                page.screenshot(new Page.ScreenshotOptions()
                        .setPath(artifacts.resolve("cookie-settings.png")));
            } finally {
                context.tracing().stop(new Tracing.StopOptions()
                        .setPath(artifacts.resolve("trace.zip")));
            }
        }
    }

    private Browser launchBrowser(String browserName) {
        BrowserType engine = switch (browserName) {
            case "chromium" -> playwright.chromium();
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> throw new IllegalArgumentException("Unknown browser: " + browserName
                    + ". Choose chromium, firefox, webkit or all.");
        };
        return engine.launch(new BrowserType.LaunchOptions()
                .setHeadless(!Boolean.getBoolean("headed")));
    }

    private void saveCookies() throws IOException {
        String metadata = context.cookies().stream()
                .sorted(Comparator.comparing(cookie -> cookie.name))
                .map(cookie -> "%s | domain=%s | path=%s | secure=%s | httpOnly=%s"
                        .formatted(cookie.name, cookie.domain, cookie.path, cookie.secure, cookie.httpOnly))
                .collect(Collectors.joining(System.lineSeparator()));
        Files.writeString(artifacts.resolve("cookies.txt"), metadata + System.lineSeparator());
    }
}
