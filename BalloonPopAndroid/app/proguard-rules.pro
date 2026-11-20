# Keep the JavascriptInterface methods callable from the WebView.
-keepclassmembers class com.midnighttales.balloonpop.MainActivity$AdsJsInterface {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
