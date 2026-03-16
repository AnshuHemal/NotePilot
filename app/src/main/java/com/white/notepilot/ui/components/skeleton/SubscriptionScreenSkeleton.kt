package com.white.notepilot.ui.components.skeleton

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SubscriptionScreenSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HeaderSectionSkeleton()
        
        repeat(3) {
            SubscriptionPlanCardSkeleton()
        }
    }
}

@Composable
fun HeaderSectionSkeleton() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonLine(
            modifier = Modifier.fillMaxWidth(0.8f),
            height = 16.dp
        )
        
        SkeletonBox(
            modifier = Modifier
                .width(200.dp)
                .height(40.dp),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun SubscriptionPlanCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonLine(
                    modifier = Modifier.width(100.dp),
                    height = 40.dp
                )
                SkeletonLine(
                    modifier = Modifier.width(60.dp),
                    height = 16.dp
                )
            }
            
            SkeletonLine(
                modifier = Modifier.width(140.dp),
                height = 24.dp
            )
            
            SkeletonLine(
                modifier = Modifier.fillMaxWidth(0.9f),
                height = 16.dp
            )
            
            repeat(4) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SkeletonBox(
                        modifier = Modifier.size(18.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    SkeletonLine(
                        modifier = Modifier.fillMaxWidth(0.7f),
                        height = 16.dp
                    )
                }
            }
            
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}
