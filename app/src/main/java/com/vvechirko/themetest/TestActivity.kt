package com.vvechirko.themetest

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val b = getPreferences(Context.MODE_PRIVATE).getBoolean("b", false)
        val theme = if (b) R.style.LightTheme else R.style.DarkTheme
        setTheme(theme)
        setWindowLightStatusBar(b)
        setContentView(R.layout.activity_test)
        setSupportActionBar(toolbar)

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            val b = getPreferences(Context.MODE_PRIVATE).getBoolean("b", false)
            if (b != isChecked) {
                getPreferences(Context.MODE_PRIVATE).edit().putBoolean("b", isChecked).apply()
                recreate()
            }
        }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun setWindowLightStatusBar(b: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = window.decorView.systemUiVisibility
            flags = if (b) flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            else flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

            window.decorView.systemUiVisibility = flags
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val typedValue = TypedValue()
            theme.resolveAttribute(R.attr.statusBarCompat, typedValue, true)
            val statusBarCompat = typedValue.data
            window.statusBarColor = statusBarCompat
        }
    }
}