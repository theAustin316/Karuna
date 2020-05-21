package info.covid.home

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import info.covid.R
import info.covid.common.RVAdapter
import info.covid.database.enities.CovidDayInfo
import info.covid.databinding.FragmentHomeBinding
import info.covid.utils.MyXAxisValueFormatter
import info.covid.utils.toNumber


class HomeFragment : Fragment() {
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: RVAdapter<CovidDayInfo>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
        binding.viewModel = viewModel
        adapter = RVAdapter(R.layout.adapter_day_count_item)
        binding.rv.adapter = adapter
        setUpChart()
        subscribeToData()
        initListeners()
        return binding.root
    }


    private fun subscribeToData() {
        viewModel.chartData.observe(viewLifecycleOwner, Observer {
            setData((binding.chartType.text == getString(R.string.cumulative)))
        })

        viewModel.stateDataList.observe(viewLifecycleOwner, Observer {

        })

        viewModel.error.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })

        viewModel.dayList.observe(viewLifecycleOwner, Observer {
            adapter.setList(it.reversed(), it.maxWith(Comparator { o1, o2 ->
                o1.dailyconfirmed.toNumber().compareTo(o2.dailyconfirmed.toNumber())
            })?.dailyconfirmed.toNumber())

            binding.rv.scrollToPosition(0)
        })
    }

    private fun initListeners() {
        binding.filters.setOnCheckedChangeListener { _, checkedId ->
            viewModel.allTime.postValue(checkedId == R.id.all)
        }

        binding.chartType.setOnClickListener {
            showChartTypeMenu()
        }
    }

    private fun showChartTypeMenu() {
        val popupMenu = PopupMenu(context, binding.chartType)
        popupMenu.menuInflater.inflate(R.menu.chart_type, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.daily -> {
                    binding.chartType.setText(R.string.daily)
                    setData(false)
                }
                R.id.cumulative -> {
                    binding.chartType.setText(R.string.cumulative)
                    setData(true)
                }
            }

            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }

    private fun setUpChart() {
        binding.dailyChart.legend.textColor = getColor(requireContext(), R.color.textColorPrimary)
        binding.dailyChart.xAxis.textColor = getColor(requireContext(), R.color.textColorPrimary)
        binding.dailyChart.axisLeft.textColor = getColor(requireContext(), R.color.textColorPrimary)
        binding.dailyChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.dailyChart.description = null
        binding.dailyChart.axisRight.isEnabled = false
        binding.dailyChart.axisLeft.setDrawGridLines(false)
        binding.dailyChart.xAxis.setDrawGridLines(false)
        binding.dailyChart.xAxis.valueFormatter = MyXAxisValueFormatter()
        binding.dailyChart.setExtraOffsets(0f, 0f, 0f, 15f)
    }


    private fun setData(isCumulative: Boolean) {
        val lines = arrayListOf<ILineDataSet>().apply {
            add(
                LineDataSet(
                    if (isCumulative) viewModel.confirmedList else viewModel.dailyConfirmedList,
                    getString(R.string.confirmed)
                ).apply {
                    color = getColor(requireContext(), R.color.confirmed)
                    setDrawCircles(false)
                    lineWidth = 4f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    cubicIntensity = 0.10f
                })

            add(
                LineDataSet(
                    if (isCumulative) viewModel.recoveredList else viewModel.dailyRecoveredList,
                    getString(R.string.recovered)
                ).apply {
                    color = getColor(requireContext(), R.color.recovered)
                    setDrawCircles(false)
                    lineWidth = 4f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    cubicIntensity = 0.10f
                })

            add(
                LineDataSet(
                    if (isCumulative) viewModel.deceasedList else viewModel.dailyDeceasedList,
                    getString(R.string.deaths)
                ).apply {
                    color = getColor(requireContext(), R.color.deaths)
                    setDrawCircles(false)
                    lineWidth = 4f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    cubicIntensity = 0.10f
                })
        }

        if (binding.dailyChart.data != null) {
            binding.dailyChart.clearValues()
        }

        binding.dailyChart.data = LineData(lines).apply {
            setDrawValues(false)
        }

        binding.dailyChart.data.notifyDataChanged()
        binding.dailyChart.invalidate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_toolbar, menu)
        val refresh = menu.findItem(R.id.refresh)

        viewModel.refreshing.observe(viewLifecycleOwner, Observer {
            if (it) {
                refresh.setActionView(R.layout.action_view_progress)
            } else refresh.actionView = null
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> {
                viewModel.getDate()
            }
        }
        return true
    }
}