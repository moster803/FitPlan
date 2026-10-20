package de.fitplan.app.core.library

import de.fitplan.app.core.model.Approach

/** Eine wissenschaftliche Quelle mit Kernaussage und Link (DOI, PubMed oder Leitlinie). */
data class Source(
    val id: String,
    val citation: String,
    val finding: String,
    val url: String,
)

/** Übungsform mit einer kurzen, ehrlichen Zusammenfassung der Studienlage. */
data class ApproachInfo(
    val approach: Approach,
    val name: String,
    val emoji: String,
    val summary: String,
    val sourceIds: List<String>,
)

/** Eine Kernaussage mit den Quellen, auf denen sie beruht. */
data class Finding(val text: String, val sourceIds: List<String>)

/**
 * Studienlage zu Übungen bei nicht-spezifischen Rücken- und Nackenschmerzen.
 *
 * Wichtig: Studien untersuchen in der Regel Übungsprogramme, nicht einzelne Übungen. Jede Übung
 * der Bibliothek gehört zu einer Übungsform, die in Leitlinien, Cochrane-Reviews oder großen
 * randomisierten Studien untersucht wurde.
 */
object Evidence {

    val sources: List<Source> = listOf(
        Source(
            id = "nvl2017",
            citation = "Nationale VersorgungsLeitlinie Nicht-spezifischer Kreuzschmerz, 2. Auflage, 2017",
            finding = "Bewegungstherapie kombiniert mit Aufklärung soll zur Behandlung subakuter und chronischer Kreuzschmerzen eingesetzt werden; von Bettruhe wird abgeraten.",
            url = "https://www.leitlinien.de/themen/kreuzschmerz",
        ),
        Source(
            id = "nice2016",
            citation = "NICE-Leitlinie NG59 „Low back pain and sciatica in over 16s“, 2016",
            finding = "Gruppen-Übungsprogramme (kräftigend, aerob, Körper-Geist-Verfahren oder kombiniert) werden empfohlen; die Übungsform soll zu Bedürfnissen und Vorlieben passen.",
            url = "https://www.nice.org.uk/guidance/ng59",
        ),
        Source(
            id = "hayden2021",
            citation = "Hayden et al. 2021, Cochrane Review „Exercise therapy for chronic low back pain“",
            finding = "249 Studien mit über 24.000 Teilnehmenden: Übungstherapie lindert chronische Kreuzschmerzen wahrscheinlich besser als keine Behandlung.",
            url = "https://doi.org/10.1002/14651858.CD009790.pub2",
        ),
        Source(
            id = "hayden2021nma",
            citation = "Hayden et al. 2021, Journal of Physiotherapy (Netzwerk-Metaanalyse)",
            finding = "217 Studien: Die meisten Übungsformen wirken besser als minimale Behandlung; Pilates, McKenzie und funktionelles Aufbautraining schnitten am besten ab, Dehnübungen verbesserten die Funktion. Empfehlung: die Übungsform wählen, die Freude macht.",
            url = "https://doi.org/10.1016/j.jphys.2021.09.004",
        ),
        Source(
            id = "owen2020",
            citation = "Owen et al. 2020, British Journal of Sports Medicine (Netzwerk-Metaanalyse)",
            finding = "89 Studien: Pilates (Schmerz), Kraft- und Stabilisationstraining (Funktion) sowie Ausdauertraining (psychisches Wohlbefinden) schnitten am besten ab – bei insgesamt niedriger Evidenzqualität.",
            url = "https://pubmed.ncbi.nlm.nih.gov/31666220/",
        ),
        Source(
            id = "fernandez2022",
            citation = "Fernández-Rodríguez et al. 2022, JOSPT (Netzwerk-Metaanalyse)",
            finding = "Pilates, Krafttraining, Rumpfstabilisation und Körper-Geist-Übungen reduzierten Schmerz und Einschränkungen am stärksten; bereits 1–2 Einheiten pro Woche waren wirksam.",
            url = "https://pubmed.ncbi.nlm.nih.gov/35722759/",
        ),
        Source(
            id = "yamato2015",
            citation = "Yamato et al. 2015, Cochrane Review „Pilates for low back pain“",
            finding = "Pilates lindert Schmerzen und verbessert die Funktion im Vergleich zu minimaler Behandlung (niedrige bis moderate Evidenz); gegenüber anderen Übungen nicht eindeutig überlegen.",
            url = "https://doi.org/10.1002/14651858.CD010265.pub2",
        ),
        Source(
            id = "saragiotto2016",
            citation = "Saragiotto et al. 2016, Cochrane Review „Motor control exercise for chronic non-specific low-back pain“",
            finding = "29 Studien: Motorkontroll- bzw. Stabilisationsübungen wirken besser als minimale Behandlung und etwa so gut wie andere Übungsformen.",
            url = "https://doi.org/10.1002/14651858.CD012004",
        ),
        Source(
            id = "wieland2022",
            citation = "Wieland et al. 2022, Cochrane Review „Yoga for chronic non-specific low back pain“",
            finding = "21 Studien: Yoga verbessert die Funktion moderat und senkt Schmerzen etwas; im Vergleich zu anderen Rückenübungen kaum Unterschied.",
            url = "https://doi.org/10.1002/14651858.CD010671.pub3",
        ),
        Source(
            id = "saper2017",
            citation = "Saper et al. 2017, Annals of Internal Medicine (randomisierte Studie, 320 Teilnehmende)",
            finding = "Ein 12-wöchiges Yoga-Programm war bei chronischen Kreuzschmerzen nicht schlechter als Physiotherapie; beide senkten den Schmerzmittelbedarf.",
            url = "https://pubmed.ncbi.nlm.nih.gov/28631003/",
        ),
        Source(
            id = "sherman2011",
            citation = "Sherman et al. 2011, Archives of Internal Medicine (randomisierte Studie, 228 Teilnehmende)",
            finding = "Yoga- und Dehnkurse verbesserten Funktion und Beschwerden stärker als ein Selbsthilfebuch – und waren untereinander gleich wirksam.",
            url = "https://pubmed.ncbi.nlm.nih.gov/22025101/",
        ),
        Source(
            id = "lam2018",
            citation = "Lam et al. 2018, JOSPT (Metaanalyse zur McKenzie-Methode)",
            finding = "Die McKenzie-Methode ist anderen aktiven Reha-Verfahren insgesamt nicht überlegen; das Ergebnis hängt von der Vergleichsbehandlung ab.",
            url = "https://pubmed.ncbi.nlm.nih.gov/29602304/",
        ),
        Source(
            id = "pocovi2024",
            citation = "Pocovi et al. 2024, The Lancet – WalkBack-Studie (701 Teilnehmende)",
            finding = "Ein Gehprogramm mit Beratung verlängerte die Zeit bis zum nächsten Rückfall von im Mittel 112 auf 208 Tage.",
            url = "https://doi.org/10.1016/S0140-6736(24)00755-4",
        ),
        Source(
            id = "steffens2016",
            citation = "Steffens et al. 2016, JAMA Internal Medicine (Metaanalyse zur Vorbeugung)",
            finding = "Übungen – besonders kombiniert mit Aufklärung – senken das Risiko für neue Kreuzschmerz-Episoden; Rückengurte und Schuheinlagen helfen nicht.",
            url = "https://doi.org/10.1001/jamainternmed.2015.7431",
        ),
        Source(
            id = "gross2015",
            citation = "Gross et al. 2015, Cochrane Review „Exercises for mechanical neck disorders“",
            finding = "Kräftigungs- und Ausdauerübungen für Nacken, Schulterblätter und Schultern können Schmerzen lindern und die Funktion verbessern; reines Dehnen bringt eher keinen Nutzen.",
            url = "https://doi.org/10.1002/14651858.CD004250.pub5",
        ),
        Source(
            id = "andersen2008",
            citation = "Andersen et al. 2008, Arthritis & Rheumatism (randomisierte Studie)",
            finding = "Gezieltes Hanteltraining für Nacken und Schultern (u. a. Rudern, Seitheben, Schulterheben, Reverse Flys) senkte chronische Nackenschmerzen deutlich – stärker als allgemeines Fitnesstraining.",
            url = "https://pubmed.ncbi.nlm.nih.gov/18163419/",
        ),
    )

