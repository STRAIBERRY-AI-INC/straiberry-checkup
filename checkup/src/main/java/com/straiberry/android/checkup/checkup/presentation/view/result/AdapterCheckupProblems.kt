package com.straiberry.android.checkup.checkup.presentation.view.result

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.presentation.viewmodel.CheckupResultLastSelectedProblemViewModel
import com.straiberry.android.checkup.databinding.ItemCheckupProblemsBinding
import com.straiberry.android.common.extensions.onClick
import com.straiberry.android.common.model.JawPosition

data class CheckupProblemsModel(
    val icon: Drawable,
    val title: String,
    var problemCount: Int,
    val listOfToothIndex: ArrayList<Int>,
    val listOfToothPosition: ArrayList<Pair<Double, Double>>,
    val jawType: JawPosition
)

class AdapterCheckupProblems(
    val context: Context,
    var data: List<CheckupProblemsModel>,
    private val toothProblemClick: ToothProblemClick,
    private val checkupResultLastSelectedProblemViewModel: CheckupResultLastSelectedProblemViewModel,
    private val currentJawPosition: JawPosition,
    private val isIllustration: Boolean
) : RecyclerView.Adapter<AdapterCheckupProblems.ViewHolder>() {
    private var performClickForFirstTime = true

    class ViewHolder(val binding: ItemCheckupProblemsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCheckupProblemsBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.imageViewProblemIcon.load(data[position].icon)
        holder.binding.textViewProblemTitle.text = data[position].title
        holder.binding.textViewProblemCount.text =
            context.getString(R.string.tooth_problem_count, data[position].problemCount)
        holder.binding.cardViewToothProblem.onClick {
            holder.binding.cardViewToothProblem.isSelected = true
            holder.binding.textViewProblemCount.isEnabled = true
            holder.binding.textViewProblemTitle.isEnabled = true
            toothProblemClick.toothProblemOnCLick(
                position,
                data[position].listOfToothIndex,
                data[position].listOfToothPosition,
                data[position].title,
                data[position].jawType
            )
        }

        if (performClickForFirstTime) {
            when (currentJawPosition) {
                JawPosition.FrontTeethUpper -> {
                    toothProblemClick.toothProblemOnCLick(
                        checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemFrontUpperIllustration.value!!,
                        data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemFrontUpperIllustration.value!!].listOfToothIndex,
                        data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemFrontUpperIllustration.value!!].listOfToothPosition,
                        data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemFrontUpperIllustration.value!!].title,
                        data[position].jawType
                    )
                }
                JawPosition.FrontTeethLower -> {
                    toothProblemClick.toothProblemOnCLick(
                        checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemFrontUpperIllustration.value!!,
                        data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemFrontUpperIllustration.value!!].listOfToothIndex,
                        data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemFrontUpperIllustration.value!!].listOfToothPosition,
                        data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemFrontUpperIllustration.value!!].title,
                        data[position].jawType
                    )
                }
                JawPosition.FrontTeeth -> {
                    toothProblemClick.toothProblemOnCLick(
                        checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemFrontRealImage.value!!,
                        data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemFrontRealImage.value!!].listOfToothIndex,
                        data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemFrontRealImage.value!!].listOfToothPosition,
                        data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemFrontRealImage.value!!].title,
                        data[position].jawType
                    )
                }
                JawPosition.UpperJaw -> {
                    if (isIllustration)
                        toothProblemClick.toothProblemOnCLick(
                            checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemUpperIllustration.value!!,
                            data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemUpperIllustration.value!!].listOfToothIndex,
                            data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemUpperIllustration.value!!].listOfToothPosition,
                            data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemUpperIllustration.value!!].title,
                            data[position].jawType
                        )
                    else
                        toothProblemClick.toothProblemOnCLick(
                            checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemUpperRealImage.value!!,
                            data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemUpperRealImage.value!!].listOfToothIndex,
                            data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemUpperRealImage.value!!].listOfToothPosition,
                            data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemUpperRealImage.value!!].title,
                            data[position].jawType
                        )
                }

                JawPosition.LowerJaw -> {
                    if (isIllustration)
                        toothProblemClick.toothProblemOnCLick(
                            checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemLowerIllustration.value!!,
                            data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemLowerIllustration.value!!].listOfToothIndex,
                            data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemLowerIllustration.value!!].listOfToothPosition,
                            data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemLowerIllustration.value!!].title,
                            data[position].jawType
                        )
                    else
                        toothProblemClick.toothProblemOnCLick(
                            checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemLowerRealImage.value!!,
                            data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemLowerRealImage.value!!].listOfToothIndex,
                            data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemLowerRealImage.value!!].listOfToothPosition,
                            data[checkupResultLastSelectedProblemViewModel.submitStateSelectedProblemLowerRealImage.value!!].title,
                            data[position].jawType
                        )
                }
            }
            performClickForFirstTime = false
        }

    }

    override fun getItemCount(): Int = data.size
}

interface ToothProblemClick {
    fun toothProblemOnCLick(
        position: Int,
        listOfToothIndex: ArrayList<Int>,
        listOfToothPosition: ArrayList<Pair<Double, Double>>,
        problemTitle: String,
        jawType: JawPosition
    )
}