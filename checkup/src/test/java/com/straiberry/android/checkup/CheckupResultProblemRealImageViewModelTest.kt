package com.straiberry.android.checkup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupResultProblemRealImageViewModel
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.*

@ExperimentalCoroutinesApi
class CheckupResultProblemRealImageViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private fun createViewModel() =
        CheckupResultProblemRealImageViewModel()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `List of problem positions for upper jaw should updated`() = runTest {
        val viewModel = createViewModel()
        viewModel.setToothWithProblemsUpperJaw(arrayListOf())
        Assert.assertEquals(
            arrayListOf<Pair<Double, Double>>(),
            viewModel.submitStateToothWithProblemUpperJaw.value
        )
    }

    @Test
    fun `List of problem positions for lower jaw should updated`() = runTest {
        val viewModel = createViewModel()
        viewModel.setToothWithProblemsLowerJaw(arrayListOf())
        Assert.assertEquals(
            arrayListOf<Pair<Double, Double>>(),
            viewModel.submitStateToothWithProblemLowerJaw.value
        )
    }

    @Test
    fun `List of problem positions for front jaw should updated`() = runTest {
        val viewModel = createViewModel()
        viewModel.setToothWithProblemsFrontTeeth(arrayListOf())
        Assert.assertEquals(
            arrayListOf<Pair<Double, Double>>(),
            viewModel.submitStateToothWithProblemFrontTeeth.value
        )
    }
}