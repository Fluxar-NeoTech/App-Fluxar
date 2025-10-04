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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.API.viewModel.GetBatchesViewModel
import com.aula.app_fluxar.API.viewModel.GetProductsViewModel
import com.aula.app_fluxar.API.viewModel.GetBatchesNamesViewModel
import com.aula.app_fluxar.API.viewModel.AddProductViewModel
import com.aula.app_fluxar.API.viewModel.AddBatchViewModel
import com.aula.app_fluxar.API.viewModel.DeleteBatchViewModel
import com.aula.app_fluxar.API.model.BatchRequest
import com.aula.app_fluxar.R
import com.aula.app_fluxar.adpters.BatchAdapter
import com.aula.app_fluxar.sessionManager.SessionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.aula.app_fluxar.API.model.ProductResponse
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
    private val getProductsViewModel: GetProductsViewModel by viewModels()
    private val getBatchesNamesViewModel: GetBatchesNamesViewModel by viewModels()
    private val addProductViewModel: AddProductViewModel by viewModels()
    private val addBatchViewModel: AddBatchViewModel by viewModels()
    private val deleteBatchViewModel: DeleteBatchViewModel by viewModels()
    private var currentProducts: List<ProductResponse> = emptyList()
    private var selectedProductIdForBatch: Long? = null
    private var selectedProductIdForRemoval: Long? = null
    private var currentBatchNumbers: List<String> = emptyList()

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

        loadProducts()

        homeScreenButton.setOnClickListener {
            showContent(R.layout.fragment_layout_home)
            updateSelectedButtons(homeScreenButton)
        }

        registerButton.setOnClickListener {
            showContent(R.layout.fragment_layout_cadastrar_produto)
            updateSelectedButtons(registerButton)

            content.post {
                val addBatchButton = content.findViewById<Button>(R.id.btCadastrar)

                updateProductDropdownForCurrentLayout()
                setupTypeDropdown()
                setupDatePicker()

                addBatchButton.setOnClickListener {
                    if (validateBatchFields()) {
                        openAddBatchPopUp()
                    }
                }
            }
        }

        removeButton.setOnClickListener {
            showContent(R.layout.fragment_layout_remover_produto)
            updateSelectedButtons(removeButton)

            content.post {
                val removeBatchButton = content.findViewById<Button>(R.id.btRemover)

                updateProductDropdownForCurrentLayout()
                setupBatchNumberDependencies()

                removeBatchButton.setOnClickListener {
                    openRemoveBatchPopUp()
                }
            }
        }

        listProductsButton.setOnClickListener {
            showContent(R.layout.fragment_layout_listar_produtos)
            updateSelectedButtons(listProductsButton)

            content.post {
                loadBatchesForListing()
            }
        }

        setupBatchesObserver()
        setupProductsObserver()
        setupBatchesNamesObserver()
        setupAddProductObserver()
        setupAddBatchObserver()
        setupDeleteBatchObserver()
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

    private fun loadBatchesForListing() {
        val employee = SessionManager.getCurrentProfile()

        if (employee != null) {
            val unitID = employee.unit.id
            val sectorID = employee.sector.id

            Log.d("NavigationHome", "Carregando lotes para listagem - unitID: $unitID, sectorID: $sectorID")
            getBatchesViewModel.getBatches(unitID, sectorID)
        } else {
            Log.e("NavigationHome", "Employee não encontrado na sessão")
            showEmptyState()
        }
    }

    private fun setupBatchesObserver() {
        getBatchesViewModel.getBatchesResult.observe(viewLifecycleOwner) { batches ->
            Log.d("NavigationHome", "Lotes recebidos para listagem: ${batches?.size ?: 0}")

            if (batches != null && batches.isNotEmpty()) {
                setupBatchesRecyclerView(batches)
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

    private fun setupBatchesRecyclerView(batches: List<com.aula.app_fluxar.API.model.Batch>) {
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
        showContent(R.layout.fragment_sem_produtos_cadastrados)
        Log.d("NavigationHome", "Mostrando estado vazio - sem lotes")
    }

    private fun loadProducts() {
        val employeeId = SessionManager.getEmployeeId()
        getProductsViewModel.getProductsByEmployee(employeeId)
    }

    private fun setupProductsObserver() {
        getProductsViewModel.getProductsResult.observe(viewLifecycleOwner) { products ->
            Log.d("NavigationHome", "Produtos recebidos: ${products?.size ?: 0}")

            if (products != null && products.isNotEmpty()) {
                currentProducts = products
                updateProductDropdownForCurrentLayout()
            } else {
                val fallbackOptions = listOf("+ Adicionar", "Linguiça", "Bisteca", "Maminha")
                updateDropdownsWithFallback(fallbackOptions)
                currentProducts = emptyList()
            }
        }

        getProductsViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Log.e("NavigationHome", "Erro ao carregar produtos: $error")
                val fallbackOptions = listOf("+ Adicionar", "Linguiça", "Bisteca", "Maminha")
                updateDropdownsWithFallback(fallbackOptions)
                currentProducts = emptyList()
            }
        }
    }

    private fun updateProductDropdownForCurrentLayout() {
        if (currentProducts.isNotEmpty()) {
            try {
                val productInput = content.findViewById<AutoCompleteTextView>(R.id.productInput)
                if (productInput != null) {
                    val optionsForBatch = listOf("+ Adicionar") + currentProducts.map { it.name }
                    updateProductDropdownForBatch(optionsForBatch, currentProducts)
                    Log.d("NavigationHome", "Dropdown de cadastro atualizado com produtos em cache")
                    return
                }

                val productInputRemove = content.findViewById<AutoCompleteTextView>(R.id.productInputRemove)
                if (productInputRemove != null) {
                    val optionsForRemoval = currentProducts.map { it.name }
                    updateProductDropdownForRemoval(optionsForRemoval, currentProducts)
                    Log.d("NavigationHome", "Dropdown de remoção atualizado com produtos em cache")
                    return
                }
            } catch (e: Exception) {
                Log.e("NavigationHome", "Erro ao atualizar dropdown do layout atual: ${e.message}")
            }
        } else {
            Log.d("NavigationHome", "Produtos ainda não carregados, fazendo nova requisição...")
            loadProducts()
        }
    }

    private fun updateAllProductDropdowns(products: List<ProductResponse>) {
        val productNames = products.map { it.name }
        val optionsForBatch = listOf("+ Adicionar") + productNames
        val optionsForRemoveBatch = productNames

        Log.d("NavigationHome", "Atualizando dropdowns com ${products.size} produtos")

        try {
            val productInput = content.findViewById<AutoCompleteTextView>(R.id.productInput)
            if (productInput != null) {
                updateProductDropdownForBatch(optionsForBatch, products)
                Log.d("NavigationHome", "Dropdown de cadastro atualizado com sucesso")
            }
        } catch (e: Exception) {
            Log.d("NavigationHome", "Dropdown de cadastro não está visível: ${e.message}")
        }

        try {
            val productInputRemove = content.findViewById<AutoCompleteTextView>(R.id.productInputRemove)
            if (productInputRemove != null) {
                updateProductDropdownForRemoval(optionsForRemoveBatch, products)
                Log.d("NavigationHome", "Dropdown de remoção atualizado com sucesso")
            }
        } catch (e: Exception) {
            Log.d("NavigationHome", "Dropdown de remoção não está visível: ${e.message}")
        }
    }

    private fun updateDropdownsWithFallback(fallbackOptions: List<String>) {
        Log.d("NavigationHome", "Usando fallback para produtos")

        val fallbackForBatch = listOf("+ Adicionar") + fallbackOptions.drop(1)
        val fallbackForRemoval = fallbackOptions.filter { it != "+ Adicionar" }

        try {
            val productInput = content.findViewById<AutoCompleteTextView>(R.id.productInput)
            if (productInput != null) {
                updateProductDropdownForBatch(fallbackForBatch, emptyList())
            }
        } catch (e: Exception) {
            Log.d("NavigationHome", "Dropdown de cadastro não está visível no fallback")
        }

        try {
            val productInputRemove = content.findViewById<AutoCompleteTextView>(R.id.productInputRemove)
            if (productInputRemove != null) {
                updateProductDropdownForRemoval(fallbackForRemoval, emptyList())
            }
        } catch (e: Exception) {
            Log.d("NavigationHome", "Dropdown de remoção não está visível no fallback")
        }
    }

    private fun updateProductDropdownForBatch(options: List<String>, products: List<ProductResponse>) {
        try {
            val productInput = content.findViewById<AutoCompleteTextView>(R.id.productInput)
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)
            productInput.setAdapter(adapter)

            productInput.setOnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem == "+ Adicionar") {
                    productInput.setText("")
                    selectedProductIdForBatch = null
                    clearTypeField()
                    openAddProductDialog()
                } else {
                    selectedProductIdForBatch = getProductIdByName(selectedItem, products)
                    Log.d("NavigationHome", "Produto selecionado para LOTE: $selectedItem - ID: $selectedProductIdForBatch")
                    fillProductType(selectedItem, products)
                }
            }
        } catch (e: Exception) {
            Log.e("NavigationHome", "Erro ao atualizar dropdown de produto para lote: ${e.message}")
        }
    }

    private fun fillProductType(selectedProductName: String, products: List<ProductResponse>) {
        try {
            val typeInput = content.findViewById<AutoCompleteTextView>(R.id.typeInput)
            val product = products.find { it.name == selectedProductName }

            product?.let {
                typeInput.setText(it.type ?: "Tipo não definido")
                Log.d("NavigationHome", "Tipo do produto preenchido automaticamente: ${it.type}")
            } ?: run {
                Log.e("NavigationHome", "Produto não encontrado para preencher tipo: $selectedProductName")
                typeInput.setText("")
            }
        } catch (e: Exception) {
            Log.e("NavigationHome", "Erro ao preencher tipo do produto: ${e.message}")
        }
    }

    private fun clearTypeField() {
        try {
            val typeInput = content.findViewById<AutoCompleteTextView>(R.id.typeInput)
            typeInput.setText("")
        } catch (e: Exception) {
            Log.e("NavigationHome", "Erro ao limpar campo de tipo: ${e.message}")
        }
    }

    private fun updateProductDropdownForRemoval(options: List<String>, products: List<ProductResponse>) {
        try {
            val productInputRemove = content.findViewById<AutoCompleteTextView>(R.id.productInputRemove)
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)
            productInputRemove.setAdapter(adapter)

            productInputRemove.setOnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                selectedProductIdForRemoval = getProductIdByName(selectedItem, products)
                Log.d("NavigationHome", "Produto selecionado para REMOÇÃO: $selectedItem - ID: $selectedProductIdForRemoval")

                selectedProductIdForRemoval?.let { productId ->
                    loadBatchNumbersForProduct(productId)
                }
            }
        } catch (e: Exception) {
            Log.e("NavigationHome", "Erro ao atualizar dropdown de produto para remoção: ${e.message}")
        }
    }

    private fun setupBatchesNamesObserver() {
        getBatchesNamesViewModel.getBatchesNamesResult.observe(viewLifecycleOwner) { batchesNames ->
            Log.d("NavigationHome", "Números de lote recebidos: ${batchesNames?.size ?: 0}")

            if (batchesNames != null && batchesNames.isNotEmpty()) {
                updateBatchNumberDropdown(batchesNames)
            } else {
                Log.d("NavigationHome", "Nenhum lote encontrado para o produto selecionado")
                updateBatchNumberDropdown(emptyList())
            }
        }

        getBatchesNamesViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Log.e("NavigationHome", "Erro ao carregar números de lote: $error")
                updateBatchNumberDropdown(emptyList())
            }
        }
    }

    private fun updateBatchNumberDropdown(batchesNames: List<String>) {
        try {
            val numLoteRemove = content.findViewById<AutoCompleteTextView>(R.id.numLoteRemove)
            val numLoteLayoutRemove = content.findViewById<TextInputLayout>(R.id.numLoteLayoutRemove)
            currentBatchNumbers = batchesNames

            if (batchesNames.isNotEmpty()) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, batchesNames)
                numLoteRemove.setAdapter(adapter)
                numLoteRemove.isEnabled = true
                numLoteLayoutRemove.isEnabled = true
                numLoteRemove.hint = "Número do lote"
                Log.d("NavigationHome", "Dropdown de números de lote atualizado com ${batchesNames.size} opções")
            } else {
                val emptyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf("Nenhum lote encontrado"))
                numLoteRemove.setAdapter(emptyAdapter)
                numLoteRemove.isEnabled = false
                numLoteLayoutRemove.isEnabled = false
            }
        } catch (e: Exception) {
            Log.e("NavigationHome", "Erro ao atualizar dropdown de números de lote: ${e.message}")
        }
    }

    private fun resetBatchNumberDropdown() {
        try {
            val numLoteRemove = content.findViewById<AutoCompleteTextView>(R.id.numLoteRemove)
            val numLoteLayoutRemove = content.findViewById<TextInputLayout>(R.id.numLoteLayoutRemove)

            numLoteRemove.text?.clear()
            numLoteRemove.isEnabled = false
            numLoteLayoutRemove.isEnabled = false
            currentBatchNumbers = emptyList()

            val emptyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf("Selecione um produto primeiro"))
            numLoteRemove.setAdapter(emptyAdapter)
            numLoteRemove.hint = "Selecione um produto primeiro"
        } catch (e: Exception) {
            Log.e("NavigationHome", "Erro ao resetar dropdown de números de lote: ${e.message}")
        }
    }

    private fun loadBatchNumbersForProduct(productId: Long) {
        Log.d("NavigationHome", "Buscando números de lote para o produto ID: $productId")
        getBatchesNamesViewModel.getBatchesNamesByProduct(productId)
    }

    private fun setupAddProductObserver() {
        addProductViewModel.addProductResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                Log.d("NavigationHome", "Produto adicionado: $it")
                loadProducts()
            }
        }

        addProductViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                if (it.isNotEmpty()) {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    Log.e("NavigationHome", "Erro ao adicionar produto: $error")
                }
            }
        }

        addProductViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                Log.d("NavigationHome", "Adicionando produto...")
            }
        }
    }

    private fun setupAddBatchObserver() {
        addBatchViewModel.addBatchResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                Log.d("NavigationHome", "Lote adicionado: $it")
                clearBatchFields()
                loadBatchesForListing()
            }
        }

        addBatchViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                if (it.isNotEmpty()) {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    Log.e("NavigationHome", "Erro ao adicionar lote: $error")
                }
            }
        }

        addBatchViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                Log.d("NavigationHome", "Adicionando lote...")
            }
        }
    }

    private fun setupDeleteBatchObserver() {
        deleteBatchViewModel.deleteBatchResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                Log.d("NavigationHome", "Lote deletado: $it")
                clearRemoveBatchFields()
                loadBatchesForListing()

                selectedProductIdForRemoval?.let { productId ->
                    loadBatchNumbersForProduct(productId)
                }
            }
        }

        deleteBatchViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                if (it.isNotEmpty()) {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    Log.e("NavigationHome", "Erro ao deletar lote: $error")
                }
            }
        }

        deleteBatchViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                Log.d("NavigationHome", "Deletando lote...")
            }
        }
    }

    private fun openAddProductDialog() {
        // Implementar quando tiver o layout do dialog
    }

    private fun getProductIdByName(productName: String, products: List<ProductResponse>): Long? {
        return products.find { it.name == productName }?.id
    }

    private fun setupTypeDropdown() {
        try {
            val typeInput = content.findViewById<AutoCompleteTextView>(R.id.typeInput)
            val typeInputLayout = content.findViewById<TextInputLayout>(R.id.typeInputLayout)

            typeInput.isEnabled = false
            typeInput.isClickable = false
            typeInput.isFocusable = false
            typeInput.isFocusableInTouchMode = false

            typeInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
            typeInput.hint = "Tipo (preenchido automaticamente)"

            Log.d("NavigationHome", "Campo de tipo configurado como bloqueado")
        } catch (e: Exception) {
            Log.e("NavigationHome", "Erro ao configurar campo de tipo: ${e.message}")
        }
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

    private fun setupBatchNumberDependencies() {
        val productInputRemove = content.findViewById<AutoCompleteTextView>(R.id.productInputRemove)
        val numLoteLayoutRemove = content.findViewById<TextInputLayout>(R.id.numLoteLayoutRemove)
        val numLoteRemove = content.findViewById<AutoCompleteTextView>(R.id.numLoteRemove)

        numLoteLayoutRemove.isEnabled = false
        numLoteRemove.isEnabled = false
        resetBatchNumberDropdown()

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
                    selectedProductIdForRemoval = null
                    resetBatchNumberDropdown()
                }
            }
        })
    }

    private fun showSnackbarMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun validateBatchFields(): Boolean {
        return try {
            val employee = SessionManager.getCurrentProfile()
            val dateInput = content.findViewById<TextInputEditText>(R.id.dateInput)
            val numLoteInput = content.findViewById<TextInputEditText>(R.id.numLoteInput)
            val alturaInput = content.findViewById<TextInputEditText>(R.id.alturaInput)
            val larguraInput = content.findViewById<TextInputEditText>(R.id.larguraInput)
            val comprimentoInput = content.findViewById<TextInputEditText>(R.id.comprimentoInput)

            if (selectedProductIdForBatch == null || selectedProductIdForBatch == 0L) {
                Toast.makeText(requireContext(), "Selecione um produto válido", Toast.LENGTH_SHORT).show()
                return false
            }

            if (employee == null || employee.unit.id == 0L) {
                Toast.makeText(requireContext(), "Dados do usuário inválidos", Toast.LENGTH_SHORT).show()
                return false
            }

            if (dateInput.text.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Informe a data de validade", Toast.LENGTH_SHORT).show()
                return false
            }

            if (numLoteInput.text.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Informe o número do lote", Toast.LENGTH_SHORT).show()
                return false
            }

            val altura = alturaInput.text.toString().toDoubleOrNull()
            val largura = larguraInput.text.toString().toDoubleOrNull()
            val comprimento = comprimentoInput.text.toString().toDoubleOrNull()

            if (altura == null || altura <= 0) {
                Toast.makeText(requireContext(), "Altura deve ser maior que zero", Toast.LENGTH_SHORT).show()
                return false
            }

            if (largura == null || largura <= 0) {
                Toast.makeText(requireContext(), "Largura deve ser maior que zero", Toast.LENGTH_SHORT).show()
                return false
            }

            if (comprimento == null || comprimento <= 0) {
                Toast.makeText(requireContext(), "Comprimento deve ser maior que zero", Toast.LENGTH_SHORT).show()
                return false
            }

            if (altura < 0.01 || largura < 0.01 || comprimento < 0.01) {
                Toast.makeText(requireContext(), "Dimensões muito pequenas (mínimo 0.01)", Toast.LENGTH_SHORT).show()
                return false
            }

            true
        } catch (e: Exception) {
            Log.e("NavigationHome", "Erro na validação: ${e.message}")
            Toast.makeText(requireContext(), "Erro ao validar campos", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun addBatch() {
        try {
            val employee = SessionManager.getCurrentProfile()
            val dateInput = content.findViewById<TextInputEditText>(R.id.dateInput)
            val numLoteInput = content.findViewById<TextInputEditText>(R.id.numLoteInput)
            val alturaInput = content.findViewById<TextInputEditText>(R.id.alturaInput)
            val larguraInput = content.findViewById<TextInputEditText>(R.id.larguraInput)
            val comprimentoInput = content.findViewById<TextInputEditText>(R.id.comprimentoInput)

            if (employee == null) {
                Toast.makeText(requireContext(), "Usuário não logado", Toast.LENGTH_SHORT).show()
                return
            }

            val unitId = employee.unit?.id
            if (unitId == null || unitId == 0L) {
                Toast.makeText(requireContext(), "Unidade não configurada", Toast.LENGTH_SHORT).show()
                return
            }

            val productId = selectedProductIdForBatch
            if (productId == null || productId == 0L) {
                Toast.makeText(requireContext(), "Produto não selecionado", Toast.LENGTH_SHORT).show()
                return
            }

            val batchRequest = BatchRequest(
                batchCode = numLoteInput.text.toString().trim().ifEmpty { "LOTE-${System.currentTimeMillis()}" },
                expirationDate = formatDateForAPI(dateInput.text.toString()),
                height = alturaInput.text.toString().toDoubleOrNull() ?: 1.0,
                width = larguraInput.text.toString().toDoubleOrNull() ?: 1.0,
                length = comprimentoInput.text.toString().toDoubleOrNull() ?: 1.0,
                unitId = unitId,
                productId = productId
            )

            Log.d("NavigationHome", "Enviando BatchRequest: $batchRequest")
            addBatchViewModel.addBatch(batchRequest)

        } catch (e: Exception) {
            Log.e("NavigationHome", "Erro ao criar BatchRequest: ${e.message}", e)
            Toast.makeText(requireContext(), "Erro ao processar dados: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun formatDateForAPI(dateInput: String): String {
        return try {
            val dateParts = dateInput.split(" / ").map { it.trim() }
            if (dateParts.size == 3) {
                val day = dateParts[0].padStart(2, '0')
                val month = dateParts[1].padStart(2, '0')
                val year = dateParts[2]
                "$year-$month-$day"
            } else {
                dateInput
            }
        } catch (e: Exception) {
            dateInput
        }
    }

    private fun clearBatchFields() {
        try {
            val dateInput = content.findViewById<TextInputEditText>(R.id.dateInput)
            val numLoteInput = content.findViewById<TextInputEditText>(R.id.numLoteInput)
            val alturaInput = content.findViewById<TextInputEditText>(R.id.alturaInput)
            val larguraInput = content.findViewById<TextInputEditText>(R.id.larguraInput)
            val comprimentoInput = content.findViewById<TextInputEditText>(R.id.comprimentoInput)
            val productInput = content.findViewById<AutoCompleteTextView>(R.id.productInput)
            val typeInput = content.findViewById<AutoCompleteTextView>(R.id.typeInput)

            dateInput.text?.clear()
            numLoteInput.text?.clear()
            alturaInput.text?.clear()
            larguraInput.text?.clear()
            comprimentoInput.text?.clear()
            productInput.text?.clear()
            typeInput.text?.clear()

            selectedProductIdForBatch = null
        } catch (e: Exception) {
            Log.e("NavigationHome", "Erro ao limpar campos: ${e.message}")
        }
    }

    private fun openAddBatchPopUp() {
        val dialogAddBatch = layoutInflater.inflate(R.layout.pop_up_cadastrar_produto, null)
        val positiveButton = dialogAddBatch.findViewById<Button>(R.id.cadastrarProdutoS)
        val negativeButton = dialogAddBatch.findViewById<Button>(R.id.cadastrarProdutoN)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogAddBatch)
            .create()

        positiveButton.setOnClickListener {
            addBatch()
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun openRemoveBatchPopUp() {
        val numLoteRemove = content.findViewById<AutoCompleteTextView>(R.id.numLoteRemove)
        val selectedBatchNumber = numLoteRemove.text.toString().trim()

        if (!isBatchNumberValid(selectedBatchNumber)) {
            Toast.makeText(requireContext(), "Número do lote não existe", Toast.LENGTH_SHORT).show()
            return
        }


        val dialogRemoveBatch = layoutInflater.inflate(R.layout.pop_up_remover_produto, null)
        val positiveButton = dialogRemoveBatch.findViewById<Button>(R.id.removerProdutoS)
        val negativeButton = dialogRemoveBatch.findViewById<Button>(R.id.removerProdutoN)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogRemoveBatch)
            .create()

        positiveButton.setOnClickListener {
            val numLoteRemove = content.findViewById<AutoCompleteTextView>(R.id.numLoteRemove)
            val selectedBatchNumber = numLoteRemove.text.toString()

            if (validateRemoveBatchFields(selectedBatchNumber)) {
                deleteBatch(selectedBatchNumber)
                dialog.dismiss()
            }
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun isBatchNumberValid(batchNumber: String): Boolean {
        if (batchNumber.isEmpty() ||
            batchNumber == "Nenhum lote encontrado" ||
            batchNumber == "Selecione um produto primeiro") {
            return false
        }
        return currentBatchNumbers.contains(batchNumber)
    }

    private fun validateRemoveBatchFields(selectedBatchNumber: String): Boolean {
        if (selectedProductIdForRemoval == null) {
            Toast.makeText(requireContext(), "Selecione um produto primeiro", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedBatchNumber.isEmpty() ||
            selectedBatchNumber == "Nenhum lote encontrado" ||
            selectedBatchNumber == "Selecione um produto primeiro") {
            Toast.makeText(requireContext(), "Selecione um número de lote válido", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun deleteBatch(batchCode: String) {
        try {
            Log.d("NavigationHome", "Iniciando deleção do lote: $batchCode")
            Log.d("NavigationHome", "Produto ID relacionado: $selectedProductIdForRemoval")

            deleteBatchViewModel.deleteBatch(batchCode)
        } catch (e: Exception) {
            Log.e("NavigationHome", "Erro ao deletar lote: ${e.message}", e)
            Toast.makeText(requireContext(), "Erro ao processar deleção: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearRemoveBatchFields() {
        try {
            val productInputRemove = content.findViewById<AutoCompleteTextView>(R.id.productInputRemove)
            val numLoteRemove = content.findViewById<AutoCompleteTextView>(R.id.numLoteRemove)

            productInputRemove.text?.clear()
            numLoteRemove.text?.clear()
            currentBatchNumbers = emptyList()

            selectedProductIdForRemoval = null
            resetBatchNumberDropdown()

            Log.d("NavigationHome", "Campos de remoção limpos")
        } catch (e: Exception) {
            Log.e("NavigationHome", "Erro ao limpar campos de remoção: ${e.message}")
        }
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