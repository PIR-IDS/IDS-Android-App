package fr.pirids.idsapp.ui.views

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceView(navController: NavHostController) {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = { TopBar(navController) }
        ) {
            Text(
                "",
                modifier = Modifier
                    .padding(top = it.calculateTopPadding())
            )
        }
    }
}

@Preview
@Composable
fun ServiceViewPreview() {
    ServiceView(navController = rememberNavController())
}