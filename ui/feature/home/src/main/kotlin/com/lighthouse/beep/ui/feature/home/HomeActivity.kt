package com.lighthouse.beep.ui.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.lighthouse.beep.auth.BeepAuth
import com.lighthouse.beep.core.ui.exts.repeatOnStarted
import com.lighthouse.beep.core.ui.exts.replace
import com.lighthouse.beep.core.ui.exts.setOnThrottleClickListener
import com.lighthouse.beep.core.ui.exts.setUpSystemInsetsPadding
import com.lighthouse.beep.core.ui.exts.show
import com.lighthouse.beep.permission.BeepPermission
import com.lighthouse.beep.navs.ActivityNavItem
import com.lighthouse.beep.navs.AppNavigator
import com.lighthouse.beep.permission.dialog.StoragePermissionDialog
import com.lighthouse.beep.ui.feature.home.databinding.ActivityHomeBinding
import com.lighthouse.beep.ui.feature.home.model.HomePageState
import com.lighthouse.beep.ui.feature.home.page.empty.HomeEmptyFragment
import com.lighthouse.beep.ui.feature.home.page.home.HomeMainFragment
import com.lighthouse.beep.ui.feature.home.provider.HomeNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import com.lighthouse.beep.theme.R as ThemeR

@AndroidEntryPoint
internal class HomeActivity : AppCompatActivity(), HomeNavigation {

    private lateinit var binding: ActivityHomeBinding

    private val viewModel by viewModels<HomeViewModel>()

    @Inject
    lateinit var navigator: AppNavigator

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (BeepPermission.checkStoragePermission(this)) {
                gotoGallery()
            } else {
                showRequestPermissionDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        setUpSystemInsetsPadding(binding.root)
        setUpCollectState()
        setUpClickEvent()
    }

    private fun setUpCollectState() {
        repeatOnStarted {
            viewModel.homePageState.collect { state ->
                when (state) {
                    HomePageState.MAIN -> replaceMainPage()
                    HomePageState.EMPTY -> replaceEmptyPage()
                    else -> Unit
                }
            }
        }

        repeatOnStarted {
            viewModel.isExistUsedGifticon.collect { isExistUsedGifticon ->
                binding.btnGotoArchive.apply {
                    isEnabled = isExistUsedGifticon
                    alpha = if (isExistUsedGifticon) 1f else 0.4f
                }
            }
        }

        repeatOnStarted {
            BeepAuth.authInfoFlow.filterNotNull().collect {
                Glide.with(this)
                    .load(it.photoUrl)
                    .placeholder(ThemeR.drawable.icon_default_profile)
                    .transform(CircleCrop())
                    .into(binding.imageUserProfile)

                binding.textUsername.text = it.displayName
            }
        }
    }

    private fun replaceMainPage() {
        replace(
            containerId = binding.containerFragment.id,
            tag = HomeMainFragment.TAG,
        ) {
            HomeMainFragment()
        }
    }

    private fun replaceEmptyPage() {
        replace(
            containerId = binding.containerFragment.id,
            tag = HomeEmptyFragment.TAG,
        ) {
            HomeEmptyFragment()
        }
    }

    private fun setUpClickEvent() {
        binding.btnGotoArchive.setOnThrottleClickListener {
            gotoArchive()
        }

        binding.btnGotoSetting.setOnThrottleClickListener {
            gotoSetting()
        }
    }

    private fun showRequestPermissionDialog() {
        show(StoragePermissionDialog.TAG) {
            StoragePermissionDialog().apply {
                setOnPermissionListener { grant ->
                    if (grant) {
                        gotoGallery()
                    }
                }
            }
        }
    }

    override fun gotoGallery() {
        when {
            BeepPermission.checkStoragePermission(this) -> {
                val intent = navigator.getIntent(this, ActivityNavItem.Gallery)
                startActivity(intent)
            }

            else -> {
                galleryLauncher.launch(BeepPermission.storage)
            }
        }
    }

    private fun gotoSetting() {
        val intent = navigator.getIntent(this, ActivityNavItem.Setting)
        startActivity(intent)
    }

    private fun gotoArchive() {
        val intent = navigator.getIntent(this, ActivityNavItem.Archive)
        startActivity(intent)
    }
}