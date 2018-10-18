/*
 * Copyright (C) 2015 tyorikan
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.tyorikan.voicerecordingvisualizer

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.FrameLayout
import java.util.*

/**
 * A class that draws visualizations of data received from [RecordingSampler]
 *
 *
 * Created by tyorikan on 2015/06/08.
 */
class VisualizerView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private var numColumns: Int = 0
    private var renderColor: Int = 0
    private var type: Int = 0
    private var renderRange: Int = 0
    private var baseY: Int = 0
    private var volumeQueue: Queue<Int>
    private var maxVolume: Int = 5_000

    private var canvas: Canvas? = null
    private var canvasBitmap: Bitmap? = null
    private val rect = Rect()
    private val paint = Paint()
    private val fadePaint = Paint()

    private var columnWidth: Float = 0f
    private var space: Float = 0f

    init {
        val args = context.obtainStyledAttributes(attrs, R.styleable.visualizerView)

        numColumns = args.getInteger(R.styleable.visualizerView_numColumns, DEFAULT_NUM_COLUMNS)
        renderColor = args.getColor(R.styleable.visualizerView_renderColor, Color.WHITE)
        type = args.getInt(R.styleable.visualizerView_renderType, Type.BAR.flag)
        renderRange = args.getInteger(R.styleable.visualizerView_renderRange, RENDAR_RANGE_TOP)
        volumeQueue = ArrayDeque(numColumns)
        volumeQueue.addAll(List(numColumns) { 0 })

        args.recycle()
        paint.color = renderColor
        fadePaint.color = Color.argb(138, 255, 255, 255)
    }

    /**
     * @param baseY center Y position of visualizer
     */

    fun setBaseY(baseY: Int) {
        this.baseY = baseY
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Create canvas once we're ready to draw
        rect.set(0, 0, width, height)

        if (canvasBitmap == null) {
            canvasBitmap = Bitmap.createBitmap(
                    canvas.width, canvas.height, Bitmap.Config.ARGB_8888)
        }

        if (this.canvas == null) {
            this.canvas = Canvas(canvasBitmap!!)
        }

        if (numColumns > width) {
            numColumns = DEFAULT_NUM_COLUMNS
        }

        columnWidth = width.toFloat() / numColumns.toFloat()
        space = columnWidth / 6f

        if (baseY == 0) {
            baseY = height / 2
        }

        canvas.drawBitmap(canvasBitmap!!, Matrix(), null)
    }

    fun receive(data: ShortArray) {
        if (volumeQueue.size >= numColumns) {
            volumeQueue.poll()
        }
        val amp = data.map { Math.abs(it.toInt()) }.max() ?: 0
        maxVolume = Math.max(maxVolume, amp)
        volumeQueue.offer(amp)
        receive(amp)
    }

    /**
     * receive volume from [RecordingSampler]
     *
     * @param volume volume from mic input
     */
    private fun receive(volume: Int) {
        Handler(Looper.getMainLooper()).post(Runnable {
            canvas ?: return@Runnable

            when {
                volume == 0 -> {
                    canvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                }
                type and Type.FADE.flag != 0 -> {
                    // Fade out old contents
                    fadePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                    canvas!!.drawPaint(fadePaint)
                }
                else -> {
                    canvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                }
            }

            if (type and Type.BAR.flag != 0) {
                drawBar()
            }
            if (type and Type.PIXEL.flag != 0) {
                drawPixel()
            }
            invalidate()
        })
    }

    private fun drawBar() {
        for (i in 0 until numColumns) {
            val volume = if (volumeQueue.size > i && volumeQueue.toTypedArray()[i] != null) {
                volumeQueue.toTypedArray()[i] as Int
            } else {
                0
            }
            val height = getRandomHeight(volume)
            val left = i * columnWidth + space
            val right = (i + 1) * columnWidth - space

            val rect = createRectF(left, right, height)
            canvas!!.drawRect(rect, paint)
        }
    }

    private fun drawPixel() {
        for (i in 0 until numColumns) {
            val volume = if (volumeQueue.toTypedArray()[i] != null) {
                volumeQueue.toTypedArray()[i] as Int
            } else {
                0
            }

            val height = getRandomHeight(volume)
            val left = i * columnWidth + space
            val right = (i + 1) * columnWidth - space

            var drawCount = (height / (right - left)).toInt()
            if (drawCount == 0) {
                drawCount = 1
            }
            val drawHeight = height / drawCount

            // draw each pixel
            for (j in 0 until drawCount) {

                val top: Float
                val bottom: Float
                val rect: RectF

                when (renderRange) {
                    RENDAR_RANGE_TOP -> {
                        bottom = baseY - drawHeight * j
                        top = bottom - drawHeight + space
                        rect = RectF(left, top, right, bottom)
                    }

                    RENDAR_RANGE_BOTTOM -> {
                        top = baseY + drawHeight * j
                        bottom = top + drawHeight - space
                        rect = RectF(left, top, right, bottom)
                    }

                    RENDAR_RANGE_TOP_BOTTOM -> {
                        bottom = baseY - height / 2 + drawHeight * j
                        top = bottom - drawHeight + space
                        rect = RectF(left, top, right, bottom)
                    }

                    else -> return
                }
                canvas!!.drawRect(rect, paint)
            }
        }
    }

    private fun getRandomHeight(volume: Int): Float {
        val height = when (renderRange) {
            RENDAR_RANGE_TOP -> baseY.toFloat()
            RENDAR_RANGE_BOTTOM -> (height - baseY).toFloat()
            RENDAR_RANGE_TOP_BOTTOM -> height.toFloat()
            else -> height.toFloat()
        }
        return height * (volume.toFloat() / maxVolume)
    }

    private fun createRectF(left: Float, right: Float, height: Float) = when (renderRange) {
        RENDAR_RANGE_TOP -> RectF(left, baseY - height, right, baseY.toFloat())
        RENDAR_RANGE_BOTTOM -> RectF(left, baseY.toFloat(), right, baseY + height)
        RENDAR_RANGE_TOP_BOTTOM -> RectF(left, baseY - height, right, baseY + height)
        else -> RectF(left, baseY - height, right, baseY.toFloat())
    }

    /**
     * visualizer type
     */
    enum class Type constructor(val flag: Int) {
        BAR(0x1), PIXEL(0x2), FADE(0x4)
    }

    companion object {
        private const val DEFAULT_NUM_COLUMNS = 20
        private const val RENDAR_RANGE_TOP = 0
        private const val RENDAR_RANGE_BOTTOM = 1
        private const val RENDAR_RANGE_TOP_BOTTOM = 2
    }
}
