package by.storksoft.http;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntityHC4;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtilsHC4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.List;

/**
 * This is Android version. For others platforms see other versions.
 * Doesn't work on pure Java either.
 * Used only for VK Apps for now
 * Uses Apache HTTP Client
 * TODO: add other methods
 */
public class HTTPClient {

    private CloseableHttpClient client; //apache client
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.97 Safari/537.36 Vivaldi/1.94.1008.40"; //for user-agent; empty by default
    private static final String DEFAULT_ACCEPT_LANGUAGE = "en-US,en;q=0.5"; //language
    private static final String HEADER_SPLITTER = ": "; //for headers splitting
    private static final boolean HANDLE_REDIRECTS = true; //redirects if response code is 302
    private static final String DEFAULT_ACCEPT = "text/html;q=0.9,image/webp,*/*;q=0.8"; //default accept for html pages

    public HTTPClient() {
        CookieHandler.setDefault(new CookieManager());
        client = HttpClients
                .custom()
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
                                .build())
                .build();
    }

    private static StringBuffer readInputStream(InputStream stream) {
        BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
        StringBuffer result = new StringBuffer();
        String line;
        try {
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public StringBuffer get(String URL, String accept, String... headers) throws IOException {
        HttpGetHC4 request = new HttpGetHC4(URL);
        request.addHeader("Accept", accept);
        request.setHeader("Accept-Language", DEFAULT_ACCEPT_LANGUAGE);
        for (String header : headers) {
            request.addHeader(header.split(HEADER_SPLITTER)[0], header.split(HEADER_SPLITTER)[1]);
        }
        request.addHeader("User-Agent", DEFAULT_USER_AGENT);
        CloseableHttpResponse response = client.execute(request);
        if (HANDLE_REDIRECTS && response.getStatusLine().getStatusCode() == 302) {
            EntityUtilsHC4.consume(response.getEntity());
            return get(response.getHeaders("Location")[0].getValue(), DEFAULT_ACCEPT);
        }
        StringBuffer result = readInputStream(response.getEntity().getContent());
        EntityUtilsHC4.consume(response.getEntity());
        return result;
    }

    public StringBuffer post(String URL, String host, String accept, String referer,
                             boolean keepAlive, List<NameValuePair> list, String... Headers) throws IOException {
        HttpPostHC4 post = new HttpPostHC4(URL);
        post.setHeader("Host", host);
        post.addHeader("Accept", accept);
        post.setHeader("Accept-Language", DEFAULT_ACCEPT_LANGUAGE);
        if (keepAlive) post.setHeader("Connection", "keep-alive");
        post.setHeader("Referer", referer);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addHeader("User-Agent", DEFAULT_USER_AGENT);
        for (String s : Headers) {
            post.addHeader(s.split(HEADER_SPLITTER)[0], s.split(HEADER_SPLITTER)[1]);
        }
        post.setEntity(new UrlEncodedFormEntityHC4(list));
        CloseableHttpResponse response = client.execute(post);
        if (HANDLE_REDIRECTS && response.getStatusLine().getStatusCode() == 302) {
            EntityUtilsHC4.consume(response.getEntity());
            return get(response.getHeaders("Location")[0].getValue(), DEFAULT_ACCEPT);
        }
        StringBuffer result = readInputStream(response.getEntity().getContent());
        EntityUtilsHC4.consume(response.getEntity());
        return result;
    }

    public StringBuffer get(String URL) {
        try {
            return get(URL, DEFAULT_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StringBuffer post(String URL, String Host, List<NameValuePair> list) {
        try {
            return post(URL, Host, DEFAULT_ACCEPT, "https://" + Host, true, list);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
