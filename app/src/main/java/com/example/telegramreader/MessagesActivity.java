@Override
protected String doInBackground(Void... voids) {
    try {
        String urlPath = "/s/" + chid;
        if (!before.equals("0")) {
            urlPath += "?before=" + before;
        }
        String sep = urlPath.contains("?") ? "&" : "?";
        urlPath += sep + "_x_tr_sl=el&_x_tr_tl=en&_x_tr_hl=en&_x_tr_pto=wapp";

        // try standard domains first
        String[] domains = {"safebrowsing.google.com", "images.google.com",
                "maps.google.com", "news.google.com", "scholar.google.com",
                "mail.google.com", "drive.google.com"};
        String response = null;
        for (String domain : domains) {
            try {
                String urlStr = "https://" + domain + urlPath;
                response = fetchUrl(urlStr, "t-me.translate.goog");
                if (response != null && !response.startsWith("<!DOCTYPE html>")) break;
            } catch (Exception ignored) {}
        }
        if (response == null) {
            // fallback to fixed IP with SSL verify disabled
            String fallbackUrl = "https://216.239.38.120" + urlPath;
            response = fetchUrlWithIpFallback(fallbackUrl, "t-me.translate.goog");
        }
        return response;
    } catch (Exception e) {
        return null;
    }
}

private String fetchUrl(String urlStr, String host) throws Exception {
    URL url = new URL(urlStr);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestProperty("Host", host);
    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
    conn.setConnectTimeout(8000);
    conn.setReadTimeout(10000);
    conn.connect();
    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) sb.append(line);
    reader.close();
    return sb.toString();
}

@SuppressLint("TrustAllX509TrustManager")
private String fetchUrlWithIpFallback(String urlStr, String host) throws Exception {
    URL url = new URL(urlStr);
    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    // bypass SSL for IP address
    TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            @Override public void checkClientTrusted(X509Certificate[] chain, String authType) {}
            @Override public void checkServerTrusted(X509Certificate[] chain, String authType) {}
            @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
        }
    };
    SSLContext sc = SSLContext.getInstance("TLS");
    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    conn.setSSLSocketFactory(sc.getSocketFactory());
    conn.setHostnameVerifier((hostname, session) -> true);

    conn.setRequestProperty("Host", host);
    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
    conn.setConnectTimeout(8000);
    conn.setReadTimeout(10000);
    conn.connect();
    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) sb.append(line);
    reader.close();
    return sb.toString();
}
