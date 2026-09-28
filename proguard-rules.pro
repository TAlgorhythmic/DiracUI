-dontobfuscate
-allowaccessmodification
-keepattributes SourceFile,LineNumberTable

# Parcelables and AIDL stubs must match the Dirac service's class names and wire format
-keep class se.dirac.acs.api.** { *; }
