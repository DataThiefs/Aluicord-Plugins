package com.github.platformspoofer

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable

enum class PlatformType { DESKTOP, MOBILE, WEB }

/**
 * Kaynak dosyası gerektirmeden kodla çizilen platform simgesi.
 * Renk, kullanıcının o platformdaki durumuna (çevrimiçi / boşta / rahatsız etmeyin) göre verilir.
 */
class PlatformIconDrawable(private val type: PlatformType, color: Int) : Drawable() {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val rect = RectF()
    private val path = Path()

    override fun draw(canvas: Canvas) {
        val b = bounds
        val s = minOf(b.width(), b.height()).toFloat()
        if (s <= 0f) return
        val l = b.left + (b.width() - s) / 2f
        val t = b.top + (b.height() - s) / 2f
        stroke.strokeWidth = s * 0.1f

        when (type) {
            PlatformType.DESKTOP -> {
                // Monitör
                rect.set(l + s * 0.06f, t + s * 0.12f, l + s * 0.94f, t + s * 0.70f)
                canvas.drawRoundRect(rect, s * 0.08f, s * 0.08f, fill)
                // Ayak
                rect.set(l + s * 0.44f, t + s * 0.70f, l + s * 0.56f, t + s * 0.84f)
                canvas.drawRect(rect, fill)
                rect.set(l + s * 0.26f, t + s * 0.82f, l + s * 0.74f, t + s * 0.92f)
                canvas.drawRoundRect(rect, s * 0.04f, s * 0.04f, fill)
            }

            PlatformType.MOBILE -> {
                rect.set(l + s * 0.26f, t + s * 0.04f, l + s * 0.74f, t + s * 0.96f)
                canvas.drawRoundRect(rect, s * 0.1f, s * 0.1f, fill)
            }

            PlatformType.WEB -> {
                val cx = l + s / 2f
                val cy = t + s / 2f
                val r = s * 0.40f
                canvas.drawCircle(cx, cy, r, stroke)
                // Boylam
                rect.set(cx - r * 0.45f, cy - r, cx + r * 0.45f, cy + r)
                canvas.drawOval(rect, stroke)
                // Ekvator
                path.reset()
                path.moveTo(cx - r, cy)
                path.lineTo(cx + r, cy)
                canvas.drawPath(path, stroke)
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        fill.alpha = alpha
        stroke.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        fill.colorFilter = colorFilter
        stroke.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

