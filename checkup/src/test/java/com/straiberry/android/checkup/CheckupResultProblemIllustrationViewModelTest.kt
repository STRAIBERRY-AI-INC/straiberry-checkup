package com.straiberry.android.checkup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupResultProblemIllustrationViewModel
import com.straiberry.android.common.model.JawPosition
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.*

@ExperimentalCoroutinesApi
class CheckupResultProblemIllustrationViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private fun createViewModel() =
        CheckupResultProblemIllustrationViewModel()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `When class is init, then all states should be zero`() = runTest {
        val viewModel = createViewModel()
        Assert.assertEquals(arrayListOf<Int>(), viewModel.submitStateToothWithProblemUpperJaw.value)
        Assert.assertEquals(arrayListOf<Int>(), viewModel.submitStateToothWithProblemLowerJaw.value)
        Assert.assertEquals(
            arrayListOf<Int>(),
            viewModel.submitStateToothWithProblemFrontTeethUpper.value
        )
        Assert.assertEquals(true, viewModel.submitStateToothCurrentFrontTeethPosition.value)
        Assert.assertEquals(false, viewModel.submitStateIsListOfProblemsEmpty.value)
    }

    @Test
    fun `When class resetting as called, then all states should be zero`() = runTest {
        val viewModel = createViewModel()
        viewModel.resetAll()
        Assert.assertEquals(arrayListOf<Int>(), viewModel.submitStateToothWithProblemUpperJaw.value)
        Assert.assertEquals(arrayListOf<Int>(), viewModel.submitStateToothWithProblemLowerJaw.value)
        Assert.assertEquals(
            arrayListOf<Int>(),
            viewModel.submitStateToothWithProblemFrontTeethUpper.value
        )
        Assert.assertEquals(true, viewModel.submitStateToothCurrentFrontTeethPosition.value)
        Assert.assertEquals(false, viewModel.submitStateIsListOfProblemsEmpty.value)
    }


    @Test
    fun `When jaw position is changed, then its state should be updated`() = runTest {
        val viewModel = createViewModel()
        viewModel.setCurrentPosition(JawPosition.LowerJaw)
        Assert.assertEquals(JawPosition.LowerJaw, viewModel.submitStateToothCurrentPosition.value)
    }

    @Test
    fun `When lower jaw is selected, then its state should be updated with related tooth index`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.setToothWithProblemsLowerJaw(arrayListOf())
            Assert.assertEquals(
                arrayListOf<Int>(),
                viewModel.submitStateToothWithProblemLowerJaw.value
            )
        }

    @Test
    fun `When upper jaw is selected, then its state should be updated with related tooth index`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.setToothWithProblemsUpperJaw(arrayListOf())
            Assert.assertEquals(
                arrayListOf<Int>(),
                viewModel.submitStateToothWithProblemUpperJaw.value
            )
        }

    @Test
    fun `When front-lower jaw is selected, then its state should be updated with related tooth index`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.setToothWithProblemsFrontTeethLower(arrayListOf())
            Assert.assertEquals(
                arrayListOf<Int>(),
                viewModel.submitStateToothWithProblemFrontTeethLower.value
            )
        }

    @Test
    fun `When front-upper jaw is selected, then its state should be updated with related tooth index`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.setToothWithProblemsFrontTeethUpper(arrayListOf())
            Assert.assertEquals(
                arrayListOf<Int>(),
                viewModel.submitStateToothWithProblemFrontTeethUpper.value
            )
        }

    @Test
    fun `When user switches between front jaws in illustration mode, then its state should be updated`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.switchBetweenFrontJaws(JawPosition.FrontTeethLower)
            Assert.assertEquals(
                JawPosition.FrontTeethLower,
                viewModel.submitStateSwitchBetweenFrontJaws.value
            )
        }

    @Test
    fun `When list of problems is empty, then its state should be true`() = runTest {
        val viewModel = createViewModel()
        viewModel.setListOfProblemsEmpty(true)
        Assert.assertEquals(true, viewModel.submitStateIsListOfProblemsEmpty.value)
    }

    @Test
    fun `When list of problems is not empty, then its state should be false`() = runTest {
        val viewModel = createViewModel()
        viewModel.setListOfProblemsEmpty(false)
        Assert.assertEquals(false, viewModel.submitStateIsListOfProblemsEmpty.value)
    }
}