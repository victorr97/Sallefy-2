package salle.android.projects.registertest.controller.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.util.ArrayList;
import java.util.List;

import salle.android.projects.registertest.R;
import salle.android.projects.registertest.controller.callbacks.FragmentCallback;
import salle.android.projects.registertest.controller.callbacks.TrackListCallback;
import salle.android.projects.registertest.controller.fragments.HomeFragment;
import salle.android.projects.registertest.controller.fragments.LibraryFragment;
import salle.android.projects.registertest.controller.fragments.PerfilFragment;
import salle.android.projects.registertest.controller.fragments.PlaylistFragment;
import salle.android.projects.registertest.controller.fragments.SearchFragment;
import salle.android.projects.registertest.controller.fragments.TrackFragment;
import salle.android.projects.registertest.controller.music.MusicCallback;
import salle.android.projects.registertest.controller.music.MusicService;
import salle.android.projects.registertest.model.Playlist;
import salle.android.projects.registertest.model.Track;
import salle.android.projects.registertest.restapi.callback.PlaylistCallback;
import salle.android.projects.registertest.restapi.callback.TrackCallback;
import salle.android.projects.registertest.restapi.manager.PlaylistManager;
import salle.android.projects.registertest.restapi.manager.TrackManager;
import salle.android.projects.registertest.utils.Constants;
import salle.android.projects.registertest.utils.Session;

public class MainActivity extends FragmentActivity implements FragmentCallback, MusicCallback, PlaylistCallback, TrackCallback {

    private FragmentManager mFragmentManager;
    private FragmentTransaction mTransaction;

    private BottomNavigationView mNav;

    private static final String PLAY_VIEW = "PlayIcon";
    private static final String STOP_VIEW = "StopIcon";

    private TextView tvTitle;
    private TextView tvAuthor;

    private ImageButton btnBackward;
    private ImageButton btnPlayStop;
    private ImageButton btnForward;
    private SeekBar mSeekBar;

    private int mDuration;
    private Handler mHandler;
    private Runnable mRunnable;

    // Service
    private MusicService mBoundService;
    private boolean mServiceBound = false;

    private ArrayList<Track> mTracks;
    private int currentTrack = 0;

    private boolean shareAction = false;

