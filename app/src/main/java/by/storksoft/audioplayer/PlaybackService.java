package by.storksoft.audioplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.un4seen.bass.BASS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import by.storksoft.vkontakte.AudioItem;

//TODO: create notification panel for controlling playback in background
public class PlaybackService extends Service {

    private ArrayList<AudioItem> list;
    private int current = -1;
    private boolean active;
    private int channel = 0;
    private FileOutputStream stream;
    private Context context;
    private int size = 0;
    private static Timer timer = new Timer();
    BASS.DOWNLOADPROC DownloadProc = new BASS.DOWNLOADPROC() {
        public void DOWNLOADPROC(ByteBuffer buffer, int length, Object user) {
            if (buffer == null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                stream.getChannel().write(buffer);
                size += length;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private SeekBar seekBar;
    private TextView t1, t2;
    private boolean repeating = false;
    private ImageButton playbutton;
    private boolean playing = false;

    public void setPlaylist(ArrayList<AudioItem> playlist) {
        list.clear();
        for (AudioItem a : playlist) list.add(a);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public PlaybackService(Context c) {
        BASS.BASS_Init(-1, 44100, 0);
        list = new ArrayList<>();
        active = false;
        this.context = c;
    }

    public void playURL(AudioItem song, TextView passed, TextView left, SeekBar seek, ImageButton button) {
        BASS.BASS_StreamFree(channel);
        playbutton = button;
        String URL = song.getMp3URL(), id = song.getId();

        t1 = passed;
        t2 = left;
        seekBar = seek;
        if (fileExistence(id)) {
            File file = new File(context.getFilesDir(), id);
            channel = BASS.BASS_StreamCreateFile(file.getAbsolutePath(), 0, 0, 0);
            timer.scheduleAtFixedRate(new mainTask(), 1000, 1000);
            playing = true;
            button.setImageResource(R.drawable.pause);
        } else {
            try {
                stream = context.openFileOutput(id, Context.MODE_PRIVATE);
                channel = BASS.BASS_StreamCreateURL(URL, 0, 0, DownloadProc, 0);
                timer.scheduleAtFixedRate(new mainTask(), 1000, 1000);
                playing = true;
                button.setImageResource(R.drawable.pause);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
        BASS.BASS_ChannelPlay(channel, repeating);
        active = true;
        current = list.indexOf(song);
    }

    public void resetTimeline(TextView passed, TextView left, SeekBar seek, ImageButton button) {
        t1 = passed;
        t2 = left;
        seekBar = seek;
        playbutton = button;
    }

    public void playPause(ImageButton button) {
        if (channel != 0) {
            if (playing) {
                BASS.BASS_ChannelPause(channel);
                playing = false;
                button.setImageResource(R.drawable.play);
            } else {
                BASS.BASS_ChannelPlay(channel, repeating);
                playing = true;
                button.setImageResource(R.drawable.pause);
            }
        }
    }

    public void setRepeat() {
        repeating = !repeating;
        BASS.BASS_ChannelPause(channel);
        BASS.BASS_ChannelPlay(channel, repeating);
    }

    public void backward() {
        if (current > 0) playURL(list.get(current - 1), t1, t2, seekBar, playbutton);
    }

    public void forward() {
        if (current < list.size() - 1) playURL(list.get(current + 1), t1, t2, seekBar, playbutton);
    }

    public boolean fileExistence(String fname) {
        File file = new File(context.getFilesDir(), fname);
        return file.exists();
    }

    private class mainTask extends TimerTask {
        public void run() {
            if (t1 != null) {
                t1.post(new Runnable() {
                    @Override
                    public void run() {
                        t1.setText(AudioItem.getDurationAsString((int) (BASS.BASS_ChannelBytes2Seconds(channel, BASS.BASS_ChannelGetPosition(channel, 0)))));
                    }
                });
            }
            if (t2 != null) {
                t2.post(new Runnable() {
                    @Override
                    public void run() {
                        t2.setText(AudioItem.getDurationAsString((int) (BASS.BASS_ChannelBytes2Seconds(channel, (BASS.BASS_ChannelGetLength(channel, 0) - BASS.BASS_ChannelGetPosition(channel, 0))))));
                        if (BASS.BASS_ChannelBytes2Seconds(channel, (BASS.BASS_ChannelGetLength(channel, 0) - BASS.BASS_ChannelGetPosition(channel, 0))) < 1)
                            forward();
                    }
                });
            }
            if (seekBar != null) {
                seekBar.post(new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setOnSeekBarChangeListener(null);
                        seekBar.setMax(0);
                        seekBar.setMax((int) (BASS.BASS_ChannelGetLength(channel, 0) / 10000));
                        seekBar.setProgress((int) (BASS.BASS_ChannelGetPosition(channel, 0) / 10000));
                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                BASS.BASS_ChannelSetPosition(channel, i * 10000, 0);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        BASS.BASS_Free();
    }

}
