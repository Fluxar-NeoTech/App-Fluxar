package com.aula.app_fluxar.ui.fragment

import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.API.RetrofitClientMapsAPI
import com.aula.app_fluxar.API.model.Profile
import com.aula.app_fluxar.API.model.UnitInfos
import com.aula.app_fluxar.API.viewModel.GetUnitsViewModel
import com.aula.app_fluxar.R
import com.aula.app_fluxar.adpters.UnitAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.aula.app_fluxar.BuildConfig

class NavigationUnits : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var employee: Profile? = null
    private lateinit var filter: Spinner
    private lateinit var unitAdapter: UnitAdapter
    private lateinit var getUnitsViewModel: GetUnitsViewModel
    private lateinit var apiKey: String

    private lateinit var unitsLoadingLayout: LinearLayout
    private lateinit var unitsErrorLayout: LinearLayout
    private lateinit var unitsContentLayout: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var unitsLoadingProgress: ProgressBar
    private lateinit var unitsLoadingText: TextView
    private lateinit var unitsErrorText: TextView
    private lateinit var unitsRetryButton: Button

    private lateinit var emptyLayout: LinearLayout
    private lateinit var emptyText: TextView
    private lateinit var emptySubtext: TextView
    private lateinit var recyclerView: RecyclerView

    private var isFirstLoad = true
    private var isDataLoaded = false
    private var dataLoadAttempts = 0
    private val maxLoadAttempts = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_nav_unidades, container, false)

    private fun debugProfileStructure() {
        val profile = com.aula.app_fluxar.sessionManager.SessionManager.getCurrentProfile()
        if (profile != null) {
            println("=== DEBUG PROFILE STRUCTURE ===")
            println("Profile: $profile")
            println("Profile class: ${profile.javaClass}")
            println("Setor field: ${profile.sector}")
            println("Setor class: ${profile.sector.javaClass}")
            println("Unit field: ${profile.unit}")
            println("Unit class: ${profile.unit.javaClass}")
            println("=== FIM DEBUG ===")
        } else {
            println("DEBUG: Profile é null")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        unitsLoadingLayout = view.findViewById(R.id.unitsLoadingLayout)
        unitsErrorLayout = view.findViewById(R.id.unitsErrorLayout)
        unitsContentLayout = view.findViewById(R.id.unitsContentLayout)
        unitsLoadingProgress = view.findViewById(R.id.unitsLoadingProgress)
        unitsLoadingText = view.findViewById(R.id.unitsLoadingText)
        unitsErrorText = view.findViewById(R.id.unitsErrorText)
        unitsRetryButton = view.findViewById(R.id.unitsRetryButton)

        emptyLayout = view.findViewById(R.id.emptyLayout)
        emptyText = view.findViewById(R.id.emptyText)
        emptySubtext = view.findViewById(R.id.emptySubtext)
        recyclerView = view.findViewById(R.id.rvUnidade)
        filter = view.findViewById(R.id.spinnerFiltro)

        unitsRetryButton.setOnClickListener {
            reloadAllData()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getUnitsViewModel = ViewModelProvider(this).get(GetUnitsViewModel::class.java)

        val filtros = listOf("Sem Filtro", "Mais Próximas", "Maior Disponibilidade")
        val filtroAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, filtros)
        filtroAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        filter.adapter = filtroAdapter

        debugProfileStructure()

        showUnitsLoadingState("Carregando mapa e unidades...")
    }

    override fun onResume() {
        super.onResume()

        if (!isFirstLoad && isDataLoaded) {
            Log.d("NavigationUnits", "🔄 Fragment retomado - recarregando dados rapidamente")
            showUnitsLoadingState("Atualizando...")

            Handler(Looper.getMainLooper()).postDelayed({
                reloadEssentialData()
            }, 300)
        }
    }

    private fun showUnitsLoadingState(message: String = "Carregando...") {
        unitsLoadingLayout.visibility = View.VISIBLE
        unitsErrorLayout.visibility = View.GONE
        unitsContentLayout.visibility = View.GONE
        unitsLoadingText.text = message

        Log.d("NavigationUnits", "📱 Mostrando estado de loading: $message")
    }

    private fun showUnitsContentState() {
        unitsLoadingLayout.visibility = View.GONE
        unitsErrorLayout.visibility = View.GONE
        unitsContentLayout.visibility = View.VISIBLE

        Log.d("NavigationUnits", "✅ Mostrando conteúdo das Unidades")
    }

    private fun showUnitsErrorState(errorMessage: String) {
        unitsLoadingLayout.visibility = View.GONE
        unitsErrorLayout.visibility = View.VISIBLE
        unitsContentLayout.visibility = View.GONE
        unitsErrorText.text = errorMessage

        Log.e("NavigationUnits", "❌ Mostrando estado de erro: $errorMessage")
    }

    private fun reloadAllData() {
        showUnitsLoadingState("Recarregando unidades...")
        isDataLoaded = false
        dataLoadAttempts++

        Log.d("NavigationUnits", "🔄 Recarregando todos os dados - Tentativa $dataLoadAttempts")

        loadUnitsData()
    }

    private fun reloadEssentialData() {
        Log.d("NavigationUnits", "⚡ Recarregando dados essenciais")
        loadUnitsData()
    }

    private fun loadUnitsData() {
        employee = com.aula.app_fluxar.sessionManager.SessionManager.getCurrentProfile()
        employee?.let { emp ->
            observeUnits()
            getUnitsViewModel.getUnits(emp.unit.industry.id)
        } ?: run {
            Log.e("NavigationUnits", "❌ Employee não encontrado")
            showUnitsErrorState("Erro ao carregar dados do usuário")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        apiKey = BuildConfig.GEOCODING_API_KEY

        if (apiKey.isEmpty()) {
            Log.e("NavigationUnidades", "API Key não configurada!")
            showUnitsErrorState("API Key do mapa não configurada")
            return
        }

        // Já iniciamos o carregamento no onViewCreated
        Log.d("NavigationUnits", "✅ Mapa pronto - iniciando carregamento de dados")
    }

    private fun observeUnits() {
        getUnitsViewModel.getUnitsResult.observe(viewLifecycleOwner) { unidades ->
            if (unidades != null && employee != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val userLatLng = getLatLngFromAddress(employee!!.unit.enderecoCompleto())
                        if (userLatLng == null) {
                            Log.e("NavigationUnidades", "Não foi possível obter a localização do usuário")
                            withContext(Dispatchers.Main) {
                                showUnitsErrorState("Erro ao carregar localização")
                            }
                            return@launch
                        }

                        val outrasUnidades = unidades.filter { unidade ->
                            unidade.id != employee!!.unit.id
                        }

                        // VERIFICA SE HÁ UNIDADES DISPONÍVEIS
                        if (outrasUnidades.isEmpty()) {
                            withContext(Dispatchers.Main) {
                                // Ainda mostra o marcador da unidade do usuário
                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(userLatLng)
                                        .title("Minha Unidade")
                                        .snippet(employee!!.unit.enderecoCompleto())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                                )
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 5f))

                                showEmptyState()
                                isDataLoaded = true
                                isFirstLoad = false
                            }
                            return@launch
                        }

                        val listaComDistancias: List<Triple<UnitInfos, LatLng, Float>> = outrasUnidades.mapNotNull { unidade ->
                            val latLng = getLatLngFromAddress(unidade.enderecoCompleto()) ?: return@mapNotNull null
                            val result = FloatArray(1)
                            Location.distanceBetween(
                                userLatLng.latitude, userLatLng.longitude,
                                latLng.latitude, latLng.longitude, result
                            )
                            Triple<UnitInfos, LatLng, Float>(
                                unidade,
                                latLng,
                                result[0] / 1000f
                            )
                        }.sortedBy { it.third }

                        val listaFinal = listaComDistancias.map { triple ->
                            Triple<UnitInfos, Float, Double>(
                                triple.first,
                                triple.third,
                                triple.first.availability
                            )
                        }

                        withContext(Dispatchers.Main) {
                            // Limpa marcadores anteriores
                            mMap.clear()

                            // Adiciona marcador da unidade do usuário
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(userLatLng)
                                    .title("Minha Unidade")
                                    .snippet(employee!!.unit.enderecoCompleto())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                            )
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 5f))

                            for (triple in listaComDistancias) {
                                val unidade = triple.first
                                val latLng = triple.second
                                val distancia = triple.third

                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                        .title(unidade.name)
                                        .snippet("Distância: %.2f km | Disponibilidade: %.1f m³".format(distancia, unidade.availability))
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                )
                            }

                            showContentState(listaFinal)
                            isDataLoaded = true
                            isFirstLoad = false
                        }
                    } catch (e: Exception) {
                        Log.e("NavigationUnits", "❌ Erro ao processar unidades: ${e.message}")
                        withContext(Dispatchers.Main) {
                            if (dataLoadAttempts >= maxLoadAttempts) {
                                showUnitsErrorState("Erro ao carregar unidades: ${e.message}")
                            } else {
                                // Tenta recarregar automaticamente
                                Handler(Looper.getMainLooper()).postDelayed({
                                    loadUnitsData()
                                }, 2000)
                            }
                        }
                    }
                }
            } else {
                Log.e("NavigationUnits", "❌ Unidades ou employee são nulos")
                if (dataLoadAttempts >= maxLoadAttempts) {
                    showUnitsErrorState("Não foi possível carregar as unidades")
                }
            }
        }

        getUnitsViewModel.errorMessage.observe(viewLifecycleOwner) { erro ->
            if (!erro.isNullOrEmpty()) {
                Log.e("NavigationUnidades", "❌ Erro no ViewModel: $erro")
                if (dataLoadAttempts >= maxLoadAttempts) {
                    showUnitsErrorState("Erro: $erro")
                } else {
                    dataLoadAttempts++
                    Handler(Looper.getMainLooper()).postDelayed({
                        loadUnitsData()
                    }, 1000)
                }
            }
        }

        getUnitsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                Log.d("NavigationUnits", "🔄 ViewModel carregando unidades...")
            }
        }
    }

    private fun showContentState(listaFinal: List<Triple<UnitInfos, Float, Double>>) {
        showUnitsContentState()

        // Mostra o RecyclerView e esconde o empty state
        recyclerView.visibility = View.VISIBLE
        emptyLayout.visibility = View.GONE

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        unitAdapter = UnitAdapter(listaFinal)
        recyclerView.adapter = unitAdapter

        // Configura o filtro
        filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> unitAdapter.reset()
                    1 -> unitAdapter.sortByDistance()
                    2 -> unitAdapter.sortByDisponibilidade()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        filter.isEnabled = true
    }

    private fun showEmptyState() {
        showUnitsContentState()

        recyclerView.visibility = View.GONE
        emptyLayout.visibility = View.VISIBLE

        emptyText.text = "Nenhuma unidade disponível"
        emptySubtext.text = "Não há outras unidades disponíveis no momento."

        filter.isEnabled = false
    }

    private suspend fun getLatLngFromAddress(address: String): LatLng? {
        return try {
            val response = RetrofitClientMapsAPI.instance.getLocation(address, apiKey)
            if (response.status == "OK" && response.results.isNotEmpty()) {
                val loc = response.results[0].geometry.location
                LatLng(loc.lat, loc.lng)
            } else {
                Log.e("Geocoding", "Erro na resposta: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("Geocoding", "Erro ao buscar localização: ${e.message}")
            null
        }
    }
}