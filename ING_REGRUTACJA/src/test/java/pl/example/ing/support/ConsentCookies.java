package pl.example.ing.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class ConsentCookies {

    private static final List<String> CONSENT = List.of("cookiePolicyGDPR", "cookiePolicyGDPR__details");
    private static final Set<String> ANALYTICS = Set.of("_ga", "_gid", "last_visited_segment", "s_cc");
    private static final List<String> MARKETING = List.of("cookieSEG", "cookieSEG__details", "GAD");

    private final Page page;

    public ConsentCookies(Page page) {
        this.page = page;
    }

    public void assertNoOptionalCookies() {
        List<String> names = cookieNames();
        assertThat(names).doesNotContainAnyElementsOf(CONSENT);
        assertThat(names).noneMatch(ConsentCookies::isAnalytics);
        assertThat(names).doesNotContainAnyElementsOf(MARKETING);
    }

    public void assertConsentSaved() {
        waitForCookies(names -> names.containsAll(CONSENT));
        assertThat(page.context().cookies(page.url()))
                .filteredOn(cookie -> CONSENT.contains(cookie.name))
                .allSatisfy(cookie -> assertThat(cookie.value)
                        .as("Value of %s", cookie.name).isNotBlank());
    }

    public void assertAnalyticsSavedWithoutMarketing() {
        waitForCookies(names -> names.containsAll(CONSENT)
                && names.stream().anyMatch(ConsentCookies::isAnalytics));
        assertThat(cookieNames()).doesNotContainAnyElementsOf(MARKETING);
    }

    private void waitForCookies(Predicate<List<String>> expected) {
        try {
            page.waitForCondition(() -> expected.test(cookieNames()),
                    new Page.WaitForConditionOptions().setTimeout(20_000));
        } catch (TimeoutError timeout) {
            throw new AssertionError("Expected cookies were not saved. Found: " + cookieNames(), timeout);
        }
    }

    private List<String> cookieNames() {
        return page.context().cookies(page.url()).stream().map(cookie -> cookie.name).toList();
    }

    private static boolean isAnalytics(String name) {
        return ANALYTICS.contains(name) || name.startsWith("_ga_");
    }
}
