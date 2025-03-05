package alv.splash.browser;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

public class WebAppInterface {

    private Context mContext;
	private StartWorking startWorking;
	
	// Constructor lama untuk backward compatibility jika diperlukan
    public WebAppInterface(Context context) {
        Log.i("WebAppInterface", "WebAppInterface Initialized");
        this.mContext = context;
    }
	
    // Constructor dengan dua parameter
    public WebAppInterface(Context context, StartWorking startWorking) {
        Log.i("WebAppInterface", "WebAppInterface Initialized, 2 parameter");
        this.mContext = context;
        this.startWorking = startWorking;
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

    private String isTitleEarningAvailable() {
        if (startWorking != null) {
            return startWorking.getTitleEarning();
        } else {
            return "Title Earning StartWorking not available | null";
        }
    }
    public String scriptInjectData = """
            (function() {
                let targetButton = null;
                let mainObserver = null;
                let captchaObserver = null;
                function startObserving() {
                    observeTitle();
                }
                function observeTitle() {
                    console.log('Starting title observer');
                    cleanupObservers();
                   
                    function checkTitle() {
                        const title = document.title;
                        console.log('Current title:', title);
                       
                        if (title && title.includes('""" +
                isTitleEarningAvailable() +
                        """
                            ')) {
                            console.log('Title: """ +
                isTitleEarningAvailable() +
                            """ 
                            detected');
                            AndroidInterface.elementFound('page-title');
                           
                            observeWorkArea();
                        } else {
                            AndroidInterface.elementNotFound('page-title');
                        }
                    }
                  
                    checkTitle();
                  
                    const titleObserver = new MutationObserver(function() {
                        checkTitle();
                    });
                  
                    const titleElement = document.querySelector('title');
                    if (titleElement) {
                        titleObserver.observe(titleElement, { childList: true, characterData: true, subtree: true });
                        console.log('Title observer started');
                    } else {
                        
                        titleObserver.observe(document.querySelector('head'), { childList: true, subtree: true });
                        console.log('Head observer started (waiting for title)');
                    }
                }
               
                function observeWorkArea() {
                    const targetNode = document.querySelector('.work-area-wrap');
                    if (!targetNode) {
                        AndroidInterface.mutationFailure('work-area-wrap', 'Target node not found');
                       
                        setTimeout(observeWorkArea, 1000);
                        return;
                    }
                    console.log('Mutation Observer initialized for work-area-wrap');
                    AndroidInterface.elementFound('work-area-wrap');
                   
                    const config = {
                        childList: true,
                        subtree: true,
                        attributes: true,
                        attributeFilter: ['style']
                    };
                   
                    const callback = function(mutationsList) {
                        console.log('Mutation detected: ', mutationsList.length + ' changes');
                        try {
                            checkAndCaptureElements();
                        } catch (e) {
                            AndroidInterface.mutationFailure('observer-callback', e.message);
                        }
                    };
                   
                    if (mainObserver) {
                        mainObserver.disconnect();
                    }
                   
                    mainObserver = new MutationObserver(callback);
                    mainObserver.observe(targetNode, config);
                   
                    checkAndCaptureElements();
                }
               
                function cleanupObservers() {
                   
                    if (mainObserver) {
                        console.log('Disconnecting main observer');
                        mainObserver.disconnect();
                        mainObserver = null;
                    }
                   
                    if (captchaObserver) {
                        console.log('Disconnecting captcha observer');
                        captchaObserver.disconnect();
                        captchaObserver = null;
                    }
                }
               
                function findSubmitButton() {
                    
                    if (targetButton && document.body.contains(targetButton)) {
                        return targetButton;
                    }
                   
                    const buttons = document.querySelectorAll('button.btn.btn-default');
                   
                    for (const button of buttons) {
                      
                        const span = button.querySelector('span.label');
                        if (span && span.textContent.trim() === 'Submit') {
                            targetButton = button;
                            AndroidInterface.elementFound('submit-button');
                            console.log('Submit button found');
                            return button;
                        }
                    }
                    AndroidInterface.elementNotFound('submit-button');
                    return null;
                }
                function checkAndCaptureElements() {
                    const captcha = document.querySelector('.captcha-image');
                    const input = document.querySelector('.inp-dft');
                    const submitButton = findSubmitButton();
                    if (captcha) {
                        AndroidInterface.elementFound('captcha-image');
                        console.log('Captcha element found');
                        try {
                            const bgImage = window.getComputedStyle(captcha).backgroundImage;
                            if (bgImage) {
                                let fullDataUrl = bgImage;
                                if (bgImage.includes('url(')) {
                                    fullDataUrl = bgImage.replace(/^url\\(['"]?/, '').replace(/['"]?\\)$/, '');
                                }
                                console.log('Captcha dataURL captured');
                                AndroidInterface.onCaptchaCaptured(fullDataUrl);
                                if (captchaObserver) {
                                    captchaObserver.disconnect();
                                }
                                captchaObserver = new MutationObserver(function(mutations) {
                                    const newBg = window.getComputedStyle(captcha).backgroundImage;
                                    if (newBg !== bgImage) {
                                        console.log('Captcha image changed');
                                        let newUrl = newBg;
                                        if (newBg.includes('url(')) {
                                            newUrl = newBg.replace(/^url\\(['"]?/, '').replace(/['"]?\\)$/, '');
                                        }
                                        AndroidInterface.onCaptchaCaptured(newUrl);
                                    }
                                });
                                captchaObserver.observe(captcha, {
                                    attributes: true,
                                    attributeFilter: ['style']
                                });
                            } else {
                                AndroidInterface.mutationFailure('captcha-image', 'Invalid background format');
                            }
                        } catch (e) {
                            AndroidInterface.mutationFailure('captcha-image', e.message);
                        }
                    } else {
                        AndroidInterface.elementNotFound('captcha-image');
                    }
                    if (input) {
                        AndroidInterface.elementFound('inp-dft');
                        console.log('Input element found');
                        try {
                            AndroidInterface.onInputCaptured(input.value);
                            input.removeEventListener('keydown', inputKeydownHandler);
                            input.addEventListener('keydown', inputKeydownHandler);
                        } catch (e) {
                            AndroidInterface.mutationFailure('inp-dft', e.message);
                        }
                    } else {
                        AndroidInterface.elementNotFound('inp-dft');
                    }
                }
                function inputKeydownHandler(e) {
                    if (e.key === 'Enter' || e.keyCode === 13) {
                        console.log('Input label: ', this.value);
                        AndroidInterface.onInputCaptured(this.value.trim());
                        
                        const submitButton = findSubmitButton();
                        if (submitButton) {
                            console.log('Clicking submit button');
                            submitButton.click();
                            AndroidInterface.buttonClicked('submit-button');
                        } else {
                            AndroidInterface.mutationFailure('submit-button', 'Button not found when trying to click');
                        }
                       
                    }
                }
               
                cleanupObservers();
               
                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', function() {
                        console.log('DOM fully loaded');
                        startObserving();
                    });
                } else {
                    startObserving();
                }
            })();
            
            """;

    // Menggunakan Java text block (Java 17) untuk menyimpan JavaScript
    public String scriptInjectDataBackUP2 = """
(function() {
    function startObserving() {
        const targetNode = document.querySelector('.work-area-wrap');
        if (!targetNode) {
            AndroidInterface.mutationFailure('work-area-wrap', 'Target node not found');
            return;
        }
        console.log('Mutation Observer initialized');
        AndroidInterface.elementFound('work-area-wrap');
        
        const config = {
            childList: true,
            subtree: true,
            attributes: true,
            attributeFilter: ['style']
        };
        
        const callback = function(mutationsList) {
            console.log('Mutation detected: ', mutationsList.length + ' changes');
            try {
                checkAndCaptureElements();
            } catch (e) {
                AndroidInterface.mutationFailure('observer-callback', e.message);
            }
        };
        
        const observer = new MutationObserver(callback);
        observer.observe(targetNode, config);
        checkAndCaptureElements();
    }
    
    function checkAndCaptureElements() {
        const captcha = document.querySelector('.captcha-image');
        const input = document.querySelector('.inp-dft');
        
        if (captcha) {
            AndroidInterface.elementFound('captcha-image');
            console.log('Captcha element found');
            try {
                const bgImage = window.getComputedStyle(captcha).backgroundImage;
                if (bgImage) {
                    let fullDataUrl = bgImage;
                    if (bgImage.includes('url(')) {
                        fullDataUrl = bgImage.replace(/^url\\(['"]?/, '').replace(/['"]?\\)$/, '');
                    }
                    console.log('Captcha dataURL captured');
                    AndroidInterface.onCaptchaCaptured(fullDataUrl);
                    
                    new MutationObserver(function(mutations) {
                        const newBg = window.getComputedStyle(captcha).backgroundImage;
                        if (newBg !== bgImage) {
                            console.log('Captcha image changed');
                            let newUrl = newBg;
                            if (newBg.includes('url(')) {
                                newUrl = newBg.replace(/^url\\(['"]?/, '').replace(/['"]?\\)$/, '');
                            }
                            AndroidInterface.onCaptchaCaptured(newUrl);
                        }
                    }).observe(captcha, {
                        attributes: true,
                        attributeFilter: ['style']
                    });
                } else {
                    AndroidInterface.mutationFailure('captcha-image', 'Invalid background format');
                }
            } catch (e) {
                AndroidInterface.mutationFailure('captcha-image', e.message);
            }
        } else {
            AndroidInterface.elementNotFound('captcha-image');
        }
        
        if (input) {
            AndroidInterface.elementFound('inp-dft');
            console.log('Input element found');
            try {
                AndroidInterface.onInputCaptured(input.value);
                input.addEventListener('keydown', function(e) {
                    if (e.key === 'Enter' || e.keyCode === 13) {
                        console.log('Input label: ', input.value);
                        AndroidInterface.onInputCaptured(input.value.trim());
                        e.preventDefault();
                    }
                });
            } catch (e) {
                AndroidInterface.mutationFailure('inp-dft', e.message);
            }
        } else {
            AndroidInterface.elementNotFound('inp-dft');
        }
    }
    
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            console.log('DOM fully loaded');
            startObserving();
        });
    } else {
        startObserving();
    }
})();
""";

    public String scriptInjectDataBackup = "(function() {" +
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
    "    input.addEventListener('keydown', function(e) {" +
    "        if (e.key === 'Enter' || e.keyCode === 13) {" +
    "            console.log('Input label: ', input.value);" +
    "            AndroidInterface.onInputCaptured(input.value.trim());" +
    "             }" +
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

    public String scriptInjectDataSimple = "(function() {" +
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
            "            inputLabel = input.value.trim(); " +
            "            if (inputLabel !== '') {" +
            "            AndroidInterface.onInputCaptured(inputLabel);" +
            "            input.addEventListener('input', function(e) {" +
            "              newLabel = e.target.value.trim(); "+
            "              if () {}"+
            "            });" +
            "            }" +
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