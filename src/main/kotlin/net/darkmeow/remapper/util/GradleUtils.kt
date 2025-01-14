@file:Suppress("UNCHECKED_CAST")

package net.darkmeow.remapper.util

import org.gradle.api.provider.Provider

fun <T, R : Provider<T>> Provider<R>.flatten() : R {
    return this.flatMap { it } as R
}