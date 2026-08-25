package com.doctorview.app.utils;

import com.doctorview.app.models.Disease;
import com.doctorview.app.models.Doctor;
import com.doctorview.app.models.NewsArticle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sample doctors used to fill the Firestore "doctors" collection for a demo.
 * Loaded by the "Load sample doctors" button on the Doctors screen.
 * Photos are free placeholder portraits from randomuser.me.
 */
public final class SampleData {

    private SampleData() {
        // No instances
    }

    public static List<Doctor> getSampleDoctors() {
        List<Doctor> doctors = new ArrayList<>();

        doctors.add(new Doctor(null, "Dr. Ahmed Khan", "Cardiologist", "City Heart Hospital", 15, 1200, 4.8,
                "https://randomuser.me/api/portraits/men/32.jpg",
                "Senior cardiologist with 15 years of experience treating heart diseases, hypertension and performing ECG analysis."));

        doctors.add(new Doctor(null, "Dr. Fatima Rahman", "Dermatologist", "Skin & Beauty Care Center", 8, 800, 4.6,
                "https://randomuser.me/api/portraits/women/44.jpg",
                "Specialist in acne, eczema, hair loss and cosmetic dermatology with a friendly, patient-first approach."));

        doctors.add(new Doctor(null, "Dr. Sarah Ahmed", "Pediatrician", "Children's Care Hospital", 10, 700, 4.9,
                "https://randomuser.me/api/portraits/women/68.jpg",
                "Caring pediatrician focused on child growth, nutrition, vaccination and childhood illnesses."));

        doctors.add(new Doctor(null, "Dr. Mohammad Ali", "Orthopedic Surgeon", "National Medical Center", 18, 1500, 4.7,
                "https://randomuser.me/api/portraits/men/41.jpg",
                "Experienced orthopedic surgeon specializing in fractures, joint pain, sports injuries and rehabilitation."));

        doctors.add(new Doctor(null, "Dr. Ayesha Siddiqua", "Gynecologist", "Women's Health Clinic", 12, 1000, 4.8,
                "https://randomuser.me/api/portraits/women/65.jpg",
                "Trusted gynecologist providing prenatal care, family planning and women's health consultations."));

        doctors.add(new Doctor(null, "Dr. Tanvir Hossain", "Neurologist", "Brain & Spine Institute", 14, 1600, 4.5,
                "https://randomuser.me/api/portraits/men/36.jpg",
                "Neurologist treating migraine, epilepsy, stroke recovery and nerve-related disorders."));

        doctors.add(new Doctor(null, "Dr. Nusrat Jahan", "General Physician", "Community Care Clinic", 6, 500, 4.4,
                "https://randomuser.me/api/portraits/women/79.jpg",
                "General physician for everyday illnesses, fever, diabetes management and preventive check-ups."));

        doctors.add(new Doctor(null, "Dr. Kamal Uddin", "Eye Specialist", "Vision Eye Hospital", 20, 900, 4.9,
                "https://randomuser.me/api/portraits/men/85.jpg",
                "Ophthalmologist with 20 years of experience in cataract surgery, glaucoma and vision correction."));

        return doctors;
    }

