package de.fitplan.app.core.library

import de.fitplan.app.core.model.Approach
import de.fitplan.app.core.model.Exercise
import de.fitplan.app.core.model.ExerciseCategory.ENDURANCE
import de.fitplan.app.core.model.ExerciseCategory.MOBILITY
import de.fitplan.app.core.model.ExerciseCategory.RELAX
import de.fitplan.app.core.model.ExerciseCategory.STRENGTH
import de.fitplan.app.core.model.ExerciseCategory.STRETCH
import de.fitplan.app.core.model.ExerciseMode.REPS
import de.fitplan.app.core.model.ExerciseMode.TIMED

private const val WEIGHTS = "2 Wasserflaschen (0,5–1,5 l) oder Kurzhanteln"

/**
 * Eingebaute Übungen. Aufgenommen sind nur Übungen aus Übungsformen, deren Wirksamkeit bei
 * Rücken- bzw. Nackenschmerzen in Studien belegt ist (siehe [Evidence]). Die IDs sind stabil
 * und werden in Plänen gespeichert.
 */
object ExerciseLibrary {

    val all: List<Exercise> = listOf(
        // region Stabilisation / Motorkontrolle
        Exercise(
            id = "drawing_in",
            name = "Tiefe Bauchmuskeln aktivieren",
            description = "Grundübung der Rumpfstabilisation: Du lernst, den tiefen Bauchmuskel gezielt anzuspannen.",
            steps = listOf(
                "Rückenlage, Beine angestellt, der untere Rücken liegt in seiner natürlichen Krümmung.",
                "Ausatmen und den Bauchnabel sanft Richtung Wirbelsäule ziehen – nur etwa ein Drittel der maximalen Kraft.",
                "Die Spannung 10 Sekunden halten und dabei ruhig weiteratmen, dann lösen.",
            ),
            tip = "Becken und Rücken bewegen sich nicht – es ist eine kleine, feine Spannung.",
            category = STRENGTH, mode = REPS, reps = 8,
            approach = Approach.MOTOR_CONTROL, builtIn = true,
        ),
        Exercise(
            id = "pelvic_tilt",
            name = "Beckenkippen",
            description = "Schult die Kontrolle über Becken und Lendenwirbelsäule.",
            steps = listOf(
                "Rückenlage, Beine angestellt, Füße hüftbreit.",
                "Bauch leicht anspannen und den unteren Rücken sanft in die Unterlage drücken.",
                "Spannung lösen und das Becken zurückrollen lassen.",
            ),
            tip = "Die Bewegung ist klein – das Gesäß bleibt am Boden.",
            category = MOBILITY, mode = REPS, reps = 12,
            approach = Approach.MOTOR_CONTROL, builtIn = true,
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
            approach = Approach.MOTOR_CONTROL, builtIn = true,
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
            approach = Approach.MOTOR_CONTROL, builtIn = true,
        ),
        Exercise(
            id = "mcgill_curl_up",
            name = "Curl-up nach McGill",
            description = "Kräftigt die gerade Bauchmuskulatur, ohne die Lendenwirbelsäule zu beugen.",
            steps = listOf(
                "Rückenlage, ein Bein gestreckt, das andere angestellt.",
                "Hände flach unter den unteren Rücken legen, um dessen natürliche Krümmung zu halten.",
                "Kopf und Schultern als Einheit nur wenige Zentimeter anheben, 8 Sekunden halten.",
                "Ablegen; nach der Hälfte der Wiederholungen das Bein wechseln.",
            ),
            tip = "Kein Kinn zur Brust – der Nacken bleibt lang, die Bewegung ist sehr klein.",
            category = STRENGTH, mode = REPS, reps = 6,
            approach = Approach.MOTOR_CONTROL, builtIn = true,
        ),
        Exercise(
            id = "plank",
            name = "Unterarmstütz",
            description = "Ganzkörperspannung für einen stabilen Rumpf (Plank).",
            steps = listOf(
                "Unterarme unter den Schultern aufstützen, Beine gestreckt.",
                "Der Körper bildet eine gerade Linie von Kopf bis Ferse.",
                "Bauch und Po fest, ruhig weiteratmen.",
            ),
            tip = "Leichtere Variante: Knie am Boden lassen.",
            category = STRENGTH, mode = TIMED, seconds = 30,
            approach = Approach.MOTOR_CONTROL, builtIn = true,
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
            approach = Approach.MOTOR_CONTROL, builtIn = true,
        ),
        // endregion

        // region Pilates
        Exercise(
            id = "pilates_bridge",
            name = "Brücke Wirbel für Wirbel",
            description = "Pilates-Klassiker: mobilisiert die Wirbelsäule und kräftigt Gesäß und Beinrückseite.",
            steps = listOf(
                "Rückenlage, Beine hüftbreit angestellt, Arme neben dem Körper.",
                "Ausatmen, das Becken einrollen und die Wirbelsäule Wirbel für Wirbel vom Boden abheben.",
                "Oben einatmen – Schultern, Hüfte und Knie bilden eine Linie.",
                "Ausatmen und Wirbel für Wirbel wieder ablegen.",
            ),
            tip = "Langsam abrollen – jeder Wirbel einzeln.",
            category = STRENGTH, mode = REPS, reps = 8,
            approach = Approach.PILATES, builtIn = true,
        ),
        Exercise(
            id = "pilates_toe_taps",
            name = "Fersentippen",
            description = "Pilates-Übung für Rumpfkontrolle: Die Beine bewegen sich, der Rücken bleibt ruhig.",
            steps = listOf(
                "Rückenlage, Beine in der Tischposition (Knie über der Hüfte, Unterschenkel waagerecht).",
                "Bauch leicht aktivieren, der untere Rücken bleibt ruhig liegen.",
                "Ein Bein langsam absenken, bis die Ferse den Boden antippt, und zurückführen.",
                "Seite wechseln.",
            ),
            tip = "Nur so tief absenken, wie der Rücken ruhig bleibt.",
            category = STRENGTH, mode = REPS, reps = 8, perSide = true,
            approach = Approach.PILATES, builtIn = true,
        ),
        Exercise(
            id = "pilates_single_leg_stretch",
            name = "Einbeinige Dehnung",
            description = "Pilates-Übung (Single Leg Stretch) für die Bauchmuskulatur und Koordination.",
            steps = listOf(
                "Rückenlage, beide Knie zur Brust; der Kopf bleibt für den Einstieg am Boden.",
                "Ein Bein schräg nach vorn oben strecken, das andere Knie mit den Händen halten.",
                "Im ruhigen Rhythmus die Beine wechseln.",
            ),
            tip = "Je höher das gestreckte Bein, desto leichter. Der untere Rücken bleibt am Boden.",
            category = STRENGTH, mode = REPS, reps = 8, perSide = true,
            approach = Approach.PILATES, builtIn = true,
        ),
        Exercise(
            id = "pilates_leg_circles",
            name = "Beinkreisen",
            description = "Pilates-Übung für ein stabiles Becken bei beweglicher Hüfte.",
            steps = listOf(
                "Rückenlage, ein Bein angestellt, das andere Richtung Decke gestreckt (Knie darf leicht gebeugt sein).",
                "Mit dem gestreckten Bein kleine, kontrollierte Kreise zeichnen – fünfmal in jede Richtung.",
                "Das Becken bleibt ruhig liegen. Dann Seite wechseln.",
            ),
            tip = "Lieber kleine Kreise mit ruhigem Becken als große mit Wackeln.",
            category = MOBILITY, mode = REPS, reps = 10, perSide = true,
            approach = Approach.PILATES, builtIn = true,
        ),
        Exercise(
            id = "pilates_swimming",
            name = "Schwimmen",
            description = "Pilates-Übung in Bauchlage für die gesamte Rückenmuskulatur.",
            steps = listOf(
                "Bauchlage, Stirn auf einem gefalteten Handtuch, Arme lang nach vorn.",
                "Bauch leicht einziehen.",
                "Rechten Arm und linkes Bein wenige Zentimeter anheben, dann im ruhigen Tempo wechseln.",
            ),
            tip = "Blick zum Boden, Nacken lang. Klein bleiben ist wichtiger als hoch kommen.",
            category = STRENGTH, mode = TIMED, seconds = 20,
            approach = Approach.PILATES, builtIn = true,
        ),
        Exercise(
            id = "pilates_clam",
            name = "Muschel",
            description = "Pilates-Übung (Clam) für die seitliche Gesäßmuskulatur – sie stabilisiert das Becken.",
            steps = listOf(
                "Seitlage, Knie gebeugt, Füße und Knie liegen aufeinander.",
                "Füße zusammenlassen und das obere Knie öffnen wie eine Muschel.",
                "Das Becken kippt nicht nach hinten. Langsam schließen.",
            ),
            category = STRENGTH, mode = REPS, reps = 12, perSide = true,
            approach = Approach.PILATES, builtIn = true,
        ),
        Exercise(
            id = "pilates_spine_stretch",
            name = "Abrollen im Sitzen",
            description = "Pilates-Übung (Spine Stretch Forward): mobilisiert die Wirbelsäule in die Beugung.",
            steps = listOf(
                "Aufrecht sitzen, Beine etwas mehr als hüftbreit (Knie dürfen gebeugt sein), Arme nach vorn.",
                "Ausatmen, den Kopf sinken lassen und Wirbel für Wirbel nach vorn abrollen.",
                "Einatmen und von unten nach oben wieder aufrichten.",
            ),
            tip = "Sitzt du nicht bequem aufrecht, setz dich auf ein Kissen.",
            category = MOBILITY, mode = REPS, reps = 6,
            approach = Approach.PILATES, builtIn = true,
        ),
        // endregion

        // region Kräftigung
        Exercise(
            id = "glute_bridge",
            name = "Beckenheben",
            description = "Stärkt Gesäß und unteren Rücken (Glute Bridge).",
            steps = listOf(
                "Rückenlage, Füße hüftbreit aufgestellt, Arme neben dem Körper.",
                "Gesäß anspannen und das Becken anheben, bis Oberschenkel und Oberkörper eine Linie bilden.",
                "Zwei Sekunden halten und langsam ablegen.",
            ),
            tip = "Die Kraft kommt aus dem Po – nicht ins Hohlkreuz drücken.",
            category = STRENGTH, mode = REPS, reps = 12,
            approach = Approach.STRENGTH, builtIn = true,
        ),
        Exercise(
            id = "single_leg_bridge",
            name = "Einbeiniges Beckenheben",
            description = "Steigerung des Beckenhebens für mehr Kraft in Gesäß und Rumpf.",
            steps = listOf(
                "Rückenlage, ein Fuß aufgestellt, das andere Bein gestreckt in Höhe des Oberschenkels.",
                "Über das Standbein das Becken anheben, ohne dass es zur Seite kippt.",
                "Kurz halten, langsam ablegen. Seite wechseln.",
            ),
            category = STRENGTH, mode = REPS, reps = 8, perSide = true,
            approach = Approach.STRENGTH, builtIn = true,
        ),
        Exercise(
            id = "superman",
            name = "Rückenstrecker in Bauchlage",
            description = "Stärkt die Rückenstrecker (Superman light).",
            steps = listOf(
                "Bauchlage, Stirn über dem Boden, Arme nach vorn gestreckt.",
                "Arme und Beine gleichzeitig wenige Zentimeter anheben.",
                "Zwei bis drei Sekunden halten und ablegen.",
            ),
            tip = "Blick bleibt zum Boden, der Nacken lang – nicht überstrecken.",
            category = STRENGTH, mode = REPS, reps = 10,
            approach = Approach.STRENGTH, builtIn = true,
        ),
        Exercise(
            id = "chair_squat",
            name = "Kniebeuge zum Stuhl",
            description = "Kräftigt Beine und Gesäß und übt rückenschonendes Hinsetzen und Aufstehen.",
            steps = listOf(
                "Hüftbreit vor einen Stuhl stellen, Arme nach vorn.",
                "Gesäß nach hinten unten schieben, bis es die Sitzfläche berührt – der Rücken bleibt lang.",
                "Mit Kraft aus den Beinen wieder aufstehen.",
            ),
            tip = "Die Knie zeigen in Richtung der Fußspitzen.",
            category = STRENGTH, mode = REPS, reps = 10,
            approach = Approach.STRENGTH, builtIn = true,
        ),
        Exercise(
            id = "hip_hinge",
            name = "Hüftbeuge",
            description = "Übt das Vorbeugen aus der Hüfte (Hip Hinge) – die Grundlage für rückenfreundliches Heben.",
            steps = listOf(
                "Hüftbreit stehen, Knie leicht gebeugt, Hände an den Hüften.",
                "Gesäß nach hinten schieben und den Oberkörper mit langem Rücken nach vorn neigen.",
                "Po anspannen und wieder aufrichten.",
            ),
            tip = "Stell dir vor, du schließt mit dem Po eine Autotür – der Rücken bleibt gerade.",
            category = STRENGTH, mode = REPS, reps = 10,
            approach = Approach.STRENGTH, builtIn = true,
        ),
        Exercise(
            id = "reverse_lunge",
            name = "Ausfallschritt rückwärts",
            description = "Kräftigt Beine und Gesäß und fordert die Rumpfstabilität.",
            steps = listOf(
                "Aufrecht stehen, einen großen Schritt nach hinten machen.",
                "Das hintere Knie Richtung Boden senken, der Oberkörper bleibt aufrecht.",
                "Über das vordere Bein zurück in den Stand drücken. Seite wechseln.",
            ),
            tip = "Für mehr Sicherheit an einer Stuhllehne festhalten.",
            category = STRENGTH, mode = REPS, reps = 8, perSide = true,
            approach = Approach.STRENGTH, builtIn = true,
        ),
        // endregion

        // region Yoga
        Exercise(
            id = "cat_cow",
            name = "Katze-Kuh",
            description = "Yoga-Bewegung, die die gesamte Wirbelsäule mobilisiert – ideal zum Aufwärmen.",
            steps = listOf(
                "Vierfüßlerstand: Hände unter den Schultern, Knie unter der Hüfte.",
                "Ausatmen: Rücken rund machen wie ein Katzenbuckel, Kinn Richtung Brust.",
                "Einatmen: Brustbein nach vorn schieben, sanft ins leichte Hohlkreuz, Blick schräg nach vorn.",
                "Im eigenen Atemrhythmus fließend wechseln.",
            ),
            tip = "Langsam bewegen – jeder Wirbel darf mitmachen.",
            category = MOBILITY, mode = REPS, reps = 10,
            approach = Approach.YOGA, builtIn = true,
        ),
        Exercise(
            id = "seated_cat_cow",
            name = "Katze-Kuh im Sitzen",
            description = "Die Yoga-Bewegung für den Schreibtisch – ohne Matte.",
            steps = listOf(
                "Auf die vordere Stuhlkante setzen, Hände auf die Knie.",
                "Einatmen: Brust nach vorn oben, leichtes Hohlkreuz.",
                "Ausatmen: Rücken rund machen, Kinn Richtung Brust.",
            ),
            category = MOBILITY, mode = REPS, reps = 8,
            approach = Approach.YOGA, builtIn = true,
        ),
        Exercise(
            id = "childs_pose",
            name = "Kindhaltung",
            description = "Yoga-Haltung, die den gesamten Rücken entspannt und dehnt.",
            steps = listOf(
                "Fersensitz, Knie leicht geöffnet.",
                "Oberkörper nach vorn auf die Oberschenkel sinken lassen, Stirn ablegen.",
                "Arme lang nach vorn ausstrecken und tief in den Rücken atmen.",
            ),
            tip = "Bei Kniebeschwerden ein Kissen zwischen Gesäß und Fersen legen.",
            category = STRETCH, mode = TIMED, seconds = 45,
            approach = Approach.YOGA, builtIn = true,
        ),
        Exercise(
            id = "lying_twist",
            name = "Liegende Drehung",
            description = "Yoga-Drehung, die Verspannungen im unteren Rücken löst.",
            steps = listOf(
                "Rückenlage, Knie angewinkelt und geschlossen, Arme seitlich ausgebreitet.",
                "Beide Knie langsam zu einer Seite sinken lassen.",
                "Schultern bleiben am Boden, ruhig weiteratmen.",
            ),
            tip = "Nur so weit, wie es angenehm ist.",
            category = STRETCH, mode = TIMED, seconds = 30, perSide = true,
            approach = Approach.YOGA, builtIn = true,
        ),
        Exercise(
            id = "belly_breathing",
            name = "Bauchatmung",
            description = "Atem- und Entspannungsübung, wie sie in den Yoga-Programmen der Studien vorkommt.",
            steps = listOf(
                "Rückenlage, Beine angestellt, Hände auf den Bauch legen.",
                "Durch die Nase tief in den Bauch einatmen – die Hände heben sich.",
                "Langsam durch den Mund ausatmen.",
            ),
            category = RELAX, mode = TIMED, seconds = 60,
            approach = Approach.YOGA, builtIn = true,
        ),
        // endregion

        // region Dehnung
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
            approach = Approach.STRETCHING, builtIn = true,
        ),
        Exercise(
            id = "hip_flexor",
            name = "Hüftbeuger-Dehnung",
            description = "Dehnt die vordere Hüfte, die bei viel Sitzen verkürzen kann.",
            steps = listOf(
                "Halbkniestand: ein Knie am Boden, der andere Fuß vorne aufgestellt.",
                "Po anspannen und das Becken sanft nach vorn schieben.",
                "Oberkörper bleibt aufrecht, Dehnung vorne an der Hüfte spüren.",
            ),
            tip = "Ein Kissen unter dem Knie macht es bequemer.",
            category = STRETCH, mode = TIMED, seconds = 30, perSide = true,
            approach = Approach.STRETCHING, builtIn = true,
        ),
        Exercise(
            id = "figure_four",
            name = "Gesäß-Dehnung",
            description = "Dehnt die tiefe Gesäßmuskulatur (Piriformis).",
            steps = listOf(
                "Rückenlage, beide Füße aufgestellt.",
                "Einen Knöchel auf das gegenüberliegende Knie legen.",
                "Das untere Bein mit beiden Händen Richtung Brust ziehen.",
            ),
            category = STRETCH, mode = TIMED, seconds = 30, perSide = true,
            approach = Approach.STRETCHING, builtIn = true,
        ),
        Exercise(
            id = "hamstring_strap",
            name = "Beinrückseiten-Dehnung",
            description = "Lockert die hintere Oberschenkelmuskulatur.",
            steps = listOf(
                "Rückenlage, ein Bein angestellt.",
                "Das andere Bein gestreckt anheben, ein Handtuch um den Fuß legen.",
                "Bein sanft zu dir ziehen, bis du eine Dehnung spürst.",
            ),
            category = STRETCH, mode = TIMED, seconds = 30, perSide = true,
            approach = Approach.STRETCHING, builtIn = true, equipment = "Handtuch oder Gurt",
        ),
        // endregion

