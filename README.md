# DG Gateway Pro — Android

**Digitale Gateway Regio Netwerk Nederland**: een zelfstandige Android-portofoonapp op basis van [PocketTS 1.7.0](https://github.com/KorppuJauho/pocketts/releases/tag/v1.7.0), met een eigen eenvoudige provincie-login en vaste TeamSpeak-verbinding.

> Dit is een **onafhankelijk TeamSpeak-project**. DG-groepen zijn eigen regiogroepen, geen officiële DMR-talkgroups en geen koppeling met het BrandMeister-netwerk. DG staat voor *Digitale Gateway*.

## Versie 2.0.2 — Android

- **Appnaam:** DG Gateway Pro; eigen Android-ID: `nl.dg.gateway.pro`. De eerdere DMR Gateway Pro-testapp heeft een ander app-ID en kan naast de nieuwe app blijven staan.
- **Eenvoudig verbinden:** voer je eigen naam in en kies je provincie. Bij verbinding krijg je bijvoorbeeld de TeamSpeak-naam `DG-103 Michiel`.
- **Vaste server:** de app maakt altijd verbinding met `heerlen.mijnts3.nl`. Gebruikers hoeven geen IP-adres, poort of eigen server in te vullen. Andere serverfavorieten en inkomende TeamSpeak-serverlinks worden in de gewone appflow niet aangeboden.
- **Naam en provincie worden onthouden:** op dit Android-apparaat via de lokale appinstellingen. Na loskoppelen kun je naam/provincie aanpassen.
- **Portofoondashboard:** oorspronkelijke PocketTS TeamSpeak-protocolengine, audio en echte PTT blijven intact; grote groene PTT wordt rood tijdens zenden; zilverkleurige omlijsting; chat en online gebruikerspopup.
- **Herstel RX-geluid:** de Audioknop opent een popup met **Origineel PocketTS geluid**, **Alternatief: telefoonluidspreker** en **Testgeluid afspelen**. De aanbevolen knop zet ook eerder opgeslagen audio-instellingen terug naar de originele PocketTS-route zonder de PTT-instelling of het microfoonniveau te veranderen.
- **Nieuwe installaties:** exact dezelfde standaardwaarden voor de ontvangen spraak als PocketTS 1.7.0: echo-onderdrukking aan, headsetgebruik aan, earpiece uit. De originele `Session.kt`, `VoicePlayer.kt`, `AudioRoute.kt` en `TestSound.kt` worden met een GitHub Actions-regressiecheck op wijzigingen gecontroleerd.
- **Geen automatische wissel van TS-spraakkanaal op basis van DG:** de DG-prefix is een **regiolabel**. Gebruikers kunnen een bestaand TeamSpeak-kanaal kiezen zoals op de originele server. Het dashboard kan later op DG-prefix groeperen.

### Nederlandse DG-regio's

| DG-groep | Provincie |
|---|---|
| DG-100 | Noord-Holland |
| DG-101 | Utrecht |
| DG-102 | Zeeland |
| DG-103 | Zuid-Holland |
| DG-104 | Noord-Brabant |
| DG-105 | Limburg |
| DG-106 | Gelderland |
| DG-107 | Overijssel |
| DG-108 | Drenthe |
| DG-109 | Groningen |
| DG-110 | Friesland |
| DG-111 | Flevoland |
| DG-999 | Overige, voor het latere dashboard bij gebruikers zonder herkenbare DG-prefix |

Een gewone TeamSpeak-gebruiker die zichzelf `DG-103 Marcel` noemt, kan later door het Windows-dashboard in DG-103 worden ingedeeld. De Android-app verandert geen officiële TeamSpeak-kanalen of rechten.

## APK downloaden en installeren

Open [de Actions-pagina](https://github.com/michiel66/DMR-Gateway-Pro/actions) van deze Android-repository. Kies de nieuwste geslaagde **Android APK** build; onder **Artifacts** staat `DG-Gateway-Pro-debug-apk`. Download het ZIP-bestand en pak `DG-Gateway-Pro-2.0.2-debug.apk` uit. Voor installatie is minimaal Android 10 nodig.

De APK is een **debug-build**. Iedere GitHub Actions-run ondertekent met een tijdelijke testsleutel. Voor toekomstige upgrades zonder herinstallatie is een vaste release-signingsleutel nodig. Het Android-app-ID is gewijzigd van `nl.dmr.gateway.pro` naar `nl.dg.gateway.pro`; daarom vervangt deze app de vorige DMR-testversie niet automatisch.

## Gebruik

1. Open **DG Gateway Pro** op de telefoon.
2. Vul je **naam** in, bijvoorbeeld Michiel.
3. Kies de **provincie** Zuid-Holland. De app toont **DG-103 Michiel**.
4. Tik op **VERBINDEN** en sta zo nodig de microfoontoestemming toe.
5. De app verbindt met de vooraf ingestelde TeamSpeak-server. Na verbinding krijg je het bestaande radiopaneel.
6. Houd de groene **PTT** ingedrukt om te praten (rood tijdens zenden); laat los om te stoppen. De microfoon en PTT werken alleen als Android, de server en de kanaalrechten dat toelaten.
7. Hoor je niets terug? Tik op **Audio** → **Origineel PocketTS geluid**. Kies daarna **Testgeluid afspelen**. Werkt de originele route niet, probeer **Alternatief: telefoonluidspreker**. Controleer Android-mediavolume, Bluetooth en of je in hetzelfde TeamSpeak-kanaal zit als de spreker.
8. Tik op **Gebruikers** voor een popup met alle online TeamSpeak-namen; tik op een kanaal om er naartoe te gaan.
9. Kies **Verbinding verbreken** om opnieuw naar het aanmeldscherm te gaan, waar naam en provincie onthouden blijven.

## Windows-dashboard: apart toekomstig project

De later geplande **DG Gateway Pro Windows Viewer** komt in een **eigen GitHub-repository** en is niet opgenomen in deze Android-build. Het ontwerp omvat een fullscreen Nederlandkaart, DG-regiogroepen, groen oplichtende regio wanneer een gebruiker spreekt, Last Heard, en twee modi:

- **Met Android:** een tijdelijke QR-code of zescijferig koppelnummer dat bij iedere Windows-start vernieuwd wordt.
- **Zonder Android:** de pc maakt zelf verbinding met TeamSpeak als `DashboardView/gebruikersnaam` en kiest een TeamSpeak-kanaal om spreekstatus te volgen, zonder microfoon of luisteraudio.

De Windows-functionaliteit en de PC-koppeling zijn **nog niet geïmplementeerd**. Voor netwerkbrede spreekstatus over alle kanalen kan later een aparte servermonitor nodig zijn.

## Broncode, rechten en licentie

Het project is gebaseerd op PocketTS van KorppuJauho, met behoud van de originele audiocode en TeamSpeak-protocolcomponenten. De gewijzigde code valt onder **GPL-3.0**. De build haalt upstream broncode van de vaste tag `v1.7.0`, voert `scripts/apply_branding.py` uit en levert de volledige aangepaste broncode als `DG-Gateway-Pro-source` artifact.

Bestanden:
- `patches/GatewayDashboard.kt` — interactieve Android-UI en PTT-knop
- `patches/DgRegions.kt` — DG-provincie-indeling en nickname-regels
- `patches/DgConnectScreen.kt` — naam/provincie invoeren en lokaal onthouden
- `scripts/apply_branding.py` — bouwt vaste serverflow en merknaam in een schone PocketTS v1.7.0 checkout
- `.github/workflows/android.yml` — bouwt de APK en volledige corresponderende broncode

Het project is onafhankelijk van TeamSpeak en is niet gekoppeld aan de officiële DMR- of BrandMeister-netwerken.
