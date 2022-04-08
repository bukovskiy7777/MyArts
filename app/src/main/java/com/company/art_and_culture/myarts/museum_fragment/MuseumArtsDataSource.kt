package com.company.art_and_culture.myarts.museum_fragment

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.company.art_and_culture.myarts.Constants
import com.company.art_and_culture.myarts.network.MuseumArtsService
import com.company.art_and_culture.myarts.network.MuseumArtsService.Companion.INITIAL_PAGE_NUMBER
import com.company.art_and_culture.myarts.pojo.Art
import com.company.art_and_culture.myarts.pojo.ServerRequest
import retrofit2.HttpException
import java.io.IOException

class MuseumArtsDataSource(
    private val museumArtsService: MuseumArtsService,
    private val artProviderId: String,
    private val userUniqueId: String
): PagingSource<Int, Art>() {

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
        request.setArtProviderId(artProviderId)
        request.setOldList(ArtDataInMemory.getInstance().allData)
        request.setOperation(Constants.GET_ARTS_LIST_MUSEUM_OPERATION)

        try {
            val response = museumArtsService.getMuseumArts(request)
            if (response.isSuccessful) {

                val serverResponse = response.body()
                if (serverResponse?.result == Constants.SUCCESS) {

                    val arts = serverResponse.listArts
                    ArtDataInMemory.getInstance().addData(arts)
                    val nextPageNumber = if (arts.isEmpty()) null else pageNumber + 1
                    val prevPageNumber = if (pageNumber > 1) pageNumber - 1 else null
                    return LoadResult.Page(ArtDataInMemory.getInstance().getPageData(pageNumber), prevPageNumber, nextPageNumber)

                } else {
                    return LoadResult.Error(Exception(serverResponse?.message ?: "server error"))
                }
            } else {
                return LoadResult.Error(Exception(response.message()))
            }
        }catch (exception: IOException) {
            return LoadResult.Error(Exception(exception.message))
        } catch (exception: HttpException) {
            return LoadResult.Error(Exception(exception.message))
        }

    }


}