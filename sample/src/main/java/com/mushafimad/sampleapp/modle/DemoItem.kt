package com.mushafimad.sampleapp.modle

import androidx.compose.ui.graphics.vector.ImageVector

data class DemoItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)