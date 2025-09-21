// File 1: Imports.kt
package com.ramadan.newandoidnavpoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// File 2: Models.kt - Data Classes, Enums, and Constants

// Main Page Indices for HorizontalPager
object PageIndex {
    const val INSIGHTS = 0
    const val HOME = 1
    const val PROFILE = 2
}

// Navigation Events - Only for screen navigation within each page
sealed class NavigationEvent {
    data object Home : NavigationEvent()
    data object Transactions : NavigationEvent()
    data object Accounts : NavigationEvent()
    data object Insights : NavigationEvent()
    data object Profile : NavigationEvent()
    data object Back : NavigationEvent()
}

// Top Bar Events - Separate from navigation
sealed class TopBarEvent {
    data object RightIconClicked : TopBarEvent()
    data object LeftIconClicked : TopBarEvent()
    data object TitleClicked : TopBarEvent()
}

// Screen Theme Configuration
enum class ScreenTheme {
    TRANSPARENT,  // Transparent container to show Msa3ed
    WHITE,        // Solid white background
    LIGHT_GRAY,   // Light gray background
    DARK          // Dark background
}

data class ScreenConfiguration(
    val theme: ScreenTheme = ScreenTheme.WHITE,
    val hasBackButton: Boolean = false,
    val title: String = "KD Banking",
    val showRightIcon: Boolean = false,
    val rightIcon: String = ""
)

// Navigation State
data class NavigationState(
    val currentEvent: NavigationEvent? = null,
    val screenConfig: ScreenConfiguration = ScreenConfiguration(),
    val onEventHandled: () -> Unit = {},
    val onNavigate: (NavigationEvent) -> Unit = {}
)

// Top Bar State
data class TopBarState(
    val currentEvent: TopBarEvent? = null,
    val onEventHandled: () -> Unit = {},
    val onTriggerEvent: (TopBarEvent) -> Unit = {}
)

// Main App State
data class AppState(
    val currentPage: Int = PageIndex.HOME,
    val isSwipeEnabled: Boolean = true,
    val pagerState: PagerState
)

// CompositionLocal declarations
val LocalNavigationState = compositionLocalOf<NavigationState> {
    error("NavigationState not provided")
}

val LocalTopBarState = compositionLocalOf<TopBarState> {
    error("TopBarState not provided")
}

val LocalAppState = compositionLocalOf<AppState> {
    error("AppState not provided")
}

// Routes
object Routes {
    const val HOME = "home"
    const val TRANSACTIONS = "transactions"
    const val ACCOUNTS = "accounts"
    const val INSIGHTS = "insights"
    const val PROFILE = "profile"
}

// Data Models
data class TransactionData(
    val title: String,
    val subtitle: String,
    val amount: String = "",
    val type: String = "default"
)

data class AccountData(
    val name: String,
    val description: String,
    val balance: String
)

data class ProfileMenuItem(
    val title: String,
    val subtitle: String = "",
    val icon: String,
    val color: Color
)


// File 3: SampleData.kt - Sample Data and Extensions

// Sample Data
val sampleTransactions = listOf(
    TransactionData("Salary Deposit", "Today, 10:30 AM", "+3,500.00", "income"),
    TransactionData("Grocery Store", "Yesterday, 6:45 PM", "-127.50", "food"),
    TransactionData("Netflix", "Dec 23, 2024", "-14.99", "entertainment"),
    TransactionData("Gas Station", "Dec 22, 2024", "-45.20", "transport"),
    TransactionData("Dividend", "Dec 21, 2024", "+250.00", "investment"),
    TransactionData("Restaurant", "Dec 20, 2024", "-89.75", "food"),
    TransactionData("Amazon", "Dec 19, 2024", "-156.30", "shopping"),
    TransactionData("Freelance", "Dec 18, 2024", "+800.00", "income"),
    TransactionData("Electric Bill", "Dec 17, 2024", "-78.90", "utilities"),
    TransactionData("Coffee", "Dec 16, 2024", "-12.45", "food"),
    TransactionData("Transfer", "Dec 15, 2024", "-200.00", "transfer"),
    TransactionData("Refund", "Dec 14, 2024", "+45.99", "refund")
)

