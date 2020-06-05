package com.shafigh.easyq.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.shafigh.easyq.fragments.MapFragment

class MapsFragmenAdapter(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        return MapFragment()

    }

    override fun getCount(): Int {
        return 3
    }
}