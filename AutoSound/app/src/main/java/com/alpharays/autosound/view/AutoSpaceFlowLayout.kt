package com.alpharays.autosound.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import pub.devrel.easypermissions.BuildConfig

class AutoSpaceFlowLayout : ViewGroup {
    private var maxHeight = 0

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (BuildConfig.DEBUG && MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            throw AssertionError()
        }
        val childCount = childCount
        val width = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        var height = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom
        var xPosition = paddingLeft
        var yPosition = paddingTop
        val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST)
        val childHeightMeasureSpec: Int =
            if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
            } else {
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            }
        maxHeight = 0
        var isXPaddingMeasured = false
        var xPadding = 0
        var widthOccupied = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
                val childWidth = child.measuredWidth
                if (!isXPaddingMeasured) {
                    xPadding = calculateXPadding(childCount, width, childWidth)
                    isXPaddingMeasured = true
                }
                maxHeight = Math.max(maxHeight, child.measuredHeight + Y_PADDING)

                //Moving the child to next 'line' if it can't fit in the same 'line'
                if (widthOccupied + childWidth > width) {
                    xPosition = paddingLeft
                    widthOccupied = 0
                    yPosition += maxHeight + paddingTop
                }
                xPosition += childWidth + xPadding
                widthOccupied += childWidth + xPadding
            }
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            height = yPosition + maxHeight
        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST && yPosition + maxHeight < height) {
            height = yPosition + maxHeight
        }

        //Fudge to avoid clipping at the bottom of last row.
        height += 5
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val availableWidth = right - left - paddingStart - paddingEnd
        var xPosition = paddingLeft
        var yPosition = paddingTop
        var widthOccupied = 0
        Log.d(TAG, "onLayout: paddingRight : $paddingRight")
        val childCount = childCount
        val xPadding = calculateXPadding(childCount, availableWidth, getChildAt(0).measuredWidth)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight

                //Moving the child to next row if it can't fit in the same row
                if (widthOccupied + childWidth > availableWidth) {
                    xPosition = paddingLeft
                    widthOccupied = 0
                    yPosition += maxHeight + paddingTop
                }
                child.layout(xPosition, yPosition, xPosition + childWidth, yPosition + childHeight)

                //Setting the xPosition for the next child
                xPosition += childWidth + xPadding
                widthOccupied += childWidth + xPadding
            }
        }
    }

    //Calculating the xPadding between the children
    //NOTE: This method assumes that every children has the same dimension!
    //      May result in overflow if each child is of different size.
    private fun calculateXPadding(childCount: Int, availableWidth: Int, childWidth: Int): Int {
        val maxChildrenPerRow = Math.min(childCount, availableWidth / childWidth)
        return availableWidth - maxChildrenPerRow * childWidth / (maxChildrenPerRow - 1)
    }

    companion object {
        private const val Y_PADDING = 2
        private const val TAG = "AutoSpaceFlowLayout"
    }
}