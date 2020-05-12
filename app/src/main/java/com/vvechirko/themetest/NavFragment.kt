package com.vvechirko.themetest

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment

class NavFragment : Fragment() {

    companion object {
        fun instance(light: Boolean = true) = NavFragment().apply {
            arguments = Bundle().also {
                it.putBoolean("b", light)
            }
        }
    }

    private var b: Boolean = false

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = arguments?.getBoolean("b") ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val theme = if (b) R.style.LightTheme else R.style.DarkTheme
        val localInflater = inflater.cloneInContext(ContextThemeWrapper(activity, theme))
        return localInflater.inflate(R.layout.fragment_nav, container, false)
    }
}