# Pipeline i lokalny runner

## Sposób wykonania

GitHub Actions zleca job, a runner `ing-local` wykonuje go na skonfigurowanym
komputerze Windows. Chromium, Firefox i WebKit uruchamiane są równolegle przez
JUnit w jednym jobie. Ustawienia znajdują się w
[junit-platform.properties](../ING_REGRUTACJA/src/test/resources/junit-platform.properties).

Runner musi mieć etykiety `self-hosted`, `Windows`, `X64` i `ing-local` oraz
dostęp do zwykłej strony ING. W sprawdzonym uruchomieniu runnery chmurowe
otrzymywały ekran hCaptcha i nie docierały do panelu cookies.

## Uruchomienie na skonfigurowanym komputerze

1. Otwórz osobne okno PowerShell i uruchom:

   ```powershell
   & "$env:LOCALAPPDATA\GitHubActionsRunners\ING_REGRUTACJA\Start-Runner.cmd"
   ```

2. Poczekaj na `Listening for Jobs` i pozostaw okno otwarte.
3. W repozytorium wybierz `Actions > ING_REGRUTACJA > Run workflow`.
4. Wskaż gałąź `main` i zatwierdź uruchomienie.
5. Otwórz job `Chromium, Firefox and WebKit`, aby obserwować jego kroki.

Runner nie działa jako usługa i nie uruchamia się przy starcie Windows.
`Start-Runner.cmd` korzysta z `run.cmd --once`: po jednym jobie proces się
zamyka, a zarejestrowany runner przechodzi w stan `Offline`. To oczekiwany stan.
Przed kolejnym wykonaniem powtórz powyższe kroki. Komputer musi pozostać
włączony i połączony z Internetem do zakończenia joba.

Workflow ma tylko zdarzenie `workflow_dispatch` i warunek gałęzi `main`.
Nie uruchamia się dla pushy ani pull requestów. Jednocześnie wykonywany jest
jeden pipeline z grupy `ing-local`; trzy przeglądarki pracują równolegle
wewnątrz tego wykonania.

## Kolejność kroków

1. `Checkout` pobiera kod repozytorium bez pozostawiania tokena dostępu w konfiguracji Gita.
2. `Set up Java 21` przygotowuje Temurin JDK 21 i cache zależności Maven.
3. `Install browsers` instaluje wersje Chromium, Firefox i WebKit zgodne z Playwright.
4. `Run three browsers in parallel` wykonuje:

   ```powershell
   .\mvnw.cmd --batch-mode --no-transfer-progress '-Dbrowser=all' clean test
   ```

5. `Upload reports, screenshots and traces` publikuje artefakt także po błędzie testu.

Polecenia wykonują się w katalogu `ING_REGRUTACJA`. Job ma limit 15 minut.
Krok testowy korzysta z wcześniej zainstalowanych przeglądarek
(`PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1`).

## Raporty i artefakty

W szczegółach ukończonego uruchomienia otwórz sekcję `Artifacts` i pobierz
`ing-cookie-all-browsers`. Archiwum zawiera:

- `surefire-reports/` — raport JUnit/Surefire, w tym liczbę testów, błędów i pominięć.
- `artifacts/chromium/`, `artifacts/firefox/`, `artifacts/webkit/` — wyniki dla każdego silnika.

W katalogu przeglądarki znajdują się `cookies.txt`, `cookie-settings.png`
i `trace.zip`. Lista cookies zawiera nazwy i właściwości, bez wartości.
Trace zawiera przebieg strony i dane sieciowe z sesji testowej.
Artefakty są przechowywane przez 14 dni.

Po rozpakowaniu artefaktu trace można otworzyć z katalogu projektu, podając
rzeczywistą ścieżkę do pliku:

```powershell
.\mvnw.cmd exec:java '-Dexec.mainClass=com.microsoft.playwright.CLI' '-Dexec.args=show-trace C:/sciezka/do/trace.zip'
```

[Zweryfikowane uruchomienie](https://github.com/AlanTlu/ing/actions/runs/41549859788):
3 testy, 0 błędów, 0 niepowodzeń i 0 pominięć. Job wykonał się na runnerze `ing-local`.

## Konfiguracja na innym komputerze

`Start-Runner.cmd` jest lokalnym skryptem utworzonym na skonfigurowanym
komputerze. Samo sklonowanie repozytorium nie instaluje ani nie rejestruje runnera.

Aby użyć innego komputera Windows x64:

1. Dodaj runnera do własnego repozytorium zgodnie z
   [instrukcją GitHub](https://docs.github.com/en/actions/how-tos/manage-runners/self-hosted-runners/add-runners).
2. Ustaw dodatkową etykietę `ing-local`; pozostaw domyślne etykiety systemu.
3. Zapewnij dostęp do ING oraz narzędzia wymagane przez runnera, w tym Git i Windows PowerShell.
4. Uruchom go jako proces użytkownika. Pojedynczy job można obsłużyć przez `run.cmd --once`.
5. Uruchom workflow ręcznie z gałęzi `main`.

Runner wykonuje kod lokalnie z uprawnieniami użytkownika. Zachowaj ręczne
uruchamianie i warunek gałęzi zapisane w
[workflowie](../.github/workflows/e2e.yml). Instalacji runnera i plików jego
poświadczeń nie dodawaj do repozytorium ani paczki ze źródłami.

## Rozwiązywanie problemów

- **Job pozostaje w kolejce:** sprawdź stan runnera w ustawieniach
  `Actions > Runners`, jego etykiety oraz komunikat `Listening for Jobs`.
  Runner zamknięty po poprzednim jobie wymaga ponownego uruchomienia.
- **Job jest pominięty:** sprawdź, czy wybrano gałąź `main`.
- **Timeout przy przycisku „Dostosuj”:** obejrzyj screenshot i trace.
  Ekran hCaptcha oznacza, że środowisko nie dotarło do strony z panelem cookies.
  W takim przypadku potrzebne jest środowisko dopuszczone do testowania strony.
- **Błąd TLS:** nie wyłączaj weryfikacji certyfikatów.
  Workflow ustawia `MAVEN_OPTS=-Djavax.net.ssl.trustStoreType=Windows-ROOT`.
  Na skonfigurowanym komputerze `Start-Runner.cmd` przekazuje procesom Node
  `NODE_EXTRA_CA_CERTS` wskazujący na lokalny `windows-roots.pem` z publicznymi
  certyfikatami zaufanych urzędów. Plik i skrypt pozostają poza repozytorium;
  inne środowisko może wymagać własnej konfiguracji zaufanych certyfikatów.
- **Brak artefaktu:** sprawdź krok wysyłania wyników. Raporty wymagają
  wcześniejszego uruchomienia Mavena; nie powstaną np. po błędzie checkoutu.
