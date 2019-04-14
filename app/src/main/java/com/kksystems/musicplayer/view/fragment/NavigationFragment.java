package com.kksystems.musicplayer.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.kksystems.musicplayer.Define;
import com.kksystems.musicplayer.R;

import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

public final class NavigationFragment extends Fragment {
    private final String TAG = NavigationFragment.class.getSimpleName();

    private View mRootView;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private NavController mNavController;

    public static NavigationFragment newInstance() {
        return new NavigationFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {
            activity.findViewById(android.R.id.content).setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            //activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        final AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {
            activity.findViewById(android.R.id.content).setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.nav_fragment, container, false);

        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbar = view.findViewById(R.id.playlistToolbar);
        if (activity != null && toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_launcher_foreground);
            toolbar.setTitle(R.string.app_name);

            final DrawerLayout drawerLayout = view.findViewById(R.id.drawer);
            ActionBarDrawerToggle actionBarDrawerToggle =
                    new ActionBarDrawerToggle(
                            getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close){
                        @Override
                        public void onDrawerOpened(View drawerView) {
                            super.onDrawerOpened(drawerView);
                            activity.invalidateOptionsMenu();
                        }

                        @Override
                        public void onDrawerClosed(View drawerView) {
                            super.onDrawerClosed(drawerView);
                            activity.invalidateOptionsMenu();
                        }
                    };
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();

            activity.setSupportActionBar(toolbar);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (drawerLayout.isDrawerVisible(Gravity.START)) {
                        drawerLayout.closeDrawer(Gravity.START);
                    } else {
                        drawerLayout.openDrawer(Gravity.START);
                    }
                }
            });
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRootView = view;

        assocViewInstance();
        setEventListener();

        // initialize view
//        transitPlaylistScreen();
    }

    private void assocViewInstance() {
        mNavigationView = mRootView.findViewById(R.id.nav_view);
        mToolbar = mRootView.findViewById(R.id.playlistToolbar);
        mDrawerLayout = mRootView.findViewById(R.id.drawer);
        mNavController = Navigation.findNavController(mRootView.findViewById(R.id.child_container));
    }

    private void setEventListener() {
        mNavigationView.setNavigationItemSelectedListener(
                (@NonNull MenuItem menuItem) -> {
                    boolean bRet = false;

                    mDrawerLayout.closeDrawer(Gravity.START);

                    switch (menuItem.getItemId()) {
                        case R.id.nav_menu_license:
                            transitLicenseScreen();
                            break;
                        case R.id.nav_menu_album:
                            //Toast.makeText(requireContext(), "You selected album list, but the page is requested is under construction...", Toast.LENGTH_SHORT).show();
                            transitAlbumlistScreen();
                            bRet = true;
                            break;
                        case R.id.nav_menu_music:
                            transitPlaylistScreen();
                            bRet = true;
                            break;
                        default:
                            break;
                    }

                    return bRet;
                }
        );
    }

    private void transitLicenseScreen() {
        NavDestination curDestination = mNavController.getCurrentDestination();
        if (curDestination != null) {
            switch (curDestination.getId()) {
                case R.id.licenseFragment:
                    break;
                default:
                    mNavController.navigate(R.id.licenseFragment, null, mAnimOption);
                    break;
            }
        }
    }

    private void transitPlaylistScreen() {
        NavDestination curDestination = mNavController.getCurrentDestination();
        if (curDestination != null) {
            switch (curDestination.getId()) {
                case R.id.playlistFragment:
                    break;
                default:
                    mNavController.popBackStack(R.id.rootFragment, false);
                    mNavController.navigate(R.id.playlistGroup, null, mAnimOption);
                    break;
            }
        }
    }

    private void transitAlbumlistScreen() {
        NavDestination curDestination = mNavController.getCurrentDestination();
        if (curDestination != null) {
            switch (curDestination.getId()) {
                case R.id.albumlistFragment:
                    break;
                default:
                    mNavController.popBackStack(R.id.rootFragment, false);
                    mNavController.navigate(R.id.albumGroup, null, mAnimOption);
                    break;
            }
        }
    }

    private void transitSonglistScreen(String mediaId) {
        Bundle bundle = new Bundle();
        bundle.putString(Define.MEDIA_ID, mediaId);

        NavDestination curDestination = mNavController.getCurrentDestination();
        if (curDestination != null) {
            switch (curDestination.getId()) {
                case R.id.songlistFragment:
                    break;
                default:
                    mNavController.navigate(R.id.songlistFragment, bundle, mAnimOption);
                    break;
            }
        }
    }

    public void requestTransitSonglistScreen(String mediaId) {
        transitSonglistScreen(mediaId);
    }

    private NavOptions mAnimOption = new NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build();
}