    private Playlist playlistShare;
    private Track trackShare;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            mBoundService = binder.getService();
            mBoundService.setCallback(MainActivity.this);
            mServiceBound = true;
            updateSeekBar();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startStreamingService();
        initViews();
        if (!checkShare()) {
            setInitialFragment();
        }
        requestPermissions();
    }

    public boolean doActionCheck(Fragment fragment) {
        Session.getInstance().setPath(null);
        Session.getInstance().setNum(-1);
        onChangeFragment(fragment);
        return true;
    }

    public boolean checkShare(){
        if (Session.getInstance().getPath() != null){
            shareAction = true;
            if (Session.getInstance().getPath().equals("track")){
                TrackManager.getInstance(this).getTrack(Session.getInstance().getNum(),this);
            } else if(Session.getInstance().getPath().equals("playlist")){
                PlaylistManager.getInstance(this).getPlaylist(Session.getInstance().getNum(),this);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBoundService != null) {
            resumeSongText();
            if (mBoundService.isPlaying()) {
                playAudio();
            } else {
                pauseAudio();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mServiceBound) {
            //pauseAudio();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mServiceBound) {
            this.unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {

        mHandler = new Handler();

        tvAuthor = findViewById(R.id.dynamic_artist);
        tvTitle = findViewById(R.id.dynamic_title);

        btnBackward = (ImageButton)findViewById(R.id.dynamic_backward_btn);
        btnBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTrack = ((currentTrack-1)%(mTracks.size()));
                currentTrack = currentTrack < 0 ? (mTracks.size()-1):currentTrack;
                updateSong(currentTrack);
            }
        });
        btnForward = (ImageButton)findViewById(R.id.dynamic_forward_btn);
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTrack = ((currentTrack+1)%(mTracks.size()));
                currentTrack = currentTrack >= mTracks.size() ? 0:currentTrack;
                updateSong(currentTrack);
            }
        });

        btnPlayStop = (ImageButton)findViewById(R.id.dynamic_play_btn);
        btnPlayStop.setTag(PLAY_VIEW);
        btnPlayStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (btnPlayStop.getTag().equals(PLAY_VIEW)) {
                    playAudio();
                } else {
                    pauseAudio();
                }
            }
        });

        mSeekBar = (SeekBar) findViewById(R.id.dynamic_seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mBoundService.setCurrentDuration(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mFragmentManager = getSupportFragmentManager();
        mTransaction = mFragmentManager.beginTransaction();

        mNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;
                switch (menuItem.getItemId()) {
                    case R.id.action_home:
                        fragment = HomeFragment.getInstance();
                        break;
                    case R.id.action_search:
                        fragment = SearchFragment.getInstance();
                        break;
                    case R.id.action_library:
                        fragment = LibraryFragment.getInstance();
                        break;
                    case R.id.action_profile:
                        fragment = PerfilFragment.getInstance();
                        break;
                }
                replaceFragment(fragment);
                return true;
            }
        });
    }

        private void startStreamingService(){
            Intent intent = new Intent(getApplicationContext(), MusicService.class);
            this.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }

        private void playAudio() {
            if (!mBoundService.isPlaying()) { mBoundService.togglePlayer(); }
            updateSeekBar();
            btnPlayStop.setImageResource(R.drawable.ic_pause);
            btnPlayStop.setTag(STOP_VIEW);
        }

        private void pauseAudio() {
            if (mBoundService.isPlaying()) { mBoundService.togglePlayer(); }
            btnPlayStop.setImageResource(R.drawable.ic_play);
            btnPlayStop.setTag(PLAY_VIEW);
        }

        public void updateSeekBar() {
            System.out.println("max duration: " + mBoundService.getMaxDuration());
            System.out.println("progress:" + mBoundService.getCurrrentPosition());
            mSeekBar.setMax(mBoundService.getMaxDuration());
            mSeekBar.setProgress(mBoundService.getCurrrentPosition());

            if(mBoundService.isPlaying()) {
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        updateSeekBar();
                    }
                };
                mHandler.postDelayed(mRunnable, 1000);
            }
        }

    public void updateSong(int index) {
        Track track = mTracks.get(index);
        currentTrack = index;
        tvAuthor.setText(track.getUserLogin());
        tvTitle.setText(track.getName());
        mBoundService.playStream(mTracks, index);
        btnPlayStop.setImageResource(R.drawable.ic_pause);
        btnPlayStop.setTag(STOP_VIEW);
        //updateSeekBar();
    }

    private void resumeSongText() {
        Track track = mBoundService.getCurrentTrack();
        if (track != null) {
            tvAuthor.setText(track.getUserLogin());
            tvTitle.setText(track.getName());
        }
    }

    private void setInitialFragment() {
        mTransaction.add(R.id.fragment_container, HomeFragment.getInstance());
        mTransaction.commit();
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.MODIFY_AUDIO_SETTINGS}, Constants.PERMISSIONS.MICROPHONE);

        } else {
            Session.getInstance().setAudioEnabled(true);
        }
    }

    private void replaceFragment(Fragment fragment) {
        String fragmentTag = getFragmentTag(fragment);
        Fragment currentFragment = mFragmentManager.findFragmentByTag(fragmentTag);
        if (currentFragment != null) {
            if (!currentFragment.isVisible()) {

                if (fragment.getArguments() != null) {
                    currentFragment.setArguments(fragment.getArguments());
                }
                mFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, currentFragment, fragmentTag)
                        .addToBackStack(null)
                        .commit();

            }
        } else {
            mFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment, fragmentTag)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private String getFragmentTag(Fragment fragment) {
        String TAG = null;
        if (fragment instanceof HomeFragment) {
             TAG = HomeFragment.TAG;
        } else {
            if (fragment instanceof SearchFragment) {
                 TAG = SearchFragment.TAG;
            } else {
                if (fragment instanceof LibraryFragment) {
                    return LibraryFragment.TAG;
                } else {
                    if (fragment instanceof PerfilFragment)
                    return PerfilFragment.TAG;
                }
            }
        }
        return TAG;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println(requestCode);
        if (requestCode == Constants.PERMISSIONS.MICROPHONE) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Session.getInstance().setAudioEnabled(true);
            } else {
            }
            return;
        }
    }

    /**********************************************************************************************
     *   *   *   *   *   *   *   *   MusicCallback   *   *   *   *   *   *   *   *   *
     **********************************************************************************************/

    @Override
    public void onMusicPlayerPrepared() {
        System.out.println("Entra en el prepared");
        mSeekBar.setMax(mBoundService.getMaxDuration());
        mDuration =  mBoundService.getMaxDuration();
        playAudio();
    }

    /**********************************************************************************************
     *   *   *   *   *   *   *   *   FragmentCallback   *   *   *   *   *   *   *   *   *
     **********************************************************************************************/

    @Override
    public void onChangeFragment(Fragment fragment) {
        replaceFragment(fragment);
    }

    @Override
    public void updateTrack(ArrayList<Track> mTracks, int index) {
        this.mTracks = mTracks;
        updateSong(index);
    }


    /**********************************************************************************************
     *   *   *   *   *   *   *   *   PlaylistCallback   *   *   *   *   *   *   *   *   *   *   *
     **********************************************************************************************/

    @Override
    public void onShowPlaylist(List<Playlist> playlists) {

    }

    @Override
    public void onShowPlaylistFailure(Throwable throwable) {

    }

    @Override
    public void onCreateSuccess(Playlist playlist) {

    }

    @Override
    public void onCreateFailed(Throwable throwable) {

    }

    @Override
    public void onUpdateSucces(Playlist playlist) {

    }

    @Override
    public void onFollowSucces(Playlist playlist) {

    }

    @Override
    public void getIsFollowed(Playlist playlist) {

    }

    @Override
    public void getPlaylist(Playlist playlist) {
         this.playlistShare = playlist;
         if (shareAction) {
             Fragment fragment = PlaylistFragment.getInstance(playlistShare);
             doActionCheck(fragment);
             shareAction = false;
         }

    }

    @Override
    public void onFailure(Throwable throwable) {

    }

    /**********************************************************************************************
     *   *   *   *   *   *   *   *   TrackCallback   *   *   *   *   *   *   *   *   *   *   *
     **********************************************************************************************/

    @Override
    public void onTracksReceived(List<Track> tracks) {

    }

    @Override
    public void onNoTracks(Throwable throwable) {

    }

    @Override
    public void onPersonalTracksReceived(List<Track> tracks) {

    }

    @Override
    public void onUserTracksReceived(List<Track> tracks) {

    }

    @Override
    public void onCreateTrack() {

    }

    @Override
    public void onLikeSuccess(Track track) {

    }

    @Override
    public void getTrack(Track track) {
        this.trackShare = track;
        if (shareAction) {
            Fragment fragment = TrackFragment.getInstance(trackShare);
            doActionCheck(fragment);
            shareAction = false;
        }
    }
}