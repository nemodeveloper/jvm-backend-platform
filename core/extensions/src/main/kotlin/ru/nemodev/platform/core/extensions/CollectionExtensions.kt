package ru.nemodev.platform.core.extensions

fun <E> MutableCollection<E>.addNotNull(element: E?) {
    if (element != null) {
        this.add(element)
    }
}

fun <T> Collection<T>?.isNotNullOrEmpty() = !this.isNullOrEmpty()

fun <T> Collection<T>.containsOne(elements: Collection<T>): Boolean {
    var result = false
    elements.forEach { element ->
        if (this.contains(element))
            result = true
    }
    return result
}