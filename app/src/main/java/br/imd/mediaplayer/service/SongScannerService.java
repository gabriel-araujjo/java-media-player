package br.imd.mediaplayer.service;

import br.imd.mediaplayer.dao.DAO;
import br.imd.mediaplayer.dao.DataObserver;
import br.imd.mediaplayer.model.Directory;
import br.imd.mediaplayer.model.Song;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Song scanner service.
 */
public class SongScannerService implements DataObserver<Directory> {

    private static SongScannerService INSTANCE = new SongScannerService();

    public static SongScannerService getInstance() {
        return INSTANCE;
    }

    private boolean running = false;

    private HashMap<Path, DirectoryWatcher> watchers = new HashMap<>();

    private SongScannerService() { }

    @Override
    public void onInsert(Directory model) {
        System.out.println(String.format("Model insertion observer on SongScannerService %s", model.getPath()));
        Path path = Paths.get(model.getPath());
        watchers.put(path, new DirectoryWatcher(model));
    }

    @Override
    public void onRemove(Directory model) {

    }

    @Override
    public void onUpdate(Directory model) {

    }

    public synchronized void start() {
        if (running) return;
        running = true;
        DAO.forModel(Directory.class).addObserver(this);
        DAO.forModel(Directory.class).list().forEach(dir -> {
            Path dirPath = Paths.get(dir.getPath());
            watchers.put(dirPath, new DirectoryWatcher(dir));
        });
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        DAO.forModel(Directory.class).removeObserver(this);
        for (DirectoryWatcher watcher : watchers.values()) {
            watcher.stop();
        }
        watchers.clear();
    }

    private static class DirectoryWatcher implements Runnable {

        Directory directory;
        Path path;
        WatchService service;

        DirectoryWatcher(Directory dir) {
            directory = dir;
            path = Paths.get(dir.getPath());

            try {
                service = path.getFileSystem().newWatchService();
                path.register(service, ENTRY_CREATE, ENTRY_MODIFY);
                Executors.newCachedThreadPool().submit(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            System.out.println(String.format("Watching %s dir for modifications", path.toAbsolutePath().toUri().toString()));
            scanDirectoryIfNecessary();
            WatchKey key;
            try {
                while ((key = service.take()) != null) {
                    System.out.println("Dir watch event");
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();


                        // We know that the event is instance of WatchEvent<Path>, because the kind is ENTRY_CREATE
                        // or ENTRY_DELETE or ENTRY_MODIFY
                        @SuppressWarnings("unchecked") Path changedPath = path.resolve(((WatchEvent<Path>) event).context());

                        changedPath.toFile();
                        if (isSong(changedPath)) {
                            if (ENTRY_CREATE == kind) {
                                Song song = new Song();
                                song.setFilePath(changedPath.toString());
                                Platform.runLater(() -> DAO.forModel(Song.class).insert(song));

                                System.out.println("adding song " + changedPath);
                            } else if (ENTRY_MODIFY == kind) {
                                Song song = new Song(changedPath);
                                Platform.runLater(() -> DAO.forModel(Song.class).update(song));

                                System.out.println("modified song " + changedPath);
                            }
                        }
                    }

                    if (!key.reset()) break;
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }

        void stop() {
            try {
                service.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void scanDirectoryIfNecessary() {
            if (mustScan()) {
                File dirFile = new File(directory.getPath());
                File[] songs = dirFile.listFiles(this::isSong);
                if (songs != null) {
                    System.out.println("Found songs = " + songs.length);
                    Arrays.stream(songs).parallel().forEach(songFile -> {
                        if (songFile.lastModified() > directory.getLastModified()) {
                            Song song = new Song(songFile);
                            if (DAO.forModel(Song.class).find(song) == null) {
                                DAO.forModel(Song.class).insert(song);
                            } else {
                                DAO.forModel(Song.class).update(song);
                            }
                        }
                    });
                    directory.setLastModified(new File(directory.getPath()).lastModified());
                }
            }
        }

        boolean mustScan() {
            File f = new File(directory.getPath());
            return f.lastModified() > directory.getLastModified();
        }

        boolean isSong(File file) {
            try {
                return isSong(file.toPath());
            } catch (IOException e) {
                return false;
            }
        }

        boolean isSong(Path path) throws IOException {
            return Files.probeContentType(path).equals("audio/mpeg");
        }
    }
}
