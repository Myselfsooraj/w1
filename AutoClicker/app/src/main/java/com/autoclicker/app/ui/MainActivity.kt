package com.autoclicker.app.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.autoclicker.app.R
import com.autoclicker.app.databinding.ActivityMainBinding
import com.autoclicker.app.service.ClickAccessibilityService
import com.autoclicker.app.util.ClickPreferences
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferences: ClickPreferences
    private lateinit var vibrator: Vibrator

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
        preferences.overlayPermissionGranted = granted
        updateUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = ClickPreferences(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        setupToolbar()
        setupClickListeners()
        loadPreferences()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupClickListeners() {
        binding.btnStartStop.setOnClickListener {
            if (ClickAccessibilityService.isRunning) {
                stopClicking()
            } else {
                startClicking()
            }
        }

        binding.btnGrantOverlayPermission.setOnClickListener {
            requestOverlayPermission()
        }

        binding.btnEnableAccessibility.setOnClickListener {
            requestAccessibilityPermission()
        }

        binding.fabSettings.setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun loadPreferences() {
        binding.etClickInterval.setText(preferences.clickInterval.toString())
        binding.etTargetX.setText(preferences.targetX.toString())
        binding.etTargetY.setText(preferences.targetY.toString())
        binding.etRandomOffset.setText(preferences.randomOffset.toString())
        binding.switchVibration.isChecked = preferences.vibrationEnabled
        binding.switchClickIndicator.isChecked = preferences.showClickIndicator
    }

    private fun savePreferences() {
        preferences.clickInterval = binding.etClickInterval.text.toString().toLongOrNull() ?: 1000L
        preferences.targetX = binding.etTargetX.text.toString().toIntOrNull() ?: 0
        preferences.targetY = binding.etTargetY.text.toString().toIntOrNull() ?: 0
        preferences.randomOffset = binding.etRandomOffset.text.toString().toIntOrNull() ?: 0
        preferences.vibrationEnabled = binding.switchVibration.isChecked
        preferences.showClickIndicator = binding.switchClickIndicator.isChecked
    }

    private fun updateUI() {
        val isServiceEnabled = ClickAccessibilityService.isServiceEnabled()
        val isRunning = ClickAccessibilityService.isRunning
        val hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }

        // Update button state
        binding.btnStartStop.isEnabled = isServiceEnabled && hasOverlayPermission
        binding.btnStartStop.text = if (isRunning) {
            getString(R.string.stop_clicking)
        } else {
            getString(R.string.start_clicking)
        }

        // Update permission status
        binding.overlayPermissionStatus.text = if (hasOverlayPermission) {
            "✓ Overlay Permission Granted"
        } else {
            "✗ Overlay Permission Required"
        }

        binding.accessibilityPermissionStatus.text = if (isServiceEnabled) {
            "✓ Accessibility Service Enabled"
        } else {
            "✗ Accessibility Service Required"
        }

        // Show/hide permission buttons
        binding.btnGrantOverlayPermission.visibility = if (!hasOverlayPermission) {
            View.VISIBLE
        } else {
            View.GONE
        }

        binding.btnEnableAccessibility.visibility = if (!isServiceEnabled) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Update status text
        binding.tvStatus.text = if (isRunning) {
            getString(R.string.status_running)
        } else {
            getString(R.string.status_stopped)
        }
    }

    private fun startClicking() {
        if (!ClickAccessibilityService.isServiceEnabled()) {
            requestAccessibilityPermission()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return
        }

        savePreferences()

        val intent = Intent(this, ClickAccessibilityService::class.java).apply {
            action = ClickAccessibilityService.ACTION_START_CLICKING
            putExtra(ClickAccessibilityService.EXTRA_INTERVAL, preferences.clickInterval)
            putExtra(ClickAccessibilityService.EXTRA_X, preferences.targetX)
            putExtra(ClickAccessibilityService.EXTRA_Y, preferences.targetY)
            putExtra(ClickAccessibilityService.EXTRA_RANDOM_OFFSET, preferences.randomOffset)
            putExtra("max_clicks", preferences.maxClicks)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        if (preferences.vibrationEnabled) {
            vibrate()
        }

        updateUI()
    }

    private fun stopClicking() {
        val intent = Intent(this, ClickAccessibilityService::class.java).apply {
            action = ClickAccessibilityService.ACTION_STOP_CLICKING
        }
        stopService(intent)

        if (preferences.vibrationEnabled) {
            vibrate()
        }

        updateUI()
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    private fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun showSettingsDialog() {
        val items = arrayOf(
            getString(R.string.click_interval),
            getString(R.string.target_x),
            getString(R.string.target_y),
            getString(R.string.random_offset),
            getString(R.string.click_count)
        )

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.settings))
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showNumberInputDialog(getString(R.string.click_interval), preferences.clickInterval) {
                        preferences.clickInterval = it
                        binding.etClickInterval.setText(it.toString())
                    }
                    1 -> showNumberInputDialog(getString(R.string.target_x), preferences.targetX) {
                        preferences.targetX = it
                        binding.etTargetX.setText(it.toString())
                    }
                    2 -> showNumberInputDialog(getString(R.string.target_y), preferences.targetY) {
                        preferences.targetY = it
                        binding.etTargetY.setText(it.toString())
                    }
                    3 -> showNumberInputDialog(getString(R.string.random_offset), preferences.randomOffset) {
                        preferences.randomOffset = it
                        binding.etRandomOffset.setText(it.toString())
                    }
                    4 -> showNumberInputDialog(getString(R.string.click_count), 
                        if (preferences.maxClicks < 0) 0 else preferences.maxClicks) {
                        preferences.maxClicks = if (it == 0) -1 else it
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showNumberInputDialog(title: String, defaultValue: Int, onSave: (Int) -> Unit) {
        val input = android.widget.EditText(this).apply {
            setText(defaultValue.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val value = input.text.toString().toIntOrNull() ?: defaultValue
                onSave(value)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        savePreferences()
    }
}
