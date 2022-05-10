package com.straiberry.android.checkup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class CoroutineTestRule(private val coroutineDispatcher: TestCoroutineDispatcher) : TestWatcher() {
    override fun finished(description: Description?) {
        super.finished(description)
        coroutineDispatcher.cleanupTestCoroutines()
        Dispatchers.resetMain()
    }

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(coroutineDispatcher)
    }
}
