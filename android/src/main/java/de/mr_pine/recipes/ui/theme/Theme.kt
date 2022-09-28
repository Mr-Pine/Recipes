package de.mr_pine.recipes.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.color.ColorRoles
import com.google.android.material.color.MaterialColors

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * @see [HarmonizedTheme]
 */
@Composable
fun RecipesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Igitt

data class CustomColor(
    val name: String,
    val color: Color,
    val harmonized: Boolean,
    var roles: ColorRoles
) {
    data class ColorRoles(
        val accent: Color = Color.Unspecified,
        val onAccent: Color = Color.Unspecified,
        val accentContainer: Color = Color.Unspecified,
        val onAccentContainer: Color = Color.Unspecified
    )
}

data class ExtendedColors(val colors: Array<CustomColor>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExtendedColors

        if (!colors.contentEquals(other.colors)) return false

        return true
    }

    override fun hashCode(): Int {
        return colors.contentHashCode()
    }
}


fun setupErrorColors(colorScheme: ColorScheme, isLight: Boolean): ColorScheme {
    val harmonizedError =
        MaterialColors.harmonize(error.toArgb(), colorScheme.primary.toArgb())
    val roles = MaterialColors.getColorRoles(harmonizedError, isLight)
    //returns a colorScheme with newly harmonized error colors
    return colorScheme.copy(
        error = Color(roles.accent),
        onError = Color(roles.onAccent),
        errorContainer = Color(roles.accentContainer),
        onErrorContainer = Color(roles.onAccentContainer)
    )
}

val initializeExtended = ExtendedColors(
    arrayOf(
        CustomColor("doneGreen", doneGreen, doneGreenHarmonize, CustomColor.ColorRoles()),
        CustomColor("revertOrange", revertOrange, revertOrangeHarmonize, CustomColor.ColorRoles())
    )
)

private fun ColorRoles.toColorRoles(): CustomColor.ColorRoles = CustomColor.ColorRoles(
    accent = Color(this.accent),
    onAccent = Color(this.onAccent),
    accentContainer = Color(this.accentContainer),
    onAccentContainer = Color(this.onAccentContainer),
)

fun setupCustomColors(
    colorScheme: ColorScheme,
    isLight: Boolean
): ExtendedColors {
    initializeExtended.colors.forEach { customColor ->
        // Retrieve record
        val shouldHarmonize = customColor.harmonized
        // Blend or not
        if (shouldHarmonize) {
            val blendedColor =
                MaterialColors.harmonize(customColor.color.toArgb(), colorScheme.primary.toArgb())
            customColor.roles = MaterialColors.getColorRoles(blendedColor, isLight).toColorRoles()
        } else {
            customColor.roles =
                MaterialColors.getColorRoles(customColor.color.toArgb(), isLight).toColorRoles()
        }
    }
    return initializeExtended
}

val LocalExtendedColors = staticCompositionLocalOf {
    initializeExtended
}

@Composable
fun HarmonizedTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    isDynamic: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2,
    content: @Composable () -> Unit
) {
    val colors = if (isDynamic) {
        val context = LocalContext.current
        if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (useDarkTheme) DarkColorScheme else LightColorScheme
    }
    val colorsWithHarmonizedError =
        if (errorHarmonize) setupErrorColors(colors, !useDarkTheme) else colors

    val extendedColors = setupCustomColors(colors, !useDarkTheme)
    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorsWithHarmonizedError,
            typography = Typography,
            content = content
        )
    }
}

object Extended {
    val doneGreen: CustomColor.ColorRoles
        @Composable
        get() = LocalExtendedColors.current.colors[0].roles

    val revertOrange: CustomColor.ColorRoles
        @Composable
        get() = LocalExtendedColors.current.colors[1].roles
}
