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
class PharmaceuticalDrugRepositoryTest {

    private lateinit var database: PharmaceuticalDrugDatabase
    private lateinit var repository: PharmaceuticalDrugRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            PharmaceuticalDrugDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = PharmaceuticalDrugRepository(database.pharmaceuticalDrugDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrievePharmaceuticalDrug() = runBlocking {
        val drug = PharmaceuticalDrug(
            id = "test_drug_01",
            name = "Ciprofloxacin",
            genericName = "Ciprofloxacin",
            classification = "Fluoroquinolone Antibacterial",
            indications = "Complicated urinary tract infections, acute pyelonephritis",
            sideEffects = "Tendinitis, tendon rupture, QT interval prolongation, GI distress",
            mechanismOfAction = "Inhibits bacterial DNA gyrase and topoisomerase IV"
        )

        repository.insertDrug(drug)

        val allDrugs = repository.allDrugs.first()
        assertEquals(1, allDrugs.size)
        val retrieved = allDrugs[0]
        assertEquals("Ciprofloxacin", retrieved.name)
        assertEquals("Fluoroquinolone Antibacterial", retrieved.classification)
        assertEquals("Complicated urinary tract infections, acute pyelonephritis", retrieved.indications)
        assertEquals("Tendinitis, tendon rupture, QT interval prolongation, GI distress", retrieved.sideEffects)
    }

    @Test
    fun searchDrugsByClassificationAndName() = runBlocking {
        repository.addDrug(
            name = "Metformin",
            classification = "Biguanide Antidiabetic",
            indications = "Type 2 Diabetes Mellitus",
            sideEffects = "Diarrhea, nausea, lactic acidosis"
        )
        repository.addDrug(
            name = "Atorvastatin",
            classification = "HMG-CoA Reductase Inhibitor",
            indications = "Hypercholesterolemia",
            sideEffects = "Myopathy, rhabdomyolysis"
        )

        val antidiabeticSearch = repository.searchDrugs("Biguanide").first()
        assertEquals(1, antidiabeticSearch.size)
        assertEquals("Metformin", antidiabeticSearch[0].name)

        val statinByClass = repository.getDrugsByClassification("HMG-CoA").first()
        assertEquals(1, statinByClass.size)
        assertEquals("Atorvastatin", statinByClass[0].name)
    }

    @Test
    fun deleteDrugRemovesFromLibrary() = runBlocking {
        val added = repository.addDrug(
            name = "Omeprazole",
            classification = "Proton Pump Inhibitor",
            indications = "GERD, peptic ulcer disease",
            sideEffects = "Headache, diarrhea, hypomagnesemia"
        )

        var list = repository.allDrugs.first()
        assertEquals(1, list.size)

        repository.deleteDrugById(added.id)
        list = repository.allDrugs.first()
        assertEquals(0, list.size)
    }

    @Test
    fun seedSampleDrugsPopulatesLibrary() = runBlocking {
        repository.seedSampleDrugsIfEmpty()
        val all = repository.allDrugs.first()
        assertTrue(all.isNotEmpty())
        assertTrue(all.any { it.name.contains("Metformin") })
        assertTrue(all.any { it.classification.contains("Statin") })
    }
}
