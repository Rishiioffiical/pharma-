package com.example.data.local

import com.example.data.model.*

object InitialPharmaData {

    val sampleResources = listOf(
        ResourceEntity(
            id = "res_pk_01",
            title = "Pharmacokinetics: Volume of Distribution & Clearance Mastery",
            subject = "Pharmacology-I",
            semester = 4,
            course = "B.Pharm",
            university = "National Institute of Pharmaceutical Education",
            author = "Prof. Arvind Sharma",
            uploadDate = "2 days ago",
            fileType = "PDF",
            fileSize = "4.2 MB",
            tags = "Pharmacology, Clearance, Half-Life, High-Yield, GPAT",
            rating = 4.9f,
            reviewCount = 142,
            views = 1250,
            downloads = 480,
            isBookmarked = true,
            description = "Complete handwritten notes with derivations for non-linear pharmacokinetics, steady-state plasma concentrations, and renal clearance formulas."
        ),
        ResourceEntity(
            id = "res_pc_02",
            title = "Medicinal Chemistry: SAR of Beta-Lactam Antibiotics & Cephalosporins",
            subject = "Medicinal Chemistry-II",
            semester = 5,
            course = "B.Pharm",
            university = "Bombay College of Pharmacy",
            author = "Dr. Ananya Ray",
            uploadDate = "5 days ago",
            fileType = "PDF",
            fileSize = "6.8 MB",
            tags = "Medicinal Chem, Antibiotics, SAR, Penicillin, Cephalosporins",
            rating = 4.8f,
            reviewCount = 98,
            views = 980,
            downloads = 390,
            description = "Detailed chemical structures, beta-lactam ring opening mechanisms, clavulanic acid synergy, and generation-wise classification of cephalosporins."
        ),
        ResourceEntity(
            id = "res_pyq_03",
            title = "GPAT 2024 & 2025 Solved Question Paper with Explanations",
            subject = "Pharmacy Examination Prep",
            semester = 7,
            course = "B.Pharm",
            university = "Pharmacy Council Consortium",
            author = "PharmaHub Editorial Board",
            uploadDate = "1 week ago",
            fileType = "PDF",
            fileSize = "8.1 MB",
            tags = "GPAT, PYQ, NIPER, Solved Papers, Mock Test",
            rating = 5.0f,
            reviewCount = 310,
            views = 3400,
            downloads = 1450,
            isBookmarked = true,
            description = "Exhaustive question-by-question analysis, reference textbook citations (Goodman & Gilman, Lachman, Trease & Evans), and high-frequency concepts."
        ),
        ResourceEntity(
            id = "res_ph_04",
            title = "Biopharmaceutics & Pharmacokinetics Lab Practical Manual",
            subject = "Biopharmaceutics",
            semester = 6,
            course = "B.Pharm",
            university = "Faculty of Pharmacy, Delhi",
            author = "Karan Patel (M.Pharm)",
            uploadDate = "2 weeks ago",
            fileType = "DOCX",
            fileSize = "3.1 MB",
            tags = "Practicals, Dissolution, In Vitro, Lab Manual, Record",
            rating = 4.7f,
            reviewCount = 64,
            views = 720,
            downloads = 280,
            description = "Step-by-step laboratory procedures: In-vitro dissolution testing using USP Apparatus I & II, urinary excretion method, and protein binding evaluation."
        ),
        ResourceEntity(
            id = "res_pg_05",
            title = "Pharmacognosy: Extraction & TLC Profiling of Tropane Alkaloids",
            subject = "Pharmacognosy",
            semester = 3,
            course = "B.Pharm",
            university = "University Department of Pharmaceutical Sciences",
            author = "Meera Deshmukh",
            uploadDate = "3 weeks ago",
            fileType = "PPTX",
            fileSize = "12.4 MB",
            tags = "Pharmacognosy, Alkaloids, TLC, Atropa Belladonna, Phytochemistry",
            rating = 4.6f,
            reviewCount = 45,
            views = 560,
            downloads = 195,
            description = "High-resolution botanical micrographs, chemical test color reactions (Vitali-Morin test), and TLC solvent systems for Belladonna, Hyoscyamus and Datura."
        ),
        ResourceEntity(
            id = "res_ce_06",
            title = "Clinical Pharmacy: Therapeutic Drug Monitoring (TDM) of Digoxin & Aminoglycosides",
            subject = "Pharmacy Practice",
            semester = 7,
            course = "Pharm.D",
            university = "Manipal College of Pharmaceutical Sciences",
            author = "Dr. Rohan Varma (Pharm.D)",
            uploadDate = "1 month ago",
            fileType = "PDF",
            fileSize = "5.5 MB",
            tags = "Clinical Pharmacy, TDM, Digoxin, Nephrotoxicity, Hospital Pharmacy",
            rating = 4.9f,
            reviewCount = 112,
            views = 1430,
            downloads = 620,
            description = "Clinical dosing protocols, trough level monitoring, Cockcroft-Gault creatinine clearance formula, and management of digitalis toxicity."
        )
    )

