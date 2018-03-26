package by.storksoft.VKontakte;

/**
 * Stores info about song.
 */

public class audioItem {
    private String Artist, Name, mp3URL, id;
    private int Duration;
    private boolean cached;

    audioItem(String artist, String name, String url, int duration, String id) {
        Artist = artist;
        Name = name;
        mp3URL = url;
        Duration = duration;
        this.id = id;
    }

    public void play() {
        //there should be android code
    }

    public String getArtist() {
        return Artist;
    }

    public void setArtist(String artist) {
        Artist = artist;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getMp3URL() {
        return mp3URL;
    }

    public void setMp3URL(String mp3URL) {
        this.mp3URL = mp3URL;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int duration) {
        Duration = duration;
    }

    public static String getDurationAsString(int Duration) {
        String hours, mins, secs;
        if (Duration>3600) hours= Integer.toString(Duration/3600)+":";
        else hours="";
        mins= Integer.toString((Duration % 3600)/60);
        secs= Integer.toString(Duration % 60);
        if (hours.length()==1) hours="0"+hours;
        if (mins.length()==1) mins="0"+mins;
        if (secs.length()==1) secs="0"+secs;
        return hours+mins+":"+secs;
    }

    public String getDurationAsString() {
        return getDurationAsString(Duration);
    }

    public String getId() {
        return id;
    }
}
