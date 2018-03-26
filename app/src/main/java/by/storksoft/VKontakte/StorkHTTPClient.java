package by.storksoft.VKontakte;

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
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.List;

/**
 * This is Android version. For others platforms see other versions.
 * Doesn't work on pure Java either.
 * Used only for VK Apps for now
 * Uses Apache HTTP Client
 */

public class StorkHTTPClient {

    private CloseableHttpClient client; //apache client
    private String UserAgent="Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.97 Safari/537.36 Vivaldi/1.94.1008.40"; //for user-agent; empty by default
    private String AcceptLanguage="en-US,en;q=0.5"; //language
    private final String splitter=": "; //for headers splitting
    private boolean HandleRedirects=true; //redirects if response code is 302
    private final String DefaulAccept="text/html;q=0.9," +
            "image/webp,*/*;q=0.8"; //default accept for html pages

    public StorkHTTPClient() {
        CookieHandler.setDefault(new CookieManager());
        client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build())
                .build();
    }

    public StringBuffer Get(String URL, String Accept, String... Headers) throws IOException {
        HttpGetHC4 request = new HttpGetHC4(URL);
        request.addHeader("Accept",Accept);
        request.setHeader("Accept-Language", AcceptLanguage);
        for (String s: Headers) {
            request.addHeader(s.split(splitter)[0],s.split(splitter)[1]);
        }
        if (!UserAgent.equals("")) request.addHeader("User-Agent",UserAgent);
        CloseableHttpResponse response = client.execute(request);
        if (HandleRedirects && response.getStatusLine().getStatusCode()==302) {
            EntityUtilsHC4.consume(response.getEntity());
            return Get(response.getHeaders("Location")[0].getValue(), DefaulAccept);
        }
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line;
        try {
            while ((line = rd.readLine()) != null) {
                //if (URL=="https://m.vk.com/edit") System.out.println(line);
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        EntityUtilsHC4.consume(response.getEntity());
        return result;
    }

    public StringBuffer Post(String URL, String Host, String Accept, String Referer, boolean keepAlive, List<NameValuePair> list, String... Headers) throws IOException {
        HttpPostHC4 post = new HttpPostHC4(URL);
        post.setHeader("Host", Host);
        post.addHeader("Accept",Accept);
        post.setHeader("Accept-Language", AcceptLanguage);
        if (keepAlive) post.setHeader("Connection", "keep-alive");
        post.setHeader("Referer", Referer);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        if (!UserAgent.equals("")) post.addHeader("User-Agent",UserAgent);
        for (String s: Headers) {
            post.addHeader(s.split(splitter)[0],s.split(splitter)[1]);
        }
        post.setEntity(new UrlEncodedFormEntityHC4(list));
        CloseableHttpResponse response = client.execute(post);
        if (HandleRedirects && response.getStatusLine().getStatusCode()==302) {
            EntityUtilsHC4.consume(response.getEntity());
            return Get(response.getHeaders("Location")[0].getValue(), DefaulAccept);
        }
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line;
        try {
            while ((line = rd.readLine()) != null) {
                //if (URL=="https://m.vk.com/audio") System.out.println(line);
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        EntityUtilsHC4.consume(response.getEntity());
        return result;
    }

    public StringBuffer DefaultGet_StringRespone(String URL) {
        try {
            return Get(URL,DefaulAccept);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StringBuffer DefaultPost_StringRespone(String URL, String Host, List<NameValuePair> list) {
        try {
            return Post(URL,Host,DefaulAccept,"https://"+Host,true,list);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
