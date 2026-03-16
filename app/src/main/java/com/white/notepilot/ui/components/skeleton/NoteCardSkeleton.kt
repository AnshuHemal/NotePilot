package com.white.notepilot.ui.components.skeleton

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NoteCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonLine(
                    modifier = Modifier.weight(0.6f),
                    height = 20.dp
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                SkeletonBox(
                    modifier = Modifier.size(24.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
            
            SkeletonLine(
                modifier = Modifier.fillMaxWidth(0.9f),
                height = 14.dp
            )
            
            SkeletonLine(
                modifier = Modifier.fillMaxWidth(0.7f),
                height = 14.dp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonLine(
                    modifier = Modifier.width(80.dp),
                    height = 12.dp
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkeletonBox(
                        modifier = Modifier.size(20.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier.size(20.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NoteListSkeleton(
    itemCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            NoteCardSkeleton()
        }
    }
}
