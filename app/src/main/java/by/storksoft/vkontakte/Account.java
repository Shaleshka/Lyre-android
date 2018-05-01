package by.storksoft.vkontakte;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import by.storksoft.http.HTTPClient;
import by.storksoft.vkontakte.utils.AudioLinksDecipher;

import static by.storksoft.vkontakte.utils.ParsingUtils.extractAccountId;
import static by.storksoft.vkontakte.utils.ParsingUtils.extractAuthorizationUrl;
import static by.storksoft.vkontakte.utils.ParsingUtils.extractNextAudioArtist;
import static by.storksoft.vkontakte.utils.ParsingUtils.extractNextAudioEncodedUrl;
import static by.storksoft.vkontakte.utils.ParsingUtils.extractNextAudioId;
import static by.storksoft.vkontakte.utils.ParsingUtils.extractNextAudioTitle;
import static by.storksoft.vkontakte.utils.ParsingUtils.extractNextAuidoDuration;

/**
 * Unites all methods related to VK account management. Represents
 * an account, stores its parameters and audios and video objects.
 * TODO: enrich with other parameters (first name, pictue, etc.)
 */
public class Account {

    private final ArrayList<AudioItem> audios;
    private final HTTPClient httpClient;
    private String login;
    private String password;
    private int accountId;

    public Account() {
        httpClient = new HTTPClient();
        audios = new ArrayList<>();
    }

    /*
    Completes Authorization. If succeeded, calls for methods for getting params.
     */
    public boolean authorize(String login, String password) {
        this.login = login;
        this.password = password;
        StringBuffer response = httpClient.get("https://m.vk.com");

        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("email", login));
        paramList.add(new BasicNameValuePair("pass", password));

        response = httpClient.post(extractAuthorizationUrl(response), "login.vk.com", paramList);

        if (response.indexOf("?act=logout_mobile") != -1) {
            updateAccountParams();
            return true;
        }
        return false;
    }

    /**
     * Loads vk audio pages and parses necessary values
     */
    private void loadAudios() {
        StringBuffer response = httpClient.get("https://m.vk.com/audio");
        String artist, title, url, id;
        int duration;
        int k=0;
        while (response.indexOf("ai_info") != -1) {
            id = extractNextAudioId(response);
            duration = Integer.valueOf(extractNextAuidoDuration(response));
            title = extractNextAudioTitle(response);
            artist = extractNextAudioArtist(response);
            url = AudioLinksDecipher.decipherURL(
                    extractNextAudioEncodedUrl(response), accountId);
            audios.add(new AudioItem(artist, title, url, duration, id));
            if (response.indexOf("ai_info") == -1) {
                k+=50;
                response = httpClient.get("https://m.vk.com/audios?offset=" + k);
            }
        }
    }

    /*
     * Receives account id
     * TODO: receive first name, last name and others fields from vk.com/edit page.
     */
    private void updateAccountParams() {
        StringBuffer sb = httpClient.get("https://m.vk.com/edit");
        accountId = Integer.parseInt(extractAccountId(sb));
        loadAudios();
    }

    public ArrayList<AudioItem> getAudios() {
        return audios;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
