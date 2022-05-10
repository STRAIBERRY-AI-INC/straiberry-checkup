package com.straiberry.android.checkup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.straiberry.android.checkup.checkup.domain.model.AddImageToCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.model.UpdateImageInCheckupSuccessModel
import com.straiberry.android.checkup.checkup.domain.usecase.AddImageToCheckupUseCase
import com.straiberry.android.checkup.checkup.domain.usecase.UpdateImageInCheckupUseCase
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupSubmitImageViewModel
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.junit.*
import java.io.File

@ExperimentalCoroutinesApi
class CheckupSubmitImageViewModelTest {
    private val dispatcher = TestCoroutineDispatcher()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule(dispatcher)

    @RelaxedMockK
    lateinit var addImageToCheckupUseCase: AddImageToCheckupUseCase

    @RelaxedMockK
    lateinit var updateImageInCheckupUseCase: UpdateImageInCheckupUseCase

    private fun createViewModel() =
        CheckupSubmitImageViewModel(
            addImageToCheckupUseCase,
            updateImageInCheckupUseCase,
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
        viewModel.addImageToCheckup("", File(""), 0, 0)
        viewModel.updateImageInCheckup("", File(""), 0, 0)
        Assert.assertEquals(NotLoading, viewModel.submitStateAddImage.value)
        Assert.assertEquals(NotLoading, viewModel.submitStateUpdateImage.value)
    }

    @Test
    fun `When checkup id is not empty, then state should be loading`() = runTest {
        val viewModel = createViewModel()
        viewModel.submitStateAddImage.observeForever {
            viewModel.addImageToCheckup("1", File(""), 0, 0)
            Assert.assertEquals(Loading, viewModel.submitStateAddImage.value)
        }

        viewModel.submitStateUpdateImage.observeForever {
            viewModel.updateImageInCheckup("1", File(""), 0, 0)
            Assert.assertEquals(NotLoading, viewModel.submitStateUpdateImage.value)
        }
    }


    @Test
    fun `When image is uploaded, then state should be success`() =
        runTest {
            coEvery {
                addImageToCheckupUseCase.execute(
                    checkupIdMultipartBody, imageFile,
                    imageTypeMultipartBody, isLastImageMultiPartBody
                )
            } returns AddImageToCheckupSuccessModel(0, 0)

            val viewModel = createViewModel()
            viewModel.submitStateAddImage.observeForever {
                viewModel.addImageToCheckup("1", File(""), 0, 0)
                Assert.assertEquals(true, (viewModel.submitStateAddImage.value is Success))
            }
        }

    @Test
    fun `When image is not uploaded, then state should be failure`() =
        runTest {
            coEvery {
                addImageToCheckupUseCase.execute(
                    checkupIdMultipartBody, imageFile,
                    imageTypeMultipartBody, isLastImageMultiPartBody
                )
            }.throws(Exception())

            val viewModel = createViewModel()
            viewModel.addImageToCheckup("1", File(""), 0, 0)
            Assert.assertEquals(true, (viewModel.submitStateAddImage.value is Failure))
        }


    @Test
    fun `When image is updated, then state should be success`() =
        runTest {
            coEvery {
                updateImageInCheckupUseCase.execute(
                    checkupIdMultipartBody, imageFile,
                    imageTypeMultipartBody, 0
                )
            } returns UpdateImageInCheckupSuccessModel(true)

            val viewModel = createViewModel()
            viewModel.submitStateUpdateImage.observeForever {
                viewModel.updateImageInCheckup("1", File(""), 0, 0)
                Assert.assertEquals(true, (it is Success))
            }

        }

    @Test
    fun `When image is not updated, then state should be failure`() =
        runTest {
            coEvery {
                updateImageInCheckupUseCase.execute(
                    checkupIdMultipartBody, imageFile,
                    imageTypeMultipartBody, 0
                )
            }.throws(Exception())

            val viewModel = createViewModel()
            viewModel.updateImageInCheckup("1", File(""), 0, 0)
            Assert.assertEquals(true, (viewModel.submitStateUpdateImage.value is Failure))
        }

    companion object {
        val imageFile = MultipartBody.Part.createFormData(
            "image", File("").name,
            File("").asRequestBody("multipart/form-data".toMediaTypeOrNull())
        )

        val isLastImageMultiPartBody = MultipartBody.Part.createFormData("lastimage", "0")

        val checkupIdMultipartBody =
            MultipartBody.Part.createFormData("checkup", "0")

        val imageTypeMultipartBody =
            MultipartBody.Part.createFormData("image_type", "")
    }
}