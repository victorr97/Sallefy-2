package salle.android.projects.registertest.controller.fragments;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.ArrayList;
import java.util.List;

import salle.android.projects.registertest.R;
import salle.android.projects.registertest.controller.adapters.TrackListAdapter;
import salle.android.projects.registertest.controller.callbacks.FragmentCallback;
import salle.android.projects.registertest.controller.callbacks.TrackListCallback;
import salle.android.projects.registertest.model.Track;
import salle.android.projects.registertest.restapi.callback.TrackCallback;
import salle.android.projects.registertest.restapi.manager.TrackManager;

public class SongsFragment extends Fragment implements TrackListCallback, TrackCallback {

    public static final String TAG = SongsFragment.class.getName();

    private RecyclerView mRecyclerView;
    private ArrayList<Track> mTracks;
    private int currentTrack = 0;
    private FragmentCallback callback;


    public static SongsFragment getInstance() {
        return new SongsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.callback = (FragmentCallback) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.activity_advanced_list, container, false);
        initViews(v);
        getData();
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initViews(View v) {
        mRecyclerView = (RecyclerView) v.findViewById(R.id.dynamic_recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        TrackListAdapter adapter = new TrackListAdapter(this, getActivity(), null);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);
    }

    private void getData() {
        TrackManager.getInstance(getActivity()).getAllTracks(this);
        mTracks = new ArrayList<>();
    }

    public int getIndex(){
        return currentTrack;
    }

    public ArrayList<Track> getmTracks(){
        return mTracks;
    }


    /**********************************************************************************************
     *   *   *   *   *   *   *   *   TrackCallback   *   *   *   *   *   *   *   *   *
     **********************************************************************************************/

    @Override
    public void onTracksReceived(List<Track> tracks) {
        mTracks = (ArrayList) tracks;
        TrackListAdapter adapter = new TrackListAdapter(this, getActivity(), mTracks);
        mRecyclerView.setAdapter(adapter);
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
    public void onFailure(Throwable throwable) {

    }

    /**********************************************************************************************
     *   *   *   *   *   *   *   *   TrackListCallback   *   *   *   *   *   *   *   *   *
     **********************************************************************************************/

    @Override
    public void onTrackSelected(Track track) {

    }
    @Override
    public void onTrackSelected(int index) {
        callback.updateTrack(mTracks, index);
    }
    @Override
    public void onTrackLike(int index) {
        TrackManager.getInstance(getContext()).likeTrack(mTracks.get(index).getId(),this);
    }
}