package com.straiberry.android.checkup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupQuestionViewModel
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CheckupQuestionsViewModelTest {
    private val dispatcher = TestCoroutineDispatcher()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule(dispatcher)

    private fun createViewModel() =
        CheckupQuestionViewModel()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `When user select a title answer, then state of question one and two should not be updated`(){
        val viewModel = createViewModel()
        viewModel.saveAnswerOne(TitleAnswer, ToothIndex)
        viewModel.saveAnswerTwo(TitleAnswer, ToothIndex)
        assertEquals(hashMapOf<Int,Int>(),viewModel.submitStateAnswerOne.value)
        assertEquals(hashMapOf<Int,Int>(),viewModel.submitStateAnswerTwo.value)
    }

    @Test
    fun `When question one is answered, then state of first question should be updated`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.saveAnswerOne(AnswerIndex, ToothIndex)
            assertEquals(
                hashMapOf(Pair(ToothIndex, AnswerIndex)),
                viewModel.submitStateAnswerOne.value
            )
        }

    @Test
    fun `When question two is answered, then state of second question should be updated`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.saveAnswerTwo(AnswerIndex, ToothIndex)
            assertEquals(
                hashMapOf(Pair(ToothIndex, AnswerIndex)),
                viewModel.submitStateAnswerTwo.value
            )
        }

    @Test
    fun `When question three is answered, then state of third question should be updated`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.saveAnswerThree(AnswerIndex, ToothIndex)
            assertEquals(
                hashMapOf(Pair(ToothIndex, AnswerIndex)),
                viewModel.submitStateAnswerThree.value
            )
        }

    @Test
    fun `When user cancels the process, then all answers must be removed`() = runTest {
        val viewModel = createViewModel()
        viewModel.removeAllAnswers()
        assertEquals(hashMapOf<Int, Int>(), viewModel.submitStateAnswerOne.value)
        assertEquals(hashMapOf<Int, Int>(), viewModel.submitStateAnswerTwo.value)
        assertEquals(hashMapOf<Int, Int>(), viewModel.submitStateAnswerThree.value)
    }

    @Test
    fun `When user removes a tooth , then all answers related to that tooth must be removed`(){
        val viewModel = createViewModel()
        viewModel.removeCanceledAnswers(AnswerIndex)
        assertEquals(hashMapOf<Int,Int>(),viewModel.submitStateAnswerOne.value)
        assertEquals(hashMapOf<Int,Int>(),viewModel.submitStateAnswerTwo.value)
        assertEquals(hashMapOf<Int,Int>(),viewModel.submitStateAnswerThree.value)
    }

    @Test
    fun `When checkup question is open then all previous sub answers must reset`(){
        val viewModel = createViewModel()
        viewModel.resetSubAnswer()
        assertEquals(false,viewModel.submitStateAnswerTwoIsSubAnswer.value)
        assertEquals(false,viewModel.submitStateAnswerOneIsSubAnswer.value)
    }

    @Test
    fun `When user answers a question , then state number should plus one`(){
        val viewModel = createViewModel()
        viewModel.resetAnswer()
        viewModel.userAnswersQuestion()
        assertEquals(1,viewModel.submitStateIsAllThreeAnswer.value)
    }

    companion object{
        private const val ToothIndex=1
        private const val AnswerIndex = 3
        private const val TitleAnswer = 2
    }
}