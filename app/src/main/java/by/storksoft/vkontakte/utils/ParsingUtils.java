package by.storksoft.vkontakte.utils;

import android.text.Html;

public class ParsingUtils {

    public static String extractAuthorizationUrl(StringBuffer stringBuffer) {
        int i = stringBuffer.indexOf("form method=") + 27,
                j = stringBuffer.indexOf(" novalidate") - 1;
        return stringBuffer.substring(i, j);
    }

    public static String extractAccountURL(StringBuffer stringBuffer) {
        return stringBuffer.substring(stringBuffer.indexOf("data-href") + 11, stringBuffer.indexOf("data-name") - 2);
    }

    public static String extractNextAudioId(StringBuffer stringBuffer) {
        stringBuffer.delete(0, stringBuffer.indexOf("ai_info") + 4);
        String internalId = findBetween(stringBuffer, "audioplayer.del(\'", "\', event);");
        return findBetween(internalId, "_", "_a");
    }

    public static String extractNextAuidoDuration(StringBuffer stringBuffer) {
        stringBuffer.delete(0, stringBuffer.indexOf("ai_body") + 4);
        return findBetween(stringBuffer, "data-dur=\"", "\" onclick");
    }

    public static String extractNextAudioTitle(StringBuffer stringBuffer) {
        return Html.fromHtml(findBetween(stringBuffer, "\"ai_title\">", "</span>")).toString();
    }

    public static String extractNextAudioArtist(StringBuffer stringBuffer) {
        stringBuffer.delete(0, stringBuffer.indexOf("ai_artist") - 2);
        return Html.fromHtml(findBetween(stringBuffer, "\"ai_artist\">", "</span>")).toString();
    }

    public static String extractNextAudioEncodedUrl(StringBuffer stringBuffer) {
        stringBuffer.delete(0, stringBuffer.indexOf("type") - 2);
        return findBetween(stringBuffer, "type=\"hidden\" value=\"", "\">");
    }

    public static String extractAccountId(StringBuffer stringBuffer) {
        return findBetween(stringBuffer, "\"/album", "_0?act");
    }

    /*
    Service function for parsing.
     */
    private static String findBetween(StringBuffer sb, String start, String end) {
        int i = sb.indexOf(start) + start.length();
        int j = sb.indexOf(end);
        return sb.substring(i, j);
    }

    private static String findBetween(String sb, String start, String end) {
        int i = sb.indexOf(start) + start.length();
        int j = sb.indexOf(end);
        return sb.substring(i, j);
    }
}
