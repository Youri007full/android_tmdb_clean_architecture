/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.rodrigobresan.tv.movies;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.rodrigobresan.domain.movie_category.model.Category;
import com.rodrigobresan.presentation.movies.contract.MoviesContract;
import com.rodrigobresan.presentation.movies.model.MovieView;
import com.rodrigobresan.tv.R;
import com.rodrigobresan.tv.movie_details.DetailsActivity;
import com.rodrigobresan.tv.movies.mapper.MovieMapper;
import com.rodrigobresan.tv.movies.model.MovieViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class MoviesFragment extends BrowseFragment implements MoviesContract.View {

    @Inject
    MoviesContract.Presenter presenter;

    @Inject
    MovieMapper movieMapper;

    private static final String TAG = "MoviesFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;
    private static final int NUM_ROWS = 6;
    private static final int NUM_COLS = 15;

    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private String mBackgroundUri;
    private BackgroundManager mBackgroundManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        AndroidInjection.inject(this);
        prepareBackgroundManager();

        setupUIElements();

        setupEventListeners();

        presenter.loadMovies();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

    private void loadMovies(List<MovieViewModel> movieList, Category category) {

        MovieAdapter movieAdapter = new MovieAdapter();
        ArrayObjectAdapter rowAdapter = new ArrayObjectAdapter(movieAdapter);

        for (int currentMovie = 0; currentMovie < movieList.size(); currentMovie++) {
            rowAdapter.add(movieList.get(currentMovie));
        }

        HeaderItem header = new HeaderItem(getNameForCategory(category));
        mRowsAdapter.add(new ListRow(header, rowAdapter));

        setAdapter(mRowsAdapter);
    }

    private String getNameForCategory(Category category) {
        switch (category) {

            case TOP_RATED:
                return "Top Rated";
            case NOW_PLAYING:
                return "Now Playing";
            case UPCOMING:
                return "Upcoming";
            case POPULAR:
                return "Popular";
        }

        throw new IllegalArgumentException();
    }

    private void prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        // setBadgeDrawable(getActivity().getResources().getDrawable(
        // R.drawable.videos_by_google_banner));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    protected void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(getActivity())
                .load(uri)
                .apply(new RequestOptions().centerCrop())
                .into(new SimpleTarget<Drawable>(width, height) {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        mBackgroundManager.setDrawable(resource);
                    }
                });

        // TODO center crop
        mBackgroundTimer.cancel();
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    @Override
    public void showProgress(@NotNull Category category) {

    }

    @Override
    public void hideProgress(@NotNull Category category) {

    }

    @Override
    public void showErrorState(@NotNull Category category) {

    }

    @Override
    public void hideErrorState(@NotNull Category category) {

    }

    @Override
    public void showEmptyState(@NotNull Category category) {

    }

    @Override
    public void hideEmptyState(@NotNull Category category) {

    }

    @Override
    public void showMovies(@NotNull Category category, @NotNull List<MovieView> movies) {
        List<MovieViewModel> movieViewModels = new ArrayList<>();
        for (MovieView currentMovie : movies) {
            movieViewModels.add(movieMapper.mapToViewModel(currentMovie));
        }

        loadMovies(movieViewModels, category);
    }

    @Override
    public void hideMovies(@NotNull Category category) {

    }

    @Override
    public void setPresenter(@NotNull MoviesContract.Presenter presenter) {
        this.presenter = presenter;
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof MovieViewModel) {
                MovieViewModel movie = (MovieViewModel) item;
                Log.d(TAG, "Item: " + item.toString());

                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.MOVIE_ID, movie.getId());

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();

                getActivity().startActivity(intent, bundle);
            } else if (item instanceof String) {
                Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof MovieViewModel) {
                mBackgroundUri = ((MovieViewModel) item).getPosterPath();
                startBackgroundTimer();
            }
        }
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateBackground(mBackgroundUri);
                }
            });
        }
    }

    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(getResources().getColor(R.color.default_background));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }

}
