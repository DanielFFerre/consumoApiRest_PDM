package br.com.consumoapirest_pdmii

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class PrevisaoRepository {

    suspend fun buscarPrevisao(cidade: String): PrevisaoTempo = withContext(Dispatchers.IO) {
        // Formata o nome da cidade para uso em URLs (ex: São Paulo -> S%C3%A3o+Paulo)
        val cidadeUrl = URLEncoder.encode(cidade, "UTF-8")
        // Utilizamos a API gratuita wttr.in, que não requer chave (API Key)
        val url = URL("https://wttr.in/$cidadeUrl?format=j1")
        val conexao = url.openConnection() as HttpURLConnection

        try {
            conexao.requestMethod = "GET"
            conexao.connectTimeout = 10000 // 10 segundos de timeout para conexão
            conexao.readTimeout = 10000    // 10 segundos de timeout para leitura

            val codigoResposta = conexao.responseCode
            val fluxo = if (codigoResposta in 200..299) {
                conexao.inputStream
            } else {
                conexao.errorStream
            }

            val resposta = fluxo?.bufferedReader()?.use { it.readText() }
                ?: throw IOException("A API não retornou conteúdo.")

            if (codigoResposta !in 200..299) {
                throw IOException("Cidade não encontrada ou erro na API.")
            }

            // Realiza a leitura e conversão do JSON recebido
            val json = JSONObject(resposta)
            
            // Busca a temperatura atual
            val condicaoAtual = json.getJSONArray("current_condition").getJSONObject(0)
            val tempC = condicaoAtual.getString("temp_C")

            // Busca o nome da área/cidade resolvida pela API
            val area = json.getJSONArray("nearest_area").getJSONObject(0)
            val areaName = area.getJSONArray("areaName").getJSONObject(0).getString("value")

            PrevisaoTempo(
                cidade = areaName,
                temperatura = tempC
            )
        } catch (e: Exception) {
            throw IOException("Falha ao buscar a cidade: ${e.localizedMessage}")
        } finally {
            conexao.disconnect()
        }
    }
}
