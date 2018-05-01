package by.storksoft.audioplayer.activities;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import by.storksoft.audioplayer.DataModel;
import by.storksoft.audioplayer.DrawerItemCustomAdapter;
import by.storksoft.audioplayer.PlaybackService;
import by.storksoft.audioplayer.R;
import by.storksoft.audioplayer.fragments.SettingsFragment;
import by.storksoft.vkontakte.Account;
import by.storksoft.vkontakte.AudioItem;

import static android.graphics.Typeface.BOLD;

public class MainActivity extends AppCompatActivity {

    private static final LinearLayout.LayoutParams LAYOUT_PARAMS = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    private static final RelativeLayout.LayoutParams LEFT_LAYOUT_PARAMS = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private static final RelativeLayout.LayoutParams BOTTOM_LAYOUT_PARAMS = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private static final FrameLayout.LayoutParams MATCH_PARENT_LAYOUT_PARAMS = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    private String[] mNavigationDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    public static Account vk = null;
    private CharSequence mTitle;
    public static boolean showLogForm = true;

    private ArrayList<RelativeLayout> list;

    static {
        LEFT_LAYOUT_PARAMS.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        LEFT_LAYOUT_PARAMS.addRule(RelativeLayout.CENTER_VERTICAL);
        BOTTOM_LAYOUT_PARAMS.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    }

