package com.locationtracker.ui.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.locationtracker.databinding.ActivityPermissionBinding
import com.locationtracker.ui.main.MainActivity

class PermissionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkPermissions()
    }

    private fun setupUI() {
        // 权限说明文本
        binding.tvPermissionDescription.text = """
为了正常使用位置追踪功能，需要以下权限：

📍 位置权限
- 精确定位：获取精确的GPS位置信息
- 粗略定位：获取网络辅助定位信息

⚙️ 后台运行设置
- 自启动：保证应用在重启后能自动恢复
- 电池优化：允许应用在后台持续运行
- 后台定位：允许应用在后台获取位置信息

请按照引导完成设置，否则应用无法正常工作。
        """.trimIndent()

        // 去设置按钮
        binding.btnGoToSettings.setOnClickListener {
            openAppSettings()
        }

        // 重试按钮
        binding.btnRetryPermissions.setOnClickListener {
            checkPermissions()
        }
    }

    private fun checkPermissions() {
        val permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

        Dexter.withContext(this)
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        // 所有权限已授权
                        showSuccessAndProceed()
                    } else {
                        // 有权限被拒绝
                        showPermissionDenied()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {
                    // 显示权限请求理由
                    token.continuePermissionRequest()
                }
            })
            .check()
    }

    private fun showSuccessAndProceed() {
        binding.tvPermissionStatus.text = "✅ 权限设置完成"
        binding.tvPermissionStatus.setTextColor(getColor(android.R.color.holo_green_dark))

        binding.btnGoToSettings.visibility = android.view.View.GONE
        binding.btnRetryPermissions.visibility = android.view.View.GONE

        // 延迟跳转到主界面
        android.os.Handler(mainLooper).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }

    private fun showPermissionDenied() {
        binding.tvPermissionStatus.text = "❌ 权限被拒绝，请点击下方按钮去设置中授权"
        binding.tvPermissionStatus.setTextColor(getColor(android.R.color.holo_red_dark))

        binding.btnGoToSettings.visibility = android.view.View.VISIBLE
        binding.btnRetryPermissions.visibility = android.view.View.VISIBLE
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}
