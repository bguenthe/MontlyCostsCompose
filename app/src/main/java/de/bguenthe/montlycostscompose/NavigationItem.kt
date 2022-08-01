package de.bguenthe.montlycostscompose

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    object Home : NavigationItem("home", R.drawable.ic_home, "Home")
    object Costs : NavigationItem("costs", R.drawable.ic_bubble, "Costs")
    object Monthly_Average : NavigationItem("monthly_average", R.drawable.ic_music, "Monthly ⌀")
    object Daily_Average : NavigationItem("daily_average", R.drawable.ic_music, "Daily ⌀")
    object Sums : NavigationItem("sums", R.drawable.ic_moon, "Sums")
    object Settings : NavigationItem("settings", R.drawable.ic_profile, "Settings")
}