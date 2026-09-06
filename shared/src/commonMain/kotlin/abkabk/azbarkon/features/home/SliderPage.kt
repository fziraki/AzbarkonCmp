package abkabk.azbarkon.features.home

sealed class SliderPage {
    data object DistichOfDay : SliderPage()

    data object Challenge : SliderPage()

    data object TasvirNegar : SliderPage()
}
