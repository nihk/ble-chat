package nick.template.data

sealed class LocationState {
    object On : LocationState()
    object Off : LocationState()
}
