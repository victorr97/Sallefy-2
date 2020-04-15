package salle.android.projects.registertest.restapi.manager;

import android.content.Context;
import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import salle.android.projects.registertest.model.Playlist;
import salle.android.projects.registertest.model.Track;
import salle.android.projects.registertest.model.UserToken;
import salle.android.projects.registertest.restapi.callback.MeCallback;
import salle.android.projects.registertest.restapi.service.MeService;
import salle.android.projects.registertest.utils.Constants;
import salle.android.projects.registertest.utils.Session;

public class MeManager {
    private static final String TAG = "MeManager";
    private Context mContext;
    private static MeManager sMeManager;
    private Retrofit mRetrofit;
    private MeService mService;

    public static MeManager getInstance (Context context) {
        if (sMeManager == null) {
            sMeManager = new MeManager(context);
        }
        return sMeManager;
    }

    public MeManager(Context context) {
        mContext = context;

        mRetrofit = new Retrofit.Builder()
                .baseUrl(Constants.NETWORK.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mService = mRetrofit.create(MeService.class);
    }

    /********************   SHOW MY PLAYLIST    ********************/
    public synchronized void getMyPlaylists(final MeCallback meCallback) {
        UserToken userToken = Session.getInstance(mContext).getUserToken();

        Call<List<Playlist>> call = mService.callMyPlaylists( "Bearer " + userToken.getIdToken());
        call.enqueue(new Callback<List<Playlist>>() {
            @Override
            public void onResponse(Call<List<Playlist>> call, Response<List<Playlist>> response) {
                int code = response.code();
                if (response.isSuccessful()) {
                    meCallback.myPlaylistsReceived(response.body());
                } else {
                    Log.d(TAG, "Error Not Successful: " + code);
                    meCallback.noPlaylistsReceived(new Throwable("ERROR " + code + ", " + response.raw().message()));
                }
            }
            @Override
            public void onFailure(Call<List<Playlist>> call, Throwable t) {
                Log.d(TAG, "Error Failure: " + t.getStackTrace());
                meCallback.onFailure(new Throwable("ERROR " + t.getStackTrace()));
            }
        });
    }

    /********************   SHOW MY PLAYLIST FOLLOWED    ********************/

    public synchronized void getMyPlaylistsFollowed(final MeCallback meCallback) {
        UserToken userToken = Session.getInstance(mContext).getUserToken();

        Call<List<Playlist>> call = mService.callPlaylistsFollowing( "Bearer " + userToken.getIdToken());
        call.enqueue(new Callback<List<Playlist>>() {
            @Override
            public void onResponse(Call<List<Playlist>> call, Response<List<Playlist>> response) {
                int code = response.code();
                if (response.isSuccessful()) {
                    meCallback.playlistsFollowingReceived(response.body());
                } else {
                    Log.d(TAG, "Error Not Successful: " + code);
                    meCallback.noPlaylistsReceived(new Throwable("ERROR " + code + ", " + response.raw().message()));
                }
            }
            @Override
            public void onFailure(Call<List<Playlist>> call, Throwable t) {
                Log.d(TAG, "Error Failure: " + t.getStackTrace());
                meCallback.onFailure(new Throwable("ERROR " + t.getStackTrace()));
            }
        });
    }

    /********************   SHOW MY TRACKS    ********************/
    public synchronized void getMyTracks(final MeCallback meCallback) {
        UserToken userToken = Session.getInstance(mContext).getUserToken();

        Call<List<Track>> call = mService.callMyTrakcs( "Bearer " + userToken.getIdToken());
        call.enqueue(new Callback<List<Track>>() {
            @Override
            public void onResponse(Call<List<Track>> call, Response<List<Track>> response) {
                int code = response.code();

                if (response.isSuccessful()) {
                    meCallback.myTracksReceived(response.body());
                } else {
                    Log.d(TAG, "Error Not Successful: " + code);
                    meCallback.noTracksReceived(new Throwable("ERROR " + code + ", " + response.raw().message()));
                }
            }

            @Override
            public void onFailure(Call<List<Track>> call, Throwable t) {
                Log.d(TAG, "Error Failure: " + t.getStackTrace());
                meCallback.onFailure(new Throwable("ERROR " + t.getStackTrace()));
            }
        });
    }

    /********************   SHOW MY TRACKS LIKED   ********************/

    public synchronized void getMyTracksLiked(final MeCallback meCallback) {
        UserToken userToken = Session.getInstance(mContext).getUserToken();

        Call<List<Track>> call = mService.callTracksLiked( "Bearer " + userToken.getIdToken());
        call.enqueue(new Callback<List<Track>>() {
            @Override
            public void onResponse(Call<List<Track>> call, Response<List<Track>> response) {
                int code = response.code();

                if (response.isSuccessful()) {
                    meCallback.tracksLikedReceived(response.body());
                } else {
                    Log.d(TAG, "Error Not Successful: " + code);
                    meCallback.noTracksReceived(new Throwable("ERROR " + code + ", " + response.raw().message()));
                }
            }

            @Override
            public void onFailure(Call<List<Track>> call, Throwable t) {
                Log.d(TAG, "Error Failure: " + t.getStackTrace());
                meCallback.onFailure(new Throwable("ERROR " + t.getStackTrace()));
            }
        });
    }
}