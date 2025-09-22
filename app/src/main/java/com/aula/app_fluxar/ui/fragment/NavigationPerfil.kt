package com.aula.app_fluxar.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aula.app_fluxar.API.model.Employee
import com.aula.app_fluxar.API.model.UpdatePhotoRequest
import com.aula.app_fluxar.API.viewModel.UpdateFotoViewModel
import com.aula.app_fluxar.R
import com.aula.app_fluxar.cloudnary.CloudnaryConfig
import com.aula.app_fluxar.databinding.FragmentNavPerfilBinding
import com.aula.app_fluxar.ui.activity.MainActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.cloudinary.android.MediaManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class NavigationPerfil : Fragment() {
    private lateinit var defaultProfilePhoto: ImageView
    private var photoUri: Uri? = null
    private lateinit var binding: FragmentNavPerfilBinding
    private var employee: Employee? = null
    private lateinit var updateFotoViewModel: UpdateFotoViewModel

    // Variável para armazenar a URL da foto
    private var profilePhotoUrl: String? = null

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

                uploadToCloudinary(photoUri!!)
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

                uploadToCloudinary(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        CloudnaryConfig.init(requireContext())
        binding = FragmentNavPerfilBinding.inflate(inflater, container, false)

        updateFotoViewModel = ViewModelProvider(this).get(UpdateFotoViewModel::class.java)

        observeUpdatePhoto()

        employee = (activity as? MainActivity)?.getEmployee()

        if (employee != null) {
            updateUIWithUserData(employee!!)
        } else {
            Toast.makeText(requireContext(), "Dados do usuário não disponíveis", Toast.LENGTH_SHORT).show()
        }

        defaultProfilePhoto = binding.fotoPerfilPadrao
        defaultProfilePhoto.setOnClickListener {
            verifyPermissions()
        }

        return binding.root
    }

    private fun observeUpdatePhoto() {
        updateFotoViewModel.updateFotoResult.observe(viewLifecycleOwner) { result ->
            result?.let { responseMap ->
                profilePhotoUrl?.let { fotoUrl ->
                    employee = employee?.copy(fotoPerfil = fotoUrl)
                    loadProfilePhoto(fotoUrl)

                    Toast.makeText(requireContext(), "Foto atualizada com sucesso!", Toast.LENGTH_SHORT).show()

                    employee?.let { emp ->
                        (activity as? MainActivity)?.updateEmployee(emp)
                    }
                } ?: run {
                    Toast.makeText(requireContext(), "Erro: URL da foto não encontrada", Toast.LENGTH_SHORT).show()
                    Log.e("UpdatePhoto", "profilePhotoUrl está null. Resposta servidor: $responseMap")
                }
            }
        }

        updateFotoViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
                if (error.isNotEmpty()) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                    Log.e("UpdatePhoto", error)
                }
            }

        updateFotoViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                Toast.makeText(requireContext(), "Salvando foto...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        employee = (activity as? MainActivity)?.getEmployee()
        employee?.let {
            profilePhotoUrl = it.fotoPerfil
            updateUIWithUserData(it)
        }
    }

    @SuppressLint("ResourceType", "SetTextI18n")
    private fun updateUIWithUserData(employee: Employee) {
        try {
            binding.nomeGestor.text = "${employee.nome ?: ""} ${employee.sobrenome ?: ""}"
            binding.nomeEmpresaGestor.text = employee.unit.industry.nome ?: "Indisponível"
            binding.setorGestor.text = "Setor: ${employee.setor.nome}" ?: "Indisponível"
            binding.cnpjEmpresaGestor.text = formatCNPJ(employee.unit.industry.cnpj) ?: "Indisponível"
            binding.unidadeGestor.text = employee.unit.nome ?: "Indisponível"
            binding.enderecoUnidadeGestor.text = "${employee.unit.rua}, ${employee.unit.numero}" ?: "Indisponível"
            binding.estoqueGestor.text = "Capacidade máx. - ${employee.capacidadeMaxima}m³" ?: "Indisponível"

            profilePhotoUrl = employee.fotoPerfil

            loadProfilePhoto(profilePhotoUrl)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erro ao carregar dados: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfilePhoto(url: String?) {
        if (url!!.isNotEmpty()) {
            Glide.with(requireContext())
                .load(url)
                .transform(CircleCrop())
                .into(binding.fotoPerfilPadrao)
        } else {
            binding.fotoPerfilPadrao.setImageResource(R.drawable.foto_de_perfil_padrao)
        }
    }

    private fun uploadToCloudinary(uri: Uri) {
        MediaManager.get().upload(uri)
            .option("folder", "user_profile_photos")
            .callback(object : com.cloudinary.android.callback.UploadCallback {

                override fun onStart(requestId: String?) {
                    Toast.makeText(requireContext(), "Enviando foto...", Toast.LENGTH_SHORT).show()
                    Log.d("Cloudinary", "Upload iniciado")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: MutableMap<*, *>?) {
                    val url = resultData?.get("secure_url") as? String
                    if (url != null) {
                        Toast.makeText(requireContext(), "Upload concluído", Toast.LENGTH_SHORT).show()
                        Log.d("Cloudinary", "URL da imagem: $url")

                        profilePhotoUrl = url

                        employee?.email?.let { email ->
                            val updateRequest = UpdatePhotoRequest(email = email, fotoPerfil = url)
                            updateFotoViewModel.updatePhoto(updateRequest)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Erro ao obter URL da imagem", Toast.LENGTH_SHORT).show()
                        Log.e("Cloudinary", "URL nula no resultado do upload")
                    }
                }

                override fun onError(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
                    val errorMsg = error?.description ?: "Erro desconhecido"
                    Toast.makeText(requireContext(), "Erro no upload: $errorMsg", Toast.LENGTH_SHORT).show()
                    Log.e("Cloudinary", "Erro no upload: $errorMsg")
                }

                override fun onReschedule(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
                    val errorMsg = error?.description ?: "Erro desconhecido"
                    Toast.makeText(requireContext(), "Upload reagendado: $errorMsg", Toast.LENGTH_SHORT).show()
                    Log.w("Cloudinary", "Upload reagendado: $errorMsg")
                }

            }).dispatch()
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

    private fun verifyPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.CAMERA)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isEmpty()) {
            showPhotoOptions()
        } else {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
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

    fun updateEmployeeData(newEmployee: Employee) {
        employee = newEmployee
        profilePhotoUrl = newEmployee.fotoPerfil
        updateUIWithUserData(newEmployee)
    }

    fun formatCNPJ(cnpj: String?): String {
        if (cnpj.isNullOrBlank()) return ""

        val somenteNumeros = cnpj.filter { it.isDigit() }

        return if (somenteNumeros.length == 14) {
            "${somenteNumeros.substring(0, 2)}." +
                    "${somenteNumeros.substring(2, 5)}." +
                    "${somenteNumeros.substring(5, 8)}/" +
                    "${somenteNumeros.substring(8, 12)}-" +
                    "${somenteNumeros.substring(12, 14)}"
        } else {
            cnpj
        }
    }
}