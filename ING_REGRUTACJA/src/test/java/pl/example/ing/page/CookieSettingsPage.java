package pl.example.ing.page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitUntilState;

public final class CookieSettingsPage {

    private final Page page;
    private final Locator customizeButton;
    private final Locator analyticsSwitch;
    private final Locator marketingSwitch;
    private final Locator acceptButton;
    private final Locator settingsButton;

    public CookieSettingsPage(Page page) {
        this.page = page;
        customizeButton = button("Dostosuj");
        analyticsSwitch = page.getByRole(AriaRole.SWITCH,
                new Page.GetByRoleOptions().setName("Cookies analityczne").setExact(true));
        marketingSwitch = page.getByRole(AriaRole.SWITCH,
                new Page.GetByRoleOptions().setName("Cookies marketingowe").setExact(true));
        acceptButton = button("Zaakceptuj zaznaczone");
        settingsButton = button("Ustawienia cookie");
    }

    public void open(String url) {
        page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        customizeButton.waitFor();
    }

    public void customize() {
        customizeButton.click();
        analyticsSwitch.waitFor();
    }

    public void selectAnalyticsOnly() {
        analyticsSwitch.setChecked(true);
        marketingSwitch.setChecked(false);
    }

    public void acceptSelected() {
        acceptButton.click();
        assertThat(acceptButton).isHidden();
    }

    public void reload() {
        page.reload(new Page.ReloadOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }

    public void reopen() {
        settingsButton.click();
        analyticsSwitch.waitFor();
    }

    public Locator analyticsSwitch() {
        return analyticsSwitch;
    }

    public Locator marketingSwitch() {
        return marketingSwitch;
    }

    private Locator button(String name) {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(name).setExact(true));
    }
}
