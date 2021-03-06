package salle.android.projects.registertest.controller.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import salle.android.projects.registertest.R;
import salle.android.projects.registertest.controller.adapters.SectionPagerAdapter;

public class LibraryFragment extends Fragment {

    public static final String TAG = LibraryFragment.class.getName();

    private View myFragment;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    public LibraryFragment(){

    }

    public static LibraryFragment getInstance(){
        return new LibraryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_library, container, false);

        viewPager = myFragment.findViewById(R.id.viewPager);
        tabLayout = myFragment.findViewById(R.id.tabLayout);

        return myFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUpViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

        private void setUpViewPager(ViewPager viewPager) {
            SectionPagerAdapter adapter = new SectionPagerAdapter(getChildFragmentManager());
            String pltitle = "Playlist";
            String ctitle = "Canciones";
            SpannableString PlaylistsTitle = new SpannableString(pltitle);
            SpannableString CacnionesTitle = new SpannableString(ctitle);;
            PlaylistsTitle.setSpan(new ForegroundColorSpan(Color.WHITE),0, PlaylistsTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            CacnionesTitle.setSpan(new ForegroundColorSpan(Color.WHITE),0, CacnionesTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            adapter.addFragment(new LibraryPlaylistFragment(), PlaylistsTitle);
            adapter.addFragment(new LibraryTrackFragment(), CacnionesTitle);
            viewPager.setAdapter(adapter);
    }
}