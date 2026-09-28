# Proguard rules for J.A.R.K. To-Do
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