    val approaches: List<ApproachInfo> = listOf(
        ApproachInfo(
            Approach.PILATES, "Pilates", "🤸",
            "Gehört in mehreren Netzwerk-Metaanalysen zu den wirksamsten Übungsformen bei chronischen Kreuzschmerzen – bei niedriger bis moderater Evidenzqualität.",
            listOf("yamato2015", "hayden2021nma", "fernandez2022", "owen2020"),
        ),
        ApproachInfo(
            Approach.MOTOR_CONTROL, "Stabilisation", "🎯",
            "Gezielte Ansteuerung der tiefen Rumpfmuskulatur: wirksamer als minimale Behandlung, etwa so gut wie andere Übungsformen.",
            listOf("saragiotto2016", "owen2020", "fernandez2022"),
        ),
        ApproachInfo(
            Approach.STRENGTH, "Kräftigung", "💪",
            "Krafttraining verbessert vor allem Funktion und Belastbarkeit im Alltag und zählt zu den am besten bewerteten Übungsformen.",
            listOf("owen2020", "fernandez2022", "hayden2021"),
        ),
        ApproachInfo(
            Approach.YOGA, "Yoga", "🧘",
            "Rückenspezifisches Yoga verbessert die Funktion moderat – in Studien ähnlich wirksam wie Physiotherapie oder Dehnprogramme.",
            listOf("wieland2022", "saper2017", "sherman2011"),
        ),
        ApproachInfo(
            Approach.STRETCHING, "Dehnung", "🙆",
            "Ein Dehnprogramm wirkte in einer großen Studie so gut wie Yoga; insgesamt ist die Studienlage für Dehnen allein aber gemischt – am besten mit kräftigenden Übungen kombinieren.",
            listOf("sherman2011", "hayden2021nma"),
        ),
        ApproachInfo(
            Approach.MCKENZIE, "McKenzie", "↩️",
            "Wiederholte Streckbewegungen der Wirbelsäule. In einer Netzwerk-Metaanalyse wirksam gegen Schmerzen, anderen aktiven Verfahren aber nicht überlegen. Werden Schmerzen stärker oder strahlen ins Bein aus: abbrechen.",
            listOf("hayden2021nma", "lam2018"),
        ),
        ApproachInfo(
            Approach.WALKING, "Gehen & Ausdauer", "🚶",
            "Regelmäßiges zügiges Gehen verzögerte Rückfälle deutlich; Ausdauertraining verbessert zudem das Wohlbefinden.",
            listOf("pocovi2024", "owen2020", "steffens2016"),
        ),
        ApproachInfo(
            Approach.NECK, "Nacken & Schultern", "🦒",
            "Kräftigung von Nacken, Schulterblättern und Schultern lindert chronische Nackenschmerzen; reines Dehnen hilft hier wenig.",
            listOf("gross2015", "andersen2008"),
        ),
    )

