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
    private lateinit var emailText: TextView
    private lateinit var btEnviarEmail: Button
    private var locationIcon: View? = null
    private var locationTitle: View? = null
    private var quadradoUnidade: View? = null
    private var distanceIcon: View? = null
    private var distanceTitle: View? = null
    private var measuresTitle: View? = null
    private var truckIcon: View? = null
    private var truckTitle: View? = null
    private var truckText: TextView? = null
    private var divider: View? = null
    private var emailTitle: View? = null

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

        showLoadingState()
        simulateDataLoading()

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
        emailText = view.findViewById(R.id.emailText)
        btEnviarEmail = view.findViewById(R.id.btEnviarEmail)

        locationIcon = view.findViewById(R.id.locationIcon)
        locationTitle = view.findViewById(R.id.locationTitle)
        quadradoUnidade = view.findViewById(R.id.quadradoUnidade)
        distanceIcon = view.findViewById(R.id.distanceIcon)
        distanceTitle = view.findViewById(R.id.distanceTitle)
        truckIcon = view.findViewById(R.id.truckIcon)
        truckTitle = view.findViewById(R.id.truckTitle)
        truckText = view.findViewById(R.id.truckText)
        divider = view.findViewById(R.id.divider)
        emailTitle = view.findViewById(R.id.emailTitle)
    }

    private fun simulateDataLoading() {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                setupBasicUI()
                showContentState()
            } catch (e: Exception) {
                showErrorState("Erro ao carregar dados: ${e.message}")
            }
        }, 1500)
    }

    private fun showLoadingState() {
        loadingProgress.visibility = View.VISIBLE
        loadingText.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        hideContentViews()

        Log.d("UnitDetails", "📱 Mostrando estado de loading")
    }

    private fun showContentState() {
        loadingProgress.visibility = View.GONE
        loadingText.visibility = View.GONE
        errorLayout.visibility = View.GONE
        showContentViews()

        Log.d("UnitDetails", "✅ Mostrando conteúdo")
    }

    private fun showErrorState(errorMessage: String) {
        loadingProgress.visibility = View.GONE
        loadingText.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        errorText.text = errorMessage
        hideContentViews()

        Log.e("UnitDetails", "❌ Erro: $errorMessage")
    }

    private fun hideContentViews() {
        val contentViews = arrayOf(
            unitName, locationText, distanceText,
            emailText, btEnviarEmail,
            locationIcon, locationTitle, quadradoUnidade,
            distanceIcon, distanceTitle, measuresTitle,
            truckIcon, truckTitle, truckText, divider, emailTitle
        )

        contentViews.forEach { it?.visibility = View.GONE }
    }

    private fun showContentViews() {
        val contentViews = arrayOf(
            unitName, locationText, distanceText,
            emailText, btEnviarEmail,
            locationIcon, locationTitle, quadradoUnidade,
            distanceIcon, distanceTitle, measuresTitle,
            truckIcon, truckTitle, truckText, divider, emailTitle
        )

        contentViews.forEach { it?.visibility = View.VISIBLE }
    }

    private fun setupBasicUI() {
        try {
            unitName.text = unit.name ?: "Indisponível"

            val enderecoCompleto = buildEnderecoCompleto()
            locationText.text = enderecoCompleto

            distanceText.text = "%.2f km da sua unidade".format(distance)
            emailText.text = unit.email ?: "Indisponível"

            setupAdditionalInfo()

            Log.d("UnitDetails", "✅ UI configurada - Nome: ${unit.name}, Email: ${unit.email}")
        } catch (e: Exception) {
            Log.e("UnitDetails", "❌ Erro ao configurar UI: ${e.message}")
            throw e
        }
    }

    private fun buildEnderecoCompleto(): String {
        return try {
            val parts = mutableListOf<String>()

            unit.street?.let { if (it.isNotEmpty()) parts.add(it) }
            unit.number?.let { if (it.isNotEmpty()) parts.add(it) }
            unit.neighborhood?.let { if (it.isNotEmpty()) parts.add(it) }
            unit.city?.let { if (it.isNotEmpty()) parts.add(it) }
            unit.state?.let { if (it.isNotEmpty()) parts.add(it) }
            unit.postalCode?.let { if (it.isNotEmpty()) parts.add("CEP: $it") }

            if (parts.isEmpty()) "Endereço indisponível" else parts.joinToString(", ")
        } catch (e: Exception) {
            Log.e("UnitDetails", "Erro ao construir endereço: ${e.message}")
            "Endereço indisponível"
        }
    }

    private fun setupAdditionalInfo() {
        truckText?.text = "Como enviar Pesagem e documentação de tudo o que será enviado;\n\nAvisar previamente por e-mail;\n\nEntrega após a confirmação desta unidade para receber;\n\nPor caminhão, refrigerado se necessário."
    }

    private fun setupClickListeners() {
        btEnviarEmail.setOnClickListener {
            sendEmail()
        }

        retryButton.setOnClickListener {
            showLoadingState()
            simulateDataLoading()
        }
    }

    private fun sendEmail() {
        try {
            val email = unit.email ?: run {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Email não disponível",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return
            }

            val subject = "Contato - Unidade ${unit.name}"
            val body = """
                Olá! Tudo bem?
                
                Gostaria de entrar em contato sobre a unidade ${unit.name}.
                
                Endereço: ${buildEnderecoCompleto()}
                
                Distância da minha unidade: ${"%.2f".format(distance)} km
                
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
        } catch (e: Exception) {
            Log.e("UnitDetails", "Erro ao enviar email: ${e.message}")
            android.widget.Toast.makeText(
                requireContext(),
                "Erro ao preparar email",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}