package alv.splash.browser;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class WebAppInterface {

    public String currentImageUrl = "";
    public String currentInputValue = "";
    @JavascriptInterface
    public void updateData(String imageUrl, String inputValue) {
        currentImageUrl = imageUrl;
        currentInputValue = inputValue;
    }

    public void injectMutationObserver(WebView webView) {
        String jsCode = """
            (function() {
                const targetNode = document.body;
                const config = { childList: true, subtree: true };
                
                const callback = function(mutationsList, observer) {
                    const img = document.querySelector('img'); // Sesuaikan selector
                    const input = document.querySelector('input'); // Sesuaikan selector
                    
                    if (img && input) {
                        Android.updateData(img.src, input.value);
                    }
                };
                
                const observer = new MutationObserver(callback);
                observer.observe(targetNode, config);
            })();
            """;
        webView.evaluateJavascript(jsCode, null);
    }

}
