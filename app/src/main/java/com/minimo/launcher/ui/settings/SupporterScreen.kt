package com.minimo.launcher.ui.settings

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.AppButton
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.AppProduct
import com.minimo.launcher.utils.isInstalledFromPlayStore
import com.minimo.launcher.utils.openDeveloperPlayStorePage
import com.minimo.launcher.utils.openKoFiPage
import com.minimo.launcher.utils.openPlayStorePage
import com.minimo.launcher.utils.showAppReview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupporterScreen(
    viewModel: SupporterViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val purchaseComplete by viewModel.purchaseComplete.collectAsState()

    if (purchaseComplete) {
        AlertDialog(
            onDismissRequest = {
                val activity = context as? Activity
                activity?.showAppReview()
                viewModel.resetPurchaseComplete()
            },
            title = {
                Text(stringResource(R.string.thank_you))
            },
            text = {
                Text(stringResource(R.string.thank_you_support_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val activity = context as? Activity
                        activity?.showAppReview()
                        viewModel.resetPurchaseComplete()
                    }
                ) {
                    Text(stringResource(R.string.dismiss))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.supporter_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.APP_HORIZONTAL_SPACING)
        ) {
            Text(
                text = stringResource(R.string.supporter_description),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (!context.isInstalledFromPlayStore()) {
                AppButton(
                    onClick = { context.openKoFiPage() },
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.become_a_supporter)
                )
            } else {
                Text(
                    text = stringResource(R.string.one_time_support),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                ProductGrid(
                    products = products.sortedBy {
                        it.price.filter { c -> c.isDigit() }.toIntOrNull() ?: 0
                    },
                    onProductClick = {
                        val activity = context as? Activity
                        if (activity != null) viewModel.launchBilling(activity, it)
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

                Text(
                    text = stringResource(R.string.support_via_review),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { context.openPlayStorePage() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(stringResource(R.string.open_play_store), fontSize = 18.sp)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text(
                text = stringResource(R.string.support_via_other_apps),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { context.openDeveloperPlayStorePage() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(stringResource(R.string.visit), fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun ProductGrid(
    products: List<AppProduct>,
    onProductClick: (AppProduct) -> Unit
) {
    if (products.isEmpty()) {
        Text(stringResource(R.string.loading))
        return
    }

    val rows = products.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (product in row) {
                    AppButton(
                        onClick = { onProductClick(product) },
                        text = product.price.ifEmpty { product.name },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
