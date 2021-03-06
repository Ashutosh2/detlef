package at.ac.tuwien.detlef.gpodder.plumbing;

import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.plumbing.ParcelableByteArray;

/**
 * Interface for the {@link PodderService} to contact you.
 */
oneway interface PodderServiceCallback
{
    /**
     * The service is still alive.
     * @param reqId The request ID you passed.
     */
    void heartbeatSucceeded(int reqId);

    /**
     * The authentication check succeeded.
     * @param reqId The request ID you passed.
     */
    void authCheckSucceeded(int reqId);

    /**
     * Authentication with a gpodder.net-compatible service failed.
     *
     * Can be called by any operation placing requests to a gpodder.net-compatible service,
     * including authCheck and downloadPodcastList.
     *
     * @param reqId The request ID you passed.
     * @param errCode Error code (see {@link PodderService.ErrorCode}) describing the type of
     * error that occurred.
     * @param errStr Error message describing the error that occurred.
     */
    void gponetLoginFailed(int reqId, int errCode, in String errStr);

    /**
     * The podcast list has been fetched successfully.
     * @param reqId The request ID you passed.
     * @param podcasts List of podcasts as fetched from server.
     */
    void downloadPodcastListSucceeded(int reqId, in List<String> podcasts);

    /**
     * The podcast list download failed.
     * @param reqId The request ID you passed.
     * @param errCode Error code (see {@link PodderService.ErrorCode}) describing the type of
     * error that occurred.
     * @param errStr Error message describing the error that occurred.
     */
    void downloadPodcastListFailed(int reqId, int errCode, in String errStr);

    /**
     * The changes to the subscriptions have been downloaded successfully.
     * @param reqId The request ID you passed.
     * @param chgs The changes as fetched from server.
     */
    void downloadChangesSucceeded(int reqId, in EnhancedSubscriptionChanges chgs);

    /**
     * The subscription changes download failed.
     * @param reqId The request ID you passed.
     * @param errCode Error code (see {@link PodderService.ErrorCode}) describing the type of
     * error that occurred.
     * @param errStr Error message describing the error that occurred.
     */
    void downloadChangesFailed(int reqId, int errCode, in String errStr);

    /**
     * The podcast search has completed successfully.
     * @param reqId The request ID you passed.
     * @param results The matching podcasts.
     */
    void searchPodcastsSucceeded(int reqId, in List<Podcast> results);

    /**
     * The podcast search has failed.
     * @param reqId The request ID you passed.
     * @param errCode Error code (see {@link PodderService.ErrorCode}) describing the type of
     * error that occurred.
     * @param errStr Error message describing the error that occurred.
     */
    void searchPodcastsFailed(int reqId, int errCode, in String errStr);

    /**
     * The toplist retrieval has completed successfully.
     * @param reqId The request ID you passed.
     * @param results The matching podcasts.
     */
    void getToplistSucceeded(int reqId, in List<Podcast> results);

    /**
     * The toplist retrieval has failed.
     * @param reqId The request ID you passed.
     * @param errCode Error code (see {@link PodderService.ErrorCode}) describing the type of
     * error that occurred.
     * @param errStr Error message describing the error that occurred.
     */
    void getToplistFailed(int reqId, int errCode, in String errStr);

    /**
     * The suggestion retrieval has completed successfully.
     * @param reqId The request ID you passed.
     * @param results The matching podcasts.
     */
    void getSuggestionsSucceeded(int reqId, in List<Podcast> results);

    /**
     * The suggestion retrieval has failed.
     * @param reqId The request ID you passed.
     * @param errCode Error code (see {@link PodderService.ErrorCode}) describing the type of
     * error that occurred.
     * @param errStr Error message describing the error that occurred.
     */
    void getSuggestionsFailed(int reqId, int errCode, in String errStr);

    /**
     * The subscription update has completed successfully.
     * @param reqId The request ID you passed.
     */
    void updateSubscriptionsSucceeded(int reqId, long timestamp);

    /**
     * The subscription update has failed.
     * @param reqId The request ID you passed.
     * @param errCode Error code (see {@link PodderService.ErrorCode}) describing the type of
     * error that occurred.
     * @param errStr Error message describing the error that occurred.
     */
    void updateSubscriptionsFailed(int reqId, int ioProblem, in String message);
    
    /**
     * The get podcast info call has succeeded
     * @param reqId The request ID you passed
     * @param Podcast The podcast that was the result
     */
    void getPodcastInfoSucceeded(int reqId, in Podcast result);
    
    /**
     * The get podcast info has failed.
     * @param reqId The request ID you passed.
     * @param errCode Error code (see {@link PodderService.ErrorCode}) describing the type of
     * error that occurred.
     * @param errStr Error message describing the error that occurred.
     */
    void getPodcastInfoFailed(int reqId, int ioProblem, in String message);

}
