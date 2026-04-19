package com.mahdimalv.prompstash

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mahdimalv.prompstash.ui.platformViewModel
import com.mahdimalv.prompstash.ui.navigation.AppNavigation
import com.mahdimalv.prompstash.ui.theme.PrompStashTheme

@Composable
fun PrompStashApp(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalAppContainer provides appContainer) {
        val mainViewModel = platformViewModel { MainViewModel(appContainer.userPreferencesRepository) }
        val themePreference by mainViewModel.themePreference.collectAsStateWithLifecycle()

        PrompStashTheme(themePreference = themePreference) {
            Surface(modifier = modifier.fillMaxSize()) {
                AppNavigation()
            }
        }
    }
}
