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

Workflow [`.github/workflows/e2e.yml`](.github/workflows/e2e.yml) uruchamia jeden
job na runnerze Windows z etykietą `ing-local`. JUnit wykonuje w nim test
równolegle w Chromium, Firefox i WebKit. Raporty, screenshoty oraz trace
są zapisywane jako artefakt.

Pipeline uruchamia się ręcznie w zakładce Actions, wyłącznie z gałęzi `main`.
Nie wykonuje kodu z pull requestów. Runner musi być uruchomiony przed startem joba.

ING wyświetlało runnerom chmurowym GitHuba ekran hCaptcha zamiast strony z panelem
cookies. Dlatego test wymaga środowiska z dostępem do zwykłej strony ING.

Na skonfigurowanym komputerze Windows runnera można uruchomić poleceniem:

```powershell
& "$env:LOCALAPPDATA\GitHubActionsRunners\ING_REGRUTACJA\Start-Runner.cmd"
```

Runner kończy pracę po jednym jobie. Do kolejnego uruchomienia powtórz polecenie,
a następnie wybierz `Actions > ING_REGRUTACJA > Run workflow`.
Instalacja runnera i jego poświadczenia pozostają poza repozytorium.
