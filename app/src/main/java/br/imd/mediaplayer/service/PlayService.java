package br.imd.mediaplayer.service;

import br.imd.mediaplayer.model.Song;
import uk.co.caprica.vlcj.component.AudioMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by gabriel on 06/12/16.
 */
public class PlayService extends AudioMediaPlayerComponent {

    private Song curSong;
    private Song scheduledSong;

    @Override
    public void playing(MediaPlayer mediaPlayer) {
        super.playing(mediaPlayer);
        if (scheduledSong != null) {
            curSong = scheduledSong;
            curSong.setIsPlaying(true);
            scheduledSong = null;
        }
        notifyPlayingStatusChange(true);
    }

    @Override
    public void paused(MediaPlayer mediaPlayer) {
        super.paused(mediaPlayer);
        notifyPlayingStatusChange(false);
    }

    @Override
    public void stopped(MediaPlayer mediaPlayer) {
        super.finished(mediaPlayer);
        if (curSong != null) curSong.setIsPlaying(false);
        if (scheduledSong !=null) {
            playScheduledSong();
        } else {
            notifyPlayingStatusChange(false);
        }
    }

    @Override
    public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
        super.timeChanged(mediaPlayer, newTime);
        float newProgress = ((float) newTime) / mediaPlayer.getLength();

        notifyProgressChange(newProgress);
    }

    public void playSong(Song song) {
        if (scheduledSong != song) {
            scheduledSong = song;

            if (getMediaPlayer().isPlaying())
                getMediaPlayer().stop();
            else
                playScheduledSong();
        }
    }

    public void pause() {
        if (getMediaPlayer().isPlaying()) {
            getMediaPlayer().pause();

        }
    }

    public void resume() {
        getMediaPlayer().play();
    }

    private void playScheduledSong() {
        curSong = scheduledSong;
        curSong.setIsPlaying(true);
        scheduledSong = null;
        getMediaPlayer().playMedia(curSong.getFilePath());
    }

    private List<MediaPlayerObserver> observers = new LinkedList<>();

    public void addObserver(MediaPlayerObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(MediaPlayerObserver observer) {
        observers.remove(observer);
    }

    private void notifyProgressChange(float newProgress) {
        for (MediaPlayerObserver observer: observers) {
            observer.onProgressChange(newProgress);
        }
    }

    private void notifyPlayingStatusChange(boolean isPlaying) {
        for (MediaPlayerObserver observer: observers) {
            observer.onPlayingStatusChange(isPlaying);
        }
    }



    /// Singleton part ///

    private PlayService() {

        getMediaPlayer().addMediaPlayerEventListener(this);
    }

    private static final PlayService INSTANCE = new PlayService();

    public static final PlayService getInstance() {
        return INSTANCE;
    }

    /// Observer ///

    public interface MediaPlayerObserver {
        void onProgressChange(float progress);
        void onPlayingStatusChange(boolean isPlaying);
    }
}
