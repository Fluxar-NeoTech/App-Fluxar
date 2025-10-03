package com.aula.app_fluxar.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.aula.app_fluxar.R
import com.aula.app_fluxar.API.model.Unit

class NavigationUnitDetails : Fragment() {

    private lateinit var unit: Unit
    private var distance: Float = 0f

    private lateinit var unitName: TextView
    private lateinit var locationText: TextView
    private lateinit var distanceText: TextView
    private lateinit var emailText: TextView
    private lateinit var btEnviarEmail: Button

    companion object {
        private const val ARG_UNIT = "unit"
        private const val ARG_DISTANCE = "distance"

        fun newInstance(unit: Unit, distance: Float): NavigationUnitDetails {
            val args = Bundle().apply {
                putParcelable(ARG_UNIT, unit)
                putFloat(ARG_DISTANCE, distance)
            }
            return NavigationUnitDetails().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            unit = it.getParcelable(ARG_UNIT)!!
            distance = it.getFloat(ARG_DISTANCE, 0f)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_navigation_unit_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        unitName = view.findViewById(R.id.unitName)
        locationText = view.findViewById(R.id.locationText)
        distanceText = view.findViewById(R.id.distanceText)
        emailText = view.findViewById(R.id.emailText)
        btEnviarEmail = view.findViewById(R.id.btEnviarEmail)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        unitName.text = unit.name
        locationText.text = unit.enderecoCompleto()
        distanceText.text = "%.2f km da sua unidade".format(distance)

        val email = "contato@${unit.name.toLowerCase().replace(" ", "")}.com.br"
        emailText.text = email
    }

    private fun setupClickListeners() {
        btEnviarEmail.setOnClickListener {
            sendEmail()
        }
    }

    private fun sendEmail() {
        val email = emailText.text.toString()
        val subject = "Contato - Unidade ${unit.name}"
        val body = """
            Prezados,
            
            Gostaria de entrar em contato sobre a unidade ${unit.name}.
            
            Endereço: ${unit.enderecoCompleto()}
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
    }
}