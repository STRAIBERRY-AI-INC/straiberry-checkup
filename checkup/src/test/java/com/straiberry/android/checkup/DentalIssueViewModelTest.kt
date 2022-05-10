package com.straiberry.android.checkup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.straiberry.android.checkup.checkup.domain.model.DentalIssueQuestionsModel
import com.straiberry.android.checkup.checkup.domain.usecase.AddDentalIssueUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.DeleteDentalIssueUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.GetAllDentalIssueUseCase
import com.straiberry.android.checkup.checkup.presentation.viewmodel.DentalIssuesViewModel
import com.straiberry.android.common.base.ReadableFailure
import com.straiberry.android.common.base.ReadableSuccess
import com.straiberry.android.common.network.CoroutineContextProvider
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.*

@ExperimentalCoroutinesApi
class DentalIssueViewModelTest {
    private val dispatcher = TestCoroutineDispatcher()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule(dispatcher)

    @RelaxedMockK
    lateinit var addDentalIssueUseCase: AddDentalIssueUseCase

    @RelaxedMockK
    lateinit var getAllDentalIssueUseCase: GetAllDentalIssueUseCase

    @RelaxedMockK
    lateinit var deleteDentalIssueUseCase: DeleteDentalIssueUseCase

    private fun createViewModel() =
        DentalIssuesViewModel(
            addDentalIssueUseCase,
            getAllDentalIssueUseCase,
            deleteDentalIssueUseCase,
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
    fun `When a dental issue is added, then state should be success`() = runTest {
        coEvery {
            addDentalIssueUseCase.execute(DentalIssue)
        } returns Unit

        val viewModel = createViewModel()
        viewModel.addDentalIssue(DentalIssue)
        Assert.assertEquals(true, (viewModel.submitStateAddDentalIssue.value is ReadableSuccess))
    }

    @Test
    fun `When dental issue is not added, then state should be failure`() = runTest {
        coEvery {
            addDentalIssueUseCase.execute(DentalIssue)
        } throws Exception()

        val viewModel = createViewModel()
        viewModel.addDentalIssue(DentalIssue)
        Assert.assertEquals(true, (viewModel.submitStateAddDentalIssue.value is ReadableFailure))
    }

    @Test
    fun `When getting all dental issue is success, then state should be success`() = runTest {
        coEvery {
            getAllDentalIssueUseCase.execute()
        } returns ArrayList()

        val viewModel = createViewModel()
        viewModel.getDentalIssues()
        Assert.assertEquals(true, (viewModel.submitStateGetDentalIssues.value is ReadableSuccess))
    }

    @Test
    fun `When getting all dental issue is failure, then state should be failure`() = runTest {
        coEvery {
            getAllDentalIssueUseCase.execute()
        } throws Exception()

        val viewModel = createViewModel()
        viewModel.getDentalIssues()
        Assert.assertEquals(true, (viewModel.submitStateGetDentalIssues.value is ReadableFailure))
    }

    @Test
    fun `When a dental issue is deleted, then state should be success`() = runTest {
        coEvery {
            deleteDentalIssueUseCase.execute(ToothId)
        } returns ToothId

        val viewModel = createViewModel()
        viewModel.deleteDentalIssues(ToothId)
        Assert.assertEquals(
            true,
            (viewModel.submitStateDeleteDentalIssues.value is ReadableSuccess)
        )
    }

    @Test
    fun `When a dental issue dos not delete, then state should be failure`() = runTest {
        coEvery {
            deleteDentalIssueUseCase.execute(ToothId)
        } throws Exception()

        val viewModel = createViewModel()
        viewModel.deleteDentalIssues(ToothId)
        Assert.assertEquals(
            true,
            (viewModel.submitStateDeleteDentalIssues.value is ReadableFailure)
        )
    }

    companion object{
        const val ToothId = 9
         val DentalIssue = DentalIssueQuestionsModel(0,0,0,0,0,)
    }
}