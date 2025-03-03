package alv.splash.browser;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

public class WebAppInterface {

    private Context mContext;

    public WebAppInterface(Context context) {
        Log.i("WebAppInterface", "WebAppInterface Initialized");
        mContext = context;
    }

    public String removeSpacesStringBuilder(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            if (!Character.isWhitespace(input.charAt(i))) {
                result.append(input.charAt(i));
            }
        }
        return result.toString();
    }

    public String scriptInjectData = "(function() {" +
    "function startObserving() {" +
    "    const targetNode = document.querySelector('.work-area-wrap');" +
    "    if (!targetNode) {" +
    "        AndroidInterface.mutationFailure('work-area-wrap', 'Target node not found');" +
    "        return;" +
    "    }" +
    "    console.log('MutationObserver initialized');" +
    "    AndroidInterface.elementFound('work-area-wrap');" +
    "    const config = { " +
    "        childList: true," +
    "        subtree: true," +
    "        attributes: true," +
    "        attributeFilter: ['style']" +
    "    };" +
    "    const callback = function(mutationsList) {" +
    "        console.log('Mutation detected:', mutationsList.length + ' changes');" +
    "        try {" +
    "            checkAndCaptureElements();" +
    "        } catch (e) {" +
    "            AndroidInterface.mutationFailure('observer-callback', e.message);" +
    "        }" +
    "    };" +
    "    const observer = new MutationObserver(callback);" +
    "    observer.observe(targetNode, config);" +
    "    checkAndCaptureElements();" +
    "}" +
    "function checkAndCaptureElements() {" +
    "    const captcha = document.querySelector('.captcha-image');" +
    "    const input = document.querySelector('.inp-dft');" +
    "    if (captcha) {" +
    "        AndroidInterface.elementFound('captcha-image');" +
    "        console.log('Captcha element found');" +
    "        try {" +
    "            const bgImage = window.getComputedStyle(captcha).backgroundImage;" +
    "            if (bgImage) {" +
    "                let fullDataUrl = bgImage;" +
    "                if (bgImage.includes('url(')) {" +
    "                    fullDataUrl = bgImage.replace(/^url\\(['\"]?/, '').replace(/['\"]?\\)$/, '');" +
    "                }" +
    "                console.log('Captcha data URL captured');" +
    "                AndroidInterface.onCaptchaCaptured(fullDataUrl);" +
    "                new MutationObserver(function(mutations) {" +
    "                    const newBg = window.getComputedStyle(captcha).backgroundImage;" +
    "                    if (newBg !== bgImage) {" +
    "                        console.log('Captcha image changed');" +
    "                        let newUrl = newBg;" +
    "                        if (newBg.includes('url(')) {" +
    "                            newUrl = newBg.replace(/^url\\(['\"]?/, '').replace(/['\"]?\\)$/, '');" +
    "                        }" +
    "                        AndroidInterface.onCaptchaCaptured(newUrl);" +
    "                    }" +
    "                }).observe(captcha, { attributes: true, attributeFilter: ['style'] });" +
    "            } else {" +
    "                AndroidInterface.mutationFailure('captcha-image', 'Invalid background format');" +
    "            }" +
    "        } catch (e) {" +
    "            AndroidInterface.mutationFailure('captcha-image', e.message);" +
    "        }" +
    "    } else {" +
    "        AndroidInterface.elementNotFound('captcha-image');" +
    "    }" +
    "    if (input) {" +
    "        AndroidInterface.elementFound('inp-dft');" +
    "        console.log('Input element found');" +
    "        try {" +
    "            AndroidInterface.onInputCaptured(input.value);" +
    "            input.addEventListener('input', function(e) {" +
    "                console.log('Input value changed:', e.target.value);" +
    "                AndroidInterface.onInputCaptured(e.target.value);" +
    "            });" +
    "        } catch (e) {" +
    "            AndroidInterface.mutationFailure('inp-dft', e.message);" +
    "        }" +
    "    } else {" +
    "        AndroidInterface.elementNotFound('inp-dft');" +
    "    }" +
    "}" +
    "if (document.readyState === 'loading') {" +
    "    document.addEventListener('DOMContentLoaded', function() {" +
    "        console.log('DOM fully loaded');" +
    "        startObserving();" +
    "    });" +
    "} else {" +
    "    startObserving();" +
    "}" +
    "})();";

    public String elementFound = "";
    public String elementNotFound = "";
    public String ImgBase64 = "";
    public String ImgLabel = "";
    public String mutationFailure = "";
    
    @JavascriptInterface
    public void elementFound(String className) {
        String msg = "Element found: " + className;
        Log.i("WebAppInterface", msg);
        elementFound = msg;
    }

    @JavascriptInterface
    public void elementNotFound(String className) {
        String msg = "Element not found: " + className;
        Log.w("WebAppInterface", msg);
        elementNotFound = msg;
    }

    @JavascriptInterface
    public void onCaptchaCaptured(String base64) {
        String logMsg = "Captcha data received (" + base64.length() + " chars)";
        Log.d("WebAppInterface", logMsg);
        ImgBase64 = base64;
    }

    @JavascriptInterface
    public void onInputCaptured(String label) {
        String logMsg = "Input received: " + (label.isEmpty() ? "<empty>" : label);
        Log.d("WebAppInterface", logMsg);
        ImgLabel = label;
    }

    @JavascriptInterface
    public void mutationFailure(String className, String reason) {
        String msg = "Mutation error in " + className + ": " + reason;
        Log.e("WebAppInterface", msg);
        mutationFailure = msg;
    }
}