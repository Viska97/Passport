package org.visapps.passport.ui.activity

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import org.visapps.passport.R

import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.content_about.*
import org.visapps.passport.util.toVisibility
import android.content.Intent
import android.net.Uri

class AboutActivity : AppCompatActivity() {

    private val GITHUB_URI = "https://github.com/Viska97/Passport"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        githubcard.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URI))
            startActivity(browserIntent)
        }
        try {
            val versionName =
                applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).versionName
            version.text = getString(R.string.version, versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            version.visibility = toVisibility(false)
        }
    }

}
