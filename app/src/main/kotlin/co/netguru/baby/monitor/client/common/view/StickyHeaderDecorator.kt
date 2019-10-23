package co.netguru.baby.monitor.client.common.view

import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class StickyHeaderDecorator(
    private val listener: StickyHeaderInterface
) : RecyclerView.ItemDecoration() {

    private var stickyHeaderHeight = 0

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)
        val topChild = parent.getChildAt(0) ?: return

        val topChildPosition = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) {
            return
        }

        val headerPos = listener.getHeaderPositionForItem(topChildPosition)
        val currentHeader = getHeaderViewForItem(headerPos, parent)
        fixLayoutSize(parent, currentHeader)
        val contactPoint = currentHeader.bottom
        val childInContact = getChildInContact(parent, contactPoint, headerPos)

        if (childInContact != null && isHeader(parent, childInContact)) {
            moveHeader(canvas, currentHeader, childInContact)
            return
        }

        moveHeader(canvas, currentHeader)
    }

    private fun getHeaderViewForItem(headerPosition: Int, parent: RecyclerView): View {
        val layoutResId = listener.getHeaderLayout(headerPosition)
        val header = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        listener.bindHeaderData(header, headerPosition)
        return header
    }

    private fun moveHeader(canvas: Canvas, currentHeader: View, nextHeader: View? = null) {
        canvas.save()
        canvas.translate(0f, nextHeader?.let { it.top.toFloat() - currentHeader.height } ?: 0f)
        currentHeader.draw(canvas)
        canvas.restore()
    }

    private fun getChildInContact(
        parent: RecyclerView,
        contactPoint: Int,
        currentHeaderPos: Int
    ): View? {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)

            val heightTolerance =
                if (currentHeaderPos != i && isHeader(parent, child)) {
                    stickyHeaderHeight - child.height
                } else {
                    0
                }

            val childBottomPosition = if (child.top > 0) {
                child.bottom + heightTolerance
            } else {
                child.bottom
            }

            if (childBottomPosition > contactPoint && child.top <= contactPoint) {
                return child
            }
        }
        return null
    }

    private fun isHeader(
        parent: RecyclerView,
        childInContact: View
    ) = listener.isHeader(
        parent.getChildAdapterPosition(childInContact)
    )

    /**
     * Properly measures and layouts the top sticky header.
     * @param parent ViewGroup: RecyclerView in this case.
     */
    private fun fixLayoutSize(parent: ViewGroup, view: View) {

        // Specs for parent (RecyclerView)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec =
            View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        // Specs for children (headers)
        val childWidthSpec = ViewGroup.getChildMeasureSpec(
            widthSpec,
            parent.paddingLeft + parent.paddingRight,
            view.layoutParams.width
        )
        val childHeightSpec = ViewGroup.getChildMeasureSpec(
            heightSpec,
            parent.paddingTop + parent.paddingBottom,
            view.layoutParams.height
        )

        view.measure(childWidthSpec, childHeightSpec)
        stickyHeaderHeight = view.measuredHeight
        view.layout(0, 0, view.measuredWidth, stickyHeaderHeight)
    }
}
