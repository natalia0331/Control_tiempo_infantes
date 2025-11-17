package com.example.control_tiempo_infantes

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.control_tiempo_infantes.features.auth.AuthScreen
import com.example.control_tiempo_infantes.features.auth.AuthViewModel
import com.example.control_tiempo_infantes.features.auth.ChildEntryScreen
import com.example.control_tiempo_infantes.features.auth.ChildRegisterScreen
import com.example.control_tiempo_infantes.features.auth.JoinByCodeScreen
import com.example.control_tiempo_infantes.features.auth.JoinByCodeViewModel
import com.example.control_tiempo_infantes.features.auth.RegisterScreen
import com.example.control_tiempo_infantes.features.auth.RoleSelectionScreen
import com.example.control_tiempo_infantes.features.circles.AddChildScreen
import com.example.control_tiempo_infantes.features.circles.ChildInvitationsScreen
import com.example.control_tiempo_infantes.features.circles.ChildInvitationsViewModel
import com.example.control_tiempo_infantes.features.circles.CircleDetailScreen
import com.example.control_tiempo_infantes.features.circles.CircleDetailViewModel
import com.example.control_tiempo_infantes.features.circles.CirclesScreen
import com.example.control_tiempo_infantes.features.circles.CirclesViewModel
import com.example.control_tiempo_infantes.features.circles.InviteMemberScreen
import com.example.control_tiempo_infantes.features.circles.InviteViewModel
import com.example.control_tiempo_infantes.features.home.HomeScreen
import com.example.control_tiempo_infantes.features.link.LinkDeviceScreen
import com.example.control_tiempo_infantes.features.link.LinkDeviceViewModel
import com.example.control_tiempo_infantes.ui.theme.Control_tiempo_infantesTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ID único del dispositivo (para vincularlo al infante)
        val deviceId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown-device"

        setContent {
            Control_tiempo_infantesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val nav = rememberNavController()

                    val authVm: AuthViewModel = hiltViewModel()
                    val isLoggedIn by authVm.isLoggedIn.collectAsState(initial = false)

                    NavHost(
                        navController = nav,
                        startDestination = if (isLoggedIn) "home" else "roleSelect",
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        composable("roleSelect") {
                            RoleSelectionScreen(
                                onSelectSupervisor = {
                                    nav.navigate("auth") {
                                        popUpTo("roleSelect") { inclusive = false }
                                    }
                                },
                                onSelectChild = {
                                    nav.navigate("childEntry") {
                                        popUpTo("roleSelect") { inclusive = false }
                                    }
                                }
                            )
                        }


                        composable("childEntry") {
                            ChildEntryScreen(
                                onLogin = {
                                    nav.navigate("auth")
                                },
                                onRegister = {
                                    nav.navigate("childRegister")
                                },
                                onBack = { nav.popBackStack() }
                            )
                        }


                        composable("auth") {
                            AuthScreen(
                                onLoggedIn = {
                                    nav.navigate("home") {
                                        popUpTo("roleSelect") { inclusive = true }
                                    }
                                },
                                onGoToRegister = { nav.navigate("register") },
                                vm = authVm
                            )
                        }


                        composable("register") {
                            RegisterScreen(
                                onRegistered = {
                                    nav.navigate("home") {
                                        popUpTo("roleSelect") { inclusive = true }
                                    }
                                },
                                onBack = { nav.popBackStack() },
                                vm = authVm
                            )
                        }


                        composable("childRegister") {
                            ChildRegisterScreen(
                                vm = authVm,
                                onRegistered = {
                                    nav.navigate("home") {
                                        popUpTo("roleSelect") { inclusive = true }
                                    }
                                },
                                onBack = { nav.popBackStack() }
                            )
                        }


                        composable("joinByCode") {
                            val vm: JoinByCodeViewModel = hiltViewModel()
                            JoinByCodeScreen(
                                vm = vm,
                                onBack = { nav.popBackStack() },
                                onJoined = {
                                    nav.navigate("home") {
                                        popUpTo("roleSelect") { inclusive = true }
                                    }
                                }
                            )
                        }


                        composable("home") {
                            // Obtener displayName del usuario actual para pasarlo a HomeScreen
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            val displayName =
                                currentUser?.displayName
                                    ?: currentUser?.email
                                    ?: "Usuario"

                            HomeScreen(
                                displayName = displayName,
                                onOpenCircles = { nav.navigate("circles") },
                                onOpenInvitations = { nav.navigate("childInvitations") },
                                onLogout = {
                                    authVm.logout()
                                    nav.navigate("roleSelect") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("circles") {
                            val circlesVm: CirclesViewModel = hiltViewModel()
                            CirclesScreen(
                                vm = circlesVm,
                                onBack = { nav.popBackStack() },
                                onOpenCircle = { circleId ->
                                    nav.navigate("circleDetail/$circleId")
                                }
                            )
                        }


                        composable("circleDetail/{circleId}") { entry ->
                            val circleId = entry.arguments?.getString("circleId") ?: return@composable
                            val vm: CircleDetailViewModel = hiltViewModel()
                            CircleDetailScreen(
                                vm = vm,
                                circleId = circleId,
                                onBack = { nav.popBackStack() },
                                onAddChild = {
                                    nav.navigate("addChild/$circleId")
                                },
                                onInviteMember = {
                                    nav.navigate("inviteMember/$circleId")
                                },
                                onOpenLinkDevice = { childId ->
                                    nav.navigate("linkDevice/$childId")
                                }
                            )
                        }


                        composable("addChild/{circleId}") { entry ->
                            val circleId = entry.arguments?.getString("circleId") ?: return@composable
                            val vm: CircleDetailViewModel = hiltViewModel()
                            AddChildScreen(
                                vm = vm,
                                circleId = circleId,
                                onBack = { nav.popBackStack() }
                            )
                        }


                        composable("inviteMember/{circleId}") { entry ->
                            val circleId = entry.arguments?.getString("circleId") ?: return@composable
                            val vm: InviteViewModel = hiltViewModel()
                            InviteMemberScreen(
                                vm = vm,
                                circleId = circleId,
                                onBack = { nav.popBackStack() }
                            )
                        }


                        composable("childInvitations") {
                            val vm: ChildInvitationsViewModel = hiltViewModel()
                            ChildInvitationsScreen(
                                vm = vm,
                                onBack = { nav.popBackStack() }
                            )
                        }


                        composable("linkDevice/{childId}") { entry ->
                            val childId = entry.arguments?.getString("childId") ?: return@composable
                            val vm: LinkDeviceViewModel = hiltViewModel()
                            LinkDeviceScreen(
                                vm = vm,
                                childId = childId,
                                deviceId = deviceId,
                                onBack = { nav.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
