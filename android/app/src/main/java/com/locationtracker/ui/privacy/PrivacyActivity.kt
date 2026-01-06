package com.locationtracker.ui.privacy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.locationtracker.databinding.ActivityPrivacyBinding
import com.locationtracker.ui.main.MainActivity
import com.locationtracker.utils.PreferenceManager

class PrivacyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // 设置隐私协议文本
        binding.tvPrivacyContent.text = getPrivacyAgreementText()

        // 同意按钮
        binding.btnAgree.setOnClickListener {
            if (binding.cbAgree.isChecked) {
                // 保存同意状态
                PreferenceManager.setPrivacyAccepted(this, true)

                // 跳转到主界面
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                binding.tvError.text = "请先勾选同意条款"
                binding.tvError.visibility = android.view.View.VISIBLE
            }
        }

        // 不同意按钮
        binding.btnDisagree.setOnClickListener {
            // 退出应用
            finishAffinity()
        }
    }

    private fun getPrivacyAgreementText(): String {
        return """
📱 应用用途

位置追踪应用是一个专为家庭成员设计的轻量级位置共享工具，主要功能包括：

- 📍 自动位置采集：每30分钟采集一次当前位置信息
- 📤 安全上报：通过加密连接将位置数据上传到指定服务器
- 🌐 网页查看：通过浏览器查看实时位置和历史轨迹
- ⚡ 省电设计：优化后台运行，尽可能减少电池消耗

👨‍👩‍👧‍👦 使用范围

仅限家庭内部使用：
- 本应用仅供您本人使用
- 不得向第三方分享应用或数据
- 所有位置数据仅存储在您控制的服务器上

🔒 隐私保护原则

数据采集最小化
我们仅采集必要的位置信息：
- 地理位置：经纬度坐标（WGS84坐标系）
- 精度信息：定位精度（米）
- 时间戳：采集时间和服务端接收时间
- 设备标识：用于区分不同设备的唯一ID

不采集的敏感信息
❌ 通讯录、短信、照片等个人数据
❌ WiFi热点信息
❌ 设备IMEI、MAC地址等硬件标识
❌ 应用使用记录

⚙️ 控制权完全在您手中

随时暂停
- 应用内提供一键暂停/恢复开关
- 暂停后立即停止位置采集和上报

数据可删除
- 支持按时间范围删除历史位置数据
- 可清空所有数据重新开始

透明可控
- 所有数据存储在您自己的服务器上
- 代码开源，可自主审查
- 提供详细的运行状态显示

📋 数据保留政策

- 位置数据：默认保留90天，可手动清理
- 超出保留期的数据自动删除
- 服务器日志：仅保留必要的错误排查信息，定期清理

🚫 免责声明

- 本应用不保证位置数据的100%准确性
- 在网络信号弱或GPS不可用的情况下，可能出现定位偏差
- 用户需自行确保设备电量充足和网络连接稳定

        """.trimIndent()
    }
}