    val sampleDrugs = listOf(
        DrugEntity(
            id = "drug_metformin",
            name = "Metformin Hydrochloride",
            genericName = "Metformin",
            brandNames = "Glucophage, Glycomet, Fortamet",
            drugClass = "Biguanide Antidiabetic",
            mechanismOfAction = "Activates AMP-activated protein kinase (AMPK), decreasing hepatic gluconeogenesis and glycogenolysis while improving peripheral insulin sensitivity and glucose uptake.",
            indications = "First-line pharmacotherapy for Type 2 Diabetes Mellitus; off-label for Polycystic Ovary Syndrome (PCOS).",
            contraindications = "Severe renal impairment (eGFR < 30 mL/min/1.73m²), metabolic acidosis, acute hypoxemic conditions (decompensated heart failure, sepsis).",
            adverseEffects = "Gastrointestinal distress (nausea, diarrhea, metallic taste), Vitamin B12 deficiency with chronic therapy; rare but fatal Lactic Acidosis.",
            interactions = "Iodinated radiocontrast agents (withhold 48h prior/post to avoid acute kidney injury), Cimetidine (competes for renal tubular organic cation transporters).",
            dosageForms = "Immediate-release tablets (500mg, 850mg, 1000mg), Extended-release tablets (500mg, 750mg, 1000mg), Oral solution.",
            storageInstructions = "Store below 25°C (77°F). Protect from excessive moisture and light.",
            pregnancyCategory = "Category B",
            halfLife = "4.0 to 8.7 hours",
            highYieldGpatFacts = "Does NOT stimulate insulin secretion; hence does not cause hypoglycemia or weight gain. Eliminated 90% unchanged by renal tubular secretion."
        ),
        DrugEntity(
            id = "drug_atorvastatin",
            name = "Atorvastatin Calcium",
            genericName = "Atorvastatin",
            brandNames = "Lipitor, Atorva, Storvas",
            drugClass = "HMG-CoA Reductase Inhibitor (Statin)",
            mechanismOfAction = "Competitive, reversible inhibition of 3-hydroxy-3-methylglutaryl-coenzyme A (HMG-CoA) reductase, the rate-limiting enzyme in cholesterol synthesis, upregulating hepatic LDL receptors.",
            indications = "Primary and secondary hypercholesterolemia, prevention of cardiovascular atherosclerotic disease, post-myocardial infarction.",
            contraindications = "Active liver disease, unexplained persistent elevations in serum transaminases, pregnancy and lactation.",
            adverseEffects = "Myopathy, rhabdomyolysis (elevated CK), hepatotoxicity, new-onset diabetes, dyspepsia.",
            interactions = "CYP3A4 inhibitors (Clarithromycin, Ketoconazole, Grapefruit juice) drastically increase plasma levels; Gemfibrozil increases rhabdomyolysis risk.",
            dosageForms = "Oral tablets (10mg, 20mg, 40mg, 80mg).",
            storageInstructions = "Store at 20°C to 25°C. Keep tightly closed.",
            pregnancyCategory = "Category X (Teratogenic)",
            halfLife = "14 hours (active metabolites have half-life of 20-30 hours)",
            highYieldGpatFacts = "Pleiotropic effects: improves endothelial function, stabilizes atherosclerotic plaques, reduces vascular inflammation. Has longest duration of action among common statins."
        ),
        DrugEntity(
            id = "drug_omeprazole",
            name = "Omeprazole",
            genericName = "Omeprazole",
            brandNames = "Prilosec, Omez, Losec",
            drugClass = "Proton Pump Inhibitor (PPI)",
            mechanismOfAction = "Irreversibly inhibits the H+/K+ ATPase enzyme system in gastric parietal cells, blocking the final common pathway of gastric acid secretion.",
            indications = "Gastroesophageal Reflux Disease (GERD), Peptic Ulcer Disease, Zollinger-Ellison Syndrome, H. pylori eradication (part of triple regimen).",
            contraindications = "Known hypersensitivity to substituted benzimidazoles; co-administration with Rilpivirine.",
            adverseEffects = "Headache, diarrhea, increased risk of Clostridium difficile colitis, hypomagnesemia, osteoporotic fractures with long-term use.",
            interactions = "Inhibits CYP2C19: decreases activation of Clopidogrel (prodrug); reduces absorption of Ketoconazole, Iron, and Atazanavir.",
            dosageForms = "Delayed-release capsules (10mg, 20mg, 40mg), IV powder for injection.",
            storageInstructions = "Store in tightly closed container at room temperature. Protect from light and humidity.",
            pregnancyCategory = "Category C",
            halfLife = "0.5 to 1 hour (biological effect persists for 24-72 hours due to covalent binding)",
            highYieldGpatFacts = "Prodrug activated only in acidic canaliculi (pH < 2) of parietal cells. Enteric coating is required to prevent degradation in gastric acid."
        ),
        DrugEntity(
            id = "drug_amoxicillin",
            name = "Amoxicillin Trihydrate",
            genericName = "Amoxicillin",
            brandNames = "Amoxil, Mox, Novamox",
            drugClass = "Aminopenicillin (Beta-Lactam Antibiotic)",
            mechanismOfAction = "Binds to bacterial penicillin-binding proteins (PBPs), inhibiting the transpeptidation step of peptidoglycan synthesis, resulting in bacterial cell wall lysis.",
            indications = "Otitis media, sinusitis, community-acquired pneumonia, urinary tract infections, dental abscesses; combined with Clavulanic acid for beta-lactamase producers.",
            contraindications = "Severe IgE-mediated penicillin allergy (anaphylaxis); caution in infectious mononucleosis (causes maculopapular rash).",
            adverseEffects = "Nausea, diarrhea, antibiotic-associated colitis, hypersensitivity skin rashes.",
            interactions = "Probenecid reduces renal tubular secretion; Methotrexate toxicity risk increased; may reduce efficacy of oral contraceptives.",
            dosageForms = "Capsules (250mg, 500mg), Oral suspension (125mg/5mL, 250mg/5mL), Dispersible tablets.",
            storageInstructions = "Dry powder at 20°-25°C. Reconstituted oral suspension refrigerated for up to 14 days.",
            pregnancyCategory = "Category B",
            halfLife = "1 to 1.5 hours",
            highYieldGpatFacts = "Better oral bioavailability (75-90%) than Ampicillin and food does not significantly interfere with its absorption."
        ),
        DrugEntity(
            id = "drug_lisinopril",
            name = "Lisinopril Dihydrate",
            genericName = "Lisinopril",
            brandNames = "Zestril, Prinivil, Listril",
            drugClass = "Angiotensin-Converting Enzyme (ACE) Inhibitor",
            mechanismOfAction = "Competitively inhibits ACE, reducing conversion of Angiotensin I to Angiotensin II, lowering aldosterone secretion and preventing bradykinin degradation.",
            indications = "Hypertension, Heart Failure with reduced ejection fraction (HFrEF), post-Myocardial Infarction, diabetic nephropathy.",
            contraindications = "History of ACEI-induced angioedema, bilateral renal artery stenosis, co-administration with Aliskiren in diabetics, pregnancy.",
            adverseEffects = "Dry persistent cough (bradykinin & substance P accumulation), hyperkalemia, acute renal impairment, angioedema.",
            interactions = "Potassium-sparing diuretics (Spironolactone) & K+ supplements (hyperkalemia danger); NSAIDs blunt hypotensive response.",
            dosageForms = "Tablets (2.5mg, 5mg, 10mg, 20mg, 40mg).",
            storageInstructions = "Store between 15°C and 30°C. Protect from moisture.",
            pregnancyCategory = "Category D (Black Box Warning: Fetotoxic)",
            halfLife = "12 hours (once-daily dosing)",
            highYieldGpatFacts = "Hydrophilic and excreted unchanged in urine; NOT a prodrug (unlike Enalapril, Ramipril) and does not require hepatic ester hydrolysis."
        ),
        DrugEntity(
            id = "drug_salbutamol",
            name = "Salbutamol (Albuterol) Sulfate",
            genericName = "Salbutamol / Albuterol",
            brandNames = "Ventolin, Asthalin, ProAir",
            drugClass = "Short-Acting Beta-2 Adrenergic Agonist (SABA)",
            mechanismOfAction = "Selective beta-2 receptor agonist: stimulates adenylyl cyclase, elevating intracellular cAMP, relaxing bronchial smooth muscle.",
            indications = "Relief of acute bronchospasm in bronchial asthma, COPD, and exercise-induced bronchoconstriction.",
            contraindications = "Hypersensitivity to salbutamol or fluorocarbons in inhalers.",
            adverseEffects = "Fine skeletal muscle tremors, tachycardia, palpitations, hypokalemia (due to intracellular K+ shift), nervousness.",
            interactions = "Non-selective beta-blockers (Propranolol) antagonize bronchodilation and precipitate bronchospasm; Loop diuretics may worsen hypokalemia.",
            dosageForms = "Metered Dose Inhaler (100 mcg/puff), Nebulizer solution, Tablets, Syrup.",
            storageInstructions = "Store inhaler between 15°C and 25°C. Avoid direct sunlight and do not puncture or incinerate canister.",
            pregnancyCategory = "Category C",
            halfLife = "3.8 to 6 hours",
            highYieldGpatFacts = "Rescue drug of choice for acute asthma attacks. Has a rapid onset of 5 minutes and duration of 3 to 6 hours."
        )
    )

