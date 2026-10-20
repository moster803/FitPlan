# FitPlan 🧘

Eine Android-App (optimiert für Samsung-Handys mit One UI) für **tägliche Rückenübungen** und die **Planung von Nahrungsergänzungsmitteln** – mit zuverlässigen Erinnerungen, geführtem Training und Fortschrittsanzeige. Alle Daten bleiben auf dem Handy, es gibt kein Konto und keine Internetverbindung.

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

## Installation auf dem Samsung-Handy

1. Auf GitHub unter **Actions → Android-Build → letzter erfolgreicher Lauf** das Artefakt **FitPlan-apk** herunterladen und entpacken – oder, falls vorhanden, die `FitPlan.apk` unter **Releases** laden.
2. Die `FitPlan.apk` auf dem Handy öffnen (z. B. über „Eigene Dateien“).
3. Beim ersten Mal fragt Android nach der Erlaubnis **„Unbekannte Apps installieren“** für die App, mit der du die Datei öffnest – erlauben.
4. Falls Google Play Protect warnt: **„Trotzdem installieren“**.
5. FitPlan öffnen und den Einrichtungsassistenten durchgehen (Plan, Uhrzeit, Supplements, Erlaubnisse).

**Updates:** Einfach die neue `FitPlan.apk` über die alte installieren – alle Daten bleiben erhalten, weil jede Version mit demselben Schlüssel signiert ist.

### Damit Erinnerungen zuverlässig ankommen (Samsung)
- Benachrichtigungen erlauben (fragt die App).
- Akku-Optimierung für FitPlan ausschalten (Knopf in der App).
- **Einstellungen › Akku › Hintergrundnutzungslimits › „Nie schlafende Apps“ › FitPlan hinzufügen.**
- Mit **Einstellungen › Test-Benachrichtigung senden** prüfen, ob alles klappt.

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
./gradlew assembleRelease     # signiertes APK unter app/build/outputs/apk/release/
```

Jeder Push baut die App per GitHub Actions. Ein Release entsteht auf einem dieser Wege:
- **Actions › Android-Build › „Run workflow“** und bei „Als Release veröffentlichen“ die Version eintragen (z. B. `v1.1.0`),
- unter **Releases › „Draft a new release“** einen neuen Tag `v*` anlegen und veröffentlichen,
- oder einen Tag `v*` per `git push` hochladen.

Der Workflow hängt dann die `FitPlan.apk` an und übernimmt die Beschreibung aus `.github/release-notes.md`.

> Der mitgelieferte Signaturschlüssel (`app/signing/fitplan.jks`) ist nur für selbst installierte APKs gedacht. Für eine Veröffentlichung im Play Store einen eigenen, geheimen Schlüssel verwenden – und die Berechtigung `USE_EXACT_ALARM` prüfen, die Google nur Wecker- und Kalender-Apps erlaubt.

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
