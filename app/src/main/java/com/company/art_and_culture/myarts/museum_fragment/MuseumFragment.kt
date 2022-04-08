package com.company.art_and_culture.myarts.museum_fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.company.art_and_culture.myarts.CommonAnimations
import com.company.art_and_culture.myarts.Constants
import com.company.art_and_culture.myarts.MainActivity
import com.company.art_and_culture.myarts.R
import com.company.art_and_culture.myarts.databinding.FragmentMuseumBinding
import com.company.art_and_culture.myarts.pojo.Art
import com.company.art_and_culture.myarts.pojo.ArtProvider
import com.company.art_and_culture.myarts.pojo.Maker
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs


class MuseumFragment : Fragment(), View.OnClickListener, View.OnTouchListener{

    private lateinit var fragmentMuseumBinding: FragmentMuseumBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var museumViewModel: MuseumViewModel
    enum class State { Collapsed, Expanded }
    private var appBarState: State = State.Expanded
    private var museum: ArtProvider? = null
    private var artProviderId: String = ""
    private var artMuseumAdapter: ArtMuseumAdapter? = null
    private var artistsAdapter: ArtistsAdapter? = null
    private var museumEventListener: MuseumEventListener? = null
    private var isLabelVisible = false
    private var displayWidth = 0
    private var displayHeight = 0
    private val spanCount = 2

    interface MuseumEventListener {
        fun onArtClickEvent(arts: MutableCollection<Art>, position: Int)
        fun onArtistsClickEvent(maker: Maker)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        fragmentMuseumBinding = FragmentMuseumBinding.inflate(inflater, container, false)

        displayWidth = resources.displayMetrics.widthPixels
        displayHeight = resources.displayMetrics.heightPixels
        preferences = requireActivity().getSharedPreferences(Constants.TAG, 0)
        artProviderId = (requireActivity() as MainActivity).navFragments.artProviderIdForMuseumFragment
        museumEventListener = (requireActivity() as MainActivity).navFragments
        ArtDataInMemory.getInstance().setArtObserver(requireActivity() as MainActivity)

        Injection.init(artProviderId, preferences.getString(Constants.USER_UNIQUE_ID, ""))
        museumViewModel = ViewModelProvider(this, Injection.viewModelFactory)[MuseumViewModel::class.java]
        museumViewModel.setMuseumId(artProviderId)

        initViews()
        initRecyclerView()
        initArtistsRecyclerView()
        setOnBackPressedListener(fragmentMuseumBinding.root)
        initAppBar()

        subscribeObservers()

        return fragmentMuseumBinding.root
    }

    override fun onDestroy() {
        museumViewModel.onDestroy()
        super.onDestroy()
    }

    private fun subscribeObservers() {

        viewLifecycleOwner.lifecycleScope.launch {
            museumViewModel.artsFlow.collectLatest { pagingData ->
                setAnimationRecyclerViewArts()
                artMuseumAdapter?.submitData(pagingData)
            }

        }
        museumViewModel.isLoading.observe(viewLifecycleOwner) { isLoading: Boolean ->
            if (isLoading)
                showProgressBar()
            else
                hideProgressBar()
        }
        museumViewModel.isContentEmpty.observe(viewLifecycleOwner) { isEmpty: Boolean ->
            if (isEmpty)
                hideContent()
            else
                showContent()
        }
        museumViewModel.artProvider.observe(viewLifecycleOwner) { artProvider: ArtProvider? ->
            if (artProvider != null) setMuseumDataInViews(artProvider)
            museum = artProvider
        }
        museumViewModel.isArtProviderLiked.observe(viewLifecycleOwner) { isLiked: Boolean? ->
            if (isLiked == true)
                fragmentMuseumBinding.header.museumLike.setImageResource(R.drawable.ic_favorite_red_100dp)
            else
                fragmentMuseumBinding.header.museumLike.setImageResource(R.drawable.ic_favorite_border_black_100dp)
            fragmentMuseumBinding.header.museumLike.scaleType = ImageView.ScaleType.FIT_CENTER
            val set = AnimatorSet()
            set.playSequentially(
                CommonAnimations.likeFadeIn(fragmentMuseumBinding.header.museumLike),
                CommonAnimations.likeScaleDown(fragmentMuseumBinding.header.museumLike)
            )
            set.start()
        }
        museumViewModel.listMakers.observe(viewLifecycleOwner) { makers: ArrayList<Maker>? ->
            if (makers == null) {
                artistsAdapter?.clearItems()
            } else {
                setAnimationRecyclerViewArtists()
                artistsAdapter?.clearItems()
                artistsAdapter?.setItems(makers)
            }
        }
    }

