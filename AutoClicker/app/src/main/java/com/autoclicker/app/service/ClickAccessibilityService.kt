package com.autoclicker.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.lifecycle.LifecycleService
import com.autoclicker.app.util.ClickPreferences

/**
 * Accessibility Service that performs automatic clicks on the screen.
 * This service uses Android's Accessibility API to perform gesture-based clicks.
 */
class ClickAccessibilityService : AccessibilityService() {

    companion object {
        const val ACTION_START_CLICKING = "com.autoclicker.START_CLICKING"
        const val ACTION_STOP_CLICKING = "com.autoclicker.STOP_CLICKING"
        const val ACTION_UPDATE_CONFIG = "com.autoclicker.UPDATE_CONFIG"
        
        const val EXTRA_INTERVAL = "click_interval"
        const val EXTRA_X = "target_x"
        const val EXTRA_Y = "target_y"
        const val EXTRA_RANDOM_OFFSET = "random_offset"
        
        @Volatile
        var isRunning: Boolean = false
            private set
        
        private var instance: ClickAccessibilityService? = null
        
        fun getInstance(): ClickAccessibilityService? = instance
        
        fun isServiceEnabled(): Boolean = instance != null
    }

    private val handler = Handler(Looper.getMainLooper())
    private var clickRunnable: Runnable? = null
    private var clickInterval: Long = 1000
    private var targetX: Int = 0
    private var targetY: Int = 0
    private var randomOffset: Int = 0
    private var clickCount: Int = 0
    private var maxClicks: Int = -1 // -1 for unlimited

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_CLICKING -> {
                clickInterval = intent.getLongExtra(EXTRA_INTERVAL, 1000L)
                targetX = intent.getIntExtra(EXTRA_X, 0)
                targetY = intent.getIntExtra(EXTRA_Y, 0)
                randomOffset = intent.getIntExtra(EXTRA_RANDOM_OFFSET, 0)
                maxClicks = intent.getIntExtra("max_clicks", -1)
                clickCount = 0
                startAutoClicking()
            }
            ACTION_STOP_CLICKING -> {
                stopAutoClicking()
            }
            ACTION_UPDATE_CONFIG -> {
                clickInterval = intent.getLongExtra(EXTRA_INTERVAL, clickInterval)
                targetX = intent.getIntExtra(EXTRA_X, targetX)
                targetY = intent.getIntExtra(EXTRA_Y, targetY)
                randomOffset = intent.getIntExtra(EXTRA_RANDOM_OFFSET, randomOffset)
            }
        }
        return START_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAutoClicking()
        instance = null
    }

    private fun startAutoClicking() {
        if (isRunning) return
        
        isRunning = true
        scheduleNextClick()
    }

    fun stopAutoClicking() {
        isRunning = false
        clickRunnable?.let { handler.removeCallbacks(it) }
        clickRunnable = null
    }

    private fun scheduleNextClick() {
        if (!isRunning) return
        
        clickRunnable = Runnable {
            performClick()
            
            // Check if we've reached max clicks
            if (maxClicks > 0 && clickCount >= maxClicks) {
                stopAutoClicking()
                return@Runnable
            }
            
            // Schedule next click
            handler.postDelayed(this, clickInterval)
        }
        
        clickRunnable?.let { handler.post(it) }
    }

    private fun performClick() {
        if (!isRunning) return

        val displayWidth = resources.displayMetrics.widthPixels
        val displayHeight = resources.displayMetrics.heightPixels

        // Calculate actual click position with optional random offset
        val clickX = if (randomOffset > 0) {
            targetX + (Math.random() * randomOffset * 2 - randomOffset).toInt()
        } else {
            targetX
        }.coerceIn(0, displayWidth)

        val clickY = if (randomOffset > 0) {
            targetY + (Math.random() * randomOffset * 2 - randomOffset).toInt()
        } else {
            targetY
        }.coerceIn(0, displayHeight)

        // Create gesture path for the click
        val path = Path()
        path.moveTo(clickX.toFloat(), clickY.toFloat())

        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        // Dispatch the gesture
        dispatchGesture(gesture, null, null)
        
        clickCount++
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not needed for basic auto-clicking
    }

    override fun onInterrupt() {
        // Called when the system needs to interrupt the service
        stopAutoClicking()
    }

    /**
     * Find a node by text and return its bounds
     */
    fun findNodeByText(text: String): Rect? {
        val rootNode = rootInActiveWindow ?: return null
        
        try {
            return findNodeByTextRecursive(rootNode, text)?.bounds?.let { Rect(it) }
        } finally {
            rootNode.recycle()
        }
    }

    private fun findNodeByTextRecursive(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.contains(text, ignoreCase = true) == true) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeByTextRecursive(child, text)
            if (found != null) {
                return found
            }
            child.recycle()
        }

        return null
    }
}
