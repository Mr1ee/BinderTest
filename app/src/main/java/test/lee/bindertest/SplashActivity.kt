package test.lee.bindertest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) //满屏显示
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        startActivity(Intent(this, BookServiceActivity::class.java))
    }
}
