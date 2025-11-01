import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.StockOutRetrofitClient
import com.aula.app_fluxar.API.model.StockOutListResponse
import com.aula.app_fluxar.API.model.StockOutRequest
import com.aula.app_fluxar.API.model.StockOutResponse
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class StockOutViewModel : ViewModel() {
    private val _productStockOut = MutableLiveData<List<StockOutResponse>?>()
    val productStockOut: LiveData<List<StockOutResponse>?> = _productStockOut

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchStockOut(stockOutRequest: StockOutRequest) {
        _isLoading.value = true
        _errorMessage.value = ""
        _productStockOut.value = null

        viewModelScope.launch {
            try {
                val response: Response<StockOutListResponse> =
                    StockOutRetrofitClient.instance.getNotification(stockOutRequest)

                if (response.isSuccessful) {
                    val body = response.body()
                    _productStockOut.value = body?.predictions
                    _errorMessage.value = ""
                    Log.d("StockOutViewModel", "✅ Carregando produtos: ${body?.predictions}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = if (!errorBody.isNullOrEmpty()) {
                        try {
                            val json = JSONObject(errorBody)
                            json.optString("detail", "Erro desconhecido no servidor.")
                        } catch (e: Exception) {
                            "Erro inesperado (${response.code()})"
                        }
                    } else {
                        "Erro inesperado (${response.code()})"
                    }

                    _errorMessage.value = errorMsg
                    Log.e("StockOutViewModel", "❌ Erro ao buscar previsão: $errorMsg")
                }

            } catch (e: Exception) {
                val errorMsg = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                _errorMessage.value = errorMsg
                Log.e("StockOutViewModel", errorMsg, e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
