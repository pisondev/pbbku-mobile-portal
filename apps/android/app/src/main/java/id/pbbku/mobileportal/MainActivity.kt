package id.pbbku.mobileportal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import id.pbbku.mobileportal.ui.component.PatternBackground
import id.pbbku.mobileportal.ui.navigation.PbbKuNavGraph
import id.pbbku.mobileportal.ui.theme.PBBKuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PBBKuTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    PatternBackground()
                    PbbKuNavGraph()
                }
            }
        }
    }
}
