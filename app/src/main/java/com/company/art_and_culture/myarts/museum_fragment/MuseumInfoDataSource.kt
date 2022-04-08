package com.company.art_and_culture.myarts.museum_fragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import com.company.art_and_culture.myarts.Constants
import com.company.art_and_culture.myarts.network.MainHttpService
import com.company.art_and_culture.myarts.pojo.*
import kotlinx.coroutines.runBlocking

class MuseumInfoDataSource(
    private val mainHttpService: MainHttpService,
    private var artProviderId: String,
    private val userUniqueId: String
) {

    var artProvider = MutableLiveData<ArtProvider?>()
        private set
    var listMakers = MutableLiveData<ArrayList<Maker>?>()
        private set
    var isLoading = MutableLiveData<Boolean>()
        private set
    var isArtProviderLiked = MutableLiveData<Boolean?>()
        private set
    var isContentEmpty = MutableLiveData<Boolean>()
        private set

    fun setMuseumId(artProviderId: String) {
        isLoading.postValue(true)
        this.artProviderId = artProviderId

        runBlocking {
            loadMuseumInfo()
            loadMakersList()
        }
    }

    fun refresh() {
        isLoading.postValue(true)

        runBlocking {
            loadMuseumInfo()
            loadMakersList()
        }
    }

    private suspend fun loadMuseumInfo() {

        val request = ServerRequest()
        request.setOperation(Constants.GET_MUSEUM_INFO_OPERATION)
        request.setUserUniqueId(userUniqueId)
        request.setArtProviderId(artProviderId)

        try {
            val response = mainHttpService.mainRequest(request)

            if (response.isSuccessful) {
                val serverResponse = response.body()
                if (serverResponse?.result == Constants.SUCCESS) {

                    isLoading.postValue(false)
                    isContentEmpty.postValue(false)
                    artProvider.postValue(serverResponse.artProvider)

                } else { }
            } else { }
        } catch (e: Exception) {
            isLoading.postValue(false)
        }
    }

    private suspend fun loadMakersList() {

        val request = ServerRequest()
        request.setOperation(Constants.GET_MUSEUM_MAKERS_LIST_OPERATION)
        request.setUserUniqueId(userUniqueId)
        request.setArtProviderId(artProviderId)

        try {
            val response = mainHttpService.mainRequest(request)
            if (response.isSuccessful) {
                val serverResponse = response.body()
                if (serverResponse?.result == Constants.SUCCESS) {

                    listMakers.postValue(serverResponse.listMakers)

                } else { }

            } else { }
        } catch (e: Exception) { }
    }

    suspend fun likeMuseum(museum: ArtProvider?, userUniqueId: String?) {

        val request = ServerRequest()
        request.setUserUniqueId(userUniqueId)
        request.setOperation(Constants.MUSEUM_LIKE_OPERATION)
        request.setArtProvider(museum)

        try {
            val response = mainHttpService.mainRequest(request)
            if (response.isSuccessful) {
                val serverResponse = response.body()
                if (serverResponse?.result == Constants.SUCCESS) {

                    isArtProviderLiked.postValue(serverResponse.artProvider.isLiked)

                } else { }
            } else { }

        } catch (e: Exception) { }
    }

    suspend fun writeDimensionsOnServer(art: Art?) {
        val request = ServerRequest()
        request.setOperation(Constants.ART_WRITE_DIMENS_OPERATION)
        request.setArt(art)

        try {
            val response = mainHttpService.mainRequest(request)
        } catch (e: Exception) { }

    }

    fun onDestroy() {
        artProvider.postValue(null)
        isArtProviderLiked.postValue(null)
        listMakers.postValue(null)
        isLoading.postValue(true)
        isContentEmpty.postValue(true)
    }


}
