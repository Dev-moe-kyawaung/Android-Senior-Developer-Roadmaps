package com.yourapp.premium.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourapp.premium.data.models.SubscriptionTier
import com.yourapp.premium.presentation.viewmodel.PremiumViewModel
import com.yourapp.premium.presentation.viewmodel.UiState

@Composable
fun PricingScreen(
    viewModel: PremiumViewModel = hiltViewModel(),
    onSubscribe: (SubscriptionTier) -> Unit
) {
    val subscriptionTiers by viewModel.subscriptionTiers.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Choose Your Plan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        when (subscriptionTiers) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Success -> {
                items((subscriptionTiers as UiState.Success<List<SubscriptionTier>>).data) { tier ->
                    SubscriptionTierCard(
                        tier = tier,
                        onSubscribe = { onSubscribe(tier) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }
            }
            is UiState.Error -> {
                item {
                    Text(
                        text = (subscriptionTiers as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun SubscriptionTierCard(
    tier: SubscriptionTier,
    onSubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = tier.tierName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Baseline) {
                Text(
                    text = "$${tier.basePrice}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "/${tier.billingCycle.name.lowercase()}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (tier.discountedPrice != null && tier.discountedPrice < tier.basePrice) {
                Text(
                    text = "Save ${String.format("%.0f", (tier.basePrice - tier.discountedPrice))}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (tier.trialDays > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Badge {
                    Text("${tier.trialDays} days free trial")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Included features:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            tier.features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✓", color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = feature, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSubscribe,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Subscribe Now")
            }
        }
    }
}

