package com.realgear.mediaplayer.database;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.PagingSource;

import com.realgear.mediaplayer.model.Song;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LibraryRepository {
    private LibraryDao m_vLibraryDao;
    private LiveData<List<Song>> m_vSongs;

    public LibraryRepository(Context context) {
        DB db = DB.getInstance(context);

        this.m_vLibraryDao = db.getLibraryDao();
        this.m_vSongs = this.m_vLibraryDao.getSongs();
    }

    public void insert(Song song) {
        Executors.newSingleThreadExecutor().execute(() -> {
            m_vLibraryDao.insert(song);
        });
    }

    public void update(Song song) {
        Executors.newSingleThreadExecutor().execute(() -> {
            m_vLibraryDao.update(song);
        });
    }

    public void delete(Song song) {
        Executors.newSingleThreadExecutor().execute(() -> {
            m_vLibraryDao.delete(song);
        });
    }

    public void deleteAllSongs() {
        Executors.newSingleThreadExecutor().execute(() -> {
            m_vLibraryDao.deleteAllSongs();
        });
    }

    public LiveData<List<Song>> getSongs() {
        return this.m_vSongs;
    }

    public DataSource.Factory<Integer, Song> getPagedSongs() {
        return this.m_vLibraryDao.getPagedSongs();
    }


    public PagingSource<Integer, Song> getPagingSource() {
        return this.m_vLibraryDao.getPagingSource();
    }
}
