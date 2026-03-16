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
fun AccountScreenSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        SkeletonLine(
            modifier = Modifier.width(120.dp),
            height = 28.dp
        )
        
        ProfileSectionSkeleton()
        
        RewardsSectionSkeleton()
        
        SubscriptionSectionSkeleton()
        
        AccountInfoSkeleton()
    }
}


@Composable
fun ProfileSectionSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SkeletonCircle(size = 56.dp)
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonLine(
                    modifier = Modifier.width(120.dp),
                    height = 18.dp
                )
                SkeletonLine(
                    modifier = Modifier.width(180.dp),
                    height = 14.dp
                )
            }
        }
    }
}

@Composable
fun RewardsSectionSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SkeletonLine(
                modifier = Modifier.width(100.dp),
                height = 20.dp
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonLine(
                    modifier = Modifier.width(80.dp),
                    height = 16.dp
                )
                SkeletonLine(
                    modifier = Modifier.width(60.dp),
                    height = 24.dp
                )
            }
            
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun SubscriptionSectionSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonCircle(size = 48.dp)
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkeletonLine(
                        modifier = Modifier.width(140.dp),
                        height = 18.dp
                    )
                    SkeletonLine(
                        modifier = Modifier.width(180.dp),
                        height = 14.dp
                    )
                }
            }
        }
    }
}

@Composable
fun AccountInfoSkeleton() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonLine(
            modifier = Modifier.width(160.dp),
            height = 18.dp
        )
        
        repeat(2) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonLine(
                    modifier = Modifier.width(80.dp),
                    height = 14.dp
                )
                SkeletonLine(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    height = 16.dp
                )
            }
        }
    }
}
