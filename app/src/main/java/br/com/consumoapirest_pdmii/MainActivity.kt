package br.com.consumoapirest_pdmii

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import br.com.consumoapirest_pdmii.ui.theme.ConsumoApiRest_PDMIITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = PrevisaoRepository()
        enableEdgeToEdge()
        setContent {
            ConsumoApiRest_PDMIITheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TelaPrevisaoTempo(
                        repository = repository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TelaPrevisaoTempo(repository: PrevisaoRepository, modifier: Modifier = Modifier) {
    var cidadeInput by remember { mutableStateOf("") }
    var previsao by remember { mutableStateOf<PrevisaoTempo?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Previsão do Tempo",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = cidadeInput,
            onValueChange = { cidadeInput = it },
            label = { Text("Digite o nome da cidade") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (cidadeInput.isNotBlank()) {
                    coroutineScope.launch {
                        isLoading = true
                        erro = ""
                        previsao = null
                        try {
                            previsao = repository.buscarPrevisao(cidadeInput)
                        } catch (e: Exception) {
                            erro = e.message ?: "Ocorreu um erro desconhecido."
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Buscando..." else "Consultar Temperatura")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Exibe um círculo de carregamento enquanto a consulta ocorre
        if (isLoading) {
            CircularProgressIndicator()
        }

        // Exibe mensagem de erro se ocorrer falha ou cidade não for encontrada
        if (erro.isNotEmpty()) {
            Text(
                text = erro,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Exibe o Card com o resultado quando ele for preenchido com sucesso
        previsao?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = it.cidade,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${it.temperatura}°C",
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }
        }
    }
}
