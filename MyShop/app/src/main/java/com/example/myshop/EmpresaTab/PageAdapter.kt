package com.example.myshop.EmpresaTab

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class PageAdapter constructor(fm: FragmentManager, private var count: Int) :
    FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> return HomeFragment()
            1 -> return ProductoFragment()
            2 -> return UpdateFragment()
        }
        return HomeFragment()

    }

    override fun getCount(): Int = count
}