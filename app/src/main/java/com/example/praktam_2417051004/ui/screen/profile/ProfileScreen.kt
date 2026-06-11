package com.example.praktam_2417051004.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.praktam_2417051004.ui.components.CustomTopAppBar
import com.example.praktam_2417051004.ui.viewmodel.AuthViewModel
import com.example.praktam_2417051004.ui.viewmodel.FoodViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    foodViewModel: FoodViewModel
) {
    val user = authViewModel.currentUser
    var isEditing by remember { mutableStateOf(false) }

    var editedName by remember(user) { mutableStateOf(user?.name ?: "") }
    var editedEmail by remember(user) { mutableStateOf(user?.email ?: "") }
    var editedUsername by remember(user) { mutableStateOf(user?.username ?: "") }
    var editedPassword by remember { mutableStateOf("") }
    
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Profil Saya", 
                onBack = { if (isEditing) isEditing = false else navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (!isEditing) {
                Text(text = user?.name ?: "Guest", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileItemRow(label = "Username", value = user?.username ?: "-")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                        ProfileItemRow(label = "Email", value = user?.email ?: "-")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { 
                        isEditing = true 
                        editedPassword = authViewModel.getPassword()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profil")
                }
            } else {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = editedEmail,
                    onValueChange = { editedEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = editedUsername,
                    onValueChange = { editedUsername = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = editedPassword,
                    onValueChange = { editedPassword = it },
                    label = { Text("Password Baru") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                if (showError) {
                    Text(errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (editedName.isBlank() || editedEmail.isBlank() || editedUsername.isBlank() || editedPassword.isBlank()) {
                            errorMessage = "Data tidak boleh kosong"
                            showError = true
                        } else {
                            val oldUsername = user?.username ?: ""
                            if (authViewModel.updateProfile(editedName, editedEmail, editedUsername, editedPassword)) {
                                if (oldUsername != editedUsername) {
                                    foodViewModel.migrateUserData(oldUsername, editedUsername)
                                }
                                isEditing = false
                                showError = false
                            } else {
                                errorMessage = "Username sudah digunakan"
                                showError = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Check, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Perubahan")
                }
                
                TextButton(onClick = { isEditing = false }) {
                    Text("Batal")
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (!isEditing) {
                Button(
                    onClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
fun ProfileItemRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
