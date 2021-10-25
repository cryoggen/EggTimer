package com.cryoggen.eggtimer.main

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.cryoggen.eggtimer.R
import com.cryoggen.eggtimer.egg.Egg
import com.cryoggen.eggtimer.receiver.AlarmReceiver
import com.cryoggen.eggtimer.util.cancelNotifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UnspecifiedImmutableFlag")
class EggTimerViewModel(eggTypeNumberInOrder: Int, private val app: Application) :
    AndroidViewModel(app) {
    var eggs = mutableListOf(
        Egg(app.resources.getString(R.string.soft_boiled), R.drawable.scrambled, 10),
        Egg(app.resources.getString(R.string.medium_boiled), R.drawable.medium_timed, 300),
        Egg(app.resources.getString(R.string.hard_boiled), R.drawable.hard_boiled, 540)
    )

    private val TRIGGER_TIME = "TRIGGER_AT$eggTypeNumberInOrder"
    private val REQUEST_CODE = 0 + eggTypeNumberInOrder

    private var prefs =
        app.getSharedPreferences("com.cryoggen.eggtimer", Context.MODE_PRIVATE)

    private var egg: Egg = eggs[eggTypeNumberInOrder]

    private val interval = 1000L * egg.time
    private var triggerTime = 0L
    private val second: Long = 1000L

    private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long>
        get() = _elapsedTime

    private var _timerOn = MutableLiveData<Boolean>()
    val timerOn: LiveData<Boolean>
        get() = _timerOn

    private var _imageEgg = MutableLiveData<Int>()
    val imageEgg: LiveData<Int>
        get() = _imageEgg

    private var _fixTheCurrentEggPage = MutableLiveData<Boolean>()
    val fixTheCurrentEggPage: LiveData<Boolean>
        get() = _fixTheCurrentEggPage

    private lateinit var notifyPendingIntent: PendingIntent
    private val notifyIntent = Intent(app, AlarmReceiver::class.java)
    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private lateinit var timer: CountDownTimer

    init {
        _timerOn.value = PendingIntent.getBroadcast(
            getApplication(),
            REQUEST_CODE,
            notifyIntent,
            PendingIntent.FLAG_NO_CREATE
        ) != null

        if (_timerOn.value!!) {
            startTimer()
        } else {
            _imageEgg.value = egg.picture
            _elapsedTime.value = interval
        }

    }

    fun buttonPressed() {
        when (_timerOn.value) {
            true -> {
                cancelNotification()
                resetTimer()
            }
            false -> {
                cancelNotification()
                createPendingIntent()
                createTimer()
                _timerOn.value = true
            }
        }
    }

    private fun createTimer() {
        triggerTime = SystemClock.elapsedRealtime() + interval
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerTime,
            notifyPendingIntent
        )
        viewModelScope.launch {
            saveTime()
        }
        startTimer()
    }

    private fun startTimer() {
        _fixTheCurrentEggPage.value = false
        _imageEgg.value = R.drawable.cooking
        viewModelScope.launch {
            loadTime()
            timer = object : CountDownTimer(triggerTime, second) {
                override fun onTick(millisUntilFinished: Long) {
                    _elapsedTime.value = triggerTime - SystemClock.elapsedRealtime()
                    if (_elapsedTime.value!! <= 0) {
                        stopTimer()
                    }
                }

                override fun onFinish() {
                    resetTimer()
                }
            }
            timer.start()
        }
    }

    private fun cancelNotification() {
        val notificationManager =
            ContextCompat.getSystemService(
                app,
                NotificationManager::class.java
            ) as NotificationManager
        notificationManager.cancelNotifications()

        createPendingIntent()

        alarmManager.cancel(notifyPendingIntent)
        notifyPendingIntent.cancel()
    }

    private fun createPendingIntent() {
        notifyPendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            REQUEST_CODE,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun stopTimer() {
        _elapsedTime.value = 0
        _imageEgg.value = R.drawable.eggcup
        timer.cancel()
    }

    private fun resetTimer() {
        _elapsedTime.value = interval
        _fixTheCurrentEggPage.value = true
        _imageEgg.value = egg.picture
        _timerOn.value = false
        timer.cancel()
    }

    private suspend fun saveTime() {
        withContext(Dispatchers.IO) {
            prefs.edit().putLong(TRIGGER_TIME, triggerTime).apply()
        }
    }

    private suspend fun loadTime() {
        withContext(Dispatchers.IO) {
            triggerTime = prefs.getLong(TRIGGER_TIME, 0)
        }
    }
}

class EggTimerViewModelFactory(
    private val eggTypeNumberInOrder: Int,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EggTimerViewModel::class.java)) {
            return EggTimerViewModel(eggTypeNumberInOrder, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}