package pl.example.ing;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.io.IOException;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pl.example.ing.page.CookieSettingsPage;
import pl.example.ing.support.BrowserSession;
import pl.example.ing.support.ConsentCookies;

class IngCookieConsentTest {

    private static final String BASE_URL = System.getProperty("ing.base-url",
            System.getenv().getOrDefault("ING_BASE_URL", "https://www.ing.pl/"));

    @ParameterizedTest(name = "analytics consent: {0}")
    @MethodSource("browsers")
    void shouldRememberAnalyticsConsentWithoutMarketing(String browserName) throws IOException {
        try (var session = new BrowserSession(browserName)) {
            var settings = new CookieSettingsPage(session.page());
            var cookies = new ConsentCookies(session.page());

            settings.open(BASE_URL);
            cookies.assertNoOptionalCookies();
            settings.customize();
            assertThat(settings.analyticsSwitch()).not().isChecked();
            assertThat(settings.marketingSwitch()).not().isChecked();

            settings.selectAnalyticsOnly();
            assertThat(settings.analyticsSwitch()).isChecked();
            assertThat(settings.marketingSwitch()).not().isChecked();
            settings.acceptSelected();
            cookies.assertConsentSaved();

            // ING uruchamia tagi analityczne po ponownym wczytaniu strony.
            settings.reload();
            cookies.assertAnalyticsSavedWithoutMarketing();
            settings.reopen();
            assertThat(settings.analyticsSwitch()).isChecked();
            assertThat(settings.marketingSwitch()).not().isChecked();
        }
    }

    private static Stream<String> browsers() {
        String browser = System.getProperty("browser", "all").trim().toLowerCase(Locale.ROOT);
        return "all".equals(browser)
                ? Stream.of("chromium", "firefox", "webkit")
                : Stream.of(browser);
    }
}