        // region McKenzie
        Exercise(
            id = "sphinx",
            name = "Sphinx",
            description = "Sanfte Streckung in Bauchlage – die Vorstufe der McKenzie-Streckung.",
            steps = listOf(
                "Bauchlage, Unterarme unter den Schultern aufstellen.",
                "Brust anheben, das Becken bleibt am Boden.",
                "Schultern weg von den Ohren, Rücken locker lassen und ruhig atmen.",
            ),
            tip = "Nehmen Schmerzen zu oder strahlen ins Bein aus: abbrechen.",
            category = STRETCH, mode = TIMED, seconds = 30,
            approach = Approach.MCKENZIE, builtIn = true,
        ),
        Exercise(
            id = "prone_press_up",
            name = "Streckung in Bauchlage",
            description = "Die klassische McKenzie-Übung: wiederholte Streckung der Lendenwirbelsäule.",
            steps = listOf(
                "Bauchlage, Hände unter den Schultern.",
                "Den Oberkörper mit den Armen hochdrücken, das Becken bleibt am Boden, der Rücken locker.",
                "Oben kurz ausatmen und wieder ablegen.",
            ),
            tip = "Gut ist, wenn sich Beschwerden zur Rückenmitte hin zurückziehen. Werden sie stärker oder strahlen ins Bein: abbrechen.",
            category = MOBILITY, mode = REPS, reps = 10,
            approach = Approach.MCKENZIE, builtIn = true,
        ),
        Exercise(
            id = "standing_extension",
            name = "Rückbeuge im Stehen",
            description = "McKenzie-Streckung für zwischendurch – gut nach langem Sitzen.",
            steps = listOf(
                "Hüftbreit stehen, Hände ins Kreuz legen, Finger zeigen nach unten.",
                "Den Oberkörper sanft nach hinten beugen, die Knie bleiben gestreckt.",
                "Kurz halten und zurück in den aufrechten Stand.",
            ),
            tip = "Werden Schmerzen stärker oder strahlen ins Bein aus: abbrechen.",
            category = MOBILITY, mode = REPS, reps = 8,
            approach = Approach.MCKENZIE, builtIn = true,
        ),
        // endregion

