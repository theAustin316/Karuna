package info.covid.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import info.covid.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class RallyLineGraphChart : View {

    private val data = mutableListOf<DataPoint>()
    private val points = mutableListOf<PointF>()
    private val conPoint1 = mutableListOf<PointF>()
    private val conPoint2 = mutableListOf<PointF>()

    private val path = Path()
    private val borderPath = Path()
    private val barPaint = Paint()
    private val pathPaint = Paint()
    private val borderPathPaint = Paint()
    private val backgroundPaint = Paint()

    private var borderColor = Color.BLACK

    private var curveTopMargin = 32

    private val barWidth by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, .5f, resources.displayMetrics
        )
    }

    private val borderPathWidth by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics
        )
    }

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(attrs)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(set: AttributeSet?) {

        val ta = context.obtainStyledAttributes(set, R.styleable.RallyLineGraphChart)
        val barColor = ta.getColor(R.styleable.RallyLineGraphChart_barColor, Color.GRAY)
        val fillColor =
            ta.getColor(
                R.styleable.RallyLineGraphChart_curveFillColor,
                Color.parseColor("#ff2A2931")
            )

        borderColor =
            ta.getColor(
                R.styleable.RallyLineGraphChart_curveBorderColor,
                Color.parseColor("#FF66AFFF")
            )

        curveTopMargin = ta.getDimensionPixelSize(R.styleable.RallyLineGraphChart_curveTopMargin, 0)
        ta.recycle()

        barPaint.apply {
            isAntiAlias = true
            strokeWidth = barWidth
            style = Paint.Style.STROKE
            color = barColor
        }

        pathPaint.apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = fillColor
            shader = LinearGradient(
                0f,
                0f,
                0f,
                height.toFloat(),
                Color.BLACK,
                Color.WHITE,
                Shader.TileMode.CLAMP
            )
        }

        borderPathPaint.apply {
            isAntiAlias = true
            strokeWidth = borderPathWidth
            style = Paint.Style.STROKE
            color = borderColor
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawBezierCurve(canvas)
    }


    private fun drawBezierCurve(canvas: Canvas?) {

        try {

            if (points.isEmpty() && conPoint1.isEmpty() && conPoint2.isEmpty()) return

            path.reset()
            path.moveTo(points.first().x, points.first().y)

            for (i in 1 until points.size) {
                path.cubicTo(
                    conPoint1[i - 1].x, conPoint1[i - 1].y, conPoint2[i - 1].x, conPoint2[i - 1].y,
                    points[i].x, points[i].y
                )
            }

            borderPath.set(path)

            path.lineTo(width.toFloat(), height.toFloat())
            path.lineTo(0f, height.toFloat())


            canvas?.drawPath(borderPath, borderPathPaint)

            backgroundPaint.apply {
                isAntiAlias = false
                style = Paint.Style.FILL
                shader = LinearGradient(
                    0f,
                    0f,
                    0f,
                    height.toFloat(),
                    borderColor,
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
            }

            canvas?.drawPath(path, backgroundPaint)

        } catch (e: Exception) {
        }
    }

    private fun calculatePointsForData(max: Float) {
        if (data.isEmpty()) return

        val bottomY = getLargeBarHeight() - CURVE_BOTTOM_MARGIN
        val xDiff =
            width.toFloat() / (data.size - 1) //subtract -1 because we want to include position at right side

        val maxData = max // data.maxBy { it.amount }!!.amount

        for (i in 0 until data.size) {
            val y = bottomY - (data[i].amount / maxData * (bottomY - curveTopMargin))
            points.add(PointF(xDiff * i, y))
        }
    }

    private fun calculateConnectionPointsForBezierCurve() {
        try {
            for (i in 1 until points.size) {
                conPoint1.add(PointF((points[i].x + points[i - 1].x) / 2, points[i - 1].y))
                conPoint2.add(PointF((points[i].x + points[i - 1].x) / 2, points[i].y))
            }
        } catch (e: Exception) {
        }
    }

    private fun getLargeBarHeight() = height / 2.1f * 2f

    fun addDataPoints(data: List<DataPoint>, max: Float) {
        //do calculation in worker thread // Note: You should use some safe thread mechanism
        //Calculation logic here are not fine, should updated when more time available
        post {
            Thread(Runnable {

                val oldPoints = points.toList()

                if (oldPoints.isEmpty()) {
                    this.data.addAll(data.toList())
                    calculatePointsForData(max)
                    calculateConnectionPointsForBezierCurve()
                    postInvalidate()
                    return@Runnable
                }

                resetDataPoints()
                this.data.addAll(data.toList())
                calculatePointsForData(max)
                calculateConnectionPointsForBezierCurve()

                val newPoints = points.toList()

                val size = oldPoints.size
                var maxDiffY = 0f
                for (i in 0 until size) {
                    val abs = abs(oldPoints[i].y - newPoints[i].y)
                    if (abs > maxDiffY) maxDiffY = abs
                }

                val loopCount = maxDiffY / 16

                val tempPointsForAnimation = mutableListOf<MutableList<PointF>>()

                for (i in 0 until size) {
                    val old = oldPoints[i]
                    val new = newPoints[i]

                    val plusOrMinusAmount = abs(new.y - old.y) / maxDiffY * 16

                    var tempY = old.y
                    val tempList = mutableListOf<PointF>()

                    for (j in 0..loopCount.toInt()) {
                        if (tempY == new.y) {
                            tempList.add(PointF(new.x, new.y))

                        } else {

                            if (new.y > old.y) {
                                tempY += plusOrMinusAmount
                                tempY = min(tempY, new.y)
                                tempList.add(PointF(new.x, tempY))

                            } else {
                                tempY -= plusOrMinusAmount
                                tempY = max(tempY, new.y)
                                tempList.add(PointF(new.x, tempY))
                            }
                        }
                    }
                    tempPointsForAnimation.add(tempList)

                }

                if (tempPointsForAnimation.isEmpty()) return@Runnable

                val first = tempPointsForAnimation[0]
                val length = first.size

                for (i in 0 until length) {
                    conPoint1.clear()
                    conPoint2.clear()
                    points.clear()
                    points.addAll(tempPointsForAnimation.map { it[i] })
                    calculateConnectionPointsForBezierCurve()
                    postInvalidate()
                    Thread.sleep(16)
                }

            }).start()
        }
    }

    private fun resetDataPoints() {
        this.data.clear()
        points.clear()
        conPoint1.clear()
        conPoint2.clear()
    }

    fun setCurveBorderColor(@ColorRes color: Int) {
        borderPathPaint.color = ContextCompat.getColor(context, color)
    }

    companion object {
        private const val INDEX_OF_LARGE_BAR = 8
        private const val VERTICAL_BARS =
            (INDEX_OF_LARGE_BAR * INDEX_OF_LARGE_BAR) + 6 // add fixed bars size
        private const val CURVE_BOTTOM_MARGIN = 32f

    }
}

data class DataPoint(val amount: Float)