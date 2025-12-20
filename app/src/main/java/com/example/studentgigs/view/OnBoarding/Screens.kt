package com.example.studentgigs.view.OnBoarding

sealed class Screens(
    val route: String
) {
    data object FirstStart: Screens("firstStart")
    data object SecondStart: Screens("secondStart")
    data object ThirdStart: Screens("thirdStart")
    data object FourthStart: Screens("fourthStart")
}