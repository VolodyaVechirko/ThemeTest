package com.vvechirko.themetest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LIFEEEEEE","MainActivity onCreate")
        setContentView(R.layout.activity_main)
        navView.setOnNavigationItemSelectedListener(this)
        switchFragment(F1())
    }

    override fun onStart() {
        super.onStart()
        Log.d("LIFEEEEEE","MainActivity onStart")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("LIFEEEEEE","MainActivity onRestart")
    }

    override fun onStop() {
        super.onStop()
        Log.d("LIFEEEEEE","MainActivity onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LIFEEEEEE","MainActivity onDestroy")
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_home -> switchFragment(F1())
            R.id.navigation_dashboard -> switchFragment(F2())
            R.id.navigation_notifications -> switchFragment(F3())
        }
        return true
    }

    fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    class F1 : Fragment() {
        override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
            i.inflate(R.layout.fragment1, c, false)
    }

    class F2 : Fragment() {
        override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
            i.inflate(R.layout.fragment2, c, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val d = TextDrawable(view.context, "why I am EUR")
            view.findViewById<EditText>(R.id.editText)
                .setCompoundDrawablesWithIntrinsicBounds(null, null, d, null)
        }
    }

    class F3 : Fragment() {
        override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
            i.inflate(R.layout.fragment3, c, false)
    }
}