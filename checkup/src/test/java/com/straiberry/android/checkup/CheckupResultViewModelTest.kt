package com.straiberry.android.checkup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupHistorySuccessResponse
import com.straiberry.android.checkup.checkup.domain.model.CheckupResultSuccessModel
import com.straiberry.android.checkup.checkup.domain.usecase.GetCheckupResultUseCase
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupResultViewModel
import com.straiberry.android.common.base.Failure
import com.straiberry.android.common.base.Loading
import com.straiberry.android.common.base.NotLoading
import com.straiberry.android.common.base.Success
import com.straiberry.android.common.network.CoroutineContextProvider
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.*

@ExperimentalCoroutinesApi
class CheckupResultViewModelTest {
    private val dispatcher = TestCoroutineDispatcher()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule(dispatcher)

    @RelaxedMockK
    lateinit var getCheckupResultUseCase: GetCheckupResultUseCase

    private fun createViewModel() =
        CheckupResultViewModel(
            getCheckupResultUseCase,
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
    fun `When checkup id is empty, then state should be not loading`() = runTest {
        val viewModel = createViewModel()
        viewModel.checkupResult("")
        Assert.assertEquals(NotLoading, viewModel.submitStateCheckupResult.value)
    }

    @Test
    fun `When checkup id is not empty, then state should be loading`() = runTest {
        val viewModel = createViewModel()
        viewModel.submitStateCheckupResult.observeForever {
            viewModel.checkupResult("1")
            Assert.assertEquals(Loading, viewModel.submitStateCheckupResult.value)
        }
    }

    @Test
    fun `When getting checkup result is success, then state should be success`() =
        runBlockingTest {
            coEvery {
                getCheckupResultUseCase.execute("1")
            } returns CheckupResultSuccessModel(CheckupHistorySuccessResponse.Data())

            val viewModel = createViewModel()
            viewModel.checkupResult("1")
            Assert.assertEquals(true, (viewModel.submitStateCheckupResult.value is Success))
        }

    @Test
    fun `When getting checkup result is failed, then state should be failure`() = runTest {
        coEvery {
            getCheckupResultUseCase.execute("1")
        } throws Exception()

        val viewModel = createViewModel()
        viewModel.checkupResult("1")
        Assert.assertEquals(true, (viewModel.submitStateCheckupResult.value is Failure))
    }
}