    private fun initViews() {

        fragmentMuseumBinding.coordinator.visibility = View.GONE

        if (appBarState == State.Expanded) fragmentMuseumBinding.header.label.alpha = 0.0f
                else fragmentMuseumBinding.header.label.alpha = 1.0f

        if (appBarState == State.Expanded) fragmentMuseumBinding.header.museumCloseBtn.alpha = 0.0f
                else fragmentMuseumBinding.header.museumCloseBtn.alpha = 1.0f

        fragmentMuseumBinding.header.museumCloseBtn.setOnClickListener(this)
        fragmentMuseumBinding.header.museumAddress.setOnClickListener(this)
        fragmentMuseumBinding.header.museumPhoneNumber.setOnClickListener(this)
        fragmentMuseumBinding.header.museumWebAddress.setOnClickListener(this)
        fragmentMuseumBinding.header.readMore.setOnClickListener(this)
        fragmentMuseumBinding.header.wikipedia.setOnClickListener(this)
        fragmentMuseumBinding.header.buttonShowTickets.setOnClickListener(this)
        fragmentMuseumBinding.header.museumLike.setOnClickListener(this)
        fragmentMuseumBinding.header.museumShare.setOnClickListener(this)

        fragmentMuseumBinding.header.wikipedia.setOnTouchListener(this)

        fragmentMuseumBinding.swipeRefreshLayout.setOnRefreshListener {
            museumViewModel.refresh()
        }

    }

