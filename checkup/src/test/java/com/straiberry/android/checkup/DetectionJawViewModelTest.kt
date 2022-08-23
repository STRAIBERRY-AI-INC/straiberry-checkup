package com.straiberry.android.checkup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.straiberry.android.checkup.checkup.presentation.viewmodel.DetectionJawViewModel
import com.straiberry.android.common.model.JawPosition
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.*

@ExperimentalCoroutinesApi
class DetectionJawViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private fun createViewModel() =
        DetectionJawViewModel()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `When class is init, then states should be default values`() = runTest {
        val viewModel = createViewModel()
        Assert.assertEquals(hashMapOf<Int, JawPosition>(), viewModel.submitStateSelectedJaws.value)
        Assert.assertEquals(true, viewModel.stateDetectionModel.value)
        Assert.assertEquals(0, viewModel.statePhotosUploaded.value)
    }

    @Test
    fun `When selected jaws are resetting, then state should an empty hashmap`() = runTest {
        val viewModel = createViewModel()
        viewModel.resetSelectedJaw()
        Assert.assertEquals(hashMapOf<Int, JawPosition>(), viewModel.submitStateSelectedJaws.value)
    }

    @Test
    fun `When user select all jaws for checkup(jaw index is 4), then state should include all jaws`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.setSelectedJaw(AllJawsAreSelected)

            val hashMapOfAllSelectedJaws =
                hashMapOf(
                    Pair(FrontIndex, JawPosition.FrontTeeth),
                    Pair(UpperIndex, JawPosition.UpperJaw), Pair(LowerIndex, JawPosition.LowerJaw)
                )

            Assert.assertEquals(hashMapOfAllSelectedJaws, viewModel.submitStateSelectedJaws.value)

        }

    @Test
    fun `When number of uploaded jaws is resetting, then state should be zero`() = runTest {
        val viewModel = createViewModel()
        viewModel.resetNumberOfUploadedJaw()
        Assert.assertEquals(0, viewModel.statePhotosUploaded.value)
    }

    @Test
    fun `When checkup photos uploaded successfully, then state should plus one`() = runTest {
        val viewModel = createViewModel()
        viewModel.photosUploaded(FrontIndex)
        Assert.assertEquals(1, viewModel.stateListOfUploadedJaws.value!!.size)
    }

    @Test
    fun `When checkup photos uploaded fails, then state should mines one`() = runTest {
        val viewModel = createViewModel()
        viewModel.photosUploadedFailed(FrontIndex)
        Assert.assertEquals(0, viewModel.stateListOfUploadedJaws.value!!.size)
    }

    companion object {
        private const val FrontIndex = 1
        private const val UpperIndex = -1
        private const val LowerIndex = 0
        private const val AllJawsAreSelected = 4
    }
}