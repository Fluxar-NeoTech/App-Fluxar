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
import com.aula.app_fluxar.API.model.Profile
import com.aula.app_fluxar.API.model.UnitInfos
import com.aula.app_fluxar.API.model.Unit as UnitModel
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

class NavigationUnidades : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var employee: Profile? = null
    private lateinit var filter: Spinner
    private lateinit var unitAdapter: UnitAdapter
    private lateinit var getUnitsViewModel: GetUnitsViewModel
    private lateinit var apiKey: String

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

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getUnitsViewModel = ViewModelProvider(this).get(GetUnitsViewModel::class.java)

        filter = view.findViewById(R.id.spinnerFiltro)
        val filtros = listOf("Sem Filtro", "Mais Próximas", "Maior Disponibilidade")
        val filtroAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filtros)
        filtroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filter.adapter = filtroAdapter
        debugProfileStructure()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        apiKey = BuildConfig.GEOCODING_API_KEY

        if (apiKey.isEmpty()) {
            Log.e("NavigationUnidades", "API Key não configurada!")
            return
        }

        employee = com.aula.app_fluxar.sessionManager.SessionManager.getCurrentProfile()
        employee?.let { emp ->
            observeUnits()
            getUnitsViewModel.getUnits(emp.unit.industry.id)
        }
    }

    private fun observeUnits() {
        getUnitsViewModel.getUnitsResult.observe(viewLifecycleOwner) { unidades ->
            if (unidades != null && employee != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val userLatLng = getLatLngFromAddress(employee!!.unit.enderecoCompleto())
                    if (userLatLng == null) {
                        Log.e("NavigationUnidades", "Não foi possível obter a localização do usuário")
                        return@launch
                    }

                    val listaComDistancias: List<Triple<UnitInfos, LatLng, Float>> = unidades.mapNotNull { unidade ->
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