    val keyFindings: List<Finding> = listOf(
        Finding("Bewegung wirkt: Übungstherapie lindert chronische Kreuzschmerzen nachweislich und ist in Leitlinien die wichtigste Behandlung.", listOf("hayden2021", "nvl2017", "nice2016")),
        Finding("Aktiv bleiben statt schonen – Bettruhe verlängert Rückenschmerzen eher.", listOf("nvl2017")),
        Finding("Keine Übungsform ist allen anderen klar überlegen. Am besten schneiden Pilates, Stabilisation und Kräftigung ab – wichtiger ist, regelmäßig dranzubleiben.", listOf("hayden2021nma", "owen2020", "fernandez2022")),
        Finding("Schon 1–2 Einheiten pro Woche zeigen Wirkung; Programme über mehrere Wochen sind sinnvoll.", listOf("fernandez2022")),
        Finding("Zügiges Gehen an mehreren Tagen pro Woche beugt Rückfällen vor.", listOf("pocovi2024")),
        Finding("Übungen schützen auch vorbeugend vor neuen Rückenschmerzen – solange man sie beibehält.", listOf("steffens2016")),
        Finding("Bei Nackenschmerzen helfen vor allem kräftigende Übungen für Nacken und Schultern.", listOf("gross2015", "andersen2008")),
    )

    /** Warnzeichen, bei denen Rückenschmerzen ärztlich abgeklärt werden sollten (nach NVL Kreuzschmerz). */
    val redFlags: List<String> = listOf(
        "Taubheit im Genital- oder Gesäßbereich, Probleme beim Wasserlassen oder Stuhlgang – sofort in die Notaufnahme",
        "Lähmungen oder zunehmende Schwäche in Beinen oder Füßen",
        "Fieber, Schüttelfrost oder allgemeines Krankheitsgefühl",
        "Schmerzen nach einem Sturz oder Unfall, besonders bei Osteoporose",
        "Krebserkrankung in der Vorgeschichte, ungewollter Gewichtsverlust",
        "Starke nächtliche Schmerzen, die sich in Ruhe nicht bessern",
        "Keine Besserung nach etwa 4–6 Wochen",
    )

    private val sourceById = sources.associateBy { it.id }

    fun source(id: String): Source? = sourceById[id]

    fun info(approach: Approach): ApproachInfo = approaches.first { it.approach == approach }
}
