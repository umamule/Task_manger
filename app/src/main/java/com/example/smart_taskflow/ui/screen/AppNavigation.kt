package com.example.smart_taskflow.ui.screen

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.smart_taskflow.viewmodel.TaskViewModel
import com.example.smart_taskflow.viewmodel.AuthViewModel
import com.example.smart_taskflow.R

// ---------------------------
//       NAVIGATION
// ---------------------------
@Composable
fun AppNavigation(
    taskViewModel: TaskViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "splash") {

        composable("splash") { SplashScreen(navController) }

        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }

        composable("register") {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }

        composable("dashboard") {
            DashboardScreen(viewModel = taskViewModel, navController = navController)
        }

        composable("home/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "all"
            HomeScreen(
                viewModel = taskViewModel,
                navController = navController,
                category = category
            )
        }
    }
}

// ---------------------------
//         SPLASH
// ---------------------------
@Composable
fun SplashScreen(navController: NavController) {
    var alphaAnim by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        androidx.compose.animation.core.animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(1200)
        ) { value, _ -> alphaAnim = value }

        kotlinx.coroutines.delay(1500)

        // No Firebase → always go to login
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6200EE)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(
                Icons.Default.DateRange,
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TaskFlow",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

// ---------------------------
//         LOGIN (ROOM)
// ---------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.taskflow_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {

            Card(
                modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(24.dp)
            ) {

                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "TaskFlow",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5)
                    )

                    Spacer(Modifier.height(32.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(28.dp))

                    Button(
                        onClick = {
                            authViewModel.login(email, password) { success ->
                                if (success) {
                                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(context, "Invalid credentials!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        ,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(Color(0xFF1E88E5))
                    ) {
                        Text("Log in", color = Color.White)
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(onClick = { navController.navigate("register") }) {
                        Text("Don't have an account yet? Sign up here", color = Color.Gray)
                    }
                }
            }
        }
    }
}

// ---------------------------
//      REGISTER (ROOM)
// ---------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel) {

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.taskflow_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {

            Card(
                modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(24.dp)
            ) {

                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "Sign up",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5)
                    )

                    Spacer(Modifier.height(32.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(28.dp))

                    Button(
                        onClick = {
                            authViewModel.register(fullName, email, password) { success ->
                                if (success) {
                                    Toast.makeText(context, "Registered Successfully!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("dashboard") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(context, "Email already exists!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(Color(0xFF1E88E5))
                    ) {
                        Text("Register", color = Color.White)
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(onClick = { navController.navigate("login") }) {
                        Text("Already have an account? Log in here.", color = Color.Gray)
                    }
                }
            }
        }
    }
}

// ---------------------------
//     DASHBOARD (NO FIREBASE)
// ---------------------------
@Composable
fun DashboardScreen(viewModel: TaskViewModel = viewModel(), navController: NavController) {

    val username = "User" // No Firebase → static or from Room if needed

    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    val totalTasks = tasks.count { !it.isDone }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.taskflow_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "User Icon",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    "Hello, $username 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "You have $totalTasks Tasks",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Spacer(Modifier.height(24.dp))

            val categories = listOf(
                "All Tasks",
                "Home",
                "Work",
                "Studies",
                "Bills",
                "Shopping",
                "Sport",
                "Health",
                "Personal Projects",
                "Transportation",
                "Other"
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(categories) { category ->

                    val count = tasks.count {
                        !it.isDone && (it.assignCategory() == category || category == "All Tasks")
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val route = if (category == "All Tasks") "all" else category
                                navController.navigate("home/$route")
                            },
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.9f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(category, fontWeight = FontWeight.SemiBold)
                            Text("$count", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Logout", color = Color.White)
            }
        }
    }
}