    val sampleDecks = listOf(
        FlashcardDeckEntity(
            id = "deck_pharmacokinetics",
            title = "High-Yield Pharmacokinetics & ADME",
            subject = "Pharmacology",
            semester = 4,
            cardCount = 6,
            masteredCount = 4,
            colorHex = 0xFF00E5A3,
            iconName = "speed"
        ),
        FlashcardDeckEntity(
            id = "deck_medchem_sar",
            title = "Medicinal Chemistry SAR & Functional Groups",
            subject = "Medicinal Chemistry",
            semester = 5,
            cardCount = 5,
            masteredCount = 2,
            colorHex = 0xFF38BDF8,
            iconName = "biotech"
        ),
        FlashcardDeckEntity(
            id = "deck_gpat_calculations",
            title = "Pharmaceutical Calculations & Formulas",
            subject = "Pharmaceutics",
            semester = 2,
            cardCount = 5,
            masteredCount = 3,
            colorHex = 0xFFF59E0B,
            iconName = "calculate"
        ),
        FlashcardDeckEntity(
            id = "deck_autonomic",
            title = "Autonomic Pharmacology & Receptors",
            subject = "Pharmacology",
            semester = 4,
            cardCount = 5,
            masteredCount = 1,
            colorHex = 0xFFEC4899,
            iconName = "psychology"
        )
    )

