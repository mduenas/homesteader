package com.markduenas.homesteader.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition
import com.markduenas.homesteader.core.designsystem.components.AdBanner
import com.markduenas.homesteader.core.designsystem.icons.AnimalsIcon
import com.markduenas.homesteader.core.designsystem.icons.CalendarIcon
import com.markduenas.homesteader.core.designsystem.icons.DashboardIcon
import com.markduenas.homesteader.core.designsystem.icons.MoreIcon
import com.markduenas.homesteader.domain.monetization.AdManager
import com.markduenas.homesteader.feature.animal.list.AnimalListScreen
import com.markduenas.homesteader.feature.calendar.CalendarScreen
import com.markduenas.homesteader.feature.dashboard.DashboardScreen
import com.markduenas.homesteader.feature.more.MoreScreen
import org.koin.compose.koinInject

class MainScreen : Screen {

    @Composable
    override fun Content() {
        val adManager: AdManager = koinInject()

        TabNavigator(
            tab = DashboardTab,
            tabDisposable = { TabDisposable(it, listOf(DashboardTab, AnimalsTab, CalendarTab, MoreTab)) }
        ) { tabNavigator ->
            Scaffold(
                topBar = {
                    // Ad banner at top (handles its own safe area padding)
                    AdBanner(adManager = adManager)
                },
                bottomBar = {
                    // Bottom navigation
                    NavigationBar {
                        TabNavigationItem(DashboardTab)
                        TabNavigationItem(AnimalsTab)
                        TabNavigationItem(CalendarTab)
                        TabNavigationItem(MoreTab)
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CurrentTab()
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current.key == tab.key
    val iconTint = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    NavigationBarItem(
        selected = isSelected,
        onClick = { tabNavigator.current = tab },
        icon = {
            when (tab) {
                is DashboardTab -> DashboardIcon(tint = iconTint)
                is AnimalsTab -> AnimalsIcon(tint = iconTint)
                is CalendarTab -> CalendarIcon(tint = iconTint)
                is MoreTab -> MoreIcon(tint = iconTint)
            }
        },
        label = { Text(text = tab.options.title) }
    )
}

object DashboardTab : Tab {
    private fun readResolve(): Any = DashboardTab

    override val options: TabOptions
        @Composable
        get() = remember {
            TabOptions(
                index = 0u,
                title = "Dashboard",
                icon = null
            )
        }

    @Composable
    override fun Content() {
        Navigator(DashboardScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}

object AnimalsTab : Tab {
    private fun readResolve(): Any = AnimalsTab

    override val options: TabOptions
        @Composable
        get() = remember {
            TabOptions(
                index = 1u,
                title = "Animals",
                icon = null
            )
        }

    @Composable
    override fun Content() {
        Navigator(AnimalListScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}

object CalendarTab : Tab {
    private fun readResolve(): Any = CalendarTab

    override val options: TabOptions
        @Composable
        get() = remember {
            TabOptions(
                index = 2u,
                title = "Calendar",
                icon = null
            )
        }

    @Composable
    override fun Content() {
        Navigator(CalendarScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}

object MoreTab : Tab {
    private fun readResolve(): Any = MoreTab

    override val options: TabOptions
        @Composable
        get() = remember {
            TabOptions(
                index = 3u,
                title = "More",
                icon = null
            )
        }

    @Composable
    override fun Content() {
        Navigator(MoreScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}

private class TabDisposable(
    private val navigator: TabNavigator,
    private val tabs: List<Tab>
)
