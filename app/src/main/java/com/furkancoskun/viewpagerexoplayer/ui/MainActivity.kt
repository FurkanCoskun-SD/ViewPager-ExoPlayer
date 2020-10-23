package com.furkancoskun.viewpagerexoplayer.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.furkancoskun.viewpagerexoplayer.model.Movie
import com.furkancoskun.viewpagerexoplayer.R
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val movies: MutableList<Movie> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Make activity full screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        movies.add(
            Movie(
                "Big Buck Bunny",
                "https://www.radiantmediaplayer.com/media/big-buck-bunny-360p.mp4"
            )
        )
        movies.add(
            Movie(
                "Elephants Dream",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
            )
        )
        movies.add(
            Movie(
                "For Bigger Fun",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
            )
        )
        movies.add(
            Movie(
                "Sintel",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
            )
        )
        movies.add(
            Movie(
                "Subaru Outback On Street And Dirt",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"
            )
        )
        initPager(movies)
    }

    private fun initPager(movie: List<Movie>) {
        viewPager.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return PlayerFragment.create(
                    movie[position]
                )
            }

            override fun getItemCount(): Int {
                return movie.size
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = movie[position].title
        }.attach()
    }


}