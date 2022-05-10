package com.straiberry.android.checkup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.straiberry.android.checkup.checkup.domain.model.AddToothToCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.usecase.AddSeveralTeethToCheckupUseCase
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupQuestionSubmitTeethViewModel
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
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SubmitCheckupQuestionViewModelTest {
    private val dispatcher = TestCoroutineDispatcher()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule(dispatcher)

    @RelaxedMockK
    lateinit var addSeveralTeethToCheckupUseCase: AddSeveralTeethToCheckupUseCase

    private fun createViewModel() =
        CheckupQuestionSubmitTeethViewModel(addSeveralTeethToCheckupUseCase,
            CoroutineContextProvider(dispatcher,dispatcher)
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
        viewModel.addTeethToCheckup("", ArrayList())
        assertEquals(NotLoading, viewModel.submitStateAddTooth.value)
    }

    @Test
    fun `When checkup is not empty, then state should be loading`() = runTest {
        val viewModel = createViewModel()
        viewModel.submitStateAddTooth.observeForever {
            viewModel.addTeethToCheckup("test", ArrayList())
            assertEquals(Loading, it)
        }
    }

    @Test
    fun `When teeth added to checkup successfully, then sate should be success`(){
        coEvery {
            addSeveralTeethToCheckupUseCase.execute("test", ArrayList())
        } returns AddToothToCheckupSuccessModel(1)

        val viewModel = createViewModel()
        viewModel.addTeethToCheckup("test", ArrayList())
        assertEquals(true, (viewModel.submitStateAddTooth.value is Success))
    }

    @Test
    fun `When adding teeth to checkup is fail, then sate should be failure`(){
        coEvery {
            addSeveralTeethToCheckupUseCase.execute("test", ArrayList())
        } throws Exception()

        val viewModel = createViewModel()
        viewModel.addTeethToCheckup("test",ArrayList())
        assertEquals(true, (viewModel.submitStateAddTooth.value is Failure))
    }
}