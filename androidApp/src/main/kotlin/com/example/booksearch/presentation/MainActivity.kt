package com.example.booksearch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.booksearch.presentation.search.BookSearchRoute
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
