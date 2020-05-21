package info.covid.utils

import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.chip.ChipGroup
import info.covid.customview.rings.Rings
import info.covid.customview.sectionbar.SectionProgressBar
import java.text.NumberFormat

@BindingAdapter(
    value = ["firstText", "secondText", "thiredText", "overAllText"],
    requireAll = false
)
fun setFirstText(rings: Rings, death: Int = 0, recovered: Int = 0, active: Int = 0, conf: Int = 0) {
    rings.overAllText = conf.toString()
    rings.innerFirstText = death.toString()
    rings.innerSecondText = recovered.toString()
    rings.innerThirdText = active.toString()

    rings.setRingInnerFirstProgress(death * 100f / conf, true)
    rings.setRingInnerSecondProgress(recovered * 100f / conf, true)
    rings.setRingInnerThirdProgress(active * 100f / conf, true)
    rings.setRingOverallProgress(100f, true)
    rings.invalidate()
}


@BindingAdapter("relativeTime")
fun setRelativeTime(tv: TextView, time: Long? = 0) {
    tv.text = if (time != null && time > 0) {
        try {
            DateUtils.getRelativeTimeSpanString(time * 1000)
        } catch (e: Exception) {
            ""
        }
    } else ""
}

@BindingAdapter("number")
fun setNumber(tv: TextView, amount: String? = null) {
    tv.text = if (amount.isNullOrEmpty().not()) {
        try {
            NumberFormat.getNumberInstance().format(amount.toNumber())
        } catch (e: Exception) {
            amount
        }
    } else amount
}

@BindingAdapter("deltaNumber")
fun setDeltaNumber(tv: TextView, amount: String? = null) {
    tv.text = if (amount.isNullOrEmpty().not() && amount.toNumber() > 0) {
        tv.visibility = View.VISIBLE
        try {
            NumberFormat.getNumberInstance().format(amount.toNumber())
        } catch (e: Exception) {
            amount
        }
    } else {
        tv.visibility = View.INVISIBLE
        " "
    }
}

@BindingAdapter(value = ["confirmed_cases", "active_cases", "recovered_cases", "deaths_cases"])
fun setProgress(
    sp: SectionProgressBar,
    c: String? = null,
    a: Int? = 0,
    r: String? = null,
    d: String? = null
) {
    sp.setProgress(
        (a?.times(100f)?.div(c.toNumber()) ?: 0f).div(100),
        r.toNumber().times(100f).div(c.toNumber()).div(100),
        d.toNumber().times(100f).div(c.toNumber()).div(100)
    )
}


@BindingAdapter("onItemSelected")
fun onItemSelected(cg: ChipGroup, listener: OnItemSelectionListener) {
    cg.setOnCheckedChangeListener { _, checkedId ->
        listener.onItemSelected(checkedId)
    }
}

interface OnItemSelectionListener {
    fun onItemSelected(id: Int)
}