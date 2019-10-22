package co.netguru.baby.monitor.client.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import co.netguru.baby.monitor.client.data.AppDatabase
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class DatabaseTests {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var appDatabase: AppDatabase

    @Before
    fun createDb() {
        appDatabase = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().context,
                AppDatabase::class.java
        ).allowMainThreadQueries()
                .build()
    }

    @After
    fun closeDb() {
        appDatabase.close()
    }

    @Test
    fun testDatabaseInsertion() {
        val log = LogDataEntity("Test Action", "", "192.168.0.12")
        appDatabase.logDataDao().insertAll(log)
        appDatabase.logDataDao().getAllData()
                .test()
                .assertValue { list ->
                    list.firstOrNull() == log
                }
    }

    @Test
    fun testDatabaseMultipleInsertions() {
        val log = listOf(
                LogDataEntity("Test Action0", "", "192.168.0.12"),
                LogDataEntity("Test Action1", "", "192.168.0.11"),
                LogDataEntity("Test Action2", "", "192.168.0.10")
        )
        appDatabase.logDataDao().insertAll(log[0], log[1], log[2])
        appDatabase.logDataDao().getAllData().test()
                .assertValue { list ->
                    list == log
                }
    }
}
