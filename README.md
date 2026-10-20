# FitPlan 🧘

Eine Android-App (optimiert für Samsung-Handys mit One UI) für **tägliche Rückenübungen** und die **Planung von Nahrungsergänzungsmitteln** – mit zuverlässigen Erinnerungen, geführtem Training und Fortschrittsanzeige. Alle Daten bleiben auf dem Handy, es gibt kein Konto und keine Internetverbindung.

## 📲 Installation (Android)

### **[⬇️ FitPlan.apk herunterladen](https://github.com/moster803/FitPlan/releases/latest/download/FitPlan.apk)**

Der Link lädt immer die neueste Version. FitPlan läuft auf Android-Handys ab Android 8 (für iPhones gibt es keine Version).

1. Den Link oben **auf dem Handy** antippen – die Datei `FitPlan.apk` wird heruntergeladen.
2. Die Datei öffnen: über die Meldung „Download abgeschlossen“ oder in **„Eigene Dateien“ › Downloads**.
3. Android fragt, ob der Browser **„Unbekannte Apps installieren“** darf: Einstellungen öffnen, erlauben, zurück und **Installieren** tippen.
4. Falls Google Play Protect warnt: **„Weitere Details“ › „Trotzdem installieren“**. FitPlan kommt nicht aus dem Play Store, deshalb die Warnung.
5. FitPlan öffnen und der Einrichtung folgen (Name, Training, Uhrzeit, Supplements, Erlaubnisse).