    val sampleCards = listOf(
        FlashcardEntity(
            id = "card_01",
            deckId = "deck_pharmacokinetics",
            subject = "Pharmacology",
            frontText = "What is the formula for Volume of Distribution (Vd) and its clinical significance?",
            backText = "Vd = Dose / C0 (where C0 is initial plasma concentration). A very high Vd (> 42 L) indicates extensive tissue binding and low plasma availability (e.g., Chloroquine Vd ~ 15,000 L).",
            formulaOrStructure = "Vd = Amount of drug in body / Plasma concentration (Cp)",
            clinicalPearls = "Drugs with high protein binding (e.g. Warfarin) stay in vascular compartment; Vd ~ 5-7 L.",
            boxLevel = 3,
            difficulty = "Medium"
        ),
        FlashcardEntity(
            id = "card_02",
            deckId = "deck_pharmacokinetics",
            subject = "Pharmacology",
            frontText = "How do you calculate Total Body Clearance (Cl) and Elimination Half-life (t1/2)?",
            backText = "Clearance = Rate of elimination / Cp, or Cl = Vd × Kel. \nElimination Half-life: t1/2 = (0.693 × Vd) / Cl.",
            formulaOrStructure = "t1/2 = 0.693 / Kel = (0.693 × Vd) / Cl",
            clinicalPearls = "Takes ~4 to 5 half-lives to reach steady-state (Css) and to completely eliminate a drug.",
            boxLevel = 4,
            difficulty = "Hard"
        ),
        FlashcardEntity(
            id = "card_03",
            deckId = "deck_pharmacokinetics",
            subject = "Pharmacology",
            frontText = "What defines Zero-Order vs First-Order Kinetics?",
            backText = "First-Order: A constant fraction of drug eliminated per unit time (Rate ∝ Cp). t1/2 is constant.\nZero-Order: A constant amount of drug eliminated per unit time due to enzyme saturation (e.g., Ethanol, Phenytoin, Aspirin at high doses). t1/2 increases with dose.",
            formulaOrStructure = "1st Order: dC/dt = -Kel × C | 0-Order: dC/dt = -K0",
            clinicalPearls = "Zero-order drugs can rapidly build up to toxic levels because elimination capacity is overwhelmed.",
            boxLevel = 2,
            difficulty = "Medium"
        ),
        FlashcardEntity(
            id = "card_04",
            deckId = "deck_pharmacokinetics",
            subject = "Pharmacology",
            frontText = "What is Bioavailability (F) and how is it calculated from AUC?",
            backText = "The fraction of an administered dose of unchanged drug that reaches systemic circulation.\nF = (AUC_oral × Dose_iv) / (AUC_iv × Dose_oral).",
            formulaOrStructure = "Absolute Bioavailability: F = AUC_po / AUC_iv (when doses are equal)",
            clinicalPearls = "IV route by definition has 100% bioavailability (F = 1.0).",
            boxLevel = 5,
            difficulty = "Easy"
        ),
        FlashcardEntity(
            id = "card_05",
            deckId = "deck_medchem_sar",
            subject = "Medicinal Chemistry",
            frontText = "What is the essential pharmacophore of Beta-Lactam antibiotics?",
            backText = "A 4-membered cyclic amide ring (Beta-lactam ring) fused to a 5-membered thiazolidine ring (Penicillins) or a 6-membered dihydrothiazine ring (Cephalosporins). The free carboxylic acid group at C-3/C-4 is mandatory for PBP receptor binding.",
            formulaOrStructure = "Beta-lactam core: C(=O)-NH strained ring mimics D-Ala-D-Ala",
            clinicalPearls = "Beta-lactamase enzymes hydrolyze the amide bond, inactivating the antibiotic.",
            boxLevel = 3,
            difficulty = "Medium"
        ),
        FlashcardEntity(
            id = "card_06",
            deckId = "deck_gpat_calculations",
            subject = "Pharmaceutics",
            frontText = "How is Young's Rule and Dilling's Rule formulated for pediatric doses?",
            backText = "Young's Rule (Child > 1 to 12 years): \nPediatric Dose = [Age in years / (Age in years + 12)] × Adult Dose.\n\nDilling's Rule (Child 4 to 20 years): \nPediatric Dose = [Age in years / 20] × Adult Dose.",
            formulaOrStructure = "Young: Age / (Age + 12) | Dilling: Age / 20 | Clark: Weight (lbs) / 150",
            clinicalPearls = "Body Surface Area (BSA) method is the most accurate for pediatric dosing (Dose = BSA / 1.73m² × Adult Dose).",
            boxLevel = 3,
            difficulty = "Easy"
        )
    )

