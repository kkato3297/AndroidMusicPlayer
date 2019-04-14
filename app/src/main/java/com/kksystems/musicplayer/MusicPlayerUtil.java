package com.kksystems.musicplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MusicPlayerUtil {
    /**
     * @param context
     * @param srcUri 外部ストレージにある画像のURI file://.... 形式
     * @return 変換されたURI content://media/external/images/media/[ID（数字）] 形式
     */
    public static String getContentIdFromFileUri(Context context, Uri srcUri) {
        final Uri baseUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String[] proj = {BaseColumns._ID};
        final String sel = MediaStore.Audio.AudioColumns.DATA + " LIKE ?";

        final ContentResolver cr = context.getContentResolver();

        final Function<Uri, Boolean> isDocumentURI = (Uri uri) ->
                uri.toString().contains("content://com.android.providers.media.documents/document/");
        final Function<Uri, Boolean> isMediaURI = (Uri uri) ->
                uri.toString().contains(baseUri.toString()) ||
                uri.toString().contains("content://com.android.externalstorage.documents/document/");
        final Function<String, String> getExternalDir = (String key) -> {
            String ret;
            ArrayList<String> dirList = (new Function<File[], ArrayList<String>>() {
                @Override
                public ArrayList<String> apply(File fileList[]) {
                    ArrayList<String> pathList = new ArrayList<>();
                    for (File file : fileList) {
                        pathList.add(file.getParentFile().getParentFile().getParentFile().getParentFile().getPath());
                    }
                    return pathList;
                }
            }).apply(context.getExternalFilesDirs(null));

            if (key.equals("primary")) {
                ret = dirList.get(0);
            } else {
                ret = dirList.stream().filter((String s) -> s.contains(key)).collect(Collectors.joining());
            }

            return ret;
        };

        String result = "";

        if (srcUri == null) {
            return null;
        }

        if (isMediaURI.apply(srcUri)) {
            String filePath = srcUri.toString().contains(baseUri.toString()) ?
                    srcUri.getPath() :
                    (new Function<String, String>() {
                        @Override
                        public String apply(String s) {
                            return getExternalDir.apply(s.split(":")[0]) + "/" + s.split(":")[1];
                        }
                    }).apply(Uri.decode(DocumentsContract.getDocumentId(srcUri)));
            String[] selArgs = new String[]{filePath};

            try {
                Cursor cur = cr.query(baseUri, proj, sel, selArgs, null);

                if (cur != null) {
                    cur.moveToFirst();
                    Log.d(MusicPlayerUtil.class.getSimpleName(), "Cursor: {" + cur + "}");
                    result = cur.getString(cur.getColumnIndex(proj[0]));
                    cur.close();
                }
            } catch (Exception e) {
                Log.v("fileUri2contentUri", "file://からcontent://形式変換中にエラー", e);
            }
        } else if (isDocumentURI.apply(srcUri)) {
            result = DocumentsContract.getDocumentId(srcUri).split(":")[1];
        }

        return result;
    }
}
