package com.straiberry.android.checkup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.straiberry.android.checkup.checkup.domain.model.CheckupHistorySuccessModel
import com.straiberry.android.checkup.checkup.domain.usecase.CheckupSdkHistoryUseCase
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupHistoryViewModel
import com.straiberry.android.core.base.Failure
import com.straiberry.android.core.base.NotLoading
import com.straiberry.android.core.base.Success
import com.straiberry.android.core.network.CoroutineContextProvider
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.any


@ExperimentalCoroutinesApi
class CheckupHistoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule(dispatcher)

    @RelaxedMockK
    lateinit var checkupHistoryUseCase: CheckupSdkHistoryUseCase

    private fun createViewModel() =
        CheckupHistoryViewModel(
            checkupHistoryUseCase,
            CoroutineContextProvider(dispatcher, dispatcher)
        )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `When page is zero, then state should be not loading`() = runTest {
        val viewModel = createViewModel()
        viewModel.checkupHistory(0)
        assertEquals(NotLoading, viewModel.submitStateCheckupHistory.value)
    }

    @Test
    fun `When getting checkup history is success, then state should be success`() =
        runTest {
            coEvery {
                checkupHistoryUseCase.execute(1)
            } returns CheckupHistorySuccessModel(any())

            val viewModel = createViewModel()
            viewModel.checkupHistory(1)
            delay(100)
            assertEquals(true, (viewModel.submitStateCheckupHistory.value is Success))
        }

    @Test
    fun `When getting checkup history is failed, then state should be failure`() = runTest {
        coEvery {
            checkupHistoryUseCase.execute(1)
        } throws Exception()

        val viewModel = createViewModel()
        viewModel.checkupHistory(1)
        delay(100)
        assertEquals(true, (viewModel.submitStateCheckupHistory.value is Failure))
    }
}