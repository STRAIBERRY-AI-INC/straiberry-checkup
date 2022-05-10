package com.straiberry.android.checkup

import android.app.Application
import io.paperdb.Paper

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Init paper db
        Paper.init(this)
    }
}