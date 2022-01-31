package com.haselab.myservice

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity() : AppCompatActivity(), Parcelable {

	constructor(parcel: Parcel) : this() {
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val fromNotification = intent.getBooleanExtra("fromNotification", false)

		if(fromNotification) {
			val btPlay = findViewById<Button>(R.id.btPlay)
			val btStop = findViewById<Button>(R.id.btStop)
			btPlay.isEnabled = false
			btStop.isEnabled = true
		}
	}

	fun onPlayButtonClick(view: View) {
		val intent = Intent(this@MainActivity, SampleService::class.java)

		startService(intent)

		val btPlay = findViewById<Button>(R.id.btPlay)
		val btStop = findViewById<Button>(R.id.btStop)
		btPlay.isEnabled = false
		btStop.isEnabled = true
	}

	fun onStopButtonClick(view: View) {
		val intent = Intent(this@MainActivity, SampleService::class.java)

		stopService(intent)

		val btPlay = findViewById<Button>(R.id.btPlay)
		val btStop = findViewById<Button>(R.id.btStop)
		btPlay.isEnabled = true
		btStop.isEnabled = false
	}

	override fun writeToParcel(parcel: Parcel, flags: Int) {

	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<MainActivity> {
		override fun createFromParcel(parcel: Parcel): MainActivity {
			return MainActivity(parcel)
		}

		override fun newArray(size: Int): Array<MainActivity?> {
			return arrayOfNulls(size)
		}
	}
}