    private fun initAppBar() {
        fragmentMuseumBinding.museumAppBar.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (abs(verticalOffset) - appBarLayout.totalScrollRange >= 0) {
                //  Collapsed
                if (!isLabelVisible) {
                    fragmentMuseumBinding.header.label.animate().alpha(1.0f).translationY(0f)
                    fragmentMuseumBinding.header.museumCloseBtn.animate().alpha(1.0f).translationY(0f)
                    fragmentMuseumBinding.header.museumCloseBtn.isEnabled = true
                    isLabelVisible = !isLabelVisible
                    appBarState = State.Collapsed
                }
            } else {
                //Expanded
                if (isLabelVisible) {
                    fragmentMuseumBinding.header.label.animate().alpha(0.0f).translationY(-fragmentMuseumBinding.header.label.height.toFloat())
                    fragmentMuseumBinding.header.museumCloseBtn.animate().alpha(0.0f).translationY(-fragmentMuseumBinding.header.label.height.toFloat())
                    fragmentMuseumBinding.header.museumCloseBtn.isEnabled = false
                    isLabelVisible = !isLabelVisible
                    appBarState = State.Expanded
                }
            }

            val fullyExpanded = appBarLayout.height - appBarLayout.bottom == 0
            if(fullyExpanded)
                fragmentMuseumBinding.swipeRefreshLayout.isEnabled = true
            else{
                if (fragmentMuseumBinding.swipeRefreshLayout.isEnabled)
                    fragmentMuseumBinding.swipeRefreshLayout.isEnabled = false
            }


        })
    }

    private fun initRecyclerView() {
        val onArtClickListener = object : ArtMuseumAdapter.OnArtClickListener {
            override fun onArtImageClick(art: Art?, itemPosition: Int) {
                val artInMemory = ArtDataInMemory.getInstance().allData
                museumEventListener!!.onArtClickEvent(artInMemory, itemPosition)
            }
        }

        artMuseumAdapter = ArtMuseumAdapter(displayWidth, displayHeight, spanCount, onArtClickListener, museumViewModel, requireContext())
        val layoutManager: RecyclerView.LayoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
        fragmentMuseumBinding.recyclerViewArt.layoutManager = layoutManager
        fragmentMuseumBinding.recyclerViewArt.adapter = artMuseumAdapter
    }

    private fun initArtistsRecyclerView() {
        val onMakerClickListener =
            ArtistsAdapter.OnMakerClickListener { maker: Maker, position: Int ->
                museumEventListener?.onArtistsClickEvent(maker)
            }
        artistsAdapter = ArtistsAdapter(context, onMakerClickListener, displayWidth, displayHeight)
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        fragmentMuseumBinding.header.recyclerViewArtists.layoutManager = layoutManager
        fragmentMuseumBinding.header.recyclerViewArtists.adapter = artistsAdapter
    }

    private fun setOnBackPressedListener(root: View) {
        //You need to add the following line for this solution to work; thanks skayred
        root.isFocusableInTouchMode = true
        root.requestFocus()
        root.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                var scrollPosition = 0
                if (artMuseumAdapter?.itemCount ?: 0 > 0) scrollPosition = getTargetScrollPosition()
                if (scrollPosition > 4) {
                    fragmentMuseumBinding.recyclerViewArt.smoothScrollToPosition(0)
                    return@OnKeyListener true
                }
                return@OnKeyListener false
            }
            false
        })
    }

    private fun getTargetScrollPosition(): Int {
        return (fragmentMuseumBinding.recyclerViewArt.layoutManager as StaggeredGridLayoutManager).findFirstVisibleItemPositions(null)[0]
    }

    override fun onClick(v: View?) {
        if (v != null) {
            if (v.id == fragmentMuseumBinding.header.museumWebAddress.id) {
                val builder = CustomTabsIntent.Builder()
                builder.setStartAnimations(requireContext(), R.anim.enter_from_right, R.anim.exit_to_left)
                builder.setExitAnimations(requireContext(), R.anim.enter_from_left, R.anim.exit_to_right)
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(requireContext(), Uri.parse(museum?.providerSiteUrl ?: getString(R.string.google)))

            } else if (v.id == fragmentMuseumBinding.header.museumAddress.id) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?q=" + fragmentMuseumBinding.header.museumAddress.text.toString())
                )
                startActivity(intent)

            } else if (v.id == fragmentMuseumBinding.header.museumPhoneNumber.id) {
                val intent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:" + fragmentMuseumBinding.header.museumPhoneNumber.text.toString())
                )
                startActivity(intent)

            } else if (v.id == fragmentMuseumBinding.header.readMore.id) {
                if (fragmentMuseumBinding.header.museumDescription.maxLines == 4) {
                    val paint = Paint()
                    paint.textSize = resources.getDimension(R.dimen.text_size_maker_description)
                    val widthText = paint.measureText(fragmentMuseumBinding.header.museumDescription.text.toString())
                    fragmentMuseumBinding.header.readMore.text = resources.getString(R.string.show_less)
                    val animation = ObjectAnimator.ofInt(
                        fragmentMuseumBinding.header.museumDescription, "maxLines",
                        ((widthText / displayWidth * 1.2) + 10).toInt()
                    )
                    animation.setDuration(600).start()
                } else {
                    val animation = ObjectAnimator.ofInt(fragmentMuseumBinding.header.museumDescription, "maxLines", 4)
                    animation.setDuration(600).start()
                    fragmentMuseumBinding.header.readMore.text = resources.getString(R.string.read_more)
                }

            } else if (v.id == fragmentMuseumBinding.header.wikipedia.id) {
                val builder = CustomTabsIntent.Builder()
                builder.setStartAnimations(requireContext(), R.anim.enter_from_right, R.anim.exit_to_left)
                builder.setExitAnimations(requireContext(), R.anim.enter_from_left, R.anim.exit_to_right)
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(requireContext(), Uri.parse(museum?.providerWikiUrl ?: getString(R.string.google)))

            } else if (v.id == fragmentMuseumBinding.header.buttonShowTickets.id) {
                val builder = CustomTabsIntent.Builder()
                builder.setStartAnimations(requireContext(), R.anim.enter_from_right, R.anim.exit_to_left)
                builder.setExitAnimations(requireContext(), R.anim.enter_from_left, R.anim.exit_to_right)
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(requireContext(), Uri.parse(museum?.providerTicketsUrl ?: getString(R.string.google)))

            } else if (v.id == fragmentMuseumBinding.header.museumCloseBtn.id) {
                (requireActivity() as MainActivity).navFragments.popBackStack()

            } else if (v.id == fragmentMuseumBinding.header.museumShare.id) {
                val set = AnimatorSet()
                set.playSequentially(
                    CommonAnimations.shareScaleUp(fragmentMuseumBinding.header.museumShare),
                    CommonAnimations.shareScaleDown(fragmentMuseumBinding.header.museumShare)
                )
                set.start()
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                val text: String = (museum?.artProvider ?: "") + System.getProperty("line.separator") + (museum?.providerSiteUrl ?: "")
                sendIntent.putExtra(Intent.EXTRA_TEXT, text)
                sendIntent.type = "text/plain"
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)

            } else if (v.id == fragmentMuseumBinding.header.museumLike.id) {
                if(!isNetworkAvailable())
                    Toast.makeText(context, R.string.network_is_unavailable, Toast.LENGTH_SHORT).show()
                else
                    lifecycleScope.launch {
                        museumViewModel.likeMuseum(museum, preferences.getString(Constants.USER_UNIQUE_ID, ""))
                    }
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {

        if (v != null) {
            if (v.id == fragmentMuseumBinding.header.wikipedia.id) {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN ->
                        (v as TextView).setTextColor(resources.getColor(R.color.colorBlack))
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP ->
                        (v as TextView).setTextColor(resources.getColor(R.color.colorText))
                }
            }
        }

        return false
    }

    private fun setAnimationRecyclerViewArtists() {
        val layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fade_in)
        fragmentMuseumBinding.header.recyclerViewArtists.layoutAnimation = layoutAnimationController
        fragmentMuseumBinding.header.recyclerViewArtists.scheduleLayoutAnimation()
    }

    private fun setAnimationRecyclerViewArts() {
        val layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fade_in)
        fragmentMuseumBinding.recyclerViewArt.layoutAnimation = layoutAnimationController
        fragmentMuseumBinding.recyclerViewArt.scheduleLayoutAnimation()
    }

    private fun setMuseumDataInViews(artProvider: ArtProvider) {
        with(fragmentMuseumBinding.header) {
            Picasso.get().load(artProvider.providerHeaderUrl).into(background)
            museumName.text = artProvider.artProvider
            label.text = artProvider.artProvider
            museumCity.text = artProvider.artProviderCity + ", " + artProvider.artProviderCountry
            museumAddress.text = artProvider.providerAddress
            museumPhoneNumber.text = artProvider.providerPhone
            museumWebAddress.text = artProvider.providerSiteUrl
            museumDescription.text = artProvider.providerDescription
            if (artProvider.isLiked)
                museumLike.setImageResource(R.drawable.ic_favorite_red_100dp)
            else
                museumLike.setImageResource(R.drawable.ic_favorite_border_black_100dp)
            museumLike.scaleType = ImageView.ScaleType.FIT_CENTER
        }
    }

    private fun showProgressBar() {
        with(fragmentMuseumBinding) {
            progressBarMuseum.visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        with(fragmentMuseumBinding) {
            progressBarMuseum.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun hideContent() {
        //textView.setVisibility(View.VISIBLE);
        fragmentMuseumBinding.coordinator.visibility = View.GONE;
    }

    private fun showContent() {
        //textView.setVisibility(View.GONE);
        fragmentMuseumBinding.coordinator.visibility = View.VISIBLE
    }

    fun isNetworkAvailable(): Boolean {
        val manager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = manager.activeNetworkInfo
        var isAvailable = false
        if (networkInfo != null && networkInfo.isConnected) {
            // Network is present and connected
            isAvailable = true
        }
        return isAvailable
    }

}