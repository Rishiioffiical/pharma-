package com.example.data.repository

import com.example.data.local.PharmaceuticalDrugDao
import com.example.data.model.PharmaceuticalDrug
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository class to manage the local library of pharmaceutical drugs.
 * Adheres to the Repository Pattern to abstract Room DAO access from ViewModels and UI.
 */
class PharmaceuticalDrugRepository(
    private val drugDao: PharmaceuticalDrugDao
) {

    /**
     * Flow of all pharmaceutical drugs in the local library, sorted alphabetically by name.
     */
    val allDrugs: Flow<List<PharmaceuticalDrug>> = drugDao.getAllDrugs()

    /**
     * Flow of bookmarked/favorite pharmaceutical drugs.
     */
    val favoriteDrugs: Flow<List<PharmaceuticalDrug>> = drugDao.getFavoriteDrugs()

    /**
     * Flow of all unique drug classifications currently in the local library.
     */
    val allClassifications: Flow<List<String>> = drugDao.getAllClassifications()

    /**
     * Observe a specific drug by its unique ID.
     */
    fun getDrugById(id: String): Flow<PharmaceuticalDrug?> {
        return drugDao.getDrugById(id)
    }

    /**
     * Retrieve a specific drug directly (one-shot suspend function).
     */
    suspend fun getDrugDirect(id: String): PharmaceuticalDrug? {
        return drugDao.getDrugDirect(id)
    }

    /**
     * Search pharmaceutical drugs by name, generic name, brand name, classification, or indications.
     */
    fun searchDrugs(query: String): Flow<List<PharmaceuticalDrug>> {
        return drugDao.searchDrugs(query.trim())
    }

    /**
     * Filter pharmaceutical drugs by therapeutic classification.
     */
    fun getDrugsByClassification(classification: String): Flow<List<PharmaceuticalDrug>> {
        return drugDao.getDrugsByClassification(classification)
    }

    /**
     * Insert a new pharmaceutical drug into the local library.
     */
    suspend fun insertDrug(drug: PharmaceuticalDrug) {
        drugDao.insertDrug(drug)
    }

    /**
     * Convenience method to add a drug with explicit core fields.
     */
    suspend fun addDrug(
        name: String,
        classification: String,
        indications: String,
        sideEffects: String,
        genericName: String = "",
        brandName: String = "",
        contraindications: String = "",
        mechanismOfAction: String = "",
        dosage: String = "",
        dosageForms: String = "",
        precautions: String = ""
    ): PharmaceuticalDrug {
        val newDrug = PharmaceuticalDrug(
            id = "drug_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            genericName = genericName.trim().ifEmpty { name.trim() },
            brandName = brandName.trim(),
            classification = classification.trim(),
            indications = indications.trim(),
            sideEffects = sideEffects.trim(),
            contraindications = contraindications.trim(),
            mechanismOfAction = mechanismOfAction.trim(),
            dosage = dosage.trim(),
            dosageForms = dosageForms.trim(),
            precautions = precautions.trim(),
            isFavorite = false,
            createdAt = System.currentTimeMillis()
        )
        drugDao.insertDrug(newDrug)
        return newDrug
    }

    /**
     * Batch insert a list of pharmaceutical drugs.
     */
    suspend fun insertDrugs(drugs: List<PharmaceuticalDrug>) {
        drugDao.insertDrugs(drugs)
    }

    /**
     * Update an existing pharmaceutical drug in the local library.
     */
    suspend fun updateDrug(drug: PharmaceuticalDrug) {
        drugDao.updateDrug(drug)
    }

    /**
     * Delete a pharmaceutical drug from the library.
     */
    suspend fun deleteDrug(drug: PharmaceuticalDrug) {
        drugDao.deleteDrug(drug)
    }

    /**
     * Delete a pharmaceutical drug by its unique ID.
     */
    suspend fun deleteDrugById(id: String) {
        drugDao.deleteDrugById(id)
    }

    /**
     * Toggle the favorite/bookmark status of a drug.
     */
    suspend fun toggleFavorite(id: String, currentStatus: Boolean) {
        drugDao.updateFavoriteStatus(id, !currentStatus)
    }

    /**
     * Populate initial high-yield pharmaceutical drugs if the local database is currently empty.
     */
    suspend fun seedSampleDrugsIfEmpty() {
        if (drugDao.getCount() == 0) {
            drugDao.insertDrugs(samplePharmaceuticalDrugs)
        }
    }

    companion object {
        val samplePharmaceuticalDrugs = listOf(
            PharmaceuticalDrug(
                id = "drug_metformin_01",
                name = "Metformin Hydrochloride",
                genericName = "Metformin",
                brandName = "Glucophage, Glycomet, Fortamet",
                classification = "Biguanide Antidiabetic",
                indications = "First-line pharmacotherapy for Type 2 Diabetes Mellitus; management of Polycystic Ovary Syndrome (PCOS).",
                sideEffects = "Gastrointestinal distress (diarrhea, nausea, vomiting, abdominal discomfort), metallic taste, Vitamin B12 malabsorption with prolonged use; rare but potentially fatal lactic acidosis.",
                contraindications = "Severe renal impairment (eGFR < 30 mL/min/1.73m²), acute or chronic metabolic acidosis, decompensated heart failure, severe hypoxemia, sepsis.",
                mechanismOfAction = "Activates AMP-activated protein kinase (AMPK), suppressing hepatic gluconeogenesis and glycogenolysis, enhancing peripheral insulin sensitivity, and slowing intestinal glucose absorption.",
                dosage = "Initial: 500mg once or twice daily with meals; titrated up to maximum 2000mg - 2550mg daily in divided doses.",
                dosageForms = "Immediate-release tablets (500mg, 850mg, 1000mg), Extended-release tablets (500mg, 750mg, 1000mg), Oral solution (500mg/5mL).",
                precautions = "Withhold for 48 hours prior to and after iodinated radiocontrast procedures; monitor renal function and serum B12 annually."
            ),
            PharmaceuticalDrug(
                id = "drug_atorvastatin_02",
                name = "Atorvastatin Calcium",
                genericName = "Atorvastatin",
                brandName = "Lipitor, Atorva, Storvas",
                classification = "HMG-CoA Reductase Inhibitor (Statin)",
                indications = "Primary and secondary hypercholesterolemia, prevention of cardiovascular atherosclerotic disease (myocardial infarction, stroke).",
                sideEffects = "Myopathy, myalgia, elevated creatine kinase (CK), rhabdomyolysis with acute kidney injury (rare), elevated serum transaminases (ALT/AST), dyspepsia, headache, hyperglycemia.",
                contraindications = "Active liver disease, unexplained persistent serum transaminase elevation, pregnancy (Category X, teratogenic), breastfeeding.",
                mechanismOfAction = "Competitive, selective reversible inhibition of 3-hydroxy-3-methylglutaryl-coenzyme A (HMG-CoA) reductase, the rate-limiting enzyme in cholesterol biosynthesis, upregulating hepatic LDL receptor clearance.",
                dosage = "10mg to 80mg orally once daily, taken at any time of day with or without food.",
                dosageForms = "Oral tablets (10mg, 20mg, 40mg, 80mg).",
                precautions = "Potent CYP3A4 substrate: co-administration with macrolides (clarithromycin), azole antifungals, or grapefruit juice markedly increases rhabdomyolysis risk."
            ),
            PharmaceuticalDrug(
                id = "drug_lisinopril_03",
                name = "Lisinopril Dihydrate",
                genericName = "Lisinopril",
                brandName = "Zestril, Prinivil, Listril",
                classification = "Angiotensin-Converting Enzyme (ACE) Inhibitor",
                indications = "Essential and secondary hypertension, heart failure with reduced ejection fraction (HFrEF), adjunct within 24 hours of acute myocardial infarction, diabetic nephropathy.",
                sideEffects = "Persistent dry nocturnal cough (mediated by bradykinin and substance P accumulation in bronchial tissue), hyperkalemia, acute renal insufficiency, angioedema (life-threatening airway obstruction), orthostatic hypotension, dizziness.",
                contraindications = "History of idiopathic or ACE-inhibitor-induced angioedema, bilateral renal artery stenosis, co-administration with Aliskiren in diabetic patients, pregnancy (Boxed Warning: Fetotoxicity).",
                mechanismOfAction = "Competitively inhibits ACE (kininase II), suppressing the conversion of Angiotensin I to Angiotensin II; reduces circulating aldosterone, blunts systemic vascular resistance, and preserves renal microcirculation.",
                dosage = "Hypertension: 10mg to 40mg once daily. Heart Failure: initial 2.5mg - 5mg, titrating up to target 20mg - 40mg daily.",
                dosageForms = "Oral tablets (2.5mg, 5mg, 10mg, 20mg, 30mg, 40mg), Oral solution (1mg/mL).",
                precautions = "Monitor serum potassium and creatinine within 1-2 weeks of initiation; avoid potassium supplements and potassium-sparing diuretics."
            ),
            PharmaceuticalDrug(
                id = "drug_amoxicillin_04",
                name = "Amoxicillin Trihydrate",
                genericName = "Amoxicillin",
                brandName = "Amoxil, Mox, Novamox",
                classification = "Aminopenicillin (Beta-Lactam Antibiotic)",
                indications = "Acute otitis media, bacterial sinusitis, community-acquired pneumonia, acute streptococcal pharyngitis, skin and soft-tissue infections, part of triple regimen for Helicobacter pylori eradication.",
                sideEffects = "Nausea, vomiting, diarrhea, Clostridioides difficile-associated pseudomembranous colitis, maculopapular erythematous skin rash (especially in infectious mononucleosis), Type I hypersensitivity (urticaria, anaphylaxis).",
                contraindications = "Documented severe immediate hypersensitivity (anaphylaxis, angioedema) to penicillins or beta-lactams.",
                mechanismOfAction = "Binds to bacterial penicillin-binding proteins (PBPs), inhibiting the transpeptidation reaction in bacterial peptidoglycan synthesis, resulting in autolytic cell wall rupture.",
                dosage = "Mild/Moderate infections: 250mg - 500mg every 8 hours or 500mg - 875mg every 12 hours. Severe/H. pylori: 1000mg twice daily.",
                dosageForms = "Capsules (250mg, 500mg), Oral suspension (125mg/5mL, 250mg/5mL, 400mg/5mL), Dispersible tablets.",
                precautions = "High oral bioavailability (75-90%) unaffected by food; dose reduction required in severe renal impairment (eGFR < 30 mL/min)."
            ),
            PharmaceuticalDrug(
                id = "drug_omeprazole_05",
                name = "Omeprazole",
                genericName = "Omeprazole",
                brandName = "Prilosec, Omez, Losec",
                classification = "Proton Pump Inhibitor (PPI)",
                indications = "Gastroesophageal Reflux Disease (GERD), erosive esophagitis, active duodenal and benign gastric ulcers, Zollinger-Ellison syndrome, H. pylori eradication.",
                sideEffects = "Headache, abdominal pain, diarrhea, nausea, flatulence, hypomagnesemia, hypocalcemia with increased risk of osteoporotic fractures (long-term therapy), Clostridioides difficile colitis, acute interstitial nephritis.",
                contraindications = "Hypersensitivity to substituted benzimidazoles; co-administration with Rilpivirine-containing antiretroviral regimens.",
                mechanismOfAction = "Acid-activated prodrug that concentrates in parietal cell secretory canaliculi; forms a covalent disulfide bond with the H+/K+ ATPase enzyme system, irreversibly inhibiting the final step of gastric acid secretion.",
                dosage = "GERD / Ulcers: 20mg to 40mg once daily taken 30-60 minutes prior to the first meal of the day.",
                dosageForms = "Delayed-release capsules (10mg, 20mg, 40mg), Delayed-release tablets, Powder for oral suspension.",
                precautions = "Potent inhibitor of CYP2C19: decreases bioactivation of Clopidogrel prodrug; impairs absorption of pH-dependent medications (iron salts, ketoconazole)."
            ),
            PharmaceuticalDrug(
                id = "drug_salbutamol_06",
                name = "Salbutamol Sulfate (Albuterol)",
                genericName = "Salbutamol / Albuterol",
                brandName = "Ventolin, Asthalin, ProAir",
                classification = "Short-Acting Beta-2 Adrenergic Agonist (SABA)",
                indications = "Rapid relief and prevention of acute bronchospasm in bronchial asthma and Chronic Obstructive Pulmonary Disease (COPD); prevention of exercise-induced bronchospasm.",
                sideEffects = "Fine skeletal muscle tremor (especially fingers/hands), tachycardia, palpitations, peripheral vasodilation with flushing, hypokalemia (intracellular potassium shift), nervousness, headache.",
                contraindications = "Hypersensitivity to salbutamol or inhaled propellant excipients; non-asthmatic threatened abortion.",
                mechanismOfAction = "Selectively stimulates beta-2 adrenergic receptors on bronchial smooth muscle, activating adenylyl cyclase to increase intracellular cAMP, promoting protein kinase A activation and smooth muscle relaxation.",
                dosage = "Inhalation: 1-2 puffs (100mcg - 200mcg) every 4-6 hours as needed for acute symptoms. Nebulization: 2.5mg - 5.0mg.",
                dosageForms = "Metered-Dose Inhaler (100mcg/actuation), Dry Powder Inhaler, Nebulizer solution (2.5mg/2.5mL), Oral syrup (2mg/5mL).",
                precautions = "Frequent use (> 2 canisters/year or daily rescue use) signifies uncontrolled asthma requiring inhaled corticosteroid (ICS) escalation; caution in cardiac arrhythmias and hyperthyroidism."
            ),
            PharmaceuticalDrug(
                id = "drug_warfarin_07",
                name = "Warfarin Sodium",
                genericName = "Warfarin",
                brandName = "Coumadin, Jantoven, Warf",
                classification = "Vitamin K Antagonist (Anticoagulant)",
                indications = "Prophylaxis and treatment of venous thromboembolism (deep vein thrombosis and pulmonary embolism), stroke prevention in atrial fibrillation, mechanical heart valve anticoagulation.",
                sideEffects = "Major hemorrhage (GI, intracranial, retroperitoneal), purple toes syndrome (cholesterol microembolization), warfarin-induced skin necrosis (due to rapid Protein C depletion), calciphylaxis, hematuria.",
                contraindications = "Active clinical bleeding, severe thrombocytopenia, hemorrhagic tendencies, recent neurosurgery/ocular surgery, uncontrolled malignant hypertension, pregnancy (Category X: Fetal Warfarin Syndrome).",
                mechanismOfAction = "Competitively inhibits Vitamin K 2,3-epoxide reductase complex 1 (VKORC1), blocking regeneration of active hydroquinone Vitamin K1, thereby preventing gamma-carboxylation of clotting factors II, VII, IX, and X, as well as regulatory Proteins C and S.",
                dosage = "Individualized based on International Normalized Ratio (INR); initial typical dose: 2mg - 5mg orally once daily, targeting INR 2.0 - 3.0 (2.5 - 3.5 for mechanical mitral valves).",
                dosageForms = "Oral tablets (1mg, 2mg, 2.5mg, 3mg, 4mg, 5mg, 6mg, 7.5mg, 10mg - color-coded by strength).",
                precautions = "Extensive CYP2C9, CYP1A2, and CYP3A4 drug and food interactions; high dietary Vitamin K intake (green leafy vegetables) blunts anticoagulant efficacy."
            ),
            PharmaceuticalDrug(
                id = "drug_levothyroxine_08",
                name = "Levothyroxine Sodium",
                genericName = "Levothyroxine (T4)",
                brandName = "Synthroid, Eltroxin, Levoxyl",
                classification = "Synthetic Thyroid Hormone",
                indications = "Replacement therapy for primary, secondary, and tertiary hypothyroidism; pituitary TSH suppression in thyroid cancer and nodular goiter management.",
                sideEffects = "Symptoms of hyperthyroidism secondary to therapeutic overdose: tachycardia, palpitations, cardiac arrhythmias, nervousness, insomnia, tremors, muscle weakness, weight loss, heat intolerance, diaphoresis; accelerated bone mineral density reduction.",
                contraindications = "Uncorrected subclinical or overt thyrotoxicosis, acute myocardial infarction, uncorrected adrenal insufficiency (initiating thyroid hormone can precipitate acute adrenal crisis).",
                mechanismOfAction = "Synthetic levo-isomer of thyroxine (T4), which undergoes peripheral 5'-deiodination by deiodinase enzymes to form triiodothyronine (T3); binds to thyroid hormone nuclear receptors, regulating gene transcription for basal metabolic rate and protein synthesis.",
                dosage = "Full replacement: approximately 1.6 mcg/kg/day orally in young healthy adults. Elderly / Coronary artery disease: initial 12.5mcg - 25mcg daily, titrated every 4-6 weeks based on serum TSH.",
                dosageForms = "Oral tablets (25mcg, 50mcg, 75mcg, 88mcg, 100mcg, 112mcg, 125mcg, 137mcg, 150mcg, 175mcg, 200mcg, 300mcg), IV injection.",
                precautions = "Must be administered in the morning on an empty stomach with a full glass of water, at least 30-60 minutes before breakfast; calcium, iron supplements, and PPIs impair absorption."
            )
        )
    }
}
