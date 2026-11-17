package com.example.memodeck

import android.app.Application
import com.example.memodeck.data.AppDatabase

class MemoDeckApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}