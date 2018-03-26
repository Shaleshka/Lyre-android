package by.storksoft.VKontakte;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Converts encrypted string from m.vk.com/audio into actual link to mp3 file.
 * Don't try to understand this shit below. If you want to rewrite this in different
 * language, you can try to google equivalents of some of Java methods used there.
 */

class audioLinkDecrypter {

    private static String key = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMN0PQRSTUVWXYZO123456789+/=";

    private static String fromCharCode(int... codePoints) {
        return new String(codePoints, 0, codePoints.length);
    }

    private static String od(String e) {
        if (e.equals("") || e.length() % 4 == 1) return "";
        int t = 0, i, a = 0;
        String r = "";
        for (int o = 0; o < e.length(); o++) {
            i = e.charAt(o);
            i = key.indexOf(i);
            t = (a % 4 != 0) ? 64 * t + i : i;
            a++;
            if (a % 4 == 1) continue;
            r += fromCharCode(255 & t >> (-2 * a & 6));
        }
        return r;
    }

    private static String v(String e) {
        String[] ar = e.split("");
        String r = "";
        for (String s : ar) r = s + r;
        return r;
    }

    private static String r(String e, int t) {
        String[] ar = e.split("");
        int i;
        String a = key + key;
        for (int o = ar.length - 1; o >= 0; o--) {
            i = a.indexOf(ar[o]);
            if (i == -1) continue;
            int l = i - t;
            if (l < 0) l = a.length() + l;
            ar[o] = a.substring(l, l + 1);
        }
        String s = "";
        for (String is : ar) s += is;
        return s;
    }

    private static String x(String e, char t) {
        String n = "";
        int tt = Character.codePointAt(new char[]{t}, 0);
        String[] ar = e.split("");
        for (String s : ar) {
            n += fromCharCode(Character.codePointAt(s, 0) ^ tt);
        }
        return n;
    }

    private static int[] s2(String e, int t) {
        int n = e.length();
        int[] i = new int[n + 1];
        if (n!=0) {
            int a = n-1;
            for (t = Math.abs(t); a >= 0; a--) {
                t = (n * (a + 1) ^ t + a) % n;
                i[a] = t;
            }
        }
        Log.d("array", Arrays.toString(i));
        return i;
    }

    private static String s(String e, int t) {
        int n = e.length();
        int[] i = s2(e, t);
        int a = 0;
        //idk why split("") adds void element in the beginnin, this is hotfix:
        String[] ar = Arrays.copyOfRange(e.split(""),1,e.split("").length);
        //String[] ar = e.split("");
        for (; ++a < n; ) {
            String temp = ar[i[n - 1 - a]];
            ar[i[n - 1 - a]] = ar[a];
            ar[a] = temp;
        }
        StringBuilder s = new StringBuilder();
        for (String is : ar) s.append(is);
        return s.toString();
    }

    static String getURLfromCode(String e, int id) {
        if (e.contains("audio_api_unavailable")) {
            String[] tt = e.substring(e.indexOf("?extra=") + 7).split("#");
            String n = od(tt[1]);
            String t = od(tt[0]);
            if (t.equals("") || n.equals("")) return e;
            String[] an = n.split(fromCharCode(9));
            String[] r;
            for (int s = an.length - 1; s >= 0; s--) {
                r = an[s].split(fromCharCode(11));
                Log.d("decrypt", Arrays.toString(r));
                if (r[0].equals("x")) t = x(t, r[1].charAt(0));
                if (r[0].equals("r")) t = r(t, Integer.valueOf(r[1]));
                if (r[0].equals("v")) t = v(t);
                if (r[0].equals("s")) t = s(t, Integer.valueOf(r[1]));
                int param = Integer.parseInt(r[1]) ^ id;
                Log.d("params", t+", "+param);
                if (r[0].equals("i")) t = s(t, param);
                Log.d("params", t);

            }
            Log.d("decrypt", t);
            if (t.substring(0, 4).equals("http")) return t;
        } else return e; //vk sometimes give's actual links
        return null;
    }
}