package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PharmaceuticalDrugDatabase
import com.example.data.model.PharmaceuticalDrug
import com.example.data.repository.PharmaceuticalDrugRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: PharmaceuticalDrugDatabase
    private lateinit var repository: PharmaceuticalDrugRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PharmaceuticalDrugDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PharmaceuticalDrugRepository(db.pharmaceuticalDrugDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun appNameStringMatches() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PHARMAHUB", appName)
    }

    @Test
    fun testPharmaceuticalDrugInsertAndQuery() = runBlocking {
        val drug = PharmaceuticalDrug(
            id = "test_drug_1",
            name = "Amoxicillin",
            classification = "Antibiotic",
            indications = "Bacterial infections",
            sideEffects = "Nausea, rash"
        )
        repository.insertDrug(drug)

        val drugs = repository.allDrugs.first()
        assertEquals(1, drugs.size)
        assertEquals("Amoxicillin", drugs[0].name)
        assertEquals("Antibiotic", drugs[0].classification)
        assertEquals("Bacterial infections", drugs[0].indications)
        assertEquals("Nausea, rash", drugs[0].sideEffects)
    }

    @Test
    fun testSearchAndClassificationFilter() = runBlocking {
        repository.addDrug(
            name = "Metformin",
            classification = "Antidiabetic",
            indications = "Type 2 Diabetes",
            sideEffects = "GI upset"
        )
        repository.addDrug(
            name = "Atorvastatin",
            classification = "Statin",
            indications = "Hypercholesterolemia",
            sideEffects = "Myalgia"
        )

        val searchResult = repository.searchDrugs("Diabetes").first()
        assertEquals(1, searchResult.size)
        assertEquals("Metformin", searchResult[0].name)

        val statinResult = repository.getDrugsByClassification("Statin").first()
        assertEquals(1, statinResult.size)
        assertEquals("Atorvastatin", statinResult[0].name)
    }
}

