# Release derlemesinde R8 kullanılmayan kodu siler ve isimleri kısaltır.
# Aşağıdaki kurallar çalışma anında yansıma (reflection) ile erişilen sınıfları korur.

# --- Gson ---
# Gson, JSON alanlarını DTO sınıflarına yansıma ile doldurur. Alan adları
# kısaltılırsa eşleştirme bozulur ve bütün alanlar null gelir.
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.kampplus.havali.data.remote.dto.** { *; }
-keep class com.kampplus.havali.data.local.dto.** { *; }

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# --- Retrofit ---
-keep,allowobfuscation interface com.kampplus.havali.data.remote.api.** { *; }
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
  @retrofit2.http.* <methods>;
}
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
