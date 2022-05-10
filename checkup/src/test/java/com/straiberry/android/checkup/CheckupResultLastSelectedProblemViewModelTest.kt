package com.straiberry.android.checkup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupResultLastSelectedProblemViewModel
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.*

@ExperimentalCoroutinesApi
class CheckupResultLastSelectedProblemViewModelTest {
    private val dispatcher = TestCoroutineDispatcher()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule(dispatcher)

    private fun createViewModel() =
        CheckupResultLastSelectedProblemViewModel()

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
        Assert.assertEquals(0, viewModel.submitStateSelectedProblemFrontUpperIllustration.value)
        Assert.assertEquals(0, viewModel.submitStateSelectedProblemFrontLowerIllustration.value)
        Assert.assertEquals(0, viewModel.submitStateSelectedProblemUpperIllustration.value)
        Assert.assertEquals(0, viewModel.submitStateSelectedProblemLowerIllustration.value)
        Assert.assertEquals(0, viewModel.submitStateSelectedProblemLowerRealImage.value)
        Assert.assertEquals(0, viewModel.submitStateSelectedProblemUpperRealImage.value)
        Assert.assertEquals(0, viewModel.submitStateSelectedProblemFrontRealImage.value)
    }

    @Test
    fun `When reset all is called, then all states should be zero`() = runTest {
        val viewModel = createViewModel()
        viewModel.resetAll()
        Assert.assertEquals(0, viewModel.submitStateSelectedProblemFrontUpperIllustration.value)
        Assert.assertEquals(0, viewModel.submitStateSelectedProblemFrontLowerIllustration.value)
        Assert.assertEquals(0, viewModel.submitStateSelectedProblemUpperIllustration.value)
        Assert.assertEquals(0, viewModel.submitStateSelectedProblemLowerIllustration.value)
        Assert.assertEquals(0, viewModel.submitStateSelectedProblemLowerRealImage.value)
        Assert.assertEquals(0, viewModel.submitStateSelectedProblemUpperRealImage.value)
        Assert.assertEquals(0, viewModel.submitStateSelectedProblemFrontRealImage.value)
    }

    @Test
    fun `When a problem is selected from front-upper in illustration mode, then its state should be updated`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.setSelectedProblemFrontUpperIllustration(1)
            Assert.assertEquals(1, viewModel.submitStateSelectedProblemFrontUpperIllustration.value)
        }

    @Test
    fun `When a problem is selected from front-lower in illustration mode, then its state should be updated`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.setSelectedProblemFrontLowerIllustration(1)
            Assert.assertEquals(1, viewModel.submitStateSelectedProblemFrontLowerIllustration.value)
        }

    @Test
    fun `When a problem is selected from lower in illustration mode, then its state should be updated`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.setSelectedProblemLowerIllustration(1)
            Assert.assertEquals(1, viewModel.submitStateSelectedProblemLowerIllustration.value)
        }

    @Test
    fun `When a problem is selected from upper in illustration mode, then its state should be updated`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.setSelectedProblemUpperIllustration(1)
            Assert.assertEquals(1, viewModel.submitStateSelectedProblemUpperIllustration.value)
        }


    @Test
    fun `When a problem is selected from front in real-image mode, then its state should be updated`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.setSelectedProblemFrontRealImage(1)
            Assert.assertEquals(1, viewModel.submitStateSelectedProblemFrontRealImage.value)
        }

    @Test
    fun `When a problem is selected from lower in real-image mode, then its state should be updated`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.setSelectedProblemLowerRealImage(1)
            Assert.assertEquals(1, viewModel.submitStateSelectedProblemLowerRealImage.value)
        }

    @Test
    fun `When a problem is selected from upper in real-image mode, then its state should be updated`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.setSelectedProblemUpperRealImage(1)
            Assert.assertEquals(1, viewModel.submitStateSelectedProblemUpperRealImage.value)
        }
}