    /** Sample diseases for the Disease Information library (demo data). */
    public static List<Disease> getSampleDiseases() {
        List<Disease> diseases = new ArrayList<>();

        diseases.add(new Disease(null, "Common Cold", "Respiratory",
                "A mild viral infection of the nose and throat. Usually clears on its own within 7 to 10 days.",
                Arrays.asList("Runny Nose", "Sneezing", "Sore Throat", "Cough", "Mild Headache"),
                Arrays.asList("Rhinovirus infection", "Close contact with an infected person", "Weakened immunity"),
                Arrays.asList("Wash hands frequently", "Avoid touching your face", "Stay hydrated and get enough rest"),
                Arrays.asList("Rest and warm fluids", "Saline nasal drops", "Paracetamol for aches", "Antibiotics do NOT help")));

        diseases.add(new Disease(null, "Influenza (Flu)", "Respiratory",
                "A contagious viral infection of the airways, stronger than a common cold, with sudden onset of fever and body aches.",
                Arrays.asList("Fever", "Body Aches", "Fatigue", "Cough", "Sore Throat", "Headache"),
                Arrays.asList("Influenza A or B virus", "Droplets from coughing and sneezing"),
                Arrays.asList("Annual flu vaccine", "Hand hygiene", "Avoid crowded places during flu season"),
                Arrays.asList("Rest and plenty of fluids", "Antiviral medication within 48 hours if prescribed", "Paracetamol for fever")));

        diseases.add(new Disease(null, "Dengue Fever", "Infectious",
                "A mosquito-borne viral illness common in tropical regions. Severe cases need hospital care.",
                Arrays.asList("High Fever", "Severe Headache", "Pain Behind Eyes", "Joint and Muscle Pain", "Rash"),
                Arrays.asList("Dengue virus from Aedes mosquito bite", "Breeding of mosquitoes in stagnant water"),
                Arrays.asList("Remove stagnant water around your home", "Use mosquito repellent", "Wear long sleeves", "Use mosquito nets"),
                Arrays.asList("Rest and fluids", "Paracetamol only (avoid aspirin and ibuprofen)", "Hospitalization if warning signs appear")));

        diseases.add(new Disease(null, "Malaria", "Infectious",
                "A life-threatening disease spread by Anopheles mosquitoes, causing recurring fever and chills.",
                Arrays.asList("Fever with Chills", "Sweating", "Headache", "Fatigue", "Nausea"),
                Arrays.asList("Plasmodium parasite via Anopheles mosquito bite"),
                Arrays.asList("Sleep under mosquito nets", "Use repellents", "Antimalarial prophylaxis when traveling to risk areas"),
                Arrays.asList("Prompt antimalarial medication prescribed by a doctor", "Fluids and rest", "Do not self-medicate")));

        diseases.add(new Disease(null, "Typhoid Fever", "Infectious",
                "A bacterial infection from contaminated food or water causing prolonged fever and stomach problems.",
                Arrays.asList("Prolonged Fever", "Stomach Pain", "Headache", "Loss of Appetite", "Diarrhea or Constipation"),
                Arrays.asList("Salmonella typhi bacteria", "Contaminated food or water"),
                Arrays.asList("Drink safe (boiled or bottled) water", "Eat well-cooked food", "Wash hands before eating", "Typhoid vaccine"),
                Arrays.asList("Prescribed antibiotics (complete the full course)", "Hydration", "Soft, easily digestible diet")));

        diseases.add(new Disease(null, "Type 2 Diabetes", "Chronic",
                "A long-term condition where the body cannot use insulin properly, raising blood sugar levels.",
                Arrays.asList("Excessive Thirst", "Frequent Urination", "Fatigue", "Blurred Vision", "Slow-healing Wounds"),
                Arrays.asList("Insulin resistance", "Family history", "Obesity", "Sedentary lifestyle"),
                Arrays.asList("Healthy balanced diet", "Regular exercise", "Maintain a healthy weight", "Regular blood sugar check-ups"),
                Arrays.asList("Lifestyle changes", "Oral medication", "Insulin if prescribed", "Regular blood sugar monitoring")));

        diseases.add(new Disease(null, "Hypertension", "Chronic",
                "High blood pressure — often silent, but over time it damages the heart, brain and kidneys.",
                Arrays.asList("Often No Symptoms", "Headache", "Dizziness", "Blurred Vision", "Chest Pain in Severe Cases"),
                Arrays.asList("Genetics", "High salt intake", "Obesity", "Stress", "Kidney disease"),
                Arrays.asList("Low-salt diet", "Regular exercise", "Weight control", "Stress management", "Limit alcohol"),
                Arrays.asList("Prescribed antihypertensive medication", "Home blood pressure monitoring", "Lifestyle changes")));

        diseases.add(new Disease(null, "Asthma", "Respiratory",
                "A chronic condition where the airways narrow and swell, causing episodes of difficult breathing.",
                Arrays.asList("Wheezing", "Shortness of Breath", "Chest Tightness", "Cough (especially at night)"),
                Arrays.asList("Allergens (dust, pollen)", "Smoke and air pollution", "Cold air", "Exercise", "Respiratory infections"),
                Arrays.asList("Avoid known triggers", "Use controller inhaler as prescribed", "Manage allergies"),
                Arrays.asList("Reliever inhaler during attacks", "Controller medication daily", "Follow your doctor's asthma action plan")));

        diseases.add(new Disease(null, "Migraine", "Neurological",
                "A recurring throbbing headache, often on one side, that may come with nausea and light sensitivity.",
                Arrays.asList("Throbbing Headache", "Nausea", "Sensitivity to Light and Sound", "Visual Aura"),
                Arrays.asList("Stress", "Lack of sleep", "Certain foods (chocolate, cheese)", "Hormonal changes"),
                Arrays.asList("Regular sleep schedule", "Stay hydrated", "Avoid personal triggers", "Stress management"),
                Arrays.asList("Rest in a dark, quiet room", "Pain relief medication", "Prescribed triptans if migraines are frequent")));

        diseases.add(new Disease(null, "Gastritis", "Digestive",
                "Inflammation of the stomach lining causing burning pain, often related to food, medication or infection.",
                Arrays.asList("Burning Stomach Pain", "Nausea", "Bloating", "Loss of Appetite"),
                Arrays.asList("H. pylori infection", "Overuse of painkillers (NSAIDs)", "Alcohol", "Spicy and oily food"),
                Arrays.asList("Regular meals", "Limit spicy, oily food and caffeine", "Avoid smoking and alcohol"),
                Arrays.asList("Antacids", "Acid-reducing medication", "Treat H. pylori infection if present")));

        return diseases;
    }

