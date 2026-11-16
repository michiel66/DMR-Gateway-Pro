# DMR Gateway Pro

**Eigen Android TeamSpeak-client met blauw-oranje radiopaneel en grote Push-to-Talk-knop.**

Dit project is een gewijzigde versie van [PocketTS 1.7.0](https://github.com/KorppuJauho/pocketts/releases/tag/v1.7.0) van KorppuJauho, onder de **GNU GPL v3**. De naam, app-ID, iconen, kleuren en verbonden-interface zijn aangepast op 16 november 2026. Het oorspronkelijke audio- en TeamSpeak-protocol blijven ongewijzigd.

> DMR Gateway Pro is de **appnaam**; de huidige app maakt via TeamSpeak verbinding. Dit voegt niet automatisch een DMR-naar-TeamSpeak-radio-gateway toe. De app is niet verbonden aan de makers van TeamSpeak.

## Wat zit er in versie 1.0.0?

- **Nieuw uiterlijk:** donkerblauw, elektrische blauwe omlijning, oranje Kanalen-tab en contrastvolle gebruikerslijst.
- **Zeer grote PTT-knop**, onderaan in het midden, met echte TeamSpeak-zendfunctie: indrukken = zenden, loslaten = stoppen. Oranje wanneer je zendt.
- **PTT standaard ingeschakeld** bij een nieuwe installatie. De gebruiker kan spraakactivatie altijd aanpassen via Instellingen.
- Alle serverkanalen, gebruikers, chat, favorieten, gebruikersopties en server-instellingen worden uit de originele PocketTS-broncode geladen.
- Microfoon, geluid, audioverwerking, verbindingsdienst en protocolmodule blijven uit de geteste upstreambron.
- **Eigen Android-app-ID:** \`nl.dmr.gateway.pro\`. Daardoor kan de app naast de originele PocketTS geïnstalleerd worden.
- Nieuw launchericoon, appnaam **DMR Gateway Pro**, en Nederlandse dashboardknoppen.
- Reagerende lay-out voor kleine telefoons en grote schermen.

## APK downloaden

Ga naar de GitHub-repository, open **Actions → Android APK → meest recente geslaagde run → Artifacts** en download **DMR-Gateway-Pro-debug-apk**. Pak het ZIP-bestand uit; daarin staat **DMR-Gateway-Pro-1.0.0-debug.apk**.

Bij een eerste installatie kan Android vragen om toestemming voor installeren vanuit de browser of bestandsbeheerder. Gebruik minimaal Android 10.

**Let op:** dit is aanvankelijk een debug-build voor tests. De GitHub-run tekent hem met een tijdelijke debug-sleutel. Voor vaste updates zonder herinstallatie moet er een eigen, veilig opgeslagen release-sleutel worden geconfigureerd.

## Hoe is de code opgebouwd?

De repository bevat de aanpassingen. De GitHub Actions-workflow haalt een schone, vaste versie van PocketTS op, voert de merk- en UI-wijzigingen uit en bouwt met de Android-omgeving van PocketTS.

- \`patches/GatewayDashboard.kt\`: echte, interactieve Android Jetpack Compose-interface.
- \`scripts/apply_branding.py\`: gecontroleerde patches op de officiële v1.7.0-code; stopt als de verwachte broncode niet overeenkomt.
- \`.github/workflows/android.yml\`: reproduceerbare APK-build + volledig gewijzigde broncode als downloadbaar bronpakket.

De workflow compileert **de originele PocketTS-audio-engine**, niet een nagemaakte of geïmiteerde audioverbinding. De server wordt door de gebruiker gekozen; er is geen TeamSpeak-server vooraf ingebouwd.

### Broncode, auteursrechten en licentie

[Originele PocketTS-repository](https://github.com/KorppuJauho/pocketts) — auteursrechten bij de oorspronkelijke rechthebbenden. De oorspronkelijke LICENSE, auteursvermeldingen, CREDITS en overige licentiegegevens blijven aanwezig in het volledige, gewijzigde bronpakket dat de build produceert.

Deze wijzigingen en de resulterende app vallen onder **GPL-3.0**, zoals PocketTS. Gebruikers krijgen toegang tot de volledige corresponderende broncode via het bronpakket van iedere build, de vastgepinde originele broncode en de patches in deze repository. Bij verspreiding moet die toegang behouden blijven.

## Technische controle

Deze eerste versie is opgezet om te bouwen en daarna op een echte Samsung-telefoon te testen. Controleer in het bijzonder: verbinding met TS3, RX, TX, PTT-loslaten, scherm draaien, kanaalwissel, slaapstand, en audiotoestemming.

Dit is een onafhankelijk nieuw project en vervangt **niet** TG 103 VOORBURG of TS Heerlen.
