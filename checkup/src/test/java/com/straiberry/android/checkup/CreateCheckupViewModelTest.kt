package com.straiberry.android.checkup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupType
import com.straiberry.android.checkup.checkup.domain.model.CreateCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.usecase.CreateCheckupSdkUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.CreateCheckupUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.GetSdkTokenUseCase
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CreateCheckupViewModel
import com.straiberry.android.core.base.Failure
import com.straiberry.android.core.base.Loading
import com.straiberry.android.core.base.NotLoading
import com.straiberry.android.core.base.Success
import com.straiberry.android.core.network.CoroutineContextProvider
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CreateCheckupViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule(dispatcher)

    @RelaxedMockK
    lateinit var createCheckupUseCase: CreateCheckupSdkUseCase

    @RelaxedMockK
    lateinit var getSdkTokenUseCase: GetSdkTokenUseCase

    private fun createViewModel() =
        CreateCheckupViewModel(
            createCheckupUseCase,
            getSdkTokenUseCase,
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
    fun `When checkup type is not listed , then state should not be loading`() {
        val viewModel = createViewModel()
        viewModel.createCheckup(CheckupType.Regular)
        assertEquals(NotLoading, viewModel.submitStateCreateCheckup.value)
    }

    @Test
    fun `When checkup type is correct, then state should be loading`() {
        val viewModel = createViewModel()
        viewModel.createCheckup(CheckupType.Whitening)
        assertEquals(Loading, viewModel.submitStateCreateCheckup.value)
    }

    @Test
    fun `When checkup is created, then state should be success`() = runTest {
        coEvery {
            createCheckupUseCase.execute(any())
        } returns CreateCheckupSuccessModel("")

        val viewModel = createViewModel()
        viewModel.createCheckup(CheckupType.Whitening)
        delay(100)
        assertEquals(true, (viewModel.submitStateCreateCheckup.value is Success))
    }

    @Test
    fun `When create a checkup is fail, then state should be failure`() = runTest {
        coEvery {
            createCheckupUseCase.execute(any())
        } throws Exception()

        val viewModel = createViewModel()
        viewModel.createCheckup(CheckupType.Whitening)
        delay(100)
        assertEquals(true, (viewModel.submitStateCreateCheckup.value is Failure))
    }
}