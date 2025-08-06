package com.aula.app_fluxar.ui.fragment

import androidx.exifinterface.media.ExifInterface
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.aula.app_fluxar.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.InputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NavigationPerfil.newInstance] factory method to
 * create an instance of this fragment.
 */
class NavigationPerfil : Fragment() {
    private lateinit var defaultProfilePhoto: ImageView
    private var bitmapProfilePhoto: Bitmap? = null
    private var photoUri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.entries.all { it.value }
        if (allPermissionsGranted) {
            showPhotoOptions()
        } else {
            Toast.makeText(
                requireContext(),
                "Permissões necessárias para acessar a câmera/galeria",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val takePhotoResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && photoUri != null) {
            try {
                Glide.with(this)
                    .load(photoUri)
                    .transform(CircleCrop())
                    .into(defaultProfilePhoto)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val pickPhotoResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri? = result.data?.data
            selectedImage?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .transform(CircleCrop())
                    .into(defaultProfilePhoto)
            }
        }
    }


    private fun rotateImageIfRequired(bitmap: Bitmap, uri: Uri? = null): Bitmap {
        if (uri == null) return bitmap

        return try {
            val inputStream: InputStream? = requireActivity().contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                    else -> bitmap
                }
            } ?: bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nav_perfil, container, false)

        defaultProfilePhoto = view.findViewById(R.id.fotoPerfilPadrao)
        defaultProfilePhoto.setOnClickListener {
            verifyPemissions()
        }

        return view
    }

    private fun showPhotoOptions() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alterar foto de perfil")
            .setItems(arrayOf("Tirar foto", "Escolher da galeria", "Cancelar")) { dialog, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> chooseFromGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun verifyPemissions() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                showPhotoOptions()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))
            }
        }
    }

    private fun takePhoto() {
        val photoFile = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "profile_photo_${System.currentTimeMillis()}.jpg")

        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        takePhotoResult.launch(intent)
    }


    private fun chooseFromGallery() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickPhotoResult.launch(pickPhotoIntent)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment navigation_perfil.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NavigationPerfil().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}