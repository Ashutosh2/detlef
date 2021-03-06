/* *************************************************************************
 *  Copyright 2012 The detlef developers                                   *
 *                                                                         *
 *  This program is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU General Public License as published by   *
 *  the Free Software Foundation, either version 2 of the License, or      *
 *  (at your option) any later version.                                    *
 *                                                                         *
 *  This program is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *  GNU General Public License for more details.                           *
 *                                                                         *
 *  You should have received a copy of the GNU General Public License      *
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
 ************************************************************************* */



package at.ac.tuwien.detlef.download;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import at.ac.tuwien.detlef.Detlef;

/**
 * DetlefDownloadManager keeps track of all active downloads and updates episode
 * information once the downloads have completed. On application shutdown, all running
 * downloads should be cancelled by calling {@link DetlefDownloadManager#cancelAll()}.
 */
public class DetlefDownloadManager {

    private static final String TAG = DetlefDownloadManager.class.getName();

    private final Map<Long, DownloadCallback> activeDownloads = new ConcurrentHashMap<Long, DownloadCallback>();
    private final Context context;
    private final DownloadManager downloadManager;

    public DetlefDownloadManager(Context context) {
        this.context = context;
        downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public void enqueue(DownloadCallback callback) throws IOException {
        if (!isExternalStorageWritable()) {
            throw new IOException("Cannot write to external storage");
        }

        Uri source = callback.getSource();

        /* Ensure the destination directory already exists. */

        File destination = new File(Detlef.getAppContext().getExternalFilesDir(
                                        callback.getDestinationDirType()), callback.getDestinationSubPath());
        destination.getParentFile().mkdirs();

        Request request = new Request(source);
        request.setDestinationInExternalFilesDir(context, callback.getDestinationDirType(),
                callback.getDestinationSubPath());
        request.addRequestHeader("user-agent", Detlef.USER_AGENT);
        request.setTitle(callback.getTitle());
        request.setDescription(callback.getDescription());
        request.setNotificationVisibility(callback.getNotificationVisibility());

        long id = downloadManager.enqueue(request);
        activeDownloads.put(id, callback);

        callback.onStart(destination.getAbsolutePath());

        Log.v(TAG, String.format("Enqueued download with id %d", id));
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public void cancel(Object object) {
        for (Entry<Long, DownloadCallback> entry : activeDownloads.entrySet()) {
            if (!entry.getValue().getObject().equals(object)) {
                continue;
            }

            downloadManager.remove(entry.getKey());

            DownloadCallback callback = activeDownloads.remove(entry.getKey());
            callback.onCancel();

            break;
        }
    }

    /**
     * Cancels all active downloads.
     */
    public void cancelAll() {
        for (Entry<Long, DownloadCallback> entry : activeDownloads.entrySet()) {
            downloadManager.remove(entry.getKey());

            DownloadCallback callback = activeDownloads.remove(entry.getKey());
            callback.onCancel();
        }
    }

    /**
     * Called once a download has completed. Responsible for updating the internal
     * state and pushing episode/podcast changes to the database.
     * @param id The download id.
     */
    public void downloadComplete(long id) {
        if (!activeDownloads.containsKey(id)) {
            return;
        }

        DownloadCallback callback = activeDownloads.remove(id);
        if (callback == null) {
            Log.w(TAG, String.format("No active download found for id %d", id));
            return;
        }

        if (!isDownloadSuccessful(id)) {
            Log.w(TAG, String.format("Download for id %d did not complete successfully (Reason: %d)",
                                     id, getDownloadFailureReason(id)));
            callback.onError();
            return;
        }

        Uri uri = downloadManager.getUriForDownloadedFile(id);
        Log.v(TAG, String.format("File %s downloaded successfully", uri.getPath()));

        callback.onFinish(uri);
    }

    private boolean isDownloadSuccessful(long id) {
        int status = getDownloadQueryAsInt(id, DownloadManager.COLUMN_STATUS);
        return (DownloadManager.STATUS_SUCCESSFUL == status);
    }

    private int getDownloadFailureReason(long id) {
        return getDownloadQueryAsInt(id, DownloadManager.COLUMN_REASON);
    }

    private int getDownloadQueryAsInt(long id, String column) {
        Query query = new Query();
        query.setFilterById(id);

        Cursor c = downloadManager.query(query);
        if (!c.moveToFirst()) {
            return -1;
        }

        int columnIndex = c.getColumnIndex(column);
        return c.getInt(columnIndex);
    }

    public interface DownloadCallback {
        void onStart(String path);
        void onCancel();
        void onError();
        void onFinish(Uri uri);

        Uri getSource();
        String getDestinationDirType();
        String getDestinationSubPath();
        String getTitle();
        String getDescription();
        int getNotificationVisibility();
        Object getObject();
    }
}
