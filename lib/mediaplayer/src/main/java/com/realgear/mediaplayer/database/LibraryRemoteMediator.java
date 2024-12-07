package com.realgear.mediaplayer.database;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.paging.ListenableFutureRemoteMediator;
import androidx.paging.LoadType;
import androidx.paging.PagingState;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.realgear.mediaplayer.model.Song;

import java.util.concurrent.Executor;

@SuppressLint("UnsafeOptInUsageError")
public class LibraryRemoteMediator extends ListenableFutureRemoteMediator<Integer, Song> {

    private String mQuery;
    private DB mDatabase;
    private LibraryDao mLibraryDao;
    private Executor mBackgroundExecutor;

    public LibraryRemoteMediator(String query, DB database, LibraryDao libraryDao, Executor backgroundExecutor) {
        this.mQuery = query;
        this.mDatabase = database;
        this.mLibraryDao = libraryDao;
        this.mBackgroundExecutor = backgroundExecutor;
    }

    @NonNull
    @Override
    public ListenableFuture<MediatorResult> loadFuture(@NonNull LoadType loadType, @NonNull PagingState<Integer, Song> pagingState) {
        Long loadKey = null;

        switch (loadType) {
            case REFRESH:
                break;

            case PREPEND:
                return Futures.immediateFuture(new MediatorResult.Success(true));

            case APPEND:
                Song lastItem = pagingState.lastItemOrNull();

                if (lastItem == null) {
                    return Futures.immediateFuture(new MediatorResult.Success(true));
                }

                loadKey = lastItem.getId();
        }

        //ListenableFuture<MediatorResult>

        return null;
    }
}
