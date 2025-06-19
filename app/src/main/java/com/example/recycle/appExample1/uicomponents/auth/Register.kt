package com.example.recycle.appExample1.uicomponents.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recycle.appExample1.uicomponents.home.createEmptyStats
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun Register(
    onRegisterSuccess: () -> Unit = {},
    onGoogleRegister: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val auth = Firebase.auth
    val context = LocalContext.current as Activity

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("553782313749-bhcrrq62v8anmfst5mjlda05ntaac07r.apps.googleusercontent.com")
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val emailFromGoogle = account.email
            val idToken = account.idToken

            if (emailFromGoogle != null && idToken != null) {
                auth.fetchSignInMethodsForEmail(emailFromGoogle)
                    .addOnCompleteListener { fetchTask ->
                        if (fetchTask.isSuccessful) {
                            val signInMethods = fetchTask.result.signInMethods
                            if (!signInMethods.isNullOrEmpty()) {
                                errorMessage = "이미 등록된 구글 계정입니다."
                            } else {
                                val credential = GoogleAuthProvider.getCredential(idToken, null)
                                auth.signInWithCredential(credential)
                                    .addOnCompleteListener { authResult ->
                                        if (authResult.isSuccessful) {
                                            val uid = Firebase.auth.currentUser?.uid
                                            if (uid != null) {
                                                createEmptyStats(uid)
                                            }
                                            onRegisterSuccess()
                                        } else {
                                            errorMessage = "구글 회원가입 실패"
                                        }

                                    }
                            }
                        } else {
                            errorMessage = "계정 확인 중 오류"
                        }
                    }
            } else {
                errorMessage = "구글 계정 정보를 가져올 수 없습니다."
            }
        } catch (e: ApiException) {
            errorMessage = "계정 인증 오류 : ${e.statusCode} - ${e.message}"
            Log.e("계정 인증 오류", "${e.statusCode} - ${e.message}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "회원가입",
            fontSize = 32.sp,
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(24.dp))

        GoogleButton(
            name = "구글 계정으로 회원가입",
            onClick = {
                googleSignInClient.signOut().addOnCompleteListener {
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "또는 이메일로 회원가입",
            fontSize = 14.sp,
            color = Color.Gray,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("PW") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "이메일과 비밀번호를 모두 입력해주세요."
                    return@Button
                }

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = Firebase.auth.currentUser?.uid
                            if (uid != null) {
                                createEmptyStats(uid)
                            }
                            onRegisterSuccess()
                        } else {
                            val message = when (task.exception?.message) {
                                "The email address is already in use by another account." ->
                                    "이미 등록한 계정입니다. 다른 계정을 사용해주세요."
                                "The given password is invalid. [ Password should be at least 6 characters ]" ->
                                    "비밀번호는 6자 이상이어야 합니다."
                                "The email address is badly formatted." ->
                                    "올바른 이메일 형식을 입력해주세요."
                                else -> "회원가입 실패: ${task.exception?.message}"
                            }
                            errorMessage = message
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6A4FB6),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(50.dp)
        ) {
            Text(text = "회원가입")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = Color.Red)
        }
    }
}

@Preview
@Composable
fun RegisterPreview() {
    Register(
        onRegisterSuccess = {},
        onGoogleRegister = {}
    )
}