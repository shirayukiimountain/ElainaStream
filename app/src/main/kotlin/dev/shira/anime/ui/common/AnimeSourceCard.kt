package dev.shira.anime.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.shira.anime.R
import dev.shira.anime.domain.model.AnimeSourceType

@Composable
fun AnimeSourceCard(
    source: AnimeSourceType,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = Color(0xFF151720)
    val cardBorder = Color(0xFF222533)
    val cardBgSelected = Color(0xFF1A1F2C)
    val accentIndigo = Color(0xFF6366F1)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val textMuted = Color(0xFF64748B)

    // Randomize artwork among available Elaina drawables
    val cardImageRes = remember(source) {
        when (source) {
            AnimeSourceType.ANIMEX -> listOf(R.drawable.elaina, R.drawable.elaina_3).random()
            AnimeSourceType.ANIMEKU -> listOf(R.drawable.elaina_2, R.drawable.elaina_4).random()
            AnimeSourceType.ANIMEIN -> listOf(R.drawable.elaina_3, R.drawable.elaina).random()
        }
    }

    val currentCardBg = if (isSelected) cardBgSelected else cardBg

    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = currentCardBg),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentIndigo.copy(alpha = 0.65f) else cardBorder
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            // 1. Right-aligned Image container with natural aspect ratio & smooth fade
            Box(
                modifier = Modifier
                    .matchParentSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Image(
                    painter = painterResource(id = cardImageRes),
                    contentDescription = source.displayName,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(110.dp)
                )

                // Horizontal Gradient Overlay Fade
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(115.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                0.0f to currentCardBg,
                                0.30f to currentCardBg.copy(alpha = 0.85f),
                                0.65f to currentCardBg.copy(alpha = 0.25f),
                                1.0f to Color.Transparent
                            )
                        )
                )
            }

            // 3. Information & Controls (Left-aligned details on top of gradient)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = source.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) textPrimary else Color(0xFFCBD5E1)
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (isSelected) accentIndigo.copy(alpha = 0.20f) else Color(0xFF1E2230),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) accentIndigo.copy(alpha = 0.35f) else cardBorder
                            )
                        ) {
                            Text(
                                text = source.serverBadge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFFA5B4FC) else textSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = source.tagLine,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        source.features.forEach { feature ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) accentIndigo.copy(alpha = 0.14f) else Color(0xFF1D202C)
                            ) {
                                Text(
                                    text = feature,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color(0xFFA5B4FC) else textMuted,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                if (isSelected) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = accentIndigo,
                        modifier = Modifier
                            .size(26.dp)
                            .padding(start = 4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Terpilih",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