    val sampleQuizzes = listOf(
        QuizEntity(
            id = "quiz_pharmacology_gpat",
            title = "GPAT Rapid Fire: Clinical Pharmacology & ADME",
            subject = "Pharmacology",
            semester = 5,
            questionCount = 5,
            durationMinutes = 10,
            difficulty = "Hard",
            highYieldTopic = "Clearance, Receptor Subtypes & Antidotes",
            bestScore = 80,
            totalAttempts = 2
        ),
        QuizEntity(
            id = "quiz_pharmaceutics_dosage",
            title = "Pharmaceutics: Solid Dosage Forms & Dissolution",
            subject = "Pharmaceutics",
            semester = 3,
            questionCount = 4,
            durationMinutes = 8,
            difficulty = "Medium",
            highYieldTopic = "Tablet Excipients, Disintegration & USP Testing",
            bestScore = 100,
            totalAttempts = 1
        ),
        QuizEntity(
            id = "quiz_medicinal_chemistry",
            title = "Medicinal Chemistry: Antibacterials & Antivirals",
            subject = "Medicinal Chemistry",
            semester = 6,
            questionCount = 4,
            durationMinutes = 8,
            difficulty = "Hard",
            highYieldTopic = "SAR, Pro-drugs & Resistance Mechanisms",
            bestScore = null,
            totalAttempts = 0
        )
    )

