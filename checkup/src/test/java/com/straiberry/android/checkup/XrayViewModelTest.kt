package com.straiberry.android.checkup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.straiberry.android.checkup.checkup.domain.usecase.AddXrayImageFromUrlUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.CheckXrayUrlUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.CreateXrayCheckupUseCase
import com.straiberry.android.checkup.checkup.presentation.viewmodel.XrayViewModel
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
import org.junit.*

@ExperimentalCoroutinesApi
class XrayViewModelTest {
    private val dispatcher = TestCoroutineDispatcher()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule(dispatcher)

    @RelaxedMockK
    lateinit var createXrayCheckupUseCase: CreateXrayCheckupUseCase

    @RelaxedMockK
    lateinit var addXrayImageFromUrlUseCase: AddXrayImageFromUrlUseCase

    @RelaxedMockK
    lateinit var checkXrayUrlUseCase: CheckXrayUrlUseCase
    private fun createViewModel() = XrayViewModel(
        createXrayCheckupUseCase, addXrayImageFromUrlUseCase, checkXrayUrlUseCase,
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
        viewModel.addXrayImageFromUrl("", "")
        Assert.assertEquals(NotLoading, viewModel.submitStateAddXrayImageFromUrl.value)
    }

    @Test
    fun `When checkup id is not empty, then state should be loading`() = runTest {
        val viewModel = createViewModel()
        viewModel.submitStateAddXrayImageFromUrl.observeForever {
            viewModel.addXrayImageFromUrl("1", "url")
            Assert.assertEquals(Loading, viewModel.submitStateAddXrayImageFromUrl.value)
        }
    }

    @Test
    fun `When x-ray image added to checkup, then state should be success`() =
        runTest {
            coEvery {
                addXrayImageFromUrlUseCase.execute("1", "url")
            } returns Unit

            val viewModel = createViewModel()
            viewModel.addXrayImageFromUrl("1", "url")
            Assert.assertEquals(true, (viewModel.submitStateAddXrayImageFromUrl.value is Success))
        }

    @Test
    fun `When ux-ray image not added to checkup, then state should be failure`() =
        runTest {
            coEvery {
                addXrayImageFromUrlUseCase.execute("1", "url")
            } throws Exception()

            val viewModel = createViewModel()
            viewModel.addXrayImageFromUrl("1", "url")
            Assert.assertEquals(true, (viewModel.submitStateAddXrayImageFromUrl.value is Failure))
        }


    @Test
    fun `When url has an x-ray image, then state should be success`() =
        runTest {
            coEvery {
                checkXrayUrlUseCase.execute("url")
            } returns Unit

            val viewModel = createViewModel()
            viewModel.checkXrayUrl("url")
            Assert.assertEquals(true, (viewModel.submitStateCheckXrayUrl.value is Success))
        }

    @Test
    fun `When url dos not have an x-ray image, then state should be failure`() =
        runTest {
            coEvery {
                checkXrayUrlUseCase.execute("url")
            } throws Exception()

            val viewModel = createViewModel()
            viewModel.checkXrayUrl("url")
            Assert.assertEquals(true, (viewModel.submitStateCheckXrayUrl.value is Failure))
        }
}