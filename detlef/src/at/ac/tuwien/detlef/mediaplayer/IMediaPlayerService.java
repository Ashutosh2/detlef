
package at.ac.tuwien.detlef.mediaplayer;

import at.ac.tuwien.detlef.domain.Episode;

public interface IMediaPlayerService {

    // TODO @Joshi how to test these methods? Need episode with downloaded audio
    // file.

    /**
     * @return The current media player position.
     */
    int getCurrentPosition();

    /**
     * @return Returns the duration of the currently played piece.
     */
    int getDuration();

    /**
     * @return Returns if the player is currently playing or paused/stopped.
     */
    boolean isCurrentlyPlaying();

    IMediaPlayerService pausePlaying();

    /**
     * Sets the media player progress.
     * 
     * @param progress The progress to set the media player to.
     */
    IMediaPlayerService seekTo(int progress);

    /**
     * Starts the next piece from the playlist.
     * 
     * @return this
     */
    IMediaPlayerService fastForward();

    /**
     * Starts playback of the active episode (if any). Otherwise, gets the first
     * episode from the playlist and starts on that. If there is no active
     * episode and the playlist is empty, does nothing.
     */
    IMediaPlayerService startPlaying();

    /**
     * @return Whether the player service has a currently active episode (paused
     *         or not). This becomes false when a file has been played to its
     *         end.
     */
    boolean hasRunningEpisode();

    /**
     * Sets the episode that should be prepared and played on the next call to
     * startPlaying().
     * 
     * @param ep The episode to be played next
     * @return this
     */
    IMediaPlayerService setNextEpisode(Episode ep);

    /**
     * Gets the episode that should be prepared and played on the next call to
     * startPlaying().
     * 
     * @return thiThe episode to be played next
     */
    Episode getNextEpisode();

    /**
     * Checks if the episode file path exists and points to a valid file.
     * 
     * @param ep The episode whose file path to check
     * @return this
     */
    boolean episodeFileOK(Episode ep);

}