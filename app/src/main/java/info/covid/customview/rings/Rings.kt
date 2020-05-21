package info.covid.customview.rings

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import info.covid.R

class Rings @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    // Default values variables
    private val defaultTextSize: Float
    private val defaultTextMarginLeft: Float
    private val defaultInnerStrokeWidth: Float
    private val defaultInnerStrokeUnfinishedWidth: Float
    private val defaultOuterStrokeWidth: Float
    private val defaultOuterStrokeUnfinishedWidth: Float
    private val defaultRingUnfinishedColor: Int
    private var defaultRingFilledColor: Int
    private val defaultRingOverallProgress: Float
    private val defaultRingInnerThirdProgress: Float
    private val defaultRingInnerSecondProgress: Float
    private val defaultRingInnerFirstProgress: Float
    var overAllText: String
    var innerFirstText: String
    var innerSecondText: String
    var innerThirdText: String

    // Attributes variables
    private var textSize = 0f
    private var textMarginLeft = 0f
    private var innerStrokeWidth = 0f
    private var innerStrokeWidthUnfinished = 0f
    private var outerStrokeWidth = 0f
    private var outerStrokeWidthUnfinished = 0f
    private var ringOverallColor = 0
    private var ringInnerThirdColor = 0
    private var ringInnerSecondColor = 0
    private var ringInnerFirstColor = 0
    private var ringUnfinishedColor = 0
    var chartRingOverallProgress = 0f
        private set
    var chartRingSpeedProgress = 0f
        private set
    var chartRingBrakingProgress = 0f
        private set
    var chartRingAccelerationProgress = 0f
        private set
    private var highlighted = false

    /**
     * @return One of [.RING_OVERALL], [.THIRD_INNER_RING], [.SECOND_INNER_RING] or [.FIRST_INNER_RING]
     */
    var highlightedRing: Short = -1
        private set
    private val startAngle = 90f
    private val emptyArcAngle = 270f

    // Paint objects
    private var textPaint: Paint? = null
    private var ringPaint: Paint? = null

    // RectF objects used to draw the arcs
    private var ringOverall: RectF? = null
    private var ringInnerThird: RectF? = null
    private var ringInnerSecond: RectF? = null
    private var ringInnerFirst: RectF? = null

    // Rect objects used by all text drawn. Used to detect user touches.
    private var rectOverallText: Rect? = null
    private var rectInnerThirdText: Rect? = null
    private var rectInnerSecondText: Rect? = null
    private var rectInnerFirstText: Rect? = null
    private var auxRect: Rect? = null

    /**
     * @return True if rings are clickable, false otherwise. If [.setRingsClickable] has not been called, returns true.
     */
    /**
     * Sets the rings to be clickable
     *
     * @param areRingsClickable Whether the rings are clickable or not.
     */
    /**
     * Set all the rings clickable.
     */
    var ringsClickable = true
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
        ringOverall!![outerStrokeWidth / 2f, outerStrokeWidth / 2f, MeasureSpec.getSize(
            widthMeasureSpec
        ) - outerStrokeWidth / 2f] = MeasureSpec.getSize(heightMeasureSpec) - outerStrokeWidth / 2f
        ringInnerThird!![ringOverall!!.left + (outerStrokeWidth + innerStrokeWidth), ringOverall!!.top + (outerStrokeWidth + innerStrokeWidth), ringOverall!!.right - (outerStrokeWidth + innerStrokeWidth)] =
            ringOverall!!.bottom - (outerStrokeWidth + innerStrokeWidth)
        ringInnerSecond!![ringInnerThird!!.left + (outerStrokeWidth + innerStrokeWidth), ringInnerThird!!.top + (outerStrokeWidth + innerStrokeWidth), ringInnerThird!!.right - (outerStrokeWidth + innerStrokeWidth)] =
            ringInnerThird!!.bottom - (outerStrokeWidth + innerStrokeWidth)
        ringInnerFirst!![ringInnerSecond!!.left + (outerStrokeWidth + innerStrokeWidth), ringInnerSecond!!.top + (outerStrokeWidth + innerStrokeWidth), ringInnerSecond!!.right - (outerStrokeWidth + innerStrokeWidth)] =
            ringInnerSecond!!.bottom - (outerStrokeWidth + innerStrokeWidth)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw empty rings
        ringPaint!!.color = ringUnfinishedColor
        ringPaint!!.strokeWidth = outerStrokeWidthUnfinished
        canvas.drawArc(ringOverall!!, startAngle, emptyArcAngle, false, ringPaint!!)
        ringPaint!!.strokeWidth = innerStrokeWidthUnfinished
        canvas.drawArc(ringInnerThird!!, startAngle, emptyArcAngle, false, ringPaint!!)
        canvas.drawArc(ringInnerSecond!!, startAngle, emptyArcAngle, false, ringPaint!!)
        canvas.drawArc(ringInnerFirst!!, startAngle, emptyArcAngle, false, ringPaint!!)
        textPaint!!.getTextBounds(overAllText, 0, overAllText.length, auxRect)
        rectOverallText!![width / 2 + textMarginLeft.toInt(), height - outerStrokeWidth.toInt(), width / 2 + auxRect!!.width() + textMarginLeft.toInt()] =
            height
        textPaint!!.getTextBounds(innerThirdText, 0, innerThirdText.length, auxRect)
        rectInnerThirdText!![width / 2 + textMarginLeft.toInt(), (ringInnerThird!!.bottom - textSize.toInt()).toInt(), width / 2 + auxRect!!.width() + textMarginLeft.toInt()] =
            (ringInnerThird!!.bottom + innerStrokeWidth).toInt()
        textPaint!!.getTextBounds(innerSecondText, 0, innerSecondText.length, auxRect)
        rectInnerSecondText!![width / 2 + textMarginLeft.toInt(), (ringInnerSecond!!.bottom - textSize.toInt()).toInt(), width / 2 + auxRect!!.width() + textMarginLeft.toInt()] =
            (ringInnerSecond!!.bottom + innerStrokeWidth).toInt()
        textPaint!!.getTextBounds(innerFirstText, 0, innerFirstText.length, auxRect)
        rectInnerFirstText!![width / 2 + textMarginLeft.toInt(), (ringInnerFirst!!.bottom - textSize.toInt()).toInt(), width / 2 + auxRect!!.width() + textMarginLeft.toInt()] =
            (ringInnerFirst!!.bottom + innerStrokeWidth).toInt()
        if (!highlighted) {
            // Draw text
            textPaint!!.color = ringOverallColor
            canvas.drawText(
                overAllText,
                width / 2f + textMarginLeft,
                height.toFloat(),
                textPaint!!
            )
            textPaint!!.color = ringInnerThirdColor
            canvas.drawText(
                innerThirdText,
                width / 2f + textMarginLeft,
                ringInnerThird!!.bottom + innerStrokeWidth / 2,
                textPaint!!
            )
            textPaint!!.color = ringInnerSecondColor
            canvas.drawText(
                innerSecondText,
                width / 2f + textMarginLeft,
                ringInnerSecond!!.bottom + innerStrokeWidth / 2,
                textPaint!!
            )
            textPaint!!.color = ringInnerFirstColor
            canvas.drawText(
                innerFirstText,
                width / 2f + textMarginLeft,
                ringInnerFirst!!.bottom + innerStrokeWidth / 2,
                textPaint!!
            )

            // Draw filled rings
            ringPaint!!.strokeWidth = outerStrokeWidth
            ringPaint!!.color = ringOverallColor
            canvas.drawArc(ringOverall!!, startAngle, chartRingOverallProgress, false, ringPaint!!)
            ringPaint!!.strokeWidth = innerStrokeWidth
            ringPaint!!.color = ringInnerThirdColor
            canvas.drawArc(
                ringInnerThird!!,
                startAngle,
                chartRingSpeedProgress,
                false,
                ringPaint!!
            )
            ringPaint!!.color = ringInnerSecondColor
            canvas.drawArc(
                ringInnerSecond!!,
                startAngle,
                chartRingBrakingProgress,
                false,
                ringPaint!!
            )
            ringPaint!!.color = ringInnerFirstColor
            canvas.drawArc(
                ringInnerFirst!!,
                startAngle,
                chartRingAccelerationProgress,
                false,
                ringPaint!!
            )
        } else {
            when (highlightedRing) {
                RING_OVERALL -> {

                    // Draw text
                    textPaint!!.color = ringOverallColor
                    canvas.drawText(
                        overAllText,
                        width / 2f + textMarginLeft,
                        height.toFloat(),
                        textPaint!!
                    )
                    textPaint!!.color = defaultRingFilledColor
                    canvas.drawText(
                        innerThirdText,
                        width / 2f + textMarginLeft,
                        ringInnerThird!!.bottom + innerStrokeWidth / 2,
                        textPaint!!
                    )
                    canvas.drawText(
                        innerSecondText,
                        width / 2f + textMarginLeft,
                        ringInnerSecond!!.bottom + innerStrokeWidth / 2,
                        textPaint!!
                    )
                    canvas.drawText(
                        innerFirstText,
                        width / 2f + textMarginLeft,
                        ringInnerFirst!!.bottom + innerStrokeWidth / 2,
                        textPaint!!
                    )

                    // Draw filled rings
                    ringPaint!!.strokeWidth = outerStrokeWidth
                    ringPaint!!.color = ringOverallColor
                    canvas.drawArc(
                        ringOverall!!,
                        startAngle,
                        chartRingOverallProgress,
                        false,
                        ringPaint!!
                    )
                    ringPaint!!.strokeWidth = innerStrokeWidth
                    ringPaint!!.color = defaultRingFilledColor
                    canvas.drawArc(
                        ringInnerThird!!,
                        startAngle,
                        chartRingSpeedProgress,
                        false,
                        ringPaint!!
                    )
                    canvas.drawArc(
                        ringInnerSecond!!,
                        startAngle,
                        chartRingBrakingProgress,
                        false,
                        ringPaint!!
                    )
                    canvas.drawArc(
                        ringInnerFirst!!,
                        startAngle,
                        chartRingAccelerationProgress,
                        false,
                        ringPaint!!
                    )
                }
                THIRD_INNER_RING -> {

                    // Draw text
                    textPaint!!.color = ringInnerThirdColor
                    canvas.drawText(
                        innerThirdText,
                        width / 2f + textMarginLeft,
                        ringInnerThird!!.bottom + innerStrokeWidth / 2,
                        textPaint!!
                    )
                    textPaint!!.color = defaultRingFilledColor
                    canvas.drawText(
                        overAllText,
                        width / 2f + textMarginLeft,
                        height.toFloat(),
                        textPaint!!
                    )
                    canvas.drawText(
                        innerSecondText,
                        width / 2f + textMarginLeft,
                        ringInnerSecond!!.bottom + innerStrokeWidth / 2,
                        textPaint!!
                    )
                    canvas.drawText(
                        innerFirstText,
                        width / 2f + textMarginLeft,
                        ringInnerFirst!!.bottom + innerStrokeWidth / 2,
                        textPaint!!
                    )

                    // Draw filled rings
                    ringPaint!!.strokeWidth = innerStrokeWidth
                    ringPaint!!.color = ringInnerThirdColor
                    canvas.drawArc(
                        ringInnerThird!!,
                        startAngle,
                        chartRingSpeedProgress,
                        false,
                        ringPaint!!
                    )
                    ringPaint!!.color = defaultRingFilledColor
                    canvas.drawArc(
                        ringInnerSecond!!,
                        startAngle,
                        chartRingBrakingProgress,
                        false,
                        ringPaint!!
                    )
                    canvas.drawArc(
                        ringInnerFirst!!,
                        startAngle,
                        chartRingAccelerationProgress,
                        false,
                        ringPaint!!
                    )
                    ringPaint!!.strokeWidth = outerStrokeWidth
                    canvas.drawArc(
                        ringOverall!!,
                        startAngle,
                        chartRingOverallProgress,
                        false,
                        ringPaint!!
                    )
                }
                SECOND_INNER_RING -> {

                    // Draw text
                    textPaint!!.color = ringInnerSecondColor
                    canvas.drawText(
                        innerSecondText,
                        width / 2f + textMarginLeft,
                        ringInnerSecond!!.bottom + innerStrokeWidth / 2,
                        textPaint!!
                    )
                    textPaint!!.color = defaultRingFilledColor
                    canvas.drawText(
                        overAllText,
                        width / 2f + textMarginLeft,
                        height.toFloat(),
                        textPaint!!
                    )
                    canvas.drawText(
                        innerThirdText,
                        width / 2f + textMarginLeft,
                        ringInnerThird!!.bottom + innerStrokeWidth / 2,
                        textPaint!!
                    )
                    canvas.drawText(
                        innerFirstText,
                        width / 2f + textMarginLeft,
                        ringInnerFirst!!.bottom + innerStrokeWidth / 2,
                        textPaint!!
                    )

                    // Draw filled rings
                    ringPaint!!.strokeWidth = innerStrokeWidth
                    ringPaint!!.color = ringInnerSecondColor
                    canvas.drawArc(
                        ringInnerSecond!!,
                        startAngle,
                        chartRingBrakingProgress,
                        false,
                        ringPaint!!
                    )
                    ringPaint!!.color = defaultRingFilledColor
                    canvas.drawArc(
                        ringInnerThird!!,
                        startAngle,
                        chartRingSpeedProgress,
                        false,
                        ringPaint!!
                    )
                    canvas.drawArc(
                        ringInnerFirst!!,
                        startAngle,
                        chartRingAccelerationProgress,
                        false,
                        ringPaint!!
                    )
                    ringPaint!!.strokeWidth = outerStrokeWidth
                    canvas.drawArc(
                        ringOverall!!,
                        startAngle,
                        chartRingOverallProgress,
                        false,
                        ringPaint!!
                    )
                }
                FIRST_INNER_RING -> {

                    // Draw text
                    textPaint!!.color = ringInnerFirstColor
                    canvas.drawText(
                        innerFirstText,
                        width / 2f + textMarginLeft,
                        ringInnerFirst!!.bottom + innerStrokeWidth / 2,
                        textPaint!!
                    )
                    textPaint!!.color = defaultRingFilledColor
                    canvas.drawText(
                        overAllText,
                        width / 2f + textMarginLeft,
                        height.toFloat(),
                        textPaint!!
                    )
                    canvas.drawText(
                        innerThirdText,
                        width / 2f + textMarginLeft,
                        ringInnerThird!!.bottom + innerStrokeWidth / 2,
                        textPaint!!
                    )
                    canvas.drawText(
                        innerSecondText,
                        width / 2f + textMarginLeft,
                        ringInnerSecond!!.bottom + innerStrokeWidth / 2,
                        textPaint!!
                    )

                    // Draw filled rings
                    ringPaint!!.strokeWidth = innerStrokeWidth
                    ringPaint!!.color = ringInnerFirstColor
                    canvas.drawArc(
                        ringInnerFirst!!,
                        startAngle,
                        chartRingAccelerationProgress,
                        false,
                        ringPaint!!
                    )
                    ringPaint!!.color = defaultRingFilledColor
                    canvas.drawArc(
                        ringInnerThird!!,
                        startAngle,
                        chartRingSpeedProgress,
                        false,
                        ringPaint!!
                    )
                    canvas.drawArc(
                        ringInnerSecond!!,
                        startAngle,
                        chartRingBrakingProgress,
                        false,
                        ringPaint!!
                    )
                    ringPaint!!.strokeWidth = outerStrokeWidth
                    canvas.drawArc(
                        ringOverall!!,
                        startAngle,
                        chartRingOverallProgress,
                        false,
                        ringPaint!!
                    )
                }
                else -> throw IllegalArgumentException("Use one of the constants provided to highlight a ring: FIRST_INNER_RING, SECOND_INNER_RING, THIRD_INNER_RING or RING_OVERALL")
            }
        }
    }

    /*
    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (getRingsClickable()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:

                    if (isOnRing(event, ringOverall, outerStrokeWidth) && isInSweep(event, ringOverall, startAngle, emptyArcAngle)
                            || rectOverallText.contains((int) event.getX(), (int) event.getY()))
                        highlight(RING_OVERALL);

                    if (isOnRing(event, ringInnerThird, outerStrokeWidth) && isInSweep(event, ringInnerThird, startAngle, emptyArcAngle)
                            || rectInnerThirdText.contains((int) event.getX(), (int) event.getY()))
                        highlight(THIRD_INNER_RING);

                    if (isOnRing(event, ringInnerSecond, outerStrokeWidth) && isInSweep(event, ringInnerSecond, startAngle, emptyArcAngle)
                            || rectInnerSecondText.contains((int) event.getX(), (int) event.getY()))
                        highlight(SECOND_INNER_RING);

                    if (isOnRing(event, ringInnerFirst, outerStrokeWidth) && isInSweep(event, ringInnerFirst, startAngle, emptyArcAngle)
                            || rectInnerFirstText.contains((int) event.getX(), (int) event.getY()))
                        highlight(FIRST_INNER_RING);

                    break;
            }

            return true;
        }

        return false;
    }*/
    private fun initByAttributes(attributes: TypedArray) {
        textSize = attributes.getDimension(R.styleable.Rings_rings_text_size, defaultTextSize)
        textMarginLeft =
            attributes.getDimension(R.styleable.Rings_rings_text_margin_left, defaultTextMarginLeft)
        innerStrokeWidth = attributes.getDimension(
            R.styleable.Rings_rings_inner_stroke_width,
            defaultInnerStrokeWidth
        )
        innerStrokeWidthUnfinished = attributes.getDimension(
            R.styleable.Rings_rings_inner_stroke_width_unfinished,
            defaultInnerStrokeUnfinishedWidth
        )
        outerStrokeWidth = attributes.getDimension(
            R.styleable.Rings_rings_outer_stroke_width,
            defaultOuterStrokeWidth
        )
        outerStrokeWidthUnfinished = attributes.getDimension(
            R.styleable.Rings_rings_outer_stroke_width_unfinished,
            defaultOuterStrokeUnfinishedWidth
        )
        ringUnfinishedColor = attributes.getColor(
            R.styleable.Rings_rings_unfinished_color,
            defaultRingUnfinishedColor
        )
        defaultRingFilledColor = attributes.getColor(
            R.styleable.Rings_rings_default_filled_color,
            defaultRingFilledColor
        )
        ringOverallColor =
            attributes.getColor(R.styleable.Rings_rings_overall_color, defaultRingFilledColor)
        ringInnerThirdColor =
            attributes.getColor(R.styleable.Rings_rings_inner_third_color, defaultRingFilledColor)
        ringInnerSecondColor =
            attributes.getColor(R.styleable.Rings_rings_inner_second_color, defaultRingFilledColor)
        ringInnerFirstColor =
            attributes.getColor(R.styleable.Rings_rings_inner_first_color, defaultRingFilledColor)
        setRingOverallProgress(defaultRingOverallProgress, false)
        setRingInnerThirdProgress(defaultRingInnerThirdProgress, false)
        setRingInnerSecondProgress(defaultRingInnerSecondProgress, false)
        setRingInnerFirstProgress(defaultRingInnerFirstProgress, false)
    }

    private fun initPainters() {
        // Ring Rectangle objects
        ringOverall = RectF()
        ringInnerThird = RectF()
        ringInnerSecond = RectF()
        ringInnerFirst = RectF()

        // Init rectangles used by texts
        rectOverallText = Rect()
        rectInnerThirdText = Rect()
        rectInnerSecondText = Rect()
        rectInnerFirstText = Rect()
        // Auxiliary rect to get the width size used by text
        auxRect = Rect()

        // Ring Paint
        ringPaint = Paint()
        ringPaint!!.isAntiAlias = true
        ringPaint!!.style = Paint.Style.STROKE
        ringPaint!!.strokeCap = Paint.Cap.ROUND

        // Text Paint
        textPaint = TextPaint()
        (textPaint as TextPaint).setTextSize(textSize)
        (textPaint as TextPaint).setAntiAlias(true)
    }

    /**
     * Determines which of the rings will be highlighted.
     *
     * @param ring One of [.RING_OVERALL], [.THIRD_INNER_RING], [.SECOND_INNER_RING] or [.FIRST_INNER_RING].
     */
    fun highlight(ring: Short) {
        highlighted = true
        highlightedRing = ring
        invalidate()
    }

    fun unhighlight() {
        highlighted = false
        highlightedRing = -1
        invalidate()
    }

    /**
     * Sets the progress for the overall ring
     *
     * @param overAllProgress progress for the overall ring. From 0 to 100.
     * @param invalidate      causes the view to be redrawn itself by calling [View.invalidate]
     */
    fun setRingOverallProgress(
        overAllProgress: Float,
        invalidate: Boolean
    ) {
        chartRingOverallProgress = emptyArcAngle / 100f * overAllProgress
        if (invalidate) invalidate()
    }

    /**
     * Sets the progress for the third inner ring
     *
     * @param innerThirdProgress progress for the overall ring. From 0 to 100.
     * @param invalidate         causes the view to be redrawn itself by calling [View.invalidate]
     */
    fun setRingInnerThirdProgress(
        innerThirdProgress: Float,
        invalidate: Boolean
    ) {
        chartRingSpeedProgress = emptyArcAngle / 100f * innerThirdProgress
        if (invalidate) invalidate()
    }

    /**
     * Sets the progress for the second inner ring
     *
     * @param innerSecondProgress progress for the overall ring. From 0 to 100.
     * @param invalidate          causes the view to be redrawn itself by calling [View.invalidate]
     */
    fun setRingInnerSecondProgress(
        innerSecondProgress: Float,
        invalidate: Boolean
    ) {
        chartRingBrakingProgress = emptyArcAngle / 100f * innerSecondProgress
        if (invalidate) invalidate()
    }

    /**
     * Sets the progress for the first inner ring
     *
     * @param innerFirstProgress progress for the overall ring. From 0 to 100.
     * @param invalidate         causes the view to be redrawn itself by calling [View.invalidate]
     */
    fun setRingInnerFirstProgress(
        innerFirstProgress: Float,
        invalidate: Boolean
    ) {
        chartRingAccelerationProgress = emptyArcAngle / 100f * innerFirstProgress
        if (invalidate) invalidate()
    }

    companion object {
        private val TAG =
            Rings::class.java.simpleName

        /**
         * Highlight the overall ring.
         * Use with [.highlight].
         */
        const val RING_OVERALL: Short = 1

        /**
         * Highlight the third inner ring.
         * Use with [.highlight].
         */
        const val THIRD_INNER_RING: Short = 2

        /**
         * Highlight the second inner ring.
         * Use with [.highlight].
         */
        const val SECOND_INNER_RING: Short = 3

        /**
         * Highlight the first inner ring.
         * Use with [.highlight].
         */
        const val FIRST_INNER_RING: Short = 4
        private fun isOnRing(
            event: MotionEvent,
            bounds: RectF,
            strokeWidth: Float
        ): Boolean {
            // Figure the distance from center point to touch point.
            val distance = distance(
                event.x, event.y,
                bounds.centerX(), bounds.centerY()
            )

            // Assuming square bounds to figure the radius.
            val radius = bounds.width() / 2f

            // The Paint stroke is centered on the circumference,
            // so the tolerance is half its width.
            val halfStrokeWidth = strokeWidth / 2f

            // Compare the difference to the tolerance.
            return Math.abs(distance - radius) <= halfStrokeWidth
        }

        private fun distance(
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float
        ): Float {
            return Math.sqrt(
                Math.pow(
                    x1 - x2.toDouble(),
                    2.0
                ) + Math.pow(y1 - y2.toDouble(), 2.0)
            ).toFloat()
        }

        private fun isInSweep(
            event: MotionEvent,
            bounds: RectF,
            startAngle: Float,
            sweepAngle: Float
        ): Boolean {
            // Figure atan2 angle.
            val at = Math.toDegrees(
                Math.atan2(
                    event.y - bounds.centerY().toDouble(),
                    event.x - bounds.centerX().toDouble()
                )
            ).toFloat()

            // Convert from atan2 to standard angle.
            val angle = (at + 360) % 360

            // Check if in sweep.
            return angle >= startAngle && angle <= startAngle + sweepAngle
        }
    }

    init {

        // Default values initialization
        defaultTextSize = info.covid.customview.rings.Utils.sp2px(resources, 10f)
        defaultTextMarginLeft = info.covid.customview.rings.Utils.sp2px(resources, 10f)
        defaultInnerStrokeWidth = info.covid.customview.rings.Utils.sp2px(resources, 6f)
        defaultInnerStrokeUnfinishedWidth =
            info.covid.customview.rings.Utils.sp2px(resources, 8f)
        defaultOuterStrokeWidth = info.covid.customview.rings.Utils.sp2px(resources, 6f)
        defaultOuterStrokeUnfinishedWidth =
            info.covid.customview.rings.Utils.sp2px(resources, 8f)
        defaultRingUnfinishedColor = Color.GRAY
        defaultRingFilledColor = Color.parseColor("#E6E6E6")
        defaultRingOverallProgress = 100f
        defaultRingInnerThirdProgress = 0f
        defaultRingInnerSecondProgress = 0f
        defaultRingInnerFirstProgress = 0f
        overAllText = "Ring Overall"
        innerFirstText = "Ring One"
        innerSecondText = "Ring Second"
        innerThirdText = "Ring Third"
        val attributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.Rings, defStyle, 0)
        initByAttributes(attributes)
        attributes.recycle()
        initPainters()
    }
}