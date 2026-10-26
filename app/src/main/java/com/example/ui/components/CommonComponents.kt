package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BizFlowAccent
import com.example.ui.theme.BizFlowBorder
import com.example.ui.theme.BizFlowCharcoal
import com.example.ui.theme.BizFlowGrayDark
import com.example.ui.theme.BizFlowGrayMuted
import com.example.ui.theme.BizFlowSurface
import com.example.ui.theme.StatusDangerBg
import com.example.ui.theme.StatusDangerBorder
import com.example.ui.theme.StatusDangerText
import com.example.ui.theme.StatusInfoBg
import com.example.ui.theme.StatusInfoBorder
import com.example.ui.theme.StatusInfoText
import com.example.ui.theme.StatusNeutralBg
import com.example.ui.theme.StatusNeutralBorder
import com.example.ui.theme.StatusNeutralText
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.StatusSuccessBorder
import com.example.ui.theme.StatusSuccessText
import com.example.ui.theme.StatusWarningBg
import com.example.ui.theme.StatusWarningBorder
import com.example.ui.theme.StatusWarningText
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 2
    }
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    fun formatCurrency(amount: Double): String = inrFormat.format(amount)
    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
    fun formatDateTime(timestamp: Long): String = timeFormat.format(Date(timestamp))
}

enum class StatusType {
    SUCCESS, WARNING, DANGER, ERROR, NEUTRAL, INFO
}

@Composable
fun StatusBadge(
    text: String,
    statusType: StatusType,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (statusType) {
        StatusType.SUCCESS -> Triple(StatusSuccessBg, StatusSuccessText, StatusSuccessBorder)
        StatusType.WARNING -> Triple(StatusWarningBg, StatusWarningText, StatusWarningBorder)
        StatusType.DANGER, StatusType.ERROR -> Triple(StatusDangerBg, StatusDangerText, StatusDangerBorder)
        StatusType.INFO -> Triple(StatusInfoBg, StatusInfoText, StatusInfoBorder)
        StatusType.NEUTRAL -> Triple(StatusNeutralBg, StatusNeutralText, StatusNeutralBorder)
    }

    Box(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
fun MetricStat(
    label: String,
    value: String,
    subtext: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = BizFlowSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BizFlowBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = BizFlowGrayMuted,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = BizFlowCharcoal
            )
            if (subtext != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtext,
                    fontSize = 11.sp,
                    color = BizFlowGrayMuted
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = BizFlowSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BizFlowBorder),
        onClick = { onClick?.invoke() },
        enabled = onClick != null
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = BizFlowGrayMuted,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BizFlowGrayDark,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = BizFlowCharcoal
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = BizFlowGrayMuted
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BizFlowCharcoal,
            letterSpacing = 0.5.sp
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BizFlowAccent,
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onActionClick() }
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFF3F4F6), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BizFlowGrayDark,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = BizFlowCharcoal
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            fontSize = 12.sp,
            color = BizFlowGrayMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
