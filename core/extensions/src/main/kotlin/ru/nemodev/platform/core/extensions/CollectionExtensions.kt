package ru.nemodev.platform.core.extensions

fun <E> MutableCollection<E>.addNotNull(element: E?) {
    if (element != null) {
        add(element)
    }
}

fun <T> Collection<T>?.isNotNullOrEmpty() = !isNullOrEmpty()

fun <T> Collection<T>.containsOne(elements: Collection<T>): Boolean {
    elements.forEach { element ->
        if (contains(element)) {
            return true
        }
    }
    return false
}