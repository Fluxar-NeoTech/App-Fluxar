package com.aula.app_fluxar.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.aula.app_fluxar.R
import com.google.android.material.button.MaterialButton

class NavigationHome : Fragment() {
    private lateinit var homeScreenButton: Button
    private lateinit var registerButton: Button
    private lateinit var removeButton: Button
    private lateinit var content: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nav_home, container, false)

        homeScreenButton = view.findViewById(R.id.bt_info_gerais)
        registerButton = view.findViewById(R.id.bt_adicionar_estoque)
        removeButton = view.findViewById(R.id.bt_remover_estoque)
        content = view.findViewById(R.id.container_conteudo)

        showContent(R.layout.fragment_layout_home)
        updateSelectedButtons(homeScreenButton)

        homeScreenButton.setOnClickListener {
            showContent(R.layout.fragment_layout_home)
            updateSelectedButtons(homeScreenButton)
        }

        registerButton.setOnClickListener {
            showContent(R.layout.fragment_layout_cadastrar_produto)
            updateSelectedButtons(registerButton)
        }

        removeButton.setOnClickListener {
            showContent(R.layout.fragment_layout_remover_produto)
            updateSelectedButtons(removeButton)
        }

        return view

    }

    private fun showContent(layoutId: Int) {
        val inflater = LayoutInflater.from(requireContext())
        content.removeAllViews()
        val novoConteudo = inflater.inflate(layoutId, content, false)
        content.addView(novoConteudo)
    }

    private fun updateSelectedButtons(selectedButton: Button) {
        val botoes = listOf(homeScreenButton, registerButton, removeButton)
        botoes.forEach { botao ->
            if (botao == selectedButton) {
                botao.setBackgroundResource(R.drawable.bt_ativo)
                botao.setTextColor(Color.WHITE)
            } else {
                botao.setBackgroundResource(R.drawable.bt_inativo)
                botao.setTextColor(ContextCompat.getColor(requireContext(), R.color.roxo_principal))
            }
        }
    }
}