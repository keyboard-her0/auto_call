package com.keyboardhero.call.features.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.keyboardhero.call.core.base.BaseActivity
import com.keyboardhero.call.databinding.ActivitySplashBinding
import com.keyboardhero.call.features.MainActivity
import com.keyboardhero.call.shared.data.AppPreference
import com.keyboardhero.call.shared.domain.ActiveDeviceUseCase
import com.keyboardhero.call.shared.domain.RefreshTokenUseCase
import com.keyboardhero.call.shared.domain.data
import com.keyboardhero.call.shared.domain.succeeded
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivitySplashBinding
        get() = ActivitySplashBinding::inflate

    @Inject
    lateinit var appReference: AppPreference

    @Inject
    lateinit var activeDeviceUseCase: ActiveDeviceUseCase

    @Inject
    lateinit var refreshTokenUseCase: RefreshTokenUseCase

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnStart.setOnClickListener {
            val code = binding.txtActiveCode.text.toString().trim()
            lifecycleScope.launch {
                showLoading()
                val result = activeDeviceUseCase(ActiveDeviceUseCase.Parameter(code))
                hideLoading()
                val data = result.data?.data
                if (result.succeeded && data != null) {
                    appReference.accessToken = data.accessToken ?: ""
                    appReference.refreshToken = data.refreshToken ?: ""
                    openApp()
                } else {
                    showSingleOptionDialog(
                        title = "Thông báo",
                        message = result.data?.error?.message ?: "Có lỗi xảy ra, Thử lại sau",
                        button = "Ok",
                    )
                }
            }
        }
    }

    private fun showLoading() {
        binding.loadingView.root.isVisible = true
    }

    private fun hideLoading() {
        binding.loadingView.root.isVisible = false
    }

    override fun onResume() {
        super.onResume()
        refreshToken()
    }

    private fun refreshToken() {
        lifecycleScope.launch {
            showLoading()
            binding.layoutContainer.isVisible = false
            val result = refreshTokenUseCase(Unit)
            hideLoading()
            if (result.data == true) {
                openApp()
            } else {
                binding.layoutContainer.isVisible = true
            }
        }
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("NAM", false)
        startActivity(intent)
        finish()
    }
}