**Updates:** Die neue Version über denselben Link laden und einfach drüber installieren – alle Daten bleiben erhalten. Was sich geändert hat, steht unter [Releases](https://github.com/moster803/FitPlan/releases).

### Damit Erinnerungen zuverlässig ankommen (vor allem auf Samsung)
- Benachrichtigungen erlauben (fragt die App).
- Akku-Optimierung für FitPlan ausschalten (Knopf in der App).
- **Einstellungen › Akku › Hintergrundnutzungslimits › „Nie schlafende Apps“ › FitPlan hinzufügen.**
- Mit **Einstellungen › Test-Benachrichtigung senden** in FitPlan prüfen, ob alles klappt.

## Funktionen

### Training
- **Fertige Pläne** als Vorlage: „Rücken täglich“ (≈ 12 Min), „Rücken-Kräftigung“, „Pilates für den Rücken“, „Rücken-Spaziergang“ (nach der WalkBack-Studie), „Nacken & Schultern“, „Büro-Pause“ und „Abend-Entspannung“.
- **Evidenzbasierte Übungsbibliothek** mit 40 Übungen aus acht Übungsformen, deren Wirkung bei Rücken- bzw. Nackenschmerzen in Studien belegt ist: Pilates, Stabilisation, Kräftigung, Yoga, Dehnung, McKenzie, Gehen sowie Nacken & Schultern. Jede Übung hat eine Schritt-für-Schritt-Anleitung, einen Tipp und ihre wissenschaftliche Grundlage mit Quellen-Links.
- **„Was sagt die Forschung?“**: Kernaussagen aus Leitlinien und Studien, Übersicht der Übungsformen, Warnzeichen für einen Arztbesuch und alle Quellen.
- **Eigene Übungen** anlegen, z. B. die aus der Physiotherapie.
- **Plan-Editor**: Name, Symbol, Farbe, Trainingstage, Reihenfolge, Sätze, Wiederholungen oder Zeit, Pausen.
- **Geführter Trainings-Player**: großer Countdown-Ring, automatische Pausen, Seitenwechsel bei einseitigen Übungen, Töne, Vibration und **deutsche Sprachansagen** – man muss beim Üben nicht aufs Handy schauen. Der Bildschirm bleibt an.
- Nach dem Training: **„Wie fühlt sich dein Rücken an?“** (5 Stufen) – der Verlauf zeigt, wie sich das Rückengefühl entwickelt.
- „Schon erledigt“ abhaken, z. B. nach der Physiotherapie-Stunde.

### Erinnerungen
- Pünktliche tägliche Erinnerung pro Plan, nur an den gewählten Tagen.
- **Nachhaken**: Ist das Training abends noch offen, kommt eine zweite, sanfte Erinnerung.
- Aktionen direkt in der Benachrichtigung: **Starten**, **In 30 Min**, **Schon erledigt**.
- Erinnerungen werden nach Neustart, Zeitumstellung und App-Update automatisch neu geplant.
- **Samsung-Hinweise** in Einrichtung und Einstellungen: Akku-Optimierung, „Nie schlafende Apps“ und eine Test-Benachrichtigung.

### Supplements
- **16 Vorlagen** (Vitamin D3, Magnesium, Omega-3, Zink, Eisen, B12, Kreatin …) mit üblichen Einnahmezeiten und Hinweisen.
- Tageszeiten (morgens, mittags, abends, vor dem Schlafen) mit einstellbaren Uhrzeiten, Wochentage, Einnahme-Hinweis (zum Essen, nüchtern …).
- **Eine gesammelte Erinnerung pro Tageszeit** mit Knopf „Alle genommen“ – sie bleibt stumm, wenn schon alles abgehakt ist.
- **Vorratsverwaltung**: zählt bei jeder Einnahme herunter, zeigt „reicht noch ca. X Tage“ und warnt rechtzeitig vor dem Nachkaufen.
- **Kur-Enddatum** für zeitlich begrenzte Einnahmen.
- **Kombinations-Hinweise**, z. B. wenn Eisen und Calcium zur selben Tageszeit eingeplant sind.

### Motivation & Überblick
- **Serie (Streak)** und Wochenleiste auf der Startseite, Rekord-Serie.
- **Verlauf**: Monatskalender, Statistik, Rückengefühl-Diagramm, Supplement-Einnahmequote.
- **Homescreen-Widget** mit Serie, heutigem Training und Supplement-Fortschritt.
- Tipp des Tages für einen gesunden Rücken.

### Sonstiges
- Hell-/Dunkelmodus, optional Farben vom Hintergrundbild (Material You).
- **Backup** als Datei speichern und wiederherstellen (z. B. beim Handywechsel).

## Entwicklung

- Kotlin, Jetpack Compose, Material 3, Navigation Compose, Glance (Widget)
- Android Gradle Plugin 9.3, Kotlin 2.4, Gradle 9.5, minSdk 26 (Android 8), targetSdk 36
- Daten als JSON-Datei im App-Speicher (atomar geschrieben), keine Netzwerkzugriffe

```
app/src/main/java/de/fitplan/app/
├── core/          # reine Kotlin-Logik ohne Android – mit Unit-Tests
│   ├── model/     # Datenmodell (Pläne, Übungen, Supplements, Verlauf)
│   ├── data/      # Repository und Speicherung
│   ├── logic/     # Serien, Statistiken, Erinnerungszeitplan, Vorrat
│   ├── library/   # Übungsbibliothek und Vorlagen
│   └── workout/   # Ablauf eines Trainings (Phasen, Timer-Zustand)
├── reminders/     # AlarmManager, Benachrichtigungen, Receiver
├── widget/        # Homescreen-Widget
└── ui/            # Compose-Oberfläche
```

Bauen und testen:

```bash
./gradlew testDebugUnitTest   # Unit-Tests
./gradlew assembleRelease     # APK unter app/build/outputs/apk/release/
```

Jeder Push baut und testet die App per GitHub Actions (Unit-Tests, Lint, UI-Test im Emulator, Stresstest mit zufälliger Bedienung auf Android 10 und 16). Ein Release entsteht, sobald alles grün ist, auf einem dieser Wege:
- **automatisch**, wenn auf dem Standard-Branch eine neue `versionName` steht (in `app/build.gradle.kts`, dazu `versionCode` erhöhen und `.github/release-notes.md` anpassen),
- über **Actions › Android-Build › „Run workflow“** mit der Version im Feld „Als Release veröffentlichen“ (z. B. `v1.1.0`),
- unter **Releases › „Draft a new release“** mit einem neuen Tag `v*`,
- oder mit einem per `git push` hochgeladenen Tag `v*`.

Der Workflow hängt dann die `FitPlan.apk` an und übernimmt die Beschreibung aus `.github/release-notes.md`.

**Signatur:** Release-APKs sind mit `app/signing/fitplan-release.p12` signiert. Die Datei ist verschlüsselt, das Passwort steht nur im Repository-Secret `FITPLAN_KEYSTORE_PASSWORD`. Ohne dieses Secret – lokal oder in Forks – signiert Gradle mit dem Debug-Schlüssel, und der Workflow veröffentlicht nichts; vor jedem Release prüft er zusätzlich den Fingerabdruck des Zertifikats. Wer das Passwort verliert, kann keine Updates mehr bauen, die sich über die installierte App legen.

> Für den Play Store wäre außerdem die Berechtigung `USE_EXACT_ALARM` zu prüfen, die Google dort nur Wecker- und Kalender-Apps erlaubt.

## Wissenschaftliche Grundlage

In die Bibliothek wurden nur Übungen aus Übungsformen aufgenommen, die in Leitlinien, Cochrane-Reviews oder großen randomisierten Studien untersucht wurden. Studien prüfen dabei meist ganze Übungsprogramme, nicht einzelne Übungen. Die wichtigsten Quellen:

- Nationale VersorgungsLeitlinie Nicht-spezifischer Kreuzschmerz (2017) und NICE-Leitlinie NG59 – Bewegungstherapie als zentrale Behandlung, keine Bettruhe
- Hayden et al. 2021, Cochrane – Übungstherapie bei chronischen Kreuzschmerzen (249 Studien)
- Netzwerk-Metaanalysen: Hayden et al. 2021 (J Physiother), Owen et al. 2020 (BJSM), Fernández-Rodríguez et al. 2022 (JOSPT) – Pilates, Stabilisation und Kräftigung schneiden am besten ab
- Cochrane-Reviews zu Pilates (Yamato 2015), Motorkontrolle (Saragiotto 2016) und Yoga (Wieland 2022)
- Saper et al. 2017 und Sherman et al. 2011 – Yoga und Dehnprogramme
- Lam et al. 2018 – McKenzie-Methode
- Pocovi et al. 2024, The Lancet (WalkBack) – Gehen verzögert Rückfälle
- Steffens et al. 2016, JAMA Intern Med – Übungen beugen Rückenschmerzen vor
- Gross et al. 2015, Cochrane, und Andersen et al. 2008 – Kräftigung bei Nackenschmerzen

Die vollständige Liste mit Links steht in der App und in `app/src/main/java/de/fitplan/app/core/library/Evidence.kt`.

## Hinweis

FitPlan ersetzt keine ärztliche oder physiotherapeutische Beratung. Bei Schmerzen Übungen abbrechen und ärztlich abklären lassen. Die Supplement-Hinweise sind allgemein gehalten – Fragen zu Dosierung und Wechselwirkungen bitte mit Ärztin, Arzt oder Apotheke klären.
