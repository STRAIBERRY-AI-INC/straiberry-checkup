package com.straiberry.android.checkup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.straiberry.android.checkup.checkup.domain.model.AddToothToCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.model.AddToothToDentalIssueSuccessModel
import com.straiberry.android.checkup.checkup.domain.usecase.RemoteAddDentalIssueUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.RemoteDeleteDentalIssueUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.RemoteUpdateDentalIssueUseCase
import com.straiberry.android.checkup.checkup.presentation.viewmodel.RemoteDentalIssueViewModel
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
import org.junit.*

@ExperimentalCoroutinesApi
class RemoteDentalIssueViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule(dispatcher)

    @RelaxedMockK
    lateinit var remoteAddDentalIssueUseCase: RemoteAddDentalIssueUseCase

    @RelaxedMockK
    lateinit var remoteDeleteDentalIssueUseCase: RemoteDeleteDentalIssueUseCase

    @RelaxedMockK
    lateinit var remoteUpdateDentalIssueUseCase: RemoteUpdateDentalIssueUseCase

    private fun createViewModel() =
        RemoteDentalIssueViewModel(
            remoteAddDentalIssueUseCase,
            remoteDeleteDentalIssueUseCase,
            remoteUpdateDentalIssueUseCase,
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
    fun `When tooth number is empty, then state should be not loading`() = runTest {
        val viewModel = createViewModel()
        viewModel.remoteAddDentalIssue(toothNumber = "", duration = 0, cause = 0, pain = 0)
        Assert.assertEquals(true, (viewModel.submitStateAddDentalIssue.value is NotLoading))
    }

    @Test
    fun `When a dental issue is not added, then state should be failure`() = runTest {
        coEvery {
            remoteAddDentalIssueUseCase.execute(
                toothNumber = "1",
                duration = 0,
                cause = 0,
                pain = 0
            )
        } throws Exception()

        val viewModel = createViewModel()
        viewModel.remoteAddDentalIssue(toothNumber = "1", duration = 0, cause = 0, pain = 0)
        delay(100)
        Assert.assertEquals(true, (viewModel.submitStateAddDentalIssue.value is Failure))
    }

    @Test
    fun `When dental issue is added, then state should be success`() = runTest {
        coEvery {
            remoteAddDentalIssueUseCase.execute(toothNumber = "", duration = 0, cause = 0, pain = 0)
        } returns AddToothToDentalIssueSuccessModel(ToothId)

        val viewModel = createViewModel()
        viewModel.remoteAddDentalIssue(toothNumber = "1", duration = 0, cause = 0, pain = 0)
        delay(100)
        Assert.assertEquals(true, (viewModel.submitStateAddDentalIssue.value is Success))
    }

    @Test
    fun `When a dental issue didn't updated, then state should be failure`() = runTest {
        coEvery {
            remoteUpdateDentalIssueUseCase.execute(
                toothId = 0,
                toothNumber = "",
                duration = 0,
                cause = 0,
                pain = 0
            )
        } throws Exception()

        val viewModel = createViewModel()
        viewModel.remoteUpdateDentalIssue(
            toothId = 0,
            toothNumber = "1",
            duration = 0,
            cause = 0,
            pain = 0
        )
        delay(100)
        Assert.assertEquals(true, (viewModel.submitStateUpdateDentalIssue.value is Success))
    }

    @Test
    fun `When a dental is updated, then state should be Success`() = runTest {
        coEvery {
            remoteUpdateDentalIssueUseCase.execute(
                toothId = 0,
                toothNumber = "1",
                duration = 0,
                cause = 0,
                pain = 0
            )
        } returns AddToothToDentalIssueSuccessModel(1)

        val viewModel = createViewModel()
        viewModel.remoteUpdateDentalIssue(
            toothId = 0,
            toothNumber = "",
            duration = 0,
            cause = 0,
            pain = 0
        )
        delay(200)
        Assert.assertEquals(true, viewModel.submitStateUpdateDentalIssue.value is Success)
    }

    @Test
    fun `When a dental issue is deleted, then state should be success`() = runTest {
        coEvery {
            remoteDeleteDentalIssueUseCase.execute(ToothId)
        } returns AddToothToCheckupSuccessModel(ToothId)

        val viewModel = createViewModel()
        viewModel.remoteDeleteDentalIssue(ToothId)
        delay(100)
        Assert.assertEquals(true, (viewModel.submitStateDeleteDentalIssue.value is Success))
    }

    @Test
    fun `When a dental issue dos not delete, then state should be failure`() = runTest {
        coEvery {
            remoteDeleteDentalIssueUseCase.execute(ToothId)
        } throws Exception()

        val viewModel = createViewModel()
        viewModel.remoteDeleteDentalIssue(ToothId)
        delay(100)
        Assert.assertEquals(true, (viewModel.submitStateDeleteDentalIssue.value is Failure))
    }

    companion object{
        const val ToothId = 9
    }
}