    /** Sample health articles for the Healthcare News feed (demo data). */
    public static List<NewsArticle> getSampleArticles() {
        List<NewsArticle> articles = new ArrayList<>();

        articles.add(new NewsArticle(null, "10 Simple Habits for a Healthier Heart",
                "Prevention",
                "https://picsum.photos/seed/hearthealth/600/300",
                "2026-08-15",
                "Your heart works for you every second of every day. Taking small, consistent steps can keep it strong for decades: walk at least 30 minutes daily, cut down on fried and salty food, eat more vegetables and fruit, and get 7 to 8 hours of sleep.\n\nAlso, check your blood pressure and cholesterol once a year, even if you feel fine. Heart problems often develop silently, and early detection makes treatment far easier and more effective."));

        articles.add(new NewsArticle(null, "The Truth About Sugar: How Much Is Too Much?",
                "Nutrition",
                "https://picsum.photos/seed/sugartruth/600/300",
                "2026-08-12",
                "Health organizations recommend keeping added sugar below about 25 grams (6 teaspoons) per day for most adults. To put that in perspective, one can of a regular soft drink already contains roughly 35 grams.\n\nHidden sugar hides in ketchup, breakfast cereals, flavored yogurt and even bread. Read nutrition labels and look for words like glucose, fructose, syrup and maltose. Cutting back gradually works better than quitting all at once."));

        articles.add(new NewsArticle(null, "Morning Walk or Evening Walk: Which Is Better?",
                "Fitness",
                "https://picsum.photos/seed/walking/600/300",
                "2026-08-10",
                "The honest answer: the best time to walk is the time you will actually stick to. Morning walks boost mood and set a healthy tone for the day, while evening walks help relieve stress after work and can improve sleep.\n\nWhat matters most is consistency. A brisk 30-minute walk five days a week lowers the risk of heart disease, improves blood sugar control and strengthens bones — regardless of when you do it."));

        articles.add(new NewsArticle(null, "5 Foods That Boost Your Immune System",
                "Nutrition",
                "https://picsum.photos/seed/immunefood/600/300",
                "2026-08-08",
                "No single food can prevent illness, but a steady supply of the right nutrients keeps your immune system ready. Add citrus fruits for vitamin C, garlic and ginger for their natural compounds, yogurt for probiotics, and spinach for vitamins and antioxidants.\n\nCombine these with enough sleep, regular exercise and stress management — the immune system responds to your whole lifestyle, not just your plate."));

        articles.add(new NewsArticle(null, "Managing Stress: Techniques That Actually Work",
                "Mental Health",
                "https://picsum.photos/seed/stressrelief/600/300",
                "2026-08-05",
                "Chronic stress raises blood pressure, weakens immunity and disturbs sleep. Three simple techniques have strong evidence behind them: deep breathing (inhale for 4 seconds, hold for 4, exhale for 6), daily physical activity, and writing down worries before bed to quiet a racing mind.\n\nIf stress feels unmanageable or you feel persistently low, talk to a doctor or counselor. Asking for help is a sign of strength, not weakness."));

        articles.add(new NewsArticle(null, "How Much Water Should You Really Drink?",
                "Lifestyle",
                "https://picsum.photos/seed/waterintake/600/300",
                "2026-08-03",
                "The famous \"8 glasses a day\" rule is a reasonable starting point, but your needs depend on the weather, your activity level and your body. A better guide is the color of your urine: pale yellow means well hydrated.\n\nSip water through the day instead of drinking a lot at once, and remember that tea, milk, soup and juicy fruits also count toward your fluid intake."));

        articles.add(new NewsArticle(null, "The Importance of Regular Health Check-ups",
                "Prevention",
                "https://picsum.photos/seed/checkup/600/300",
                "2026-07-30",
                "Many serious conditions — high blood pressure, diabetes, high cholesterol, even some cancers — cause no symptoms in their early stages. Regular check-ups catch them while they are still easy to manage.\n\nAdults should check blood pressure and blood sugar at least once a year, plus basic screening tests recommended for their age. Prevention is always cheaper and gentler than treatment."));

        articles.add(new NewsArticle(null, "Hand Hygiene: Your First Line of Defense",
                "Hygiene",
                "https://picsum.photos/seed/handwash/600/300",
                "2026-07-28",
                "Washing your hands properly with soap and water for at least 20 seconds removes most germs that cause diarrhea, flu and many other infections. Wash before eating, after using the toilet, and after touching shared surfaces.\n\nWhen soap is unavailable, an alcohol-based hand sanitizer with at least 60% alcohol is a good alternative — but soap and water remain the gold standard."));

        return articles;
    }
}
