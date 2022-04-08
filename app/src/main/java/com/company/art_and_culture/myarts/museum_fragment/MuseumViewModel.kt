package com.company.art_and_culture.myarts.museum_fragment

import androidx.lifecycle.*
import androidx.paging.*
import com.company.art_and_culture.myarts.pojo.Art
import com.company.art_and_culture.myarts.pojo.ArtProvider
import com.company.art_and_culture.myarts.pojo.Maker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class MuseumViewModel (
    private val repository: IMuseumRepository
    ) : ViewModel() {

    var artsFlow: Flow<PagingData<Art>>

    var isLoading: LiveData<Boolean> = repository.getIsLoading()
    var isContentEmpty: LiveData<Boolean> = repository.isContentEmpty()
    var artProvider: LiveData<ArtProvider?> = repository.getArtProvider()
    var isArtProviderLiked: LiveData<Boolean?> = repository.getIsArtProviderLiked()
    var listMakers: LiveData<ArrayList<Maker>?> = repository.getListMakers()

    private val museumId = MutableLiveData("")

    init {
        artsFlow = museumId.asFlow().flatMapLatest { repository.getArtsFlow(it)  }.cachedIn(viewModelScope)
    }

    fun setMuseumId(artProviderId: String) {
        if (this.museumId.value == artProviderId) return
        this.museumId.value = artProviderId
    }

    fun refresh() {
        ArtDataInMemory.getInstance().refresh()
        this.museumId.postValue(this.museumId.value)
    }

    fun onDestroy() {
        ArtDataInMemory.getInstance().refresh()
        repository.onDestroy()
    }

    suspend fun likeMuseum(museum: ArtProvider?, userUniqueId: String?) {
        return repository.likeMuseum(museum, userUniqueId)
    }

    suspend fun writeDimensionsOnServer(art: Art?) {
        repository.writeDimensionsOnServer(art)
    }

}



@Suppress("UNCHECKED_CAST")
class ViewModelFactory (
    private val repository: IMuseumRepository): ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T = MuseumViewModel(repository) as T
    }


object Injection {

    private var artProviderId: String = ""
    private var userUniqueId: String = ""

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    fun init (artProviderId: String?, userUniqueId: String?) {
        if (artProviderId != null) this.artProviderId = artProviderId
        if (userUniqueId != null) this.userUniqueId = userUniqueId
    }

    private val repository: IMuseumRepository by lazy { MuseumRepository(artProviderId, userUniqueId, ioDispatcher) }

    val viewModelFactory: ViewModelFactory by lazy { ViewModelFactory(repository) }
}