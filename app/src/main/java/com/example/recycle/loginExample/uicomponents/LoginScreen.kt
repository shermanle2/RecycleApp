package com.example.recycle.loginExample.uicomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onWelcomeNavigate: (String) -> Unit,
    onRegisterNavigate: (String, String) -> Unit,
) {
    val userID = "greenjoa"
    val userPasswd = "1234"

    var userIdState by remember { mutableStateOf("") }
    var userPasswdState by remember { mutableStateOf("") }
    val loginResult by remember {
        derivedStateOf { userID == userIdState && userPasswd == userPasswdState }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login Screen",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold
        )

        OutlinedTextField(
            value = userIdState,
            onValueChange = { userIdState = it }
        )

        OutlinedTextField(
            value = userPasswdState,
            onValueChange = { userPasswdState = it },
            label = { Text("Enter Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Button(
            onClick = {
                if (loginResult) {
                    onWelcomeNavigate(userIdState)
                } else {
                    onRegisterNavigate(userIdState, userPasswdState)
                }
            }
        ) {
            Text(text = "Login")
        }
    }
}