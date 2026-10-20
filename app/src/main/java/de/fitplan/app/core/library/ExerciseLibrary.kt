package de.fitplan.app.core.library

import de.fitplan.app.core.model.Exercise
import de.fitplan.app.core.model.ExerciseCategory.MOBILITY
import de.fitplan.app.core.model.ExerciseCategory.RELAX
import de.fitplan.app.core.model.ExerciseCategory.STRENGTH
import de.fitplan.app.core.model.ExerciseCategory.STRETCH
import de.fitplan.app.core.model.ExerciseMode.REPS
import de.fitplan.app.core.model.ExerciseMode.TIMED

/** Eingebaute Übungen mit Schwerpunkt Rücken. Die IDs sind stabil und werden in Plänen gespeichert. */
object ExerciseLibrary {

    val all: List<Exercise> = listOf(
        Exercise(
            id = "cat_cow",
            name = "Katze-Kuh",
            description = "Mobilisiert die gesamte Wirbelsäule – ideal zum Aufwärmen.",
            steps = listOf(
                "Vierfüßlerstand: Hände unter den Schultern, Knie unter der Hüfte.",
                "Ausatmen: Rücken rund machen wie ein Katzenbuckel, Kinn Richtung Brust.",
                "Einatmen: Brustbein nach vorn schieben, sanft ins leichte Hohlkreuz, Blick schräg nach vorn.",
                "Im eigenen Atemrhythmus fließend wechseln.",
            ),
            tip = "Langsam bewegen – jeder Wirbel darf mitmachen.",
            category = MOBILITY, mode = REPS, reps = 10,
            builtIn = true,
        ),
        Exercise(
            id = "pelvic_tilt",
            name = "Beckenkippen",
            description = "Sanfte Mobilisation der Lendenwirbelsäule im Liegen.",
            steps = listOf(
                "Rückenlage, Beine angestellt, Füße hüftbreit.",
                "Bauch leicht anspannen und den unteren Rücken in die Unterlage drücken.",
                "Spannung lösen und das Becken zurückrollen lassen.",
            ),
            tip = "Die Bewegung ist klein – Gesäß bleibt am Boden.",
            category = MOBILITY, mode = REPS, reps = 12,
            builtIn = true,
        ),
        Exercise(
            id = "lying_twist",
            name = "Liegende Drehung",
            description = "Löst Verspannungen im unteren Rücken.",
            steps = listOf(
                "Rückenlage, Knie angewinkelt und geschlossen, Arme seitlich ausgebreitet.",
                "Beide Knie langsam zu einer Seite sinken lassen.",
                "Schultern bleiben am Boden, ruhig weiteratmen.",
            ),
            tip = "Nur so weit, wie es angenehm ist.",
            category = STRETCH, mode = TIMED, seconds = 30, perSide = true,
            builtIn = true,
        ),
        Exercise(
            id = "open_book",
            name = "Offenes Buch",
            description = "Dreht die Brustwirbelsäule auf – gut gegen den Schreibtisch-Rundrücken.",
            steps = listOf(
                "Seitlage, Knie 90° angewinkelt, Arme gestreckt vor der Brust übereinander.",
                "Den oberen Arm wie eine Buchseite zur anderen Seite öffnen, der Blick folgt der Hand.",
                "Kurz halten und langsam zurückführen.",
            ),
            tip = "Die Knie bleiben aufeinander liegen.",
            category = MOBILITY, mode = REPS, reps = 8, perSide = true,
            builtIn = true,
        ),
        Exercise(
            id = "bird_dog",
            name = "Vierfüßler-Diagonale",
            description = "Kräftigt die tiefe Rückenmuskulatur und schult die Stabilität (Bird-Dog).",
            steps = listOf(
                "Vierfüßlerstand, Rücken gerade, Bauch leicht angespannt.",
                "Rechten Arm und linkes Bein gleichzeitig gestreckt anheben, bis sie eine Linie mit dem Rücken bilden.",
                "Zwei Sekunden halten, kontrolliert absenken.",
                "Seite wechseln.",
            ),
            tip = "Das Becken bleibt waagerecht – stell dir ein Glas Wasser auf deinem Rücken vor.",
            category = STRENGTH, mode = REPS, reps = 8, perSide = true,
            builtIn = true,
        ),
        Exercise(
            id = "glute_bridge",
            name = "Beckenheben",
            description = "Stärkt Gesäß und unteren Rücken (Glute Bridge).",
            steps = listOf(
                "Rückenlage, Füße hüftbreit aufgestellt, Arme neben dem Körper.",
                "Gesäß anspannen und das Becken anheben, bis Oberschenkel und Oberkörper eine Linie bilden.",
                "Zwei Sekunden halten und Wirbel für Wirbel ablegen.",
            ),
            tip = "Die Kraft kommt aus dem Po – nicht ins Hohlkreuz drücken.",
            category = STRENGTH, mode = REPS, reps = 12,
            builtIn = true,
        ),
        Exercise(
            id = "dead_bug",
            name = "Toter Käfer",
            description = "Trainiert die Rumpfstabilität, ohne den Rücken zu belasten (Dead Bug).",
            steps = listOf(
                "Rückenlage, Arme zur Decke gestreckt, Beine im 90°-Winkel angehoben.",
                "Unteren Rücken sanft in den Boden drücken.",
                "Rechten Arm und linkes Bein langsam Richtung Boden strecken und zurückholen.",
                "Seite wechseln.",
            ),
            tip = "Der untere Rücken bleibt die ganze Zeit am Boden.",
            category = STRENGTH, mode = REPS, reps = 8, perSide = true,
            builtIn = true,
        ),
        Exercise(
            id = "plank",
            name = "Unterarmstütz",
            description = "Ganzkörperspannung für einen starken Rumpf (Plank).",
            steps = listOf(
                "Unterarme unter den Schultern aufstützen, Beine gestreckt.",
                "Körper bildet eine gerade Linie von Kopf bis Ferse.",
                "Bauch und Po fest, ruhig weiteratmen.",
            ),
            tip = "Leichtere Variante: Knie am Boden lassen.",
            category = STRENGTH, mode = TIMED, seconds = 30,
            builtIn = true,
        ),
        Exercise(
            id = "side_plank",
            name = "Seitstütz",
            description = "Kräftigt die seitliche Rumpfmuskulatur.",
            steps = listOf(
                "Seitlage, Unterarm unter der Schulter aufstützen.",
                "Becken anheben, bis der Körper eine gerade Linie bildet.",
                "Halten, ruhig atmen, dann Seite wechseln.",
            ),
            tip = "Leichtere Variante: Knie angewinkelt am Boden lassen.",
            category = STRENGTH, mode = TIMED, seconds = 20, perSide = true,
            builtIn = true,
        ),
        Exercise(
            id = "superman",
            name = "Rückenstrecker",
            description = "Stärkt die Rückenstrecker in Bauchlage (Superman light).",
            steps = listOf(
                "Bauchlage, Stirn über dem Boden, Arme nach vorn gestreckt.",
                "Arme und Beine gleichzeitig wenige Zentimeter anheben.",
                "Zwei bis drei Sekunden halten und ablegen.",
            ),
            tip = "Blick bleibt zum Boden, der Nacken lang – nicht überstrecken.",
            category = STRENGTH, mode = REPS, reps = 10,
            builtIn = true,
        ),
        Exercise(
            id = "prone_w",
            name = "W-Heben in Bauchlage",
            description = "Aktiviert die Muskulatur zwischen den Schulterblättern.",
            steps = listOf(
                "Bauchlage, Arme zu einem „W“ angewinkelt, Daumen zeigen nach oben.",
                "Schulterblätter zusammen- und nach unten ziehen, Arme leicht anheben.",
                "Kurz halten und ablegen.",
            ),
            tip = "Schultern weg von den Ohren.",
            category = STRENGTH, mode = REPS, reps = 10,
            builtIn = true,
        ),
        Exercise(
            id = "childs_pose",
            name = "Kindhaltung",
            description = "Entspannt und dehnt den gesamten Rücken.",
            steps = listOf(
                "Fersensitz, Knie leicht geöffnet.",
                "Oberkörper nach vorn auf die Oberschenkel sinken lassen, Stirn ablegen.",
                "Arme lang nach vorn ausstrecken und tief in den Rücken atmen.",
            ),
            tip = "Bei Kniebeschwerden ein Kissen zwischen Gesäß und Fersen legen.",
            category = STRETCH, mode = TIMED, seconds = 45,
            builtIn = true,
        ),
        Exercise(
            id = "knees_to_chest",
            name = "Knie zur Brust",
            description = "Sanfte Dehnung für den unteren Rücken.",
            steps = listOf(
                "Rückenlage, beide Knie anziehen.",
                "Mit den Händen die Knie sanft Richtung Brust ziehen.",
                "Locker lassen und ruhig atmen – leichtes Schaukeln ist erlaubt.",
            ),
            category = STRETCH, mode = TIMED, seconds = 30,
            builtIn = true,
        ),
        Exercise(
            id = "hip_flexor",
            name = "Hüftbeuger-Dehnung",
            description = "Verkürzte Hüftbeuger ziehen am unteren Rücken – diese Dehnung schafft Entlastung.",
            steps = listOf(
                "Halbkniestand: ein Knie am Boden, der andere Fuß vorne aufgestellt.",
                "Po anspannen und das Becken sanft nach vorn schieben.",
                "Oberkörper bleibt aufrecht, Dehnung vorne an der Hüfte spüren.",
            ),
            tip = "Ein Kissen unter dem Knie macht es bequemer.",
            category = STRETCH, mode = TIMED, seconds = 30, perSide = true,
            builtIn = true,
        ),
        Exercise(
            id = "figure_four",
            name = "Gesäß-Dehnung",
            description = "Dehnt den Piriformis und entlastet Gesäß und unteren Rücken.",
            steps = listOf(
                "Rückenlage, beide Füße aufgestellt.",
                "Einen Knöchel auf das gegenüberliegende Knie legen.",
                "Das untere Bein mit beiden Händen zur Brust ziehen.",
            ),
            category = STRETCH, mode = TIMED, seconds = 30, perSide = true,
            builtIn = true,
        ),
        Exercise(
            id = "hamstring_strap",
            name = "Beinrückseiten-Dehnung",
            description = "Lockert die hintere Oberschenkelmuskulatur – das entlastet das Becken.",
            steps = listOf(
                "Rückenlage, ein Bein angestellt.",
                "Das andere Bein gestreckt anheben, ein Handtuch um den Fuß legen.",
                "Bein sanft zu dir ziehen, bis du eine Dehnung spürst.",
            ),
            category = STRETCH, mode = TIMED, seconds = 30, perSide = true,
            builtIn = true,
        ),
        Exercise(
            id = "sphinx",
            name = "Sphinx",
            description = "Sanfte Streckung der Wirbelsäule in Bauchlage.",
            steps = listOf(
                "Bauchlage, Unterarme unter den Schultern aufstellen.",
                "Brust anheben, das Becken bleibt am Boden.",
                "Schultern weg von den Ohren, ruhig atmen.",
            ),
            tip = "Nur so weit aufrichten, wie es sich gut anfühlt.",
            category = STRETCH, mode = TIMED, seconds = 30,
            builtIn = true,
        ),
        Exercise(
            id = "belly_breathing",
            name = "Bauchatmung",
            description = "Entspannt die Rumpfmuskulatur und baut Stress ab.",
            steps = listOf(
                "Rückenlage, Beine angestellt, Hände auf den Bauch legen.",
                "Durch die Nase tief in den Bauch einatmen – die Hände heben sich.",
                "Langsam durch den Mund ausatmen.",
            ),
            category = RELAX, mode = TIMED, seconds = 60,
            builtIn = true,
        ),
        Exercise(
            id = "chin_tuck",
            name = "Kinn-Retraktion",
            description = "Richtet den Kopf auf und entlastet den Nacken.",
            steps = listOf(
                "Aufrecht sitzen oder stehen, Blick geradeaus.",
                "Den Kopf waagerecht nach hinten schieben („Doppelkinn“).",
                "Drei Sekunden halten und lösen.",
            ),
            category = MOBILITY, mode = REPS, reps = 10,
            builtIn = true,
        ),
        Exercise(
            id = "shoulder_squeeze",
            name = "Schulterblätter zusammenziehen",
            description = "Aktiviert den oberen Rücken – perfekt für zwischendurch.",
            steps = listOf(
                "Aufrecht sitzen, Arme locker.",
                "Schulterblätter nach hinten unten zusammenziehen.",
                "Zwei Sekunden halten, lösen.",
            ),
            category = STRENGTH, mode = REPS, reps = 12,
            builtIn = true,
        ),
        Exercise(
            id = "seated_twist",
            name = "Sitzende Drehung",
            description = "Mobilisiert die Wirbelsäule direkt am Schreibtisch.",
            steps = listOf(
                "Aufrecht auf die vordere Stuhlkante setzen.",
                "Oberkörper zur Seite drehen, eine Hand an die Stuhllehne.",
                "Mit jeder Ausatmung ein kleines Stück weiter drehen.",
            ),
            category = MOBILITY, mode = TIMED, seconds = 20, perSide = true,
            builtIn = true,
        ),
        Exercise(
            id = "side_bend",
            name = "Seitneige im Stehen",
            description = "Dehnt die seitliche Rumpfmuskulatur.",
            steps = listOf(
                "Hüftbreit stehen, einen Arm über den Kopf strecken.",
                "Oberkörper sanft zur Gegenseite neigen.",
                "Nicht nach vorn oder hinten kippen.",
            ),
            category = STRETCH, mode = TIMED, seconds = 20, perSide = true,
            builtIn = true,
        ),
    )

    private val byId = all.associateBy { it.id }

    fun find(id: String): Exercise? = byId[id]
}
