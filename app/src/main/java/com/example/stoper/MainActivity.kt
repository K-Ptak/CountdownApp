package com.example.stoper

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private var seconds: Long = 0L
    private var isRunning: Boolean = false

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private lateinit var timer: Job

    companion object {
        private const val TIME_KEY = "time_Key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        seconds = savedInstanceState?.getLong(TIME_KEY) ?: 0L
        initView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(TIME_KEY, seconds)
    }


    private fun initView() {
        val addMinute = findViewById<ImageButton>(R.id.minute_add)
        val minusMinute = findViewById<ImageButton>(R.id.minute_minus)
        val addSecond = findViewById<ImageButton>(R.id.second_add)
        val minusSecond = findViewById<ImageButton>(R.id.second_minus)


        val start = findViewById<Button>(R.id.start)
        val pause = findViewById<Button>(R.id.pause)
        val stop = findViewById<Button>(R.id.stop)


        if(seconds <= 0) {
            start.isEnabled = false
            pause.isEnabled = false
            stop.isEnabled = false
        } else {
            updateButtonsOnView()
        }

        updateTimeOnView()

        addMinute.setOnClickListener { if (!isRunning) addMinute() }
        minusMinute.setOnClickListener { if (!isRunning) minusMinute() }

        addSecond.setOnClickListener { if (!isRunning) addSecond() }
        minusSecond.setOnClickListener { if (!isRunning) minusSecond() }

        start.setOnClickListener {
            if (isRunning) return@setOnClickListener
            isRunning = true
            stop.isEnabled = true
            updateButtonsOnView()
            timer = startCountdown()
            timer.start()
        }

        pause.setOnClickListener {
            if (!isRunning) return@setOnClickListener
            isRunning = false
            stop.isEnabled = true
            updateButtonsOnView()
            if (::timer.isInitialized) {
                timer.cancel()
            }
        }

        stop.setOnClickListener {
            isRunning = false
            updateButtonsOnView()
            start.isEnabled = false
            stop.isEnabled = false
            if (::timer.isInitialized) {
                timer.cancel()
            }
            if (seconds != 0L) {
                resetTime()
            }
        }
    }

    private fun startCountdown(): Job =
        scope.launch(context = Dispatchers.IO, start = CoroutineStart.LAZY) {
            while (isActive) {
                if (seconds <= 0) {
                    isRunning = false
                    withContext(Main) {
                        updateButtonsOnView()
                        onCountdownFinish()
                    }
                    timer.cancel()
                }
                delay(1000)
                withContext(Main) {
                    minusSecond()
                }
            }
        }

    private fun updateTimeOnView() {
        val minute1 = findViewById<TextView>(R.id.minute_1)
        val minute2 = findViewById<TextView>(R.id.minute_2)
        val second1 = findViewById<TextView>(R.id.second_1)
        val second2 = findViewById<TextView>(R.id.second_2)

        val (minutes, seconds) = formatTime(seconds)
        minute1.text = minutes[0].toString()
        minute2.text = minutes[1].toString()
        second1.text = seconds[0].toString()
        second2.text = seconds[1].toString()

    }

    private fun updateButtonsOnView() {
        val start = findViewById<Button>(R.id.start)
        val pause = findViewById<Button>(R.id.pause)

        start.isEnabled = !isRunning
        pause.isEnabled = isRunning
    }

    private fun resetTime() {
        seconds = 0
        updateTimeOnView()
    }

    private fun reenableStart() {
        val start = findViewById<Button>(R.id.start)
        start.isEnabled = seconds >= 1 && !isRunning
    }

    private fun addMinute() {
        if (seconds >= 60 * 60) return
        seconds += 60
        reenableStart()
        updateTimeOnView()
    }

    private fun minusMinute() {
        if (seconds < 60) return
        seconds -= 60
        reenableStart()
        updateTimeOnView()
    }

    private fun addSecond() {
        if (seconds > 60 * 60 + 59) return
        seconds += 1
        reenableStart()
        updateTimeOnView()
    }

    private fun minusSecond() {
        if (seconds <= 0) return
        seconds -= 1
        reenableStart()
        updateTimeOnView()
    }

    private fun formatTime(time: Long): Pair<String, String> {
        val minutes = time / 60
        val remainingSeconds = time % 60

        return Pair("%02d".format(minutes), "%02d".format(remainingSeconds))
    }

    private fun onCountdownFinish() {
        Toast.makeText(this, "Finished!", Toast.LENGTH_SHORT).show()
        val start = findViewById<Button>(R.id.start)
        val stop = findViewById<Button>(R.id.stop)
        start.isEnabled = false
        stop.isEnabled = false
    }
}
