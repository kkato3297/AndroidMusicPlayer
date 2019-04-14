package com.kksystems.musicplayer.view.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kksystems.musicplayer.Define;
import com.kksystems.musicplayer.PlayerActivity;
import com.kksystems.musicplayer.R;
import com.kksystems.musicplayer.adapter.PlaylistGridViewAdapter;
import com.kksystems.musicplayer.model.PlaylistMediaItemData;
import com.kksystems.musicplayer.utils.InjectorUtils;
import com.kksystems.musicplayer.view.event.RecyclerViewItemClickListener;
import com.kksystems.musicplayer.viewmodel.PlaylistFragmentViewModel;

import java.util.ArrayList;
import java.util.List;

public final class PlaylistFragment extends Fragment {
    private final String TAG = PlaylistFragment.class.getSimpleName();

    private View rootView = null;

    private RecyclerView listView;

    private PlaylistFragmentViewModel mModel;
    private String mMediaId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arg = getArguments();

        mMediaId = (arg != null) ? arg.getString(Define.MEDIA_ID) : null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.playlist, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mModel = ViewModelProviders
            .of(this, InjectorUtils.providePlaylistFragmentViewModel(requireContext(), mMediaId))
            .get(PlaylistFragmentViewModel.class);

        mModel.getMediaList().observe(this, new Observer<List<MediaBrowserCompat.MediaItem>>() {
            @Override
            public void onChanged(@Nullable List<MediaBrowserCompat.MediaItem> mediaItem) {
                updateList(mediaItem);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rootView = view;

        assocViewInstance();
        setEventListener();

//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        if (layoutManager instanceof GridLayoutManager) {
            // anything to do...
        } else if (layoutManager instanceof LinearLayoutManager) {
            ((LinearLayoutManager) layoutManager).setOrientation(LinearLayoutManager.VERTICAL);
        }
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);
    }

    private void assocViewInstance() {
        listView = rootView.findViewById(R.id.musicList);
    }

    private void setEventListener() {
        listView.addOnItemTouchListener(
                new RecyclerViewItemClickListener(getActivity(), new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        String targetMediaId = mModel.getMediaList().getValue().get(position).getMediaId();
                        boolean isAlreadyPlaying = mModel.getPlayingMediaId().equals(targetMediaId);

                        // 現在セットされているメディアと異なるメディアをセットする
                        if (!isAlreadyPlaying) {
                            mModel.setMedia(targetMediaId);
                        }
                        // 現在再生中の楽曲とは違う楽曲が選択された
                        // あるいは
                        // 同じ楽曲であるものの再生されていない場合、再生する
                        if (!isAlreadyPlaying || !mModel.isPlaying()) {
                            mModel.play();
                        }

                        final PlayerActivity activity = (PlayerActivity) getActivity();
                        if (activity != null) {
                            activity.requestTransitPlayerFragment(mModel.getMediaList().getValue().get(position).getMediaId());
                        }
                    }
                })
        );
    }

    private void updateList(List<MediaBrowserCompat.MediaItem> mediaList) {
        List<PlaylistMediaItemData> list = new ArrayList<>();

        for (MediaBrowserCompat.MediaItem item : mediaList) {
            PlaylistMediaItemData map = new PlaylistMediaItemData();

            map.setMediaId(item.getMediaId());
            map.setJacketImageUri(item.getDescription().getIconUri());
            map.setTitle((String) item.getDescription().getTitle());
            map.setArtist((String) item.getDescription().getSubtitle());

            list.add(map);
        }

        PlaylistGridViewAdapter adapter = new PlaylistGridViewAdapter(list);

        listView.setAdapter(adapter);
    }
}
