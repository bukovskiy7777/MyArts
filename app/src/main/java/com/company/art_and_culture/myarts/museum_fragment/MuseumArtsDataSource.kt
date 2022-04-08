package com.company.art_and_culture.myarts.museum_fragment

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.company.art_and_culture.myarts.Constants
import com.company.art_and_culture.myarts.museum_fragment.MuseumArtsService.Companion.INITIAL_PAGE_NUMBER
import com.company.art_and_culture.myarts.network.NetworkQuery
import com.company.art_and_culture.myarts.network.NetworkQueryKt
import com.company.art_and_culture.myarts.pojo.Art
import com.company.art_and_culture.myarts.pojo.ServerRequest
import retrofit2.HttpException

class MuseumArtsSource(
    private val museumId: String,
    private val userUniqueId: String): PagingSource<Int, Art>() {

    override fun getRefreshKey(state: PagingState<Int, Art>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Art> {
        val pageNumber = params.key ?: INITIAL_PAGE_NUMBER

        val request = ServerRequest()
        request.setPageNumber(pageNumber)
        request.setUserUniqueId(userUniqueId)
        request.setArtProviderId(museumId)
        request.setOldList(ArtDataInMemory.getInstance().allData)
        request.setOperation(Constants.GET_ARTS_LIST_MUSEUM_OPERATION)

        val response = NetworkQueryKt.instance.getMuseumArts(Constants.BASE_URL, request)

        if (response.isSuccessful) {

            val serverResponse = response.body()
            if (serverResponse?.result == Constants.SUCCESS) {

                val arts = serverResponse.listArts
                ArtDataInMemory.getInstance().addData(arts)
                val nextPageNumber = if (arts.isEmpty()) null else pageNumber + 1
                val prevPageNumber = if (pageNumber > 1) pageNumber - 1 else null

                return LoadResult.Page(arts, prevPageNumber, nextPageNumber)
            } else {
                return LoadResult.Error(HttpException(response))
            }
        } else {
            return LoadResult.Error(HttpException(response))
        }

    }


}