val sampleAccounts = listOf(
    AccountData("Current Account", "Primary checking", "4,256.78"),
    AccountData("Savings Account", "High-yield savings", "12,450.00"),
    AccountData("Investment", "Portfolio", "28,975.50"),
    AccountData("Credit Card", "Monthly expenses", "-1,234.56"),
    AccountData("Business", "Operations", "5,678.90"),
    AccountData("Emergency", "Reserves", "8,500.00")
)

fun getProfileMenuItems(): List<ProfileMenuItem> = listOf(
    ProfileMenuItem("Personal Information", "Name, email, phone", "👤", Color(0xFF6B46C1)),
    ProfileMenuItem("Account Preferences", "Language, currency", "⚙", Color(0xFF059669)),
    ProfileMenuItem("Privacy Settings", "Data usage, permissions", "🔒", Color(0xFFEA580C)),
    ProfileMenuItem("Notification Settings", "Alerts and updates", "🔔", Color(0xFFDC2626)),
    ProfileMenuItem("Linked Devices", "Manage your devices", "📱", Color(0xFF7C2D12)),
    ProfileMenuItem("Backup & Sync", "Data backup options", "☁", Color(0xFF4F46E5))
)

// Top Bar Event Extensions
@Composable
fun TopBarState.onRightIconClick(action: () -> Unit) {
    LaunchedEffect(this.currentEvent) {
        if (this@onRightIconClick.currentEvent is TopBarEvent.RightIconClicked) {
            action()
            this@onRightIconClick.onEventHandled()
        }
    }
}

@Composable
fun TopBarState.onTitleClick(action: () -> Unit) {
    LaunchedEffect(this.currentEvent) {
        if (this@onTitleClick.currentEvent is TopBarEvent.TitleClicked) {
            action()
            this@onTitleClick.onEventHandled()
        }
    }
}

@Composable
fun TopBarState.onLeftIconClick(action: () -> Unit) {
    LaunchedEffect(this.currentEvent) {
        if (this@onLeftIconClick.currentEvent is TopBarEvent.LeftIconClicked) {
            action()
            this@onLeftIconClick.onEventHandled()
        }
    }
}

@Composable
fun TopBarState.handleEvents(
    onRightIcon: () -> Unit = {},
    onTitle: () -> Unit = {},
    onLeftIcon: () -> Unit = {}
) {
    LaunchedEffect(this.currentEvent) {
        when (this@handleEvents.currentEvent) {
            is TopBarEvent.RightIconClicked -> onRightIcon()
            is TopBarEvent.TitleClicked -> onTitle()
            is TopBarEvent.LeftIconClicked -> onLeftIcon()
            null -> {}
        }
        this@handleEvents.currentEvent?.let {
            this@handleEvents.onEventHandled()
        }
    }
}

