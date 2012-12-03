
package at.ac.tuwien.detlef.activities;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.adapters.PlaylistListAdapter;
import at.ac.tuwien.detlef.db.PlaylistDAO;
import at.ac.tuwien.detlef.db.PlaylistDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.download.DetlefDownloadManager;
import at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService;
import at.ac.tuwien.detlef.mediaplayer.MediaPlayerService;

import com.mobeta.android.dslv.DragSortListView;

public class PlaylistActivity extends ListActivity implements PlaylistDAO.OnPlaylistChangeListener {

    private ArrayList<Episode> playlistItems;
    private PlaylistListAdapter adapter;
    private PlaylistDAO playlistDAO;
    private DetlefDownloadManager downloadManager;

    private boolean bound;
    private IMediaPlayerService service;

    /**
     * Handles the connection to the MediaPlayerService that plays music.
     */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            bound = true;
            Log.d(getClass().getName(), "Service connected to playlistactivity");
            MediaPlayerService.MediaPlayerBinder binder =
                    (MediaPlayerService.MediaPlayerBinder) iBinder;
            service = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_activity_layout);

        playlistDAO = PlaylistDAOImpl.i();
        playlistDAO.addPlaylistChangedListener(this);
        playlistItems = playlistDAO.getNonCachedEpisodes();

        downloadManager = DependencyAssistant.getDependencyAssistant().getDownloadManager(
                Detlef.getAppContext());

        initListView();
        registerForContextMenu(getListView());
        connectToMediaService();
    }

    private void initListView() {
        DragSortListView lv = (DragSortListView) getListView();
        lv.setDropListener(onDrop);
        lv.setRemoveListener(onRemove);
        adapter = new PlaylistListAdapter(this, R.layout.playlist_list_layout,
                playlistItems);
        setListAdapter(adapter);
    }

    private void connectToMediaService() {
        if (!MediaPlayerService.isRunning()) {
            Intent serviceIntent =
                    new Intent(Detlef.getAppContext(), MediaPlayerService.class);
            Detlef.getAppContext().startService(serviceIntent);
        }
        Intent intent = new Intent(this, MediaPlayerService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (bound) {
            unbindService(connection);
            bound = false;
        }
    }

    private final DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            playlistDAO.moveEpisode(from, to);
        }
    };

    private final DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
        @Override
        public void remove(int which) {
            playlistDAO.removeEpisode(which);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.playlist_menu, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playlist_context, menu);
    }

    @Override
    public void onPlaylistEpisodeAdded(int position, Episode episode) {
        playlistItems.add(position, episode);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPlaylistEpisodePositionChanged(int firstPosition, int secondPosition) {
        Episode ep = playlistItems.remove(firstPosition);
        playlistItems.add(secondPosition, ep);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPlaylistEpisodeRemoved(int position) {
        playlistItems.remove(position);
        adapter.notifyDataSetChanged();
    }

    public void removeFromPlaylist(View v) {
        int position = (Integer) v.getTag();
        playlistDAO.removeEpisode(position);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.playlistClear:
                playlistClear();
                break;
            case R.id.playlistDownloadAll:
                playlistDownloadAll();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * Adds all items on the playlist to the downloader
     */
    private void playlistDownloadAll() {
        for (int i = 0; i < playlistItems.size(); i++) {
            try {
                downloadManager.enqueue(playlistItems.get(i));
            } catch (Exception e) {
                // TODO @Joshi show that episodes are being downloaded somehow?
                Log.d(getClass().getName(), "Could not add episode " + i
                        + " on playlist to download manager");
            }
        }
    }

    /**
     * Clears the playlist.
     */
    private void playlistClear() {
        Log.d(getClass().getName(), "Clearing playlist");
        playlistDAO.clearPlaylist();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(getClass().getName(), "List item " + position + " clicked");
        if (service == null) {
            return;
        }
        service.skipToPosition(position);
        service.startPlaying();
    }
}
