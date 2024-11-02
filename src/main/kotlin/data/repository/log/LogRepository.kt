package data.repository.log

import data.model.LogItem
import data.model.UuidItem
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.coroutines.CoroutineContext

interface LogRepository {

    suspend fun add(logItem: LogItem)
    fun by(context: CoroutineContext, uuid: UUID): Flow<List<LogItem>>
    fun uuids(context: CoroutineContext): Flow<List<UuidItem>>
    suspend fun deleteBy(uuid: UUID)
    suspend fun deleteAll()
}