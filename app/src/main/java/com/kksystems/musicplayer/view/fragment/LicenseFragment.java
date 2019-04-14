package com.kksystems.musicplayer.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kksystems.musicplayer.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class LicenseFragment extends Fragment {
    private final String TAG = LicenseFragment.class.getSimpleName();

    private View mRootView = null;

    private TextView mLicenseText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.license, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//
//        mModel = ViewModelProviders
//            .of(this, InjectorUtils.providePlaylistFragmentViewModel(requireContext(), mMediaId))
//            .get(PlaylistFragmentViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRootView = view;

        assocViewInstance();
        setEventListener();
        initView();
    }

    private void assocViewInstance() {
        mLicenseText = mRootView.findViewById(R.id.licenseBody);
    }

    private void setEventListener() {
        // nothing to do...
    }

    private void initView() {
        try {
            StringBuilder sb = new StringBuilder();
            InputStream is = requireContext().getAssets().open("license.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
                sb.append("\n");
            }
            br.close();

            mLicenseText.setText(sb);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
