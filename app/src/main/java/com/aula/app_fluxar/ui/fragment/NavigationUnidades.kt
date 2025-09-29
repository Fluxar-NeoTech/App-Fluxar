package com.aula.app_fluxar.ui.fragment

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.API.RetrofitClientMapsAPI
import com.aula.app_fluxar.API.model.Employee
import com.aula.app_fluxar.API.model.Unit as UnitModel
import com.aula.app_fluxar.API.viewModel.GetUnitsViewModel
import com.aula.app_fluxar.R
import com.aula.app_fluxar.adpters.UnitAdapter
import com.aula.app_fluxar.ui.activity.MainActivity
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

class NavigationUnidades : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var employee: Employee? = null
    private lateinit var filter: Spinner
    private lateinit var unitAdapter: UnitAdapter
    private lateinit var getUnitsViewModel: GetUnitsViewModel

    private lateinit var apiKey: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_nav_unidades, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getUnitsViewModel = ViewModelProvider(this).get(GetUnitsViewModel::class.java)

        filter = view.findViewById(R.id.spinnerFiltro)
        val filtros = listOf("Sem Filtro", "Mais Próximas", "Maior Disponibilidade")
        val filtroAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filtros)
        filtroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filter.adapter = filtroAdapter
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        apiKey = BuildConfig.GEOCODING_API_KEY

        if (apiKey.isEmpty()) {
            Log.e("NavigationUnidades", "API Key não configurada!")
            return
        }

        Log.d("NavigationUnidades", "API Key configurada: ${apiKey.take(10)}...")

        employee = (activity as? MainActivity)?.getEmployee()
        employee?.let { emp ->
            Log.d("NavigationUnidades", "Employee encontrado: ${emp.nome}")
            Log.d("NavigationUnidades", "Endereço da unidade do employee: ${emp.unit.enderecoCompleto()}")
            observeUnits()
            getUnitsViewModel.getUnits(emp.unit.industry.id)
        } ?: run {
            Log.e("NavigationUnidades", "Employee não encontrado!")
        }
    }

    private fun observeUnits() {
        getUnitsViewModel.getUnitsResult.observe(viewLifecycleOwner) { unidades ->
            if (unidades != null && employee != null) {
                Log.d("NavigationUnidades", "Unidades recebidas: ${unidades.size}")

                CoroutineScope(Dispatchers.IO).launch {
                    val userAddress = employee!!.unit.enderecoCompleto()
                    Log.d("NavigationUnidades", "Buscando localização do usuário: $userAddress")

                    val userLatLng = getLatLngFromAddress(userAddress)
                    if (userLatLng == null) {
                        Log.e("NavigationUnidades", "Não foi possível obter a localização do usuário")
                        return@launch
                    }

                    Log.d("NavigationUnidades", "Localização do usuário encontrada: $userLatLng")

                    val listaComDistancias = mutableListOf<Triple<UnitModel, LatLng, Float>>()

                    for (unidade in unidades) {
                        val unitAddress = unidade.enderecoCompleto()
                        Log.d("NavigationUnidades", "Buscando localização da unidade: $unitAddress")

                        val latLng = getLatLngFromAddress(unitAddress)
                        if (latLng != null) {
                            val result = FloatArray(1)
                            Location.distanceBetween(
                                userLatLng.latitude, userLatLng.longitude,
                                latLng.latitude, latLng.longitude, result
                            )
                            val distanciaKm = result[0] / 1000f
                            Log.d("NavigationUnidades", "Distância calculada: $distanciaKm km")

                            listaComDistancias.add(Triple(unidade, latLng, distanciaKm))
                        } else {
                            Log.e("NavigationUnidades", "Não foi possível obter localização para: ${unidade.nome}")
                        }
                    }

                    // Ordena por distância
                    listaComDistancias.sortBy { it.third }

                    // Mock de disponibilidades
                    val disponibilidadesMock = mapOf(
                        1L to 170,
                        2L to 250,
                        3L to 120,
                        4L to 300
                    )

                    val listaFinal = listaComDistancias.map { triple ->
                        Triple<UnitModel, Float, Int>(
                            triple.first,
                            triple.third,
                            disponibilidadesMock[triple.first.id] ?: 0
                        )
                    }

                    withContext(Dispatchers.Main) {
                        Log.d("NavigationUnidades", "Adicionando ${listaComDistancias.size} marcadores no mapa")

                        // Limpa o mapa antes de adicionar novos marcadores
                        mMap.clear()

                        // Adiciona marcador da unidade do usuário
                        mMap.addMarker(
                            MarkerOptions()
                                .position(userLatLng)
                                .title("Minha Unidade")
                                .snippet(employee!!.unit.enderecoCompleto())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                        )

                        // Adiciona marcadores das outras unidades
                        for (triple in listaComDistancias) {
                            val unidade = triple.first
                            val latLng = triple.second
                            val distancia = triple.third

                            mMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title(unidade.nome)
                                    .snippet("Distância: %.2f km".format(distancia))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                            )
                        }

                        // Move a câmera para mostrar todos os marcadores
                        val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
                        bounds.include(userLatLng)
                        listaComDistancias.forEach { bounds.include(it.second) }

                        try {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
                        } catch (e: Exception) {
                            Log.e("NavigationUnidades", "Erro ao ajustar câmera: ${e.message}")
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 10f))
                        }

                        // Configura o RecyclerView
                        val recyclerView = view?.findViewById<RecyclerView>(R.id.rvUnidade)
                        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
                        unitAdapter = UnitAdapter(listaFinal)
                        recyclerView?.adapter = unitAdapter

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
                    }
                }
            }
        }

        getUnitsViewModel.errorMessage.observe(viewLifecycleOwner) { erro ->
            if (!erro.isNullOrEmpty()) Log.e("NavigationUnidades", erro)
        }
    }

    private suspend fun getLatLngFromAddress(address: String): LatLng? {
        return try {
            Log.d("Geocoding", "Buscando: $address")
            val response = RetrofitClientMapsAPI.instance.getLocation(address, apiKey)
            Log.d("Geocoding", "Resposta: ${response.status}")

            if (response.status == "OK" && response.results.isNotEmpty()) {
                val loc = response.results[0].geometry.location
                val latLng = LatLng(loc.lat, loc.lng)
                Log.d("Geocoding", "Localização encontrada: $latLng")
                latLng
            } else {
                Log.e("Geocoding", "Erro na resposta: ${response.status} - ${response ?: "Sem mensagem"}")
                null
            }
        } catch (e: Exception) {
            Log.e("Geocoding", "Erro ao buscar localização: ${e.message}")
            null
        }
    }
}