// File 4: MainActivity.kt - Main Activity and Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                TwoLayerNavigationApp()
            }
        }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) {
        darkColorScheme(primary = Color(0xFF6B46C1))
    } else {
        lightColorScheme(primary = Color(0xFF6B46C1))
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

@Composable
fun TwoLayerNavigationApp() {
    val configuration = LocalConfiguration.current
    val screenHeight = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    val scrollState = rememberScrollState()
    val listState = rememberLazyListState()

    val targetPaddingTop by remember {
        derivedStateOf {
            if (scrollState.value > 0 || listState.firstVisibleItemScrollOffset > 0) 60.dp else 90.dp
        }
    }

    val dynamicPaddingTop by animateDpAsState(
        targetValue = targetPaddingTop,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        ),
        label = "paddingAnim"
    )

    val offsetY = remember { Animatable(0f) }
    var isHomeVisible by remember { mutableStateOf(true) }
    var containerBackground by remember { mutableStateOf(Color.Transparent) }
    var currentRoute by remember { mutableStateOf(Routes.HOME) }
    val scope = rememberCoroutineScope()

    // Track if we're on home route to enable/disable pager scrolling
    var isOnHomeRoute by remember { mutableStateOf(true) }

    val pagerState = rememberPagerState(
        initialPage = PageIndex.HOME,
        pageCount = { 3 }
    )

    val appState = AppState(
        currentPage = pagerState.currentPage,
        isSwipeEnabled = isOnHomeRoute,
        pagerState = pagerState
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Background Msa3ed Screen - Fixed outside pager
        Msa3edScreen(
            showCloseButton = !isHomeVisible,
            onClose = {
                isHomeVisible = true
                scope.launch {
                    offsetY.animateTo(
                        0f,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessLow,
                            dampingRatio = Spring.DampingRatioNoBouncy
                        )
                    )
                }
            }
        )

        // Foreground Navigation Container - Fixed outside pager
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .zIndex(1f)
                .padding(top = dynamicPaddingTop)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(containerBackground)
        ) {
            // Drag Handle Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { _, dragAmount ->
                                val newOffset = (offsetY.value + dragAmount.y).coerceAtLeast(0f)
                                scope.launch { offsetY.snapTo(newOffset) }
                            },
                            onDragEnd = {
                                scope.launch {
                                    if (offsetY.value > screenHeight * 0.25f) {
                                        isHomeVisible = false
                                        offsetY.animateTo(
                                            screenHeight + 100f,
                                            animationSpec = spring(
                                                stiffness = Spring.StiffnessLow,
                                                dampingRatio = Spring.DampingRatioNoBouncy
                                            )
                                        )
                                    } else {
                                        isHomeVisible = true
                                        offsetY.animateTo(
                                            0f,
                                            animationSpec = spring(
                                                stiffness = Spring.StiffnessLow,
                                                dampingRatio = Spring.DampingRatioNoBouncy
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.Gray.copy(alpha = 0.5f))
                )
            }

            // Fixed Top Bar
            TopNavigationBarFixed(
                onRouteChange = { route ->
                    currentRoute = route
                    isOnHomeRoute = route == Routes.HOME
                },
                onBackgroundChange = { background ->
                    containerBackground = background
                }
            )

            // HorizontalPager with only the navigation content (NavHost + BottomBar)
            CompositionLocalProvider(LocalAppState provides appState) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 70.dp), // Space for fixed top bar
                    beyondViewportPageCount = 1,
                    pageSpacing = 0.dp,
                    pageSize = PageSize.Fill,
                    flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
                    userScrollEnabled = isOnHomeRoute,
                    key = { page ->
                        when (page) {
                            PageIndex.INSIGHTS -> "insights"
                            PageIndex.HOME -> "home"
                            PageIndex.PROFILE -> "profile"
                            else -> page
                        }
                    }
                ) { pageIndex ->
                    when (pageIndex) {
                        PageIndex.INSIGHTS -> FullScreenInsightsPage()
                        PageIndex.HOME -> BankingHomePage(
                            scrollState = scrollState,
                            listState = listState,
                            onRouteChange = { route ->
                                currentRoute = route
                                isOnHomeRoute = route == Routes.HOME
                            }
                        )
                        PageIndex.PROFILE -> FullScreenProfilePage()
                    }
                }
            }
        }
    }
}

// File 5: MainPages.kt - Main Page Components

@Composable
fun BankingHomePage(
    scrollState: ScrollState,
    listState: LazyListState,
    onRouteChange: (String) -> Unit
) {
    // Banking home page now contains only the navigation content
    NavigationHost(
        onRouteChange = onRouteChange,
        scrollState = scrollState,
        listState = listState
    )
}

