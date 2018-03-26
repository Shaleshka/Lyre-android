package by.storksoft.VKontakte;

import android.text.Html;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Unites all methods related to VK account management. Represents
 * an account, stores its parameters and audio and video objects.
 */
public class Account {
    private String firstName;
    private String lastName;
    private String countryName;
    private String cityName;
    private String accURL;
    private int bDay, bMonth, bYear;
    private String picURL;
    private String login,password;
    private int accId;

    private ArrayList<audioItem> audio;

    private CloseableHttpClient client;

    private StorkHTTPClient httpClient;

    public Account() {
        httpClient = new StorkHTTPClient();
        audio = new ArrayList<>();
    }


    /*
    Completes Authorization. If succeeded, calls for methods for getting params.
     */
    public boolean authorize(String login, String password) {
        this.login = login;
        this.password = password;
        StringBuffer sb = httpClient.DefaultGet_StringRespone("https://m.vk.com");
        int i = sb.indexOf("form method=") + 27, j = sb.indexOf(" novalidate") - 1;
        String postURL = sb.substring(i, j);
        System.out.println(postURL);

        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("email", login));
        paramList.add(new BasicNameValuePair("pass", password));

        sb = httpClient.DefaultPost_StringRespone(postURL, "login.vk.com", paramList);

        if (sb.indexOf("?act=logout_mobile") != -1) {
            this.accURL = sb.substring(sb.indexOf("data-href") + 11, sb.indexOf("data-name") - 2);
            getAccParams();
            return true;
        }
        return false;
    }

    private void loadAudios() {
        StringBuffer sb = httpClient.DefaultGet_StringRespone("https://m.vk.com/audio");
        String artist, name, url, id;
        int duration;
        int k=0;
        Log.d("id", String.valueOf(accId));
        while (sb.indexOf("ai_info")!=-1) {
            sb.delete(0, sb.indexOf("ai_info")+4);
            id = findBetween(sb,"audioplayer.del(\'","\', event);");
            id = findBetween(id,"_","_a");
            sb.delete(0, sb.indexOf("ai_body")+4);
            duration = Integer.valueOf(findBetween(sb,"data-dur=\"","\" onclick"));
            artist = Html.fromHtml(findBetween(sb, "\"ai_artist\">", "</span></div>")).toString();
            name = Html.fromHtml(findBetween(sb,"\"ai_title\">","</span>")).toString();
            url = audioLinkDecrypter.getURLfromCode(findBetween(sb,"type=\"hidden\" value=\"","\"></div></div>"),accId);
            Log.d("lol",id);
            audio.add(new audioItem(artist,name,url,duration,id));
            if (sb.indexOf("ai_info")==-1) {
                k+=50;
                sb = httpClient.DefaultGet_StringRespone("https://m.vk.com/audio?offset="+k);
            }

        }
    }

    /*
    Service function for parsing.
     */
    private String findBetween(StringBuffer sb, String start, String end) {
        int i=sb.indexOf(start)+start.length();
        int j=sb.indexOf(end);
        return sb.substring(i, j);
    }

    private String findBetween(String sb, String start, String end) {
        int i=sb.indexOf(start)+start.length();
        int j=sb.indexOf(end);
        return sb.substring(i, j);
    }

    /*
    Receives first name, last name and others fields from vk.com/edit page.
     */
    private void getAccParams() {
        //StringBuffer sb = httpClient.DefaultGet_StringRespone("https://m.vk.com"+accURL);
        //picURL=findBetween(sb,"img src=\"","\" class=\"pp_img\"");
        //accId= Integer.parseInt(findBetween(sb,"panel al_u","\" ontouchstart"));
        StringBuffer sb = httpClient.DefaultGet_StringRespone("https://m.vk.com/edit");
        accId= Integer.parseInt(findBetween(sb,"\"/album","_0?act"));
        //sb.delete(0,sb.indexOf("first_name"));
        //this.firstName=findBetween(sb,"field\" value=\"","\" /></dd>");
        //sb.delete(0,sb.indexOf("last_name"));
        //this.lastName=findBetween(sb,"field\" value=\"","\" /></dd>");
        loadAudios();
    }

    public ArrayList<audioItem> getAudio() {
        return audio;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getCityName() {
        return cityName;
    }

    public String getAccURL() {
        return accURL;
    }

    public int getbDay() {
        return bDay;
    }

    public int getbMonth() {
        return bMonth;
    }

    public int getbYear() {
        return bYear;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