        // region Gehen
        Exercise(
            id = "brisk_walk",
            name = "Zügiges Gehen",
            description = "Ausdauer für den Rücken – regelmäßiges Gehen verzögerte in der WalkBack-Studie Rückfälle deutlich.",
            steps = listOf(
                "Geh in einem Tempo, bei dem du noch sprechen, aber nicht mehr singen kannst.",
                "Aufrecht gehen, die Arme schwingen locker mit.",
                "Schrittweise steigern – Ziel sind 30 Minuten an fünf Tagen pro Woche.",
            ),
            tip = "Draußen, auf dem Laufband oder als Umweg zur Arbeit – Hauptsache regelmäßig.",
            category = ENDURANCE, mode = TIMED, seconds = 30 * 60,
            approach = Approach.WALKING, builtIn = true,
        ),
        // endregion

        // region Nacken & Schultern
        Exercise(
            id = "chin_tuck",
            name = "Kinn-Retraktion",
            description = "Kräftigt die tiefen Halsbeuger und richtet den Kopf auf.",
            steps = listOf(
                "Aufrecht sitzen oder stehen, Blick geradeaus.",
                "Den Kopf waagerecht nach hinten schieben („Doppelkinn“).",
                "Drei Sekunden halten und lösen.",
            ),
            category = STRENGTH, mode = REPS, reps = 10,
            approach = Approach.NECK, builtIn = true,
        ),
        Exercise(
            id = "shoulder_squeeze",
            name = "Schulterblätter zusammenziehen",
            description = "Aktiviert die Muskulatur zwischen den Schulterblättern – perfekt für zwischendurch.",
            steps = listOf(
                "Aufrecht sitzen, Arme locker.",
                "Schulterblätter nach hinten unten zusammenziehen.",
                "Zwei Sekunden halten, lösen.",
            ),
            category = STRENGTH, mode = REPS, reps = 12,
            approach = Approach.NECK, builtIn = true,
        ),
        Exercise(
            id = "prone_w",
            name = "W-Heben in Bauchlage",
            description = "Kräftigt die Muskulatur zwischen den Schulterblättern.",
            steps = listOf(
                "Bauchlage, Arme zu einem „W“ angewinkelt, Daumen zeigen nach oben.",
                "Schulterblätter zusammen- und nach unten ziehen, Arme leicht anheben.",
                "Kurz halten und ablegen.",
            ),
            tip = "Schultern weg von den Ohren.",
            category = STRENGTH, mode = REPS, reps = 10,
            approach = Approach.NECK, builtIn = true,
        ),
        Exercise(
            id = "shrugs",
            name = "Schulterheben",
            description = "Kräftigt den oberen Trapezmuskel – Teil des Programms aus der Andersen-Studie.",
            steps = listOf(
                "Aufrecht stehen, in jeder Hand ein Gewicht, die Arme hängen locker.",
                "Die Schultern gerade nach oben Richtung Ohren ziehen.",
                "Kurz halten und langsam senken.",
            ),
            tip = "Gewicht so wählen, dass die letzten Wiederholungen anstrengend, aber sauber sind.",
            category = STRENGTH, mode = REPS, reps = 10,
            approach = Approach.NECK, builtIn = true, equipment = WEIGHTS,
        ),
        Exercise(
            id = "one_arm_row",
            name = "Einarmiges Rudern",
            description = "Kräftigt oberen Rücken und Schulterblattmuskulatur.",
            steps = listOf(
                "Eine Hand und ein Knie auf einem Stuhl oder einer Bank abstützen, der Rücken ist gerade.",
                "Das Gewicht nah am Körper Richtung Hüfte ziehen, das Schulterblatt nach hinten führen.",
                "Langsam ablassen. Seite wechseln.",
            ),
            category = STRENGTH, mode = REPS, reps = 10, perSide = true,
            approach = Approach.NECK, builtIn = true, equipment = WEIGHTS,
        ),
        Exercise(
            id = "reverse_fly",
            name = "Reverse Flys",
            description = "Kräftigt die hintere Schulter und den oberen Rücken.",
            steps = listOf(
                "Mit geradem Rücken leicht nach vorn beugen oder vorgebeugt auf die Stuhlkante setzen.",
                "Die Arme mit leicht gebeugten Ellbogen seitlich bis auf Schulterhöhe anheben.",
                "Schulterblätter zusammenführen, dann langsam senken.",
            ),
            category = STRENGTH, mode = REPS, reps = 10,
            approach = Approach.NECK, builtIn = true, equipment = WEIGHTS,
        ),
        Exercise(
            id = "lateral_raise",
            name = "Seitheben",
            description = "Kräftigt die Schultermuskulatur.",
            steps = listOf(
                "Aufrecht stehen, Gewichte seitlich am Körper.",
                "Die Arme mit leicht gebeugten Ellbogen seitlich bis auf Schulterhöhe heben.",
                "Langsam senken.",
            ),
            tip = "Schultern bleiben unten – nicht zu den Ohren ziehen.",
            category = STRENGTH, mode = REPS, reps = 10,
            approach = Approach.NECK, builtIn = true, equipment = WEIGHTS,
        ),
        // endregion
    )

    private val byId = all.associateBy { it.id }

    fun find(id: String): Exercise? = byId[id]
}
