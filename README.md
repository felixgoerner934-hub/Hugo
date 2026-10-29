# HugoSpawner – Fabric-Mod (Minecraft 1.21.11)

Spawner-System angelehnt an HugoSMP: Loot-Erzeugung ohne echte Mobs,
Stacking statt Upgrade-Stufen, eigener Shop über `/spawner`.

## Was schon drin ist (Phase 2+3 aus dem Plan)

- 7 Spawner-Typen als Block + Item: Skelett, Creeper, Spinne, Kuh, Golem, Piglin, Lohe
- Preise/Drops zentral editierbar in `src/main/resources/data/hugospawner/spawner_types.json`
  (keine Java-Kenntnisse nötig, nur Zahlen/Text im Editor ändern)
- Spawner-BlockEntity: Lager mit "Auslastung" (Füllstand), mehrseitig (36 Slots/Seite),
  Stacking per Rechtsklick mit passendem Spawner-Item
- `/spawner`-Befehl öffnet eine Chest-Style-Shop-GUI mit allen 7 Typen
- Button-Leiste im Spawner-Menü: Alles verkaufen, vorherige/nächste Seite, aktuelle Seite droppen
- einfaches eigenständiges Gem-Guthaben (NICHT an echtes Geld gekoppelt)

## Was noch fehlt / offen ist

1. **Yarn-Mappings-Build-Nummer** in `gradle.properties` – da steht ein Platzhalter
   (`1.21.11+build.1`). Echten Wert holen: https://fabricmc.net/develop/ → 1.21.11 auswählen,
   oder in IntelliJ nach dem ersten Sync-Versuch den Fehler lesen (der nennt meist den
   korrekten Build).
2. **Texturen**: Blöcke/Items nutzen aktuell `minecraft:block/amethyst_block` als
   Platzhalter-Textur (lila, damit es wenigstens zum Farbschema passt). Eigene Texturen
   unter `assets/hugospawner/textures/block/<typ>_spawner.png` ablegen und die
   `models/block/<typ>_spawner.json`-Dateien entsprechend anpassen.
3. **GUI-Hintergrundtextur**: `SpawnerScreen`/`SpawnerShopScreen` zeichnen aktuell nur
   ein dunkles Rechteck statt der echten lila "SPAWNER"-Kopfzeile aus deinen
   Screenshots. Sobald du ein Textur-Asset hast, in `drawBackground` einbauen
   (Stelle ist im Code mit `FILL` markiert).
4. **Verkaufspreise pro Item** sind reine Platzhalter (1 Gem pro Item, in
   `GemManager.SELL_PRICE_PER_ITEM_PLACEHOLDER`). Noch nicht aus HugoSMP bestätigt.
5. **Drops/Preise der anderen 6 Spawner-Typen** (nur Skelett ist mit 650 Gems /
   Bones+Arrows bestätigt) – in `spawner_types.json` stehen plausible Platzhalter,
   markiert mit `"confirmed": false`.
6. **API-Stellen mit Versions-Risiko**: 1.21.11 ist eine sehr neue Minecraft-Version,
   manche Methodensignaturen (`Block#onUse`, `Item#appendTooltip`) wurden zwischen
   1.20 und 1.21.x mehrfach umgebaut. Die betroffenen Stellen sind im Code mit
   `ACHTUNG`-Kommentaren markiert – beim ersten Kompilieren zeigt IntelliJ dir rot an,
   falls sich die Signatur nochmal geändert hat; das lässt sich dann 1:1 anpassen.

## Projekt öffnen (Schritt für Schritt)

1. [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) installieren (Community reicht).
2. [JDK 21](https://adoptium.net/) installieren, falls noch nicht vorhanden.
3. Diesen Ordner entpacken.
4. IntelliJ → "Open" → den entpackten `hugospawner`-Ordner auswählen.
5. IntelliJ erkennt automatisch Gradle und lädt alle Abhängigkeiten herunter
   (erster Start dauert ein paar Minuten, braucht Internet).
6. Falls der Sync wegen der Yarn-Mappings-Nummer fehlschlägt: Punkt 1 oben befolgen.
7. Zum Testen: Gradle-Tab rechts → `Tasks` → `fabric` → `runClient` doppelklicken.
   Startet ein Testclient mit der Mod bereits geladen.
8. Für eine fertige JAR zum Verteilen: Gradle-Tab → `Tasks` → `build` → `build`
   doppelklicken. Die JAR liegt danach unter `build/libs/hugospawner-0.1.0.jar`.

## Nächste Schritte, die ich vorschlage

- Du schickst mir Preise/Beschreibungen für die anderen 6 Spawner-Typen (Screenshot
  vom Shop reicht, ein Hover pro Typ) → ich trage echte Werte in `spawner_types.json` ein.
- Du schickst mir, falls vorhanden, Screenshots der echten Button-Tooltips im
  Spawner-Menü → ich kann die Texte exakt übernehmen statt frei zu formulieren.
- Sobald du eigene Texturen hast (oder ich dir helfen soll, einfache Platzhalter-Texturen
  zu bauen), binden wir die GUI- und Block-Texturen final ein.
