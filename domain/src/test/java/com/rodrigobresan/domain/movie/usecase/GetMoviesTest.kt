package com.rodrigobresan.domain.movie.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.rodrigobresan.domain.base.executor.PostExecutionThread
import com.rodrigobresan.domain.base.executor.ThreadExecutor
import com.rodrigobresan.domain.movie_category.model.Category
import com.rodrigobresan.domain.movies.interactor.GetMovies
import com.rodrigobresan.domain.movies.model.Movie
import com.rodrigobresan.domain.movies.repository.MovieRepository
import com.rodrigobresan.domain.test.factory.MovieFactory
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Class for testing GetMovies use case class
 */
@RunWith(JUnit4::class)
class GetMoviesTest {

    private lateinit var getMovies: GetMovies
    private lateinit var movieRepository: MovieRepository
    private lateinit var threadExecutor: ThreadExecutor
    private lateinit var postExecutionThread: PostExecutionThread

    @Before
    fun setUp() {
        movieRepository = mock()
        threadExecutor = mock()
        postExecutionThread = mock()
        getMovies = GetMovies(movieRepository, threadExecutor, postExecutionThread)
    }

    @Test
    fun buildCaseObservableCallsMoviesRepository() {
        getMovies.buildUseCaseObservable(Category.POPULAR)
        verify(movieRepository).getMovies(any())
    }

    @Test(expected = IllegalArgumentException::class)
    fun buildUseCaseObservableThrowsException() {
        getMovies.buildUseCaseObservable(null)
    }

    @Test
    fun buildUseCaseObservableCompletes() {
        stubMovieRepositoryGetMovies(Single.just(MovieFactory.makeMovieList(2)))

        val testObservable = getMovies.buildUseCaseObservable(Category.POPULAR).test()
        testObservable.assertComplete()
    }

    @Test
    fun buildUseCaseObservableReturnsData() {
        val movies = MovieFactory.makeMovieList(2)
        stubMovieRepositoryGetMovies(Single.just(movies))

        val testObserver = getMovies.buildUseCaseObservable(Category.POPULAR).test()
        testObserver.assertResult(movies)
    }

    private fun stubMovieRepositoryGetMovies(singleMovieList: Single<List<Movie>>?) {
        whenever(movieRepository.getMovies(any()))
                .thenReturn(singleMovieList)
    }
}