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
import androidx.compose.runtime.LaunchedEffect


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

                        // Selección de rol inicial
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

                        // Pantalla de entrada de infante (elige login o registro)
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

                        // Login (adulto / infante)
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

                        // Registro supervisor (adulto)
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

                        // Registro infante
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

                        // Unirse por código (opcional)
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
                            val profile by authVm.profile.collectAsState()
                            val currentUser = FirebaseAuth.getInstance().currentUser

                            LaunchedEffect(currentUser?.uid) {
                                authVm.ensureProfileLoaded()
                            }

                            val fallbackDisplayName =
                                currentUser?.displayName
                                    ?: currentUser?.email
                                    ?: "Usuario"

                            val displayName =
                                profile?.displayName?.takeIf { !it.isNullOrBlank() }
                                    ?: fallbackDisplayName

                            val isChild = profile?.role?.equals("CHILD", ignoreCase = true) == true

                            HomeScreen(
                                displayName = displayName,
                                isChild = isChild,
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
                                },
                                onOpenInvitations = {
                                    nav.navigate("childInvitations")
                                }
                            )
                        }


                        // Detalle de círculo
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

                        // Registrar infante en círculo
                        composable("addChild/{circleId}") { entry ->
                            val circleId = entry.arguments?.getString("circleId") ?: return@composable
                            val vm: CircleDetailViewModel = hiltViewModel()
                            AddChildScreen(
                                vm = vm,
                                circleId = circleId,
                                onBack = { nav.popBackStack() }
                            )
                        }

                        // Invitar miembro (supervisor)
                        composable("inviteMember/{circleId}") { entry ->
                            val circleId = entry.arguments?.getString("circleId") ?: return@composable
                            val vm: InviteViewModel = hiltViewModel()
                            InviteMemberScreen(
                                vm = vm,
                                circleId = circleId,
                                onBack = { nav.popBackStack() }
                            )
                        }

                        // Invitaciones del infante
                        composable("childInvitations") {
                            val vm: ChildInvitationsViewModel = hiltViewModel()
                            ChildInvitationsScreen(
                                vm = vm,
                                onBack = { nav.popBackStack() }
                            )
                        }

                        // Vincular dispositivo del infante
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
