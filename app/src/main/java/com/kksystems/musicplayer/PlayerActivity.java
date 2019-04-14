package com.kksystems.musicplayer;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.content.*;

import android.support.v4.app.FragmentTransaction;

import com.kksystems.musicplayer.service.AudioPlayerService;
import com.kksystems.musicplayer.utils.InjectorUtils;
import com.kksystems.musicplayer.view.fragment.NavigationFragment;
import com.kksystems.musicplayer.view.fragment.PlayerFragment;
import com.kksystems.musicplayer.viewmodel.PlayerActivityViewModel;

import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

/**
 * PlayerActivity class<br>
 * Activity for playing music.
 * <pre>
 * Create Player UI
 * </pre>
 * @author KK Systems
 * @version 1.0
 * @since 1.0
 */
public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = PlayerActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 10;

    private PlayerActivityViewModel mModel;
    private Bundle mSavedInstanceState = null;
    private NavController mNavController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        mSavedInstanceState = savedInstanceState;

        // 権限の確認を行う
        // 初回起動時は権限確認用のダイアログが表示される
        checkPermission();
    }

    private void checkPermission() {
        // 外部ストレージの使用権限があるかどうか
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // 権限がある
            initializeApp();
            return;
        }

        // 外部ストレージの使用が許可されていない場合
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // 毎回権限取得用ダイアログが表示される
        }

        // 外部ストレージの使用権限を取得する
        requestPermissions(new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode != MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            return;
        }
        if ( !((permissions.length > 0) && (permissions.length == grantResults.length)) ) {
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 外部ストレージの使用権限が取得できた
            initializeApp();
        } else {
            // 権限が取得できなかったため、アプリを終了する
            finish();
        }
    }

    private void initializeApp() {
        setContentView(R.layout.main);

        mNavController = Navigation.findNavController(this, R.id.view_container);

        // AudioPlayerServiceサービスの開始
        startService(new Intent(this, AudioPlayerService.class));

        mModel = ViewModelProviders
                .of(this, InjectorUtils.providePlayerActivityViewModel(this))
                .get(PlayerActivityViewModel.class);

        mModel.getRootMediaId().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s != null) {
//                    transitNavigationScreen();
//
                    onNewIntent(getIntent());
                }
            }
        });
    }
//
//    @Override
//    public void onBackPressed() {
//       FragmentManager fragmentMgr = getSupportFragmentManager();
//        for (Fragment fragment : fragmentMgr.getFragments()) {
//            if (fragment.isVisible()) {
//                FragmentManager childFragmentMgr = fragment.getChildFragmentManager();
//                if (childFragmentMgr.getBackStackEntryCount() > 0) {
//                    childFragmentMgr.popBackStack();
//                    return;
//                }
//            }
//        }
//        super.onBackPressed();
//    }


    @Override
    public void supportNavigateUpTo(@NonNull Intent upIntent) {
        super.supportNavigateUpTo(upIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
	protected void onDestroy() {
		// TODO: Implement this method
        Log.d(TAG, "onDestroy");

        super.onDestroy();

        // 再生中はサービスを終了しない
        if (mModel != null && !mModel.isPlaying()) {
            stopService(new Intent(this, AudioPlayerService.class));
        }
	}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String mediaId = intent.getStringExtra("MEDIA_ID");
        if (mediaId == null) {
            mediaId = MusicPlayerUtil.getContentIdFromFileUri(getApplicationContext(), intent.getData());
        }

        if (mediaId != null && !mediaId.equals("")) {
            transitPlayerFragment(mediaId);
        }
    }

    private void transitPlayerFragment(String mediaId) {
        Bundle args = new Bundle();
        args.putString(Define.MEDIA_ID, mediaId);

        NavDestination curDest = mNavController.getCurrentDestination();
        int curId = curDest != null ? curDest.getId() : 0;
        switch (curId) {
            case R.id.navigationFragment:
                mNavController.navigate(R.id.action_navigationFragment_to_playerFragment, args);
                break;
            case R.id.playerFragment:
                break;
            default:
                mNavController.navigate(R.id.playerFragment, args);
                break;
        }
    }

    public void requestTransitPlayerFragment(String mediaId) {
        transitPlayerFragment(mediaId);
    }

    private boolean isRootId(String mediaId) {
        return mediaId.equals(mModel.getRootMediaId().getValue());
    }
}