    val sampleQuizQuestions = listOf(
        QuizQuestionEntity(
            id = "q_01",
            quizId = "quiz_pharmacology_gpat",
            questionText = "Which of the following parameters remains CONSTANT regardless of dose in a drug following First-Order kinetics?",
            options = "Elimination Rate|Clearance & Elimination Half-life|Area Under the Curve (AUC)|Peak plasma concentration (Cmax)",
            correctIndex = 1,
            explanation = "In first-order kinetics, clearance and elimination half-life (t1/2 = 0.693 / Kel) are independent of dose. However, the elimination rate is directly proportional to plasma concentration.",
            subject = "Pharmacology"
        ),
        QuizQuestionEntity(
            id = "q_02",
            quizId = "quiz_pharmacology_gpat",
            questionText = "What is the specific antidote for Acetaminophen (Paracetamol) overdose, and its mechanism?",
            options = "N-Acetylcysteine; replenishes hepatic glutathione reserves|Naloxone; competitive mu-opioid receptor antagonist|Flumazenil; GABA-A receptor antagonist|Pralidoxime; reactivates acetylcholinesterase",
            correctIndex = 0,
            explanation = "N-acetylcysteine (NAC) acts as a sulfhydryl donor to replenish hepatic glutathione stores, neutralizing the toxic reactive metabolite NAPQI (N-acetyl-p-benzoquinone imine).",
            subject = "Pharmacology"
        ),
        QuizQuestionEntity(
            id = "q_03",
            quizId = "quiz_pharmacology_gpat",
            questionText = "A patient with hypertension and Type 2 Diabetes develops a persistent dry nocturnal cough. Which drug is the most probable culprit?",
            options = "Losartan|Amlodipine|Enalapril|Hydrochlorothiazide",
            correctIndex = 2,
            explanation = "Enalapril is an ACE inhibitor. Inhibiting ACE prevents the degradation of bradykinin and substance P in bronchial tissues, precipitating a dry intractable cough in 5-20% of patients.",
            subject = "Pharmacology"
        ),
        QuizQuestionEntity(
            id = "q_04",
            quizId = "quiz_pharmacology_gpat",
            questionText = "Which receptor subtype is selectively targeted by Dobutamine for inotropic support in acute cardiogenic shock?",
            options = "Alpha-1 adrenergic|Beta-1 adrenergic|Beta-2 adrenergic|Dopamine D1",
            correctIndex = 1,
            explanation = "Dobutamine is a relatively selective Beta-1 adrenergic agonist with potent positive inotropic action (increasing myocardial contractility) with minimal chronotropic and vasoconstrictive effects.",
            subject = "Pharmacology"
        ),
        QuizQuestionEntity(
            id = "q_05",
            quizId = "quiz_pharmacology_gpat",
            questionText = "Which antiepileptic agent displays non-linear (Michaelis-Menten) zero-order kinetics at therapeutic serum levels?",
            options = "Sodium Valproate|Phenytoin|Carbamazepine|Levetiracetam",
            correctIndex = 1,
            explanation = "Phenytoin saturates hepatic CYP2C9 and CYP2C19 enzymes within the normal therapeutic window (10-20 mcg/mL). Small dosage increments can lead to exponential surges in plasma concentration and toxicity.",
            subject = "Pharmacology"
        ),
        QuizQuestionEntity(
            id = "q_p1",
            quizId = "quiz_pharmaceutics_dosage",
            questionText = "According to USP, Apparatus 1 and Apparatus 2 for dissolution testing are respectively:",
            options = "Rotating Basket and Flow-Through Cell|Rotating Basket and Paddle Apparatus|Paddle Apparatus and Reciprocating Cylinder|Paddle over Disc and Rotating Cylinder",
            correctIndex = 1,
            explanation = "USP Apparatus 1 is the Rotating Basket apparatus (commonly 100 rpm, 40-mesh), and USP Apparatus 2 is the Paddle apparatus (commonly 50 rpm).",
            subject = "Pharmaceutics"
        ),
        QuizQuestionEntity(
            id = "q_p2",
            quizId = "quiz_pharmaceutics_dosage",
            questionText = "Which superdisintegrant works predominantly by high water uptake and rapid swelling without forming a gel?",
            options = "Starch 1500|Sodium Starch Glycolate (Explotab)|Lactose monohydrate|Magnesium Stearate",
            correctIndex = 1,
            explanation = "Sodium starch glycolate swells 200-300% in volume upon contact with water, rapidly rupturing tablet cores without developing excessive gelatinous resistance.",
            subject = "Pharmaceutics"
        )
    )

