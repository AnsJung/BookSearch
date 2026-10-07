package com.example.booksearch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.booksearch.presentation.search.BookSearchRoute
import com.example.booksearch.presentation.search.BookSearchScreen
import com.example.booksearch.presentation.theme.BookSearchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            BookSearchTheme {
                BookSearchRoute()
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    BookSearchTheme {
        BookSearchScreen()
    }
}
