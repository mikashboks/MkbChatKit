package com.stfalcon.chatkit.sample.features.main.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.stfalcon.chatkit.sample.R

/*
 * Created by troy379 on 11.04.17.
 */
class MainActivityPagerAdapter(
    private val context: Context,
    fm: FragmentManager?
) : FragmentStatePagerAdapter(
    fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {
    override fun getItem(position: Int): Fragment {
        var title: String? = null
        var description: String? = null
        when (position) {
            ID_DEFAULT -> {
                title =
                    context.getString(R.string.sample_title_default)
                description =
                    context.getString(R.string.sample_subtitle_default)
            }

            ID_STYLED -> {
                title =
                    context.getString(R.string.sample_title_attrs)
                description =
                    context.getString(R.string.sample_subtitle_attrs)
            }

            ID_CUSTOM_LAYOUT -> {
                title =
                    context.getString(R.string.sample_title_layout)
                description =
                    context.getString(R.string.sample_subtitle_layout)
            }

            ID_CUSTOM_VIEW_HOLDER -> {
                title =
                    context.getString(R.string.sample_title_holder)
                description =
                    context.getString(R.string.sample_subtitle_holder)
            }

            ID_CUSTOM_CONTENT -> {
                title =
                    context.getString(R.string.sample_title_custom_content)
                description =
                    context.getString(R.string.sample_subtitle_custom_content)
            }
        }
        return DemoCardFragment.newInstance(
            position,
            title,
            description
        )
    }

    override fun getCount(): Int {
        return 5
    }

    companion object {
        const val ID_DEFAULT = 0
        const val ID_STYLED = 1
        const val ID_CUSTOM_LAYOUT = 2
        const val ID_CUSTOM_VIEW_HOLDER = 3
        const val ID_CUSTOM_CONTENT = 4
    }
}