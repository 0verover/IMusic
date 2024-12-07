package com.realgear.mediaplayer.database;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.realgear.mediaplayer.model.Song;

import java.util.List;

@Dao
public interface LibraryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Song song);

    @Update
    void update(Song song);

    @Delete
    void delete(Song song);

    @Query("DELETE FROM song_table")
    void deleteAllSongs();

    @Query("SELECT * FROM song_table ORDER BY title ASC")
    LiveData<List<Song>> getSongs();

    @Query("SELECT * FROM song_table ORDER BY title ASC")
    DataSource.Factory<Integer, Song> getPagedSongs();

    @Query("SELECT * FROM song_table ORDER BY title ASC")
    PagingSource<Integer, Song> getPagingSource();
}
