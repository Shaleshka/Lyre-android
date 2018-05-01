package by.storksoft.vkontakte;

/**
 * Stores info about song.
 */

public class AudioItem {
    private String artist, Name, mp3URL, id;
    private int duration;
    private boolean cached;

    AudioItem(String artist, String name, String url, int duration, String id) {
        this.artist = artist;
        Name = name;
        mp3URL = url;
        this.duration = duration;
        this.id = id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
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
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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
        return getDurationAsString(duration);
    }

    public String getId() {
        return id;
    }
}
