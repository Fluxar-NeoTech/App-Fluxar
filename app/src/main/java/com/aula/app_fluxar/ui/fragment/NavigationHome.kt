package com.aula.app_fluxar.ui.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aula.app_fluxar.R
import com.aula.app_fluxar.ui.activity.MainActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar

class NavigationHome : Fragment() {
    private lateinit var homeScreenButton: Button
    private lateinit var registerButton: Button
    private lateinit var removeButton: Button
    private lateinit var listProductsButton: Button
    private lateinit var content: FrameLayout
    private lateinit var profileButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nav_home, container, false)

        homeScreenButton = view.findViewById(R.id.bt_info_gerais)
        registerButton = view.findViewById(R.id.bt_adicionar_estoque)
        removeButton = view.findViewById(R.id.bt_remover_estoque)
        listProductsButton = view.findViewById(R.id.bt_listar_produtos)
        content = view.findViewById(R.id.container_conteudo)
        profileButton = view.findViewById(R.id.fotoPerfilGestor)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProfilePhoto()

        (activity as? MainActivity)?.employeeLiveData?.observe(viewLifecycleOwner) { employee ->
            if (employee != null) {
                loadProfilePhoto()
            }
        }

        profileButton.setOnClickListener {
            findNavController().navigate(R.id.nav_perfil)
        }

        showContent(R.layout.fragment_layout_home)
        updateSelectedButtons(homeScreenButton)

        homeScreenButton.setOnClickListener {
            showContent(R.layout.fragment_layout_home)
            updateSelectedButtons(homeScreenButton)
        }

        registerButton.setOnClickListener {
            showContent(R.layout.fragment_layout_cadastrar_produto)
            updateSelectedButtons(registerButton)

            content.post {
                val addButton = content.findViewById<Button>(R.id.btCadastrar)

                addProductDropListAdd()
                addTypeDropList()
                setupDatePicker()

                addButton.setOnClickListener {
                    openAddProductPopUp()
                }
            }
        }

        removeButton.setOnClickListener {
            showContent(R.layout.fragment_layout_remover_produto)
            updateSelectedButtons(removeButton)

            content.post {
                val removeButton = content.findViewById<Button>(R.id.btRemover)

                addProductDropListRemove()
                setupFieldDependenciesRemove()

                removeButton.setOnClickListener {
                    openRemoverProductPopUp()
                }
            }
        }

        listProductsButton.setOnClickListener {
            showContent(R.layout.fragment_layout_listar_produtos)
            updateSelectedButtons(listProductsButton)

            content.post {
                // add lógica de verificar se existe produto ou não
                val exists = false
                if (exists)
                    showContent(R.layout.fragment_layout_listar_produtos)
                else
                    showContent(R.layout.fragment_sem_produtos_cadastrados)
            }
        }
    }

    private fun showContent(layoutId: Int) {
        val inflater = LayoutInflater.from(requireContext())
        content.removeAllViews()
        val novoConteudo = inflater.inflate(layoutId, content, false)
        content.addView(novoConteudo)
    }

    private fun updateSelectedButtons(selectedButton: Button) {
        val botoes = listOf(homeScreenButton, registerButton, removeButton, listProductsButton)
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

    private fun addProductDropListAdd() {
        val autoCompleteAdd = content.findViewById<AutoCompleteTextView>(R.id.productInput)
        val options = listOf("Linguiça", "Bisteca", "Maminha", "Optimus Prime", "+ Adicionar")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)

        autoCompleteAdd.setAdapter(adapter)

        autoCompleteAdd.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()

            // Se selecionar "+ Adicionar", abre uma tela para adicionar novo produto
            if (selectedItem == "+ Adicionar") {
                // Aqui você pode implementar a navegação para adicionar novo produto
                autoCompleteAdd.setText("") // Limpa o campo
            }
        }
    }

    private fun addProductDropListRemove() {
        val autoCompleteRemove = content.findViewById<AutoCompleteTextView>(R.id.productInputRemove)
        val options = listOf("Linguiça", "Bisteca", "Maminha", "Optimus Prime", "+ Adicionar")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)

        autoCompleteRemove.setAdapter(adapter)

        autoCompleteRemove.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()

            // Se selecionar "+ Adicionar", abre uma tela para adicionar novo produto
            if (selectedItem == "+ Adicionar") {
                // Aqui você pode implementar a navegação para adicionar novo produto
                autoCompleteRemove.setText("") // Limpa o campo
            }
        }
    }

    private fun addTypeDropList() {
        val autoComplete = content.findViewById<AutoCompleteTextView>(R.id.typeInput)
        val options = listOf("sim", "olá", "aqui")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_expandable_list_item_1, options)

        autoComplete.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        val dateInput = content.findViewById<TextInputEditText>(R.id.dateInput)
        val calendar = Calendar.getInstance()

        dateInput.setOnClickListener {
            showDatePickerDialog(dateInput, calendar)
        }

        // Também configure o ícone do calendário para abrir o date picker
        val dateInputLayout = content.findViewById<TextInputLayout>(R.id.dateInputLayout)
        dateInputLayout.setEndIconOnClickListener {
            showDatePickerDialog(dateInput, calendar)
        }
    }

    private fun showDatePickerDialog(dateInput: TextInputEditText, calendar: Calendar) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Formatando a data no padrão DD / MM / AAAA
                val formattedDate = String.format("%02d / %02d / %04d", selectedDay, selectedMonth + 1, selectedYear)
                dateInput.setText(formattedDate)

                // Atualiza o calendário com a data selecionada
                calendar.set(selectedYear, selectedMonth, selectedDay)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    private fun setupFieldDependenciesRemove() {
        val productInputRemove = content.findViewById<AutoCompleteTextView>(R.id.productInputRemove)
        val numLoteLayoutRemove = content.findViewById<TextInputLayout>(R.id.numLoteLayoutRemove)
        val numLoteRemove = content.findViewById<AutoCompleteTextView>(R.id.numLoteRemove)

        // Inicialmente desabilita o campo de número do lote
        numLoteLayoutRemove.isEnabled = false
        numLoteRemove.isEnabled = false

        numLoteRemove.setOnClickListener {
            if (!numLoteRemove.isEnabled) {
                showSnackbarMessage("Selecione um produto primeiro")
            }
        }

        numLoteLayoutRemove.setOnClickListener {
            if (!numLoteLayoutRemove.isEnabled) {
                showSnackbarMessage("Selecione um produto primeiro")
            }
        }

        // Adiciona um listener para monitorar mudanças no campo de produto
        productInputRemove.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val isProductSelected = s?.isNotEmpty() == true && s.toString() != "Escolha um produto"

                // Habilita ou desabilita o campo de número do lote baseado na seleção do produto
                numLoteLayoutRemove.isEnabled = isProductSelected
                numLoteRemove.isEnabled = isProductSelected

                // Limpa o campo de número do lote se o produto for deselecionado
                if (!isProductSelected) {
                    numLoteRemove.text?.clear()
                }
            }
        })
    }

    private fun showSnackbarMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun openAddProductPopUp() {
        val dialogAddProduct = layoutInflater.inflate(R.layout.pop_up_cadastrar_produto, null)
        val positiveButton = dialogAddProduct.findViewById<Button>(R.id.cadastrarProdutoS)
        val negativeButton = dialogAddProduct.findViewById<Button>(R.id.cadastrarProdutoN)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogAddProduct)
            .create()

        positiveButton.setOnClickListener {
            // Lógica para cadastrar produto
            Toast.makeText(requireContext(), "Você cadastrou um produto", Toast.LENGTH_SHORT).show()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun openRemoverProductPopUp() {
        val dialogAddProduct = layoutInflater.inflate(R.layout.pop_up_remover_produto, null)
        val positiveButton = dialogAddProduct.findViewById<Button>(R.id.removerProdutoS)
        val negativeButton = dialogAddProduct.findViewById<Button>(R.id.removerProdutoN)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogAddProduct)
            .create()

        positiveButton.setOnClickListener {
            // Lógica para remover produto
            Toast.makeText(requireContext(), "Você removeu um produto", Toast.LENGTH_SHORT).show()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun loadProfilePhoto() {
        val employee = (activity as? MainActivity)?.getEmployee()

        employee?.let {
            if (it.fotoPerfil.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(it.fotoPerfil)
                    .placeholder(R.drawable.foto_de_perfil_padrao)
                    .error(R.drawable.foto_de_perfil_padrao)
                    .transform(CircleCrop())
                    .into(profileButton)
            } else {
                profileButton.setImageResource(R.drawable.foto_de_perfil_padrao)
            }
        } ?: run {
            profileButton.setImageResource(R.drawable.foto_de_perfil_padrao)
        }
    }
}