package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.MainNavTab
import com.example.ui.components.localizedCopy
import com.example.ui.screens.DlsKitUploaderScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KitCollectionScreen
import com.example.ui.screens.KitCreatorScreen
import com.example.ui.screens.KitDetailScreen
import com.example.ui.screens.LogoCreatorScreen
import com.example.ui.screens.MyDesignsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.DlsKitStudioTheme
import com.example.ui.viewmodel.KitCreatorViewModel
import com.example.ui.viewmodel.KitEditorTab
import com.example.ui.viewmodel.LogoCreatorViewModel
import com.example.ui.viewmodel.MainStudioViewModel
import kotlinx.coroutines.launch

private const val ROUTE_KIT_DETAIL = "kit_detail/{clubId}"
private const val ROUTE_MY_DESIGNS = "my_designs/{initialTab}"
private const val ROUTE_KIT_UPLOADER = "kit_uploader"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as DlsKitStudioApp
        setContent {
            val mainVm: MainStudioViewModel = viewModel(
                factory = MainStudioViewModel.Factory(
                    app.catalogRepository,
                    app.designRepository,
                    app.preferencesDataStore
                )
            )
            val kitCreatorVm: KitCreatorViewModel = viewModel(
                factory = KitCreatorViewModel.Factory(
                    app.designRepository,
                    app.preferencesDataStore
                )
            )
            val logoCreatorVm: LogoCreatorViewModel = viewModel(
                factory = LogoCreatorViewModel.Factory(
                    app.designRepository,
                    app.preferencesDataStore
                )
            )
            val prefs by mainVm.userPreferences.collectAsStateWithLifecycle()

            DlsKitStudioTheme(themePreference = prefs.themePreference) {
                DlsKitStudioRoot(
                    mainVm = mainVm,
                    kitCreatorVm = kitCreatorVm,
                    logoCreatorVm = logoCreatorVm
                )
            }
        }
    }
}

