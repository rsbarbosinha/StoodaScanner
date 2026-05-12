# Preserve Aalto XML and StAX classes
-keep class com.fasterxml.aalto.** { *; }
-keep class org.codehaus.stax2.** { *; }
-keep class javax.xml.stream.** { *; }
-keep class org.dhatim.fastexcel.reader.** { *; }

# Allow ServiceLoader to find the implementations if needed
-keepattributes Signature,AnnotationDefault,EnclosingMethod,InnerClasses,SourceFile,LineNumberTable
-optimizations !class/merging/vertical*,!class/merging/horizontal*
