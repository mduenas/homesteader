package com.markduenas.homesteader.core.designsystem.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

/**
 * Standard animation durations used throughout the app.
 */
object AnimationDurations {
    const val SHORT = 150
    const val MEDIUM = 300
    const val LONG = 500
}

/**
 * Standard enter animations for screens and components.
 */
object EnterAnimations {
    /**
     * Fade in animation.
     */
    fun fadeIn(durationMillis: Int = AnimationDurations.MEDIUM): EnterTransition =
        fadeIn(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = LinearOutSlowInEasing
            )
        )

    /**
     * Slide in from the right (for forward navigation).
     */
    fun slideInFromRight(durationMillis: Int = AnimationDurations.MEDIUM): EnterTransition =
        slideInHorizontally(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            initialOffsetX = { fullWidth -> fullWidth }
        ) + fadeIn(
            animationSpec = tween(durationMillis = durationMillis)
        )

    /**
     * Slide in from the left (for back navigation).
     */
    fun slideInFromLeft(durationMillis: Int = AnimationDurations.MEDIUM): EnterTransition =
        slideInHorizontally(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            initialOffsetX = { fullWidth -> -fullWidth }
        ) + fadeIn(
            animationSpec = tween(durationMillis = durationMillis)
        )

    /**
     * Slide in from the bottom (for modal-like screens).
     */
    fun slideInFromBottom(durationMillis: Int = AnimationDurations.MEDIUM): EnterTransition =
        slideInVertically(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            initialOffsetY = { fullHeight -> fullHeight }
        ) + fadeIn(
            animationSpec = tween(durationMillis = durationMillis)
        )

    /**
     * Scale and fade in (for popups and dialogs).
     */
    fun scaleIn(durationMillis: Int = AnimationDurations.MEDIUM): EnterTransition =
        scaleIn(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            initialScale = 0.8f
        ) + fadeIn(
            animationSpec = tween(durationMillis = durationMillis)
        )

    /**
     * Spring-based enter for bouncy feel.
     */
    fun springIn(): EnterTransition =
        scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialScale = 0.8f
        ) + fadeIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
}

/**
 * Standard exit animations for screens and components.
 */
object ExitAnimations {
    /**
     * Fade out animation.
     */
    fun fadeOut(durationMillis: Int = AnimationDurations.MEDIUM): ExitTransition =
        fadeOut(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            )
        )

    /**
     * Slide out to the left (for forward navigation).
     */
    fun slideOutToLeft(durationMillis: Int = AnimationDurations.MEDIUM): ExitTransition =
        slideOutHorizontally(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            targetOffsetX = { fullWidth -> -fullWidth }
        ) + fadeOut(
            animationSpec = tween(durationMillis = durationMillis)
        )

    /**
     * Slide out to the right (for back navigation).
     */
    fun slideOutToRight(durationMillis: Int = AnimationDurations.MEDIUM): ExitTransition =
        slideOutHorizontally(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            targetOffsetX = { fullWidth -> fullWidth }
        ) + fadeOut(
            animationSpec = tween(durationMillis = durationMillis)
        )

    /**
     * Slide out to the bottom (for dismissing modal-like screens).
     */
    fun slideOutToBottom(durationMillis: Int = AnimationDurations.MEDIUM): ExitTransition =
        slideOutVertically(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            targetOffsetY = { fullHeight -> fullHeight }
        ) + fadeOut(
            animationSpec = tween(durationMillis = durationMillis)
        )

    /**
     * Scale and fade out (for popups and dialogs).
     */
    fun scaleOut(durationMillis: Int = AnimationDurations.MEDIUM): ExitTransition =
        scaleOut(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            targetScale = 0.8f
        ) + fadeOut(
            animationSpec = tween(durationMillis = durationMillis)
        )
}
