package com.example.data.local

import androidx.room.*
import com.example.data.model.PharmaceuticalDrug
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for pharmaceutical drugs local library.
 */
@Dao
interface PharmaceuticalDrugDao {

    @Query("SELECT * FROM pharmaceutical_drugs ORDER BY name ASC")
    fun getAllDrugs(): Flow<List<PharmaceuticalDrug>>

    @Query("SELECT * FROM pharmaceutical_drugs WHERE id = :id")
    fun getDrugById(id: String): Flow<PharmaceuticalDrug?>

    @Query("SELECT * FROM pharmaceutical_drugs WHERE id = :id")
    suspend fun getDrugDirect(id: String): PharmaceuticalDrug?

    @Query("""
        SELECT * FROM pharmaceutical_drugs 
        WHERE name LIKE '%' || :query || '%' 
           OR genericName LIKE '%' || :query || '%' 
           OR brandName LIKE '%' || :query || '%' 
           OR classification LIKE '%' || :query || '%' 
           OR indications LIKE '%' || :query || '%' 
        ORDER BY name ASC
    """)
    fun searchDrugs(query: String): Flow<List<PharmaceuticalDrug>>

    @Query("SELECT * FROM pharmaceutical_drugs WHERE classification LIKE '%' || :classification || '%' ORDER BY name ASC")
    fun getDrugsByClassification(classification: String): Flow<List<PharmaceuticalDrug>>

    @Query("SELECT * FROM pharmaceutical_drugs WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteDrugs(): Flow<List<PharmaceuticalDrug>>

    @Query("SELECT DISTINCT classification FROM pharmaceutical_drugs ORDER BY classification ASC")
    fun getAllClassifications(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrug(drug: PharmaceuticalDrug)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrugs(drugs: List<PharmaceuticalDrug>)

    @Update
    suspend fun updateDrug(drug: PharmaceuticalDrug)

    @Delete
    suspend fun deleteDrug(drug: PharmaceuticalDrug)

    @Query("DELETE FROM pharmaceutical_drugs WHERE id = :id")
    suspend fun deleteDrugById(id: String)

    @Query("UPDATE pharmaceutical_drugs SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM pharmaceutical_drugs")
    suspend fun getCount(): Int
}
