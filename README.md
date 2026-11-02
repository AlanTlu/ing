# ING — test zgody na cookies analityczne

Projekt Java + Playwright znajduje się w katalogu `ING_REGRUTACJA`.
Test działa równolegle w Chromium, Firefox i WebKit.

## Uruchomienie lokalne

Wymagany jest JDK 21 lub nowszy oraz dostęp do Internetu. Z katalogu głównego
repozytorium wykonaj w PowerShell:

```powershell
cd ING_REGRUTACJA
.\mvnw.cmd exec:java '-Dexec.mainClass=com.microsoft.playwright.CLI' '-Dexec.args=install chromium firefox webkit'
.\mvnw.cmd clean test
```

Na Linuxie i macOS użyj `./mvnw` zamiast `.\mvnw.cmd`, wcześniej nadając skryptowi
prawo wykonania przez `chmod +x mvnw`.

[Pełna instrukcja, opis scenariusza i konfiguracji](ING_REGRUTACJA/README.md).

## Pipeline

Workflow [`.github/workflows/e2e.yml`](.github/workflows/e2e.yml) uruchamia
osobne joby dla Chromium, Firefox i WebKit. Każdy job instaluje swój silnik,
wykonuje test i zapisuje raporty, screenshoty oraz trace jako artefakt.

Pipeline działa po pushu do `main`, dla pull requestów i po ręcznym uruchomieniu
w zakładce Actions. Joby mogą działać równolegle zależnie od dostępnych runnerów.
