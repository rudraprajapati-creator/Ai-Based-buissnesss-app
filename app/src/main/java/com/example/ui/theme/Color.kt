package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// BizFlow Professional Business Palette
// Calm, restrained, high-contrast, professional (No AI purple/blue gradients or neon)
val BizFlowCharcoal = Color(0xFF111827)       // Primary text
val BizFlowGrayDark = Color(0xFF374151)       // Secondary text
val BizFlowGrayMuted = Color(0xFF6B7280)      // Tertiary text
val BizFlowGrayLight = Color(0xFF9CA3AF)      // Placeholder text

val BizFlowBorder = Color(0xFFE5E7EB)         // Subtle structural borders
val BizFlowBorderSubtle = Color(0xFFF3F4F6)   // Subtle dividers
val BizFlowBackground = Color(0xFFFAFAFA)     // Light neutral background
val BizFlowSurface = Color(0xFFFFFFFF)        // Clean white surface
val BizFlowSurfaceHover = Color(0xFFF9FAFB)   // Row hover / subtle highlight

// Accent: Executive Slate Navy (Used sparingly for primary actions)
val BizFlowAccent = Color(0xFF0F172A)
val BizFlowAccentHover = Color(0xFF1E293B)
val BizFlowAccentSoft = Color(0xFFF1F5F9)

// Semantic Status Colors (Restrained, muted, non-neon)
val StatusSuccessBg = Color(0xFFECFDF5)
val StatusSuccessText = Color(0xFF065F46)
val StatusSuccessBorder = Color(0xFFA7F3D0)

val StatusWarningBg = Color(0xFFFFFBEB)
val StatusWarningText = Color(0xFF92400E)
val StatusWarningBorder = Color(0xFFFDE68A)

val StatusDangerBg = Color(0xFFFEF2F2)
val StatusDangerText = Color(0xFF991B1B)
val StatusDangerBorder = Color(0xFFFECACA)

val StatusNeutralBg = Color(0xFFF3F4F6)
val StatusNeutralText = Color(0xFF374151)
val StatusNeutralBorder = Color(0xFFE5E7EB)

val StatusInfoBg = Color(0xFFF0F9FF)
val StatusInfoText = Color(0xFF0369A1)
val StatusInfoBorder = Color(0xFFBAE6FD)

// Backward compatible aliases for unified screen styling
val BizFlowBlue = BizFlowAccent
val BizFlowBlueDark = BizFlowAccentHover
val BizFlowBlueLight = BizFlowAccent
val BizFlowBlueContainer = Color(0xFFF1F5F9)

val BizFlowNavy = BizFlowAccent
val BizFlowSlate = BizFlowAccentHover
val BizFlowGreen = StatusSuccessText
val BizFlowGreenLight = StatusSuccessBg
val BizFlowRose = StatusDangerText
val BizFlowRoseLight = StatusDangerBg
val BizFlowAmber = StatusWarningText
val BizFlowAmberLight = StatusWarningBg
val BizFlowTeal = Color(0xFF0F766E)
