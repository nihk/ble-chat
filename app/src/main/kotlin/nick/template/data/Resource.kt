package nick.template.data

sealed class Resource<T> {
    abstract val data: T?
    data class Loading<T>(override val data: T? = null) : Resource<T>()
    data class Success<T>(override val data: T? = null) : Resource<T>()
    data class Error<T>(override val data: T? = null, val throwable: Throwable): Resource<T>()
}
