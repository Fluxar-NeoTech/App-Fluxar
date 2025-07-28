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
    private lateinit var btTelaInicial: Button
    private lateinit var btCadastrar: Button
    private lateinit var btRemover: Button
    private lateinit var conteudo: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nav_home, container, false)

        btTelaInicial = view.findViewById(R.id.bt_info_gerais)
        btCadastrar = view.findViewById(R.id.bt_adicionar_estoque)
        btRemover = view.findViewById(R.id.bt_remover_estoque)
        conteudo = view.findViewById(R.id.container_conteudo)

        mostrarConteudo(R.layout.fragment_layout_home)
        atualizarBotoesSelecionados(btTelaInicial)

        btTelaInicial.setOnClickListener {
            mostrarConteudo(R.layout.fragment_layout_home)
            atualizarBotoesSelecionados(btTelaInicial)
        }

        btCadastrar.setOnClickListener {
            mostrarConteudo(R.layout.fragment_layout_cadastrar_produto)
            atualizarBotoesSelecionados(btCadastrar)
        }

        btRemover.setOnClickListener {
            mostrarConteudo(R.layout.fragment_layout_remover_produto)
            atualizarBotoesSelecionados(btRemover)
        }

        return view

    }

    private fun mostrarConteudo(layoutId: Int) {
        val inflater = LayoutInflater.from(requireContext())
        conteudo.removeAllViews()
        val novoConteudo = inflater.inflate(layoutId, conteudo, false)
        conteudo.addView(novoConteudo)
    }

    private fun atualizarBotoesSelecionados(botaoSelecionado: Button) {
        val botoes = listOf(btTelaInicial, btCadastrar, btRemover)
        botoes.forEach { botao ->
            if (botao == botaoSelecionado) {
                botao.setBackgroundResource(R.drawable.bt_ativo)
                botao.setTextColor(Color.WHITE)
            } else {
                botao.setBackgroundResource(R.drawable.bt_inativo)
                botao.setTextColor(ContextCompat.getColor(requireContext(), R.color.roxo_principal))
            }
        }
    }
}