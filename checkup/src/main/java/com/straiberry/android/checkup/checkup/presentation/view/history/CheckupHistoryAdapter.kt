package com.straiberry.android.checkup.checkup.presentation.view.history

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.checkup.data.networking.model.CheckupHistorySuccessResponse
import com.straiberry.android.checkup.checkup.domain.model.CheckupResultSuccessModel
import com.straiberry.android.checkup.databinding.ItemCheckupHistoryBinding
import com.straiberry.android.checkup.databinding.ItemCheckupHistoryDividerBinding
import com.straiberry.android.checkup.databinding.ItemCheckupHistoryLoadingBinding
import com.straiberry.android.common.extensions.*
import java.util.*

data class CheckupHistory(val checkup: CheckupHistorySuccessResponse.Data, val type: Int)
class CheckupHistoryAdapter(
    private val context: Context,
    private val onCheckupClick: OnCheckupClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isLoadingAdded = false
    private var checkupYear = 0
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var dataCheckupHistory: ArrayList<CheckupHistory>? = ArrayList()

    init {

    }

    inner class ViewHolderItem(val binding: ItemCheckupHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CheckupHistorySuccessResponse.Data) {
            binding.textViewMonthAndDay.text = context.getString(
                R.string.month_day,
                item.createdAt.toDate()?.getDayFromDate(),
                item.createdAt.toDate()?.getMonthFromDate()
            )
            binding.textViewCheckupType.text =
                convertCheckupTypeToString(item.checkupType.toInt())

            // If checkup type is whitening then hide oral hygiene score.
            // If checkup type is x-ray then hide whitening score
            when {
                item.checkupType.toInt() == TEETH_WHITENING_TYPE -> {
                    binding.apply {
                        textViewWhiteningScore.visible()
                        textViewWhiteningTitle.visible()
                        imageViewWhitening.visible()
                        textViewOralHygieneTitle.gone()
                        cardViewOralHygieneScore.gone()
                        imageViewOral.gone()
                    }
                }
                item.checkupType.toInt() == XRAY_TYPE -> {
                    binding.apply {
                        textViewWhiteningScore.gone()
                        textViewWhiteningTitle.gone()
                        imageViewWhitening.gone()
                        textViewOralHygieneTitle.visible()
                        cardViewOralHygieneScore.visible()
                        imageViewOral.visible()
                    }
                }
                else -> binding.apply {
                    textViewWhiteningScore.visible()
                    textViewWhiteningTitle.visible()
                    imageViewWhitening.visible()
                    textViewOralHygieneTitle.visible()
                    cardViewOralHygieneScore.visible()
                    imageViewOral.visible()
                }
            }
            binding.textViewOralHygieneScore.text = item.overalScore
            binding.textViewWhiteningScore.text =
                item.whiteningScore.toStringOrZero().toFloat().toInt().toString()
            binding.textViewTime.text = item.createdAt.toDate()?.getTimeFromDate()
            binding.root.onClick { onCheckupClick(CheckupResultSuccessModel(item)) }
        }
    }

    class ViewHolderLoading(val binding: ItemCheckupHistoryLoadingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(isLoading: Boolean) {
            if (isLoading)
                binding.textViewLoadingMore.visible()
            else
                binding.textViewLoadingMore.gone()
        }
    }


    class ViewHolderDivider(val binding: ItemCheckupHistoryDividerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CheckupHistorySuccessResponse.Data) {
            binding.textViewYear.text = item.createdAt.toDate()?.getYearFromDate()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Loading -> {
                ViewHolderLoading(
                    ItemCheckupHistoryLoadingBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }
            Item -> {
                ViewHolderItem(
                    ItemCheckupHistoryBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }
            Divider -> {
                ViewHolderDivider(
                    ItemCheckupHistoryDividerBinding.inflate(
                        LayoutInflater.from(
                            context
                        ), parent, false
                    )
                )
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            Loading -> {
                val viewHolderLoading = holder as ViewHolderLoading
                viewHolderLoading.bind(true)
            }
            Item -> {
                val viewHolderItem = holder as ViewHolderItem
                viewHolderItem.bind(dataCheckupHistory?.get(position)!!.checkup)
            }
            Divider -> {
                val viewHolderDivider = holder as ViewHolderDivider
                viewHolderDivider.bind(dataCheckupHistory?.get(position)!!.checkup)
            }
        }
    }

    override fun getItemCount(): Int = dataCheckupHistory?.size!!

    override fun getItemViewType(position: Int): Int {
        return if (position == dataCheckupHistory?.size!! - 1 && isLoadingAdded)
            Loading else dataCheckupHistory!![position].type

    }

    fun addLoadingFooter() {
        isLoadingAdded = true
    }

    /**
     * Remove loading view from bottom of the list
     */
    fun removeLoadingFooter() {
        isLoadingAdded = false
        notifyItemChanged(dataCheckupHistory?.size!!)
    }

    /**
     * Add every page date to list
     */
    fun add(dataListAddMore: List<CheckupHistorySuccessResponse.Data?>?) {
        dataListAddMore?.forEachIndexed { index, data ->
            if (data!!.createdAt.toDate()?.getYearFromDate()?.toInt()!! < currentYear) {
                dataCheckupHistory?.add(CheckupHistory(data, Divider))
                currentYear = data.createdAt.toDate()?.getYearFromDate()?.toInt()!!
            }
            dataCheckupHistory?.add(CheckupHistory(data, Item))
        }
        notifyItemInserted(dataCheckupHistory?.size!! - 1)
    }

    /**
     * Convert incoming integer from api to checkup name
     */
    private fun convertCheckupTypeToString(checkupType: Int): String {
        return when (checkupType) {
            REGULAR_CHECKUP_TYPE -> context.getString(R.string.regular_checkup)
            TEETH_WHITENING_TYPE -> context.getString(R.string.teeth_whitening)
            TOOTH_SENSITIVITY_TYPE -> context.getString(R.string.toothache_amp_tooth_sensitivity)
            PREVIOUS_TREATMENT_TYPE -> context.getString(R.string.problems_with_previous_treatment)
            XRAY_TYPE -> context.getString(R.string.x_rays)
            OTHERS_CHECKUP_TYPE -> context.getString(R.string.others)
            else -> ""
        }
    }

    companion object {
        private const val TEETH_WHITENING_TYPE = 0
        private const val REGULAR_CHECKUP_TYPE = -1
        private const val TOOTH_SENSITIVITY_TYPE = 1
        private const val PREVIOUS_TREATMENT_TYPE = 2
        private const val XRAY_TYPE = 4
        private const val OTHERS_CHECKUP_TYPE = 3
        private var firstYear = true
        private const val Loading = 0
        private const val Item = 1
        private const val Divider = 2
    }
}
typealias OnCheckupClick = (checkup: CheckupResultSuccessModel) -> Unit
