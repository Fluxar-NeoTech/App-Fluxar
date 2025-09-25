package com.aula.app_fluxar.ui.fragment

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.API.RetrofitClientMapsAPI
import com.aula.app_fluxar.API.model.Employee
import com.aula.app_fluxar.R
import com.aula.app_fluxar.ui.activity.MainActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.aula.app_fluxar.API.model.Unit as UnitModel
import com.aula.app_fluxar.adpters.UnitAdapter
import com.google.android.gms.maps.model.MarkerOptions

class NavigationUnidades : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var employee: Employee? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nav_unidades, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        employee = (activity as? MainActivity)?.getEmployee()

        employee?.let {
            loadUnits(it)
        }
    }

    suspend fun getLatLngFromAddress(address: String): LatLng? {
        return try {
            val response =
                RetrofitClientMapsAPI.instance.getLocation(address, getString(R.string.google_maps_geocoding_key))
            if (response.status == "OK" && response.results.isNotEmpty()) {
                val loc = response.results[0].geometry.location
                LatLng(loc.lat, loc.lng)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun loadUnits(employee: Employee) {
        CoroutineScope(Dispatchers.IO).launch {

            // Pega LatLng da unidade do usuário
            val userLatLng = getLatLngFromAddress(employee.unit.enderecoCompleto())
            if (userLatLng == null) return@launch

            // Outras unidades (mock)
            val unidadesMock = listOf(
                UnitModel(1, "Unidade São Paulo", "01001000", "Rua XV de Novembro", "São Paulo", "SP", "123", "Centro", employee.unit.industry),
                UnitModel(2, "Unidade Belo Horizonte", "30140071", "Av. Afonso Pena", "Belo Horizonte", "MG", "456", "Funcionários", employee.unit.industry),
                UnitModel(3, "Unidade Salvador", "40020000", "Rua Chile", "Salvador", "BA", "789", "Comércio", employee.unit.industry),
                UnitModel(4, "Unidade Curitiba", "80010010", "Rua Marechal Deodoro", "Curitiba", "PR", "321", "Centro", employee.unit.industry)
            )

            // Pega LatLng de cada unidade e calcula distância
            val listaComDistancias = mutableListOf<Triple<UnitModel, LatLng, Float>>()
            for (unidade in unidadesMock) {
                val latLng = getLatLngFromAddress(unidade.enderecoCompleto()) ?: continue
                if (latLng == null) {
                    Log.d("GeoCoding", "Falhou ao geocodificar: ${unidade.nome} - ${unidade.enderecoCompleto()}")
                    continue
                }
                val result = FloatArray(1)
                Location.distanceBetween(
                    userLatLng.latitude, userLatLng.longitude,
                    latLng.latitude, latLng.longitude, result
                )
                listaComDistancias.add(Triple(unidade, latLng, result[0] / 1000f)) // km
            }

            // Ordena por distância
            val ordenada = listaComDistancias.sortedBy { it.third }

            withContext(Dispatchers.Main) {
                // Marcador da unidade do usuário
                mMap.addMarker(
                    MarkerOptions()
                        .position(userLatLng)
                        .title("Minha Unidade")
                        .snippet(employee.unit.enderecoCompleto())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                )

                // Centraliza o mapa
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 5f))

                // Marcadores das outras unidades
                for ((unidade, latLng, distancia) in ordenada) {
                    mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(unidade.nome)
                            .snippet("Distância: %.2f km".format(distancia))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                    )
                }

                // Preenche RecyclerView
                val recyclerView = view?.findViewById<RecyclerView>(R.id.rvUnidade)
                recyclerView?.layoutManager = LinearLayoutManager(requireContext())
                recyclerView?.adapter = UnitAdapter(
                    ordenada.map { it.first to it.third }
                )

                Log.d("RV_CHECK", "Itens no RecyclerView: ${ordenada.size}")
                ordenada.forEach {
                    Log.d("RV_CHECK", "${it.first.nome} - ${it.third} km")
                }
            }
        }
    }
}