private data class NavItemMeta(
    val tab: MainNavTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun DlsKitStudioRoot(
    mainVm: MainStudioViewModel,
    kitCreatorVm: KitCreatorViewModel,
    logoCreatorVm: LogoCreatorViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val prefs by mainVm.userPreferences.collectAsStateWithLifecycle()
    val copy = remember(prefs.language) { localizedCopy(prefs.language) }

    val allClubs by mainVm.allClubs.collectAsStateWithLifecycle()
    val filteredClubs by mainVm.filteredClubs.collectAsStateWithLifecycle()
    val searchQuery by mainVm.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by mainVm.selectedFilter.collectAsStateWithLifecycle()
    val sortOption by mainVm.sortOption.collectAsStateWithLifecycle()
    val isSyncing by mainVm.isRemoteSyncing.collectAsStateWithLifecycle()
    val recentlyViewed by mainVm.recentlyViewed.collectAsStateWithLifecycle()

    val navItems = remember(copy) {
        listOf(
            NavItemMeta(MainNavTab.HOME, copy.navHome, Icons.Filled.Home, Icons.Outlined.Home),
            NavItemMeta(MainNavTab.KITS, copy.navKits, Icons.Filled.Checkroom, Icons.Outlined.Checkroom),
            NavItemMeta(MainNavTab.CREATE, copy.navCreate, Icons.Filled.Brush, Icons.Outlined.Brush),
            NavItemMeta(MainNavTab.LOGOS, copy.navLogos, Icons.Filled.Shield, Icons.Outlined.Shield),
            NavItemMeta(MainNavTab.PROFILE, copy.navProfile, Icons.Filled.Person, Icons.Outlined.Person)
        )
    }

    val showTopLevelNav = MainNavTab.entries.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showTopLevelNav && !useRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        navItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(item.tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_${item.tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showTopLevelNav && useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        navItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.tab.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(item.tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = MainNavTab.HOME.route,
                    enterTransition = { fadeIn(animationSpec = tween(220)) },
                    exitTransition = { fadeOut(animationSpec = tween(180)) },
                    modifier = Modifier.weight(1f)
                ) {
                    composable(MainNavTab.HOME.route) {
                        HomeScreen(
                            copy = copy,
                            trendingClubs = mainVm.trendingClubs,
                            recentlyAddedClubs = mainVm.recentlyAddedClubs,
                            popularClubCollections = mainVm.popularClubCollections,
                            nationalTeams = mainVm.nationalTeams,
                            featuredTemplates = mainVm.featuredTemplates,
                            gkTemplates = mainVm.gkTemplates,
                            recentlyViewed = recentlyViewed,
                            allClubs = allClubs,
                            onSearchClicked = { navigateToTab(MainNavTab.KITS.route) },
                            onClubSelected = { club ->
                                navController.navigate("kit_detail/${club.id}")
                            },
                            onSeeAllFilter = { filter ->
                                mainVm.selectFilter(filter)
                                navigateToTab(MainNavTab.KITS.route)
                            },
                            onOpenKitCreator = { navigateToTab(MainNavTab.CREATE.route) },
                            onOpenGkStudio = {
                                kitCreatorVm.setActiveTab(KitEditorTab.GK_STUDIO)
                                navigateToTab(MainNavTab.CREATE.route)
                            },
                            onOpenLogoCreator = { navigateToTab(MainNavTab.LOGOS.route) },
                            onOpenKitUploader = { navController.navigate(ROUTE_KIT_UPLOADER) },
                            onUseTemplate = { tpl ->
                                kitCreatorVm.loadFromSpec(
                                    spec = tpl.designSpec,
                                    name = tpl.title
                                )
                                navigateToTab(MainNavTab.CREATE.route)
                            }
                        )
                    }

                    composable(MainNavTab.KITS.route) {
                        KitCollectionScreen(
                            clubs = filteredClubs,
                            searchQuery = searchQuery,
                            selectedFilter = selectedFilter,
                            sortOption = sortOption,
                            isSyncing = isSyncing,
                            onSearchQueryChange = mainVm::updateSearchQuery,
                            onFilterSelected = mainVm::selectFilter,
                            onSortSelected = mainVm::selectSort,
                            onSyncCatalog = {
                                mainVm.attemptCatalogSync { remoteUpdated ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (remoteUpdated) "Synced latest remote DLS kits"
                                            else "Using offline DLS 2025/26 catalog"
                                        )
                                    }
                                }
                            },
                            onClubSelected = { club ->
                                navController.navigate("kit_detail/${club.id}")
                            },
                            onOpenKitUploader = { navController.navigate(ROUTE_KIT_UPLOADER) }
                        )
                    }

                    composable(MainNavTab.CREATE.route) {
                        KitCreatorScreen(
                            viewModel = kitCreatorVm,
                            catalogClubs = allClubs,
                            onOpenMyDesigns = {
                                navController.navigate("my_designs/0")
                            },
                            onOpenKitUploader = { navController.navigate(ROUTE_KIT_UPLOADER) }
                        )
                    }

                    composable(MainNavTab.LOGOS.route) {
                        LogoCreatorScreen(
                            viewModel = logoCreatorVm,
                            templates = mainVm.logoTemplates,
                            onOpenMyDesigns = {
                                navController.navigate("my_designs/1")
                            }
                        )
                    }

                    composable(MainNavTab.PROFILE.route) {
                        ProfileScreen(
                            viewModel = mainVm,
                            onOpenMyDesigns = { tabIndex ->
                                navController.navigate("my_designs/$tabIndex")
                            }
                        )
                    }

                    composable(
                        route = ROUTE_KIT_DETAIL,
                        arguments = listOf(navArgument("clubId") { type = NavType.StringType }),
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(250))
                        },
                        exitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(220))
                        }
                    ) { entry ->
                        val clubId = entry.arguments?.getString("clubId").orEmpty()
                        val club = mainVm.getClubById(clubId) ?: allClubs.first()
                        KitDetailScreen(
                            club = club,
                            viewModel = mainVm,
                            onBack = { navController.popBackStack() },
                            onRemixInCreator = { selectedClub, slotIndex ->
                                kitCreatorVm.loadFromClub(selectedClub, slotIndex)
                                navigateToTab(MainNavTab.CREATE.route)
                            }
                        )
                    }

                    composable(
                        route = ROUTE_MY_DESIGNS,
                        arguments = listOf(navArgument("initialTab") { type = NavType.IntType })
                    ) { entry ->
                        val initialTab = entry.arguments?.getInt("initialTab") ?: 0
                        MyDesignsScreen(
                            viewModel = mainVm,
                            initialTab = initialTab,
                            onBack = { navController.popBackStack() },
                            onEditKit = { entity, spec ->
                                kitCreatorVm.loadFromSpec(
                                    spec = spec,
                                    name = entity.name,
                                    existingId = entity.id,
                                    league = entity.league,
                                    season = entity.season
                                )
                                navigateToTab(MainNavTab.CREATE.route)
                            },
                            onEditLogo = { entity, spec ->
                                logoCreatorVm.loadSavedLogo(
                                    id = entity.id,
                                    name = entity.name,
                                    spec = spec
                                )
                                navigateToTab(MainNavTab.LOGOS.route)
                            },
                            onClubSelected = { club ->
                                navController.navigate("kit_detail/${club.id}")
                            }
                        )
                    }

                    composable(
                        route = ROUTE_KIT_UPLOADER,
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(240))
                        },
                        exitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(200))
                        }
                    ) {
                        DlsKitUploaderScreen(
                            onBack = { navController.popBackStack() },
                            onImportSpecToCreator = { spec, title ->
                                kitCreatorVm.loadFromSpec(spec = spec, name = title)
                                navigateToTab(MainNavTab.CREATE.route)
                            }
                        )
                    }
                }
            }
        }
    }
}
