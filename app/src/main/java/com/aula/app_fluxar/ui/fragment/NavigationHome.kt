package com.aula.app_fluxar.ui.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.API.viewModel.GetBatchesViewModel
import com.aula.app_fluxar.R
import com.aula.app_fluxar.adpters.BatchAdapter
import com.aula.app_fluxar.sessionManager.SessionManager
import com.aula.app_fluxar.ui.activity.MainActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import androidx.fragment.app.viewModels
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
    private lateinit var greetingManager: TextView
    private val getBatchesViewModel: GetBatchesViewModel by viewModels()

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
        greetingManager = view.findViewById(R.id.cumprimentoGestor)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProfileInfos()

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
                // Carrega os lotes quando o layout for inflado
                loadBatchesForListProducts()
            }
        }

        // Observar os resultados dos lotes
        setupBatchesObserver()
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

    private fun loadBatchesForListProducts() {
        val employee = SessionManager.getCurrentProfile()

        if (employee != null) {
            val unitID = employee.unit.id
            val sectorID = employee.sector.id

            Log.d("NavigationHome", "Carregando batches para unitID: $unitID, sectorID: $sectorID")
            getBatchesViewModel.getBatches(unitID, sectorID)
        } else {
            Log.e("NavigationHome", "Employee não encontrado na sessão")
            showEmptyState()
        }
    }

    private fun setupBatchesObserver() {
        getBatchesViewModel.getBatchesResult.observe(viewLifecycleOwner) { batches ->
            Log.d("NavigationHome", "Batches recebidos: ${batches?.size ?: 0}")

            if (batches != null && batches.isNotEmpty()) {
                setupRecyclerViewWithBatches(batches)
            } else {
                showEmptyState()
            }
        }

        getBatchesViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Log.e("NavigationHome", "Erro ao carregar lotes: $error")
                showEmptyState()
            }
        }
    }

    private fun setupRecyclerViewWithBatches(batches: List<com.aula.app_fluxar.API.model.Batch>) {
        try {
            val recyclerView = content.findViewById<RecyclerView>(R.id.product_list_RV)
            if (recyclerView != null) {
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                val adapter = BatchAdapter(batches)
                recyclerView.adapter = adapter
                Log.d("NavigationHome", "RecyclerView configurado com ${batches.size} lotes")

                adapter.notifyDataSetChanged()
            } else {
                Log.e("NavigationHome", "RecyclerView não encontrado no layout")
            }
        } catch (e: Exception) {
            Log.e("NavigationHome", "Erro ao configurar RecyclerView: ${e.message}")
        }
    }

    private fun showEmptyState() {
        // Se não há lotes, mostra o layout de lista vazia
        showContent(R.layout.fragment_sem_produtos_cadastrados)
        Log.d("NavigationHome", "Mostrando estado vazio - sem produtos")
    }

    // Os demais métodos permanecem iguais...
    private fun addProductDropListAdd() {
        val autoCompleteAdd = content.findViewById<AutoCompleteTextView>(R.id.productInput)
        val options = listOf("Linguiça", "Bisteca", "Maminha", "Optimus Prime", "+ Adicionar")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)

        autoCompleteAdd.setAdapter(adapter)

        autoCompleteAdd.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()

            if (selectedItem == "+ Adicionar") {
                autoCompleteAdd.setText("")
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

            if (selectedItem == "+ Adicionar") {
                autoCompleteRemove.setText("")
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
                val formattedDate = String.format("%02d / %02d / %04d", selectedDay, selectedMonth + 1, selectedYear)
                dateInput.setText(formattedDate)
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

        productInputRemove.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val isProductSelected = s?.isNotEmpty() == true && s.toString() != "Escolha um produto"
                numLoteLayoutRemove.isEnabled = isProductSelected
                numLoteRemove.isEnabled = isProductSelected
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
            Toast.makeText(requireContext(), "Você removeu um produto", Toast.LENGTH_SHORT).show()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun loadProfileInfos() {
        val employee = SessionManager.getCurrentProfile()

        employee?.let {
            if (it.firstName.isNotEmpty()) {
                greetingManager.text = "Olá, ${it.firstName}!"
            } else {
                greetingManager.text = "Olá, usuário!"
            }

            if (it.profilePhoto.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(it.profilePhoto)
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