package com.lighthouse.presentation.ui.addgifticon

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lighthouse.presentation.R
import com.lighthouse.presentation.databinding.ActivityAddGifticonBinding
import com.lighthouse.presentation.ui.addgifticon.adapter.AddGifticonAdapter
import com.lighthouse.presentation.ui.addgifticon.event.AddGifticonDirections
import com.lighthouse.presentation.ui.cropgifticon.CropGifticonActivity
import com.lighthouse.presentation.ui.gallery.GalleryActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddGifticonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddGifticonBinding

    private val viewModel: AddGifticonViewModel by viewModels()

    private val gifticonAdapter = AddGifticonAdapter(
        onClickGallery = {
            viewModel.gotoGallery()
        },
        onClickGifticon = {},
        onDeleteGifticon = {}
    )

    val gallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_gifticon)
        binding.apply {
            lifecycleOwner = this@AddGifticonActivity
            vm = viewModel
        }

        binding.rvGifticon.adapter = gifticonAdapter
        setUpDirections()
    }

    private fun setUpDirections() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.directionsFlow.collect { directions ->
                    navigate(directions)
                }
            }
        }
    }

    private fun navigate(directions: AddGifticonDirections) {
        when (directions) {
            AddGifticonDirections.Back -> {
                cancelAddGifticon()
            }
            is AddGifticonDirections.Gallery -> {
                gotoGallery(directions.list)
            }
            is AddGifticonDirections.CropGifticon -> {
                gotoCropGallery(directions.origin)
            }
            is AddGifticonDirections.OriginGifticon -> {
                showOriginGifticonDialog(directions.origin)
            }
        }
    }

    private fun cancelAddGifticon() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun gotoGallery(list: List<Uri>) {
        val intent = Intent(this, GalleryActivity::class.java)
        gallery.launch(intent)
    }

    private fun gotoCropGallery(uri: Uri) {
        val intent = Intent(this, CropGifticonActivity::class.java)
        gallery.launch(intent)
    }

    private fun showOriginGifticonDialog(uri: Uri) {}
}
