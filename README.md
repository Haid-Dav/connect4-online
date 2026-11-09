# Connect4 Online

Spielbarer erster Prototyp für Gruppe 8. Zwei Personen spielen in einem privaten Raum über echte WebSockets. React zeigt den vom Spring-Boot-Server bestätigten Zustand an. Das Backend entscheidet über jeden Zug.

## Schnell ausprobieren

Während die Entwicklungsserver laufen: http://localhost:5174

1. Anzeigenamen eingeben und **Privaten Raum erstellen** wählen.
2. Einladungslink in einem zweiten Browser-Tab öffnen, einen zweiten Namen eingeben und **Beitreten** wählen.
3. Abwechselnd auf eine Spalte klicken. Koralle beginnt. Vier verbundene Steine gewinnen.
4. Nach einer Partie können beide eine Revanche bestätigen. Die Farben und der Startspieler wechseln.

Auf einem zweiten Gerät im selben WLAN die LAN-Adresse des Rechners statt localhost verwenden (die Vite-Konsole zeigt die Network-Adresse). Den Raum auf dieser Adresse erstellen, damit der Link auf das richtige Gerät verweist. Der Zugriff hängt von der lokalen Firewall ab. Es gibt noch keine öffentliche Internet-Bereitstellung.

## Starten

Voraussetzungen: Git, JDK 21 oder 24 (getestet mit 24), Maven 3.6.3+, Node.js 22.12+ und npm oder pnpm. Für diesen Prototyp wird keine Datenbank benötigt.

Repository herunterladen und Frontend-Abhängigkeiten installieren:

```sh
git clone https://github.com/Haid-Dav/connect4-online.git
cd connect4-online/frontend
npm install
cd ..
```

Mit `java -version`, `mvn -version` und `node --version` kannst du die Voraussetzungen prüfen. `JAVA_HOME` muss auf dein JDK zeigen, falls Maven Java nicht findet.

Einmalig im Ordner `frontend`:

```sh
npm install
```

Alternativ `pnpm install`; ein pnpm-Lockfile liegt bei. Dann unter Windows `./Start-Connect4.ps1` aus PowerShell starten. Der Starter findet Maven im PATH oder im vorhandenen Maven-Wrapper-Cache. Die Server laufen bis zum Beenden mit Strg+C.

Manueller Start in zwei Terminals, jeweils ausgehend vom Projektstamm `connect4-online`:

```sh
cd backend
mvn spring-boot:run
```

```sh
cd frontend
npm run dev
```

Frontend: Port 5174. Backend: Port 8081. Vite leitet `/ws` zum Backend weiter. Der Browser verwendet dadurch dieselbe Origin für Webseite und WebSocket. Port 8080 bleibt frei für andere Anwendungen.

## Enthalten

- Private Räume mit sechsstelligen Codes und Einladungslinks
- Zwei Spieler pro Raum, serverseitige Zugvalidierung
- Schwerkraft, volle Spalten, alle vier Gewinnrichtungen, Unentschieden
- Hervorgehobene Gewinnlinie und gemeinsam bestätigte Revanche
- Rückmeldungen bei falschem Code, vollem Raum und Verbindungsabbruch
- Responsive deutsche Oberfläche und per Tastatur bedienbare Spalten

## Bewusste Grenzen des Prototyps

Noch keine Benutzerkonten, PostgreSQL-Anbindung, gespeicherten Partien, ELO oder Ranglisten. Spielräume liegen nur im Arbeitsspeicher eines einzelnen Servers. Beim Neuladen, Verlassen oder Verbindungsabbruch eines Spielers wird sein Raum für beide geschlossen. Der Browser baut die Serververbindung automatisch neu auf; die alte Partie wird nicht wiederhergestellt. Ein Serverneustart löscht alle Räume. Namen sind frei wählbar und keine authentifizierten Konten. Ein Code ist eine Einladung, kein Login.

Die Schrift lädt optional von Google Fonts; ohne Internet werden Systemschriften verwendet. Für eine öffentliche Bereitstellung fehlen insbesondere HTTPS/WSS, Rate-Limits und ein robustes Sitzungs- und Wiederverbindungskonzept.

## Struktur und Weiterentwicklung

- `frontend/src/main.jsx`: React-Oberfläche und WebSocket-Lebenszyklus
- `frontend/src/style.css`: Layout und responsive Gestaltung
- `backend/src/main/java/at/gr8/connect4/Game.java`: reine, separat testbare Spielregeln
- `backend/src/main/java/at/gr8/connect4/Rooms.java`: Räume, Teilnehmer, Nachrichten und Revanche
- `tests/multiplayer.mjs`: Integrationstest gegen laufende Server

