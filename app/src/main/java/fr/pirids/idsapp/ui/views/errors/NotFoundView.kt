@file:OptIn(ExperimentalAnimationApi::class)

package fr.pirids.idsapp.ui.views.errors

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import fr.pirids.idsapp.R

@Composable
fun NotFoundView(navController: NavHostController) {
    Surface(
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            modifier = Modifier.wrapContentHeight(),
            text = stringResource(id = R.string.not_found),
            color = MaterialTheme.colorScheme.onError,
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotFoundViewPreview() {
    NotFoundView(navController = rememberAnimatedNavController())
}