package alv.splash.browser;

import android.net.Uri;

public class UrlValidator {

    public String processInput(String input) {
        // Hapus spasi di awal dan akhir input
        String query = input.trim();

        // Cek apakah input kosong
        if (query.isEmpty()) {
            return "https://www.google.com";
        }

        // Cek apakah dimulai dengan http:// atau https://
        boolean startsWithHttp = query.toLowerCase().startsWith("http://");
        boolean startsWithHttps = query.toLowerCase().startsWith("https://");
        boolean hasProtocol = startsWithHttp || startsWithHttps;

        // Cek apakah mengandung titik (domain)
        boolean hasDomain = query.contains(".");

        if (hasProtocol) {
            // Jika dimulai dengan protocol, cek apakah memiliki domain
            if (hasDomain) {
                return query; // Gunakan URL sebagaimana adanya
            } else {
                // Jika tidak ada domain, perlakukan sebagai search query
                return "https://www.google.com/search?q=" + Uri.encode(query);
            }
        } else if (hasDomain) {
            // Jika memiliki domain tapi tidak ada protocol, tambahkan https://
            return "https://" + query;
        } else {
            // Jika tidak ada protocol dan domain, perlakukan sebagai search query
            return "https://www.google.com/search?q=" + Uri.encode(query);
        }
    }
}
