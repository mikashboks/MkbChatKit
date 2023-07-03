package com.stfalcon.chatkit.sample.features.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.stfalcon.chatkit.sample.R
import com.stfalcon.chatkit.sample.features.demo.custom.holder.CustomHolderDialogsActivity
import com.stfalcon.chatkit.sample.features.demo.custom.layout.CustomLayoutDialogsActivity
import com.stfalcon.chatkit.sample.features.demo.custom.media.CustomMediaMessagesActivity
import com.stfalcon.chatkit.sample.features.demo.def.DefaultDialogsActivity
import com.stfalcon.chatkit.sample.features.demo.styled.StyledDialogsActivity
import com.stfalcon.chatkit.sample.features.main.adapter.DemoCardFragment.OnActionListener
import com.stfalcon.chatkit.sample.features.main.adapter.MainActivityPagerAdapter
import me.relex.circleindicator.CircleIndicator

/*
 * Created by troy379 on 04.04.17.
 */
class MainActivity : AppCompatActivity(), OnActionListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pager = findViewById<ViewPager>(R.id.pager)
        pager.adapter = MainActivityPagerAdapter(
            this,
            supportFragmentManager
        )
        pager.pageMargin =
            resources.getDimension(R.dimen.card_padding)
                .toInt() / 4
        pager.offscreenPageLimit = 3
        val indicator =
            findViewById<CircleIndicator>(R.id.indicator)
        indicator.setViewPager(pager)
    }

    override fun onAction(id: Int) {
        when (id) {
            MainActivityPagerAdapter.ID_DEFAULT -> DefaultDialogsActivity.open(
                this
            )

            MainActivityPagerAdapter.ID_STYLED -> StyledDialogsActivity.open(
                this
            )

            MainActivityPagerAdapter.ID_CUSTOM_LAYOUT -> CustomLayoutDialogsActivity.open(
                this
            )

            MainActivityPagerAdapter.ID_CUSTOM_VIEW_HOLDER -> CustomHolderDialogsActivity.open(
                this
            )

            MainActivityPagerAdapter.ID_CUSTOM_CONTENT -> CustomMediaMessagesActivity.open(
                this
            )
        }
    }
}