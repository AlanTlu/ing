# ING_REGRUTACJA

Test zgody na cookies analityczne na [ing.pl](https://www.ing.pl/).
Java 21, Maven Wrapper, Playwright 1.64.0, JUnit 5 i AssertJ.

## Uruchomienie

Wymagany jest JDK 21 lub nowszy oraz dostęp do Internetu. Otwórz `pom.xml`
jako projekt Maven w IntelliJ lub wykonaj poniższe polecenia w katalogu projektu.

Przy pierwszym uruchomieniu pobierz przeglądarki:

```powershell
.\mvnw.cmd exec:java '-Dexec.mainClass=com.microsoft.playwright.CLI' '-Dexec.args=install chromium firefox webkit'
```

Następnie uruchom test równolegle w Chromium, Firefox i WebKit:

```powershell
.\mvnw.cmd clean test
```

Jedna przeglądarka z widocznym oknem:

```powershell
.\mvnw.cmd -Dbrowser=chromium -Dheaded=true test
```

`browser` przyjmuje `chromium`, `firefox`, `webkit` lub `all` (domyślnie).
Adres można zmienić przez `-Ding.base-url=...` lub zmienną `ING_BASE_URL`.

Na Linuxie i macOS zamiast `.\mvnw.cmd` użyj `./mvnw`. W Linuxie instalację
przeglądarek wraz z zależnościami systemowymi wykonasz tak:

```bash
chmod +x mvnw
./mvnw exec:java -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="install --with-deps chromium firefox webkit"
./mvnw clean test
```

## Scenariusz

1. Otwórz ING w nowym kontekście przeglądarki i sprawdź brak cookies opcjonalnych.
2. Wybierz „Dostosuj” i sprawdź początkowo wyłączone zgody.
3. Włącz tylko cookies analityczne.
4. Kliknij „Zaakceptuj zaznaczone” i sprawdź zapis niepustych cookies zgody.
5. Przeładuj stronę i sprawdź cookies analityczne oraz brak wskazanych marketingowych.
6. Ponownie otwórz panel i sprawdź, czy zachował wybrany stan zgód.

Sprawdzane cookies zgody to `cookiePolicyGDPR` i `cookiePolicyGDPR__details`.
Test oczekuje przynajmniej jednego cookie analitycznego: `_ga`, `_ga_*`, `_gid`,
`last_visited_segment` lub `s_cc`. Sprawdza też brak `cookieSEG`,
`cookieSEG__details` i `GAD`. Nazwy pochodzą z
[polityki cookies ING](https://www.ing.pl/indywidualni/tabele-i-regulaminy/regulacje/polityka-plikow-cookie).

Po akceptacji strona jest przeładowywana, ponieważ podczas lokalnej weryfikacji
dopiero kolejne wczytanie uruchamiało tagi analityczne. Test korzysta z publicznej
strony: zmiany banera, polityki cookies i dostępności serwisu mogą wpływać na wynik.

## Kod

Źródła znajdują się w `src/test/java/pl/example/ing`:

- `IngCookieConsentTest` — scenariusz i asercje interfejsu.
- `page/CookieSettingsPage` — Page Object z selektorami i obsługą panelu.
- `support/ConsentCookies` — odczyt i sprawdzanie cookies.
- `support/BrowserSession` — uruchamianie przeglądarki, artefakty i zamykanie zasobów.

Każdy wariant testu ma własny `Playwright` i świeży `BrowserContext`.
Wszystkie operacje danej sesji wykonuje ten sam wątek. Język, strefa czasowa
i rozmiar okna są jednakowe. Test oczekuje na stan elementów i cookies
zamiast stosować stałe opóźnienia. Równoległość konfiguruje
`src/test/resources/junit-platform.properties`.

## Wyniki

Raport JUnit trafia do `target/surefire-reports`. W `target/artifacts/<browser>`
zapisywane są także po błędzie:

- `cookies.txt` — nazwy i właściwości cookies bez wartości.
- `cookie-settings.png` — ostatni widok strony.
- `trace.zip` — przebieg testu do analizy.

Trace można otworzyć lokalnie:

```powershell
.\mvnw.cmd exec:java '-Dexec.mainClass=com.microsoft.playwright.CLI' '-Dexec.args=show-trace target/artifacts/chromium/trace.zip'
```

## Docker i CI

```powershell
docker compose run --build --rm ing-tests
```

Obraz Docker zawiera przeglądarki zgodne z wersją Playwright w `pom.xml`.
Raporty pozostają w lokalnym katalogu `target`.

Przykładowy pipeline [GitHub Actions](.github/workflows/e2e.yml) ma macierz
`chromium`, `firefox`, `webkit`. Każdy job instaluje swój silnik, uruchamia test
i publikuje artefakty. Joby mogą działać równolegle zależnie od dostępnych runnerów.
Plik jest przykładem realizacji CI; lokalne uruchomienie nie wymaga Gita ani GitHuba.

## Pakowanie

Projekt można przekazać jako ZIP. Przy pakowaniu pomiń wygenerowane katalogi
`target`, `.idea` i pliki `*.iml`. Raporty i trace mogą zawierać lokalne ścieżki
oraz dane sesji przeglądarki. Zachowaj źródła, Maven Wrapper, `pom.xml`, README
i konfigurację Docker oraz CI.

## Certyfikaty

Jeśli instalator przeglądarek w Windows zgłasza problem z certyfikatem firmowego
proxy, a certyfikat jest zainstalowany w systemie, użyj:

```powershell
$env:NODE_OPTIONS = "--use-system-ca"
```
