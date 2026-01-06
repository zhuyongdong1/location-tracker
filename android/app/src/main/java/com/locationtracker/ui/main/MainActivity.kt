package com.locationtracker.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.locationtracker.databinding.ActivityMainBinding
import com.locationtracker.ui.permission.PermissionActivity
import com.locationtracker.ui.privacy.PrivacyActivity
import com.locationtracker.utils.PreferenceManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // 检查是否已同意隐私协议
        if (!PreferenceManager.isPrivacyAccepted(this)) {
            startActivity(Intent(this, PrivacyActivity::class.java))
            finish()
            return
        }

        // 检查权限
        if (!viewModel.hasRequiredPermissions()) {
            startActivity(Intent(this, PermissionActivity::class.java))
            finish()
            return
        }

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // 位置上报开关
        binding.switchLocationReporting.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setLocationReportingEnabled(isChecked)
        }


        // 复制设备信息
        binding.btnCopyDeviceInfo.setOnClickListener {
            viewModel.copyDeviceInfo()
        }
    }

    private fun observeViewModel() {
        // 上报开关状态
        viewModel.isReportingEnabled.observe(this) { enabled ->
            binding.switchLocationReporting.isChecked = enabled
        }

        // 消息提示
        viewModel.message.observe(this) { message ->
            android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}
