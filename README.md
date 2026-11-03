# ING — test zgody na cookies analityczne

Projekt Java + Playwright znajduje się w katalogu `ING_REGRUTACJA`.
Ten sam scenariusz wykonuje się równolegle w Chromium, Firefox i WebKit.

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

[Pełna instrukcja lokalna, scenariusz, opcje i Docker](ING_REGRUTACJA/README.md).

## Pipeline

Workflow [`.github/workflows/e2e.yml`](.github/workflows/e2e.yml) działa w GitHub
Actions na lokalnym runnerze Windows z etykietą `ing-local`. Jeden job uruchamia
przez JUnit trzy warianty testu równolegle.

ING wyświetlało runnerom chmurowym GitHuba ekran hCaptcha. Dlatego wykonanie
scenariusza w pipeline przeniesiono na komputer mający dostęp do zwykłej strony ING.

Na skonfigurowanym komputerze:

1. Uruchom runnera w osobnym oknie PowerShell:

   ```powershell
   & "$env:LOCALAPPDATA\GitHubActionsRunners\ING_REGRUTACJA\Start-Runner.cmd"
   ```

2. Poczekaj na komunikat `Listening for Jobs`.
3. Otwórz [Actions → ING_REGRUTACJA](https://github.com/AlanTlu/ing/actions/workflows/e2e.yml),
   wybierz `Run workflow`, gałąź `main` i zatwierdź uruchomienie.
4. Sprawdź job `Chromium, Firefox and WebKit` oraz artefakt `ing-cookie-all-browsers`.

Workflow uruchamia się wyłącznie ręcznie z `main`. Push ani pull request go nie
uruchamia. Skrypt runnera kończy pracę po jednym jobie; przed kolejnym wykonaniem
trzeba uruchomić go ponownie. Nie jest zainstalowany jako usługa Windows.

[Konfiguracja runnera, przebieg joba i rozwiązywanie problemów](docs/CI.md).

## Zweryfikowany wynik

[Zakończony sukcesem pipeline](https://github.com/AlanTlu/ing/actions/runs/41549859788)
wykonał na runnerze `ing-local` **3 testy: 0 błędów, 0 niepowodzeń, 0 pominiętych**.
Raport JUnit oraz screenshoty i trace dla każdej przeglądarki są dostępne
w artefakcie tego uruchomienia. Artefakty przechowywane są przez 14 dni.

## Struktura

- `.github/workflows/e2e.yml` — ręcznie uruchamiany pipeline.
- `docs/CI.md` — instrukcja obsługi runnera i wyników.
- `ING_REGRUTACJA/` — projekt Maven, testy, Maven Wrapper i konfiguracja Docker.

Instalacja runnera, plik certyfikatów oraz jego poświadczenia pozostają poza
repozytorium.
