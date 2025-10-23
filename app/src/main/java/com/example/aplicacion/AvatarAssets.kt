package com.example.aplicacion

import com.example.aplicacion.R

object AvatarAssets {
    /** Claves que se guardan en Firebase */
    val keys: List<String> = listOf(
        "ic_avatar_1", "ic_avatar_2", "ic_avatar_3",
        "ic_avatar_4", "ic_avatar_5", "ic_avatar_6"
    )

    fun resFor(key: String?): Int = when (key) {

        "ic_avatar_1" -> R.drawable.ic_avatar_1
      "ic_avatar_2" -> R.drawable.ic_avatar_2
        "ic_avatar_3" -> R.drawable.ic_avatar_3
         "ic_avatar_4" -> R.drawable.ic_avatar_4
         "ic_avatar_5" -> R.drawable.ic_avatar_5
        "ic_avatar_6" -> R.drawable.ic_avatar_6
        else -> R.drawable.outline_account_circle_24
    }
}
