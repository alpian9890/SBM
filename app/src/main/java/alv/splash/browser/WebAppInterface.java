package alv.splash.browser;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class WebAppInterface {
    private Context mContext;

    public WebAppInterface(Context context) {
        mContext = context;
    }

    public String scriptInjectData =
            "(function() {" +
                    "function observeElementsByClass(className, callback) {" +
                    "var targets = document.querySelectorAll('.' + className);" +
                    "if (!targets || targets.length === 0) {" +
                    "window.android.elementNotFound(className);" +
                    "return;" +
                    "}" +
                    "window.android.elementFound(className);" +

                    "var backgroundImage = document.querySelector('.captcha-image').style.backgroundImage;" +
                    "if (backgroundImage) {" +
                    "var base64Match2 = backgroundImage.match(/base64,(.*)\"$ /);" +
                    "if (base64Match2) {" +
                    "window.android.getImgBase642(base64Match2[1]);" +
                    "} else {" +
                    "window.android.mutationFailure('.captcha-image', 'No base64 found');" +
                    "}" +

                    "targets.forEach(function(target) {" +
                    "var observer = new MutationObserver(function(mutations) {" +
                    "mutations.forEach(function(mutation) {" +
                    "callback(mutation, target);" +
                    "});" +
                    "});" +
                    "observer.observe(target, { attributes: true, childList: true, subtree: true });" +
                    "});" +
                    "}" +

                    // Observer for captcha-image
                    "observeElementsByClass('captcha-image', function(mutation, target) {" +
                    "var backgroundImage = target.style.backgroundImage;" +
                    "if (backgroundImage) {" +
                    "var base64Match = backgroundImage.match(/base64,(.*)\"$ /);" +
                    "if (base64Match) {" +
                    "window.android.getImgBase64(base64Match[1]);" +
                    "} else {" +
                    "window.android.mutationFailure('captcha-image', 'No base64 found');" +
                    "}" +
                    "} else {" +
                    "window.android.mutationFailure('captcha-image', 'No background image');" +
                    "}" +
                    "});" +

                    // Observer for inp-dft
                    "observeElementsByClass('inp-dft', function(mutation, target) {" +
                    "if (mutation.type === 'attributes' && mutation.attributeName === 'value') {" +
                    "window.android.inputLabel(target.value);" +
                    "} else if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {" +
                    "window.android.inputLabel(target.value);" +
                    "}" +
                    "});" +
                    "})();";

    public String elementFound = null;
    public String elementNotFound = null;
    public String getImgBase64 = null;
    public String getImgBase642 = null;
    public String inputLabel = null;
    public String mutationFailure = null;

    @JavascriptInterface
    public void elementFound(String className) {
        // Handle success: Element with 'className' found
        Log.i("WebView", "Element found: " + className);
        elementFound = "Element found: " + className;
    }

    @JavascriptInterface
    public void elementNotFound(String className) {
        // Handle failure: Element not found
        Log.e("WebView", "Element not found: " + className);
        elementNotFound = "Element not found: " + className;
    }

    @JavascriptInterface
    public void getImgBase64(String base64) {
        // Handle base64 image data
        Log.i("WebView", "Image base64 received: " + base64);
        getImgBase64 = base64;
    }

    @JavascriptInterface
    public void getImgBase642(String base64) {
        // Handle base64 image data
        Log.i("WebView", "Image base64 [2] received: " + base64);
        getImgBase642 = base64;
    }

    @JavascriptInterface
    public void inputLabel(String value) {
        // Handle input value
        Log.i("WebView", "Input value received: " + value);
        inputLabel = value;
    }

    @JavascriptInterface
    public void mutationFailure(String className, String reason) {
        // Handle mutation observer failure
        Log.e("WebView", "Mutation failure in " + className + ": " + reason);
        mutationFailure = className + ": " + reason;
    }
}

