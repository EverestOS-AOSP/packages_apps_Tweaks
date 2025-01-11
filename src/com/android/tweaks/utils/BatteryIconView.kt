/*
 * Copyright (C) 2024-2025 The EverestOS Android Project
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
 */

package com.android.tweaks.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.BatteryManager
import android.util.AttributeSet
import android.view.View

class BatteryIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val CRITICAL_LEVEL = 15
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        isDither = true
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        try {
            typeface = Typeface.createFromAsset(context.assets, "SanFranciscoText-Semibold.otf")
        } catch (e: Exception) {
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        }
    }

    private val bodyRect = RectF()
    private val capRect = RectF()
    private val fillRect = RectF()

    private var batteryLevel = 0
    private var isCharging = false
    private var showPercent = true
    private var voltage = 0f
    private var isAttached = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                            status == BatteryManager.BATTERY_STATUS_FULL
                voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f
                invalidate()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = width / 2 // Maintain 2:1 aspect ratio
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val strokeWidth = h * 0.12f
        val cornerRadius = h * 0.2f
        val capWidth = w * 0.12f

        bodyRect.set(
            strokeWidth / 2,
            strokeWidth / 2,
            w - capWidth - strokeWidth / 2,
            h - strokeWidth / 2
        )

        capRect.set(
            w - capWidth,
            h * 0.35f,
            w - strokeWidth / 2,
            h * 0.65f
        )

        updateFillRect()
    }

    private fun updateFillRect() {
        val strokeWidth = height * 0.12f
        val fillWidth = (bodyRect.width() * batteryLevel / 100)
        
        fillRect.set(
            bodyRect.left + strokeWidth,
            bodyRect.top + strokeWidth,
            bodyRect.left + fillWidth - strokeWidth,
            bodyRect.bottom - strokeWidth
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val strokeWidth = height * 0.12f
        val thinStrokeWidth = height * 0.06f
        val cornerRadius = height * 0.2f
        
        // Draw thin grey battery outline
        paint.apply {
            style = Paint.Style.STROKE
            setStrokeWidth(thinStrokeWidth)
            shader = null
            color = Color.GRAY
            alpha = 180
        }
        
        canvas.drawRoundRect(bodyRect, cornerRadius, cornerRadius, paint)
        canvas.drawRoundRect(capRect, cornerRadius * 0.3f, cornerRadius * 0.3f, paint)

        // Draw thick white battery level outline
        if (batteryLevel > 0) {
            val fillWidth = (bodyRect.width() * batteryLevel / 100)
            val left = bodyRect.left
            val top = bodyRect.top
            val right = left + fillWidth
            val bottom = bodyRect.bottom
            
            val levelRect = RectF(left, top, right, bottom)
            
            paint.apply {
                setStrokeWidth(strokeWidth)
                color = Color.WHITE
                alpha = 255
            }
            canvas.drawRoundRect(levelRect, cornerRadius, cornerRadius, paint)
            
            // If battery level fills the whole battery, draw the cap in white too
            if (batteryLevel >= 95) {
                canvas.drawRoundRect(capRect, cornerRadius * 0.3f, cornerRadius * 0.3f, paint)
            }
        }

        // Draw percentage text
        if (showPercent) {
            val textSize = width * 0.26f
            textPaint.textSize = textSize
            textPaint.color = Color.WHITE
            
            val text = batteryLevel.toString()
            val pctX = bodyRect.centerX()
            val pctY = bodyRect.centerY() + (textSize * 0.35f)
            
            canvas.drawText(text, pctX, pctY, textPaint)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isAttached) {
            isAttached = true
            context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (isAttached) {
            context.unregisterReceiver(batteryReceiver)
            isAttached = false
        }
    }
}