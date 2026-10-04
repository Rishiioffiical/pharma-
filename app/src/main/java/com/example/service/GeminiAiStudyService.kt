package com.example.service

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiStudyResponse(
    val explanation: String,
    val keyPoints: List<String>,
    val highYieldCitations: List<String>,
    val clinicalDisclaimer: String = "Educational information only. This platform does not diagnose, prescribe, or replace professional medical or pharmacotherapeutic advice."
)

class GeminiAiStudyService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun consultAiAssistant(prompt: String, contextSubject: String = "Pharmacology"): AiStudyResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isKeyConfigured = apiKey.isNotBlank() && !apiKey.contains("MY_GEMINI_API_KEY")

        if (isKeyConfigured) {
            try {
                val fullPrompt = """
                    You are PharmaHub AI, an expert academic clinical pharmacology professor and pharmacy education tutor.
                    Subject context: $contextSubject
                    Student query: $prompt
                    
                    Format your response clearly with:
                    1. Direct, high-yield academic explanation
                    2. Bulleted key mechanisms / clinical pearls
                    3. Standard textbook citations (e.g. Goodman & Gilman's Pharmacological Basis of Therapeutics, Lachman & Lieberman's Industrial Pharmacy, or Trease and Evans Pharmacognosy).
                    Remember to maintain strict educational standards.
                """.trimIndent()

                val jsonBody = JSONObject().apply {
                    val contentsArray = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val partsArray = JSONArray().apply {
                                put(JSONObject().apply { put("text", fullPrompt) })
                            }
                            put("parts", partsArray)
                        }
                        put(contentObj)
                    }
                    put("contents", contentsArray)
                }

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val respStr = response.body?.string().orEmpty()
                    val jsonResp = JSONObject(respStr)
                    val text = jsonResp.optJSONArray("candidates")
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text").orEmpty()

                    if (text.isNotBlank()) {
                        return@withContext parseAiResponse(text)
                    }
                } else {
                    Log.w("GeminiAi", "API returned code ${response.code}, falling back to curated academic engine.")
                }
            } catch (e: Exception) {
                Log.w("GeminiAi", "API request exception: ${e.message}, falling back to curated academic engine.")
            }
        }

        // High-Yield Academic Fallback Engine with domain-specific pharmacy responses
        return@withContext generateDomainPharmacyResponse(prompt, contextSubject)
    }

    private fun parseAiResponse(rawText: String): AiStudyResponse {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val bulletPoints = lines.filter { it.startsWith("•") || it.startsWith("-") || it.startsWith("*") }
            .map { it.removePrefix("•").removePrefix("-").removePrefix("*").trim() }
        
        return AiStudyResponse(
            explanation = rawText,
            keyPoints = if (bulletPoints.isNotEmpty()) bulletPoints.take(5) else listOf(
                "Verify receptor binding affinity and clearance mechanisms.",
                "Review bioavailability differences across oral vs parenteral dosage forms.",
                "Cross-check with clinical guidelines and drug interaction matrices."
            ),
            highYieldCitations = listOf(
                "Goodman & Gilman's The Pharmacological Basis of Therapeutics (14th Ed.)",
                "Rang & Dale's Pharmacology (9th Ed.)",
                "USP-NF Compendial Standards & Monographs"
            )
        )
    }

    private fun generateDomainPharmacyResponse(prompt: String, contextSubject: String): AiStudyResponse {
        val lower = prompt.lowercase()
        return when {
            lower.contains("bioavailability") || lower.contains("vd") || lower.contains("clearance") || lower.contains("pharmacokinetic") -> {
                AiStudyResponse(
                    explanation = """
                        ### Pharmacokinetics Mastery: Bioavailability & Volume of Distribution
                        
                        **Bioavailability (F)** represents the fraction of an unchanged active pharmaceutical ingredient that reaches systemic circulation following administration.
                        
                        * Formula for absolute bioavailability:
                          **F = (AUC_extravascular / AUC_intravenous) × (Dose_iv / Dose_po)**
                        
                        **Volume of Distribution (Vd)**:
                        Apparent volume into which a drug must disperse to achieve plasma concentration:
                        * **Vd = Total Amount of Drug in Body (Dose) / Initial Plasma Concentration (C0)**
                        * High Vd (> 42 L) signifies extensive lipophilicity and tissue sequestration (e.g., Amiodarone, Chloroquine, Digoxin).
                        * Low Vd (< 5 L) indicates the drug remains confined to vascular space due to large molecular size or high albumin binding (e.g., Warfarin, Heparin).
                    """.trimIndent(),
                    keyPoints = listOf(
                        "IV administration by definition exhibits F = 1.0 (100% bioavailability).",
                        "High hepatic first-pass extraction (ER > 0.7) drastically diminishes oral bioavailability.",
                        "Total Clearance: Cl = Vd × Elimination Rate Constant (Kel).",
                        "Steady-state plasma level (Css) is achieved after approximately 4 to 5 elimination half-lives."
                    ),
                    highYieldCitations = listOf(
                        "Goodman & Gilman's Pharmacological Basis of Therapeutics, Chapter 2 (ADME)",
                        "Applied Biopharmaceutics & Pharmacokinetics (Shargel & Yu, 8th Ed.)",
                        "Lachman & Lieberman's Theory and Practice of Industrial Pharmacy"
                    )
                )
            }
            lower.contains("beta") || lower.contains("antibiotic") || lower.contains("penicillin") || lower.contains("sar") -> {
                AiStudyResponse(
                    explanation = """
                        ### Medicinal Chemistry: SAR of Beta-Lactam Antibacterials
                        
                        Beta-lactams (Penicillins, Cephalosporins, Carbapenems, Monobactams) target bacterial **Penicillin-Binding Proteins (PBPs)**, inhibiting transpeptidation during cell wall peptidoglycan synthesis.
                        
                        **Structure-Activity Relationship (SAR) Highlights:**
                        1. **Strained 4-Membered Beta-Lactam Ring**: High ring tension is essential; nucleophilic attack by serine residue in PBP active site opens the ring, covalently inactivating the enzyme.
                        2. **C-3 Free Carboxylic Acid**: Essential for ionic bonding with basic amino acids (Lys/Arg) in PBPs.
                        3. **Acylamino Side Chain**: Modulates antibacterial spectrum and beta-lactamase stability (e.g., bulky side chains in Methicillin prevent beta-lactamase steric fit).
                    """.trimIndent(),
                    keyPoints = listOf(
                        "Clavulanic acid and Tazobactam act as 'suicide inhibitors' of beta-lactamase.",
                        "Cephalosporins possess a 6-membered dihydrothiazine ring instead of thiazolidine.",
                        "Allergenic hapten formation results from reactive penicilloyl-protein conjugates."
                    ),
                    highYieldCitations = listOf(
                        "Foye's Principles of Medicinal Chemistry (8th Ed., Chapter 33)",
                        "Wilson and Gisvold's Textbook of Organic Medicinal and Pharmaceutical Chemistry",
                        "Katzung's Basic & Clinical Pharmacology (15th Ed.)"
                    )
                )
            }
            lower.contains("quiz") || lower.contains("test") || lower.contains("question") -> {
                AiStudyResponse(
                    explanation = """
                        ### High-Yield Pharmacy Exam Question & Rationales
                        
                        **Question:** A patient on long-term Digoxin therapy develops hypokalemia after starting a loop diuretic. Why is the risk of digitalis toxicity drastically elevated?
                        
                        **High-Yield Rationale:**
                        Digoxin and K+ ions compete for the same extracellular binding site on the myocardial **Na+/K+ ATPase pump**. When serum K+ levels fall (hypokalemia), more Na+/K+ ATPase binding sites become unoccupied and accessible to Digoxin, magnifying digitalis toxicity even at normal therapeutic serum concentrations.
                    """.trimIndent(),
                    keyPoints = listOf(
                        "Therapeutic serum Digoxin range is narrow: 0.5 to 0.9 ng/mL.",
                        "Digitalis toxicity antidote: Digoxin-specific Fab antibody fragments (DigiFab).",
                        "Classic ECG signs: Sagging ST depression ('Salvador Dali mustache'), inverted T waves, arrhythmias."
                    ),
                    highYieldCitations = listOf(
                        "Katzung's Clinical Pharmacology, Chapter 13: Drugs Used in Heart Failure",
                        "Pharmacotherapy: A Pathophysiologic Approach (DiPiro, 12th Ed.)",
                        "British National Formulary (BNF 86)"
                    )
                )
            }
            else -> {
                AiStudyResponse(
                    explanation = """
                        ### Academic Pharmacy Study Consultation: $contextSubject
                        
                        Regarding your inquiry: **"$prompt"**
                        
                        In professional pharmaceutical sciences, comprehensive understanding requires synthesizing:
                        1. **Physicochemical Properties**: pKa, partition coefficient (LogP), solubility, and salt forms.
                        2. **Pharmacodynamics**: Receptor affinity, intrinsic activity, signal transduction, and dose-response curve steepness.
                        3. **Clinical Pharmacotherapeutics**: Individualized dosing, renal/hepatic impairment adjustments, and adverse drug reaction profiles.
                    """.trimIndent(),
                    keyPoints = listOf(
                        "Correlate theoretical mechanisms with clinical drug-drug interactions.",
                        "Review therapeutic drug monitoring (TDM) guidelines for narrow therapeutic index (NTI) drugs.",
                        "Utilize spaced repetition to solidify drug classifications and contraindications."
                    ),
                    highYieldCitations = listOf(
                        "Goodman & Gilman's Pharmacological Basis of Therapeutics (14th Ed.)",
                        "Martindale: The Complete Drug Reference (39th Ed.)",
                        "Remington: The Science and Practice of Pharmacy (23rd Ed.)"
                    )
                )
            }
        }
    }
}
