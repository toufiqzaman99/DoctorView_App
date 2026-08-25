package com.doctorview.app.utils;

import com.doctorview.app.models.Condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Rule-based symptom analyzer (no machine learning).
 *
 * Matches the user's selected symptoms against a small, hand-written
 * rule base of common conditions and returns the best matches,
 * ranked by how many symptoms overlap.
 *
 * Educational only — always recommend consulting a real doctor.
 */
public final class SymptomAnalyzer {

    // ===== The symptoms the user can select =====
    public static final String S_FEVER = "Fever";
    public static final String S_HEADACHE = "Headache";
    public static final String S_COUGH = "Cough";
    public static final String S_SORE_THROAT = "Sore Throat";
    public static final String S_RUNNY_NOSE = "Runny Nose";
    public static final String S_SNEEZING = "Sneezing";
    public static final String S_BODY_ACHES = "Body Aches";
    public static final String S_FATIGUE = "Fatigue";
    public static final String S_NAUSEA = "Nausea";
    public static final String S_VOMITING = "Vomiting";
    public static final String S_DIARRHEA = "Diarrhea";
    public static final String S_STOMACH_PAIN = "Stomach Pain";
    public static final String S_LOSS_OF_APPETITE = "Loss of Appetite";
    public static final String S_CHEST_PAIN = "Chest Pain";
    public static final String S_SHORTNESS_OF_BREATH = "Shortness of Breath";
    public static final String S_DIZZINESS = "Dizziness";
    public static final String S_RASH = "Rash";
    public static final String S_ITCHING = "Itching";
    public static final String S_FREQUENT_URINATION = "Frequent Urination";
    public static final String S_EXCESSIVE_THIRST = "Excessive Thirst";
    public static final String S_BURNING_URINATION = "Burning Urination";
    public static final String S_LOSS_OF_SMELL_TASTE = "Loss of Smell or Taste";
    public static final String S_BLURRED_VISION = "Blurred Vision";

    public static final String[] ALL_SYMPTOMS = {
            S_FEVER, S_HEADACHE, S_COUGH, S_SORE_THROAT, S_RUNNY_NOSE, S_SNEEZING,
            S_BODY_ACHES, S_FATIGUE, S_NAUSEA, S_VOMITING, S_DIARRHEA, S_STOMACH_PAIN,
            S_LOSS_OF_APPETITE, S_CHEST_PAIN, S_SHORTNESS_OF_BREATH, S_DIZZINESS,
            S_RASH, S_ITCHING, S_FREQUENT_URINATION, S_EXCESSIVE_THIRST,
            S_BURNING_URINATION, S_LOSS_OF_SMELL_TASTE, S_BLURRED_VISION
    };

    /** A condition matched to the user's symptoms, with the overlap count. */
    public static class Match {
        private final Condition condition;
        private final int matchedCount;

        Match(Condition condition, int matchedCount) {
            this.condition = condition;
            this.matchedCount = matchedCount;
        }

        public Condition getCondition() {
            return condition;
        }

        public int getMatchedCount() {
            return matchedCount;
        }

        /** Percentage of the condition's typical symptoms the user selected. */
        public int getPercent() {
            return (int) Math.round(100.0 * matchedCount / condition.getSymptoms().size());
        }
    }

    // ===== The rule base: common conditions and their typical symptoms =====
    private static final List<Condition> CONDITIONS = new ArrayList<>();

