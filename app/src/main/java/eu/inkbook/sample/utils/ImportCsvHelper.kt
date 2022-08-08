package eu.inkbook.sample.utils


import eu.inkbook.sample.data.DataEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.io.File

import kotlin.collections.ArrayList

class ImportCsvHelper {
//I/O operations should be triggered in coroutine along with Dispatcher.IO due to their blocking nature
    fun save(file: File): Deferred<ArrayList<DataEntity>> =
        CoroutineScope(Dispatchers.IO).async {
            val dataToLoad = ArrayList<DataEntity>()
            if (file.exists()) {
                val iteration = file.readLines().listIterator()
                iteration.next()
                while (iteration.hasNext()) {
                    val next = iteration.next()

                    try {
                        val split = next.split(",")
                        val entity = DataEntity(
                            split[0],
                            split[1].toInt(),
                            split[2].toInt(),
                            split[3].toInt(),
                            split[4].toInt(),
                            split[5].toInt(),
                            split[6],
                            split[7],
                            split[8],
                            split[9].toLong(),
                            split[10],
                            split[11].toDouble()
                        )
                        dataToLoad.add(entity)
                    } catch (e: Exception) {
                    }
                }
            }
            return@async dataToLoad

        }
}