@Composable
fun FullScreenInsightsPage() {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
           // .padding(horizontal = 10.dp)
         //   .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
           // FullScreenTopBar(title = "Financial Insights", showBackToHome = true)

            TransparentTwoPartLayout(
                scrollState = scrollState,
                topContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Financial Health",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Excellent",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Green
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        repeat(3) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Metric ${it + 1}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    "${(it + 1) * 25}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                },
                bottomContent = {
                    Column {
                        Text(
                            "Advanced Analytics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        repeat(15) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "Analysis ${it + 1}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "Deep insights",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    "${(it + 1) * 150}.00 KWD",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun FullScreenProfilePage() {
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
           // .padding(horizontal = 10.dp)
           // .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xFFF8F9FA))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
          //  FullScreenTopBar(title = "User Profile", showBackToHome = true)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { UserProfileCard() }
                item { QuickActionsRow() }
                item {
                    Text(
                        "Advanced Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                items(getProfileMenuItems()) { menuItem ->
                    ProfileMenuCard(menuItem)
                }
                item { SecuritySection() }
                item { SupportSection() }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

// File 6: Navigation.kt - Navigation Components

@Composable
fun NavigationHost(
    onRouteChange: (String) -> Unit,
    scrollState: ScrollState,
    listState: LazyListState
) {
    val navController = rememberNavController()
    var currentEvent by remember { mutableStateOf<NavigationEvent?>(null) }
    var currentTopBarEvent by remember { mutableStateOf<TopBarEvent?>(null) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Notify parent about route changes
    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            onRouteChange(route)
        }
    }

    val screenConfig = when (currentRoute) {
        Routes.HOME -> ScreenConfiguration(
            theme = ScreenTheme.TRANSPARENT,
            hasBackButton = false,
            title = "KD Banking",
            showRightIcon = true,
            rightIcon = "♥"
        )

        Routes.TRANSACTIONS -> ScreenConfiguration(
            theme = ScreenTheme.WHITE,
            hasBackButton = true,
            title = "Transactions"
        )

        Routes.ACCOUNTS -> ScreenConfiguration(
            theme = ScreenTheme.WHITE,
            hasBackButton = true,
            title = "Accounts"
        )

        Routes.INSIGHTS -> ScreenConfiguration(
            theme = ScreenTheme.TRANSPARENT,
            hasBackButton = true,
            title = "Insights"
        )

        Routes.PROFILE -> ScreenConfiguration(
            theme = ScreenTheme.LIGHT_GRAY,
            hasBackButton = true,
            title = "Profile",
            showRightIcon = true,
            rightIcon = "🔍"
        )

        else -> ScreenConfiguration()
    }

    val navigationState = NavigationState(
        currentEvent = currentEvent,
        screenConfig = screenConfig,
        onEventHandled = { currentEvent = null },
        onNavigate = { event ->
            currentEvent = event
            when (event) {
                is NavigationEvent.Home -> navController.navigate(Routes.HOME)
                is NavigationEvent.Transactions -> navController.navigate(Routes.TRANSACTIONS)
                is NavigationEvent.Accounts -> navController.navigate(Routes.ACCOUNTS)
                is NavigationEvent.Insights -> navController.navigate(Routes.INSIGHTS)
                is NavigationEvent.Profile -> navController.navigate(Routes.PROFILE)
                is NavigationEvent.Back -> navController.popBackStack()
            }
        }
    )

    val topBarState = TopBarState(
        currentEvent = currentTopBarEvent,
        onEventHandled = { currentTopBarEvent = null },
        onTriggerEvent = { event -> currentTopBarEvent = event }
    )

    CompositionLocalProvider(
        LocalNavigationState provides navigationState,
        LocalTopBarState provides topBarState
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                NavHost(navController = navController, startDestination = Routes.HOME) {
                    composable(Routes.HOME) { HomeScreen(scrollState) }
                    composable(Routes.TRANSACTIONS) { TransactionsScreen(listState = listState) }
                    composable(Routes.ACCOUNTS) { AccountsScreen(listState = listState) }
                    composable(Routes.INSIGHTS) { InsightsScreen(scrollState = scrollState) }
                    composable(Routes.PROFILE) { ProfileScreen(listState = listState) }
                }
            }

            BottomNavigationBar()
        }
    }
}

@Composable
fun TopNavigationBarFixed(
    onRouteChange: (String) -> Unit,
    onBackgroundChange: (Color) -> Unit
) {
    // Create a separate nav controller for tracking route changes
    var currentRoute by remember { mutableStateOf(Routes.HOME) }
    var currentTopBarEvent by remember { mutableStateOf<TopBarEvent?>(null) }

    // Listen to route changes from the NavigationHost
    LaunchedEffect(currentRoute) {
        onRouteChange(currentRoute)
    }

    val screenConfig = when (currentRoute) {
        Routes.HOME -> ScreenConfiguration(
            theme = ScreenTheme.TRANSPARENT,
            hasBackButton = false,
            title = "KD Banking",
            showRightIcon = true,
            rightIcon = "♥"
        )

        Routes.TRANSACTIONS -> ScreenConfiguration(
            theme = ScreenTheme.WHITE,
            hasBackButton = true,
            title = "Transactions"
        )

        Routes.ACCOUNTS -> ScreenConfiguration(
            theme = ScreenTheme.WHITE,
            hasBackButton = true,
            title = "Accounts"
        )

        Routes.INSIGHTS -> ScreenConfiguration(
            theme = ScreenTheme.TRANSPARENT,
            hasBackButton = true,
            title = "Insights"
        )

        Routes.PROFILE -> ScreenConfiguration(
            theme = ScreenTheme.LIGHT_GRAY,
            hasBackButton = true,
            title = "Profile",
            showRightIcon = true,
            rightIcon = "🔍"
        )

        else -> ScreenConfiguration()
    }

    val containerBackground = when (screenConfig.theme) {
        ScreenTheme.TRANSPARENT -> Color.Transparent
        ScreenTheme.WHITE -> Color.White
        ScreenTheme.LIGHT_GRAY -> Color(0xFFF8F9FA)
        ScreenTheme.DARK -> Color(0xFF1a1a1a)
    }

    LaunchedEffect(containerBackground) {
        onBackgroundChange(containerBackground)
    }

    val topBarState = TopBarState(
        currentEvent = currentTopBarEvent,
        onEventHandled = { currentTopBarEvent = null },
        onTriggerEvent = { event -> currentTopBarEvent = event }
    )

    CompositionLocalProvider(LocalTopBarState provides topBarState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (screenConfig.hasBackButton) {
                    IconButton(
                        onClick = {
                            // Handle back navigation by updating current route
                            currentRoute = Routes.HOME
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable { topBarState.onTriggerEvent(TopBarEvent.TitleClicked) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🇰🇼", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        screenConfig.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                if (screenConfig.showRightIcon) {
                    Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                        IconButton(
                            onClick = { topBarState.onTriggerEvent(TopBarEvent.RightIconClicked) }
                        ) {
                            Text(
                                screenConfig.rightIcon,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (screenConfig.rightIcon == "♥") Color.Red else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar() {
    val navigationState = LocalNavigationState.current
    val config = navigationState.screenConfig

    NavigationBar(
        containerColor = Color.White,
        contentColor = Color.Black
    ) {
        NavigationBarItem(
            icon = { Text("🏠") },
            label = { Text("Home", color = Color.Black) },
            selected = config.title == "KD Banking" && !config.hasBackButton,
            onClick = { navigationState.onNavigate(NavigationEvent.Home) }
        )
        NavigationBarItem(
            icon = { Text("📊") },
            label = { Text("Activity", color = Color.Black) },
            selected = config.title == "Transactions",
            onClick = { navigationState.onNavigate(NavigationEvent.Transactions) }
        )
        NavigationBarItem(
            icon = { Text("💳") },
            label = { Text("Accounts", color = Color.Black) },
            selected = config.title == "Accounts",
            onClick = { navigationState.onNavigate(NavigationEvent.Accounts) }
        )
        NavigationBarItem(
            icon = { Text("🔍") },
            label = { Text("Insights", color = Color.Black) },
            selected = config.title == "Insights",
            onClick = { navigationState.onNavigate(NavigationEvent.Insights) }
        )
        NavigationBarItem(
            icon = { Text("⋯") },
            label = { Text("Profile", color = Color.Black) },
            selected = config.title == "Profile",
            onClick = { navigationState.onNavigate(NavigationEvent.Profile) }
        )
    }
}

@Composable
fun FullScreenTopBar(title: String, showBackToHome: Boolean = false) {
    val appState = LocalAppState.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (showBackToHome) {
                IconButton(
                    onClick = {
                        scope.launch {
                            appState.pagerState.animateScrollToPage(PageIndex.HOME)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = Color.Black
                    )
                }
            }

            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🇰🇼", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

// File 7: CommonComponents.kt - Shared UI Components

@Composable
fun Msa3edScreen(showCloseButton: Boolean, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C0032),
                        Color(0xFFE6004C),
                        Color(0xFF2D0B55),
                        Color(0xFF2D0B55),
                        Color(0xFFE6004C)
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Hey Mohammed",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "How can I help?",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFDB2777)
            )
            Spacer(modifier = Modifier.height(48.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ActionButton("See my transactions")
                ActionButton("View spending insights")
            }
        }

        if (showCloseButton) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

@Composable
fun ActionButton(text: String) {
    Button(
        onClick = { },
        modifier = Modifier.size(width = 280.dp, height = 56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(text, color = Color.White)
    }
}

@Composable
fun TransparentTwoPartLayout(
    scrollState: ScrollState,
    topContent: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                topContent()
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                bottomContent()
            }
        }
    }
}

@Composable
fun RegularTransactionCard(transaction: TransactionData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        when (transaction.type) {
                            "income" -> "💰"
                            "food" -> "🍔"
                            "transport" -> "🚗"
                            "shopping" -> "🛍"
                            "utilities" -> "⚡"
                            "entertainment" -> "🎬"
                            "investment" -> "📈"
                            "transfer" -> "↗"
                            "refund" -> "↩"
                            else -> "💳"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        transaction.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        transaction.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            if (transaction.amount.isNotEmpty()) {
                Text(
                    if (transaction.amount.startsWith("+")) "${transaction.amount.drop(1)}" else "-${
                        transaction.amount.drop(
                            1
                        )
                    }",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (transaction.amount.startsWith("+")) Color.Green else Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// File 8: Screens.kt - Individual Screen Implementations

@Composable
fun HomeScreen(scrollState: ScrollState) {
    val navigationState = LocalNavigationState.current
    val topBarState = LocalTopBarState.current

    // Handle navigation events
    LaunchedEffect(navigationState.currentEvent) {
        navigationState.currentEvent?.let { event ->
            println("HomeScreen received navigation event: ${event::class.simpleName}")
            navigationState.onEventHandled()
        }
    }

    // Handle top bar events with clean extensions
    topBarState.onRightIconClick {
        println("Heart icon clicked in HomeScreen!")
        // Handle heart functionality - maybe favorites, liked transactions, etc.
    }

    topBarState.onTitleClick {
        println("Home title clicked! Scrolling to top...")
        // Scroll to top functionality could go here if needed
    }

    TransparentTwoPartLayout(
        scrollState = scrollState,
        topContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Total Balance",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "45,482.72",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("KWD", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)

                // Duplicate content for scrolling demo
                repeat(3) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Account Overview",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Text(
                        "Available Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            }
        },
        bottomContent = {
            Column {
                Text(
                    "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                repeat(15) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Transaction ${it + 1}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Today ${it + 8}:${(it * 15) % 60} AM",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Text(
                            "${if (it % 3 == 0) "+" else "-"}${(it + 1) * 50}.00",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (it % 3 == 0) Color.Green else Color.Red
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun InsightsScreen(scrollState: ScrollState) {
    val navigationState = LocalNavigationState.current
    val topBarState = LocalTopBarState.current

    // Handle navigation events
    LaunchedEffect(navigationState.currentEvent) {
        navigationState.currentEvent?.let { event ->
            println("InsightsScreen received navigation event: ${event::class.simpleName}")
            navigationState.onEventHandled()
        }
    }

    // Handle top bar events with clean extensions
    topBarState.onTitleClick {
        println("Insights title clicked! Refreshing data...")
        // Handle refresh or scroll to top
    }

    TransparentTwoPartLayout(
        scrollState = scrollState,
        topContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Financial Health",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Excellent",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Green
                )
                Spacer(modifier = Modifier.height(16.dp))

                repeat(3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Metric ${it + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            "${(it + 1) * 25}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        bottomContent = {
            Column {
                Text(
                    "Spending Analysis",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                repeat(15) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Category ${it + 1}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "This month",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Text(
                            "${(it + 1) * 150}.00 KWD",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(listState: LazyListState) {
    val navigationState = LocalNavigationState.current
    val topBarState = LocalTopBarState.current
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Handle navigation events
    LaunchedEffect(navigationState.currentEvent) {
        navigationState.currentEvent?.let { event ->
            println("TransactionsScreen received navigation event: ${event::class.simpleName}")
            navigationState.onEventHandled()
        }
    }

    // Handle top bar events
    topBarState.onTitleClick {
        println("Transactions title clicked! Scrolling to top...")
        scope.launch { listState.animateScrollToItem(0) }
    }

    if (isRefreshing) {
        LaunchedEffect(Unit) {
            delay(2000)
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { isRefreshing = true }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "All Transactions",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Complete transaction history",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            items(sampleTransactions) { transaction ->
                RegularTransactionCard(transaction)
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun AccountsScreen(listState: LazyListState) {
    val navigationState = LocalNavigationState.current
    val topBarState = LocalTopBarState.current
    val scope = rememberCoroutineScope()

    // Handle navigation events
    LaunchedEffect(navigationState.currentEvent) {
        navigationState.currentEvent?.let { event ->
            println("AccountsScreen received navigation event: ${event::class.simpleName}")
            navigationState.onEventHandled()
        }
    }

    // Handle top bar events
    topBarState.onTitleClick {
        println("Accounts title clicked! Scrolling to top...")
        scope.launch { listState.animateScrollToItem(0) }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { AccountsHeader() }
        item { AccountsSummaryCard() }
        item {
            Text(
                "My Accounts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        items(sampleAccounts) { account -> EnhancedAccountCard(account) }
        item { AddAccountCard() }
        item { AccountServicesSection() }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun ProfileScreen(listState: LazyListState) {
    val navigationState = LocalNavigationState.current
    val topBarState = LocalTopBarState.current
    val scope = rememberCoroutineScope()

    // Handle navigation events
    LaunchedEffect(navigationState.currentEvent) {
        navigationState.currentEvent?.let { event ->
            println("ProfileScreen received navigation event: ${event::class.simpleName}")
            navigationState.onEventHandled()
        }
    }

    // Handle top bar events with clean extensions
    topBarState.onRightIconClick {
        println("Search clicked in ProfileScreen!")
        // Show search functionality, filter users, search settings, etc.
    }

    topBarState.onTitleClick {
        println("Profile title clicked! Scrolling to top...")
        scope.launch { listState.animateScrollToItem(0) }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ProfileHeader() }
        item { UserProfileCard() }
        item { QuickActionsRow() }
        item {
            Text(
                "Settings & Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        items(getProfileMenuItems()) { menuItem -> ProfileMenuCard(menuItem) }
        item { SecuritySection() }
        item { SupportSection() }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

// Component Definitions
@Composable
fun AccountsHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "My Accounts",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Manage all your banking accounts",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AccountsSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6B46C1)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Total Balance",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "55,389.64",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "KWD",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem("Assets", "63,624.20", true)
                SummaryItem("Liabilities", "8,234.56", false)
                SummaryItem("Net Worth", "55,389.64", true)
            }
        }
    }
}

@Composable
fun SummaryItem(title: String, amount: String, isPositive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            amount,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EnhancedAccountCard(account: AccountData) {
    val cardColors = listOf(
        Color(0xFF4F46E5), Color(0xFF059669), Color(0xFFDC2626),
        Color(0xFF7C2D12), Color(0xFF6B46C1), Color(0xFFEA580C)
    )
    val cardColor = cardColors.random()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            account.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            account.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "Available Balance",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                if (account.balance.startsWith("-"))
                                    "-${account.balance.drop(1)}"
                                else account.balance,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "KWD",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            when {
                                account.name.contains("Current") -> "CHECKING"
                                account.name.contains("Savings") -> "SAVINGS"
                                account.name.contains("Investment") -> "INVESTMENT"
                                account.name.contains("Credit") -> "CREDIT"
                                account.name.contains("Business") -> "BUSINESS"
                                else -> "ACCOUNT"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddAccountCard() {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF6B46C1).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add account",
                    tint = Color(0xFF6B46C1),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Open New Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                "Start your financial journey with us",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AccountServicesSection() {
    Column {
        Text(
            "Account Services",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(listOf("Transfer Funds", "Pay Bills", "Mobile Deposit", "Statements")) { service ->
                ServiceCard(service)
            }
        }
    }
}

@Composable
fun ServiceCard(service: String) {
    Card(
        modifier = Modifier.width(140.dp).clickable { },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                when (service) {
                    "Transfer Funds" -> "💸"
                    "Pay Bills" -> "🧾"
                    "Mobile Deposit" -> "📱"
                    "Statements" -> "📄"
                    else -> "⚙"
                },
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                service,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProfileHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "My Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Manage your account settings",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun UserProfileCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF6B46C1).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "M",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color(0xFF6B46C1),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Mohammed Al-Rashid",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Premium Customer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B46C1),
                    fontWeight = FontWeight.Medium
                )

                Text(
                    "Member since 2019",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            IconButton(onClick = { }) {
                Text("✏", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun QuickActionsRow() {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(
            listOf(
                Triple("Edit Profile", "👤", Color(0xFF6B46C1)),
                Triple("Security", "🔒", Color(0xFF059669)),
                Triple("Notification", "🔔", Color(0xFFEA580C)),
                Triple("Help", "❓", Color(0xFFDC2626))
            )
        ) { (title, icon, color) ->
            QuickActionCard(title, icon, color)
        }
    }
}

@Composable
fun QuickActionCard(title: String, icon: String, color: Color) {
    Card(
        modifier = Modifier.width(100.dp).clickable { },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProfileMenuCard(menuItem: ProfileMenuItem) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(menuItem.color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(menuItem.icon, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        menuItem.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    if (menuItem.subtitle.isNotEmpty()) {
                        Text(
                            menuItem.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Navigate",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SecuritySection() {
    Column {
        Text(
            "Security & Privacy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF059669).copy(alpha = 0.05f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF059669).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✓",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF059669)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Account Secure",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        "All security features are active",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun SupportSection() {
    Column {
        Text(
            "Support & Help",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B46C1)
                )
            ) {
                Text("Contact Support")
            }

            OutlinedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Text("FAQ", color = Color(0xFF6B46C1))
            }
        }
    }
}