    static {
        CONDITIONS.add(new Condition("Common Cold",
                "Rest, drink warm fluids and use decongestants. See a doctor if symptoms last more than 10 days.",
                Condition.SEVERITY_LOW,
                S_RUNNY_NOSE, S_SNEEZING, S_SORE_THROAT, S_COUGH, S_HEADACHE));

        CONDITIONS.add(new Condition("Flu (Influenza)",
                "Rest, drink plenty of fluids and take paracetamol for fever. See a doctor within 48 hours if the fever is high.",
                Condition.SEVERITY_MEDIUM,
                S_FEVER, S_BODY_ACHES, S_FATIGUE, S_HEADACHE, S_COUGH, S_SORE_THROAT));

        CONDITIONS.add(new Condition("COVID-19",
                "Self-isolate, get tested and monitor your oxygen level. Seek medical care if breathing gets worse.",
                Condition.SEVERITY_HIGH,
                S_FEVER, S_COUGH, S_SHORTNESS_OF_BREATH, S_FATIGUE, S_LOSS_OF_SMELL_TASTE, S_HEADACHE));

        CONDITIONS.add(new Condition("Migraine",
                "Rest in a dark, quiet room and stay hydrated. Consult a doctor if migraines are frequent.",
                Condition.SEVERITY_MEDIUM,
                S_HEADACHE, S_NAUSEA, S_DIZZINESS, S_FATIGUE));

        CONDITIONS.add(new Condition("Gastroenteritis (Stomach Flu)",
                "Drink oral rehydration solution (ORS) and eat light food. See a doctor if you feel dehydrated.",
                Condition.SEVERITY_MEDIUM,
                S_NAUSEA, S_VOMITING, S_DIARRHEA, S_STOMACH_PAIN, S_FEVER));

        CONDITIONS.add(new Condition("Food Poisoning",
                "Stay hydrated and avoid solid food until vomiting stops. Seek care if symptoms are severe.",
                Condition.SEVERITY_MEDIUM,
                S_NAUSEA, S_VOMITING, S_DIARRHEA, S_STOMACH_PAIN, S_FATIGUE));

        CONDITIONS.add(new Condition("Gastritis / Acid Reflux",
                "Avoid spicy, oily food, coffee and late meals. Antacids may help. See a doctor if pain persists.",
                Condition.SEVERITY_LOW,
                S_STOMACH_PAIN, S_NAUSEA, S_LOSS_OF_APPETITE));

        CONDITIONS.add(new Condition("Urinary Tract Infection",
                "Drink plenty of water and see a doctor for a urine test — antibiotics may be needed.",
                Condition.SEVERITY_MEDIUM,
                S_BURNING_URINATION, S_FREQUENT_URINATION, S_FEVER, S_STOMACH_PAIN));

        CONDITIONS.add(new Condition("Diabetes Warning",
                "Get a fasting blood sugar test soon. Limit sugary food and stay active.",
                Condition.SEVERITY_HIGH,
                S_EXCESSIVE_THIRST, S_FREQUENT_URINATION, S_FATIGUE, S_BLURRED_VISION));

        CONDITIONS.add(new Condition("High Blood Pressure Warning",
                "Check your blood pressure and reduce salt intake. Consult a doctor urgently if you have chest pain.",
                Condition.SEVERITY_HIGH,
                S_HEADACHE, S_DIZZINESS, S_CHEST_PAIN, S_SHORTNESS_OF_BREATH));

        CONDITIONS.add(new Condition("Asthma",
                "Use your prescribed inhaler and avoid triggers. Seek immediate care if breathing is severe.",
                Condition.SEVERITY_HIGH,
                S_SHORTNESS_OF_BREATH, S_COUGH, S_CHEST_PAIN, S_FATIGUE));

        CONDITIONS.add(new Condition("Heart Problem Warning",
                "SEEK EMERGENCY CARE IMMEDIATELY — call emergency services right now.",
                Condition.SEVERITY_EMERGENCY,
                S_CHEST_PAIN, S_SHORTNESS_OF_BREATH, S_DIZZINESS, S_NAUSEA, S_FATIGUE));

        CONDITIONS.add(new Condition("Skin Allergy",
                "Take an antihistamine and avoid the trigger. See a doctor if the rash spreads or swells.",
                Condition.SEVERITY_LOW,
                S_RASH, S_ITCHING, S_SNEEZING));

        CONDITIONS.add(new Condition("Anemia",
                "Eat iron-rich food (leafy greens, meat, lentils) and get a hemoglobin blood test.",
                Condition.SEVERITY_MEDIUM,
                S_FATIGUE, S_DIZZINESS, S_SHORTNESS_OF_BREATH, S_LOSS_OF_APPETITE));

        CONDITIONS.add(new Condition("Dehydration",
                "Drink water or oral rehydration solution immediately and rest in a cool place.",
                Condition.SEVERITY_LOW,
                S_EXCESSIVE_THIRST, S_DIZZINESS, S_FATIGUE, S_HEADACHE));
    }

    private SymptomAnalyzer() {
        // No instances
    }

    /**
     * Returns conditions that share at least 2 symptoms with the selection,
     * best matches first (most overlapping symptoms, then highest percentage).
     */
    public static List<Match> analyze(Collection<String> selectedSymptoms) {
        List<Match> matches = new ArrayList<>();
        for (Condition condition : CONDITIONS) {
            int count = 0;
            for (String symptom : condition.getSymptoms()) {
                if (selectedSymptoms.contains(symptom)) {
                    count++;
                }
            }
            if (count >= 2) {
                matches.add(new Match(condition, count));
            }
        }
        matches.sort((a, b) -> {
            int byCount = Integer.compare(b.getMatchedCount(), a.getMatchedCount());
            if (byCount != 0) {
                return byCount;
            }
            return Integer.compare(b.getPercent(), a.getPercent());
        });
        return matches;
    }
}
