package com.company.art_and_culture.myarts.museum_fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import com.company.art_and_culture.myarts.Constants
import com.company.art_and_culture.myarts.network.MainHttpService
import com.company.art_and_culture.myarts.network.MuseumArtsService
import com.company.art_and_culture.myarts.pojo.Art
import com.company.art_and_culture.myarts.pojo.ArtProvider
import com.company.art_and_culture.myarts.pojo.Maker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MuseumRepository(
    private val artProviderId: String,
    private val userUniqueId: String,
    private val ioDispatcher: CoroutineDispatcher
) : IMuseumRepository {

    private var museumInfoDataSource: MuseumInfoDataSource = MuseumInfoDataSource(MainHttpService.create(), artProviderId, userUniqueId)
    private var museumArtsDataSource: MuseumArtsDataSource = MuseumArtsDataSource(MuseumArtsService.create(), artProviderId, userUniqueId)

    override suspend fun getArtsFlow(artProviderId: String): Flow<PagingData<Art>>
        = withContext(ioDispatcher) {

            museumArtsDataSource = MuseumArtsDataSource(MuseumArtsService.create(), artProviderId, userUniqueId)
            museumInfoDataSource.setMuseumId(artProviderId)

            return@withContext Pager(
                config = PagingConfig(
                    pageSize = Constants.PAGE_SIZE,
                    enablePlaceholders = true
                ),
                pagingSourceFactory = { museumArtsDataSource }
            ).flow
        }

    override fun onDestroy() {
        museumInfoDataSource.onDestroy()
    }

    override fun getIsLoading(): LiveData<Boolean> {
        return museumInfoDataSource.isLoading
    }

    override fun isContentEmpty(): LiveData<Boolean> {
        return museumInfoDataSource.isContentEmpty
    }

    override fun getArtProvider(): MutableLiveData<ArtProvider?> {
        return museumInfoDataSource.artProvider
    }

    override fun getIsArtProviderLiked(): MutableLiveData<Boolean?> {
        return museumInfoDataSource.isArtProviderLiked
    }

    override fun getListMakers(): MutableLiveData<ArrayList<Maker>?> {
        return museumInfoDataSource.listMakers
    }

    override suspend fun writeDimensionsOnServer(art: Art?) =
        withContext(ioDispatcher) {
            museumInfoDataSource.writeDimensionsOnServer(art)
        }

    override suspend fun likeMuseum(museum: ArtProvider?, userUniqueId: String?) =
        withContext(ioDispatcher) {
            museumInfoDataSource.likeMuseum(museum, userUniqueId)
        }


}