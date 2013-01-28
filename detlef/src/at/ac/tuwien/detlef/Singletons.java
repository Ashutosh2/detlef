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



package at.ac.tuwien.detlef;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import at.ac.tuwien.detlef.db.CachingEpisodeDAO;
import at.ac.tuwien.detlef.db.CachingPodcastDAO;
import at.ac.tuwien.detlef.db.EpisodeActionDAO;
import at.ac.tuwien.detlef.db.EpisodeActionDAOImpl;
import at.ac.tuwien.detlef.db.EpisodeDAO;
import at.ac.tuwien.detlef.db.PlaylistDAO;
import at.ac.tuwien.detlef.db.PlaylistDAOImpl;
import at.ac.tuwien.detlef.db.PodcastDAO;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.download.DetlefDownloadManager;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.gpodder.responders.SynchronousSyncResponder;
import at.ac.tuwien.detlef.settings.ConnectionTester;
import at.ac.tuwien.detlef.settings.ConnectionTesterGpodderNet;
import at.ac.tuwien.detlef.settings.DeviceIdGenerator;
import at.ac.tuwien.detlef.settings.DeviceIdGeneratorRandom;
import at.ac.tuwien.detlef.settings.DeviceRegistrator;
import at.ac.tuwien.detlef.settings.DeviceRegistratorGpodderNet;
import at.ac.tuwien.detlef.settings.GpodderSettings;
import at.ac.tuwien.detlef.settings.GpodderSettingsDAO;
import at.ac.tuwien.detlef.settings.GpodderSettingsDAOAndroid;

/**
 * This class acts as a central point for setting and retrieving service
 * classes.
 *
 * @author johannes
 */
public class Singletons {

    private static Singletons dependencyAssistant = null;

    private DetlefDownloadManager downloadManager = null;
    private GPodderSync gPodderSync = null;
    private PodcastDAO podcastDAO = null;
    private EpisodeDAO episodeDAO = null;
    private EpisodeActionDAO episodeActionDAO = null;
    private PlaylistDAO playlistDAO = null;

    /**
     * Lazy initialization to both improve performance and avoid issues getting the
     * application context in unit tests.
     *
     * @return Gets the quasi-singleton GPodderSync instance for this program.
     */
    public GPodderSync getGPodderSync() {
        if (gPodderSync == null) {
            gPodderSync = new GPodderSync(new SynchronousSyncResponder(
                                              Detlef.getAppContext()));
        }
        return gPodderSync;
    }

    /**
     * Returns the download manager instance.
     */
    public synchronized DetlefDownloadManager getDownloadManager(Context context) {
        if (downloadManager == null) {
            downloadManager = new DetlefDownloadManager(context);
        }
        return downloadManager;
    }

    /**
     * @param context The {@link Context}. This is needed in order to be able to
     *            access Android's system settings. Usually this will be the
     *            current {@link Activity}.
     * @return Gets the {@link GpodderSettings gpodder.net settings instance}
     *         that provides the user name, password and device name settings.
     */
    public GpodderSettings getGpodderSettings(Context context) {
        return getGpodderSettingsDAO(context).getSettings();
    }

    /**
     * The DAO class that is used to store and retrieve {@link GpodderSettings}.
     * @param context The current {@link Context}.
     * @return an implementation of {@link GpodderSettingsDAO}.
     * @throws IllegalArgumentException In case context is null.
     */
    public GpodderSettingsDAO getGpodderSettingsDAO(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }

        HashMap<String, Object> dependencies = new HashMap<String, Object>();
        dependencies.put("sharedPreferences",
                         PreferenceManager.getDefaultSharedPreferences(context));

        GpodderSettingsDAO gpodderSettingsDAO = new GpodderSettingsDAOAndroid();
        gpodderSettingsDAO.setDependencies(dependencies);

        return gpodderSettingsDAO;
    }

    /**
     * @return The {@link DeviceRegistrator} that is able to register a device.
     */
    public DeviceRegistrator getDeviceRegistrator() {
        return new DeviceRegistratorGpodderNet();
    }

    /**
     * @return The {@link ConnectionTester} that verifies a set of of
     *         {@link GpodderSettings}.
     */
    public ConnectionTester getConnectionTester() {
        return new ConnectionTesterGpodderNet();
    }

    /**
     * @return The static instance.
     */
    public static Singletons i() {
        if (dependencyAssistant == null) {
            dependencyAssistant = new Singletons();
        }
        return dependencyAssistant;
    }

    /**
     * Overwrites the default {@link Singletons} with a custom one. By
     * using this method you can easily replace parts of the Application with
     * Mocks which is useful for testing.
     *
     * @param pDependencyAssistant
     */
    public static void setDependencyAssistant(Singletons pDependencyAssistant) {
        dependencyAssistant = pDependencyAssistant;
    }

    /**
     * Convenience Method that calls {@link #getGpodderSettings(Context)} with
     * {@link Detlef#getAppContext()} as Context.
     * @return {@value Singletons#getGpodderSettings(Context)}
     */
    public GpodderSettings getGpodderSettings() {
        return getGpodderSettings(Detlef.getAppContext());
    }

    public DeviceIdGenerator getDeviceIdGenerator() {
        return new DeviceIdGeneratorRandom();
    }

    /**
     * @return The DAO that is used to crud {@link Podcast Podcasts}.
     */
    public PodcastDAO getPodcastDAO() {
        if (podcastDAO == null) {
            podcastDAO  = new CachingPodcastDAO(Detlef.getAppContext());
        }
        return podcastDAO;
    }

    public EpisodeDAO getEpisodeDAO() {
        if (episodeDAO == null) {
            episodeDAO = new CachingEpisodeDAO(Detlef.getAppContext());
        }
        return episodeDAO;
    }

    public EpisodeActionDAO getEpisodeActionDAO() {
        if (episodeActionDAO == null) {
            episodeActionDAO = new EpisodeActionDAOImpl(Detlef.getAppContext());
        }
        return episodeActionDAO;
    }

    public PlaylistDAO getPlaylistDAO() {
        if (playlistDAO == null) {
            playlistDAO = new PlaylistDAOImpl(Detlef.getAppContext());
        }
        return playlistDAO;
    }
}
