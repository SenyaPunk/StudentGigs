package com.example.studentgigs.view.OnApp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CurrencyRuble
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Category(
    val name: String,
    val icon: String
)

data class Gig(
    val title: String,
    val company: String,
    val duration: String,
    val location: String,
    val price: String,
    val tags: List<String>,
    val isNew: Boolean = false,
    val isSaved: Boolean = false,
    val iconEmoji: String
)

sealed class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    object Home : BottomNavItem("Главная", Icons.Rounded.Home, "home")
    object Search : BottomNavItem("Поиск", Icons.Rounded.Search, "search")
    object Saved : BottomNavItem("Избранное", Icons.Rounded.Bookmark, "saved")
    object Profile : BottomNavItem("Профиль", Icons.Rounded.Person, "profile")

}


@Composable
fun MainApp(innerPadding: PaddingValues) {
    var currentRoute by remember { mutableStateOf("home") }

    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)

        ) {
            when (currentRoute) {
                "home" -> {
                    TopContainer("Сеня")
                    Spacer(modifier = Modifier.height(16.dp))
                    MediumContainer()
                }
                "search" -> {
                    Text("Экран поиска", modifier = Modifier.padding(16.dp))
                }
                "saved" -> {
                    Text("Сохраненные проекты", modifier = Modifier.padding(16.dp))
                }
                "profile" -> {
                    Text("Ваш профиль", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        BottomContainer(
            currentRoute = currentRoute,
            onNavigate = { currentRoute = it }
        )
    }





}

@Composable
fun TopContainer(name: String?) {
    var search by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(horizontal = 15.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(
                    "Привет, ${name ?: "гость"}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
                Text(
                    "Найди свой проект",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                HeaderCircleButton(
                    icon = Icons.Outlined.Notifications,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { /* TODO */ },
                    hasBadge = true
                )

                HeaderCircleButton(
                    icon = Icons.Outlined.Person,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    iconColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { /* TODO */ }
                )

            }
        }

        SearchInput(search = search, onSearchChange = {search = it})
        CategorySelector()


    }
}

@Preview
@Composable
fun MediumContainer() {
    val gigs = listOf(
        Gig("Landing Page для стартапа", "TechStart", "2 недели", "Удалённо", "15 000", listOf("React", "Figma"), isNew = true, iconEmoji = "🚀"),
        Gig("Анализ данных пользователей", "DataCorp", "3 недели", "Москва", "20 000", listOf("Python", "SQL"), isSaved = true, iconEmoji = "📊"),
        Gig("Landing Page для стартапа", "TechStart", "2 недели", "Удалённо", "15 000", listOf("React", "Figma"), isNew = true, iconEmoji = "🚀"),
        Gig("Анализ данных пользователей", "DataCorp", "3 недели", "Москва", "20 000", listOf("Python", "SQL"), isSaved = true, iconEmoji = "📊"),
        Gig("Landing Page для стартапа", "TechStart", "2 недели", "Удалённо", "15 000", listOf("React", "Figma"), isNew = true, iconEmoji = "🚀"),
        Gig("Анализ данных пользователей", "DataCorp", "3 недели", "Москва", "20 000", listOf("Python", "SQL"), isSaved = true, iconEmoji = "📊"),
    )

    Column(
        modifier = Modifier.padding(horizontal = 15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                "Новые проекты",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${gigs.size} доступно",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }


        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f)
        ) {
            items(gigs) {
                GigCard(it)
            }
        }


    }
}


@Composable
fun BottomContainer(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Saved,
        BottomNavItem.Profile
    )

    Surface(
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 12.dp)
            .fillMaxWidth()
            .height(72.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach {
                val isSelected = currentRoute == it.route

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(it.route) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Surface(
                            modifier = Modifier
                                .fillMaxHeight(0.9f)
                                .fillMaxWidth(0.9f),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface
                        ) {}
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = it.title,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun TagItem(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InfoRowItem(
    icon: ImageVector,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

@Composable
fun GigCard(gig: Gig) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(gig.iconEmoji, style = MaterialTheme.typography.headlineSmall)
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = gig.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = gig.company,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (gig.isNew) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = CircleShape
                        ) {
                            Text(
                                "Новое",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    IconButton(onClick = { /* Save */ }) {
                        Icon(
                            imageVector = if (gig.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            tint = if (gig.isSaved) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoRowItem(Icons.Outlined.Schedule, gig.duration)
                InfoRowItem(Icons.Outlined.Place, gig.location)
                InfoRowItem(
                    Icons.Outlined.CurrencyRuble,
                    gig.price,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gig.tags.forEach { tag ->
                    TagItem(tag)
                }
            }
        }
    }
}




@Composable
fun HeaderCircleButton(
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    hasBadge: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        if (hasBadge) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-10).dp, y = 10.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
                    .padding(2.dp)
            )
        }
    }
}




@Composable
fun PillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.outline
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.outline,
            focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    )
}

@Composable
fun SearchInput(
    search: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PillTextField(
        value = search,
        onValueChange = onSearchChange,
        modifier = modifier,
        placeholder = "Поиск проектов...",
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.size(20.dp)
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        )
    )
}



@Composable
fun CategorySelector() {
    val categories = listOf(
        Category("Все", "🔥"),
        Category("Разработка", "💻"),
        Category("Дизайн", "🎨"),
        Category("Маркетинг", "📈")
    )

    var selectedCategory by remember { mutableStateOf(categories[0]) }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory

            CategoryShip(
                category = category,
                isSelected = isSelected,
                onClick = { selectedCategory = category }
            )



        }
    }
}

@Composable
fun CategoryShip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = category.icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.name,
                color = contentColor,
                fontSize = 12.sp
            )
        }
    }
}


@Preview
@Composable
fun viewFun() {

    TopContainer("Senya")
}