    private final PlaybackService service = new PlaybackService(this);
    private SeekBar seekBar;
    private Toolbar toolbar;
    private android.support.v7.app.ActionBarDrawerToggle mDrawerToggle;
    private int current = -1;
    private ArrayList<AudioItem> playlist;
    private TextView timePassed, timeLeft;
    private ImageButton playPauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTitle = getTitle();
        mNavigationDrawerItemTitles = getResources().getStringArray(R.array.navigation_drawer_items_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        setupToolbar();

        DataModel[] drawerItem = new DataModel[3];

        drawerItem[0] = new DataModel(R.drawable.muisc, "My Audios");
        drawerItem[1] = new DataModel(R.drawable.cached, "Cached");
        drawerItem[2] = new DataModel(R.drawable.settings, "Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.list_view_item_row, drawerItem);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        setupDrawerToggle();

        checkLogin();
    }

    /*
     * Check if login already stored in file
     */
    private void checkLogin() {
        String login, password;
        if (showLogForm) {
            try {
                InputStream stream = this.openFileInput("logpas");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder result = new StringBuilder();
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        //if (URL=="https://m.vk.com/audio") System.out.println(line);
                        result.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String loginPasswordString = result.toString();
                login = loginPasswordString.split("!logpas")[0];
                password = loginPasswordString.split("!logpas")[1];
                LoginTask task = new LoginTask(login, password);
                task.execute((Void) null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                //if login not stored
                if (showLogForm) {
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i);
                }
            }
        }
    }

    private void saveLoginToFile() {
        try {
            String s = vk.getLogin() + "!logpas" + vk.getPassword();
            OutputStream stream = this.openFileOutput("logpas", Context.MODE_PRIVATE);
            stream.write(s.getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (vk == null) return;
        list = new ArrayList<>();
        playlist = vk.getAudios();
        selectItem(0);

        saveLoginToFile();

        service.setPlaylist(playlist);
        service.resetTimeline(timePassed, timeLeft, seekBar, playPauseButton);

    }

    private void displayPlaylist(boolean cachedOnly) {
        //top container
        FrameLayout Frame = ((FrameLayout) findViewById(R.id.content_frame));
        if (Frame == null) return;
        Frame.removeAllViews();
        Frame.setLayoutParams(LAYOUT_PARAMS);
        //we need relative to place control with scrollview together
        LinearLayout topContainer = new LinearLayout(this);
        topContainer.setOrientation(LinearLayout.VERTICAL);
        topContainer.setLayoutParams(MATCH_PARENT_LAYOUT_PARAMS);
        //creating playlist container
        final LinearLayout playlistLayout = new LinearLayout(this);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        scrollView.setLayoutParams(scrollParams);
        list.clear();
        //control panel
        LinearLayout controlLayout = new LinearLayout(this);
        controlLayout.setOrientation(LinearLayout.VERTICAL);
        //controlLayout.setLayoutParams(BOTTOM_LAYOUT_PARAMS);
        LinearLayout timeLine = new LinearLayout(this);
        timeLine.setOrientation(LinearLayout.HORIZONTAL);
        timeLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        createTimePassedView();
        createTimeLeftView();
        seekBar = new SeekBar(this);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        timeLine.addView(timePassed);
        timeLine.addView(seekBar);
        timeLine.addView(timeLeft);

        RelativeLayout buttonsTop = new RelativeLayout(this);
        LinearLayout buttons = new LinearLayout(this);
        RelativeLayout.LayoutParams centric = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        centric.addRule(RelativeLayout.CENTER_HORIZONTAL);
        centric.addRule(RelativeLayout.CENTER_IN_PARENT);
        buttons.setLayoutParams(centric);

        playPauseButton = new ImageButton(this);
        playPauseButton.setBackgroundResource(0);
        playPauseButton.setImageResource(R.drawable.play);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                service.playPause((ImageButton) view);
            }
        });

        buttons.addView(createRepeatButton());
        buttons.addView(createBackwardButton());
        buttons.addView(playPauseButton);
        buttons.addView(createForwardButton());
        buttons.addView(createShuffleButton());

        buttonsTop.addView(buttons);
        controlLayout.addView(timeLine);
        controlLayout.addView(buttonsTop);
        //adding
        topContainer.addView(scrollView);
        topContainer.addView(controlLayout);
        Frame.addView(topContainer);
        scrollView.addView(playlistLayout);
        playlistLayout.setOrientation(LinearLayout.VERTICAL);
        playlistLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        list.clear();
        for (AudioItem a : playlist) {
            if (!service.fileExistance(a.getId()) && cachedOnly) continue;
            //container for the song
            RelativeLayout songLayout = new RelativeLayout(this);
            LinearLayout left = new LinearLayout(this);
            left.setOrientation(LinearLayout.HORIZONTAL);
            //if (service.fileExistance(a.getId())) {
            TextView cachedIndicator = new TextView(this);
            cachedIndicator.setTextSize(30);
            if (service.fileExistance(a.getId())) cachedIndicator.setText("|");
            else cachedIndicator.setText(" ");
            //ind.setLayoutParams(rp);
            left.addView(cachedIndicator);
            //}
            //artist and name
            LinearLayout artistNameLayout = new LinearLayout(this);
            artistNameLayout.setOrientation(LinearLayout.VERTICAL);
            TextView t1 = new TextView(this);
            t1.setTextSize(20);
            t1.setTypeface(null, BOLD);
            t1.setText(a.getArtist());
            t1.setTextColor(Color.BLACK);
            TextView t2 = new TextView(this);
            t2.setTextSize(16);
            t2.setText(a.getName());
            t2.setTextColor(Color.BLACK);
            artistNameLayout.addView(t1);
            artistNameLayout.addView(t2);
            //time
            TextView t3 = new TextView(this);
            t3.setTextSize(16);
            t3.setLayoutParams(LEFT_LAYOUT_PARAMS);
            t3.setText(a.getDurationAsString());
            t3.setTextColor(Color.BLACK);
            songLayout.setMinimumHeight(40);
            left.addView(artistNameLayout);
            songLayout.addView(left);
            songLayout.addView(t3);
            playlistLayout.addView(songLayout);
            list.add(songLayout);
            songLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RelativeLayout bt1 = (RelativeLayout) v;
                    bt1.setBackgroundColor(Color.GRAY);
                    if (current != -1) list.get(current).setBackgroundColor(Color.WHITE);
                    current = list.indexOf(bt1);
                    service.playURL(vk.getAudios().get(current), timePassed, timeLeft, seekBar, playPauseButton);
                }
            });
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

    }

    private ImageButton createShuffleButton() {
        ImageButton shuffle = new ImageButton(this);
        shuffle.setBackgroundResource(0);
        shuffle.setImageResource(R.drawable.shuffle);
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //service.forward();
            }
        });
        return shuffle;
    }

    private ImageView createForwardButton() {
        ImageButton forward = new ImageButton(this);
        forward.setBackgroundResource(0);
        forward.setImageResource(R.drawable.forward);
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                service.forward();
            }
        });
        return forward;
    }

    private ImageButton createBackwardButton() {
        ImageButton backward = new ImageButton(this);
        backward.setBackgroundResource(0);
        backward.setImageResource(R.drawable.backward);
        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                service.backward();
            }
        });
        return backward;
    }

    private void createTimeLeftView() {
        timeLeft = new TextView(this);
        timeLeft.setText(getString(R.string.time));
        timeLeft.setTextSize(16);
        timeLeft.setTextColor(Color.BLACK);
        timeLeft.setGravity(Gravity.CENTER_VERTICAL);
        timeLeft.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
    }

    private void createTimePassedView() {
        timePassed = new TextView(this);
        timePassed.setText(getString(R.string.time));
        timePassed.setTextSize(16);
        timePassed.setTextColor(Color.BLACK);
        timePassed.setGravity(Gravity.CENTER_VERTICAL);
        timePassed.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
    }

    private ImageButton createRepeatButton() {
        ImageButton repeat = new ImageButton(this);
        repeat.setBackgroundResource(0);
        repeat.setImageResource(R.drawable.repeat);
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                service.setRepeat();
            }
        });
        return repeat;
    }

    private void selectItem(int position) {

        switch (position) {
            case 0:
                mDrawerList.setItemChecked(position, true);
                mDrawerList.setSelection(position);
                setTitle(mNavigationDrawerItemTitles[position]);
                mDrawerLayout.closeDrawer(mDrawerList);
                displayPlaylist(false);
                break;
            case 1:
                mDrawerList.setItemChecked(position, true);
                mDrawerList.setSelection(position);
                setTitle(mNavigationDrawerItemTitles[position]);
                mDrawerLayout.closeDrawer(mDrawerList);
                displayPlaylist(true);
                break;
            case 2:
                Fragment fragment = new SettingsFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                mDrawerList.setItemChecked(position, true);
                mDrawerList.setSelection(position);
                setTitle(mNavigationDrawerItemTitles[position]);
                mDrawerLayout.closeDrawer(mDrawerList);
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    void setupDrawerToggle() {
        mDrawerToggle = new android.support.v7.app.ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        //This is necessary to change the icon of the Drawer Toggle upon state change.
        mDrawerToggle.syncState();
    }

    private class LoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        LoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            vk = new Account();
            return vk.authorize(mEmail, mPassword);
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                showLogForm = false;
                list = new ArrayList<>();
                playlist = vk.getAudios();
                selectItem(0);
                service.setPlaylist(playlist);
            }
        }
    }

}
