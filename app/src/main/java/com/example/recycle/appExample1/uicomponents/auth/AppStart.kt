package com.example.recycle.appExample1.uicomponents.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.example.recycle.R

@Composable
fun AppStart(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onGuest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val buttonWidth = 150.dp

        val primayButtonColor = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6A4FB6),
            contentColor = Color.White
        )

        val secondaryButtonColor = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF888888),
            contentColor = Color.White
        )

        Image(
            painter = painterResource(id = R.drawable.baseline_photo_24),
            contentDescription = "앱 로고",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 32.dp)
        )

        Button(
            onClick = onLogin,
            modifier = Modifier
                .width(buttonWidth)
                .height(48.dp),
            colors = primayButtonColor
        ) {
            Text(text = "로그인")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onRegister,
            modifier = Modifier
                .width(buttonWidth)
                .height(48.dp),
            colors = primayButtonColor
        ) {
            Text(text = "회원가입")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onGuest,
            modifier = Modifier
                .width(buttonWidth)
                .height(48.dp),
            colors = secondaryButtonColor
        ) {
            Text(text = "비회원으로 시작")
        }
    }
}