    val samplePosts = listOf(
        CommunityPostEntity(
            id = "post_01",
            postType = "QUESTION",
            title = "Why does Propranolol precipitate bronchospasm in Asthmatic patients?",
            body = "In our Pharmacology lecture today, the professor emphasized that non-selective beta-blockers are strictly contraindicated in reactive airway disease. Could someone explain the precise molecular mechanism of bronchial smooth muscle contraction when Beta-2 is blocked?",
            authorName = "Aarav Sharma",
            authorRole = "B.Pharm 5th Sem",
            authorUniversity = "Jamia Hamdard College of Pharmacy",
            timestamp = "1 hour ago",
            tags = "Pharmacology, Beta-Blockers, Asthma, Bronchospasm, GPAT",
            upvotes = 34,
            isUpvoted = false,
            commentsCount = 8
        ),
        CommunityPostEntity(
            id = "post_02",
            postType = "STUDY_TIP",
            title = "High-Yield Mnemonic: Antiarrhythmic Drugs Vaughan Williams Classification",
            body = "Remember 'No Bad Boy Keeps Clean':\nClass I: Na+ channel blockers (1A: Double Quarter Pounder = Disopyramide, Quinidine, Procainamide)\nClass II: Beta blockers\nClass III: K+ channel blockers (Amiodarone, Sotalol)\nClass IV: Calcium channel blockers (Verapamil, Diltiazem)\nSaved me 12 marks on the recent university exam!",
            authorName = "Sneha Kulkarni",
            authorRole = "Pharm.D Final Year",
            authorUniversity = "Manipal College of Pharmaceutical Sciences",
            timestamp = "3 hours ago",
            tags = "Mnemonics, High-Yield, Cardiology, Pharmacology, Exams",
            upvotes = 128,
            isUpvoted = true,
            commentsCount = 22
        ),
        CommunityPostEntity(
            id = "post_03",
            postType = "POLL",
            title = "Which Pharmacy subject is the most challenging for GPAT 2026 preparation?",
            body = "Vote and share your primary strategy for tackling this subject in the comments.",
            authorName = "PharmaHub Academic Bot",
            authorRole = "Verified Community Lead",
            authorUniversity = "PharmaHub Network",
            timestamp = "6 hours ago",
            tags = "GPAT, Poll, StudyStrategy, BPharm",
            upvotes = 89,
            commentsCount = 37,
            pollOptionsJson = "Pharmacology;Medicinal Chemistry;Pharmaceutics;Pharmacognosy",
            pollVotesJson = "45;32;18;12"
        ),
        CommunityPostEntity(
            id = "post_04",
            postType = "RESOURCE",
            title = "Shared: Flowchart for Identification of Glycosides & Alkaloids (TLC & Chemical Tests)",
            body = "Created an editable cheat sheet with Borntrager test for Anthraquinones, Keller-Kiliani for Cardiac glycosides, Mayer/Wagner/Dragendorff for Alkaloids. Feel free to use for your lab practicals!",
            authorName = "Rishi Pandit",
            authorRole = "Pharm.D Scholar",
            authorUniversity = "National College of Pharmacy",
            timestamp = "Yesterday",
            tags = "Pharmacognosy, LabCheatSheet, Alkaloids, PracticalRecords",
            upvotes = 95,
            isUpvoted = true,
            commentsCount = 14
        )
    )

