package com.aula.app_fluxar.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.aula.app_fluxar.API.model.UnitInfos
import com.aula.app_fluxar.databinding.FragmentNavigationUnitDetailsBinding

class UnitDetails : Fragment() {

    private var _binding: FragmentNavigationUnitDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var unit: UnitInfos
    private var distance: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        unit = arguments?.getParcelable("unit")
            ?: throw IllegalArgumentException("Unit não encontrada nos argumentos")
        distance = arguments?.getFloat("distance", 0f) ?: 0f
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNavigationUnitDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoadingState()

        view.postDelayed({
            try {
                updateUI()
                showContentState()
            } catch (e: Exception) {
                showErrorState("Erro ao carregar dados: ${e.message}")
            }
        }, 1200)

        binding.btEnviarEmail.setOnClickListener { sendEmail() }
        binding.unitRetryButton.setOnClickListener {
            showLoadingState()
            view.postDelayed({
                try {
                    updateUI()
                    showContentState()
                } catch (e: Exception) {
                    showErrorState("Erro ao carregar dados: ${e.message}")
                }
            }, 1200)
        }
    }

    private fun updateUI() {
        binding.unitName.text = unit.name ?: "Indisponível"
        binding.locationText.text = buildEnderecoCompleto()
        binding.distanceText.text = "%.2f km da sua unidade".format(distance)
        binding.emailText.text = unit.email ?: "Indisponível"
        binding.truckText.text = """
            Como enviar:
            • Pesagem e documentação de tudo o que será enviado;
            • Avisar previamente por e-mail;
            • Entrega após a confirmação desta unidade;
            • Enviar por caminhão refrigerado, se necessário.
        """.trimIndent()

    }

    private fun buildEnderecoCompleto(): String {
        val parts = mutableListOf<String>()
        unit.street?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }
        unit.number?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }
        unit.neighborhood?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }
        unit.city?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }
        unit.state?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }
        unit.postalCode?.takeIf { it.isNotEmpty() }?.let { parts.add("CEP: $it") }
        return parts.joinToString(", ").ifEmpty { "Endereço indisponível" }
    }

    private fun sendEmail() {
        val email = unit.email
        if (email.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Email não disponível", Toast.LENGTH_SHORT).show()
            return
        }

        val subject = "Contato - Unidade ${unit.name}"
        val body = """
            Olá!

            Gostaria de entrar em contato sobre a unidade ${unit.name}.

            Endereço: ${buildEnderecoCompleto()}
            Distância: ${"%.2f".format(distance)} km

            Atenciosamente,
            [Seu Nome]
        """.trimIndent()

        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            startActivity(Intent.createChooser(intent, "Enviar email..."))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Nenhum app de email encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoadingState() {
        binding.unitLoadingLayout.visibility = View.VISIBLE
        binding.unitErrorLayout.visibility = View.GONE
        setContentElementsVisibility(false)
    }

    private fun showContentState() {
        binding.unitLoadingLayout.visibility = View.GONE
        binding.unitErrorLayout.visibility = View.GONE
        setContentElementsVisibility(true)
    }

    private fun showErrorState(message: String) {
        binding.unitLoadingLayout.visibility = View.GONE
        binding.unitErrorLayout.visibility = View.VISIBLE
        setContentElementsVisibility(false)
        binding.unitErrorText.text = message
    }

    private fun setContentElementsVisibility(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        binding.unitName.visibility = visibility
        binding.quadradoUnidade.visibility = visibility
        binding.locationTitle.visibility = visibility
        binding.locationText.visibility = visibility
        binding.locationIcon.visibility = visibility
        binding.distanceTitle.visibility = visibility
        binding.distanceText.visibility = visibility
        binding.distanceIcon.visibility = visibility
        binding.truckTitle.visibility = visibility
        binding.truckText.visibility = visibility
        binding.truckIcon.visibility = visibility
        binding.divider.visibility = visibility
        binding.emailTitle.visibility = visibility
        binding.emailText.visibility = visibility
        binding.btEnviarEmail.visibility = visibility
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
