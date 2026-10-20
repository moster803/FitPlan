# kotlinx.serialization bringt eigene R8-Regeln mit. Die Datenmodelle werden zusätzlich
# behalten, damit gespeicherte JSON-Daten und Backups lesbar bleiben.
-keep,includedescriptorclasses class de.fitplan.app.core.model.**$$serializer { *; }
-keepclassmembers class de.fitplan.app.core.model.** {
    *** Companion;
}
-keepclasseswithmembers class de.fitplan.app.core.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