    val sampleChatMessages = listOf(
        ChatMessageEntity(
            id = "chat_01",
            roomId = "group_pharmacology",
            senderName = "Dr. Ananya Ray",
            senderRole = "Pharmacology Educator",
            messageText = "Welcome to the Pharmacology Revision Room! Today we are discussing Renal Clearance vs Hepatic First-Pass Metabolism.",
            timestamp = "10:15 AM",
            isSelf = false
        ),
        ChatMessageEntity(
            id = "chat_02",
            roomId = "group_pharmacology",
            senderName = "Aarav Sharma",
            senderRole = "Student",
            messageText = "Hello Professor! What is the cutoff for extraction ratio (ER) when considering high vs low hepatic clearance?",
            timestamp = "10:17 AM",
            isSelf = false
        ),
        ChatMessageEntity(
            id = "chat_03",
            roomId = "group_pharmacology",
            senderName = "Dr. Ananya Ray",
            senderRole = "Pharmacology Educator",
            messageText = "Great question! Drugs with ER > 0.7 (like Lidocaine, Morphine, Propranolol) are flow-limited. Drugs with ER < 0.3 (Warfarin, Diazepam) are capacity-limited and dependent on enzyme induction/inhibition.",
            timestamp = "10:20 AM",
            isSelf = false,
            reactionEmoji = "💡"
        ),
        ChatMessageEntity(
            id = "chat_04",
            roomId = "group_pharmacology",
            senderName = "Rishi Pandit",
            senderRole = "Pharm.D Scholar",
            messageText = "That explains why Lidocaine cannot be administered orally due to extensive first-pass loss!",
            timestamp = "10:22 AM",
            isSelf = true
        )
    )

    val sampleAnalytics = StudyAnalyticsEntity(
        id = "current_user_stats",
        streakDays = 8,
        totalStudyHours = 42.0f,
        xpEarned = 1850,
        userLevel = 6,
        quizzesCompleted = 32,
        quizAccuracyPercent = 86,
        flashcardsMastered = 184,
        resourcesShared = 14,
        weakArea = "Pharmacokinetics (Clearance & Vd)",
        recommendedTopic = "Renal Clearance and First-Pass Metabolism"
    )

    val sampleUsers = emptyList<UserEntity>()

    val samplePendingResources = emptyList<ResourceEntity>()

    val sampleReports = emptyList<ResourceReportEntity>()

    val sampleAuditLogs = emptyList<AuditLogEntity>()

    val sampleSystemSettings = AppSystemSettingsEntity(
        id = "global_settings",
        maintenanceMode = false,
        maintenanceNotice = "PharmaHub is undergoing scheduled infrastructure upgrades. Student services will resume shortly.",
        allowRegistration = true,
        maxUploadSizeBytes = 52428800L,
        allowedFileExtensions = "pdf,docx,pptx,txt",
        aiDailyLimitPerUser = 40,
        aiServiceEnabled = true,
        downloadsEnabled = true
    )
}

