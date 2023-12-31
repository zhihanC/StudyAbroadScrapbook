package hu.ait.studyabroadscrapbook.ui.screen.login

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hu.ait.studyabroadscrapbook.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("zhihanc@ait.edu") }
    var password by rememberSaveable { mutableStateOf("zhihanc") }

    val coroutineScope = rememberCoroutineScope()

    Box() {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text(
                text = stringResource(R.string.study_abroad_scrapbook),
                modifier = Modifier
                    .padding(top = 50.dp),
                fontSize = 30.sp
            )

            Text(
                text = stringResource(R.string.ait_budapest_fall_2023),
                modifier = Modifier
                    .padding(top = 20.dp),
                fontSize = 30.sp
            )
        }


        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(0.8f),
                label = {
                    Text(text = stringResource(R.string.e_mail))
                },
                value = email,
                onValueChange = {
                    email = it
                },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Email, null)
                }
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(0.8f),
                label = {
                    Text(text = stringResource(R.string.password))
                },
                value = password,
                onValueChange = { password = it },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(Icons.Default.Password, null)
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        if (showPassword) {
                            Icon(Icons.Default.Visibility, null)
                        } else {
                            Icon(Icons.Default.VisibilityOff, null)
                        }
                    }
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        val result = loginViewModel.loginUser(email, password)
                        if (result?.user != null) {
                            withContext(Dispatchers.Main) {
                                onLoginSuccess()
                            }
                        }
                    }
                }) {
                    Text(text = stringResource(R.string.login))
                }
                OutlinedButton(onClick = {
                    loginViewModel.registerUser(email, password)
                }) {
                    Text(text = stringResource(R.string.register))
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (loginViewModel.loginUiState) {
                is LoginUiState.Loading -> CircularProgressIndicator()
                is LoginUiState.RegisterSuccess -> Text(text = stringResource(R.string.registration_ok))
                is LoginUiState.Error -> Text(
                    text = stringResource(R.string.error) + "${
                        (loginViewModel.loginUiState as LoginUiState.Error).error
                    }",
                    modifier = Modifier
                        .padding(16.dp)
                )
                is LoginUiState.LoginSuccess -> {}
                LoginUiState.Init -> {}
            }
        }
    }

}