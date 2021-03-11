package com.brightpattern

inline fun <reified R> ifNotNull(vararg values: Any?, block: (List<Any>) -> R?): R? {
    values.forEach { element ->
        if (element == null) return null
    }
    return block(values.filterNotNull())
}