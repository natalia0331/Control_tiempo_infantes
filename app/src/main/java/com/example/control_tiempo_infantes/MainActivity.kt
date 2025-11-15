package com.example.control_tiempo_infantes

import android.os.Bundle
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
import com.example.control_tiempo_infantes.features.auth.RegisterScreen
import com.example.control_tiempo_infantes.features.circles.CirclesScreen
import com.example.control_tiempo_infantes.features.circles.CirclesViewModel
import com.example.control_tiempo_infantes.features.home.HomeScreen
import com.example.control_tiempo_infantes.ui.theme.Control_tiempo_infantesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Control_tiempo_infantesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val nav = rememberNavController()

                    // ViewModel de autenticación
                    val authVm: AuthViewModel = hiltViewModel()
                    val isLoggedIn by authVm.isLoggedIn.collectAsState(initial = false)

                    NavHost(
                        navController = nav,
                        startDestination = if (isLoggedIn) "home" else "auth",
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        // Pantalla de login
                        composable("auth") {
                            AuthScreen(
                                onLoggedIn = {
                                    nav.navigate("home") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                },
                                onGoToRegister = { nav.navigate("register") },
                                vm = authVm
                            )
                        }

                        // Pantalla de registro
                        composable("register") {
                            RegisterScreen(
                                onRegistered = {
                                    // Después de registrarse, lo mandas a home
                                    nav.navigate("home") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                },
                                onBack = { nav.popBackStack() }, // volver a login
                                vm = authVm
                            )
                        }

                        // Pantalla principal (home)
                        composable("home") {
                            HomeScreen(
                                onOpenCircles = { nav.navigate("circles") },
                                onLogout = { authVm.logout() }
                            )
                        }

                        // Pantalla de círculos familiares
                        composable("circles") {
                            val circlesVm: CirclesViewModel = hiltViewModel()
                            CirclesScreen(
                                vm = circlesVm,
                                onBack = { nav.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
