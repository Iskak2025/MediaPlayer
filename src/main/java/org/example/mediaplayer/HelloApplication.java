package org.example.mediaplayer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class HelloApplication extends Application {
    private ArrayList<File> songs;
    private ArrayList<String> songNames;
    private String currentTrack = null;
    private MediaPlayer mediaPlayer;
    private int id;

    private Label currentTime;
    private Slider progressSlider;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ListView<String> playlist;

    @Override
    public void start(Stage stage) throws IOException {
        VBox root = new VBox();
        playlist = new ListView<>();
        playlist.setMaxHeight(200);

        Label currentTrackLabel = new Label("Current Track: ");
        Label trackText = new Label(null);
        currentTime = new Label("Current Time: 00:00 / 00:00");

        progressSlider = new Slider();
        progressSlider.setMin(0);
        progressSlider.setMax(100);
        progressSlider.setValue(0);
        progressSlider.setPrefWidth(400);

        progressSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (mediaPlayer != null && !isChanging) {
                mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
            }
        });

        progressSlider.setOnMouseReleased(event -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
            }
        });

        playlist.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                int selectedIndex = playlist.getSelectionModel().getSelectedIndex();
                currentTrack = newValue;
                id = selectedIndex;
                currentTrackLabel.setText("Current Track: " + currentTrack);
            }
        });

        Button loadSongs = new Button("Загрузите песни");
        loadSongs.setOnAction(event -> chooseDirectory(stage, currentTrackLabel));

        Button shuffleButton = new Button("Shuffle: OFF");
        shuffleButton.setOnAction(event -> {
            isShuffle = !isShuffle;
            shuffleButton.setText(isShuffle ? "Shuffle: ON" : "Shuffle: OFF");
        });

        Button repeatButton = new Button("Repeat: OFF");
        repeatButton.setOnAction(event -> {
            isRepeat = !isRepeat;
            repeatButton.setText(isRepeat ? "Repeat: ON" : "Repeat: OFF");
        });

        root.getChildren().addAll(loadSongs, shuffleButton, repeatButton, playlist, currentTrackLabel, trackText, currentTime, progressSlider);

        HBox controlBox = new HBox();

        Button previous = new Button("Prev");
        previous.setStyle("-fx-font-size: 12px; -fx-pref-width: 70px; -fx-pref-height: 35px;");
        previous.setOnAction(event -> {
            if (songs != null && !songs.isEmpty()) {
                id = (id != 0) ? id - 1 : songs.size() - 1;
                highlightCurrentTrack();
                playCurrentSong();
            }
        });

        Button play = new Button("Play");
        play.setStyle("-fx-font-size: 12px; -fx-pref-width: 70px; -fx-pref-height: 35px;");
        play.setOnAction(event -> {
            if (songs != null && !songs.isEmpty()) {
                playCurrentSong();
            }
        });

        Button pause = new Button("Pause");
        pause.setStyle("-fx-font-size: 12px; -fx-pref-width: 70px; -fx-pref-height: 35px;");
        pause.setOnAction(event -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        });

        Button stop = new Button("Stop");
        stop.setStyle("-fx-font-size: 12px; -fx-pref-width: 70px; -fx-pref-height: 35px;");
        stop.setOnAction(event -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        });

        Button next = new Button("Next");
        next.setStyle("-fx-font-size: 12px; -fx-pref-width: 70px; -fx-pref-height: 35px;");
        next.setOnAction(event -> playNextSong());

        controlBox.getChildren().addAll(previous, pause, play, stop, next);
        root.getChildren().add(controlBox);


        Scene scene = new Scene(root, 860, 560);
        stage.setTitle("MediaPlayer");
        stage.setScene(scene);
        stage.show();

        scene.getRoot().requestFocus();

        scene.setOnMouseClicked(e -> scene.getRoot().requestFocus());


        scene.setOnKeyPressed(event -> {
            if (mediaPlayer == null) return;

            if (event.getCode() == KeyCode.SPACE) {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.play();
                }
            } else if (event.getCode() == KeyCode.RIGHT) {
                mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(10)));
            } else if (event.getCode() == KeyCode.LEFT) {
                mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(10)));
            } else if (event.getCode() == KeyCode.N) {
                playNextSong();
            }
        });
    }

    private void chooseDirectory(Stage stage, Label currentTrackLabel) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            songs = MediaPlayerService.getAllSongs(selectedDirectory);
            songNames = MediaPlayerService.getAllSongs(songs);

            playlist.getItems().clear();
            playlist.getItems().addAll(songNames);

            if (!songNames.isEmpty()) {
                id = 0;
                playlist.getSelectionModel().select(id);
                currentTrack = songNames.get(id);
                currentTrackLabel.setText("Current Track: " + currentTrack);
            }
        }
    }

    private void playCurrentSong() {
        if (songs == null || songs.isEmpty()) return;

        mediaPlayer = MediaPlayerService.playSong(mediaPlayer, songs.get(id));
        Media currentMedia = mediaPlayer.getMedia();

        highlightCurrentTrack();

        mediaPlayer.setOnReady(() -> {
            Duration totalDuration = currentMedia.getDuration();
            currentTime.setText("Current Time: 00:00 / " + formatTime(totalDuration));
            progressSlider.setMax(totalDuration.toSeconds());
        });

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> updateProgress());

        mediaPlayer.setOnEndOfMedia(() -> {
            if (isRepeat) {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            } else {
                playNextSong();
            }
        });
    }

    private void updateProgress() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            Duration current = mediaPlayer.getCurrentTime();
            Duration total = mediaPlayer.getMedia().getDuration();
            currentTime.setText("Current Time: " + formatTime(current) + " / " + formatTime(total));
            progressSlider.setValue(current.toSeconds());
        }
    }

    private void highlightCurrentTrack() {
        playlist.getSelectionModel().select(id);
    }

    private void playNextSong() {
        if (songs == null || songs.isEmpty()) return;

        if (isShuffle) {
            int newId;
            do {
                newId = (int) (Math.random() * songs.size());
            } while (songs.size() > 1 && newId == id);
            id = newId;
        } else {
            id = (id < songs.size() - 1) ? id + 1 : 0;
        }

        highlightCurrentTrack();
        playCurrentSong();
    }

    private String formatTime(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static void main(String[] args) {
        launch();
    }
}
