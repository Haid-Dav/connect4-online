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

Voraussetzungen: JDK 21 oder neuer (getestet mit 24), Maven 3.6.3+, Node.js 22.12+ und npm oder pnpm.

Einmalig im Ordner `frontend`:

```sh
npm install
```

Alternativ `pnpm install`; ein pnpm-Lockfile liegt bei. Dann unter Windows `./Start-Connect4.ps1` aus PowerShell starten. Der Starter findet Maven im PATH oder im vorhandenen Maven-Wrapper-Cache. Die Server laufen bis zum Beenden mit Strg+C.

Manueller Start in zwei Terminals:

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

Technische Referenz: https://docs.spring.io/spring-boot/system-requirements.html