Sinnvolle nächste Schritte: PostgreSQL und abgeschlossene Partien speichern; Benutzerkonten und serverseitige Sitzungen ergänzen; danach ELO aus den gespeicherten Ergebnissen berechnen. Das ersetzt schrittweise die Gastnamen und die flüchtige Raumverwaltung, ohne die Spielregeln neu schreiben zu müssen.

## Prüfen

### Manuell in zwei Browser-Tabs

1. Beide Server starten und warten, bis Vite die URL ausgibt und Spring Boot `Started Application` meldet.
2. `http://localhost:5174` öffnen. Oben muss **Server verbunden** stehen.
3. Als **Alice** einen privaten Raum erstellen. Den angezeigten Einladungslink in einem zweiten Tab öffnen.
4. Im zweiten Tab **Bob** eingeben und **Beitreten** wählen. Alice spielt Koralle und beginnt; Bob spielt Gold.
5. Für einen einfachen Sieg abwechselnd diese Spalten anklicken: Alice **1**, Bob **2**, Alice **1**, Bob **2**, Alice **1**, Bob **2**, Alice **1**.
6. Erwartung: Beide Tabs zeigen dieselben sieben Steine und **Alice gewinnt!**. Die vier korallenfarbenen Steine sind markiert. Weitere Züge sind gesperrt.
7. In beiden Tabs die Revanche bestätigen. Erwartung: Das Brett ist leer; Bob spielt jetzt Koralle und beginnt.
8. Einen Tab schließen. Erwartung: Im anderen Tab erscheint die Meldung, dass der Mitspieler den Raum verlassen hat.

Zusätzlich prüfen: Ein dritter Tab darf einem vollen Raum nicht beitreten; ein erfundener Raumcode zeigt eine Fehlermeldung; der wartende Spieler kann keinen Stein setzen.

### Automatisierte Tests

Jeden der folgenden Befehlsblöcke aus dem Projektstamm starten. Für die Regeltests und den Frontend-Build müssen die Server nicht laufen.

```sh
cd backend
mvn test
```

```sh
cd frontend
npm run build
```

Bei laufenden Servern aus dem Projektstamm:

```sh
node tests/multiplayer.mjs
```

Der Integrationstest verwendet eigene Testräume und prüft Beitritt, Kapazität, falsche Zugreihenfolge, ungültige Eingaben, Synchronisierung, Sieg, Revanche und Aufräumen bei Disconnect. `WS_URL` kann einen anderen Test-Endpunkt festlegen.

Erwartete Ergebnisse: `mvn test` meldet **7 Tests, 0 Fehler**, der Frontend-Build endet erfolgreich und der Integrationstest gibt eine Zeile beginnend mit **PASS:** aus.

## Häufige Startprobleme

| Problem | Lösung |
| --- | --- |
| `mvn` oder `node` wird nicht gefunden | Maven bzw. Node.js installieren, zum PATH hinzufügen und ein neues Terminal öffnen. |
| `Port 8081/5174 is already in use` | Prüfen, ob Connect4 schon läuft. Falls ja, die vorhandene Seite öffnen. Andernfalls den betreffenden eigenen Entwicklungsserver beenden. |
| Webseite lädt, aber keine Serververbindung | Backend-Terminal auf Fehler prüfen; der Server muss auf Port 8081 laufen. |
| `npm run dev` findet Vite nicht | Im Ordner `frontend` zuerst `npm install` ausführen. |
| PowerShell lässt den Starter nicht ausführen | Den oben beschriebenen manuellen Start in zwei Terminals verwenden. |
| Handy erreicht die Seite nicht | Beide Geräte ins gleiche WLAN bringen und die von Vite angezeigte Network-Adresse verwenden. `localhost` auf dem Handy bezeichnet das Handy selbst. |
| Integrationstest meldet einen Timeout | Backend und Frontend starten und zuerst im Browser auf **Server verbunden** prüfen. |

Die Entwicklungsserver lassen sich in ihren Terminals mit **Strg+C** beenden. `npm run build` erstellt nur das Frontend-Bundle; der Befehl startet keine Server und veröffentlicht die App nicht.

Technische Referenz: https://docs.spring.io/spring-boot/system-requirements.html
