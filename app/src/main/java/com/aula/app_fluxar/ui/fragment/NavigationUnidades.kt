package com.aula.app_fluxar.ui.fragment

import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.API.model.Employee
import com.aula.app_fluxar.R
import com.aula.app_fluxar.ui.activity.MainActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
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


    private fun loadUnits(employee: Employee) {
        CoroutineScope(Dispatchers.IO).launch {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())

            //Unidade do usuário
            val userAddresses = geocoder.getFromLocationName(employee.unit.enderecoCompleto(), 1)
            if (userAddresses.isNullOrEmpty()) return@launch
            val userLatLng = LatLng(userAddresses[0].latitude, userAddresses[0].longitude)

            //Outras unidades (mock)
            val unidadesMock = listOf(
                UnitModel(1, "Unidade São Paulo", "01001000", "Rua XV de Novembro", "São Paulo", "SP", "123", "Centro", employee.unit.industry),
                UnitModel(2, "Unidade Belo Horizonte", "30140071", "Av. Afonso Pena", "Belo Horizonte", "MG", "456", "Funcionários", employee.unit.industry),
                UnitModel(3, "Unidade Salvador", "40020000", "Rua Chile", "Salvador", "BA", "789", "Comércio", employee.unit.industry),
                UnitModel(4, "Unidade Curitiba", "80010010", "Rua Marechal Deodoro", "Curitiba", "PR", "321", "Centro", employee.unit.industry)
            )

            val listaComDistancias = mutableListOf<Pair<UnitModel, Float>>()

            for (unidade in unidadesMock) {
                val addresses = geocoder.getFromLocationName(unidade.enderecoCompleto(), 1)
                if (!addresses.isNullOrEmpty()) {
                    val latLng = LatLng(addresses[0].latitude, addresses[0].longitude)

                    val result = FloatArray(1)
                    Location.distanceBetween(
                        userLatLng.latitude, userLatLng.longitude,
                        latLng.latitude, latLng.longitude,
                        result
                    )

                    listaComDistancias.add(unidade to (result[0] / 1000f))
                }
            }

            val ordenada = listaComDistancias.sortedBy { it.second }

            withContext(Dispatchers.Main) {
                // Marcador da unidade do usuário
                mMap.addMarker(
                    MarkerOptions()
                        .position(userLatLng)
                        .title("Minha Unidade")
                        .snippet(employee.unit.enderecoCompleto())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )

                // Centraliza o mapa
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 5f))

                // Marcadores das outras unidades
                for ((unidade, distancia) in ordenada) {
                    val addresses = geocoder.getFromLocationName(unidade.enderecoCompleto(), 1)
                    if (!addresses.isNullOrEmpty()) {
                        val latLng = LatLng(addresses[0].latitude, addresses[0].longitude)
                        mMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(unidade.nome)
                                .snippet("Distância: %.2f km".format(distancia))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        )
                    }
                }

                // Preenche RecyclerView
                val recyclerView = view?.findViewById<RecyclerView>(R.id.rvUnidade)
                recyclerView?.layoutManager = LinearLayoutManager(requireContext())
                recyclerView?.adapter = UnitAdapter(ordenada)
            }
        }
    }
}