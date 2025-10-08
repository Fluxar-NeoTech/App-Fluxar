package com.aula.app_fluxar.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.aula.app_fluxar.R
import com.aula.app_fluxar.API.model.UnitInfos
import com.aula.app_fluxar.API.model.Dimensions
import com.aula.app_fluxar.API.viewModel.GetDimensionsViewModel
import androidx.fragment.app.viewModels

class NavigationUnitDetails : Fragment() {
    private lateinit var unit: UnitInfos
    private var distance: Float = 0f

    private lateinit var loadingProgress: ProgressBar
    private lateinit var loadingText: TextView
    private lateinit var errorLayout: View
    private lateinit var errorText: TextView
    private lateinit var retryButton: Button

    private lateinit var unitName: TextView
    private lateinit var locationText: TextView
    private lateinit var distanceText: TextView
    private lateinit var measuresText: TextView
    private lateinit var emailText: TextView
    private lateinit var btEnviarEmail: Button

    private lateinit var locationIcon: View
    private lateinit var locationTitle: View
    private lateinit var quadradoUnidade: View
    private lateinit var distanceIcon: View
    private lateinit var distanceTitle: View
    private lateinit var measuresIcon: View
    private lateinit var measuresTitle: View
    private lateinit var truckIcon: View
    private lateinit var truckTitle: View
    private lateinit var truckText: View
    private lateinit var divider: View
    private lateinit var emailTitle: View

    private val getDimensionsViewModel: GetDimensionsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        unit = arguments?.getParcelable("unit") ?: throw IllegalArgumentException("Unit não encontrada nos argumentos")
        distance = arguments?.getFloat("distance", 0f) ?: 0f

        Log.d("UnitDetails", "Unidade recebida: ${unit.name}")
        Log.d("UnitDetails", "Distância: $distance km")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_navigation_unit_details, container, false)
        setupViews(view)
        setupClickListeners()
        setupDimensionsObserver()
        showLoadingState()
        getDimensionsViewModel.getDimensionsByUnitId(unit.id)

        return view
    }

    private fun setupViews(view: View) {
        loadingProgress = view.findViewById(R.id.loadingProgress)
        loadingText = view.findViewById(R.id.loadingText)
        errorLayout = view.findViewById(R.id.errorLayout)
        errorText = view.findViewById(R.id.errorText)
        retryButton = view.findViewById(R.id.retryButton)

        unitName = view.findViewById(R.id.unitName)
        locationText = view.findViewById(R.id.locationText)
        distanceText = view.findViewById(R.id.distanceText)
        measuresText = view.findViewById(R.id.measuresText)
        emailText = view.findViewById(R.id.emailText)
        btEnviarEmail = view.findViewById(R.id.btEnviarEmail)

        locationIcon = view.findViewById(R.id.locationIcon)
        locationTitle = view.findViewById(R.id.locationTitle)
        quadradoUnidade = view.findViewById(R.id.quadradoUnidade)
        distanceIcon = view.findViewById(R.id.distanceIcon)
        distanceTitle = view.findViewById(R.id.distanceTitle)
        measuresIcon = view.findViewById(R.id.measuresIcon)
        measuresTitle = view.findViewById(R.id.measuresTitle)
        truckIcon = view.findViewById(R.id.truckIcon)
        truckTitle = view.findViewById(R.id.truckTitle)
        truckText = view.findViewById(R.id.truckText)
        divider = view.findViewById(R.id.divider)
        emailTitle = view.findViewById(R.id.emailTitle)
    }

    private fun setupDimensionsObserver() {
        getDimensionsViewModel.dimensionsResult.observe(viewLifecycleOwner) { dimensions ->
            dimensions?.let { dim ->
                updateDimensionsUI(dim)
                showContentState()
                Log.d("UnitDetails", "Dimensões carregadas: $dim")
            }
        }

        getDimensionsViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                showErrorState("Erro ao carregar dimensões: $error")
                Log.e("UnitDetails", "Erro nas dimensões: $error")
            }
        }

        getDimensionsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                showLoadingState()
            }
        }
    }

    private fun showLoadingState() {
        loadingProgress.visibility = View.VISIBLE
        loadingText.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        hideContentViews()
    }

    private fun showContentState() {
        loadingProgress.visibility = View.GONE
        loadingText.visibility = View.GONE
        errorLayout.visibility = View.GONE
        showContentViews()
        setupBasicUI()
    }

    private fun showErrorState(errorMessage: String) {
        loadingProgress.visibility = View.GONE
        loadingText.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        errorText.text = errorMessage
        hideContentViews()
    }

    private fun hideContentViews() {
        val contentViews = arrayOf(
            unitName, locationText, distanceText, measuresText,
            emailText, btEnviarEmail,
            locationIcon, locationTitle, quadradoUnidade,
            distanceIcon, distanceTitle, measuresIcon, measuresTitle,
            truckIcon, truckTitle, truckText, divider, emailTitle
        )

        contentViews.forEach { it.visibility = View.GONE }
    }

    private fun showContentViews() {
        val contentViews = arrayOf(
            unitName, locationText, distanceText, measuresText,
            emailText, btEnviarEmail,
            locationIcon, locationTitle, quadradoUnidade,
            distanceIcon, distanceTitle, measuresIcon, measuresTitle,
            truckIcon, truckTitle, truckText, divider, emailTitle
        )

        contentViews.forEach { it.visibility = View.VISIBLE }
    }

    private fun setupBasicUI() {
        unitName.text = unit.name ?: "Indisponível"
        locationText.text = unit.enderecoCompleto() ?: "Indisponível"
        distanceText.text = "%.2f km da sua unidade".format(distance) ?: "Indisponível"
        emailText.text = unit.email ?: "Indisponível"
    }

    private fun updateDimensionsUI(dimensions: Dimensions) {
        measuresText.text =
            "Esta unidade pode receber: ${dimensions.widthDimension}m de comprimento, ${dimensions.heightDimension}m de largura e ${dimensions.lengthDimension}m de altura."
    }

    private fun setupClickListeners() {
        btEnviarEmail.setOnClickListener {
            sendEmail()
        }

        retryButton.setOnClickListener {
            showLoadingState()
            getDimensionsViewModel.getDimensionsByUnitId(unit.id)
        }
    }

    private fun sendEmail() {
        val email = emailText.text.toString()
        val subject = "Contato - Unidade ${unit.name}"
        val body = """
            Olá! Tudo bem?
            
            Gostaria de entrar em contato sobre a unidade ${unit.name}.
            
            Endereço: ${unit.enderecoCompleto()}
            Distância da minha unidade: ${"%.2f".format(distance)} km
            
            Dimensões disponíveis: ${measuresText.text}
            
            Atenciosamente,
            [Seu Nome]
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            startActivity(Intent.createChooser(intent, "Enviar email..."))
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                requireContext(),
                "Nenhum app de email encontrado",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}