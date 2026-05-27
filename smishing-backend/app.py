# from flask import Flask, request, jsonify
# from flask_cors import CORS

# app = Flask(__name__)
# CORS(app)

# with open("signatures.txt", "r") as f:
#     signatures = set(line.strip().lower() for line in f if line.strip())

# @app.route("/predict", methods=["POST"])
# def predict():
#     try:
#         data = request.get_json()
#         message = data.get("message", "").lower()
#         matched_keywords = [kw for kw in signatures if kw in message]
#         is_spam = bool(matched_keywords)

#         return jsonify({
#             "spam": is_spam,
#             "reason": f"Matched keywords: {', '.join(matched_keywords)}" if is_spam else "No suspicious keywords found"
#         }), 200

#     except Exception as e:
#         return jsonify({
#             "spam": False,
#             "reason": f"Error: {str(e)}"
#         }), 500

# if __name__ == "__main__":
#     app.run(host="0.0.0.0", port=5000, debug=True)



from flask import Flask, request, jsonify
from flask_cors import CORS
import joblib

app = Flask(__name__)
CORS(app)

# Load signature keywords
with open("signatures.txt", "r") as f:
    signatures = set(line.strip().lower() for line in f if line.strip())

# Load saved ML model and vectorizer
model = joblib.load("random_forest_spam_model.pkl")
vectorizer = joblib.load("tfidf_vectorizer.pkl")

@app.route("/predict", methods=["POST"])
def predict():
    try:
        data = request.get_json()
        message = data.get("message", "").lower()

        # Signature-based detection
        matched_keywords = [kw for kw in signatures if kw in message]
        if matched_keywords:
            return jsonify({
                "spam": True,
                "method": "signature",
                "reason": f"Matched keywords: {', '.join(matched_keywords)}"
            }), 200

        # ML-based detection (fallback)
        vector = vectorizer.transform([message])
        prediction = model.predict(vector)[0]
        is_spam = bool(prediction)

        return jsonify({
            "spam": is_spam,
            "method": "model",
            "reason": "Predicted by ML model"
        }), 200

    except Exception as e:
        return jsonify({
            "spam": False,
            "reason": f"Error: {str(e)}"
        }), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
