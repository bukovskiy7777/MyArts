package com.company.art_and_culture.myarts.museum_fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import androidx.paging.PagingData
import com.company.art_and_culture.myarts.pojo.Art
import com.company.art_and_culture.myarts.pojo.ArtProvider
import com.company.art_and_culture.myarts.pojo.Maker
import kotlinx.coroutines.flow.Flow

interface IMuseumRepository {

    fun getIsLoading(): LiveData<Boolean>
    fun isContentEmpty(): LiveData<Boolean>

    suspend fun getArtsFlow(artProviderId: String): Flow<PagingData<Art>>
    fun getArtProvider(): MutableLiveData<ArtProvider?>
    fun getListMakers(): MutableLiveData<ArrayList<Maker>?>
    fun getIsArtProviderLiked(): MutableLiveData<Boolean?>

    suspend fun writeDimensionsOnServer(art: Art?)
    suspend fun likeMuseum(museum: ArtProvider?, userUniqueId: String?)

    fun onDestroy()

}