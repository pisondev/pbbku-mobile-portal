package id.pbbku.mobileportal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import id.pbbku.mobileportal.ui.navigation.PbbKuNavGraph
import id.pbbku.mobileportal.ui.theme.PBBKuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PBBKuTheme {
                PbbKuNavGraph()
            }
        }
    }
}
