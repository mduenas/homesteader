package com.markduenas.homesteader.core.designsystem.accessibility

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

/**
 * Accessibility utilities for the Homesteader app.
 * Provides consistent semantic descriptions for screen readers.
 */

/**
 * Adds a content description for accessibility.
 */
fun Modifier.accessibleDescription(description: String): Modifier = this.semantics {
    contentDescription = description
}

/**
 * Marks an element as a heading for screen readers.
 */
fun Modifier.accessibleHeading(): Modifier = this.semantics {
    heading()
}

/**
 * Adds both content description and role for interactive elements.
 */
fun Modifier.accessibleButton(description: String): Modifier = this.semantics {
    contentDescription = description
    role = Role.Button
}

/**
 * Adds content description for image elements.
 */
fun Modifier.accessibleImage(description: String): Modifier = this.semantics {
    contentDescription = description
    role = Role.Image
}

/**
 * Adds state description for elements with dynamic states.
 */
fun Modifier.accessibleState(state: String): Modifier = this.semantics {
    stateDescription = state
}

/**
 * Clears semantics and sets a custom merged description.
 * Use for compound elements that should be read as a single unit.
 */
fun Modifier.accessibleMerged(description: String): Modifier = this.clearAndSetSemantics {
    contentDescription = description
}

/**
 * Content descriptions for common animal-related elements.
 */
object AnimalAccessibility {
    fun animalCardDescription(
        name: String,
        species: String,
        status: String,
        tagId: String?
    ): String {
        val tagPart = tagId?.let { ", tag number $it" } ?: ""
        return "$name, $species$tagPart. Status: $status. Tap to view details."
    }

    fun animalAvatarDescription(name: String): String {
        return "Avatar for $name"
    }

    fun statusBadgeDescription(status: String): String {
        return "Status: $status"
    }

    fun tagBadgeDescription(tagId: String): String {
        return "Tag number: $tagId"
    }
}

/**
 * Content descriptions for event-related elements.
 */
object EventAccessibility {
    fun eventCardDescription(
        eventType: String,
        date: String,
        animalName: String?
    ): String {
        val animalPart = animalName?.let { " for $it" } ?: ""
        return "$eventType event$animalPart on $date. Tap to view details."
    }

    fun eventTypeIconDescription(eventType: String): String {
        return "$eventType event icon"
    }
}

/**
 * Content descriptions for task-related elements.
 */
object TaskAccessibility {
    fun taskCardDescription(
        title: String,
        dueDate: String,
        isOverdue: Boolean
    ): String {
        val overdueText = if (isOverdue) "Overdue. " else ""
        return "${overdueText}Task: $title. Due: $dueDate. Tap to view details."
    }

    fun completeButtonDescription(taskTitle: String): String {
        return "Mark $taskTitle as complete"
    }
}

/**
 * Content descriptions for stat cards.
 */
object StatAccessibility {
    fun statCardDescription(
        title: String,
        value: String,
        subtitle: String?
    ): String {
        val subtitlePart = subtitle?.let { ". $it" } ?: ""
        return "$title: $value$subtitlePart"
    }
}

/**
 * Content descriptions for navigation elements.
 */
object NavigationAccessibility {
    fun backButtonDescription(): String = "Go back"
    fun closeButtonDescription(): String = "Close"
    fun menuButtonDescription(): String = "Open menu"
    fun moreOptionsDescription(): String = "More options"
    fun searchButtonDescription(): String = "Search"
    fun filterButtonDescription(): String = "Filter"
    fun addButtonDescription(itemType: String): String = "Add new $itemType"
    fun editButtonDescription(itemName: String): String = "Edit $itemName"
    fun deleteButtonDescription(itemName: String): String = "Delete $itemName"
}

/**
 * Content descriptions for form elements.
 */
object FormAccessibility {
    fun textFieldDescription(label: String, value: String): String {
        return if (value.isBlank()) {
            "$label, empty"
        } else {
            "$label: $value"
        }
    }

    fun dateFieldDescription(label: String, date: String): String {
        return "$label: $date. Tap to change."
    }

    fun dropdownDescription(label: String, selectedValue: String): String {
        return "$label: $selectedValue selected. Tap to change."
    }

    fun checkboxDescription(label: String, isChecked: Boolean): String {
        val state = if (isChecked) "checked" else "unchecked"
        return "$label, $state"
    }

    fun switchDescription(label: String, isOn: Boolean): String {
        val state = if (isOn) "on" else "off"
        return "$label, $state"
    }
}

/**
 * Content descriptions for empty states and error states.
 */
object StateAccessibility {
    fun emptyStateDescription(title: String, message: String): String {
        return "$title. $message"
    }

    fun errorStateDescription(message: String): String {
        return "Error: $message"
    }

    fun loadingDescription(context: String = "content"): String {
        return "Loading $context"
    }
}
