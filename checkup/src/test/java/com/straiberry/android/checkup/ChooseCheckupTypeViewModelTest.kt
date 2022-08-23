package com.straiberry.android.checkup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupType
import com.straiberry.android.checkup.checkup.presentation.viewmodel.ChooseCheckupTypeViewModel
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ChooseCheckupTypeViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private fun createViewModel() =
        ChooseCheckupTypeViewModel()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `When user select a checkup, then checkup name must be updated`() {
        val checkupName = "Regular checkup"
        val viewModel = createViewModel()
        viewModel.setSelectedCheckup(checkupName)
        assertEquals(checkupName, viewModel.submitStateSelectedCheckup.value)
    }

    @Test
    fun `When user select a checkup, then checkup index must be updated`() {
        val viewModel = createViewModel()
        viewModel.setSelectedCheckupIndex(CheckupType.Whitening)
        assertEquals(CheckupType.Whitening, viewModel.submitStateSelectedCheckupIndex.value)
    }

    @Test
    fun `When a checkup is created, then checkup id must be updated`() {
        val checkupId = "1"
        val viewModel = createViewModel()
        viewModel.setCheckupId(checkupId)
        assertEquals(checkupId, viewModel.submitStateCreateCheckupId.value)
    }
}