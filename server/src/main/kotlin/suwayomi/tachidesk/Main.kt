package suwayomi.tachidesk

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import suwayomi.tachidesk.manga.impl.Category
import suwayomi.tachidesk.manga.impl.CategoryManga
import suwayomi.tachidesk.manga.impl.Chapter
import suwayomi.tachidesk.server.JavalinSetup.javalinSetup
import suwayomi.tachidesk.server.applicationSetup

fun main() {
    applicationSetup()
    javalinSetup()
    MangasToUpdate()
}

class MangasToUpdate() {
    private val jobs = emptyList<Job>().toMutableList()
    private val sources = emptyList<Source>().toMutableList()
    private val routine: CoroutineScope = CoroutineScope(Dispatchers.Default)

    class Source(sourceId: String) {
        val id = sourceId
        val mangasId = emptyList<Int>().toMutableList()
        fun add(mangaId: Int) {
            mangasId.add(mangaId)
        }
    }

    private fun find(sourceId: String): Source {
        val src = sources.firstOrNull { it.id == sourceId }
        return if (src !== null) src
        else {
            val newSrc = Source(sourceId)
            sources.add(newSrc)
            newSrc
        }
    }

    private fun fetch() {
        Category.getCategoryList().flatMap { cat ->
            CategoryManga.getCategoryMangaList(cat.id).map { manga ->
                find(manga.sourceId).add(manga.id)
            }
        }
    }

    init {
        routine.launch {
            while (true) {
                fetch()
                sources.forEach { src ->
                    src.mangasId.forEach { mg ->
                        Chapter.getChapterList(mg, true)
                    }
                }
                delay(21600000) // 6 hours between each full library update
            }
        }
    }
}
