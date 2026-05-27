import joblib

# Load the saved model and vectorizer
model = joblib.load("random_forest_spam_model.pkl")
vectorizer = joblib.load("tfidf_vectorizer.pkl")

# Function to classify message
def classify_message(message):
    message_vec = vectorizer.transform([message])
    prediction = model.predict(message_vec)[0]
    return "SPAM 🚫" if prediction == 1 else "HAM ✅"

# Example: Take user input
user_input = input("Enter a message: ")
result = classify_message(user_input)
print(f"\nPrediction: {result}")
