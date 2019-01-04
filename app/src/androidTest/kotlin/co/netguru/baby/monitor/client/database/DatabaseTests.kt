package co.netguru.baby.monitor.client.database

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import co.netguru.baby.monitor.client.application.database.AppDatabase
import co.netguru.baby.monitor.client.feature.client.home.log.database.LogDataEntity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTests {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var appDatabase: AppDatabase

    @Before
    fun createDb() {
        appDatabase = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getContext(),
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
