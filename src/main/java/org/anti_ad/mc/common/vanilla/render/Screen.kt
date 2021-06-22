package org.anti_ad.mc.common.vanilla.render

import org.anti_ad.mc.common.math2d.Rectangle
import org.anti_ad.mc.common.math2d.Size
import org.anti_ad.mc.common.vanilla.Vanilla
import org.anti_ad.mc.common.vanilla.VanillaUtil
import org.anti_ad.mc.common.vanilla.alias.LiteralText
import org.anti_ad.mc.common.vanilla.alias.Screen

val rScreenWidth
    get() = Vanilla.window().scaledWidth
val rScreenHeight
    get() = Vanilla.window().scaledHeight
val rScreenSize
    get() = Size(rScreenWidth,
                 rScreenHeight)
val rScreenBounds
    get() = Rectangle(0,
                      0,
                      rScreenWidth,
                      rScreenHeight)

fun rRenderDirtBackground() { // Screen.renderDirtBackground
//  (Vanilla.screen() ?: dummyScreen).renderBackgroundTexture(0)
    (Vanilla.screen() ?: dummyScreen).renderDirtBackground(0)
//  (Vanilla.screen() ?: dummyScreen).func_231165_f_(0)
}

fun rRenderBlackOverlay() { // Screen.renderBackground
    rFillGradient(0,
                  0,
                  rScreenWidth,
                  rScreenHeight,
                  -1072689136,
                  -804253680)
}

fun rRenderVanillaScreenBackground() { // Screen.renderBackground
    if (VanillaUtil.inGame()) {
        rRenderBlackOverlay()
    } else {
        rRenderDirtBackground()
    }
}

private val dummyScreen = object : Screen(
    LiteralText(
        ""
    )
) {}
