package org.example.mediaplayer;

import java.io.File;
import java.util.ArrayList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MediaPlayerService {


    public static ArrayList<File> getAllSongs(File directory) {
        ArrayList<File> songs = new ArrayList<>();
        if (directory != null) {
            File[] audioFiles = directory.listFiles(file ->
                    file.getName().toLowerCase().endsWith(".mp3") || file.getName().toLowerCase().endsWith(".wav"));

            if (audioFiles != null) {
                for (File audioFile : audioFiles) {
                    songs.add(audioFile);
                }
            }
        }
        return songs;
    }

    public static ArrayList<String> getAllSongs(ArrayList<File> songs) {
        ArrayList<String> songList = new ArrayList<>();
        for (File file : songs) {
            songList.add(file.getName());
        }
        return songList;
    }

    public static MediaPlayer playSong(MediaPlayer oldPlayer, File song) {
        if (oldPlayer != null) {
            oldPlayer.stop();
            oldPlayer.dispose();
        }

        Media media = new Media(song.toURI().toString());
        MediaPlayer newPlayer = new MediaPlayer(media);
        newPlayer.play();

        return newPlayer;
    }
}
