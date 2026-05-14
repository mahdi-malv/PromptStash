package com.mahdimalv.promptstash

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mahdimalv.promptstash.ui.platformViewModel
import com.mahdimalv.promptstash.ui.navigation.AppNavigation
import com.mahdimalv.promptstash.ui.theme.PromptStashTheme

@Composable
fun PromptStashApp(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalAppContainer provides appContainer) {
        val mainViewModel = platformViewModel { MainViewModel(appContainer.userPreferencesRepository) }
        val themePreference by mainViewModel.themePreference.collectAsStateWithLifecycle()

        PromptStashTheme(themePreference = themePreference) {
            Surface(modifier = modifier.fillMaxSize()) {
                AppNavigation()
            }
        }
    }
}
