package com.rodrigobresan.tv.injection.module

import com.rodrigobresan.data.connection.ConnectionStatus
import com.rodrigobresan.domain.movie_detail.interactor.GetMovieDetails
import com.rodrigobresan.domain.movies.interactor.FavoriteMovie
import com.rodrigobresan.domain.movies.interactor.UnfavoriteMovie
import com.rodrigobresan.domain.review.interactor.GetReviews
import com.rodrigobresan.presentation.movie_details.contract.MovieDetailsContract
import com.rodrigobresan.presentation.movie_details.mapper.MovieDetailsMapper
import com.rodrigobresan.presentation.movie_details.presenter.MovieDetailsPresenter
import com.rodrigobresan.tv.injection.scope.PerActivity
import com.rodrigobresan.tv.movie_details.MovieDetailsFragment
import dagger.Module
import dagger.Provides


@Module
open class MovieDetailsModule {

    @PerActivity
    @Provides
    internal fun provideMovieDetailsView(moviesView: MovieDetailsFragment): MovieDetailsContract.View {
        return moviesView
    }

    @PerActivity
    @Provides
    internal fun provideMovieDetailsPresenter(
            connectionStatus: ConnectionStatus,
            movieDetailsView: MovieDetailsContract.View,
            getMovieDetails: GetMovieDetails,
            favoriteMovie: FavoriteMovie,
            unfavoriteMovie: UnfavoriteMovie,
            getReviews: GetReviews,
            movieMapper: MovieDetailsMapper): MovieDetailsContract.Presenter {

        return MovieDetailsPresenter(connectionStatus, movieDetailsView, getMovieDetails,
                favoriteMovie, unfavoriteMovie, getReviews, movieMapper)
    }
}