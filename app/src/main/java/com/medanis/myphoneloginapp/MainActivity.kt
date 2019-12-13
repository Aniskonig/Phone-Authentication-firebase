package com.medanis.myphoneloginapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
verification_btn.setOnClickListener {
    val intent = Intent(this, Login::class.java)
    startActivity(intent)

}

    }
}
